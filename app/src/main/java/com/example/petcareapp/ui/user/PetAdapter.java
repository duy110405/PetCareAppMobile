package com.example.petcareapp.ui.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.Pet;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder>{
    private List<Pet> petList;

    public PetAdapter(List<Pet> petList) {
        this.petList = petList;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pet, parent, false);

        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {

        Pet pet = petList.get(position);

        holder.txtName.setText(pet.getName() != null ? pet.getName() : "");

        holder.txtBreedAge.setText(
                (pet.getBreed() != null ? pet.getBreed() : "") +
                        " • " + pet.getAge() + " tuổi"
        );

        holder.txtInfo.setText(
                "Cân nặng: " + pet.getWeight() +
                        " kg   Màu: " + (pet.getColor() != null ? pet.getColor() : "")
        );

        holder.txtStatus.setText(pet.getStatus() != null ? pet.getStatus() : "");

        // 🎨 màu trạng thái
        if ("Khỏe mạnh".equals(pet.getStatus())) {
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_green);
        } else {
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_red);
        }
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtBreedAge, txtInfo, txtStatus;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtBreedAge = itemView.findViewById(R.id.txtBreedAge);
            txtInfo = itemView.findViewById(R.id.txtInfo);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}
