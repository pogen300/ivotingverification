package ee.vvk.ivotingverification.util;

import org.spongycastle.asn1.x509.ExtendedKeyUsage;
import org.spongycastle.asn1.x509.KeyPurposeId;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.ocsp.BasicOCSPResp;
import org.spongycastle.cert.ocsp.CertificateID;
import org.spongycastle.cert.ocsp.CertificateStatus;
import org.spongycastle.cert.ocsp.OCSPResp;
import org.spongycastle.cert.ocsp.SingleResp;
import org.spongycastle.jce.X509Principal;
import org.spongycastle.operator.ContentVerifierProvider;
import org.spongycastle.operator.jcajce.JcaContentVerifierProviderBuilder;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
public class Ocsp {
    private static final String TAG = "OCSP";
    private final BasicOCSPResp basicOcspResp;

    public Ocsp(InputStream response) throws Exception {
        OCSPResp ocspResp = new OCSPResp(response);
        if (ocspResp.getStatus() == OCSPResp.SUCCESSFUL) {
            this.basicOcspResp = basicResp(ocspResp.getResponseObject());
        } else {
            throw new Exception("Ocsp response status not successful: " + ocspResp.getStatus());
        }
    }

    private static BasicOCSPResp basicResp(Object respObject) throws Exception {
        if (!(respObject instanceof BasicOCSPResp basicResp)) {
            throw new Exception("Ocsp response is not basic");
        }
        return basicResp;
    }

    public void verifySignature(List<ContentVerifierProvider> ocspVerifiers, X509Certificate issuerCert) throws Exception {
        boolean verified = false;
        for (ContentVerifierProvider verifier : ocspVerifiers) {
            if (basicOcspResp.isSignatureValid(verifier)) {
                verified = true;
                break;
            }
        }
        if (!verified) {
            Util.logInfo(TAG, "Preconfigured OCSP responders not suitable, trying AIA");
            verified = checkAIAResponder(basicOcspResp, issuerCert);
        }

        if (!verified) {
            throw new Exception("Signature verification failed");
        }
    }

    public void verifyResponse(X509Certificate requestedCert) throws Exception {
        SingleResp[] responses = basicOcspResp.getResponses();
        for (SingleResp resp : responses) {
            if (!isResponseStatusGood(resp) || !isOcspAndCertMatchValid(resp, requestedCert))
                continue;
            return;
        }
        throw new Exception("Couldn't verify ocsp response");
    }

    private boolean isOcspAndCertMatchValid(SingleResp resp, X509Certificate requestedCert) throws Exception {
        CertificateID id = resp.getCertID();
        byte[] certAuthorityKeyID = getAuthorityKey(requestedCert);
        if (!requestedCert.getSerialNumber().equals(id.getSerialNumber())) {
            Util.logWarning(TAG, "Cert's and Ocsp responses serial numbers do not match");
            return false;
        }
        MessageDigest digest = MessageDigest.getInstance(id.getHashAlgOID().toString(), "SC");
        if (!Arrays.equals(certAuthorityKeyID, id.getIssuerKeyHash())) {
            Util.logWarning(TAG, "Cert's and ocsp's authority key hash do not match");
            return false;
        }

        byte[] dn;
        try {
            dn = digest.digest(((X509Principal) requestedCert.getIssuerDN()).getEncoded());
        } catch (Exception e) {
            dn = digest.digest(new X509Principal(true, requestedCert.getIssuerDN().getName()).getEncoded());
        }

        if (!Arrays.equals(dn, id.getIssuerNameHash())) {
            Util.logWarning(TAG, "Cert's and ocsp's issuer DN hash do not match");
            return false;
        }
        return true;
    }

    private byte[] getAuthorityKey(X509Certificate requestedCert) throws Exception {
        byte[] authorityKeyId = requestedCert.getExtensionValue("2.5.29.35");
        if (authorityKeyId != null) {
            return Arrays.copyOfRange(authorityKeyId, 6, authorityKeyId.length);
        } else {
            throw new Exception("Certificate verification failed");
        }
    }

    public long getProducedAt() {
        return basicOcspResp.getProducedAt().getTime();
    }

    private boolean isResponseStatusGood(SingleResp resp) {
        if (resp.getCertStatus() != CertificateStatus.GOOD) {
            Util.logWarning(TAG, "Ocsp SingleResponse cert status not GOOD: " + resp.getCertStatus());
            return false;
        }
        return true;
    }

    private static boolean checkAIAResponder(BasicOCSPResp basicResp, X509Certificate issuerCert) throws Exception {
        JcaContentVerifierProviderBuilder builder = new JcaContentVerifierProviderBuilder().setProvider("SC");
        ContentVerifierProvider issuerVerifier = builder.build(issuerCert);
        for (X509CertificateHolder responder : basicResp.getCerts()) {
            // is signed by same issuer as the cert whose ocsp be are checking
            if (responder.isSignatureValid(issuerVerifier)) {
                // and has proper signature of the responder
                if (basicResp.isSignatureValid(builder.build(responder))) {
                    ExtendedKeyUsage keyUsage = ExtendedKeyUsage.fromExtensions(responder.getExtensions());
                    // and responder has proper key extension
                    if (keyUsage.hasKeyPurposeId(KeyPurposeId.id_kp_OCSPSigning)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
