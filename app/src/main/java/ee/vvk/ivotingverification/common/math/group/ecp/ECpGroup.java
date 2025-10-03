package ee.vvk.ivotingverification.common.math.group.ecp;

import androidx.annotation.NonNull;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1String;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.custom.sec.SecP384R1Curve;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ee.vvk.ivotingverification.common.math.group.Element;
import ee.vvk.ivotingverification.common.math.group.Group;

/**
 * ECp group represents NIST P elliptic curve that is defined over an abstract group.
 */
public final class ECpGroup implements Group {
    public static final ASN1ObjectIdentifier OID = new ASN1ObjectIdentifier("1.3.6.1.4.1.99999.1");
    private static final Map<String, ECCurve.AbstractFp> supportedCurves = new HashMap<>() {{
        put("P-384", new SecP384R1Curve());
    }};
    private final ECCurve.AbstractFp curve;

    /**
     * Parses ASN.1 encoded group parameters to extract EC curve name.
     *
     * @param params ASN.1 encoded group parameters
     * @throws IllegalArgumentException if params refer to an unsupported EC curve
     */
    public ECpGroup(@NonNull final ASN1Encodable[] params) {
        Objects.requireNonNull(params, "ECp group parameters cannot be null");

        String curveName = ((ASN1String) params[0]).getString();
        this.curve = supportedCurves.get(curveName);
        if (Objects.isNull(this.curve))
            throw new IllegalArgumentException("Unsupported curve: " + curveName);
    }

    @Override
    public String name() {
        return "ECpGroup";
    }

    @Override
    public ASN1ObjectIdentifier oid() {
        return OID;
    }

    public Element generator() {
        BigInteger gx = new BigInteger("aa87ca22be8b05378eb1c71ef320ad746e1d3b628ba79b9859f741e082542a385502f25dbf55296c3a545e3872760ab7", 16);
        BigInteger gy = new BigInteger("3617de4a96262c6f5d9e98bf9292dc29f8f41dbd289a147ce9da3113b5f0b8c00a60b1ce1d7e819d7a431d7c90ea0e5f", 16);
        ECPoint point = this.curve.createPoint(gx, gy);
        return new ECpElement(this, point);
    }

    @Override
    public BigInteger order() {
        return this.curve.getOrder(); // N
    }

    @Override
    public BigInteger fieldOrder() {
        return this.curve.getField().getCharacteristic(); // P
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

        ECpGroup otherGroup = (ECpGroup) other;

        // Field comparison
        return Objects.equals(curve, otherGroup.curve);
    }

    @Override
    public Element ofASN1(@NonNull final byte[] data) throws IOException {
        Objects.requireNonNull(data, "ASN.1 data cannot not be null");

        ASN1Primitive asn1Object = ASN1Primitive.fromByteArray(data);
        byte[] data2oct = ((DEROctetString) asn1Object).getOctets();

        if (data2oct.length == 1 && data[0] == 0x00) {
            throw new IllegalArgumentException("ASN.1 data has no coordinates");
        }

        if (data2oct.length % 2 != 1) {
            throw new IllegalArgumentException("X and Y coordinates should have same amount of bytes");
        }

        if (data2oct[0] != 0x04) {
            throw new IllegalArgumentException("Point is not in uncompressed form");
        }

        int coordLen = (data2oct.length - 1) / 2;
        BigInteger x = new BigInteger(1, Arrays.copyOfRange(data2oct, 1, coordLen + 1));
        BigInteger y = new BigInteger(1, Arrays.copyOfRange(data2oct, coordLen + 1, 2 * coordLen + 1));

        ECPoint point;
        if (x.intValue() == -1 && y.intValue() == -1)
            point = this.curve.getInfinity();
        else
            point = this.curve.createPoint(x, y);

        return new ECpElement(this, point);
    }
}
