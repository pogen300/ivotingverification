package ee.vvk.ivotingverification;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import ee.vvk.ivotingverification.util.C;

public class VersionErrorActivity extends BaseErrorActivity {

    protected void initErrorWindow(String errorTitle, String errorMessage) {
        super.initErrorWindow(errorTitle, errorMessage);

        Button buttonLeft = findViewById(R.id.btn_left);
        Button buttonRight = findViewById(R.id.btn_right);

        buttonLeft.setText(C.btnUpdate);
        buttonLeft.setVisibility(View.VISIBLE);
        buttonLeft.setOnClickListener(this::clickUpdateButton);

        buttonRight.setText(C.btnOk);
        buttonRight.setVisibility(View.VISIBLE);
        buttonRight.setOnClickListener(v -> finish());
    }

    private void clickUpdateButton(View view) {
        VersionErrorActivity.this.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=ee.ivxv.ivotingverification")));
    }

}
