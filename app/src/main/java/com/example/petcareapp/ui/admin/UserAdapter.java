package com.example.petcareapp.ui.admin;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> list;
    private Context context;
    private FirebaseFirestore db;

    public UserAdapter(Context context, List<User> list) {
        this.context = context;
        this.list = list;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = list.get(position);

        holder.tvName.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhone());
        holder.tvPet.setText(user.getPetCount() + " thú cưng");

        // trạng thái khóa
        if (user.isLocked()) {
            holder.tvLocked.setVisibility(View.VISIBLE);
            holder.btnUnlock.setVisibility(View.VISIBLE);
            holder.btnLock.setVisibility(View.GONE);
        } else {
            holder.tvLocked.setVisibility(View.GONE);
            holder.btnUnlock.setVisibility(View.GONE);
            holder.btnLock.setVisibility(View.VISIBLE);
        }

        // ===== CLICK KHÓA =====
        holder.btnLock.setOnClickListener(v -> {
            db.collection("users").document(user.getId())
                    .update("locked", true)
                    .addOnSuccessListener(unused -> {
                        user.setLocked(true);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Đã khóa user", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Lỗi khóa", Toast.LENGTH_SHORT).show()
                    );
        });

        // ===== CLICK MỞ KHÓA =====
        holder.btnUnlock.setOnClickListener(v -> {
            db.collection("users").document(user.getId())
                    .update("locked", false)
                    .addOnSuccessListener(unused -> {
                        user.setLocked(false);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Đã mở khóa", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Lỗi mở khóa", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvPet, tvLocked;
        LinearLayout btnUnlock, btnLock;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvPet = itemView.findViewById(R.id.tvPet);
            tvLocked = itemView.findViewById(R.id.tvLocked);

            btnUnlock = itemView.findViewById(R.id.btnUnlock);
            btnLock = itemView.findViewById(R.id.btnLock);
        }
    }
    public void updateList(List<User> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

}
