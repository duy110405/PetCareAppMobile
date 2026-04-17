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
import com.example.petcareapp.data.model.ChiNhanh;
import com.example.petcareapp.utils.MenuAdmin;
import com.example.petcareapp.utils.MenuUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class AChiNhanhActivity extends AppCompatActivity {

    private AChiNhanhViewModel aChiNhanhViewModel;
    private MaterialCardView btnMoDialogThem;
    private RecyclerView rvChiNhanh;
    private AChiNhanhAdapter adapter;
    private Dialog dialogThemSua;
    private TextView tvDemChiNhanh; // Nơi hiển thị "3 chi nhánh" ở Header

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_qly_chinhanh);

        aChiNhanhViewModel = new ViewModelProvider(this).get(AChiNhanhViewModel.class);

        // Ánh xạ View
        btnMoDialogThem = findViewById(R.id.btnThemChiNhanh);
        rvChiNhanh = findViewById(R.id.rvBranches);
        tvDemChiNhanh = findViewById(R.id.tvBranchCount);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        MenuAdmin.setup(this, bottomNav);
        // Cài đặt RecyclerView
        rvChiNhanh.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AChiNhanhAdapter();
        rvChiNhanh.setAdapter(adapter);

        // Bắt sự kiện khi danh sách trên Firebase thay đổi
        aChiNhanhViewModel.getDanhSachChiNhanh().observe(this, danhSach -> {
            adapter.setDanhSachChiNhanh(danhSach);
            tvDemChiNhanh.setText(danhSach.size() + " chi nhánh"); // Cập nhật số lượng trên Header
        });

        // Bắt sự kiện Thông báo (Thêm/Sửa/Xóa xong)
        aChiNhanhViewModel.getTrangThaiThemChiNhanh().observe(this, thongBao -> {
            Toast.makeText(this, thongBao, Toast.LENGTH_SHORT).show();
            if (dialogThemSua != null && dialogThemSua.isShowing()) {
                dialogThemSua.dismiss();
            }
        });

        btnMoDialogThem.setOnClickListener(v -> hienThiDialog(null));

        // Bắt sự kiện bấm nút SỬA / XÓA trên Adapter
        adapter.setOnItemClickListener(new AChiNhanhAdapter.OnItemClickListener() {
            @Override
            public void onSuaClick(ChiNhanh chiNhanh) {
                // Truyền đối tượng đã có vào Dialog (Sửa)
                hienThiDialog(chiNhanh);
            }

            @Override
            public void onXoaClick(ChiNhanh chiNhanh) {
                // Hiện bảng hỏi cho chắc chắn trước khi xóa
                new AlertDialog.Builder(AChiNhanhActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa " + chiNhanh.getTenChiNhanh() + "?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            aChiNhanhViewModel.xoaChiNhanh(chiNhanh.getId());
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
    }

    // HÀM DÙNG CHUNG CHO CẢ THÊM VÀ SỬA
    private void hienThiDialog(ChiNhanh chiNhanhDuocChon) {
        dialogThemSua = new Dialog(this);
        dialogThemSua.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogThemSua.setContentView(R.layout.dialog_them_chinhanh);

        Window window = dialogThemSua.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView btnDong = dialogThemSua.findViewById(R.id.btnDong);
        MaterialButton btnHuy = dialogThemSua.findViewById(R.id.btnHuy);
        MaterialButton btnXacNhan = dialogThemSua.findViewById(R.id.btnXacNhanThem);

        TextInputEditText edtTen = dialogThemSua.findViewById(R.id.edtTenChiNhanh);
        TextInputEditText edtDiaChi = dialogThemSua.findViewById(R.id.edtDiaChi);
        TextInputEditText edtSdt = dialogThemSua.findViewById(R.id.edtSdt);
        TextInputEditText edtGio = dialogThemSua.findViewById(R.id.edtGioLamViec);
        TextInputEditText edtViDo = dialogThemSua.findViewById(R.id.edtViDo);
        TextInputEditText edtKinhDo = dialogThemSua.findViewById(R.id.edtKinhDo);

        // NẾU LÀ SỬA: Bơm dữ liệu cũ vào form và đổi chữ trên nút
        if (chiNhanhDuocChon != null) {
            edtTen.setText(chiNhanhDuocChon.getTenChiNhanh());
            edtDiaChi.setText(chiNhanhDuocChon.getDiaChi());
            edtSdt.setText(chiNhanhDuocChon.getSoDienThoai());
            edtGio.setText(chiNhanhDuocChon.getGioLamViec());
            edtViDo.setText(String.valueOf(chiNhanhDuocChon.getViDo()));
            edtKinhDo.setText(String.valueOf(chiNhanhDuocChon.getKinhDo()));

            btnXacNhan.setText("Cập nhật");
        } else {
            btnXacNhan.setText("Thêm");
        }

        btnDong.setOnClickListener(v -> dialogThemSua.dismiss());
        btnHuy.setOnClickListener(v -> dialogThemSua.dismiss());

        // BẤM NÚT XÁC NHẬN (Thêm hoặc Sửa)
        btnXacNhan.setOnClickListener(v -> {
            String ten = edtTen.getText().toString().trim();
            String diaChi = edtDiaChi.getText().toString().trim();
            String sdt = edtSdt.getText().toString().trim();
            String gio = edtGio.getText().toString().trim();
            String chuoiViDo = edtViDo.getText().toString().trim();
            String chuoiKinhDo = edtKinhDo.getText().toString().trim();

            if (ten.isEmpty() || diaChi.isEmpty() || sdt.isEmpty() || chuoiViDo.isEmpty() || chuoiKinhDo.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin có dấu *", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double viDo = Double.parseDouble(chuoiViDo);
                double kinhDo = Double.parseDouble(chuoiKinhDo);

                btnXacNhan.setEnabled(false);

                if (chiNhanhDuocChon == null) {
                    // TRƯỜNG HỢP THÊM MỚI
                    aChiNhanhViewModel.taoMoiChiNhanh(ten, diaChi, sdt, gio, viDo, kinhDo);
                } else {
                    // TRƯỜNG HỢP CẬP NHẬT: Giữ lại ID cũ, chỉ cập nhật thông tin
                    chiNhanhDuocChon.setTenChiNhanh(ten);
                    chiNhanhDuocChon.setDiaChi(diaChi);
                    chiNhanhDuocChon.setSoDienThoai(sdt);
                    chiNhanhDuocChon.setGioLamViec(gio);
                    chiNhanhDuocChon.setViDo(viDo);
                    chiNhanhDuocChon.setKinhDo(kinhDo);

                    aChiNhanhViewModel.capNhatChiNhanh(chiNhanhDuocChon);
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Tọa độ phải là số hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        dialogThemSua.show();
    }
}