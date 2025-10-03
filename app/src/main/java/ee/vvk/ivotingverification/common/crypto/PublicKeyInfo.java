package ee.vvk.ivotingverification.common.crypto;

public interface PublicKeyInfo {
    /**
     * @return an election ID that this public key belongs to
     */
    String getElectionID();
}
