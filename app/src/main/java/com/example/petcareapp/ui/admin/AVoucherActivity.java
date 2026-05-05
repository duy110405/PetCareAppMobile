package com.example.petcareapp.ui.admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.Voucher;
import com.example.petcareapp.utils.MenuAdmin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AVoucherActivity extends AppCompatActivity {

    private TextView tvVoucherCount;
    private RecyclerView rvVouchers;
    private MaterialCardView btnThemVoucher;

    private FirebaseFirestore db;
    private List<Voucher> voucherList = new ArrayList<>();
    private VoucherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_qly_voucher);

        initViews();
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadData();

        btnThemVoucher.setOnClickListener(v -> showDialogVoucher(null));
    }

    private void initViews() {
        tvVoucherCount = findViewById(R.id.tvVoucherCount);
        rvVouchers = findViewById(R.id.rvVouchers);
        btnThemVoucher = findViewById(R.id.btnThemVoucher);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        MenuAdmin.setup(this, bottomNav);
    }

    private void setupRecyclerView() {
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VoucherAdapter();
        rvVouchers.setAdapter(adapter);
    }

    private void loadData() {
        db.collection("Voucher").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null) {
                voucherList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Voucher v = doc.toObject(Voucher.class);
                    if (v != null) {
                        v.setIdVoucher(doc.getId());
                        voucherList.add(v);
                    }
                }
                tvVoucherCount.setText(voucherList.size() + " voucher");
                adapter.notifyDataSetChanged();
            }
        });
    }

    // Dialog Thêm & Sửa (Dùng chung 1 hàm cho gọn)
    private void showDialogVoucher(Voucher voucher) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

       // View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_voucher, null);
        //gọi dialog mặc định không cần XML phụ

        EditText edtTen = new EditText(this);
        edtTen.setHint("Tên Voucher (VD: Giảm 50K phí khám)");

        EditText edtDiem = new EditText(this);
        edtDiem.setHint("Điểm yêu cầu (VD: 100)");
        edtDiem.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        EditText edtGiamGia = new EditText(this);
        edtGiamGia.setHint("Số tiền giảm (VD: 50000)");
        edtGiamGia.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(edtTen);
        layout.addView(edtDiem);
        layout.addView(edtGiamGia);

        String title = (voucher == null) ? "Thêm Voucher Mới" : "Sửa Voucher";
        if (voucher != null) {
            edtTen.setText(voucher.getTenVoucher());
            edtDiem.setText(String.valueOf(voucher.getDiemYeuCau()));
            edtGiamGia.setText(String.valueOf(voucher.getSoTienGiam()));
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Lưu", (d, which) -> {
                    String ten = edtTen.getText().toString().trim();
                    String diemStr = edtDiem.getText().toString().trim();
                    String giamStr = edtGiamGia.getText().toString().trim();

                    if (TextUtils.isEmpty(ten) || TextUtils.isEmpty(diemStr) || TextUtils.isEmpty(giamStr)) {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int diem = Integer.parseInt(diemStr);
                    int giam = Integer.parseInt(giamStr);

                    if (voucher == null) {
                        // Tạo mới
                        String newId = db.collection("Voucher").document().getId();
                        Voucher newVoucher = new Voucher(newId, ten, diem, giam);
                        db.collection("Voucher").document(newId).set(newVoucher)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show());
                    } else {
                        // Cập nhật
                        db.collection("Voucher").document(voucher.getIdVoucher())
                                .update("tenVoucher", ten, "diemYeuCau", diem, "soTienGiam", giam)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteVoucher(Voucher voucher) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa voucher này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("Voucher").document(voucher.getIdVoucher()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ================= ADAPTER INNER CLASS =================
    private class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_voucher, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Voucher v = voucherList.get(position);

            holder.tvTenVoucher.setText(v.getTenVoucher());
            holder.tvThongTin.setText(String.format("Giảm: %,dđ • Đổi: %d điểm", v.getSoTienGiam(), v.getDiemYeuCau()));

            holder.btnEdit.setOnClickListener(view -> showDialogVoucher(v));
            holder.btnDelete.setOnClickListener(view -> deleteVoucher(v));
        }

        @Override
        public int getItemCount() {
            return voucherList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTenVoucher, tvThongTin;
            ImageView btnEdit, btnDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTenVoucher = itemView.findViewById(R.id.tvTenVoucher);
                tvThongTin = itemView.findViewById(R.id.tvThongTin);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}