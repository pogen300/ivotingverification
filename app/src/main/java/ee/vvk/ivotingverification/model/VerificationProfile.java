package ee.vvk.ivotingverification.model;

import org.spongycastle.cms.SignerInformationVerifier;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.spongycastle.operator.ContentVerifierProvider;
import org.spongycastle.operator.jcajce.JcaContentVerifierProviderBuilder;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import ee.vvk.ivotingverification.common.crypto.DecrypterWithRandomness;
import ee.vvk.ivotingverification.common.crypto.x509.X509PublicKey;
import ee.vvk.ivotingverification.util.Util;

public record VerificationProfile(DecrypterWithRandomness publicKey, X509Certificate[] esteidCerts,
                                  List<ContentVerifierProvider> ocspCerts,
                                  SignerInformationVerifier tspregServiceCert,
                                  X509Certificate tspregClientCert) {

    public static VerificationProfile loadVerificationProfile(
            String publicKey,
            X509Certificate[] esteidCerts,
            String[] ocspCerts,
            String tspregServiceCert,
            String tspregClientCert
    ) throws Exception {

        DecrypterWithRandomness pub = X509PublicKey.parseX509PEMPublicKey(publicKey);

        JcaContentVerifierProviderBuilder builder = new JcaContentVerifierProviderBuilder().setProvider("SC");
        List<ContentVerifierProvider> ocspVerifierProviders = new ArrayList<>();
        for (String ocspCertStr : ocspCerts) {
            ocspVerifierProviders.add(builder.build(Util.loadCertificate(ocspCertStr)));
        }

        SignerInformationVerifier tspregCert = new JcaSimpleSignerInfoVerifierBuilder().build(Util.loadCertificate(tspregServiceCert));
        X509Certificate collectorCert = Util.loadCertificate(tspregClientCert);

        return new VerificationProfile(pub, esteidCerts, ocspVerifierProviders, tspregCert, collectorCert);
    }


}
