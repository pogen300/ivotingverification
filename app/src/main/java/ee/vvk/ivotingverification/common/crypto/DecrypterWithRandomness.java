package ee.vvk.ivotingverification.common.crypto;

import androidx.annotation.NonNull;

import java.io.IOException;

public interface DecrypterWithRandomness extends PublicKeyInfo {
    /**
     * Returns a plaintext that has been decrypted from a ciphertext with a provided secret.
     * Secret should only belong to this particular ciphertext and make completely no sense
     * when using outside the context.
     *
     * @param ciphertext data to be decrypted
     * @param random     random value that has been used during ciphertext creation
     * @return plaintext
     * @throws IOException if ciphertext has incorrect ASN.1 structure
     */
    String decryptWithRandomness(@NonNull final byte[] ciphertext, @NonNull final byte[] random) throws IOException;

    /**
     * Method to validate incoming ciphertext ASN1
     *
     * @param in bytes containing the DER encoded ciphertext
     * @return True if the bytes were valid DER encoded ciphertext
     */
    boolean checkAsn1Structure(byte[] in);

    /**
     * Decode group representation of a plaintext into string
     *
     * @param plaintextEncoded ASN1 of a plaintext encoded as group element
     * @return decoded plaintext
     */
    String decodePlaintext(byte[] plaintextEncoded) throws IOException;

}
