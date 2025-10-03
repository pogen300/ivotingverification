package ee.vvk.ivotingverification.qr;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.Util;

@SuppressWarnings("ALL")
public final class CameraManager extends AppCompatActivity {

    private static final String TAG = CameraManager.class.getSimpleName();
    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 940;
    private static final int MAX_FRAME_HEIGHT = 540;
    private final Context context;
    private int cameraId;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private boolean reverseImage;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider processCameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private int resoWidth;
    private int resoHeight;

    public CameraManager(Context context) {
        this.context = context;
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void cameraPreview(PreviewView previewView, boolean allowQRScanning, QRAnalyzer qrAnalyzer) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Util.startErrorIntent((Activity) context, C.badDeviceMessage);
            return;
        }
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraExecutor = Executors.newSingleThreadExecutor();
        try {
            processCameraProvider = cameraProviderFuture.get();
        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
        }
        cameraProviderFuture.addListener(() -> {
            cameraProviderFutureListener(previewView, allowQRScanning, qrAnalyzer);
        }, ContextCompat.getMainExecutor(context));
    }

    private void cameraProviderFutureListener(PreviewView previewView, boolean allowQRScanning, QRAnalyzer qrAnalyzer) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        processCameraProvider.unbindAll();
        if (allowQRScanning) {
            ImageCapture imageCapture = new ImageCapture.Builder().build();
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();
            imageAnalysis.setAnalyzer(cameraExecutor, qrAnalyzer);
            processCameraProvider.bindToLifecycle(
                    (LifecycleOwner) context, cameraSelector, preview, imageCapture, imageAnalysis
            );
        } else {
            processCameraProvider.bindToLifecycle(
                    (LifecycleOwner) context, cameraSelector, preview
            );
        }
    }
}
