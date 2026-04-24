package com.example.petcareapp.ui.user;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.auth.DangNhapActivity;
import com.example.petcareapp.utils.MenuUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class UCaiDatActivity extends AppCompatActivity implements SensorEventListener {

    private LinearLayout btnHoSo, btnCheDoSangToi, btnDangXuat;
    private BottomNavigationView bottomNavigationView;
    private ImageView btnBack;

    // ===== BIẾN CHO CẢM BIẾN ÁNH SÁNG =====
    private SensorManager sensorManager;
    private Sensor lightSensor;

    private float initialLightLevel = -1f;
    private boolean isInitialLightSet = false;

    // Chống chớp nháy màn hình (Debounce)
    private Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable themeSwitchRunnable;
    private boolean isTargetingDarkMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_cai_dat);

        // 1. Ánh xạ View
        btnHoSo = findViewById(R.id.btnHoSo);
        btnCheDoSangToi = findViewById(R.id.btnCheDoSangToi);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        btnBack = findViewById(R.id.btnBack);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // 2. Cài đặt Menu và nút Quay lại
        MenuUser.setup(this, bottomNavigationView);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 3. Khởi tạo Cảm biến Ánh sáng
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        // 4. Xử lý sự kiện bấm các nút
        btnDangXuat.setOnClickListener(v -> hienThiBangHoiDangXuat());
        btnHoSo.setOnClickListener(v -> Toast.makeText(this, "Chức năng Hồ sơ đang phát triển", Toast.LENGTH_SHORT).show());

        // Cho phép chuyển đổi thủ công (Click vào thay vì chờ cảm biến)
        btnCheDoSangToi.setOnClickListener(v -> {
            int currentNightMode = AppCompatDelegate.getDefaultNightMode();
            if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
    }

    // Bật cảm biến khi mở màn hình
    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // Tắt cảm biến khi thoát màn hình để tiết kiệm Pin
    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (themeSwitchRunnable != null) {
            debounceHandler.removeCallbacks(themeSwitchRunnable);
        }
    }

    // XỬ LÝ LOGIC ÁNH SÁNG
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float currentLight = event.values[0];

            // Thiết lập mốc ánh sáng môi trường lúc mới mở ứng dụng (Baseline)
            if (!isInitialLightSet) {
                initialLightLevel = currentLight;
                isInitialLightSet = true;
                return;
            }

            // Giao diện chuyển tối khi: Độ sáng giảm tương đối xuống dưới 40% so với mốc, HOẶC rất tối (< 10 lux)
            boolean shouldBeDarkMode = (currentLight < (initialLightLevel * 0.4f)) || (currentLight < 10f);

            // Logic Delay 1.2s chống nháy màn hình
            if (shouldBeDarkMode != isTargetingDarkMode) {
                isTargetingDarkMode = shouldBeDarkMode;

                if (themeSwitchRunnable != null) {
                    debounceHandler.removeCallbacks(themeSwitchRunnable);
                }

                themeSwitchRunnable = () -> switchTheme(isTargetingDarkMode);
                debounceHandler.postDelayed(themeSwitchRunnable, 1200);
            }
        }
    }

    private void switchTheme(boolean toDark) {
        int targetMode = toDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không bắt buộc xử lý
    }

    // GỌI HỘP THOẠI ĐĂNG XUẤT
    private void hienThiBangHoiDangXuat() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(UCaiDatActivity.this, DangNhapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}