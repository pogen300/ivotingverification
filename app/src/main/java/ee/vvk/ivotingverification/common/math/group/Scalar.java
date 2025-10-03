package ee.vvk.ivotingverification.common.math.group;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Immutable Scalar that belongs to an abstract Group with an order of mod, which therefore makes
 * Scalar to be defined over a [1, mod) field.
 * Note, that Scalar value can be negative as well, whereas all operations performed on a Scalar
 * will still push that value into a [1, mod) range.
 */
public final class Scalar {
    public final BigInteger value;
    private final BigInteger mod;

    /**
     * Creates a Scalar from provided value and mod.
     *
     * @param value value of a Scalar
     * @param mod   modulo of a Scalar
     */
    public Scalar(@NonNull final BigInteger value, @NonNull final BigInteger mod) {
        Objects.requireNonNull(value, "Value cannot be null");
        Objects.requireNonNull(mod, "Modulo cannot be null");

        this.value = value;
        this.mod = mod;
    }
}
