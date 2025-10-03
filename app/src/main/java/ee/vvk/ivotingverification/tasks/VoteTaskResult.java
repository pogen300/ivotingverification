package ee.vvk.ivotingverification.tasks;

import ee.vvk.ivotingverification.model.VoteContainerInfo;

public class VoteTaskResult extends Result<VoteContainerInfo> {

    public VoteTaskResult(VoteContainerInfo value, String errorMessage) {
        super(value, errorMessage);
    }
}
