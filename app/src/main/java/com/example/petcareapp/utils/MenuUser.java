package com.example.petcareapp.utils;

import android.app.Activity;
import android.content.Intent;
import com.example.petcareapp.R;
import com.example.petcareapp.ui.user.TrangChu.UTrangChuActivity;
import com.example.petcareapp.ui.user.UCaiDatActivity;
import com.example.petcareapp.ui.user.UDaoChoiActivity;
import com.example.petcareapp.ui.user.ULichHenActivity;
import com.example.petcareapp.ui.user.UTimPhongActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class MenuUser {
    private static final Map<Integer, Class<?>> mapMenu = new HashMap<>();
    static {
        mapMenu.put(R.id.nav_home, UTrangChuActivity.class);
        mapMenu.put(R.id.nav_lichhen, ULichHenActivity.class);
        mapMenu.put(R.id.nav_phongkham, UTimPhongActivity.class);
        mapMenu.put(R.id.nav_caidat, UCaiDatActivity.class);
        mapMenu.put(R.id.nav_daochoi, UDaoChoiActivity.class);
    }
    public static void setup(Activity activity, BottomNavigationView navView) {

        for (Map.Entry<Integer, Class<?>> entry : mapMenu.entrySet()) {
            if (entry.getValue().equals(activity.getClass())) {
                navView.setSelectedItemId(entry.getKey());
                break;
            }
        }
        navView.setOnItemSelectedListener(item -> {
            Class<?> targetActivity = mapMenu.get(item.getItemId());

            if (targetActivity != null) {
                Intent intent = new Intent(activity, targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }

            return true;
        });
    }
}
