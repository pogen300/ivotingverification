package ee.vvk.ivotingverification;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import ee.vvk.ivotingverification.dialog.LoadingSpinner;
import ee.vvk.ivotingverification.model.QRCodeContents;
import ee.vvk.ivotingverification.model.VerificationProfile;
import ee.vvk.ivotingverification.tasks.AsyncTaskActivity;
import ee.vvk.ivotingverification.tasks.GetVoteTask;
import ee.vvk.ivotingverification.tasks.TaskRunner;
import ee.vvk.ivotingverification.tasks.VoteTaskResult;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.EstEidLoader;
import ee.vvk.ivotingverification.util.Util;

public class VoteDownloadActivity extends Activity implements AsyncTaskActivity<VoteTaskResult> {

    private static final String TAG = VoteDownloadActivity.class.getSimpleName();
    private LoadingSpinner mLoadingSpinner;
    private QRCodeContents qrCodeContents;
    private VerificationProfile verificationProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.vote_download_activity);

        Intent intent = getIntent();
        qrCodeContents = intent.getParcelableExtra(Util.QR_CODE_CONTENTS);

        try {
            verificationProfile = VerificationProfile.loadVerificationProfile(
                    C.publicKey, EstEidLoader.getEsteidCerts(VoteDownloadActivity.this),
                    C.ocspServiceCertArray, C.tspregServiceCert, C.tspregClientCert);
        } catch (Exception e) {
            Util.logException(TAG, e);
            Util.startErrorIntent(VoteDownloadActivity.this, C.badConfigMessage);
            return;
        }

        LinearLayout frameBg = findViewById(R.id.vote_download_frame_bg);
        frameBg.setBackgroundColor(Util.generateHexColorValue(C.frameBackground));

        LinearLayout linearLayout = findViewById(R.id.target_window_load);
        GradientDrawable bgShape = (GradientDrawable) linearLayout.getBackground();
        bgShape.setColor(Util.generateHexColorValue(C.loadingWindow));

        if (Util.isNetworkConnected(this)) {
            TaskRunner runner = new TaskRunner();
            runner.executeAsync(new GetVoteTask(this, qrCodeContents, verificationProfile));
        } else {
            Util.startNetworkErrorIntent(VoteDownloadActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.stopSpinner(mLoadingSpinner);
    }

    @Override
    public void onPreExecute() {
        mLoadingSpinner = Util.startSpinner(VoteDownloadActivity.this, true);
    }

    @Override
    public void onPostExecute(VoteTaskResult result) {
        Util.stopSpinner(mLoadingSpinner);
        if (result.isFailure()) {
            Util.startErrorIntent(VoteDownloadActivity.this, result.errorMessage);
            return;
        }
        Intent next_intent = new Intent(VoteDownloadActivity.this, VoteReceivedActivity.class);
        next_intent.putExtra(Util.VOTE_CONTAINER_INFO, result.value);
        next_intent.putExtra(Util.QR_CODE_CONTENTS, qrCodeContents);
        startActivity(next_intent);
        finish();
    }
}
