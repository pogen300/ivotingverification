package ee.vvk.ivotingverification.common.crypto.elgamal;

public interface IVXVPublicKey {
    String getPublicKey();

    byte[] getCiphertext();

    byte[] getRandomness();

    String getPlaintext();
}
