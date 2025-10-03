package ee.vvk.ivotingverification;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import java.util.Objects;

import ee.vvk.ivotingverification.qr.CameraManager;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.DeviceOrientationManager;
import ee.vvk.ivotingverification.util.Util;
import ee.vvk.ivotingverification.util.accessibility.AccessibilityDelegate;

public abstract class BaseErrorActivity extends AppCompatActivity {

    private static final String TAG = BaseErrorActivity.class.getSimpleName();
    private DeviceOrientationManager screenOrientationManager;
    private final AccessibilityDelegate accessibilityDelegate = new AccessibilityDelegate();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Intent intent = getIntent();
        String errorTitle = intent.getStringExtra(Util.ERROR_TITLE);
        String errorMessage = intent.getStringExtra(Util.ERROR_MESSAGE);
        setContentView(R.layout.error_activity);

        PreviewView previewView = findViewById(R.id.error_previewView);
        new CameraManager(this)
                .cameraPreview(previewView, false, null);

        screenOrientationManager = initScreenOrientationManager();
        screenOrientationManager.registerDeviceRotationListener();
        screenOrientationManager.registerAutoRotateObserver();

        Util.logError(TAG, "Title: " + errorTitle + "\nMessage:" + errorMessage);
        initErrorWindow(errorTitle, errorMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        screenOrientationManager.unregisterDeviceRotationListener();
        screenOrientationManager.unregisterAutoRotateObserver();
    }

    protected void initErrorWindow(String errorTitle, String errorMessage) {

        LinearLayout frameBg = findViewById(R.id.frame_bg_error);
        frameBg.setBackgroundColor(Util.generateHexColorValue(C.frameBackground));

        LinearLayout linearLayout = findViewById(R.id.target_window_error);
        GradientDrawable bgShape = (GradientDrawable) linearLayout.getBackground();
        bgShape.setColor(Util.generateHexColorValue(C.errorWindow));

        linearLayout = findViewById(R.id.target_window_error_shadow);
        GradientDrawable bgShapeShadow = (GradientDrawable) linearLayout.getBackground();
        bgShapeShadow.setColor(Util.generateHexColorValue(C.errorWindowShadow));

        TextView textViewTitle = findViewById(R.id.text_title_error);
        TextView textViewMessage = findViewById(R.id.text_message_error);
        textViewTitle.setTypeface(C.typeFace);
        textViewMessage.setTypeface(C.typeFace);
        textViewTitle.setText(errorTitle);
        textViewMessage.setText(errorMessage);
        textViewTitle.setVisibility(View.VISIBLE);
        textViewTitle.setTextColor(Util.generateHexColorValue(C.errorWindowForeground));
        textViewMessage.setVisibility(View.VISIBLE);
        textViewMessage.setTextColor(Util.generateHexColorValue(C.errorWindowForeground));

        Button buttonWifi = findViewById(R.id.btn_left);
        buttonWifi.setTypeface(C.typeFace);
        setButtonColors(buttonWifi);

        Button buttonOk = findViewById(R.id.btn_right);
        buttonOk.setTypeface(C.typeFace);
        setButtonColors(buttonOk);

        ScrollView scrollView = findViewById(R.id.scrollView_error);
        accessibilityDelegate.handleScrollViewFocus(this, scrollView, textViewMessage);
    }

    private void setButtonColors(Button button) {
        button.setTextColor(Util.generateHexColorValue(C.btnForeground));

        GradientDrawable buttonShape = (GradientDrawable) button.getBackground();
        buttonShape.setColor(Util.generateHexColorValue(C.btnBackground));
        button.setOnFocusChangeListener((view, isFocused) -> {
            if (isFocused) {
                buttonShape.setColor(Util.generateHexColorValue(C.btnBackgroundFocused));
            } else {
                buttonShape.setColor(Util.generateHexColorValue(C.btnBackground));
            }
        });
    }

    private DeviceOrientationManager initScreenOrientationManager() {
        return new DeviceOrientationManager(this) {
            @Override
            public void rotateUI(int orientation) {
                RelativeLayout foregroundUserInterface = findViewById(R.id.foreground_ui_error);
                if (orientation == screenOrientationManager.PORTRAIT) {
                    foregroundUserInterface.setRotation(0);
                } else if (orientation == screenOrientationManager.LANDSCAPE_RIGHT) {
                    foregroundUserInterface.setRotation(270);
                } else if (orientation == screenOrientationManager.LANDSCAPE_LEFT) {
                    foregroundUserInterface.setRotation(90);
                }
            }
        };
    }

}
