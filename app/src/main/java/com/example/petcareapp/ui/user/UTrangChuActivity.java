package com.example.petcareapp.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.Pet;
import com.example.petcareapp.ui.user.Pet.AddPetActivity;
import com.example.petcareapp.ui.user.Pet.PetAdapter;
import com.example.petcareapp.ui.user.Pet.PetDetailActivity;
import com.example.petcareapp.ui.user.Pet.PetViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserActivity extends AppCompatActivity {

    private RecyclerView rvPets;
    private PetAdapter adapter;
    private PetViewModel viewModel;
    private TextView tvPetCount;
    private Button btnAddPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trangchu);

        // 🔥 mapping view
        rvPets = findViewById(R.id.rvPets);
        tvPetCount = findViewById(R.id.tvPetCount);
        btnAddPet = findViewById(R.id.btnAddPet);

        // 🔥 setup RecyclerView
        adapter = new PetAdapter();
        rvPets.setLayoutManager(new LinearLayoutManager(this));
        rvPets.setAdapter(adapter);

        // 🔥 ViewModel
        viewModel = new ViewModelProvider(this).get(PetViewModel.class);

        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
            viewModel.loadPets(userId);
        }

        // 🔥 observe data
        viewModel.getPets().observe(this, pets -> {
            if (pets != null) {
                adapter.setData(pets);
                tvPetCount.setText(String.valueOf(pets.size()));
            }
        });

        // 🔥 click thêm pet
        btnAddPet.setOnClickListener(v -> {
            startActivity(new Intent(this, AddPetActivity.class));
        });

        adapter.setOnItemClickListener(pet -> {
            Intent intent = new Intent(UserActivity.this, PetDetailActivity.class);
            intent.putExtra("petId", pet.getId());
            startActivity(intent);
        });


    }
    // 🔥 reload khi quay lại từ AddPet
    @Override
    protected void onResume() {
        super.onResume();

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            viewModel.loadPets(userId);
        }
    }

}
