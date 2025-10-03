package ee.vvk.ivotingverification.util;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.SignerInformationVerifier;
import org.spongycastle.tsp.TSPValidationException;
import org.spongycastle.tsp.TimeStampToken;
import org.spongycastle.tsp.TimeStampTokenInfo;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;

public class Pkix {

    private final TimeStampToken token;

    public Pkix(byte[] response) throws Exception {
        this.token = new TimeStampToken(new CMSSignedData(response));
    }

    public void validateToken(SignerInformationVerifier sigVerifier) throws Exception {
        // Pkix responder signature on the response has to verified with trusted certificate
        try {
            token.validate(sigVerifier);
        } catch (TSPValidationException e) {
            throw new Exception("TSP response invalid: " + e.getMessage(), e);
        }
    }

    public void verifySignature(PublicKey collectorPub, byte[] signatureValueCanon) throws Exception {
        TimeStampTokenInfo tokenInfo = token.getTimeStampInfo();
        byte[] signatureValueCanonVerified = getVerifiedSignatureValueCanon(signatureValueCanon, tokenInfo);
        Nonce nonce = getStructuredNonce(tokenInfo.getNonce().toByteArray());

        // Collector's signature on the msg has to be verified with trusted key
        Signature signature = Signature.getInstance(nonce.oid, "SC");
        signature.initVerify(collectorPub);
        signature.update(signatureValueCanonVerified);
        if (!signature.verify(nonce.sig)) {
            throw new Exception("Verification of collector's signature in nonce failed");
        }
    }

    private byte[] getVerifiedSignatureValueCanon(byte[] signatureValueCanon, TimeStampTokenInfo tokenInfo) throws Exception {
        // Msg digest and msg imprint in Pkix response have to match
        MessageDigest digest = MessageDigest.getInstance(tokenInfo.getMessageImprintAlgOID().toString(), "SC");
        if (!Arrays.equals(digest.digest(signatureValueCanon), tokenInfo.getMessageImprintDigest())) {
            throw new Exception("MessageImprints of input data and TSP response do not match");
        }
        return signatureValueCanon;
    }

    private Nonce getStructuredNonce(byte[] nonceBytes) throws Exception {
        // Nonce has to have proper structure
        try {
            return new Nonce(nonceBytes);
        } catch (Exception e) {
            throw new Exception("Invalid nonce asn1 structure in PKIX response");
        }
    }

    public long getGenTime() {
        return token.getTimeStampInfo().getGenTime().getTime();
    }

    private static class Nonce {
        private final String oid;
        private final byte[] sig;

        private Nonce(byte[] nonce) throws Exception {
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(nonce));
            ASN1Sequence root = (ASN1Sequence) in.readObject();
            oid = ((ASN1ObjectIdentifier) ((ASN1Sequence) root.getObjectAt(0)).getObjectAt(0)).getId();
            sig = ((DEROctetString) root.getObjectAt(1)).getOctets();
        }
    }
}
