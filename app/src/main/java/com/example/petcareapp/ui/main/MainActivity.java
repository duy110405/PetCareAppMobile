package com.example.petcareapp.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.admin.lichhen.AdminLichHenActivity;
import com.example.petcareapp.ui.user.TrangChu.UTrangChuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ===== CHECK LOGIN =====
        if (mAuth.getCurrentUser() == null) {
            // chưa login → về màn hình đăng nhập
            setContentView(R.layout.dangnhap);
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // ===== LẤY ROLE =====
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        String role = doc.getString("role");

                        if ("admin".equals(role)) {
                            // 👉 ADMIN
                            startActivity(new Intent(this, AdminLichHenActivity.class));
                        } else {
                            // 👉 USER (default)
                            startActivity(new Intent(this, UTrangChuActivity.class));
                        }

                        finish(); // đóng MainActivity
                    } else {
                        // không có user → quay về login
                        setContentView(R.layout.dangnhap);
                    }
                })
                .addOnFailureListener(e -> {
                    setContentView(R.layout.dangnhap);
                });
    }
}