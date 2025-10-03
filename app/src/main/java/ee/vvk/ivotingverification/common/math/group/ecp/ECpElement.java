package ee.vvk.ivotingverification.common.math.group.ecp;

import androidx.annotation.NonNull;

import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import ee.vvk.ivotingverification.common.math.group.Element;
import ee.vvk.ivotingverification.common.math.group.Scalar;

/**
 * ECp element represents NIST P elliptic curve coordinate that is defined in an abstract group.
 */
public final class ECpElement implements Element {
    private final ECpGroup group;
    private final ECPoint point;

    /**
     * Creates ECp group Element with provided elliptic curve coordinate.
     *
     * @param group underlying ECp group
     * @param point elliptic curve coordinate
     * @throws IllegalArgumentException if point is invalid
     */
    public ECpElement(@NonNull final ECpGroup group, @NonNull final ECPoint point) {
        Objects.requireNonNull(group, "Cannot create an element of a null group");
        Objects.requireNonNull(point, "Element cannot have null coordinates");

        this.group = group;
        this.point = point.normalize();

        if (!this.point.isValid())
            throw new IllegalArgumentException("Invalid EC point");
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

        ECpElement otherElement = (ECpElement) other;

        // Field comparison
        return Objects.equals(point, otherElement.point)
               && Objects.equals(group, otherElement.group);
    }

    @Override
    public Element inverse() {
        return new ECpElement(this.group, this.point.negate());
    }

    @Override
    public Element scale(@NonNull final Scalar s) {
        Objects.requireNonNull(s, "Cannot scale by a null Scalar");

        return new ECpElement(this.group, this.point.multiply(s.value));
    }

    @Override
    public Element op(@NonNull final Element E) {
        // Equal already performs null check
        if (!group.equals(((ECpElement) E).group))
            throw new IllegalArgumentException("Group elements from mismatching groups");

        return new ECpElement(this.group, this.point.add(((ECpElement) E).point));
    }

    @Override
    public byte[] decode() {
        BigInteger X = this.point.getAffineXCoord().toBigInteger();

        if ((X.bitLength()) != this.group.fieldOrder().bitLength() - 1)
            throw new IllegalArgumentException("Plaintext not padded to correct length");

        X = X.shiftRight(10); // Strip field encoding bits

        byte[] XBytes = X.toByteArray();

        int mostSignificantByte = XBytes[0] >> 1; // First byte

        // Leading 0 bits count in a byte
        int zerosCount = Byte.SIZE - (Integer.SIZE - Integer.numberOfLeadingZeros(mostSignificantByte));
        // All 1 bits count in a byte
        int onesCount = Integer.bitCount(mostSignificantByte);

        // After leading zeros there should only be continuous stream of ones up until
        // padding end bit (0) is met, e.g. 0b00000111 0b11111111 ... 0b1110
        //
        // Therefore we assume that inside a single byte (8 bits), once we subtract all 1 bits,
        // there would only be leading 0 bits, so such combinations like 0b00000101 are not possible
        // since 1 bits count is indeed 2, leading 0 bits are 5 and therefore 8 - 2 = 6 which makes it
        // 6 != 5
        if (8 - onesCount != zerosCount)
            throw new IllegalArgumentException("Plaintext not padded correctly by inspecting first byte");

        // Strip 0xFF padding bytes, starting from second byte as first byte is already checked
        for (int i = 1; i < XBytes.length; i++) {
            switch (XBytes[i]) {
                case (byte) 0xFF: // padding byte
                    break;
                case (byte) 0xFE: // end of padding
                    // encoded plaintext contains only padding bytes, so decoded plaintext is ""
                    if (i + 1 == XBytes.length)
                        return new byte[]{};
                    return Arrays.copyOfRange(XBytes, i + 1, XBytes.length);
                default:
                    throw new IllegalArgumentException("Unexpected padding byte");
            }
        }

        throw new IllegalArgumentException("Incorrect padding");
    }
}
