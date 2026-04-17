package com.example.petcareapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.admin.AChiNhanhActivity; // Sửa thành trang chủ Admin của bạn
import com.example.petcareapp.ui.user.TrangChu.UTrangChuActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore; // THÊM IMPORT

public class DangNhapActivity extends AppCompatActivity {
    private TextInputEditText edtUsername, edtPassword;
    private MaterialButton btnLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Khai báo Database

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangnhap);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Khởi tạo Database

        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, DangKyActivity.class)));
        btnLogin.setOnClickListener(v -> loginUser());

        if (mAuth.getCurrentUser() != null) {
            checkUserRole(mAuth.getCurrentUser().getUid());
        }
    }

    private void loginUser() {
        String email = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // (Validate giữ nguyên...)
        if (email.isEmpty()) { edtUsername.setError("Nhập email"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { edtUsername.setError("Email không hợp lệ"); return; }
        if (password.isEmpty()) { edtPassword.setError("Nhập mật khẩu"); return; }
        if (password.length() < 6) { edtPassword.setError("Mật khẩu >= 6 ký tự"); return; }

        // Firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        // KHÔNG CHUYỂN TRANG NGAY. MÀ ĐI KIỂM TRA QUYỀN TRƯỚC
                        String userId = mAuth.getCurrentUser().getUid();
                        checkUserRole(userId);

                    } else {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Truy vấn Firestore để phân luồng Admin / User
    private void checkUserRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        if ("admin".equals(role)) {
                            // Là Admin Vào trang Quản lý
                            startActivity(new Intent(this, AChiNhanhActivity.class)); // Đổi thành trang ALichHenActivity nếu muốn
                        } else {
                            // Là User Vào trang Chủ
                            startActivity(new Intent(this, UTrangChuActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi: Không tìm thấy hồ sơ người dùng", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kiểm tra quyền: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}