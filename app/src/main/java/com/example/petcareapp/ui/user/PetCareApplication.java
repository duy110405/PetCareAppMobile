package com.example.petcareapp.ui.user;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.petcareapp.data.model.LichSu;
import com.example.petcareapp.utils.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

public class PetCareApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Giữ nguyên code cũ của bạn
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        ThemeManager.init(this);

        // Đăng ký bắt sự kiện khi mở màn hình
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                // Lấy tên class của màn hình đang mở
                String tenManHinh = activity.getClass().getSimpleName();
                luuLichSuHoatDong(tenManHinh);
            }

            // Mấy hàm này bắt buộc phải Overide nhưng cứ để trống
            @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
            @Override public void onActivityStarted(@NonNull Activity activity) {}
            @Override public void onActivityPaused(@NonNull Activity activity) {}
            @Override public void onActivityStopped(@NonNull Activity activity) {}
            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
            @Override public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }

    private void luuLichSuHoatDong(String tenManHinh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Chỉ lưu khi đã đăng nhập
        if (user != null) {
            String maUser = user.getUid();
            String email = user.getEmail();
            if (email == null) email = "Không rõ";

            long thoiGianHienTai = System.currentTimeMillis();

            // Tạo đối tượng lịch sử
            LichSu ls = new LichSu(maUser, email, tenManHinh, thoiGianHienTai);

            // Đẩy lên collection "LichSuSuDung"
            FirebaseFirestore.getInstance().collection("LichSuSuDung").add(ls);
        }
    }
}