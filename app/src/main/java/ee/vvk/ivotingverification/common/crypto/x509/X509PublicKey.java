package ee.vvk.ivotingverification.common.crypto.x509;

import androidx.annotation.NonNull;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERGeneralString;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;

import ee.vvk.ivotingverification.common.crypto.DecrypterWithRandomness;
import ee.vvk.ivotingverification.common.crypto.elgamal.ElGamalPublicKey;
import ee.vvk.ivotingverification.common.math.group.Group;
import ee.vvk.ivotingverification.common.math.group.modp.ModPGroup;
import ee.vvk.ivotingverification.common.math.group.ecp.ECpGroup;

public final class X509PublicKey {

    // Prevent instantiation
    private X509PublicKey() {
    }

    /**
     * Returns DecryptedWithSecret parsed from an ASN.1 PEM encoded public key.
     *
     * @param in ASN.1 PEM encoded public key
     * @return DecryptedWithSecret
     * @throws IOException if reading ASN.1 PEM encoded public key fails
     */
    public static DecrypterWithRandomness parseX509PEMPublicKey(@NonNull final String in) throws IOException {
        Objects.requireNonNull(in, "ASN.1 PEM encoded public key should not be null");

        byte[] content;

        // Read PEM public key
        try (PemReader pemReader = new PemReader(new StringReader(in))) {
            PemObject pemObject = pemReader.readPemObject();
            content = pemObject.getContent(); // PEM headers are removed, content is base64 decoded
        }

        // ASN.1 parse PEM decoded content
        ASN1InputStream contentASN1 = new ASN1InputStream(content);
        ASN1Primitive data = contentASN1.readObject();
        SubjectPublicKeyInfo spki;
        spki = SubjectPublicKeyInfo.getInstance(data);

        ASN1Sequence pkey = (ASN1Sequence) spki.parsePublicKey(); // public key
        ASN1ObjectIdentifier oid = spki.getAlgorithm().getAlgorithm(); // public key OID
        ASN1Encodable[] params = ((ASN1Sequence) spki.getAlgorithm().getParameters()).toArray(); // public key params

        Group g;
        String electionID;
        if (oid.getId().equals(ModPGroup.OID.getId())) {
            g = new ModPGroup(params);
            electionID = ((DERGeneralString) params[2]).getString();
        } else if (oid.getId().equals(ECpGroup.OID.getId())) {
            g = new ECpGroup(params);
            electionID = ((DERGeneralString) params[1]).getString();
        } else {
            throw new IllegalArgumentException("Unsupported group OID: " + oid.getId());
        }

        return new ElGamalPublicKey(electionID, g, pkey);
    }
}
