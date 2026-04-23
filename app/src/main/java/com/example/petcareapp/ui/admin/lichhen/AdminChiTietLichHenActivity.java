package com.example.petcareapp.ui.admin.lichhen;

import android.app.AlertDialog;
import android.os.Bundle;
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
import android.widget.EditText;
import android.text.TextUtils;

public class AdminChiTietLichHenActivity extends AppCompatActivity {

    private TextView txtPetName;
    private TextView txtTime;
    private TextView txtOwnerName;
    private TextView txtPhone;
    private TextView txtBranch;
    private TextView txtStatus;
    private TextView txtNote;

    private MaterialButton btnApprove;
    private MaterialButton btnReject;

    private String lichHenId;
    private TextView txtRejectReason;
    private ImageView btnBack;
    private TextView txtServices;
    private TextView txtTotalPrice;
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chi_tiet_lich_hen);

        initViews();

        lichHenId = getIntent().getStringExtra("lichHenId");

        if (lichHenId == null) {
            finish();
            return;
        }

        loadDetail();

        setupButtons();
    }

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

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadDetail() {
        FirebaseFirestore.getInstance()
                .collection("LichHen")
                .document(lichHenId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;
                    LichHen item = snapshot.toObject(LichHen.class);
                    if (item == null) return;
                    item.setId(snapshot.getId());
                    txtPetName.setText(item.getTenThuCung());
                    if(item.getTenChuThuCung() != null) {
                        txtOwnerName.setText("Khách hàng: " + item.getTenChuThuCung());
                    }
                    if(item.getSoDienThoai() != null) {
                        txtPhone.setText("SĐT: " + item.getSoDienThoai());
                    }

                    if (item.getThoiGianHen() != null) {
                        txtTime.setText(
                                sdf.format(item.getThoiGianHen().toDate())
                        );
                    }
                    StringBuilder dvBuilder = new StringBuilder();
                    if (item.getDanhSachDichVu() != null && !item.getDanhSachDichVu().isEmpty()) {
                        for (com.example.petcareapp.data.model.DichVu dv : item.getDanhSachDichVu()) {
                            dvBuilder.append(dv.getTen()).append(", ");
                        }
                        dvBuilder.setLength(dvBuilder.length() - 2);
                        txtServices.setText(dvBuilder.toString());
                    } else {
                        txtServices.setText("Không có dịch vụ đi kèm");
                    }

                    // Gán tổng tiền
                    txtTotalPrice.setText(String.format("%,d đ", item.getTongTien()));

                    // Đổi màu nền của Status tùy theo trạng thái (Tính năng UX nâng cao)
                    String status = item.getTrangThai();
                    txtStatus.setText(status);
                    if ("Chờ duyệt".equals(status)) {
                        txtStatus.setTextColor(android.graphics.Color.parseColor("#F57C00"));
                        txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"));
                    } else if ("Đã xác nhận".equals(status)) {
                        txtStatus.setTextColor(android.graphics.Color.parseColor("#388E3C"));
                        txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
                    } else if ("Đã hủy".equals(status)) {
                        txtStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                        txtStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
                    }


                    txtBranch.setText(item.getTenChiNhanh());
                    txtStatus.setText(item.getTrangThai());
                    txtNote.setText(item.getGhiChu());

                    if ("Đã hủy".equals(item.getTrangThai())) {
                        txtRejectReason.setVisibility(TextView.VISIBLE);
                        txtRejectReason.setText(
                                "Lý do từ chối: " + item.getLyDoTuChoi()
                        );
                    } else {
                        txtRejectReason.setVisibility(TextView.GONE);
                    }

                    if (!"Chờ duyệt".equals(item.getTrangThai())) {
                        btnApprove.setVisibility(TextView.GONE);
                        btnReject.setVisibility(TextView.GONE);
                    }

                    txtPhone.setOnClickListener(v -> {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                        intent.setData(android.net.Uri.parse("tel:" + item.getSoDienThoai()));
                        startActivity(intent);
                    });
                });
    }

    private void setupButtons() {

        // DUYỆT
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


        // TỪ CHỐI
        btnReject.setOnClickListener(v -> showRejectDialog());
    }
    private void guiThongBaoChoUser(
            String userId,
            String noiDung
    ) {
        ThongBao thongBao = new ThongBao(userId, noiDung);

        FirebaseFirestore.getInstance()
                .collection("ThongBao")
                .add(thongBao);
    }
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

                        // BẮT BUỘC nhập lý do
                        if (TextUtils.isEmpty(reason)) {
                            edtReason.setError("Vui lòng nhập lý do");
                            return;
                        }

                        // UPDATE FIRESTORE
                        FirebaseFirestore.getInstance()
                                .collection("LichHen")
                                .document(lichHenId)
                                .get()
                                .addOnSuccessListener(snapshot -> {

                                    if (!snapshot.exists()) return;

                                    String currentStatus =
                                            snapshot.getString("trangThai");

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

                                    snapshot.getReference()
                                            .update(
                                                    "trangThai", "Đã hủy",
                                                    "lyDoTuChoi", reason
                                            )
                                            .addOnSuccessListener(unused -> {
                                                LichHen item = snapshot.toObject(LichHen.class);

                                                if (item != null) {
                                                    guiThongBaoChoUser(
                                                            item.getUserId(),
                                                            "Lịch hẹn bị từ chối. Lý do: " + reason
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