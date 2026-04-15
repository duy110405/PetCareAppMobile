package com.example.petcareapp.ui.user;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.admin.AChiNhanhAdapter;
import com.example.petcareapp.ui.admin.AChiNhanhViewModel;

public class UTimPhongActivity extends AppCompatActivity {
    private UChiNhanhViewModel uChiNhanhViewModel;
    private RecyclerView rvChiNhanhUser;
    private Button btnChiDuong;
    private UChiNhanhAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_timphong);

        uChiNhanhViewModel = new ViewModelProvider(this).get(UChiNhanhViewModel.class);

        // Ánh xạ View
        rvChiNhanhUser = findViewById(R.id.rvChiNhanhUser);

        // Cài đặt RecyclerView
        rvChiNhanhUser.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UChiNhanhAdapter();
        rvChiNhanhUser.setAdapter(adapter);

        // Bắt sự kiện khi danh sách trên Firebase thay đổi
        uChiNhanhViewModel.getDanhSachChiNhanh().observe(this, danhSach -> {
            adapter.setDanhSachChiNhanh(danhSach);
        });
    }
}
