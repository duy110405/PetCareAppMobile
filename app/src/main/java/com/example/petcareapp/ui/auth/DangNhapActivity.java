package com.example.petcareapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;


import com.example.petcareapp.ui.user.UserActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class DangNhapActivity extends AppCompatActivity {
    private TextInputEditText edtUsername, edtPassword;
    private MaterialButton btnLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangnhap);

        // 🔗 ánh xạ view
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        // 🔥 Firebase
        mAuth = FirebaseAuth.getInstance();

        // 👉 chuyển sang đăng ký
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this,  DangKyActivity.class));
        });

        // 👉 xử lý đăng nhập
        btnLogin.setOnClickListener(v -> loginUser());

        // 🔥 Auto login (có thể tắt nếu test UI)
        if (mAuth.getCurrentUser() != null) {
            goToHome();
        }
    }

    private void loginUser() {
        String email = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // ❗ validate
        if (email.isEmpty()) {
            edtUsername.setError("Nhập email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtUsername.setError("Email không hợp lệ");
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu >= 6 ký tự");
            return;
        }

        // 🔐 Firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        goToHome();

                    } else {
                        Toast.makeText(this,
                                "Sai tài khoản hoặc mật khẩu",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToHome() {
        startActivity(new Intent(this, UserActivity.class));
        finish();
    }

}
