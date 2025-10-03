package ee.vvk.ivotingverification.common.math.group.modp;

import androidx.annotation.NonNull;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.pqc.math.linearalgebra.IntegerFunctions;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

import ee.vvk.ivotingverification.common.math.group.Element;
import ee.vvk.ivotingverification.common.math.group.Group;

/**
 * ModP group represents modulo safe prime abstract group.
 */
public final class ModPGroup implements Group {
    public static final ASN1ObjectIdentifier OID = new ASN1ObjectIdentifier("1.3.6.1.4.1.3029.2.1");
    private final BigInteger p;
    private final BigInteger g;
    private final BigInteger q;

    /**
     * Parses ASN.1 encoded group parameters to extract P and G.
     *
     * @param params ASN.1 encoded group parameters
     */
    public ModPGroup(@NonNull final ASN1Encodable[] params) {
        Objects.requireNonNull(params, "ModP group parameters cannot be null");

        this.p = ((ASN1Integer) params[0]).getValue();
        this.g = ((ASN1Integer) params[1]).getValue();
        this.q = this.p.subtract(BigInteger.ONE).shiftRight(1);
    }

    @Override
    public String name() {
        return "ModPGroup";
    }

    @Override
    public ASN1ObjectIdentifier oid() {
        return OID;
    }

    @Override
    public BigInteger order() {
        return this.q;
    }

    public Element generator() {
        return new ModPElement(this, this.g);
    }

    @Override
    public BigInteger fieldOrder() {
        return this.p;
    }


    @Override
    public boolean equals(Object other) {
        // self check
        if (this == other) return true;

        // Null check
        if (other == null) return false;

        // Type check and cast
        if (getClass() != other.getClass())
            return false;

        ModPGroup otherGroup = (ModPGroup) other;

        // Field comparison
        return Objects.equals(p, otherGroup.p) &&
               Objects.equals(g, otherGroup.g) &&
               Objects.equals(q, otherGroup.q);
    }

    @Override
    public Element ofASN1(@NonNull final byte[] data) throws IOException {
        Objects.requireNonNull(data, "ASN.1 data cannot not be null");

        ASN1Primitive asn1Object = ASN1Primitive.fromByteArray(data);
        BigInteger data2int = ((ASN1Integer) asn1Object).getPositiveValue();

        if (data2int.bitLength() < 1) {
            throw new IllegalArgumentException("ASN.1 unmarshalled element length is less than 1");
        }

        if (data2int.compareTo(this.fieldOrder()) >= 0) {
            throw new IllegalArgumentException("ASN.1 unmarshalled element is larger than or equal to P");
        }

        if (IntegerFunctions.jacobi(data2int, this.fieldOrder()) != 1) {
            throw new IllegalArgumentException("ASN.1 unmarshalled element is not a quadratic residue modulo P");
        }

        return new ModPElement(this, data2int);
    }
}
