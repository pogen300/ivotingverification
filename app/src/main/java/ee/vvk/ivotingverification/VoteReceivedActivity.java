package ee.vvk.ivotingverification;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import ee.vvk.ivotingverification.model.QRCodeContents;
import ee.vvk.ivotingverification.model.VoteContainerInfo;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;
import ee.vvk.ivotingverification.util.accessibility.AccessibilityDelegate;


public class VoteReceivedActivity extends Activity {

    private VoteContainerInfo voteContainerInfo;
    private QRCodeContents qrCodeContents;
    private final AccessibilityDelegate accessibilityDelegate = new AccessibilityDelegate();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.vote_activity);

        Intent intent = getIntent();
        voteContainerInfo = intent.getParcelableExtra(Util.VOTE_CONTAINER_INFO);
        qrCodeContents = intent.getParcelableExtra(Util.QR_CODE_CONTENTS);

        TextView lblVote = findViewById(R.id.vote_label);
        lblVote.setText(C.lblVote);
        lblVote.setTypeface(C.typeFace);
        lblVote.setTextColor(Util.generateHexColorValue(C.lblForeground));
        lblVote.setBackgroundColor(Util.generateHexColorValue(C.lblBackground));

        View lblShadow = findViewById(R.id.vote_label_shadow);
        lblShadow.setBackgroundColor(Util.generateHexColorValue(C.lblShadow));

        TextView lblVoteTxt = findViewById(R.id.vote_txt_label);
        lblVoteTxt.setTypeface(C.typeFace);
        lblVoteTxt.setText(C.lblVoteTxt);
        lblVoteTxt.setTextColor(Util.generateHexColorValue(C.lblOuterContainerForeground));

        TextView lblVoteSigner = findViewById(R.id.vote_signer_label);
        lblVoteSigner.setTypeface(C.typeFace);
        String lblVoteSignerText = getString(R.string.lblVoteSinger,C.lblVoteSigner,voteContainerInfo.getSignerCN());
        lblVoteSigner.setText(lblVoteSignerText);
        lblVoteSigner.setTextColor(Util.generateHexColorValue(C.lblOuterContainerForeground));
        accessibilityDelegate.textToLowercase(lblVoteSigner, lblVoteSignerText);

        Button btnVoteVerify = findViewById(R.id.vote_btn_verify);
        btnVoteVerify.setTypeface(C.typeFace);
        btnVoteVerify.setText(C.btnVerify);
        setVerifyButtonColors(btnVoteVerify);
        btnVoteVerify.setOnClickListener(this::clickVerifyButton);
        btnVoteVerify.setClickable(true);
        btnVoteVerify.setVisibility(android.view.View.VISIBLE);
    }

    private void setVerifyButtonColors(Button verifyButton) {
        verifyButton.setTextColor(Util.generateHexColorValue(C.btnVerifyForeground));

        int[] defaultColors = new int[3];
        defaultColors[0] = Util.generateHexColorValue(C.btnVerifyBackgroundStart);
        defaultColors[1] = Util.generateHexColorValue(C.btnVerifyBackgroundCenter);
        defaultColors[2] = Util.generateHexColorValue(C.btnVerifyBackgroundEnd);
        GradientDrawable bgVoteVerifyShapeDefault = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, defaultColors);
        bgVoteVerifyShapeDefault.setCornerRadius(5);
        verifyButton.setBackground(bgVoteVerifyShapeDefault);

        GradientDrawable bgVoteVerifyShapeFocused = new GradientDrawable();
        bgVoteVerifyShapeFocused.setCornerRadius(5);
        bgVoteVerifyShapeFocused.setColor(Util.generateHexColorValue(C.btnVerifyBackgroundFocused));

        verifyButton.setOnFocusChangeListener((view, isFocused) -> {
            if (isFocused) {
                view.setBackground(bgVoteVerifyShapeFocused);
            } else {
                view.setBackground(bgVoteVerifyShapeDefault);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    private void clickVerifyButton(View view) {
        Intent next_intent = new Intent(this, DecryptionActivity.class);
        next_intent.putExtra(Util.QR_CODE_CONTENTS, qrCodeContents);
        next_intent.putExtra(Util.VOTE_CONTAINER_INFO, voteContainerInfo);
        startActivity(next_intent);
        finish();
    }
}
