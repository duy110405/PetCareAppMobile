package com.example.petcareapp.utils;

import android.app.Activity;
import android.content.Intent;
import com.example.petcareapp.R;
//import com.example.petcareapp.ui.user.UCaiDatActivity;
//import com.example.petcareapp.ui.user.UDaoChoiActivity;
import com.example.petcareapp.ui.user.ULichHenActivity;
import com.example.petcareapp.ui.user.UTimPhongActivity;
import com.example.petcareapp.ui.user.TrangChu.UTrangChuActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class MenuUser {
    // TẠO TỪ ĐIỂN: Ghép cặp [ID của Menu trong XML] -> [Lớp Activity tương ứng]
    private static final Map<Integer, Class<?>> mapMenu = new HashMap<>();

    static {
        mapMenu.put(R.id.nav_trangchu, UTrangChuActivity.class);
        mapMenu.put(R.id.nav_lichhen, ULichHenActivity.class);
        mapMenu.put(R.id.nav_phongkham, UTimPhongActivity.class);
        //mapMenu.put(R.id.nav_nguoidung, UDaoChoiActivity.class);
        //mapMenu.put(R.id.nav_thoat, UCaiDatActivity.class);
    }

    /**
     * Hàm thiết lập Bottom Navigation cho Activity
     * @param activity Activity hiện tại
     * @param navView View BottomNavigationView từ Layout
     */
    public static void setup(Activity activity, BottomNavigationView navView) {

        // 1. Tự động làm sáng đèn tab hiện tại bằng cách dò trong từ điển
        for (Map.Entry<Integer, Class<?>> entry : mapMenu.entrySet()) {
            if (entry.getValue().equals(activity.getClass())) {
                navView.setSelectedItemId(entry.getKey());
                break;
            }
        }

        // 2. Xử lý sự kiện khi người dùng nhấn chuyển trang
        navView.setOnItemSelectedListener(item -> {
            Class<?> targetActivity = mapMenu.get(item.getItemId());

            // Nếu tìm thấy màn hình đích và nó khác với màn hình đang đứng thì mới chuyển
            if (targetActivity != null && !activity.getClass().equals(targetActivity)) {
                Intent intent = new Intent(activity, targetActivity);
                // Cờ này giúp ứng dụng không mở mới hoàn toàn mà dùng lại Activity cũ nếu đã có trong bộ nhớ
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);

                // Tắt hiệu ứng chuyển trang mặc định để tạo cảm giác app mượt mà (như đang chuyển tab)
                activity.overridePendingTransition(0, 0);
            }
            return true;
        });
    }
}
