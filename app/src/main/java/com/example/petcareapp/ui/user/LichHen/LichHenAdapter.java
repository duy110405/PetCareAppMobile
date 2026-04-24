package com.example.petcareapp.ui.user.LichHen;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.DichVu;
import com.example.petcareapp.data.model.LichHen;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách lịch hẹn của người dùng
 *
 * Chức năng:
 * - Hiển thị thông tin lịch hẹn
 * - Hiển thị màu theo trạng thái
 * - Cho phép xem chi tiết lịch hẹn
 * - Cho phép hủy lịch nếu còn hợp lệ
 */
public class LichHenAdapter
        extends RecyclerView.Adapter<LichHenAdapter.ViewHolder> {

    /**
     * Danh sách lịch hẹn
     */
    private List<LichHen> appointments = new ArrayList<>();

    /**
     * Format thời gian hiển thị
     */
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());

    /**
     * Cập nhật dữ liệu mới cho RecyclerView
     */
    public void setData(List<LichHen> list) {
        appointments = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_lich_hen, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        LichHen appointment = appointments.get(position);

        bindAppointmentData(holder, appointment);
        setupItemClick(holder, appointment);
        setupCancelButton(holder, appointment);
    }

    /**
     * Gán dữ liệu lên item view
     */
    private void bindAppointmentData(
            ViewHolder holder,
            LichHen appointment
    ) {
        holder.tvPetName.setText(appointment.getTenThuCung());
        holder.tvService.setText(getServicesText(appointment));
        holder.tvStatus.setText(appointment.getTrangThai());

        // Hiển thị thời gian
        if (appointment.getThoiGianHen() != null) {
            holder.tvTime.setText(
                    dateFormat.format(
                            appointment.getThoiGianHen().toDate()
                    )
            );
        } else {
            holder.tvTime.setText("Chưa có thời gian");
        }

        // Hiển thị màu trạng thái
        setupStatusColor(
                holder.cvStatus,
                appointment.getTrangThai()
        );

        // Hiển thị nút hủy
        setupCancelButtonVisibility(
                holder.btnCancelAppointment,
                appointment.getTrangThai()
        );
    }

    /**
     * Chuyển danh sách dịch vụ thành text
     */
    private String getServicesText(LichHen appointment) {
        List<DichVu> services = appointment.getDanhSachDichVu();

        if (services == null || services.isEmpty()) {
            return "Chưa có dịch vụ";
        }

        StringBuilder builder = new StringBuilder();

        for (DichVu service : services) {
            builder.append(service.getTenDichVu())
                    .append(", ");
        }

        // Xóa dấu ", " cuối
        builder.setLength(builder.length() - 2);

        return builder.toString();
    }

    /**
     * Thiết lập màu theo trạng thái
     */
    private void setupStatusColor(
            MaterialCardView statusCard,
            String status
    ) {
        int color;

        switch (status) {
            case "Chờ duyệt":
            case "Chờ xác nhận":
                color = Color.parseColor("#FFA000");
                break;

            case "Đã xác nhận":
                color = Color.parseColor("#2E64FE");
                break;

            case "Đang khám":
                color = Color.parseColor("#7B1FA2");
                break;

            case "Hoàn thành":
                color = Color.parseColor("#4CAF50");
                break;

            case "Đã hủy":
                color = Color.parseColor("#9E9E9E");
                break;

            default:
                color = Color.parseColor("#BDBDBD");
                break;
        }

        statusCard.setCardBackgroundColor(color);
    }

    /**
     * Kiểm tra có được phép hủy không
     */
    private boolean canCancelAppointment(String status) {
        return "Chờ duyệt".equals(status)
                || "Chờ xác nhận".equals(status)
                || "Đã xác nhận".equals(status);
    }

    /**
     * Hiển thị / ẩn nút hủy
     */
    private void setupCancelButtonVisibility(
            MaterialButton cancelButton,
            String status
    ) {
        cancelButton.setVisibility(
                canCancelAppointment(status)
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    /**
     * Click item -> mở trang chi tiết
     */
    private void setupItemClick(
            ViewHolder holder,
            LichHen appointment
    ) {
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(
                    v.getContext(),
                    ChiTietLichHenActivity.class
            );

            intent.putExtra("lichHenId", appointment.getId());
            v.getContext().startActivity(intent);
        });
    }

    /**
     * Xử lý nút hủy lịch
     */
    private void setupCancelButton(
            ViewHolder holder,
            LichHen appointment
    ) {
        holder.btnCancelAppointment.setOnClickListener(v -> {
            showCancelDialog(v, appointment);
        });
    }

    /**
     * Dialog xác nhận hủy
     */
    private void showCancelDialog(
            View view,
            LichHen appointment
    ) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("Xác nhận hủy")
                .setMessage("Bạn có chắc muốn hủy lịch hẹn này không?")
                .setPositiveButton("Đồng ý",
                        (dialog, which) ->
                                cancelAppointment(view, appointment)
                )
                .setNegativeButton("Không", null)
                .show();
    }

    /**
     * Hủy lịch trên Firestore
     */
    private void cancelAppointment(
            View view,
            LichHen appointment
    ) {
        FirebaseFirestore.getInstance()
                .collection("LichHen")
                .document(appointment.getId())
                .update("trangThai", "Đã hủy")
                .addOnSuccessListener(unused -> {

                    // Update local data
                    appointment.setTrangThai("Đã hủy");

                    Toast.makeText(
                            view.getContext(),
                            "Đã hủy lịch",
                            Toast.LENGTH_SHORT
                    ).show();

                    notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                view.getContext(),
                                "Hủy lịch thất bại",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    /**
     * ViewHolder chứa view của item
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvPetName;
        TextView tvService;
        TextView tvTime;
        TextView tvStatus;

        MaterialCardView cvStatus;
        MaterialButton btnCancelAppointment;

        ViewHolder(View view) {
            super(view);

            tvPetName = view.findViewById(R.id.tvPetName);
            tvService = view.findViewById(R.id.tvService);
            tvTime = view.findViewById(R.id.tvTime);
            tvStatus = view.findViewById(R.id.tvStatus);

            cvStatus = view.findViewById(R.id.cvStatus);

            btnCancelAppointment =
                    view.findViewById(R.id.btnCancelAppointment);
        }
    }
}