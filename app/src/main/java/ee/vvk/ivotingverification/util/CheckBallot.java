package ee.vvk.ivotingverification.util;

import ee.vvk.ivotingverification.model.Candidate;

public class CheckBallot {

    private static final String TAG = CheckBallot.class.getSimpleName();

    public static boolean decryptedChoiceNotInCandidateList(String decryptedChoice) {
        for (Candidate c : C.candidateList) {
            if (c.number.equals(decryptedChoice))
                return false;
        }
        Util.logDebug(TAG, "Ballot not in election list");
        return true;
    }
}
