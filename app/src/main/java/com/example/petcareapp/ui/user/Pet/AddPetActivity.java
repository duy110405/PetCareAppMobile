package com.example.petcareapp.ui.user.Pet;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.Pet;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddPetActivity extends AppCompatActivity {
    private EditText edtName, edtBreed, edtDob, edtWeight, edtColor;
    private Button btnSubmit, btnCancel;

    private FirebaseFirestore db;

    private ImageView imgAvatar;
    private Button btnCapturePhoto, btnUploadPhoto;

    private Uri imageUri; // lưu ảnh đã chọn


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thempet);

        db = FirebaseFirestore.getInstance();

        // mapping
        edtName = findViewById(R.id.edtPetName);
        edtBreed = findViewById(R.id.edtPetBreed);
        edtDob = findViewById(R.id.edtPetDob);
        edtWeight = findViewById(R.id.edtPetWeight);
        edtColor = findViewById(R.id.edtPetColor);

        btnSubmit = findViewById(R.id.btnAddPetSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        imgAvatar = findViewById(R.id.imgAvatar);
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);

        btnUploadPhoto.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 1);

            } else {
                galleryLauncher.launch("image/*");
            }
        });


        btnCapturePhoto.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        Manifest.permission.CAMERA
                }, 2);

            } else {
                imageUri = createImageUri();
                cameraLauncher.launch(imageUri);
            }
        });

        edtDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        String date = d + "/" + (m + 1) + "/" + y;
                        edtDob.setText(date);
                    },
                    year, month, day);

            dialog.show();
        });




        btnSubmit.setOnClickListener(v -> addPet());
        btnCancel.setOnClickListener(v -> finish());

    }

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    imgAvatar.setImageURI(imageUri);
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    imgAvatar.setImageURI(uri);
                }
            });


    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "pet_image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "from camera");
        return getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (requestCode == 1) {
                galleryLauncher.launch("image/*");
            }

            if (requestCode == 2) {
                imageUri = createImageUri();
                cameraLauncher.launch(imageUri);
            }

        } else {
            Toast.makeText(this, "Bạn cần cấp quyền", Toast.LENGTH_SHORT).show();
        }
    }



    private void addPet() {

        String name = edtName.getText().toString().trim();
        String breed = edtBreed.getText().toString().trim();

        String dobStr = edtDob.getText().toString().trim();

        if (dobStr.isEmpty()) {
            edtDob.setError("Chọn ngày sinh");
            return;
        }

        Timestamp dob = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(dobStr);
            dob = new Timestamp(date);
        } catch (Exception e) {
            Toast.makeText(this, "Ngày không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }




        String imageUrl = (imageUri != null) ? imageUri.toString() : "";


        String weightStr = edtWeight.getText().toString().trim();
        String color = edtColor.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Nhập tên thú cưng");
            return;
        }

        if (breed.isEmpty()) {
            edtBreed.setError("Nhập giống");
            return;
        }

        double weight = 0;
        try {
            if (!weightStr.isEmpty()) {
                weight = Double.parseDouble(weightStr);
            }
        } catch (Exception e) {
            edtWeight.setError("Cân nặng không hợp lệ");
            return;
        }


        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        // 🔥 tạo id pet
        String petId = db.collection("users")
                .document(userId)
                .collection("pets")
                .document()
                .getId();

        Pet pet = new Pet(
                petId,
                name,
                breed,
                dob, // 🔥 lưu DOB
                weight,
                color,
                "Khỏe mạnh",
                imageUrl,
                0
        );


        // 🔥 lưu Firestore
        db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .set(pet)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    finish(); // quay lại UserActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
