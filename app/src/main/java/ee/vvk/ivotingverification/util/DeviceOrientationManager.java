package ee.vvk.ivotingverification.util;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

/**
 * This class is used since the app needs to be accessible in landscape mode.
 * App uses accelerometer sensor to listen device rotations and then rotate the UI based on that.
 * This solution is used in the activities that have an active camera in the background and some
 * layout in the foreground. Current solution sets the app in portrait mode, so the camera preview
 * will not know when the device is rotated and only the foreground UI reacts to it.
 * When keeping the activity (avoiding the activity recreation) on orientation changes,
 * then sometimes the camera preview of CameraX API gets confused about the aspect ratio of the
 * preview frames. It seems to be a bug from CameraX side so it might be solved in future releases.
 * -----
 * DeviceRotationListener - listens rotation changes based on accelerometer sensor information
 * AutoRotateObserver - observer to monitor changes in the auto-rotation setting - if orientation is locked or not
 */

public abstract class DeviceOrientationManager {

    public final int PORTRAIT = 1;
    public final int LANDSCAPE_RIGHT = 2;
    public final int LANDSCAPE_LEFT = 3;
    private int currentOrientation = PORTRAIT;
    private final Context context;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;

    private AutoRotateObserver autoRotateObserver;

    public DeviceOrientationManager(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void registerDeviceRotationListener() {
        if (isAutoRotateEnabled(context)) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterDeviceRotationListener() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            int newOrientation = getDeviceOrientation(values[0], values[1]);
            if (newOrientation >= 0 && newOrientation != currentOrientation) {
                currentOrientation = newOrientation;
                rotateUI(newOrientation);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // No-op
        }
    };

    private int getDeviceOrientation(float x, float y) {
        if (Math.abs(Math.abs(x) - Math.abs(y)) > 3) {
            if (Math.abs(x) < Math.abs(y)) {
                if (y > 0) return PORTRAIT;
                else return currentOrientation; // ignore orientation change when upside-down
            } else {
                if (x > 0) return LANDSCAPE_LEFT;
                else return LANDSCAPE_RIGHT;
            }
        }
        return -1;
    }

    public void registerAutoRotateObserver() {
        autoRotateObserver = new AutoRotateObserver(new Handler(Looper.getMainLooper()), context);
        context.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                false, // set to false as we're only interested in the ACCELEROMETER_ROTATION setting
                autoRotateObserver
        );
    }

    public void unregisterAutoRotateObserver() {
        context.getContentResolver().unregisterContentObserver(autoRotateObserver);
    }

    private boolean isAutoRotateEnabled(Context context) {
        try {
            return Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION
            ) == 1;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    public abstract void rotateUI(int orientation);

    private class AutoRotateObserver extends ContentObserver {
        private final Context context;

        public AutoRotateObserver(Handler handler, Context context) {
            super(handler);
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (isAutoRotateEnabled(context)) {
                sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                sensorManager.unregisterListener(sensorEventListener);
            }
        }
    }
}
