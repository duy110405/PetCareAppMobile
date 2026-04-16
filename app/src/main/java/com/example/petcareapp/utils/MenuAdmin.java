package com.example.petcareapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.admin.AChiNhanhActivity;
import com.example.petcareapp.ui.admin.ADichVuActivity;
import com.example.petcareapp.ui.admin.ALichHenActivity;
import com.example.petcareapp.ui.admin.ANguoiDungActivity;
import com.example.petcareapp.ui.auth.DangNhapActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class MenuAdmin {
    // TẠO TỪ ĐIỂN: Chỉ chứa các màn hình chuyển hướng thông thường
    private static final Map<Integer, Class<?>> mapMenu = new HashMap<>();

    static {
        mapMenu.put(R.id.nav_chinhanh, AChiNhanhActivity.class);
        mapMenu.put(R.id.nav_lichhen, ALichHenActivity.class);
        mapMenu.put(R.id.nav_dichvu, ADichVuActivity.class);
        mapMenu.put(R.id.nav_nguoidung, ANguoiDungActivity.class);
    }

    public static void setup(Activity activity, BottomNavigationView navView) {

        // Tự động sáng đèn tab hiện tại
        for (Map.Entry<Integer, Class<?>> entry : mapMenu.entrySet()) {
            if (entry.getValue().equals(activity.getClass())) {
                navView.setSelectedItemId(entry.getKey());
                break;
            }
        }

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            // KIỂM TRA ĐẶC BIỆT CHO NÚT THOÁT
            if (itemId == R.id.nav_thoat) {
                hienThiBangHoiThoat(activity);
                return false; // Trả về false để nó KHÔNG đổi màu tab đang chọn
            }
            // LOGIC CHUYỂN TRANG BÌNH THƯỜNG (Từ điển)
            Class<?> targetActivity = mapMenu.get(itemId);

            if (targetActivity != null && activity.getClass() != targetActivity) {
                Intent intent = new Intent(activity, targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    // Hàm xử lý riêng chức năng Thoát
    private static void hienThiBangHoiThoat(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn thoát khỏi phiên làm việc?")
                .setPositiveButton("Thoát", (dialog, which) -> {
                    // Đăng xuất Firebase (Nếu bạn có dùng Firebase Authentication)
                     FirebaseAuth.getInstance().signOut();
                    // Chuyển về màn hình Đăng Nhập
                    Intent intent = new Intent(activity, DangNhapActivity.class);
                    // FLAG này cực kỳ quan trọng: Xóa sạch toàn bộ lịch sử màn hình Admin đã mở,
                    // để người dùng không bấm nút Back quay lại được nữa.
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                    activity.finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}