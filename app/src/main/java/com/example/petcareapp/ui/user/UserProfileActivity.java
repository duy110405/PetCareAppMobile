package com.example.petcareapp.ui.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class UserProfileActivity extends AppCompatActivity {

    private EditText edtUsername, edtEmail, edtPhone, edtPetCount;
    private ImageView imgAvatar, btnBack;
    private Button btnEdit, btnGallery , btnDoiVoucher;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String currentUserId;
    private boolean isEditing = false;

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

        // ===== DISABLE EDIT =====
        setEditable(false);

        // ===== BACK =====
        btnBack.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), UCaiDatActivity.class);
            startActivity(intent);
        });

        // ===== CHỌN ẢNH =====
        btnGallery.setOnClickListener(v -> openGallery());

        // ===== SỬA / LƯU =====
        btnEdit.setOnClickListener(v -> {
            if (!isEditing) {
                isEditing = true;
                setEditable(true);
                btnEdit.setText("Lưu");
            } else {
                updateUser();
            }
        });
        btnDoiVoucher.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), UVoucherActivity.class);
            startActivity(intent);
        });
    }

    // ================= LOAD DATA =================
    private void loadUserData() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        edtUsername.setText(documentSnapshot.getString("username"));
                        edtEmail.setText(documentSnapshot.getString("email"));
                        edtPhone.setText(documentSnapshot.getString("phone"));

                        Long petCount = documentSnapshot.getLong("petCount");
                        edtPetCount.setText(petCount != null ? String.valueOf(petCount) : "0");

                        // ===== LOAD AVATAR BASE64 =====
                        String base64Image = documentSnapshot.getString("avatarBase64");

                        if (base64Image != null && !base64Image.isEmpty()) {
                            Bitmap bitmap = decodeBase64(base64Image);
                            imgAvatar.setImageBitmap(bitmap);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ================= ENABLE/DISABLE EDIT =================
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

        if (username.isEmpty()) {
            edtUsername.setError("Không được để trống");
            return;
        }

        db.collection("users").document(currentUserId)
                .update(
                        "username", username,
                        "phone", phone
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                    isEditing = false;
                    setEditable(false);
                    btnEdit.setText("Sửa");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ================= MỞ GALLERY =================
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    // ================= CHỌN ẢNH =================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            // preview
            imgAvatar.setImageURI(imageUri);

            // convert + lưu luôn
            String base64 = encodeImage(imageUri);
            if (base64 != null) {
                saveAvatar(base64);
            }
        }
    }

    // ================= ENCODE BASE64 =================
    private String encodeImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // resize
            bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);

            byte[] bytes = baos.toByteArray();

            return Base64.encodeToString(bytes, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================= SAVE AVATAR =================
    private void saveAvatar(String base64) {
        db.collection("users").document(currentUserId)
                .update("avatarBase64", base64)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Cập nhật avatar", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ================= DECODE BASE64 =================
    private Bitmap decodeBase64(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
