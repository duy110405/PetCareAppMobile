package com.example.petcareapp.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.DichVu;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ADichVuAdapter extends RecyclerView.Adapter<ADichVuAdapter.ViewHolder> {

    private List<DichVu> list = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEdit(DichVu dv);
        void onDelete(DichVu dv);
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<DichVu> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvGia;
        MaterialButton btnSua, btnXoa;

        public ViewHolder(View v) {
            super(v);
            tvTen = v.findViewById(R.id.tvTenDV);
            tvGia = v.findViewById(R.id.tvGiaDV);
            btnSua = v.findViewById(R.id.btnSua);
            btnXoa = v.findViewById(R.id.btnXoa);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dichvu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        DichVu dv = list.get(position);
        h.tvTen.setText(dv.getTenDichVu());
        h.tvGia.setText(dv.getGia() + " VNĐ");

        h.btnSua.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(dv);
        });

        h.btnXoa.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(dv);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}