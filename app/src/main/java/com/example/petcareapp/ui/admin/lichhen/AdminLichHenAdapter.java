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

/**
 * Adapter hiển thị danh sách lịch hẹn cho Admin.
 *
 * Chức năng:
 * - Hiển thị thông tin cơ bản của từng lịch hẹn
 * - Hiển thị trạng thái theo màu sắc
 * - Duyệt nhanh lịch hẹn
 * - Mở màn hình chi tiết lịch hẹn
 */
public class AdminLichHenAdapter
        extends RecyclerView.Adapter<AdminLichHenAdapter.ViewHolder> {

    // Danh sách lịch hẹn
    private List<LichHen> list;

    // Context dùng để mở Activity / Toast
    private final Context context;

    // Listener callback khi trạng thái thay đổi
    private OnStatusChangedListener listener;

    /**
     * Callback dùng để báo Activity reload lại dữ liệu.
     */
    public interface OnStatusChangedListener {
        void onStatusChanged();
    }

    public void setOnStatusChangedListener(
            OnStatusChangedListener listener
    ) {
        this.listener = listener;
    }

    // Formatter thời gian hiển thị
    private final SimpleDateFormat sdf =
            new SimpleDateFormat(
                    "HH:mm - dd/MM/yyyy",
                    Locale.getDefault()
            );

    public AdminLichHenAdapter(
            Context context,
            List<LichHen> list
    ) {
        this.context = context;
        this.list = list;
    }

    /**
     * Cập nhật lại danh sách dữ liệu.
     */
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

        // Inflate layout item cho từng lịch hẹn
        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_admin_lich_hen,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        LichHen item = list.get(position);

        // ===== Thông tin thú cưng =====
        holder.txtName.setText(item.getTenThuCung());

        // ===== Thông tin chủ nuôi =====
        if (item.getTenChuThuCung() != null) {
            holder.txtOwnerName.setText(
                    item.getTenChuThuCung()
            );
        } else {
            holder.txtOwnerName.setText("Khách ẩn danh");
        }

        // ===== Số điện thoại =====
        if (item.getSoDienThoai() != null) {
            holder.txtPhone.setText(
                    item.getSoDienThoai()
            );
        }

        // ===== Thời gian hẹn =====
        if (item.getThoiGianHen() != null) {
            holder.txtTime.setText(
                    sdf.format(
                            item.getThoiGianHen().toDate()
                    )
            );
        }

        // ===== Chi nhánh =====
        holder.txtBranch.setText(
                item.getTenChiNhanh()
        );

        // ===== Trạng thái =====
        String status = item.getTrangThai();
        holder.txtStatus.setText(status);

        // Thiết lập màu trạng thái
        setupStatusColor(holder, status);

        // Ẩn / hiện nút xử lý
        setupButtonVisibility(holder, status);

        // ===== Click mở chi tiết =====
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(
                    context,
                    AdminChiTietLichHenActivity.class
            );

            intent.putExtra(
                    "lichHenId",
                    item.getId()
            );

            context.startActivity(intent);
        });

        // ===== Duyệt nhanh =====
        holder.btnApprove.setOnClickListener(
                v -> approveAppointment(item)
        );

        // ===== Mở chi tiết để từ chối =====
        holder.btnReject.setOnClickListener(v -> {
            Intent intent = new Intent(
                    context,
                    AdminChiTietLichHenActivity.class
            );

            intent.putExtra(
                    "lichHenId",
                    item.getId()
            );

            context.startActivity(intent);
        });
    }

    /**
     * Duyệt nhanh lịch hẹn ngay tại danh sách.
     */
    private void approveAppointment(LichHen item) {
        FirebaseFirestore.getInstance()
                .collection("LichHen")
                .document(item.getId())
                .get()
                .addOnSuccessListener(snapshot -> {

                    // Kiểm tra document tồn tại
                    if (!snapshot.exists()) {
                        Toast.makeText(
                                context,
                                "Lịch hẹn không tồn tại",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    String currentStatus =
                            snapshot.getString("trangThai");

                    // Kiểm tra xung đột trạng thái
                    if (!"Chờ duyệt".equals(currentStatus)) {
                        Toast.makeText(
                                context,
                                "Khách hàng đã hủy lịch hẹn này",
                                Toast.LENGTH_SHORT
                        ).show();

                        notifyDataSetChanged();
                        return;
                    }

                    // Cập nhật trạng thái duyệt
                    snapshot.getReference()
                            .update(
                                    "trangThai",
                                    "Đã xác nhận"
                            )
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        context,
                                        "Đã duyệt lịch hẹn",
                                        Toast.LENGTH_SHORT
                                ).show();

                                // Cập nhật UI local
                                item.setTrangThai("Đã xác nhận");
                                notifyDataSetChanged();

                                // Callback cho Activity nếu cần
                                if (listener != null) {
                                    listener.onStatusChanged();
                                }
                            });
                });
    }

    /**
     * Thiết lập màu chữ theo trạng thái.
     */
    private void setupStatusColor(
            ViewHolder holder,
            String status
    ) {
        switch (status) {
            case "Chờ duyệt":
                holder.txtStatus.setTextColor(Color.parseColor("#FFA000")); // Cam
                break;
            case "Đã xác nhận":
                holder.txtStatus.setTextColor(Color.parseColor("#4CAF50")); // Xanh lá
                break;
            case "Đang khám":
                holder.txtStatus.setTextColor(Color.parseColor("#2E64FE")); // Xanh dương
                break;
            case "Hoàn thành":
                holder.txtStatus.setTextColor(Color.parseColor("#8E24AA")); // Tím
                break;
            case "Đã hủy":
                holder.txtStatus.setTextColor(Color.parseColor("#9E9E9E")); // Xám
                break;
            default:
                holder.txtStatus.setTextColor(Color.BLACK);
                break;
        }
    }

    /**
     * Ẩn / hiện nút Duyệt và Từ chối.
     *
     * Chỉ hiển thị khi trạng thái đang chờ duyệt.
     */
    private void setupButtonVisibility(
            ViewHolder holder,
            String status
    ) {
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

    /**
     * ViewHolder quản lý view của từng item lịch hẹn.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        TextView txtTime;
        TextView txtBranch;
        TextView txtStatus;
        TextView txtOwnerName;
        TextView txtPhone;

        MaterialButton btnApprove;
        MaterialButton btnReject;

        public ViewHolder(@NonNull View v) {
            super(v);

            txtName = v.findViewById(R.id.txtName);
            txtOwnerName = v.findViewById(R.id.txtOwnerName);
            txtPhone = v.findViewById(R.id.txtPhone);
            txtTime = v.findViewById(R.id.txtTime);
            txtBranch = v.findViewById(R.id.txtBranch);
            txtStatus = v.findViewById(R.id.txtStatus);

            btnApprove = v.findViewById(R.id.btnApprove);
            btnReject = v.findViewById(R.id.btnReject);
        }
    }
}