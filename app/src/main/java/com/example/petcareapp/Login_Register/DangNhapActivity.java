package com.example.petcareapp.Login_Register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.MainActivity;
import com.example.petcareapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class DangNhapActivity extends AppCompatActivity {
    TextInputEditText edtUsername, edtPassword;
    MaterialButton btnLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangnhap);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, DangKyActivity.class));
        });


        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());



        // 🔥 Auto login
        if (mAuth.getCurrentUser() != null) {
            goToHome();
        }
    }

    private void loginUser() {
        String email = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty()) {
            edtUsername.setError("Nhập email");
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Nhập mật khẩu");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        goToHome();

                    } else {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


}
