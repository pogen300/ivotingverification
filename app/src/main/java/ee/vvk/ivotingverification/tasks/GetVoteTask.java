package ee.vvk.ivotingverification.tasks;

import android.util.Base64;

import java.io.InputStream;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ee.vvk.ivotingverification.model.QRCodeContents;
import ee.vvk.ivotingverification.model.Vote;
import ee.vvk.ivotingverification.model.VoteContainerInfo;
import ee.vvk.ivotingverification.model.VerificationProfile;
import ee.vvk.ivotingverification.util.BDocContainer;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.JSONParser;
import ee.vvk.ivotingverification.util.JsonRpc;
import ee.vvk.ivotingverification.util.TlsConnection;
import ee.vvk.ivotingverification.util.Util;

public class GetVoteTask extends BaseTask<VoteTaskResult> {

    private static final String TAG = GetVoteTask.class.getSimpleName();

    private final AsyncTaskActivity<VoteTaskResult> activity;
    private final QRCodeContents qrCodeContents;
    private final VerificationProfile verificationProfile;

    public GetVoteTask(AsyncTaskActivity<VoteTaskResult> activity, QRCodeContents qrCodeContents, VerificationProfile verificationProfile) {
        this.activity = activity;
        this.qrCodeContents = qrCodeContents;
        this.verificationProfile = verificationProfile;
    }

    @Override
    public void setUiForLoading() {
        activity.onPreExecute();
    }

    @Override
    public VoteTaskResult call() throws Exception {
        InputStream response;
        Map<String, Object> params = new HashMap<>();
        params.put("voteid", qrCodeContents.getVoteId());
        params.put("sessionid", qrCodeContents.getSessionId());
        ByteBuffer buf = JsonRpc.createRequest(JsonRpc.Method.VERIFY, params);
        TlsConnection tls = new TlsConnection(C.verificationTlsArray);
        try {
            response = tls.sendRequest(C.verificationUrlArray, C.verificationSNI, buf);
        } catch (ConnectException ex) {
            return new VoteTaskResult(null, C.sendServerRequestMessage);
        }
        return readResponse(response);
    }

    private VoteTaskResult readResponse(InputStream in) throws Exception {
        JsonRpc.Response res = JsonRpc.unmarshalResponse(JsonRpc.Method.VERIFY, in);
        if (res.error != null) {
            Util.logError(TAG, res.error);
            return new VoteTaskResult(null, C.badServerResponseMessage);
        }

        byte[] containerData = Base64.decode((String) res.result.get("Vote"), Base64.DEFAULT);
        byte[] ocspData = Base64.decode((String) res.result.get("ocsp"), Base64.DEFAULT);
        byte[] regData = Base64.decode((String) res.result.get("tspreg"), Base64.DEFAULT);
        String choicesListInput = (String) res.result.get("ChoicesList");

        if (containerData == null || ocspData == null || regData == null || choicesListInput == null) {
            Util.logError(TAG, "Vote data invalid");
            return new VoteTaskResult(null, C.badConfigMessage);
        }
        byte[] choicesListDecode = org.spongycastle.util.encoders.Base64.decode(choicesListInput);
        JSONParser.parseCandidateList(new String(choicesListDecode));

        BDocContainer container;
        try {
            container = BDocContainer.getVerifiedContainer(verificationProfile,
                    containerData, ocspData, regData);
        } catch (Exception ex) {
            Util.logException(TAG, ex);
            return new VoteTaskResult(null, C.badServerResponseMessage);
        }
        assert container != null;
        String signerCN = container.getSignerCN();

        if (container.getVotes() == null) {
            Util.logError(TAG, "Container does not have any votes");
            return new VoteTaskResult(null, C.badServerResponseMessage);
        }

        ArrayList<Vote> voteList = new ArrayList<>();
        for (Map.Entry<String, byte[]> vote : container.getVotes().entrySet()) {
            voteList.add(new Vote(vote.getKey(), vote.getValue()));
        }
        return new VoteTaskResult(new VoteContainerInfo(signerCN, voteList), null);
    }

    @Override
    public void setDataAfterLoading(VoteTaskResult result) {
        activity.onPostExecute(result);
    }
}


