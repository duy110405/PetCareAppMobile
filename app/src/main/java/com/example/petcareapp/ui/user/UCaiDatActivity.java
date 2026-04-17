package com.example.petcareapp.ui.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.auth.DangNhapActivity;
import com.example.petcareapp.utils.MenuUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class UCaiDatActivity extends AppCompatActivity {

    private LinearLayout btnHoSo, btnCheDoSangToi, btnDangXuat;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_cai_dat);

        // 1. Ánh xạ View
        btnHoSo = findViewById(R.id.btnHoSo);
        btnCheDoSangToi = findViewById(R.id.btnCheDoSangToi);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        MenuUser.setup(this, bottomNav);
        // 2. Cài đặt thanh Menu dưới cùng (Gọi từ điển MenuUser)
        MenuUser.setup(this, bottomNavigationView);

        // 3. Xử lý sự kiện bấm Đăng xuất
        btnDangXuat.setOnClickListener(v -> hienThiBangHoiDangXuat());

        // (Tùy chọn) Xử lý các nút khác tạm thời
        btnHoSo.setOnClickListener(v -> Toast.makeText(this, "Chức năng Hồ sơ đang phát triển", Toast.LENGTH_SHORT).show());
        btnCheDoSangToi.setOnClickListener(v -> Toast.makeText(this, "Chức năng Giao diện đang phát triển", Toast.LENGTH_SHORT).show());
    }

    private void hienThiBangHoiDangXuat() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    //  Xóa phiên đăng nhập trên Firebase
                    FirebaseAuth.getInstance().signOut();

                    // Chuyển về màn hình Đăng Nhập
                    Intent intent = new Intent(UCaiDatActivity.this, DangNhapActivity.class);

                    // Xóa sạch lịch sử màn hình
                    // Tránh trường hợp người dùng đã đăng xuất nhưng bấm phím Back lại lọt vào app
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                    finish(); // Kết thúc màn hình Cài đặt
                })
                .setNegativeButton("Hủy", null) // Bấm hủy thì không làm gì cả, tự đóng bảng
                .show();
    }
}