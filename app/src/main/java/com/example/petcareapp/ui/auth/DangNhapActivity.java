package com.example.petcareapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.User;
import com.example.petcareapp.ui.admin.AChiNhanhActivity; // Sửa thành trang chủ Admin của bạn
import com.example.petcareapp.ui.admin.lichhen.AdminLichHenActivity;
import com.example.petcareapp.ui.user.TrangChu.UTrangChuActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        mAuth.signOut();
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, DangKyActivity.class)));
        btnLogin.setOnClickListener(v -> loginUser());

        // 🔥 nếu đã login trước đó → kiểm tra luôn
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserStatusAndRole(currentUser.getUid());
        }
    }

    private void loginUser() {
        String email = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // validate
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

        // firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserStatusAndRole(user.getUid());
                        }

                    } else {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 🔥 Hàm quan trọng nhất:
     * - Check bị khóa
     * - Check role
     * - Điều hướng
     */
    private void checkUserStatusAndRole(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        // 🔒 Check khóa tài khoản
                        Boolean locked = doc.getBoolean("locked");
                        if (locked != null && locked) {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(this, "Tài khoản đã bị khóa!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 🔑 Lấy role
                        String role = doc.getString("role");

                        // 🔀 Điều hướng theo role
                        if ("admin".equals(role)) {
                            startActivity(new Intent(this, AdminLichHenActivity.class));
                        } else {
                            startActivity(new Intent(this, UTrangChuActivity.class));
                        }

                        finish();

                    } else {
                        Toast.makeText(this, "Lỗi: Không tìm thấy hồ sơ người dùng", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}