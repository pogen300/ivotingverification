package ee.vvk.ivotingverification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ee.vvk.ivotingverification.adapter.CandidatesListAdapter;
import ee.vvk.ivotingverification.common.crypto.DecrypterWithRandomness;
import ee.vvk.ivotingverification.common.crypto.x509.X509PublicKey;
import ee.vvk.ivotingverification.dialog.LoadingSpinner;
import ee.vvk.ivotingverification.model.QRCodeContents;
import ee.vvk.ivotingverification.model.Vote;
import ee.vvk.ivotingverification.model.VoteContainerInfo;
import ee.vvk.ivotingverification.tasks.AsyncTaskActivity;
import ee.vvk.ivotingverification.tasks.DecryptionTask;
import ee.vvk.ivotingverification.tasks.DecryptionTaskResult;
import ee.vvk.ivotingverification.tasks.TaskRunner;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;
import ee.vvk.ivotingverification.util.accessibility.AccessibilityDelegate;

public class DecryptionActivity extends Activity implements AsyncTaskActivity<DecryptionTaskResult> {

    private static final String TAG = DecryptionActivity.class.getSimpleName();

    private final Activity thisActivity = this;
    private VoteContainerInfo voteContainerInfo;
    private ListView list;
    private TextView lblChoice;
    private View lblShadow;
    private TextView lblCloseTimeout;
    private View lblcloseTimeoutShadow;
    private View closeBtn;
    private CustomCountDownTimer countDownTimer;
    private LoadingSpinner mLoadingSpinner;
    private final AccessibilityDelegate accessibilityDelegate = new AccessibilityDelegate();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.bruteforce_activity);

        countDownTimer = new CustomCountDownTimer(this, C.closeTimeout, C.closeInterval);

        Intent intent = getIntent();
        voteContainerInfo = intent.getParcelableExtra(Util.VOTE_CONTAINER_INFO);
        QRCodeContents qrCodeContents = intent.getParcelableExtra(Util.QR_CODE_CONTENTS);

        list = findViewById(R.id.list);

        closeBtn = findViewById(R.id.close_view_btn);
        closeBtn.setContentDescription(C.btnClose);
        closeBtn.setOnClickListener(view -> finish());
        closeBtn.setOnFocusChangeListener((view, isFocused) -> {
            if (isFocused) {
                view.setBackgroundColor(Util.generateHexColorValue(C.btnCloseViewFocused));
            } else {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        lblChoice = findViewById(R.id.choice_title_label);
        lblChoice.setText(C.lblChoice);
        lblChoice.setTypeface(C.typeFace);
        lblChoice.setTextColor(Util.generateHexColorValue(C.lblForeground));
        lblChoice.setBackgroundColor(Util.generateHexColorValue(C.lblBackground));

        accessibilityDelegate.setAccessibilityFocusOnStart(closeBtn, lblChoice);

        lblShadow = findViewById(R.id.choice_title_label_shadow);
        lblShadow.setBackgroundColor(Util.generateHexColorValue(C.lblShadow));

        lblCloseTimeout = findViewById(R.id.close_timeout_label);
        lblCloseTimeout.setTypeface(C.typeFace);
        lblCloseTimeout.setText(C.lblCloseTimeout);
        lblCloseTimeout.setTextColor(Util.generateHexColorValue(C.lblCloseTimeoutForeground));
        int[] colors = new int[3];
        colors[0] = Util.generateHexColorValue(C.lblCloseTimeoutBackgroundStart);
        colors[1] = Util.generateHexColorValue(C.lblCloseTimeoutBackgroundCenter);
        colors[2] = Util.generateHexColorValue(C.lblCloseTimeoutBackgroundEnd);

        GradientDrawable bgCloseTimeoutShape = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, colors);
        bgCloseTimeoutShape.setCornerRadius(5);
        lblCloseTimeout.setBackground(bgCloseTimeoutShape);

        lblcloseTimeoutShadow = findViewById(R.id.close_timeout_label_shadow);
        GradientDrawable bgCloseTimeoutShadowShape = (GradientDrawable) lblcloseTimeoutShadow
                .getBackground();
        bgCloseTimeoutShadowShape.setColor(Util.generateHexColorValue(C.lblCloseTimeoutShadow));
        bgCloseTimeoutShadowShape.setCornerRadius(5);

        closeBtn.setVisibility(View.INVISIBLE);
        lblChoice.setVisibility(View.INVISIBLE);
        lblShadow.setVisibility(View.INVISIBLE);
        lblCloseTimeout.setVisibility(View.INVISIBLE);
        lblcloseTimeoutShadow.setVisibility(View.INVISIBLE);

        DecrypterWithRandomness pub;
        try {
            pub = X509PublicKey.parseX509PEMPublicKey(C.publicKey);
        } catch (Exception e) {
            Util.logDebug(TAG, "Bad public key", e);
            Util.startErrorIntent(DecryptionActivity.this, C.badConfigMessage);
            return;
        }
        TaskRunner runner = new TaskRunner();
        runner.executeAsync(new DecryptionTask(this, voteContainerInfo.getVoteList(), pub, qrCodeContents.getRndSeed()));
    }

    public void onPreExecute() {
        mLoadingSpinner = Util.startSpinner(DecryptionActivity.this, false);
    }

    public void onPostExecute(DecryptionTaskResult result) {
        Util.stopSpinner(mLoadingSpinner);
        if (result.isFailure()) {
            Util.startErrorIntent(DecryptionActivity.this, result.errorMessage);
            return;
        }
        List<String> questionIds = new ArrayList<>();
        for (Vote vote : voteContainerInfo.getVoteList()) {
            questionIds.add(vote.questionId);
        }
        CandidatesListAdapter adapter = new CandidatesListAdapter(
                thisActivity, result.value, questionIds);
        list.setAdapter(adapter);

        ((Vibrator) thisActivity.getSystemService(Context.VIBRATOR_SERVICE))
                .vibrate(Util.VIBRATE_DURATION);

        closeBtn.setVisibility(View.VISIBLE);
        lblChoice.setVisibility(View.VISIBLE);
        lblShadow.setVisibility(View.VISIBLE);
        lblCloseTimeout.setVisibility(View.VISIBLE);
        lblcloseTimeoutShadow.setVisibility(View.VISIBLE);

        countDownTimer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.stopSpinner(mLoadingSpinner);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public class CustomCountDownTimer extends CountDownTimer {

        private final Context context;

        public CustomCountDownTimer(Context context, long startTime, long interval) {
            super(startTime, interval);
            this.context = context;
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    accessibilityDelegate.informAboutTimeLimit(
                            context,
                            C.lblCloseTimeout.replace("XX", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(startTime)))
                    ),
                    500);
        }



        @Override
        public void onFinish() {
            finish();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            lblCloseTimeout.setText(C.lblCloseTimeout.replace("XX", String
                    .valueOf(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished))));
            if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) == 5) {
                accessibilityDelegate.informAboutTimeLimit(
                        context,
                        C.lblCloseTimeout.replace("XX", "5")
                );
            }
        }
    }
}
