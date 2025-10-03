package ee.vvk.ivotingverification.common.crypto.elgamal;

import org.spongycastle.asn1.ASN1Encodable;

public interface GroupParams {
    ASN1Encodable[] getParams();
}
