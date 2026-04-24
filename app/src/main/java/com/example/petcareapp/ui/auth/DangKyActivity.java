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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DangKyActivity extends AppCompatActivity {

    // Cập nhật các trường Input cho đúng với XML mới
    private TextInputEditText edtFullName, edtEmail, edtPhone, edtPassword;
    private MaterialButton btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangky);

        // Ánh xạ View
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, DangNhapActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // 1. KIỂM TRA ĐẦU VÀO TRƯỚC KHI ĐĂNG KÝ
        if (fullName.isEmpty()) {
            edtFullName.setError("Vui lòng nhập họ và tên");
            return;
        }
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Định dạng email không hợp lệ");
            return;
        }
        if (phone.isEmpty()) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            return;
        }
        if (phone.length() < 9 || phone.length() > 11) {
            edtPhone.setError("Số điện thoại không hợp lệ");
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
            return;
        }

        // Disable nút bấm để tránh spam click khi đang tải
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang xử lý...");

        // 2. TẠO TÀI KHOẢN VỚI FIREBASE AUTH
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {

                        String userId = mAuth.getCurrentUser().getUid();

                        // 3. LƯU THÔNG TIN ĐẦY ĐỦ VÀO FIRESTORE
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", email);
                        // ĐỔI "username" thành "hoTen" và "soDienThoai" để đồng bộ với tính năng Đặt lịch hẹn
                        userMap.put("hoTen", fullName);
                        userMap.put("soDienThoai", phone);
                        userMap.put("role", "user");

                        db.collection("users").document(userId).set(userMap)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, DangNhapActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("Đăng ký tài khoản");
                                    Toast.makeText(this, "Lỗi tạo hồ sơ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Đăng ký tài khoản");
                        Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}