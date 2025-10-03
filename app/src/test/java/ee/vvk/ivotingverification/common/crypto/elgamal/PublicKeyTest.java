package ee.vvk.ivotingverification.common.crypto.elgamal;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import ee.vvk.ivotingverification.common.crypto.DecrypterWithRandomness;
import ee.vvk.ivotingverification.common.crypto.x509.X509PublicKey;

@RunWith(Parameterized.class)
public class PublicKeyTest {

    private final IVXVPublicKey pkey;

    public PublicKeyTest(IVXVPublicKey pkey) {
        this.pkey = pkey;
    }

    @Parameterized.Parameters
    public static List<Object[]> getRegisteredIVXVPublicKeys() {
        return Arrays.asList(new IVXVPublicKey[][]{
                // {new IVXVEC384_5bitPublicKeyPEM()}, // To pass this test change X = X.shiftRight(5); in ECpElement.java
                // {new IVXVEC384_8bitPublicKeyPEM()}, // To pass this test change X = X.shiftRight(8); in ECpElement.java
                {new IVXVEC384_10bitPublicKeyPEM()},
                // {new IVXVEC384_20bitPublicKeyPEM()}, // To pass this test change X = X.shiftRight(20); in ECpElement.java
                {new IVXVModP3kPublicKeyPEM()},
        });
    }

    @Test
    public void decryptWithRandomnessTest() throws Exception {
        DecrypterWithRandomness decrypter;
        // X509 parse public key
        //
        // It is assumed that public key is in form of String
        decrypter = X509PublicKey.parseX509PEMPublicKey(this.pkey.getPublicKey());

        // Decrypt with provided randomness
        //
        // Public key can ONLY decrypt this particular ciphertext with this particular randomness,
        // whereas private key can decrypt any ciphertext
        String plaintext = decrypter.decryptWithRandomness(this.pkey.getCiphertext(), this.pkey.getRandomness());

        Assert.assertEquals(plaintext, this.pkey.getPlaintext());
    }
}