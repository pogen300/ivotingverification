package ee.vvk.ivotingverification;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import ee.vvk.ivotingverification.util.C;

public class NetworkErrorActivity extends BaseErrorActivity {

    protected void initErrorWindow(String errorTitle, String errorMessage) {
        super.initErrorWindow(errorTitle, errorMessage);

        Button buttonLeft = findViewById(R.id.btn_left);

        buttonLeft.setText(C.errorRetryButton);
        buttonLeft.setVisibility(View.VISIBLE);
        buttonLeft.setOnClickListener(this::clickRetryButton);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) buttonLeft.getLayoutParams();
        params.weight = 2;
        buttonLeft.setLayoutParams(params);

    }

    public void clickRetryButton(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}
