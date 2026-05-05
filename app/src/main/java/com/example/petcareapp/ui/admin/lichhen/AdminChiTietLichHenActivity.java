package com.example.petcareapp.ui.admin.lichhen;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.DichVu;
import com.example.petcareapp.data.model.LichHen;
import com.example.petcareapp.data.model.ThongBao;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình hiển thị chi tiết lịch hẹn dành cho Admin.
 *
 * Tích hợp vòng đời thực hiện dịch vụ:
 * 1. Chờ duyệt: Hiện nút Duyệt / Từ chối
 * 2. Đã xác nhận: Hiện nút Bắt đầu thực hiện (khi khách đến)
 * 3. Đang thực hiện:
 *    - Nếu là dịch vụ Y tế -> Hiện nút Cập nhật Bệnh án
 *    - Nếu là dịch vụ Spa/Chăm sóc -> Hiện nút Hoàn tất nhanh
 * 4. Hoàn thành / Đã hủy: Ẩn tất cả thao tác
 */
public class AdminChiTietLichHenActivity extends AppCompatActivity {

    // ===== Các thành phần giao diện thông tin =====
    private TextView txtPetName, txtTime, txtOwnerName, txtPhone, txtBranch;
    private TextView txtStatus, txtNote, txtRejectReason, txtServices, txtTotalPrice;
    private ImageView btnBack;

    // ===== Các thành phần giao diện thao tác (Dynamic UI) =====
    private LinearLayout layoutDuyetHuy;
    private MaterialButton btnXacNhan;
    private MaterialButton btnTuChoi;
    private MaterialButton btnBatDauThucHien; // Đã đổi tên chuẩn
    private MaterialButton btnHoanTatNhanh;   // Nút dành cho luồng Spa
    private MaterialButton btnCapNhatBenhAn;  // Nút dành cho luồng Y tế

    // Firebase & Data
    private FirebaseFirestore db;
    private String lichHenId;
    private ListenerRegistration snapshotListener;

