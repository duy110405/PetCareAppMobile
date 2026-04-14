package com.example.petcareapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;



public class DangKyActivity extends AppCompatActivity {

    private TextInputEditText edtUsername, edtPassword, edtGmail;
    private MaterialButton btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangky);

        // 🔗 ánh xạ view
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtGmail = findViewById(R.id.edtGmail);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin); // nút quay lại login

        // 🔥 Firebase
        mAuth = FirebaseAuth.getInstance();

        // 👉 quay lại màn đăng nhập
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, DangNhapActivity.class));
            finish();
        });

        // 👉 xử lý đăng ký
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = edtUsername.getText().toString().trim();
        String email = edtGmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // ❗ validate
        if (username.isEmpty()) {
            edtUsername.setError("Nhập tên đăng nhập");
            return;
        }

        if (email.isEmpty()) {
            edtGmail.setError("Nhập email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtGmail.setError("Email không hợp lệ");
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

        // 🔥 Firebase đăng ký
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Toast.makeText(this,
                                "Đăng ký thành công",
                                Toast.LENGTH_SHORT).show();

                        // 👉 quay về login
                        startActivity(new Intent(this, DangNhapActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this,
                                "Lỗi: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
