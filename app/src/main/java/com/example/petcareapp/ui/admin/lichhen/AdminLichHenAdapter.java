package com.example.petcareapp.ui.admin.lichhen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.LichHen;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminLichHenAdapter
        extends RecyclerView.Adapter<AdminLichHenAdapter.ViewHolder> {

    private List<LichHen> list;
    private final Context context;
    private OnStatusChangedListener listener;
    public interface OnStatusChangedListener {
        void onStatusChanged();
    }

    public void setOnStatusChangedListener(OnStatusChangedListener listener) {
        this.listener = listener;
    }

    private final SimpleDateFormat sdf =
            new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

    public AdminLichHenAdapter(Context context, List<LichHen> list) {
        this.context = context;
        this.list = list;
    }

    public void updateList(List<LichHen> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_lich_hen, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        LichHen item = list.get(position);

        holder.txtName.setText(item.getTenThuCung());

        if (item.getThoiGianHen() != null) {
            holder.txtTime.setText(
                    sdf.format(item.getThoiGianHen().toDate())
            );
        }

        holder.txtBranch.setText(item.getTenChiNhanh());

        String status = item.getTrangThai();
        holder.txtStatus.setText(status);

        setupStatusColor(holder, status);
        setupButtonVisibility(holder, status);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(
                    context,
                    AdminChiTietLichHenActivity.class
            );
            intent.putExtra("lichHenId", item.getId());
            context.startActivity(intent);
        });

        holder.btnApprove.setOnClickListener(v -> approveAppointment(item));

        holder.btnReject.setOnClickListener(v -> {
            Intent intent = new Intent(
                    context,
                    AdminChiTietLichHenActivity.class
            );
            intent.putExtra("lichHenId", item.getId());
            context.startActivity(intent);
        });
    }

    private void approveAppointment(LichHen item) {
        FirebaseFirestore.getInstance()
                .collection("LichHen")
                .document(item.getId())
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        Toast.makeText(
                                context,
                                "Lịch hẹn không tồn tại",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    String currentStatus = snapshot.getString("trangThai");

                    // CHECK XUNG ĐỘT
                    if (!"Chờ duyệt".equals(currentStatus)) {
                        Toast.makeText(
                                context,
                                "Khách hàng đã hủy lịch hẹn này",
                                Toast.LENGTH_SHORT
                        ).show();

                        notifyDataSetChanged();
                        return;
                    }

                    // UPDATE NẾU HỢP LỆ
                    snapshot.getReference()
                            .update("trangThai", "Đã xác nhận")
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(
                                        context,
                                        "Đã duyệt lịch hẹn",
                                        Toast.LENGTH_SHORT
                                ).show();

                                item.setTrangThai("Đã xác nhận");
                                notifyDataSetChanged();
                            });
                });
    }

    private void setupStatusColor(ViewHolder holder, String status) {
        switch (status) {
            case "Chờ duyệt":
                holder.txtStatus.setTextColor(Color.parseColor("#FFA000"));
                break;

            case "Đã xác nhận":
                holder.txtStatus.setTextColor(Color.parseColor("#4CAF50"));
                break;

            case "Đã hủy":
                holder.txtStatus.setTextColor(Color.parseColor("#9E9E9E"));
                break;

            default:
                holder.txtStatus.setTextColor(Color.BLACK);
                break;
        }
    }

    private void setupButtonVisibility(ViewHolder holder, String status) {
        if ("Chờ duyệt".equals(status)) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtTime, txtBranch, txtStatus;
        MaterialButton btnApprove, btnReject;

        public ViewHolder(@NonNull View v) {
            super(v);

            txtName = v.findViewById(R.id.txtName);
            txtTime = v.findViewById(R.id.txtTime);
            txtBranch = v.findViewById(R.id.txtBranch);
            txtStatus = v.findViewById(R.id.txtStatus);

            btnApprove = v.findViewById(R.id.btnApprove);
            btnReject = v.findViewById(R.id.btnReject);
        }
    }
}