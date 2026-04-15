package com.example.petcareapp.ui.user.Pet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.petcareapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PetDetailActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private TextView tvName, tvBreed, tvDob, tvWeight, tvColor;
    private MaterialButton btnDelete;

    private FirebaseFirestore db;
    private String userId, petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chitietpet);

        // 🔥 init firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        // 🔥 lấy petId từ intent
        petId = getIntent().getStringExtra("petId");

        // 🔥 mapping view
        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvPetName);
        tvBreed = findViewById(R.id.tvPetBreed);
        tvDob = findViewById(R.id.tvPetDob);
        tvWeight = findViewById(R.id.tvPetWeight);
        tvColor = findViewById(R.id.tvPetColor);

        btnDelete = findViewById(R.id.btnDeletePet);

        // 🔥 load data
        loadPet();

        // 🔥 delete pet
        btnDelete.setOnClickListener(v -> deletePet());
    }

    // ===============================
    // 🔥 LOAD PET FROM FIRESTORE
    // ===============================
    private void loadPet() {
        if (userId == null || petId == null) return;

        db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        String name = doc.getString("name");
                        String breed = doc.getString("breed");
                        String color = doc.getString("color");
                        String base64Image = doc.getString("imageBase64");
                        Double weight = doc.getDouble("weight");
                        Timestamp dob = doc.getTimestamp("dob");

                        // 🔥 set data
                        tvName.setText(name);
                        tvBreed.setText(breed);
                        tvColor.setText(color);
                        tvWeight.setText(weight != null ? weight + " kg" : "");

                        if (dob != null) {
                            tvDob.setText(formatDate(dob) + " (" + calculateAge(dob) + " tuổi)");
                        }

                        if (base64Image != null && !base64Image.isEmpty()) {
                            byte[] decoded = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            imgAvatar.setImageBitmap(bitmap);
                        } else {
                            imgAvatar.setImageResource(R.drawable.sample_dog);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi load: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ===============================
    // 🔥 FORMAT DATE
    // ===============================
    private String formatDate(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    // ===============================
    // 🔥 TÍNH TUỔI
    // ===============================
    private int calculateAge(Timestamp dob) {
        Calendar birth = Calendar.getInstance();
        birth.setTime(dob.toDate());

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    // ===============================
    // 🔥 DELETE PET
    // ===============================
    private void deletePet() {
        if (userId == null || petId == null) return;

        db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                    finish(); // quay về UserActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
