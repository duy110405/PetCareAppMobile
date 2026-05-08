package com.example.petcareapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.appcompat.app.AppCompatDelegate;

import android.os.Handler;
import android.os.Looper;

public class LightSensorHelper implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor lightSensor;
    private final SharedPreferences prefs;

    private boolean isDarkMode;
    private boolean isRegistered = false;

    private static final String PREF_NAME = "AppThemeSettings";
    private static final String KEY_AUTO_THEME = "auto_theme_enabled";

    private static final float DARK_THRESHOLD = 15f;
    private static final float LIGHT_THRESHOLD = 30f;
    private static final long DEBOUNCE_DELAY = 1200;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingRunnable;

    public LightSensorHelper(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager != null
                ? sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
                : null;

        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Lấy trạng thái theme THỰC TẾ (không dùng AppCompatDelegate)
        int nightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        isDarkMode = (nightMode == Configuration.UI_MODE_NIGHT_YES);
    }

    // ========================
    // Lifecycle
    // ========================

    public void register() {
        if (!isRegistered && sensorManager != null && lightSensor != null && isAutoThemeEnabled()) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            isRegistered = true;
        }
    }

    public void unregister() {
        if (isRegistered && sensorManager != null) {
            sensorManager.unregisterListener(this);
            isRegistered = false;
        }

        clearPendingTask();
    }

    // ========================
    // Sensor callback
    // ========================

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_LIGHT) return;

        float lux = event.values[0];

        boolean shouldDark = lux < DARK_THRESHOLD;
        boolean shouldLight = lux > LIGHT_THRESHOLD;

        if (shouldDark && !isDarkMode) {
            scheduleThemeChange(true);
        } else if (shouldLight && isDarkMode) {
            scheduleThemeChange(false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignore
    }

    // ========================
    // Core logic
    // ========================

    private void scheduleThemeChange(boolean toDarkMode) {
        clearPendingTask();

        pendingRunnable = () -> {
            if (isDarkMode == toDarkMode) return;

            isDarkMode = toDarkMode;

            AppCompatDelegate.setDefaultNightMode(
                    toDarkMode
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        };

        handler.postDelayed(pendingRunnable, DEBOUNCE_DELAY);
    }

    private void clearPendingTask() {
        if (pendingRunnable != null) {
            handler.removeCallbacks(pendingRunnable);
            pendingRunnable = null;
        }
    }

    // ========================
    // Preferences
    // ========================

    public boolean isAutoThemeEnabled() {
        return prefs.getBoolean(KEY_AUTO_THEME, false);
    }

    public void setAutoThemeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_THEME, enabled).apply();

        if (enabled) {
            register();
        } else {
            unregister();
        }
    }

    // ========================
    // Optional: debug helper
    // ========================

    public boolean isRegistered() {
        return isRegistered;
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }
}
