package com.example.petcareapp.ui.admin.lichhen;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.LichHen;
import com.example.petcareapp.data.model.ThongBao;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Màn hình hiển thị chi tiết lịch hẹn dành cho Admin.
 *
 * Chức năng:
 * - Xem toàn bộ thông tin lịch hẹn
 * - Duyệt lịch hẹn
 * - Từ chối lịch hẹn kèm lý do
 * - Gửi thông báo cho người dùng
 */
public class AdminChiTietLichHenActivity extends AppCompatActivity {

    // ===== Các thành phần giao diện =====
    private TextView txtPetName;
    private TextView txtTime;
    private TextView txtOwnerName;
    private TextView txtPhone;
    private TextView txtBranch;
    private TextView txtStatus;
    private TextView txtNote;
    private TextView txtRejectReason;
    private TextView txtServices;
    private TextView txtTotalPrice;

    private MaterialButton btnApprove;
    private MaterialButton btnReject;
    private ImageView btnBack;

    // ID lịch hẹn được truyền từ màn hình trước
    private String lichHenId;

    // Formatter hiển thị thời gian
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chi_tiet_lich_hen);

        // Ánh xạ view
        initViews();

        // Lấy ID lịch hẹn từ Intent
        lichHenId = getIntent().getStringExtra("lichHenId");

        // Nếu không có ID thì đóng màn hình
        if (lichHenId == null) {
            finish();
            return;
        }

        // Load dữ liệu chi tiết
        loadDetail();

        // Gán sự kiện cho các nút
        setupButtons();
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

        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);

        txtRejectReason = findViewById(R.id.txtRejectReason);
        btnBack = findViewById(R.id.btnBack);

        txtServices = findViewById(R.id.txtServices);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);

        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Tải dữ liệu chi tiết lịch hẹn từ Firestore và hiển thị lên UI.
     */
    private void loadDetail() {
        FirebaseFirestore.getInstance()
                .collection("LichHen")
                .document(lichHenId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    // Nếu document không tồn tại thì bỏ qua
                    if (!snapshot.exists()) return;

                    LichHen item = snapshot.toObject(LichHen.class);

                    // Nếu parse object lỗi thì bỏ qua
                    if (item == null) return;

                    item.setId(snapshot.getId());

                    // ===== Thông tin thú cưng =====
                    txtPetName.setText(item.getTenThuCung());

                    // ===== Thông tin chủ thú cưng =====
                    if (item.getTenChuThuCung() != null) {
                        txtOwnerName.setText("Khách hàng: " + item.getTenChuThuCung());
                    }

                    if (item.getSoDienThoai() != null) {
                        txtPhone.setText("SĐT: " + item.getSoDienThoai());
                    }

                    // ===== Thời gian hẹn =====
                    if (item.getThoiGianHen() != null) {
                        txtTime.setText(
                                sdf.format(item.getThoiGianHen().toDate())
                        );
                    }

                    // ===== Danh sách dịch vụ =====
                    StringBuilder dvBuilder = new StringBuilder();

                    if (item.getDanhSachDichVu() != null
                            && !item.getDanhSachDichVu().isEmpty()) {

                        for (com.example.petcareapp.data.model.DichVu dv
                                : item.getDanhSachDichVu()) {
                            dvBuilder.append(dv.getTen()).append(", ");
                        }

                        // Xóa dấu phẩy cuối cùng
                        dvBuilder.setLength(dvBuilder.length() - 2);

                        txtServices.setText(dvBuilder.toString());
                    } else {
                        txtServices.setText("Không có dịch vụ đi kèm");
                    }

                    // ===== Tổng tiền =====
                    txtTotalPrice.setText(
                            String.format("%,d đ", item.getTongTien())
                    );

                    // ===== Hiển thị trạng thái + UX màu sắc =====
                    String status = item.getTrangThai();
                    txtStatus.setText(status);

                    if ("Chờ duyệt".equals(status)) {
                        txtStatus.setTextColor(
                                android.graphics.Color.parseColor("#F57C00")
                        );
                        txtStatus.setBackgroundColor(
                                android.graphics.Color.parseColor("#FFF3E0")
                        );

                    } else if ("Đã xác nhận".equals(status)) {
                        txtStatus.setTextColor(
                                android.graphics.Color.parseColor("#388E3C")
                        );
                        txtStatus.setBackgroundColor(
                                android.graphics.Color.parseColor("#E8F5E9")
                        );

                    } else if ("Đã hủy".equals(status)) {
                        txtStatus.setTextColor(
                                android.graphics.Color.parseColor("#D32F2F")
                        );
                        txtStatus.setBackgroundColor(
                                android.graphics.Color.parseColor("#FFEBEE")
                        );
                    }

                    // ===== Chi nhánh + ghi chú =====
                    txtBranch.setText(item.getTenChiNhanh());
                    txtNote.setText(item.getGhiChu());

                    // ===== Hiển thị lý do từ chối nếu bị hủy =====
                    if ("Đã hủy".equals(item.getTrangThai())) {
                        txtRejectReason.setVisibility(TextView.VISIBLE);
                        txtRejectReason.setText(
                                "Lý do từ chối: " + item.getLyDoTuChoi()
                        );
                    } else {
                        txtRejectReason.setVisibility(TextView.GONE);
                    }

                    // ===== Ẩn nút nếu lịch đã xử lý =====
                    if (!"Chờ duyệt".equals(item.getTrangThai())) {
                        btnApprove.setVisibility(TextView.GONE);
                        btnReject.setVisibility(TextView.GONE);
                    }

                    // ===== Click gọi điện cho khách =====
                    txtPhone.setOnClickListener(v -> {
                        android.content.Intent intent =
                                new android.content.Intent(
                                        android.content.Intent.ACTION_DIAL
                                );

                        intent.setData(
                                android.net.Uri.parse(
                                        "tel:" + item.getSoDienThoai()
                                )
                        );

                        startActivity(intent);
                    });
                });
    }

    /**
     * Gán sự kiện cho nút Duyệt và Từ chối.
     */
    private void setupButtons() {

        // ===== Duyệt lịch hẹn =====
        btnApprove.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("LichHen")
                    .document(lichHenId)
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        if (!snapshot.exists()) return;

                        LichHen item = snapshot.toObject(LichHen.class);

                        if (item == null) return;

                        String currentStatus =
                                snapshot.getString("trangThai");

                        // Chỉ cho phép duyệt khi đang chờ duyệt
                        if (!"Chờ duyệt".equals(currentStatus)) {
                            Toast.makeText(
                                    this,
                                    "Lịch hẹn đã được xử lý",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadDetail();
                            return;
                        }

                        snapshot.getReference()
                                .update("trangThai", "Đã xác nhận")
                                .addOnSuccessListener(unused -> {

                                    // Gửi thông báo cho user
                                    guiThongBaoChoUser(
                                            item.getUserId(),
                                            "Lịch hẹn của bạn đã được xác nhận"
                                    );

                                    Toast.makeText(
                                            this,
                                            "Đã duyệt lịch hẹn",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    loadDetail();
                                });
                    });
        });

        // ===== Từ chối lịch hẹn =====
        btnReject.setOnClickListener(v -> showRejectDialog());
    }

    /**
     * Gửi thông báo cho người dùng sau khi admin xử lý lịch hẹn.
     *
     * @param userId   ID người dùng
     * @param noiDung  nội dung thông báo
     */
    private void guiThongBaoChoUser(
            String userId,
            String noiDung
    ) {
        ThongBao thongBao = new ThongBao(userId, noiDung);

        FirebaseFirestore.getInstance()
                .collection("ThongBao")
                .add(thongBao);
    }

    /**
     * Hiển thị dialog nhập lý do từ chối.
     */
    private void showRejectDialog() {

        EditText edtReason = new EditText(this);
        edtReason.setHint("Nhập lý do từ chối");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Từ chối lịch hẹn")
                .setView(edtReason)
                .setPositiveButton("Gửi", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String reason = edtReason.getText()
                                .toString()
                                .trim();

                        // Bắt buộc nhập lý do
                        if (TextUtils.isEmpty(reason)) {
                            edtReason.setError("Vui lòng nhập lý do");
                            return;
                        }

                        FirebaseFirestore.getInstance()
                                .collection("LichHen")
                                .document(lichHenId)
                                .get()
                                .addOnSuccessListener(snapshot -> {

                                    if (!snapshot.exists()) return;

                                    String currentStatus =
                                            snapshot.getString("trangThai");

                                    // Kiểm tra lịch đã xử lý chưa
                                    if (!"Chờ duyệt".equals(currentStatus)) {
                                        Toast.makeText(
                                                this,
                                                "Lịch hẹn đã được xử lý trước đó",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        dialog.dismiss();
                                        loadDetail();
                                        return;
                                    }

                                    // Cập nhật trạng thái từ chối
                                    snapshot.getReference()
                                            .update(
                                                    "trangThai", "Đã hủy",
                                                    "lyDoTuChoi", reason
                                            )
                                            .addOnSuccessListener(unused -> {

                                                LichHen item =
                                                        snapshot.toObject(
                                                                LichHen.class
                                                        );

                                                if (item != null) {
                                                    guiThongBaoChoUser(
                                                            item.getUserId(),
                                                            "Lịch hẹn bị từ chối. Lý do: "
                                                                    + reason
                                                    );
                                                }

                                                Toast.makeText(
                                                        this,
                                                        "Đã từ chối lịch hẹn",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                                dialog.dismiss();
                                                loadDetail();
                                            });
                                });
                    });
        });

        dialog.show();
    }
}