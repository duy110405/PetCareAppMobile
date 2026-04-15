package com.example.petcareapp.ui.user.Pet;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.Pet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder>{
    private List<Pet> list = new ArrayList<>();

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Pet pet);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Pet> newList) {
        this.list = (newList != null) ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }

    private int calculateAge(com.google.firebase.Timestamp dob) {
        if (dob == null) return 0;

        Calendar birth = Calendar.getInstance();
        birth.setTime(dob.toDate());

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }


    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = list.get(position);

        holder.txtName.setText(pet.getName());

        int age = calculateAge(pet.getDob());
        holder.txtBreedAge.setText(pet.getBreed() + " • " + age + " tuổi");

        holder.txtInfo.setText(
                "Cân nặng: " + pet.getWeight() + " kg   Màu: " + pet.getColor()
        );

        holder.txtActivity.setText(
                pet.getReminderCount() + " nhắc nhở đang hoạt động"
        );

        holder.txtStatus.setText(pet.getStatus());

        if ("Khỏe mạnh".equalsIgnoreCase(pet.getStatus())) {
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_green);
        } else {
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_red);
        }

        String base64 = pet.getImageBase64();

        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                holder.imgPet.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.imgPet.setImageResource(R.drawable.sample_dog);
            }
        } else {
            holder.imgPet.setImageResource(R.drawable.sample_dog);
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(pet);
            }
        });

    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    static class PetViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPet;
        TextView txtName, txtStatus, txtBreedAge, txtInfo, txtActivity;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPet = itemView.findViewById(R.id.imgPet);
            txtName = itemView.findViewById(R.id.txtName);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtBreedAge = itemView.findViewById(R.id.txtBreedAge);
            txtInfo = itemView.findViewById(R.id.txtInfo);
            txtActivity = itemView.findViewById(R.id.txtActivity);
        }
    }
}