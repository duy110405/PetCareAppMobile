package com.example.petcareapp.ui.user.LichHen;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.DichVu;
import com.example.petcareapp.data.model.LichHen;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/*
 * Activity hiển thị chi tiết lịch hẹn của người dùng
 *
 * Bao gồm:
 * - thông tin thú cưng
 * - thời gian lịch hẹn
 * - chi nhánh
 * - dịch vụ đã chọn
 * - tổng tiền
 * - ghi chú
 * - trạng thái
 * - lý do từ chối (nếu có)
 */
public class ChiTietLichHenActivity extends AppCompatActivity {

    // Các TextView hiển thị thông tin
    private TextView tvPetName;
    private TextView tvTime;
    private TextView tvBranch;
    private TextView tvServices;
    private TextView tvTotalPrice;
    private TextView tvNote;
    private TextView tvStatus;
    private TextView tvRejectReason;

    // ID lịch hẹn được truyền từ Intent
    private String lichHenId;

    // Formatter dùng chung
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

    private final NumberFormat currencyFormat =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chi_tiet_lich_hen);

        // Lấy ID lịch hẹn từ Intent
        lichHenId = getAppointmentIdFromIntent();

        if (lichHenId == null) {
            showToast("Không tìm thấy lịch hẹn");
            finish();
            return;
        }

        initViews();
        setupEvents();
        loadAppointmentDetail();
    }

    /*
     * Lấy ID lịch hẹn từ Intent
     */
    private String getAppointmentIdFromIntent() {
        return getIntent().getStringExtra("lichHenId");
    }

    /*
     * Ánh xạ view từ XML
     */
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

    /*
     * Gán sự kiện cho nút back
     */
    private void setupEvents() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    /*
     * Tải dữ liệu lịch hẹn từ Firestore
     */
    private void loadAppointmentDetail() {
        FirebaseFirestore.getInstance()
                .collection("LichHen")
                .document(lichHenId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        showToast("Lịch hẹn không tồn tại");
                        return;
                    }

                    LichHen lichHen = document.toObject(LichHen.class);

                    if (lichHen == null) {
                        showToast("Không thể đọc dữ liệu");
                        return;
                    }

                    displayAppointmentDetail(lichHen);
                })
                .addOnFailureListener(e ->
                        showToast("Lỗi tải dữ liệu")
                );
    }

    /*
     * Hiển thị toàn bộ thông tin lịch hẹn lên giao diện
     */
    private void displayAppointmentDetail(LichHen lichHen) {
        displayPetInfo(lichHen);
        displayTimeInfo(lichHen);
        displayServiceInfo(lichHen);
        displayPriceInfo(lichHen);
        displayNoteInfo(lichHen);
        displayStatusInfo(lichHen);
        displayRejectReason(lichHen);
    }

    /*
     * Hiển thị thông tin thú cưng và chi nhánh
     */
    private void displayPetInfo(LichHen lichHen) {
        tvPetName.setText("Thú cưng: " + safeText(lichHen.getTenThuCung()));
        tvBranch.setText("Chi nhánh: " + safeText(lichHen.getTenChiNhanh()));
    }

    /*
     * Hiển thị thời gian lịch hẹn
     */
    private void displayTimeInfo(LichHen lichHen) {
        if (lichHen.getThoiGianHen() != null) {
            String formattedTime =
                    dateFormat.format(lichHen.getThoiGianHen().toDate());

            tvTime.setText("Thời gian: " + formattedTime);
        } else {
            tvTime.setText("Thời gian: Không xác định");
        }
    }

    /*
     * Hiển thị danh sách dịch vụ
     */
    private void displayServiceInfo(LichHen lichHen) {
        tvServices.setText(
                "Dịch vụ: " + buildServiceText(
                        lichHen.getDanhSachDichVu()
                )
        );
    }

    /*
     * Chuyển danh sách dịch vụ thành chuỗi text
     */
    private String buildServiceText(List<DichVu> services) {
        if (services == null || services.isEmpty()) {
            return "Không có";
        }

        StringBuilder builder = new StringBuilder();

        for (DichVu dichVu : services) {
            if (dichVu != null && dichVu.getTen() != null) {
                builder.append(dichVu.getTen())
                        .append(", ");
            }
        }

        // Xóa dấu ", " cuối cùng
        if (builder.length() > 2) {
            builder.setLength(builder.length() - 2);
        }

        return builder.toString();
    }

    /*
     * Hiển thị tổng tiền
     */
    private void displayPriceInfo(LichHen lichHen) {
        String formattedPrice =
                currencyFormat.format(lichHen.getTongTien());

        tvTotalPrice.setText("Tổng tiền: " + formattedPrice + " VNĐ");
    }

    /*
     * Hiển thị ghi chú
     */
    private void displayNoteInfo(LichHen lichHen) {
        String note = lichHen.getGhiChu();

        if (note == null || note.trim().isEmpty()) {
            note = "Không có";
        }

        tvNote.setText("Ghi chú: " + note);
    }

    /*
     * Hiển thị trạng thái lịch hẹn
     */
    private void displayStatusInfo(LichHen lichHen) {
        tvStatus.setText(
                safeText(lichHen.getTrangThai())
        );
    }

    /*
     * Hiển thị lý do từ chối nếu lịch bị hủy
     */
    private void displayRejectReason(LichHen lichHen) {
        String reason = lichHen.getLyDoTuChoi();

        boolean shouldShow =
                "Đã hủy".equals(lichHen.getTrangThai())
                        && reason != null
                        && !reason.trim().isEmpty();

        if (shouldShow) {
            tvRejectReason.setVisibility(View.VISIBLE);
            tvRejectReason.setText("Lý do từ chối: " + reason);
        } else {
            tvRejectReason.setVisibility(View.GONE);
        }
    }

    /*
     * Tránh null khi hiển thị text
     */
    private String safeText(String text) {
        return text != null ? text : "Không có";
    }

    /*
     * Hiển thị Toast message
     */
    private void showToast(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}