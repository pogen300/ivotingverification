package ee.vvk.ivotingverification.tasks;

import java.util.ArrayList;

import ee.vvk.ivotingverification.common.crypto.DecrypterWithRandomness;
import ee.vvk.ivotingverification.model.Candidate;
import ee.vvk.ivotingverification.model.Vote;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.CheckBallot;
import ee.vvk.ivotingverification.util.Util;

public class DecryptionTask extends BaseTask<DecryptionTaskResult> {

    private static final String TAG = DecryptionTask.class.getSimpleName();
    private final AsyncTaskActivity<DecryptionTaskResult> activity;

    private final ArrayList<Vote> voteList;
    private final DecrypterWithRandomness pub;
    private final byte[] rnd;

    public DecryptionTask(AsyncTaskActivity<DecryptionTaskResult> activity, ArrayList<Vote> voteList, DecrypterWithRandomness pub, byte[] rnd) {
        this.activity = activity;
        this.voteList = voteList;
        this.pub = pub;
        this.rnd = rnd;
    }

    @Override
    public void setUiForLoading() {
        activity.onPreExecute();
    }

    @Override
    public DecryptionTaskResult call() {

        ArrayList<Candidate> result = new ArrayList<>();
        for (Vote vote : voteList) {
            try {
                String decChoice = pub.decryptWithRandomness(vote.vote, rnd);
                if (decChoice.isEmpty()) {
                    result.add(Candidate.NO_CHOICE);
                } else {
                    if (CheckBallot.decryptedChoiceNotInCandidateList(decChoice)) {
                        return new DecryptionTaskResult(null, C.badVerificationMessage);
                    }
                    result.add(new Candidate(decChoice));
                }
            } catch (NullPointerException nullPointerException) {
                Util.logException(TAG, nullPointerException);
                return new DecryptionTaskResult(null, C.badVerificationMessage);
            } catch (Exception ex) {
                Util.logException(TAG, ex);
                return new DecryptionTaskResult(null, C.badServerResponseMessage);
            }
        }
        return new DecryptionTaskResult(result, null);
    }

    @Override
    public void setDataAfterLoading(DecryptionTaskResult result) {
        activity.onPostExecute(result);
    }
}

