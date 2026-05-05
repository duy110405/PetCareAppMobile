package com.example.petcareapp.ui.user;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class UserProfileActivity extends AppCompatActivity {

    private EditText edtUsername, edtEmail, edtPhone, edtPetCount;
    private ImageView imgAvatar, btnBack;
    private Button btnEdit, btnGallery, btnCamera, btnDoiVoucher;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String currentUserId;
    private boolean isEditing = false;

    private Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        // ===== ÁNH XẠ =====
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPetCount = findViewById(R.id.edtPetCount);

        imgAvatar = findViewById(R.id.imgAvatar);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);
        btnDoiVoucher = findViewById(R.id.btnDoiVoucher);

        // ===== FIREBASE =====
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            loadUserData();
        } else {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }

        setEditable(false);

        // ===== BACK =====
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, UCaiDatActivity.class));
        });

        // ===== GALLERY =====
        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // ===== CAMERA =====
        btnCamera.setOnClickListener(v ->
                requestCameraPermission.launch(Manifest.permission.CAMERA)
        );

        // ===== EDIT =====
        btnEdit.setOnClickListener(v -> {
            if (!isEditing) {
                isEditing = true;
                setEditable(true);
                btnEdit.setText("Lưu");
            } else {
                updateUser();
            }
        });

        btnDoiVoucher.setOnClickListener(v ->
                startActivity(new Intent(this, UVoucherActivity.class))
        );
    }

    // ================= LOAD DATA =================
    private void loadUserData() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        edtUsername.setText(doc.getString("username"));
                        edtEmail.setText(doc.getString("email"));
                        edtPhone.setText(doc.getString("phone"));

                        Long petCount = doc.getLong("petCount");
                        edtPetCount.setText(petCount != null ? String.valueOf(petCount) : "0");

                        String base64 = doc.getString("avatarBase64");
                        if (base64 != null && !base64.isEmpty()) {
                            imgAvatar.setImageBitmap(decodeBase64(base64));
                        }
                    }
                });
    }

    // ================= EDIT ENABLE =================
    private void setEditable(boolean enable) {
        edtUsername.setEnabled(enable);
        edtPhone.setEnabled(enable);
        edtEmail.setEnabled(false);
        edtPetCount.setEnabled(false);
    }

    // ================= UPDATE USER =================
    private void updateUser() {
        String username = edtUsername.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            edtUsername.setError("Không được để trống");
            return;
        }

        db.collection("users").document(currentUserId)
                .update("username", username, "phone", phone)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    isEditing = false;
                    setEditable(false);
                    btnEdit.setText("Sửa");
                });
    }

    // ================= GALLERY =================
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imgAvatar.setImageURI(uri);
                    saveAvatar(encodeImage(uri));
                }
            });

    // ================= CAMERA =================
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    imgAvatar.setImageURI(cameraImageUri);
                    saveAvatar(encodeImage(cameraImageUri));
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCamera();
                else Toast.makeText(this, "Cần cấp quyền camera", Toast.LENGTH_SHORT).show();
            });

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "avatar");

        cameraImageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        );

        cameraLauncher.launch(cameraImageUri);
    }

    // ================= ENCODE =================
    private String encodeImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);

            bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);

            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        } catch (Exception e) {
            return null;
        }
    }

    // ================= SAVE AVATAR =================
    private void saveAvatar(String base64) {
        if (base64 == null) return;

        db.collection("users").document(currentUserId)
                .update("avatarBase64", base64)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Đã cập nhật avatar", Toast.LENGTH_SHORT).show()
                );
    }

    // ================= DECODE =================
    private Bitmap decodeBase64(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}