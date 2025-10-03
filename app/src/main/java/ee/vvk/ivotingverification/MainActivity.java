package ee.vvk.ivotingverification;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.security.Security;
import java.util.Objects;

import ee.vvk.ivotingverification.dialog.LoadingSpinner;
import ee.vvk.ivotingverification.qr.CameraManager;
import ee.vvk.ivotingverification.qr.InactivityTimer;
import ee.vvk.ivotingverification.tasks.AsyncTaskActivity;
import ee.vvk.ivotingverification.tasks.ConfigTaskResult;
import ee.vvk.ivotingverification.tasks.GetConfigTask;
import ee.vvk.ivotingverification.tasks.TaskRunner;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.DeviceOrientationManager;
import ee.vvk.ivotingverification.util.JSONParser;
import ee.vvk.ivotingverification.util.Util;
import ee.vvk.ivotingverification.util.accessibility.AccessibilityDelegate;

public class MainActivity extends AppCompatActivity implements AsyncTaskActivity<ConfigTaskResult> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private InactivityTimer inactivityTimer;
    private LoadingSpinner mLoadingSpinner;
    private Button buttonMore;
    private Button buttonNext;
    private PreviewView previewView;
    private DeviceOrientationManager screenOrientationManager;
    private AccessibilityDelegate accessibilityDelegate = new AccessibilityDelegate();

    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return true;
        }
        boolean all_deleted = true;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    all_deleted &= deleteDir(new File(dir, child));
                }
            }
        }
        if (all_deleted) {
            return dir.delete();
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();



        C.configURL = BuildConfig.CONFIG_FILE;
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        Util.DEBUGGABLE = (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
        if (getIntent().getBooleanExtra(Util.EXIT, false)) {
            finish();
        }

        inactivityTimer = new InactivityTimer(this);
        setContentView(R.layout.main_activity);
        previewView = findViewById(R.id.main_previewView);

        LinearLayout linearLayout = findViewById(R.id.target_window);
        GradientDrawable bgShape = (GradientDrawable) linearLayout.getBackground();
        bgShape.setColor(Color.GRAY);

        if (Util.isNetworkConnected(this)) {
            TaskRunner runner = new TaskRunner();
            runner.executeAsync(new GetConfigTask(this, C.configURL));
        } else {
            Util.startNetworkErrorIntent(MainActivity.this);
        }

        LinearLayout frameBg = findViewById(R.id.frame_bg);
        frameBg.setBackgroundColor(Util.generateHexColorValue(C.frameBackground));

        buttonNext = findViewById(R.id.btn_next);
        buttonNext.setText(C.btnNext);
        setButtonColors(buttonNext);
        buttonNext.setOnClickListener(this::clickNextButton);

        buttonMore = findViewById(R.id.btn_more);
        buttonMore.setText(C.btnMore);
        setButtonColors(buttonMore);
        buttonMore.setOnClickListener(this::clickMoreButton);

        screenOrientationManager = initScreenOrientationManager();
        screenOrientationManager.registerDeviceRotationListener();
        screenOrientationManager.registerAutoRotateObserver();
    }

    @Override
    public void onResume() {
        super.onResume();
        inactivityTimer.onResume();
        new CameraManager(this)
                .cameraPreview(previewView, false, null);
    }

    @Override
    public void onPause() {
        inactivityTimer.onPause();
        super.onPause();
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

    private void clickNextButton(View view) {
        Intent QRCodeDecoder = new Intent(this, QRScannerActivity.class);
        startActivity(QRCodeDecoder);
    }

    private void clickMoreButton(View view) {
        Intent showHelp = new Intent(Intent.ACTION_VIEW, Uri.parse(C.helpURL));
        try {
            startActivity(showHelp);
        } catch (ActivityNotFoundException e) {
            Util.logDebug(TAG, "Error while displaying help: ", e);
        }
    }


    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
        Util.stopSpinner(mLoadingSpinner);
        screenOrientationManager.unregisterDeviceRotationListener();
        screenOrientationManager.unregisterAutoRotateObserver();
        try {
            deleteDir(getApplicationContext().getCacheDir());
        } catch (Exception e) {
            Util.logDebug(TAG, "Error while cleaning app cache: ", e);
        }
    }

    private DeviceOrientationManager initScreenOrientationManager() {
        return new DeviceOrientationManager(this) {
            @Override
            public void rotateUI(int orientation) {
                RelativeLayout foregroundUserInterface = findViewById(R.id.foreground_ui_main);
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

    private void initMainWindow() {

        LinearLayout linearLayout = findViewById(R.id.target_window);
        GradientDrawable bgShape = (GradientDrawable) linearLayout.getBackground();
        bgShape.setColor(Util.generateHexColorValue(C.mainWindow));

        linearLayout = findViewById(R.id.target_window_shadow);
        GradientDrawable bgShapeShadow = (GradientDrawable) linearLayout.getBackground();
        bgShapeShadow.setColor(Util.generateHexColorValue(C.mainWindowShadow));

        LinearLayout linearLayoutShadow = findViewById(R.id.window_shadow);
        linearLayoutShadow.setVisibility(View.VISIBLE);

        TextView textView = findViewById(R.id.text_message);
        textView.setTypeface(C.typeFace);
        textView.setText(C.welcomeMessage);
        textView.setVisibility(View.VISIBLE);
        textView.setTextColor(Util.generateHexColorValue(C.mainWindowForeground));

        buttonMore.setText(C.btnMore);
        buttonMore.setVisibility(View.VISIBLE);
        buttonMore.setTypeface(C.typeFace);

        buttonNext.setText(C.btnNext);
        buttonNext.setVisibility(View.VISIBLE);
        buttonNext.setTypeface(C.typeFace);

        ImageView frameImage = findViewById(R.id.frame_image);
        frameImage.setVisibility(View.GONE);

        ScrollView scrollView = findViewById(R.id.scrollview_main);
        accessibilityDelegate.handleScrollViewFocus(this, scrollView, textView);
    }

    public void onPreExecute() {
        mLoadingSpinner = Util.startSpinner(this, true);
    }

    public void onPostExecute(ConfigTaskResult result) {
        try {
            if (result.isFailure()) {
                Util.startErrorIntent(MainActivity.this, result.errorMessage);
                return;
            }
            try {
                JSONParser.parseConfig(result.value);
            } catch (Exception e) {
                Util.logException(TAG, e);
                Util.startErrorIntent(MainActivity.this, C.badConfigMessage);
                return;
            }

            if (!isApplicationUpToDate(C.expectedVersion)) {
                Util.startVersionErrorIntent(MainActivity.this);
                return;
            }

            Util.stopSpinner(mLoadingSpinner);
            initMainWindow();
        } catch (Exception e) {
            Util.logException(TAG, e);
        }
    }

    private boolean isApplicationUpToDate(long expectedVersion)
            throws PackageManager.NameNotFoundException {
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
        return packageInfo.versionCode >= expectedVersion;
    }
}
