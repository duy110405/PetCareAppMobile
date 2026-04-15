package com.example.petcareapp.ui.user;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.Pet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

import javax.annotation.Nullable;

public class AddPetActivity extends AppCompatActivity {

    TextInputEditText edtName, edtBreed, edtDob, edtWeight, edtColor;

    MaterialButton btnCancel, btnAddPetSubmit;

    FirebaseFirestore db;
    String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thempet);

        // init view
        edtName = findViewById(R.id.edtPetName);
        edtBreed = findViewById(R.id.edtPetBreed);
        edtDob = findViewById(R.id.edtPetDob);
        edtWeight = findViewById(R.id.edtPetWeight);
        edtColor = findViewById(R.id.edtPetColor);

        btnCancel = findViewById(R.id.btnCancel);
        btnAddPetSubmit = findViewById(R.id.btnAddPetSubmit);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 📅 chọn ngày sinh
        edtDob.setOnClickListener(v -> showDatePicker());

        // ❌ cancel
        btnCancel.setOnClickListener(v -> finish());

        // ✅ save
        btnAddPetSubmit.setOnClickListener(v -> savePet());
    }

    // =====================
    // 📅 DATE PICKER
    // =====================
    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    String dob = dayOfMonth + "/" + (month + 1) + "/" + year;
                    edtDob.setText(dob);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private int calculateAge(String dob) {

        try {
            String[] parts = dob.split("/");

            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - year;

            // nếu chưa tới sinh nhật trong năm nay thì trừ 1
            if (today.get(Calendar.MONTH) + 1 < month ||
                    (today.get(Calendar.MONTH) + 1 == month &&
                            today.get(Calendar.DAY_OF_MONTH) < day)) {
                age--;
            }

            return Math.max(age, 0);

        } catch (Exception e) {
            return 0;
        }
    }


    // =====================
    // 💾 SAVE FIRESTORE
    // =====================
    private void savePet() {

        String name = edtName.getText().toString().trim();
        String breed = edtBreed.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        int age = calculateAge(dob);
        String weightStr = edtWeight.getText().toString().trim();
        String color = edtColor.getText().toString().trim();

        // ⚠️ validate
        if (name.isEmpty() || breed.isEmpty()) {
            Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = weightStr.isEmpty() ? 0 : Double.parseDouble(weightStr);

        // 📌 auto id
        String petId = db.collection("users")
                .document(uid)
                .collection("pets")
                .document()
                .getId();

        // 🐶 create object
        Pet pet = new Pet(
                petId,
                name,
                breed,
                age,          // tuổi có thể tính sau từ dob
                (int) weight,
                color,
                "Khỏe mạnh"
        );

        // 💾 save firestore
        db.collection("users")
                .document(uid)
                .collection("pets")
                .document(petId)
                .set(pet)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Thêm thú cưng thành công", Toast.LENGTH_SHORT).show();
                    finish(); // quay lại UserActivity
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
