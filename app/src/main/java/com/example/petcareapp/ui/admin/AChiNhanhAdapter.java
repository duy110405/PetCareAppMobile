package com.example.petcareapp.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.ChiNhanh;

import java.util.ArrayList;
import java.util.List;

public class AChiNhanhAdapter extends RecyclerView.Adapter<AChiNhanhAdapter.ChiNhanhViewHolder> {

    private List<ChiNhanh> danhSachChiNhanh = new ArrayList<>();
    private OnItemClickListener listener;

    // Interface để truyền sự kiện click ra ngoài Activity
    public interface OnItemClickListener {
        void onSuaClick(ChiNhanh chiNhanh);
        void onXoaClick(ChiNhanh chiNhanh);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Hàm cập nhật danh sách khi Firebase có dữ liệu mới
    public void setDanhSachChiNhanh(List<ChiNhanh> danhSachMoi) {
        this.danhSachChiNhanh = danhSachMoi;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChiNhanhViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_chi_nhanh, parent, false);
        return new ChiNhanhViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChiNhanhViewHolder holder, int position) {
        ChiNhanh cn = danhSachChiNhanh.get(position);

        holder.tvTen.setText(cn.getTenChiNhanh());
        holder.tvDiaChi.setText(cn.getDiaChi());
        holder.tvSdt.setText(cn.getSoDienThoai());
        holder.tvGio.setText(cn.getGioLamViec());

        // Bắt sự kiện nhấn nút Sửa
        holder.btnSua.setOnClickListener(v -> {
            if (listener != null) listener.onSuaClick(cn);
        });

        // Bắt sự kiện nhấn nút Xóa
        holder.btnXoa.setOnClickListener(v -> {
            if (listener != null) listener.onXoaClick(cn);
        });
    }

    @Override
    public int getItemCount() {
        return danhSachChiNhanh.size();
    }

    class ChiNhanhViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvDiaChi, tvSdt, tvGio;
        ImageView btnSua, btnXoa;

        public ChiNhanhViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTen = itemView.findViewById(R.id.tvTenChiNhanh);
            tvDiaChi = itemView.findViewById(R.id.tvDiaChi);
            tvSdt = itemView.findViewById(R.id.tvSdt);
            tvGio = itemView.findViewById(R.id.tvGioLamViec);
            btnSua = itemView.findViewById(R.id.btnSuaChiNhanh);
            btnXoa = itemView.findViewById(R.id.btnXoaChiNhanh);
        }
    }
}