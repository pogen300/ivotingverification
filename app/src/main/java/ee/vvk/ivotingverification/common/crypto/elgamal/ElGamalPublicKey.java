package ee.vvk.ivotingverification.common.crypto.elgamal;

import androidx.annotation.NonNull;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Sequence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import ee.vvk.ivotingverification.common.crypto.DecrypterWithRandomness;
import ee.vvk.ivotingverification.common.math.group.Element;
import ee.vvk.ivotingverification.common.math.group.Group;
import ee.vvk.ivotingverification.common.math.group.Scalar;

public final class ElGamalPublicKey implements DecrypterWithRandomness {
    private final String electionID;
    private final Group group;
    private final Element hh;
    private final Element gen;

    /**
     * @param group Group implementation
     * @param pkey  ASN.1 encoded public key
     * @throws IOException if pkey has incorrect ASN.1 structure
     */
    public ElGamalPublicKey(@NonNull final String electionID, @NonNull final Group group, @NonNull final ASN1Sequence pkey) throws IOException {
        Objects.requireNonNull(electionID, "Election ID cannot be null");
        Objects.requireNonNull(group, "ElGamal public key cannot be created from null group");
        Objects.requireNonNull(pkey, "ElGamal public key is null");

        this.electionID = electionID;
        this.group = group;
        this.hh = this.group.ofASN1(pkey.getObjectAt(0).toASN1Primitive().getEncoded());
        this.gen = this.group.generator();
    }

    @Override
    public String getElectionID() {
        return this.electionID;
    }

    @Override
    public boolean checkAsn1Structure(byte[] in) {
        ASN1Sequence res = getAsn1Structure(in);
        return (res != null);
    }

    private ASN1Sequence getAsn1Structure(byte[] in) {
        Objects.requireNonNull(in, "Input cannot be null");

        try (ASN1InputStream bIn = new ASN1InputStream(new ByteArrayInputStream(in))) {
            ASN1Sequence root = (ASN1Sequence) bIn.readObject();

            if (root.size() != 2) {
                return null;
            }

            ASN1Sequence firstSequence = (ASN1Sequence) root.getObjectAt(0);
            ASN1ObjectIdentifier asn1ObjectIdentifier = (ASN1ObjectIdentifier) firstSequence.getObjectAt(0);

            if (!asn1ObjectIdentifier.getId().equals(this.group.oid().getId())) {
                return null;
            }

            if (firstSequence.size() != 1) {
                return null;
            }

            ASN1Sequence secondSequence = (ASN1Sequence) root.getObjectAt(1);
            if (secondSequence.size() != 2) {
                return null;
            }

            return root;
        } catch (IOException | ClassCastException e) {
            return null; // Invalid structure
        }
    }

    @Override
    public String decryptWithRandomness(@NonNull final byte[] ciphertext, @NonNull final byte[] random) throws IOException {
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null");
        Objects.requireNonNull(random, "Randomness cannot be null");

        // Validate and parse ASN.1 structure
        ASN1Sequence root = getAsn1Structure(ciphertext);
        if (root == null) {
            throw new IOException("Invalid ciphertext");
        }

        ASN1Sequence secondSequence = (ASN1Sequence) root.getObjectAt(1);
        byte[] uBlindBytes = secondSequence.getObjectAt(0).toASN1Primitive().getEncoded();
        byte[] vBlindedMsgBytes = secondSequence.getObjectAt(1).toASN1Primitive().getEncoded();

        // Process randomness
        Scalar rr = new Scalar(new BigInteger(1, random), this.group.order());

        // Convert ASN.1 objects to group elements
        Element uBlind = this.group.ofASN1(uBlindBytes);
        Element vBlindedMsg = this.group.ofASN1(vBlindedMsgBytes);

        // Validate randomness
        Element gr = this.gen.scale(rr);
        if (!gr.equals(uBlind)) {
            throw new IOException("Invalid randomness used for decryption");
        }

        // Compute plaintext
        Element result = this.hh
                .scale(rr)
                .inverse()
                .op(vBlindedMsg);

        byte[] plaintext = result.decode();
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    public String decodePlaintext(byte[] plaintextEncoded) throws IOException {
        Element tmp = group.ofASN1(plaintextEncoded);
        return new String(tmp.decode(), StandardCharsets.UTF_8);
    }
}
