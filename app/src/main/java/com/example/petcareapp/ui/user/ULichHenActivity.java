package com.example.petcareapp.ui.user;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_lich_hen);

        tvTotalCount = findViewById(R.id.tvPetCount); // TextView hiển thị "2" của bạn
        MaterialButton btnAdd = findViewById(R.id.btnAddAppointment);
        ImageView btnBack = findViewById(R.id.btnBack);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        MenuUser.setup(this, bottomNav);

        // Setup 2 RecyclerViews
        RecyclerView rvUpcoming = findViewById(R.id.rvUpcomingAppointments);
        RecyclerView rvHistory = findViewById(R.id.rvHistoryAppointments);

        upcomingAdapter = new LichHenAdapter();
        historyAdapter = new LichHenAdapter();

        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        rvUpcoming.setAdapter(upcomingAdapter);
        rvHistory.setAdapter(historyAdapter);

        loadData();

        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddLichHenActivity.class)));
        btnBack.setOnClickListener(v -> finish());

    }

    private void loadData() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseFirestore.getInstance().collection("LichHen")
                .whereEqualTo("userId", userId)
                .orderBy("thoiGianHen", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value == null) return;
                    List<LichHen> all = value.toObjects(LichHen.class);
                    List<LichHen> upcoming = new ArrayList<>();
                    List<LichHen> history = new ArrayList<>();

                    for (LichHen lh : all) {
                        if ("Hoàn thành".equals(lh.getTrangThai())) history.add(lh);
                        else upcoming.add(lh);
                    }

                    upcomingAdapter.setData(upcoming);
                    historyAdapter.setData(history);
                    tvTotalCount.setText(String.valueOf(upcoming.size()));

                });
    }
}