package ee.vvk.ivotingverification.common.math.group;

import androidx.annotation.NonNull;

import org.spongycastle.asn1.ASN1ObjectIdentifier;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Abstract group which is used as a unit in cryptographic operations.
 */
public interface Group {
    /**
     * @return name of a Group
     */
    String name();

    /**
     * @return OID associated with the Group
     */
    ASN1ObjectIdentifier oid();

    /**
     * @return generator of a Group
     */
    Element generator();

    /**
     * @return this Group generator order
     */
    BigInteger order();

    /**
     * @return this Group field order
     */
    BigInteger fieldOrder();

    /**
     * Creates Element instance from ASN.1 encoded Group element.
     *
     * @param data ASN.1 encoded Group element
     * @return Element from ASN.1
     * @throws IOException if reading ASN.1 fails
     */
    Element ofASN1(@NonNull final byte[] data) throws IOException;
}
