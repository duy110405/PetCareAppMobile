package com.example.petcareapp.ui.user;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.LichSu;

import java.util.Calendar;
import java.util.List;

public class LichSuAdapter extends RecyclerView.Adapter<LichSuAdapter.ViewHolder> {

    private List<LichSu> danhSach;

    public LichSuAdapter(List<LichSu> danhSach) {
        this.danhSach= danhSach;
    }

    public void capNhatDuLieu(List<LichSu> danhSachMoi) {
        this.danhSach =danhSachMoi;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lich_su, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichSu ls=danhSach.get(position);

        holder.tvManHinh.setText("Mở màn hình: " + ls.getTenManHinh());
        holder.tvEmail.setText("Tài khoản: " + ls.getEmailUser());


        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(ls.getThoiGian());
        String ngayGio=DateFormat.format("dd/MM/yyyy HH:mm:ss", cal).toString();
        holder.tvThoiGian.setText(ngayGio);
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvManHinh,tvEmail,tvThoiGian;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvManHinh =itemView.findViewById(R.id.tvManHinh);
            tvEmail= itemView.findViewById(R.id.tvEmail);
            tvThoiGian =itemView.findViewById(R.id.tvThoiGian);
        }
    }
}