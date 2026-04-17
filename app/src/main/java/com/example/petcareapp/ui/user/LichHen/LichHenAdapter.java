package com.example.petcareapp.ui.user.LichHen;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.petcareapp.R;
import com.example.petcareapp.data.model.LichHen;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LichHenAdapter extends RecyclerView.Adapter<LichHenAdapter.ViewHolder> {
    private List<LichHen> list = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());

    public void setData(List<LichHen> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp file layout item_user_lich_hen vừa tạo ở trên
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_lich_hen, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichHen item = list.get(position);
        holder.tvPetName.setText(item.getTenThuCung());
        holder.tvService.setText(item.getLyDo());
        holder.tvStatus.setText(item.getTrangThai());

        if (item.getThoiGianHen() != null) {
            holder.tvTime.setText(sdf.format(item.getThoiGianHen().toDate()));
        }

        // Logic đổi màu Tag trạng thái dựa trên dữ liệu thực tế
        if ("Chờ xác nhận".equals(item.getTrangThai())) {
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#FFA000")); // Màu cam
        } else if ("Đang khám".equals(item.getTrangThai())) {
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#2E64FE")); // Màu xanh dương
        } else if ("Hoàn thành".equals(item.getTrangThai())) {
            holder.cvStatus.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Màu xanh lá
        }
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPetName, tvService, tvTime, tvStatus;
        MaterialCardView cvStatus;

        ViewHolder(View v) {
            super(v);
            // CÁC ID DƯỚI ĐÂY PHẢI TRÙNG KHỚP VỚI TRONG FILE item_user_lich_hen.xml
            tvPetName = v.findViewById(R.id.tvPetName);
            tvService = v.findViewById(R.id.tvService);
            tvTime = v.findViewById(R.id.tvTime);
            tvStatus = v.findViewById(R.id.tvStatus);
            cvStatus = v.findViewById(R.id.cvStatus);
        }
    }
}