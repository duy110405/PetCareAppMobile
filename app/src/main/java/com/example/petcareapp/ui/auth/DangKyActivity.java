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
import com.google.firebase.firestore.FirebaseFirestore; // THÊM IMPORT

import java.util.HashMap;
import java.util.Map;

public class DangKyActivity extends AppCompatActivity {

    private TextInputEditText edtUsername, edtPassword, edtGmail, edtPhone;
    private MaterialButton btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Khai báo Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangky);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtGmail = findViewById(R.id.edtGmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Khởi tạo Database

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, DangNhapActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = edtUsername.getText().toString().trim();
        String email = edtGmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (username.isEmpty()) { edtUsername.setError("Nhập tên đăng nhập"); return; }
        if (email.isEmpty()) { edtGmail.setError("Nhập email"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { edtGmail.setError("Email không hợp lệ"); return; }
        if (phone.isEmpty()) {edtPhone.setError("Nhập số điện thoại");return;}
        if (phone.length() < 9 || phone.length() > 11) {edtPhone.setError("Số điện thoại không hợp lệ");return;}
        if (password.isEmpty()) { edtPassword.setError("Nhập mật khẩu"); return; }
        if (password.length() < 6) { edtPassword.setError("Mật khẩu >= 6 ký tự"); return; }

        // BƯỚC 1: Đăng ký với Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Lấy ID của người dùng vừa tạo
                        String userId = mAuth.getCurrentUser().getUid();

                        // Lưu thông tin và Role "user" vào Firestore
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", email);
                        userMap.put("phone", phone);
                        userMap.put("username", username);
                        userMap.put("role", "user"); // MẶC ĐỊNH AI ĐĂNG KÝ CŨNG LÀ USER

                        db.collection("users").document(userId).set(userMap)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, DangNhapActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}