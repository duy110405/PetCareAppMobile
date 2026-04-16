package com.example.petcareapp.ui.user.Pet;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddPetActivity extends AppCompatActivity {
    private EditText edtName, edtBreed, edtDob, edtWeight, edtColor;
    private Button btnSubmit, btnCancel;

    private ImageView imgAvatar;
    private Button btnCapturePhoto, btnUploadPhoto;

    private FirebaseFirestore db;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_them_pet);

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

        // ===================== CHỌN ẢNH TỪ GALLERY =====================
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

        // ===================== CHỤP ẢNH =====================
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

        // ===================== DATE PICKER =====================
        edtDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        String date = d + "/" + (m + 1) + "/" + y;
                        edtDob.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            // 🔥 chặn ngày tương lai
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();

        });

        btnSubmit.setOnClickListener(v -> addPet());
        btnCancel.setOnClickListener(v -> finish());
    }

    // ===================== CAMERA RESULT =====================
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    imgAvatar.setImageURI(imageUri);
                }
            });

    // ===================== GALLERY RESULT =====================
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {

                    // giữ quyền truy cập URI
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    try {
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (Exception ignored) {}

                    imageUri = uri;
                    imgAvatar.setImageURI(uri);
                }
            });

    // ===================== CREATE IMAGE URI =====================
    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "pet_image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "from camera");

        return getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
        );
    }

    // ===================== PERMISSION RESULT =====================
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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

    private Bitmap uriToBitmap(Uri uri) {
        try {
            Bitmap original = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            // THU NHỎ ẢNH ĐỂ TRÁNH LỖI 1MB CỦA FIRESTORE
            int maxWidth = 400; // Chiều rộng tối đa 400px (Đủ nét cho Avatar)
            int maxHeight = (int) ((double) original.getHeight() / original.getWidth() * maxWidth);

            return Bitmap.createScaledBitmap(original, maxWidth, maxHeight, true);
        } catch (Exception e) {
            return null;
        }
    }

    private String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] bytes = baos.toByteArray();
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    }



    // ===================== ADD PET =====================
    private void addPet() {

        String name = edtName.getText().toString().trim();
        String breed = edtBreed.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim();
        String color = edtColor.getText().toString().trim();
        String dobStr = edtDob.getText().toString().trim();

        // validate
        if (name.isEmpty()) {
            edtName.setError("Nhập tên thú cưng");
            return;
        }

        if (breed.isEmpty()) {
            edtBreed.setError("Nhập giống");
            return;
        }

        if (dobStr.isEmpty()) {
            edtDob.setError("Chọn ngày sinh");
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // parse dob
        Timestamp dob;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(dobStr);

            if (date.after(new Date())) {
                Toast.makeText(this, "Ngày sinh không được ở tương lai", Toast.LENGTH_SHORT).show();
                return;
            }

            dob = new Timestamp(date);
        } catch (Exception e) {
            Toast.makeText(this, "Ngày không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // parse weight
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

        // tạo petId
        String petId = db.collection("users")
                .document(userId)
                .collection("pets")
                .document()
                .getId();

        Bitmap bitmap = uriToBitmap(imageUri);

        if (bitmap == null) {
            Toast.makeText(this, "Không đọc được ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        String base64Image = encodeToBase64(bitmap);


        Pet pet = new Pet(
                petId,
                name,
                breed,
                dob,
                weight,
                color,
                "Khỏe mạnh",
                base64Image,
                0
        );

        db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .set(pet)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}