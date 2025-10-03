package ee.vvk.ivotingverification.tasks;

import java.util.ArrayList;

import ee.vvk.ivotingverification.model.Candidate;

public class DecryptionTaskResult extends Result<ArrayList<Candidate>> {

    public DecryptionTaskResult(ArrayList<Candidate> value, String errorMessage) {
        super(value, errorMessage);
    }
}
