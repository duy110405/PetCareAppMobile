package com.example.petcareapp.utils;

import android.app.Activity;
import android.content.Intent;
import com.example.petcareapp.R;

import com.example.petcareapp.ui.user.UCaiDatActivity;
import com.example.petcareapp.ui.user.UDaoChoiActivity;
import com.example.petcareapp.ui.user.ULichHenActivity;
import com.example.petcareapp.ui.user.UTimPhongActivity;
import com.example.petcareapp.ui.user.UTrangChuActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class MenuUser {
    // TẠO TỪ ĐIỂN: Ghép cặp [ID Menu] -> [Màn hình Activity]
    private static final Map<Integer, Class<?>> mapMenu = new HashMap<>();
    static {
        mapMenu.put(R.id.nav_trangchu, UTrangChuActivity.class);
        mapMenu.put(R.id.nav_lichhen, ULichHenActivity.class);
        mapMenu.put(R.id.nav_phongkham, UTimPhongActivity.class);
        mapMenu.put(R.id.nav_nguoidung, UDaoChoiActivity.class);
        mapMenu.put(R.id.nav_thoat, UCaiDatActivity.class);
    }
    public static void setup(Activity activity, BottomNavigationView navView) {

// Tự động sáng đèn tab hiện tại (Dò ngược từ điển)
        for (Map.Entry<Integer, Class<?>> entry : mapMenu.entrySet()) {
            if (entry.getValue().equals(activity.getClass())) {
                navView.setSelectedItemId(entry.getKey());
                break;
            }
        }
// Tự động chuyển trang khi bấm
        navView.setOnItemSelectedListener(item -> {
// Tra từ điển xem ID này ứng với Màn hình nào
            Class<?> targetActivity = mapMenu.get(item.getItemId());
// Nếu tìm thấy màn hình, và màn hình đó KHÁC màn hình đang đứng thì mới mở
            if (targetActivity != null && activity.getClass() != targetActivity) {
                Intent intent = new Intent(activity, targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0); // Tắt hiệu ứng chớp
            }
            return true;
        });
    }
}
