package com.example.petcareapp.ui.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.ChiNhanh;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class UChiNhanhAdapter extends RecyclerView.Adapter<UChiNhanhAdapter.UChiNhanhViewHolder> {

    private List<ChiNhanh> danhSachChiNhanh = new ArrayList<>();

    // Hàm cập nhật danh sách khi Firebase có dữ liệu mới
    public void setDanhSachChiNhanh(List<ChiNhanh> danhSachMoi) {
        this.danhSachChiNhanh = danhSachMoi;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UChiNhanhViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_chi_nhanh, parent, false);
        return new UChiNhanhViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UChiNhanhViewHolder holder, int position) {
        ChiNhanh chiNhanh = danhSachChiNhanh.get(position);

        //  Gắn dữ liệu chữ lên giao diện thẻ
        holder.tvTen.setText(chiNhanh.getTenChiNhanh());
        holder.tvDiaChi.setText(chiNhanh.getDiaChi());
        holder.tvSdt.setText(chiNhanh.getSoDienThoai());
        holder.tvGio.setText(chiNhanh.getGioLamViec());

        // Xử lý nút Chỉ đường (Mở Google Maps thẳng từ Adapter)
        holder.btnChiDuong.setOnClickListener(v -> {
            double lat = chiNhanh.getViDo();
            double lng = chiNhanh.getKinhDo();
            String ten = chiNhanh.getTenChiNhanh();

            String uriMaps = String.format("geo:0,0?q=%f,%f(%s)", lat, lng, ten);
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uriMaps));
            intent.setPackage("com.google.android.apps.maps");

            try {
                holder.itemView.getContext().startActivity(intent);
            } catch (android.content.ActivityNotFoundException ex) {
                android.content.Intent webIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("http://maps.google.com/maps?q=" + lat + "," + lng));
                holder.itemView.getContext().startActivity(webIntent);
            }
        });
    }

    // Nếu thiếu hàm này danh sách sẽ không hiện gì cả
    @Override
    public int getItemCount() {
        return danhSachChiNhanh.size();
    }

    //Khai báo ánh xạ View
    class UChiNhanhViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvDiaChi, tvSdt, tvGio;
        MaterialButton btnChiDuong;

        public UChiNhanhViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTen = itemView.findViewById(R.id.tvTenChiNhanhUser);
            tvDiaChi = itemView.findViewById(R.id.tvDiaChiUser);
            tvSdt = itemView.findViewById(R.id.tvSdtUser);
            tvGio = itemView.findViewById(R.id.tvGioUser);
            btnChiDuong = itemView.findViewById(R.id.btnChiDuong);
        }
    }
}