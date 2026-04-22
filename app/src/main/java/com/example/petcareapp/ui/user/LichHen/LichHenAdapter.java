package com.example.petcareapp.ui.user.LichHen;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
 * Adapter hiển thị danh sách lịch hẹn của user
 * Bao gồm:
 * - Hiển thị thông tin lịch hẹn
 * - Hiển thị trạng thái bằng màu
 * - Cho phép hủy lịch nếu hợp lệ
 */
public class LichHenAdapter extends RecyclerView.Adapter<LichHenAdapter.ViewHolder> {

    // Danh sách dữ liệu lịch hẹn
    private List<LichHen> list = new ArrayList<>();

    // Format thời gian hiển thị
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());

    /**
     * Cập nhật dữ liệu cho adapter
     */
    public void setData(List<LichHen> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        // Inflate layout item lịch hẹn
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_lich_hen, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Format danh sách dịch vụ thành chuỗi hiển thị
     */
    private String getServicesText(LichHen item) {

        if (item.getDanhSachDichVu() == null
                || item.getDanhSachDichVu().isEmpty()) {
            return "Chưa có dịch vụ";
        }

        StringBuilder builder = new StringBuilder();

        for (DichVu dv : item.getDanhSachDichVu()) {
            builder.append(dv.getTen()).append(", ");
        }

        // Xóa dấu phẩy cuối
        if (builder.length() > 2) {
            builder.setLength(builder.length() - 2);
        }

        return builder.toString();
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        LichHen item = list.get(position);

        // ===== HIỂN THỊ THÔNG TIN =====
        holder.tvPetName.setText(item.getTenThuCung());
        holder.tvService.setText(getServicesText(item));
        holder.tvStatus.setText(item.getTrangThai());

        if (item.getThoiGianHen() != null) {
            holder.tvTime.setText(
                    sdf.format(item.getThoiGianHen().toDate())
            );
        }

        String status = item.getTrangThai();

        // ===== ĐỔI MÀU THEO TRẠNG THÁI =====
        if ("Chờ duyệt".equals(status)
                || "Chờ xác nhận".equals(status)) {

            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#FFA000"));

        } else if ("Đã xác nhận".equals(status)) {

            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#2E64FE"));

        } else if ("Đang khám".equals(status)) {

            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#7B1FA2"));

        } else if ("Hoàn thành".equals(status)) {

            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#4CAF50"));

        } else if ("Đã hủy".equals(status)) {

            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#9E9E9E"));
        }

        // ===== HIỂN THỊ NÚT HỦY =====
        boolean canCancel =
                "Chờ duyệt".equals(status)
                        || "Chờ xác nhận".equals(status)
                        || "Đã xác nhận".equals(status);

        holder.btnCancelAppointment.setVisibility(
                canCancel ? View.VISIBLE : View.GONE
        );

        // ===== XỬ LÝ HỦY LỊCH =====
        holder.btnCancelAppointment.setOnClickListener(v -> {

            // lấy vị trí an toàn tại thời điểm click
            int adapterPos = holder.getAdapterPosition();

            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Xác nhận hủy")
                    .setMessage("Bạn có chắc muốn hủy lịch hẹn này không?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {

                        // UPDATE trạng thái lên Firestore
                        FirebaseFirestore.getInstance()
                                .collection("LichHen")
                                .document(item.getId())
                                .update("trangThai", "Đã hủy")
                                .addOnSuccessListener(aVoid -> {

                                    // cập nhật local model
                                    item.setTrangThai("Đã hủy");

                                    // refresh item an toàn
                                    if (adapterPos != RecyclerView.NO_POSITION) {
                                        notifyItemChanged(adapterPos);
                                    }

                                    // ===== (TÙY CHỌN) LOGIC SLOT =====
                                    // TODO: giải phóng giờ đặt nếu bạn có bảng slot
                                });

                    })
                    .setNegativeButton("Không", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder giữ view item lịch hẹn
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvPetName;
        TextView tvService;
        TextView tvTime;
        TextView tvStatus;

        MaterialCardView cvStatus;
        MaterialButton btnCancelAppointment;

        ViewHolder(View v) {
            super(v);

            tvPetName = v.findViewById(R.id.tvPetName);
            tvService = v.findViewById(R.id.tvService);
            tvTime = v.findViewById(R.id.tvTime);
            tvStatus = v.findViewById(R.id.tvStatus);

            cvStatus = v.findViewById(R.id.cvStatus);

            btnCancelAppointment =
                    v.findViewById(R.id.btnCancelAppointment);
        }
    }
}