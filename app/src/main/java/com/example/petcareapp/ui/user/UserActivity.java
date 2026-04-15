package com.example.petcareapp.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.Pet;
import com.example.petcareapp.ui.auth.DangNhapActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    TextView tvPetCount, tvUserName;

    RecyclerView rvPets;
    PetAdapter adapter;
    List<Pet> petList;

    FirebaseFirestore db;
    String uid;

    FirebaseUser user;

    BottomNavigationView bottomNav;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trangchu);

        rvPets = findViewById(R.id.rvPets);
        tvPetCount = findViewById(R.id.tvPetCount);

        tvUserName = findViewById(R.id.tvUserName);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String name = user.getDisplayName();

            if (name != null && !name.isEmpty()) {
                tvUserName.setText(name);
            } else {
                tvUserName.setText(user.getEmail());
            }
        } else {
            tvUserName.setText("Chưa đăng nhập");
        }


        bottomNav = findViewById(R.id.bottomNavigationView);


        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_logout) {

                FirebaseAuth.getInstance().signOut();

                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                // quay về màn login
                Intent intent = new Intent(this, DangNhapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                return true;
            }

            return true;
        });

        petList = new ArrayList<>();
        adapter = new PetAdapter(petList);

        rvPets.setLayoutManager(new LinearLayoutManager(this));
        rvPets.setAdapter(adapter);

        Button btnAddPet = findViewById(R.id.btnAddPet);

        btnAddPet.setOnClickListener(v -> {
            startActivity(new Intent(this, AddPetActivity.class));
        });


        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        loadPets();
    }

    /// =========================
    // 🔥 LOAD PETS REALTIME FIRESTORE
    // =========================
    private void loadPets() {

        db.collection("users")
                .document(uid)
                .collection("pets")
                .addSnapshotListener((QuerySnapshot value,
                                      FirebaseFirestoreException error) -> {

                    if (error != null) {
                        Log.e("FIRESTORE", "Error: " + error.getMessage());
                        Toast.makeText(this, "Lỗi load dữ liệu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null) return;

                    petList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {

                        Pet pet = doc.toObject(Pet.class);

                        if (pet != null) {
                            pet.setId(doc.getId());
                            petList.add(pet);
                        }
                    }

                    // 🔥 UPDATE COUNT
                    tvPetCount.setText(String.valueOf(petList.size()));

                    adapter.notifyDataSetChanged();

                    Log.d("FIRESTORE", "Loaded pets: " + petList.size());
                });
    }
}
