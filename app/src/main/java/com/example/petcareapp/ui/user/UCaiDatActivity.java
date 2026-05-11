package com.example.petcareapp.ui.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.auth.DangNhapActivity;
import com.example.petcareapp.utils.LightSensorHelper;
import com.example.petcareapp.utils.MenuUser;
import com.example.petcareapp.utils.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class UCaiDatActivity extends AppCompatActivity {

    private LinearLayout btnHoSo, btnDangXuat;
    private BottomNavigationView bottomNavigationView;
    private ImageView btnBack;

    private Switch switchAutoTheme;
    private LightSensorHelper lightSensorHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        setContentView(R.layout.user_cai_dat);

        btnHoSo = findViewById(R.id.btnHoSo);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        btnBack = findViewById(R.id.btnBack);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        switchAutoTheme = findViewById(R.id.switchAutoTheme);

        MenuUser.setup(this, bottomNavigationView);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // INIT SENSOR
        lightSensorHelper = new LightSensorHelper(this);

        // LOAD STATE
        boolean isAuto = ThemeManager.isAutoTheme(this);
        switchAutoTheme.setChecked(isAuto);
        lightSensorHelper.setEnabled(isAuto);

        // SWITCH AUTO THEME
        switchAutoTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {

            lightSensorHelper.setEnabled(isChecked);
            ThemeManager.setAutoTheme(this, isChecked);

            if (isChecked) {
                lightSensorHelper.register();
            } else {
                lightSensorHelper.unregister();

                applyThemeSmooth(ThemeManager.isDarkMode(this));
            }
        });

        btnHoSo.setOnClickListener(v -> {
            startActivity(new Intent(this, UserProfileActivity.class));
        });

        btnDangXuat.setOnClickListener(v -> hienThiBangHoiDangXuat());
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isAuto = ThemeManager.isAutoTheme(this);
        lightSensorHelper.syncState(isAuto);

        if (isAuto) {
            lightSensorHelper.register();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (lightSensorHelper != null) {
            lightSensorHelper.unregister();
        }
    }
    private void applyThemeSmooth(boolean dark) {

        View root = getWindow().getDecorView();

        root.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {

                    ThemeManager.applyTheme(dark);
                    recreate();

                    getWindow().getDecorView().setAlpha(0f);
                    getWindow().getDecorView().animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                })
                .start();
    }

    private void hienThiBangHoiDangXuat() {

        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {

                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(
                            UCaiDatActivity.this,
                            DangNhapActivity.class
                    );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}