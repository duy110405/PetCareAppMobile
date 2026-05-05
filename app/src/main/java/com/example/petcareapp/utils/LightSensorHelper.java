package com.example.petcareapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatDelegate;

public class LightSensorHelper implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;

    // Trạng thái hiện tại
    private boolean isDarkMode;

    private SharedPreferences prefs;
    private static final String PREF_NAME = "AppThemeSettings";
    private static final String KEY_AUTO_THEME = "auto_theme_enabled";

    // Handler để xử lý đệm thời gian (Debounce) - Chống chớp nháy
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable themeSwitchRunnable;

    public LightSensorHelper(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        // Lấy trạng thái theme hiện tại của App
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        isDarkMode = (currentMode == AppCompatDelegate.MODE_NIGHT_YES);
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void register() {
        // Chỉ bật cảm biến nếu người dùng đã tích chọn "Bật" trong Cài đặt
        if (lightSensor != null && isAutoThemeEnabled()) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregister() {
        if (lightSensor != null) {
            // Hủy lắng nghe khi tắt app để tiết kiệm pin
            sensorManager.unregisterListener(this);
        }
        if (handler != null && themeSwitchRunnable != null) {
            handler.removeCallbacks(themeSwitchRunnable);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];

            // Logic xác định môi trường sáng/tối
            // Dưới 15 lux: Tối -> Bật Dark Mode
            // Trên 30 lux: Sáng -> Bật Light Mode
            // Khoảng 15-30: Vùng đệm giúp không bị nháy qua lại khi ánh sáng lập lờ

            boolean shouldBeDark = lux < 15.0f;
            boolean shouldBeLight = lux > 30.0f;

            if (shouldBeDark && !isDarkMode) {
                scheduleThemeChange(true);
            } else if (shouldBeLight && isDarkMode) {
                scheduleThemeChange(false);
            }
        }
    }

    private void scheduleThemeChange(boolean toDarkMode) {
        // Hủy bỏ lệnh chuyển theme trước đó nếu có (để tính lại thời gian)
        if (themeSwitchRunnable != null) {
            handler.removeCallbacks(themeSwitchRunnable);
        }

        themeSwitchRunnable = () -> {
            isDarkMode = toDarkMode;
            if (toDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        };

        // Đợi 1200ms (1.2s) - Thỏa mãn điều kiện chuyển đổi mượt mà < 1.5s của đồ án
        // Nếu độ sáng duy trì ổn định trong 1.2s thì mới quyết định đổi theme
        handler.postDelayed(themeSwitchRunnable, 1200);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý
    }

    // 1. Kiểm tra xem người dùng có đang bật chế độ Tự động không
    public boolean isAutoThemeEnabled() {
        return prefs.getBoolean(KEY_AUTO_THEME, false); // Mặc định là TẮT (false)
    }

    // 2. Lưu cài đặt Bật/Tắt của người dùng
    public void setAutoThemeEnabled(boolean isEnabled) {
        prefs.edit().putBoolean(KEY_AUTO_THEME, isEnabled).apply();

        // Nếu người dùng tắt chế độ tự động, lập tức hủy cảm biến
        if (!isEnabled) {
            unregister();
        } else {
            register();
        }
    }


}