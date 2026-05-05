package com.example.petcareapp.ui.user;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.KhoVoucher;
import com.example.petcareapp.data.model.Voucher;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class UVoucherActivity extends AppCompatActivity {

    private TextView tvTongDiem;
    private RecyclerView rcvVouchers;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private String userId;
    private int currentPoints = 0; // Lưu điểm hiện tại của user

    private List<Voucher> voucherList = new ArrayList<>();
    private VoucherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_voucher);

        initViews();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        setupRecyclerView();
        loadUserPoints();
        loadVouchers();
    }

    private void initViews() {
        tvTongDiem = findViewById(R.id.tvTongDiem);
        rcvVouchers = findViewById(R.id.rcvVouchers);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), UserProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        rcvVouchers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VoucherAdapter();
        rcvVouchers.setAdapter(adapter);
    }

    // Lấy tổng điểm hiện tại của người dùng
    private void loadUserPoints() {
        if (userId == null) return;
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long diem = document.getLong("tongDiem");
                        currentPoints = diem != null ? diem.intValue() : 0;
                        tvTongDiem.setText(String.valueOf(currentPoints));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải điểm", Toast.LENGTH_SHORT).show());
    }

    // Lấy danh sách Voucher từ Firestore
    private void loadVouchers() {
        db.collection("Voucher").get()
                .addOnSuccessListener(snapshot -> {
                    voucherList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Voucher v = doc.toObject(Voucher.class);
                        if (v != null) {
                            v.setIdVoucher(doc.getId()); // Gắn ID document vào object
                            voucherList.add(v);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải Voucher", Toast.LENGTH_SHORT).show());
    }

    // Logic xử lý khi bấm đổi Voucher
    private void handleExchangeVoucher(Voucher voucher) {
        if (currentPoints < voucher.getDiemYeuCau()) {
            Toast.makeText(this, "Bạn không đủ điểm để đổi voucher này!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đổi")
                .setMessage("Bạn có chắc muốn dùng " + voucher.getDiemYeuCau() + " điểm để đổi voucher: " + voucher.getTenVoucher() + "?")
                .setPositiveButton("Đổi ngay", (dialog, which) -> executeExchangeTransaction(voucher))
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Thực hiện Giao dịch (Trừ điểm & Thêm vào kho) sử dụng WriteBatch để đảm bảo dữ liệu nhất quán
    private void executeExchangeTransaction(Voucher voucher) {
        WriteBatch batch = db.batch();

        // 1. Lệnh trừ điểm của User
        batch.update(db.collection("users").document(userId),
                "tongDiem", FieldValue.increment(-voucher.getDiemYeuCau()));

        // 2. Lệnh tạo bản ghi mới trong KhoVoucher
        String khoId = db.collection("KhoVoucher").document().getId();
        KhoVoucher khoVoucher = new KhoVoucher(khoId, voucher.getIdVoucher(), userId, "Chưa dùng");
        batch.set(db.collection("KhoVoucher").document(khoId), khoVoucher);

        // Chạy đồng thời 2 lệnh trên
        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đổi Voucher thành công! Kiểm tra trong kho nhé.", Toast.LENGTH_LONG).show();
                    loadUserPoints(); // Tải lại điểm trên giao diện
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi đổi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ================= ADAPTER =================
    private class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Voucher v = voucherList.get(position);

            holder.tvTenVoucher.setText(v.getTenVoucher());
            holder.tvGiamGia.setText(String.format("Giảm: %,d đ", v.getSoTienGiam()));
            holder.tvDiemYeuCau.setText(v.getDiemYeuCau() + " Điểm");

            holder.btnDoiVoucher.setOnClickListener(view -> handleExchangeVoucher(v));
        }

        @Override
        public int getItemCount() {
            return voucherList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTenVoucher, tvGiamGia, tvDiemYeuCau;
            MaterialButton btnDoiVoucher;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTenVoucher = itemView.findViewById(R.id.tvTenVoucher);
                tvGiamGia = itemView.findViewById(R.id.tvGiamGia);
                tvDiemYeuCau = itemView.findViewById(R.id.tvDiemYeuCau);
                btnDoiVoucher = itemView.findViewById(R.id.btnDoiVoucher);
            }
        }
    }
}