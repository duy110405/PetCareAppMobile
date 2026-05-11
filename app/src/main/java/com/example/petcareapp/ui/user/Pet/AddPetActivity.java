package com.example.petcareapp.ui.user.Pet;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.Pet;
import com.example.petcareapp.ui.user.TrangChu.UTrangChuActivity;
import com.example.petcareapp.ui.user.UCaiDatActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddPetActivity extends AppCompatActivity {

    private EditText edtName, edtBreed, edtDob, edtWeight, edtColor;

    private Button btnSubmit,
            btnCancel,
            btnUploadPhoto,
            btnCapturePhoto;

    private ImageView imgAvatar, btnBack;

    private Uri imageUri;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_them_pet);

        db = FirebaseFirestore.getInstance();

        // ===================== MAPPING =====================
        edtName = findViewById(R.id.edtPetName);
        edtBreed = findViewById(R.id.edtPetBreed);
        edtDob = findViewById(R.id.edtPetDob);
        edtWeight = findViewById(R.id.edtPetWeight);
        edtColor = findViewById(R.id.edtPetColor);

        btnSubmit = findViewById(R.id.btnAddPetSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto);

        imgAvatar = findViewById(R.id.imgAvatar);

        btnBack = findViewById(R.id.btnBack);

        // ===================== CAMERA PERMISSION =====================
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    100
            );
        }

        // ===================== BACK =====================
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, UTrangChuActivity.class));
        });

        // ===================== GALLERY =====================
        btnUploadPhoto.setOnClickListener(v ->
                galleryLauncher.launch("image/*"));

        // ===================== CAMERA =====================
        btnCapturePhoto.setOnClickListener(v ->
                cameraLauncher.launch(null));

        // ===================== DATE PICKER =====================
        edtDob.setOnClickListener(v -> showDatePicker());

        // ===================== BUTTON =====================
        btnSubmit.setOnClickListener(v -> addPet());

        btnCancel.setOnClickListener(v -> finish());
    }

    // ===================== GALLERY =====================
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {

                            imageUri = uri;

                            imgAvatar.setImageURI(uri);
                        }
                    });

    // ===================== CAMERA =====================
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    bitmap -> {

                        if (bitmap != null) {

                            imgAvatar.setImageBitmap(bitmap);

                            String path =
                                    MediaStore.Images.Media.insertImage(
                                            getContentResolver(),
                                            bitmap,
                                            "PetImage",
                                            null
                                    );

                            imageUri = Uri.parse(path);
                        }
                    });

    // ===================== DATE PICKER =====================
    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) ->
                        edtDob.setText(
                                d + "/" + (m + 1) + "/" + y
                        ),

                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker()
                .setMaxDate(System.currentTimeMillis());

        dialog.show();
    }

    // ===================== COMPRESS IMAGE =====================
    private String compressAndEncodeImage(Uri uri) {

        try {

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(),
                    uri
            );

            // resize nhỏ lại
            int maxWidth = 300;

            int maxHeight = (int)
                    ((double) bitmap.getHeight()
                            / bitmap.getWidth()
                            * maxWidth);

            Bitmap resized = Bitmap.createScaledBitmap(
                    bitmap,
                    maxWidth,
                    maxHeight,
                    true
            );

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            // compress 60%
            resized.compress(
                    Bitmap.CompressFormat.JPEG,
                    60,
                    baos
            );

            byte[] bytes = baos.toByteArray();

            // check size
            if (bytes.length > 900000) {

                Toast.makeText(
                        this,
                        "Ảnh quá lớn",
                        Toast.LENGTH_SHORT
                ).show();

                return null;
            }

            return Base64.encodeToString(
                    bytes,
                    Base64.DEFAULT
            );

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }

    // ===================== ADD PET =====================
    private void addPet() {

        String name =
                edtName.getText().toString().trim();

        String breed =
                edtBreed.getText().toString().trim();

        String weightStr =
                edtWeight.getText().toString().trim();

        String color =
                edtColor.getText().toString().trim();

        String dobStr =
                edtDob.getText().toString().trim();

        // ===================== VALIDATE =====================

        if (TextUtils.isEmpty(name)) {

            edtName.setError("Nhập tên thú cưng");

            return;
        }

        if (TextUtils.isEmpty(breed)) {

            edtBreed.setError("Nhập giống");

            return;
        }

        if (TextUtils.isEmpty(dobStr)) {

            edtDob.setError("Chọn ngày sinh");

            return;
        }

        if (imageUri == null) {

            Toast.makeText(
                    this,
                    "Chọn ảnh",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ===================== DATE =====================
        Timestamp dob;

        try {

            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                    );

            Date date = sdf.parse(dobStr);

            if (date.after(new Date())) {

                Toast.makeText(
                        this,
                        "Ngày sinh không hợp lệ",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            dob = new Timestamp(date);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Ngày lỗi",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // ===================== WEIGHT =====================
        double weight = 0;

        try {

            if (!TextUtils.isEmpty(weightStr)) {

                weight =
                        Double.parseDouble(weightStr);
            }

        } catch (Exception e) {

            edtWeight.setError("Sai cân nặng");

            return;
        }

        // ===================== IMAGE =====================
        String base64Image =
                compressAndEncodeImage(imageUri);

        if (base64Image == null) return;

        // ===================== USER =====================
        String userId =
                FirebaseAuth.getInstance().getUid();

        if (userId == null) return;

        // ===================== PET ID =====================
        String petId =
                db.collection("users")
                        .document(userId)
                        .collection("pets")
                        .document()
                        .getId();

        // ===================== CREATE PET =====================
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

        // ===================== SAVE FIRESTORE =====================
        db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .set(pet)

                .addOnSuccessListener(unused -> {

                    db.collection("users")
                            .document(userId)
                            .update(
                                    "petCount",
                                    FieldValue.increment(1)
                            );

                    Toast.makeText(
                            this,
                            "Thêm thành công",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })

                .addOnFailureListener(e ->

                        Toast.makeText(
                                this,
                                "Lỗi: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}