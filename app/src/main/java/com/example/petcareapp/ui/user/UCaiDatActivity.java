package com.example.petcareapp.ui.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.auth.DangNhapActivity;
import com.example.petcareapp.utils.LightSensorHelper;
import com.example.petcareapp.utils.MenuUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class UCaiDatActivity extends AppCompatActivity {

    private LinearLayout btnHoSo, btnDangXuat;
    private BottomNavigationView bottomNavigationView;
    private ImageView btnBack;

    // Nút gạt Tự động chuyển theme
    private Switch switchAutoTheme;

    // Lớp tiện ích quản lý cảm biến ánh sáng
    private LightSensorHelper lightSensorHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_cai_dat);

        // 1. Ánh xạ View
        btnHoSo = findViewById(R.id.btnHoSo);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        btnBack = findViewById(R.id.btnBack);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        switchAutoTheme = findViewById(R.id.switchAutoTheme);

        // 2. Khởi tạo Helper & Lấy trạng thái cài đặt hiện tại
        lightSensorHelper = new LightSensorHelper(this);
        boolean isAutoOn = lightSensorHelper.isAutoThemeEnabled();
        switchAutoTheme.setChecked(isAutoOn);

        // 3. Cài đặt Menu và nút Quay lại
        MenuUser.setup(this, bottomNavigationView);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 4. Xử lý các sự kiện bấm nút

        // Sự kiện gạt công tắc Bật/Tắt Cảm biến
        switchAutoTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            lightSensorHelper.setAutoThemeEnabled(isChecked);
        });

        // Nút Hồ sơ
        btnHoSo.setOnClickListener(v -> {
            Intent intent = new Intent(UCaiDatActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        // Nút Đăng xuất
        btnDangXuat.setOnClickListener(v -> hienThiBangHoiDangXuat());
    }

    // Đảm bảo cảm biến được kích hoạt lại (nếu đang bật) khi quay lại màn hình này
    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensorHelper != null) {
            lightSensorHelper.register();
        }
    }

    // Tắt cảm biến khi rời khỏi màn hình để tiết kiệm Pin
    @Override
    protected void onPause() {
        super.onPause();
        if (lightSensorHelper != null) {
            lightSensorHelper.unregister();
        }
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
                    finish(); // Kết thúc màn hình cài đặt
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}