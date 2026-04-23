package com.example.petcareapp.ui.user.LichHen;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.petcareapp.R;
import com.example.petcareapp.data.model.DichVu;
import com.example.petcareapp.data.model.LichHen;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChiTietLichHenActivity extends AppCompatActivity {

    private TextView tvPetName, tvTime, tvBranch, tvServices, tvTotalPrice, tvNote, tvStatus, tvRejectReason;
    private String lichHenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chi_tiet_lich_hen);

        lichHenId = getIntent().getStringExtra("lichHenId");
        if (lichHenId == null) { finish(); return; }

        initViews();
        loadData();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvPetName = findViewById(R.id.tvPetName);
        tvTime = findViewById(R.id.tvTime);
        tvBranch = findViewById(R.id.tvBranch);
        tvServices = findViewById(R.id.tvServices);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvNote = findViewById(R.id.tvNote);
        tvStatus = findViewById(R.id.tvStatus);
        tvRejectReason = findViewById(R.id.tvRejectReason);
    }

    private void loadData() {
        FirebaseFirestore.getInstance().collection("LichHen").document(lichHenId)
                .get().addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    LichHen item = doc.toObject(LichHen.class);
                    if (item == null) return;

                    tvPetName.setText("Thú cưng: " + item.getTenThuCung());
                    tvBranch.setText("Chi nhánh: " + item.getTenChiNhanh());
                    tvStatus.setText(item.getTrangThai());

                    if (item.getThoiGianHen() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
                        tvTime.setText("Thời gian: " + sdf.format(item.getThoiGianHen().toDate()));
                    }

                    // Nối tên dịch vụ
                    StringBuilder dvBuilder = new StringBuilder();
                    if (item.getDanhSachDichVu() != null) {
                        for (DichVu dv : item.getDanhSachDichVu()) dvBuilder.append(dv.getTen()).append(", ");
                        if (dvBuilder.length() > 0) dvBuilder.setLength(dvBuilder.length() - 2);
                    }
                    tvServices.setText("Dịch vụ: " + dvBuilder.toString());
                    tvTotalPrice.setText("Tổng tiền: " + item.getTongTien() + " VNĐ");
                    tvNote.setText("Ghi chú: " + (item.getGhiChu() != null && !item.getGhiChu().isEmpty() ? item.getGhiChu() : "Không có"));

                    if ("Đã hủy".equals(item.getTrangThai()) && item.getLyDoTuChoi() != null && !item.getLyDoTuChoi().isEmpty()) {
                        tvRejectReason.setVisibility(View.VISIBLE);
                        tvRejectReason.setText("Lý do từ chối: " + item.getLyDoTuChoi());
                    } else {
                        tvRejectReason.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show());
    }
}