    // Formatter hiển thị thời gian
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chi_tiet_lich_hen);

        db = FirebaseFirestore.getInstance();
        lichHenId = getIntent().getStringExtra("lichHenId");

        if (lichHenId == null) {
            finish();
            return;
        }

        initViews();
        setupButtons();

        // Bắt đầu lắng nghe dữ liệu Real-time
        loadDetailRealtime();
    }

    /**
     * Khởi tạo và ánh xạ toàn bộ view trong layout.
     */
    private void initViews() {
        txtPetName = findViewById(R.id.txtPetName);
        txtOwnerName = findViewById(R.id.txtOwnerName);
        txtPhone = findViewById(R.id.txtPhone);
        txtTime = findViewById(R.id.txtTime);
        txtBranch = findViewById(R.id.txtBranch);
        txtStatus = findViewById(R.id.txtStatus);
        txtNote = findViewById(R.id.txtNote);
        txtRejectReason = findViewById(R.id.txtRejectReason);
        txtServices = findViewById(R.id.txtServices);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnBack = findViewById(R.id.btnBack);

        // Các Group View phục vụ quy trình
        layoutDuyetHuy = findViewById(R.id.layoutDuyetHuy);
        btnXacNhan = findViewById(R.id.btnXacNhan);
        btnTuChoi = findViewById(R.id.btnTuChoi);
        btnBatDauThucHien = findViewById(R.id.btnBatDauThucHien);
        btnCapNhatBenhAn = findViewById(R.id.btnCapNhatBenhAn);
        btnHoanTatNhanh = findViewById(R.id.btnHoanTatNhanh);

        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Kiểm tra xem danh sách dịch vụ có chứa nghiệp vụ Y tế không
     */
    private boolean kiemTraCoDichVuYTe(List<DichVu> danhSachDichVu) {
        if (danhSachDichVu == null || danhSachDichVu.isEmpty()) return false;

        for (DichVu dv : danhSachDichVu) {
            if (dv.getTenDichVu() != null) {
                String tenDV = dv.getTenDichVu().toLowerCase();
                if (tenDV.contains("khám bệnh") || tenDV.contains("chữa") ||
                        tenDV.contains("tiêm phòng") || tenDV.contains("y tế") ||
                        tenDV.contains("siêu âm") || tenDV.contains("xét nghiệm")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Lắng nghe dữ liệu chi tiết lịch hẹn từ Firestore (Real-time).
     */
    private void loadDetailRealtime() {
        snapshotListener = db.collection("LichHen").document(lichHenId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        return;
                    }

                    LichHen item = snapshot.toObject(LichHen.class);
                    if (item == null) return;
                    item.setId(snapshot.getId());

                    // ===== Cập nhật thông tin lên UI =====
                    txtPetName.setText(item.getTenThuCung());

                    if (item.getTenChuThuCung() != null) {
                        txtOwnerName.setText("Khách hàng: " + item.getTenChuThuCung());
                    }
                    if (item.getSoDienThoai() != null) {
                        txtPhone.setText("SĐT: " + item.getSoDienThoai());
                    }
                    if (item.getThoiGianHen() != null) {
                        txtTime.setText(sdf.format(item.getThoiGianHen().toDate()));
                    }

                    // Danh sách dịch vụ
                    StringBuilder dvBuilder = new StringBuilder();
                    if (item.getDanhSachDichVu() != null && !item.getDanhSachDichVu().isEmpty()) {
                        for (com.example.petcareapp.data.model.DichVu dv : item.getDanhSachDichVu()) {
                            dvBuilder.append(dv.getTenDichVu()).append(", ");
                        }
                        dvBuilder.setLength(dvBuilder.length() - 2);
                        txtServices.setText(dvBuilder.toString());
                    } else {
                        txtServices.setText("Không có dịch vụ đi kèm");
                    }

                    txtTotalPrice.setText(String.format("%,d đ", item.getTongTien()));
                    txtBranch.setText(item.getTenChiNhanh());
                    txtNote.setText(item.getGhiChu());

                    // ===== Cập nhật Trạng thái & Giao diện động =====
                    String status = item.getTrangThai();
                    boolean requiresMedicalRecord = kiemTraCoDichVuYTe(item.getDanhSachDichVu());

                    txtStatus.setText(status);
                    updateColorByStatus(status);
                    updateActionButtonsByStatus(status, requiresMedicalRecord);

                    // Hiển thị lý do từ chối nếu bị hủy
                    if ("Đã hủy".equals(status) && item.getLyDoTuChoi() != null) {
                        txtRejectReason.setVisibility(View.VISIBLE);
                        txtRejectReason.setText("Lý do từ chối: " + item.getLyDoTuChoi());
                    } else {
                        txtRejectReason.setVisibility(View.GONE);
                    }

                    // Click gọi điện cho khách
                    txtPhone.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + item.getSoDienThoai()));
                        startActivity(intent);
                    });
                });
    }

    /**
     * Thay đổi màu sắc nhãn trạng thái
     */
    private void updateColorByStatus(String status) {
        if ("Chờ duyệt".equals(status)) {
            txtStatus.setTextColor(android.graphics.Color.parseColor("#F57C00"));
            txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"));
        } else if ("Đã xác nhận".equals(status) || "Đang thực hiện".equals(status)) {
            txtStatus.setTextColor(android.graphics.Color.parseColor("#388E3C"));
            txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
        } else if ("Đã hủy".equals(status)) {
            txtStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
            txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
        } else if ("Hoàn thành".equals(status)) {
            txtStatus.setTextColor(android.graphics.Color.parseColor("#1976D2"));
            txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"));
        }
    }

    /**
     * Ẩn/hiện các nút chức năng theo ĐÚNG LOẠI DỊCH VỤ
     */
    private void updateActionButtonsByStatus(String status, boolean requiresMedical) {
        layoutDuyetHuy.setVisibility(View.GONE);
        btnBatDauThucHien.setVisibility(View.GONE);
        btnCapNhatBenhAn.setVisibility(View.GONE);
        btnHoanTatNhanh.setVisibility(View.GONE);

        if (status == null) return;

        switch (status) {
            case "Chờ duyệt":
                layoutDuyetHuy.setVisibility(View.VISIBLE);
                break;
            case "Đã xác nhận":
                btnBatDauThucHien.setVisibility(View.VISIBLE);
                break;
            case "Đang thực hiện":
                if (requiresMedical) {
                    btnCapNhatBenhAn.setVisibility(View.VISIBLE); // Bắt buộc lập bệnh án
                } else {
                    btnHoanTatNhanh.setVisibility(View.VISIBLE); // Cho phép kết thúc nhanh
                }
                break;
            case "Hoàn thành":
            case "Đã hủy":
                break;
        }
    }

    /**
     * Gán sự kiện click cho toàn bộ các nút thao tác
     */
    private void setupButtons() {
        // 1. Nút Xác nhận lịch
        btnXacNhan.setOnClickListener(v -> updateLichHenStatus("Đã xác nhận", "Lịch hẹn của bạn đã được phòng khám xác nhận."));

        // 2. Nút Từ chối lịch
        btnTuChoi.setOnClickListener(v -> showRejectDialog());

        // 3. Nút Bắt đầu thực hiện (thay cho Bắt đầu khám cũ)
        btnBatDauThucHien.setOnClickListener(v -> updateLichHenStatus("Đang thực hiện", "Cửa hàng đang tiến hành dịch vụ cho thú cưng của bạn."));

        // 4A. Nút Cập nhật Bệnh án (Dành cho luồng Y Tế)
        btnCapNhatBenhAn.setOnClickListener(v -> {
            Intent intent = new Intent(AdminChiTietLichHenActivity.this, AdminCapNhatBenhAnActivity.class);
            intent.putExtra("LICH_HEN_ID", lichHenId);
            startActivity(intent);
        });

        // 4B. Nút Hoàn tất nhanh (Dành cho luồng Spa/Chăm sóc)
        btnHoanTatNhanh.setOnClickListener(v -> {
            updateLichHenStatus("Hoàn thành", "Dịch vụ của thú cưng đã hoàn tất, bạn có thể đến đón bé!");
        });
    }

    /**
     * Hàm dùng chung để update trạng thái lịch hẹn và gửi thông báo
     */
    private void updateLichHenStatus(String newStatus, String thongBaoChoUser) {
        db.collection("LichHen").document(lichHenId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;
                    LichHen item = snapshot.toObject(LichHen.class);
                    if (item == null) return;

                    snapshot.getReference().update("trangThai", newStatus)
                            .addOnSuccessListener(unused -> {
                                if (thongBaoChoUser != null && !thongBaoChoUser.isEmpty()) {
                                    guiThongBaoChoUser(item.getUserId(), thongBaoChoUser);
                                }
                                Toast.makeText(this, "Đã chuyển trạng thái: " + newStatus, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    /**
     * Hiển thị dialog nhập lý do từ chối.
     */
    private void showRejectDialog() {
        EditText edtReason = new EditText(this);
        edtReason.setHint(" Nhập lý do từ chối...");
        edtReason.setPadding(32, 32, 32, 32);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Từ chối lịch hẹn")
                .setView(edtReason)
                .setPositiveButton("Gửi", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String reason = edtReason.getText().toString().trim();

                if (TextUtils.isEmpty(reason)) {
                    edtReason.setError("Vui lòng nhập lý do");
                    return;
                }

                db.collection("LichHen").document(lichHenId).get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.exists()) return;

                            snapshot.getReference()
                                    .update(
                                            "trangThai", "Đã hủy",
                                            "lyDoTuChoi", reason
                                    )
                                    .addOnSuccessListener(unused -> {
                                        LichHen item = snapshot.toObject(LichHen.class);
                                        if (item != null) {
                                            guiThongBaoChoUser(item.getUserId(), "Lịch hẹn bị từ chối. Lý do: " + reason);
                                        }
                                        Toast.makeText(this, "Đã từ chối lịch hẹn", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    });
                        });
            });
        });
        dialog.show();
    }

    /**
     * Gửi thông báo cho người dùng
     */
    private void guiThongBaoChoUser(String userId, String noiDung) {
        ThongBao thongBao = new ThongBao(userId, noiDung);
        db.collection("ThongBao").add(thongBao);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy bỏ lắng nghe khi đóng màn hình để tiết kiệm tài nguyên
        if (snapshotListener != null) {
            snapshotListener.remove();
        }
    }
}