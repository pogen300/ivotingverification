package ee.vvk.ivotingverification;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;


public class ErrorActivity extends BaseErrorActivity {

    private static final String TAG = ErrorActivity.class.getSimpleName();

    protected void initErrorWindow(String errorTitle, String errorMessage) {
        super.initErrorWindow(errorTitle, errorMessage);

        Button buttonLeft = findViewById(R.id.btn_left);
        Button buttonRight = findViewById(R.id.btn_right);

        buttonLeft.setText(C.btnMore);
        buttonLeft.setVisibility(View.VISIBLE);
        buttonLeft.setOnClickListener(this::clickMoreButton);

        buttonRight.setText(C.btnOk);
        buttonRight.setVisibility(View.VISIBLE);
        buttonRight.setOnClickListener(v -> finish());

    }

    private void clickMoreButton(View view) {
        Intent showHelp = new Intent(Intent.ACTION_VIEW, Uri.parse(C.helpURL));
        try {
            startActivity(showHelp);
        } catch (ActivityNotFoundException e) {
            Util.logDebug(TAG, "Error while displaying help: ", e);
        }
    }


}
