package com.example.petcareapp.ui.admin.lichhen;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.LichHen;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;
import android.widget.EditText;
import android.text.TextUtils;

public class AdminChiTietLichHenActivity extends AppCompatActivity {

    private TextView txtPetName;
    private TextView txtTime;
    private TextView txtBranch;
    private TextView txtStatus;
    private TextView txtNote;

    private MaterialButton btnApprove;
    private MaterialButton btnReject;

    private String lichHenId;
    private TextView txtRejectReason;
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
        txtTime = findViewById(R.id.txtTime);
        txtBranch = findViewById(R.id.txtBranch);
        txtStatus = findViewById(R.id.txtStatus);
        txtNote = findViewById(R.id.txtNote);

        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
        txtRejectReason = findViewById(R.id.txtRejectReason);
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

                    if (item.getThoiGianHen() != null) {
                        txtTime.setText(
                                sdf.format(item.getThoiGianHen().toDate())
                        );
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
                });
    }

    private void setupButtons() {

        // DUYỆT
        btnApprove.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("LichHen")
                    .document(lichHenId)
                    .update("trangThai", "Đã xác nhận")
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(
                                this,
                                "Đã duyệt lịch hẹn",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadDetail();
                    });
        });


        // TỪ CHỐI
        btnReject.setOnClickListener(v -> showRejectDialog());
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
                                .update(
                                        "trangThai", "Đã hủy",
                                        "lyDoTuChoi", reason
                                )
                                .addOnSuccessListener(unused -> {

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

        dialog.show();
    }
}