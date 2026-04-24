package com.example.petcareapp.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.LichHen;
import com.example.petcareapp.ui.user.LichHen.AddLichHenActivity;
import com.example.petcareapp.ui.user.LichHen.LichHenAdapter;
import com.example.petcareapp.utils.MenuUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ULichHenActivity extends AppCompatActivity {

    private LichHenAdapter upcomingAdapter, historyAdapter;
    private TextView tvTotalCount;

    // 1. KHAI BÁO THÊM CÁC BIẾN CHO GIAO DIỆN
    private RecyclerView rvUpcoming, rvHistory;
    private LinearLayout layoutEmptyUpcoming, layoutEmptyHistory;

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_lich_hen);

        // ===== ÁNH XẠ UI CƠ BẢN =====
        tvTotalCount = findViewById(R.id.tvPetCount);
        MaterialButton btnAdd = findViewById(R.id.btnAddAppointment);
        ImageView btnBack = findViewById(R.id.btnBack);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        MenuUser.setup(this, bottomNav);

        // ===== 2. ÁNH XẠ RECYCLERVIEW VÀ EMPTY LAYOUT =====
        rvUpcoming = findViewById(R.id.rvUpcomingAppointments);
        rvHistory = findViewById(R.id.rvHistoryAppointments);
        layoutEmptyUpcoming = findViewById(R.id.layoutEmptyUpcoming);
        layoutEmptyHistory = findViewById(R.id.layoutEmptyHistory);

        upcomingAdapter = new LichHenAdapter();
        historyAdapter = new LichHenAdapter();

        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        rvUpcoming.setAdapter(upcomingAdapter);
        rvHistory.setAdapter(historyAdapter);

        // Load data realtime
        loadData();

        // ===== Actions =====
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddLichHenActivity.class))
        );

        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Load danh sách lịch hẹn theo userId
     * + Tách upcoming / history
     */
    private void loadData() {

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseFirestore.getInstance()
                .collection("LichHen")
                .whereEqualTo("userId", userId)
                .orderBy("thoiGianHen", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    // ===== ERROR HANDLING =====
                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }

                    if (value == null) return;

                    List<LichHen> all = new ArrayList<>();

                    for (var doc : value.getDocuments()) {
                        LichHen lh = doc.toObject(LichHen.class);

                        if (lh != null) {
                            lh.setId(doc.getId());
                            all.add(lh);
                        }
                    }

                    List<LichHen> upcoming = new ArrayList<>();
                    List<LichHen> history = new ArrayList<>();

                    // ===== PHÂN LOẠI LỊCH HẸN =====
                    for (LichHen lh : all) {
                        String status = lh.getTrangThai();
                        if (status == null) status = "";

                        // Lịch đã kết thúc → history
                        if (status.equals("Hoàn thành") || status.equals("Đã hủy")) {
                            history.add(lh);
                        } else {
                            // Chờ duyệt / xác nhận / đang khám → upcoming
                            upcoming.add(lh);
                        }
                    }

                    // ===== UPDATE DỮ LIỆU =====
                    upcomingAdapter.setData(upcoming);
                    historyAdapter.setData(history);
                    tvTotalCount.setText(String.valueOf(upcoming.size()));

                    // ===== 3. XỬ LÝ LOGIC HIỂN THỊ EMPTY STATE =====

                    // Xử lý mảng Sắp tới (Upcoming)
                    if (upcoming.isEmpty()) {
                        // Nếu danh sách trống -> Hiện layout báo trống, ẩn danh sách đi
                        layoutEmptyUpcoming.setVisibility(View.VISIBLE);
                        rvUpcoming.setVisibility(View.GONE);
                    } else {
                        // Nếu có dữ liệu -> Ẩn layout báo trống, hiện danh sách
                        layoutEmptyUpcoming.setVisibility(View.GONE);
                        rvUpcoming.setVisibility(View.VISIBLE);
                    }

                    // Xử lý mảng Lịch sử (History)
                    if (history.isEmpty()) {
                        layoutEmptyHistory.setVisibility(View.VISIBLE);
                        rvHistory.setVisibility(View.GONE);
                    } else {
                        layoutEmptyHistory.setVisibility(View.GONE);
                        rvHistory.setVisibility(View.VISIBLE);
                    }
                });
    }
}