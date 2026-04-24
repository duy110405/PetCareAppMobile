package com.example.petcareapp.ui.admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.DichVu;
import com.example.petcareapp.utils.MenuAdmin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class ADichVuActivity extends AppCompatActivity {

    private ADichVuViewModel viewModel;
    private RecyclerView rvDichVu;
    private ADichVuAdapter adapter;
    private MaterialCardView btnThem;
    private TextView tvSoLuong;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_qly_dichvu);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(ADichVuViewModel.class);

        // Ánh xạ View
        rvDichVu = findViewById(R.id.rvDichVu);
        btnThem = findViewById(R.id.btnThemDichVu);
        tvSoLuong = findViewById(R.id.tvSoLuong);
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);

        // Setup menu admin
        MenuAdmin.setup(this, nav);

        // RecyclerView
        rvDichVu.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ADichVuAdapter();
        rvDichVu.setAdapter(adapter);

        // 🔥 Observe danh sách dịch vụ
        viewModel.getDanhSachDichVu().observe(this, list -> {
            adapter.setData(list);
            tvSoLuong.setText(list.size() + " dịch vụ");
        });

        // 🔥 Observe thông báo
        viewModel.getThongBao().observe(this, msg -> {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        });

        // Nút thêm
        btnThem.setOnClickListener(v -> showDialog(null));

        // Sự kiện adapter
        adapter.setListener(new ADichVuAdapter.OnItemClickListener() {
            @Override
            public void onEdit(DichVu dv) {
                showDialog(dv);
            }

            @Override
            public void onDelete(DichVu dv) {
                new AlertDialog.Builder(ADichVuActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa " + dv.getTenDichVu() + "?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            viewModel.xoaDichVu(dv.getId());
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
    }

    // 🔥 Dialog dùng chung cho Thêm + Sửa
    private void showDialog(DichVu dv) {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_dichvu);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView btnDong = dialog.findViewById(R.id.btnDong);
        MaterialButton btnXacNhan = dialog.findViewById(R.id.btnXacNhan);
        MaterialButton btnHuy = dialog.findViewById(R.id.btnHuy);

        TextInputEditText edtTen = dialog.findViewById(R.id.edtTen);
        TextInputEditText edtMoTa = dialog.findViewById(R.id.edtMoTa);
        TextInputEditText edtGia = dialog.findViewById(R.id.edtGia);

        // Nếu là sửa → fill data
        if (dv != null) {
            edtTen.setText(dv.getTenDichVu());
            edtMoTa.setText(dv.getMoTa());
            edtGia.setText(String.valueOf(dv.getGia()));
            btnXacNhan.setText("Cập nhật");
        } else {
            btnXacNhan.setText("Thêm");
        }

        btnDong.setOnClickListener(v -> dialog.dismiss());
        btnHuy.setOnClickListener(v -> dialog.dismiss());

        // Xác nhận
        btnXacNhan.setOnClickListener(v -> {
            String ten = edtTen.getText().toString().trim();
            String moTa = edtMoTa.getText().toString().trim();
            String giaStr = edtGia.getText().toString().trim();

            // Validate
            if (ten.isEmpty()) {
                edtTen.setError("Nhập tên dịch vụ");
                return;
            }

            if (giaStr.isEmpty()) {
                edtGia.setError("Nhập giá");
                return;
            }

            try {
                double gia = Double.parseDouble(giaStr);

                if (gia < 0) {
                    edtGia.setError("Giá phải >= 0");
                    return;
                }

                if (dv == null) {
                    // Thêm
                    viewModel.themDichVu(ten, moTa, gia);
                } else {
                    // Sửa
                    dv.setTenDichVu(ten);
                    dv.setMoTa(moTa);
                    dv.setGia(gia);
                    viewModel.capNhatDichVu(dv);
                }

            } catch (Exception e) {
                edtGia.setError("Giá không hợp lệ");
            }
        });

        dialog.show();
    }
}