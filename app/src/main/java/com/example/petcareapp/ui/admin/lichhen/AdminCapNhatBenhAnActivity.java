package com.example.petcareapp.ui.admin.lichhen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.PhieuKham;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminCapNhatBenhAnActivity extends AppCompatActivity {

    private EditText edtThanNhiet, edtNhipTim, edtHuyetAp, edtNhipTho;
    private EditText edtTrieuChung, edtTinhTrang, edtKetLuan, edtDonThuoc, edtNgayTaiKham;
    private Button btnLuuBenhAn;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private String lichHenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cap_nhat_benh_an);

        db = FirebaseFirestore.getInstance();
        lichHenId = getIntent().getStringExtra("LICH_HEN_ID");

        initViews();

        btnBack.setOnClickListener(v -> finish());
        btnLuuBenhAn.setOnClickListener(v -> luuDuLieuBenhAn());
    }

    private void initViews() {
        edtThanNhiet = findViewById(R.id.edtThanNhiet);
        edtNhipTim = findViewById(R.id.edtNhipTim);
        edtHuyetAp = findViewById(R.id.edtHuyetAp);
        edtNhipTho = findViewById(R.id.edtNhipTho);
        edtTrieuChung = findViewById(R.id.edtTrieuChung);
        edtTinhTrang = findViewById(R.id.edtTinhTrang);
        edtKetLuan = findViewById(R.id.edtKetLuan);
        edtDonThuoc = findViewById(R.id.edtDonThuoc);
        edtNgayTaiKham = findViewById(R.id.edtNgayTaiKham);
        btnLuuBenhAn = findViewById(R.id.btnLuuBenhAn);
        btnBack = findViewById(R.id.btnBack);
    }

    private void luuDuLieuBenhAn() {
        String ketLuan = edtKetLuan.getText().toString().trim();
        if (ketLuan.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Kết luận bệnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng PhieuKham theo chuẩn Model đã tạo
        PhieuKham phieuKham = new PhieuKham();
        phieuKham.setLichHenId(lichHenId);
        phieuKham.setThoiGianKham(Timestamp.now());

        // Parse số liệu an toàn (tránh Crash nếu bỏ trống)
        try {
            if (!edtThanNhiet.getText().toString().isEmpty())
                phieuKham.setThanNhiet(Double.parseDouble(edtThanNhiet.getText().toString()));
            if (!edtNhipTim.getText().toString().isEmpty())
                phieuKham.setNhipTim(Integer.parseInt(edtNhipTim.getText().toString()));
            if (!edtNhipTho.getText().toString().isEmpty())
                phieuKham.setNhipTho(Integer.parseInt(edtNhipTho.getText().toString()));
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi định dạng số ở Chỉ số sinh tồn!", Toast.LENGTH_SHORT).show();
            return;
        }

        phieuKham.setHuyetAp(edtHuyetAp.getText().toString().trim());
        phieuKham.setTrieuChungChinh(edtTrieuChung.getText().toString().trim());
        phieuKham.setTinhTrangTongQuat(edtTinhTrang.getText().toString().trim());
        phieuKham.setKetLuan(ketLuan);
        phieuKham.setDonThuoc(edtDonThuoc.getText().toString().trim());
        phieuKham.setNgayTaiKham(edtNgayTaiKham.getText().toString().trim());

        // 1. Lưu vào Collection "PhieuKham" (Dùng lichHenId làm ID Document để quan hệ 1-1)
        db.collection("PhieuKham").document(lichHenId)
                .set(phieuKham)
                .addOnSuccessListener(aVoid -> capNhatTrangThaiLichHen())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu bệnh án: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void capNhatTrangThaiLichHen() {
        // 2. Chuyển trạng thái Lịch Hẹn sang "Hoàn thành"
        Map<String, Object> updates = new HashMap<>();
        updates.put("trangThai", "Hoàn thành");

        db.collection("LichHen").document(lichHenId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Hoàn tất ca khám thành công!", Toast.LENGTH_LONG).show();
                    finish(); // Trở về danh sách
                });
    }
}