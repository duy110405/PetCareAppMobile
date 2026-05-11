package com.example.petcareapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.auth.DangNhapActivity;
import com.example.petcareapp.utils.ThemeManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // ✔ CHỈ INIT THEME (KHÔNG AUTO LOGIC Ở ĐÂY)
        ThemeManager.init(this);

        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.imgLogo);

        // ✔ ANIMATION MƯỢT
        ScaleAnimation scale = new ScaleAnimation(
                0.8f, 1f,
                0.8f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        );

        scale.setDuration(1200);
        scale.setFillAfter(true);

        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(1200);
        fade.setFillAfter(true);

        logo.startAnimation(scale);
        logo.startAnimation(fade);

        // ✔ delay chuyển màn
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            startActivity(new Intent(this, DangNhapActivity.class));
            finish();

        }, 2500);
    }
}