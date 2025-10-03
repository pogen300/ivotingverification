package ee.vvk.ivotingverification.common.math.group.modp;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import ee.vvk.ivotingverification.common.math.group.Element;
import ee.vvk.ivotingverification.common.math.group.Scalar;

/**
 * ModP group element represents a value that is defined over a [1, safe prime) field.
 */
public final class ModPElement implements Element {
    private final ModPGroup group;
    private final BigInteger value;


    /**
     * Creates ModP group Element with provided value.
     *
     * @param group underlying ModP group
     * @param value value of an Element
     */
    public ModPElement(@NonNull final ModPGroup group, @NonNull final BigInteger value) {
        Objects.requireNonNull(group, "Cannot create an element of a null group");
        Objects.requireNonNull(value, "Element cannot have null value");

        this.group = group;
        this.value = value;
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

        ModPElement otherElement = (ModPElement) other;

        // Field comparison
        return Objects.equals(value, otherElement.value)
               && Objects.equals(group, otherElement.group);
    }

    @Override
    public Element inverse() {
        return new ModPElement(this.group, this.value.modInverse(this.group.fieldOrder()));
    }

    @Override
    public Element scale(@NonNull final Scalar s) {
        Objects.requireNonNull(s, "Cannot scale by a null Scalar");

        return new ModPElement(this.group, this.value.modPow(s.value, this.group.fieldOrder()));
    }

    @Override
    public Element op(@NonNull final Element E) {
        // Equal already performs null check
        if (!group.equals(((ModPElement) E).group))
            throw new IllegalArgumentException("Group elements from mismatching groups");

        return new ModPElement(this.group, this.value.multiply(((ModPElement) E).value).mod(this.group.fieldOrder()));
    }

    @Override
    public byte[] decode() {
        BigInteger quadraticResidue;

        // If this element is not a quadratic residue modulo P
        if (this.value.compareTo(this.group.order()) > 0)
            quadraticResidue = this.group.fieldOrder().subtract(this.value);
        else
            quadraticResidue = this.value;

        // Check that group element has a correct byte length.
        // BigInteger strips leading 0x00 padding byte that's why "-1"
        if (((quadraticResidue.bitLength() + 7) / 8) != ((this.group.order().bitLength() + 7) / 8) - 1)
            throw new IllegalArgumentException("Group element has incorrect length to be decoded to a plaintext");

        byte[] valueStrippedLeadingZeroBytes = quadraticResidue.toByteArray();
        byte[] plaintext = new byte[(this.group.fieldOrder().bitLength() + 7) / 8];
        // BigInteger strips all leading 0x00 bytes, that is why we restore padded plaintext with
        // leading 0x00 byte included
        System.arraycopy(valueStrippedLeadingZeroBytes, 0, plaintext, Math.max(plaintext.length - valueStrippedLeadingZeroBytes.length, 0), valueStrippedLeadingZeroBytes.length);

        // Padded plaintext should be padded up to group field byte length
        if (plaintext.length != (this.group.fieldOrder().bitLength() + 7) / 8)
            throw new IllegalArgumentException("Plaintext not padded to correct length");

        // Check that value contains at least 0x00 0x01 leading padding and 0x00
        // ending padding bytes
        if (plaintext.length < 3)
            throw new IllegalArgumentException("Padded plaintext contains no mandatory padding bytes");

        // Then we must ensure that first leading padding byte is 0x00
        if (plaintext[0] != 0x00)
            throw new IllegalArgumentException("Incorrect first padding byte");

        // After, we ensure that second leading padding byte is 0x01
        if (plaintext[1] != 0x01)
            throw new IllegalArgumentException("Incorrect second padding byte");

        // Iterate over 0xFF padding bytes until 0x00 is met, which separates 0xFF padding bytes and a plaintext
        for (int i = 2; i < plaintext.length; i++) {
            switch (plaintext[i]) {
                case 0x00: // Padding end
                    return Arrays.copyOfRange(plaintext, i + 1, plaintext.length);
                case (byte) 0xFF:
                    continue;
                default:
                    throw new IllegalArgumentException("Unexpected padding byte");
            }
        }

        throw new IllegalArgumentException("Incorrect padding");
    }
}
