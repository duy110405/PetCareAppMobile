package com.example.petcareapp.Login_Register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.auth.FirebaseAuth;



public class DangKyActivity extends AppCompatActivity {

    TextInputEditText edtUsername, edtPassword, edtGmail;
    MaterialButton btnRegister;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangky);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtGmail = findViewById(R.id.edtGmail);
        btnRegister = findViewById(R.id.btnRegister);

        TextView tvLogin = findViewById(R.id.tvLogin);

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, DangNhapActivity.class));
        });


        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(v -> registerUser());


    }

    private void registerUser() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String input = edtGmail.getText().toString().trim();

        // 🔎 Validate
        if (username.isEmpty()) {
            edtUsername.setError("Nhập tên đăng nhập");
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu >= 6 ký tự");
            return;
        }

        if (input.isEmpty()) {
            edtGmail.setError("Nhập email hoặc số điện thoại");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            edtGmail.setError("Firebase chỉ hỗ trợ email (tạm thời)");
            return;
        }

        // 🔥 Firebase Register
        mAuth.createUserWithEmailAndPassword(input, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                        // 👉 chuyển sang login
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
