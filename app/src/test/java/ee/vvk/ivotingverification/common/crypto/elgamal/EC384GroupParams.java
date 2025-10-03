package ee.vvk.ivotingverification.common.crypto.elgamal;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERGeneralString;
import org.spongycastle.asn1.DERSequence;

public class EC384GroupParams implements GroupParams {
    private final DERGeneralString curveName = new DERGeneralString("P-384"); // GeneralString
    private final DERGeneralString electionID = new DERGeneralString("DEMO2024"); // GeneralString
    private final ASN1EncodableVector vector = new ASN1EncodableVector();

    //  sequence ::= SEQUENCE {
    //      GeneralString
    //      GeneralString
    //  }
    private final ASN1Sequence sequence;

    public EC384GroupParams() {
        this.vector.add(this.curveName);
        this.vector.add(this.electionID);
        this.sequence = new DERSequence(this.vector);
    }

    @Override
    public ASN1Encodable[] getParams() {
        return this.sequence.toArray();
    }
}
