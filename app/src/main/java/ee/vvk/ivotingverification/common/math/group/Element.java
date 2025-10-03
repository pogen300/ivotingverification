package ee.vvk.ivotingverification.common.math.group;

import androidx.annotation.NonNull;

public interface Element {
    /**
     * Returns an Element E such that it satisfies an equation (this op E) == identity Element.
     *
     * @return this Element inverse value
     */
    Element inverse();

    /**
     * Returns an Element whose value is (this ^ s) for multiplicative group (e.g. ModP) and
     * (this * s) for additive groups (e.g. ECp).
     *
     * @param s Scalar used to scale this Element
     * @return this {^,*} s
     */
    Element scale(@NonNull final Scalar s);

    /**
     * Returns an Element whose value is (this * E) for multiplicative group (e.g. ModP) and
     * (this + E) for additive groups (e.g. ECp).
     *
     * @param E Element to perform group operation on
     * @return this {*,+} E
     * @throws IllegalArgumentException if this and E are not from the same Group
     */
    Element op(@NonNull final Element E);

    /**
     * @return plaintext decoded from this Element
     */
    byte[] decode();
}
