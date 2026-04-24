package com.example.petcareapp.ui.user.Pet;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.utils.MenuUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PetDetailActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private TextView tvAppointmentHistory;
    private View cvPetAppointment;
    private TextView tvApptName, tvApptStatus, tvApptDesc, tvApptTime;
    private EditText edtName, edtBreed, edtDob, edtWeight, edtColor;
    private MaterialButton btnDelete;

    private FirebaseFirestore db;
    private String userId, petId;

    private Button btnCapture, btnUpload, btnEditPet, btnAddAlarm;

    private LinearLayout alarmContainer;
    private Uri imageUri;

    private boolean isEditing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_chi_tiet_pet);

        alarmContainer = findViewById(R.id.alarmContainer);
        btnAddAlarm = findViewById(R.id.btnAddAlarm);

        //  init firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        //  lấy petId từ intent
        petId = getIntent().getStringExtra("petId");

        //  mapping view
        imgAvatar = findViewById(R.id.imgAvatar);

        btnEditPet = findViewById(R.id.btnEditPet);

        btnCapture = findViewById(R.id.btnCapture);
        btnUpload = findViewById(R.id.btnUpload);

        edtName = findViewById(R.id.edtName);
        edtBreed = findViewById(R.id.edtBreed);
        edtDob = findViewById(R.id.edtDob);
        edtWeight = findViewById(R.id.edtWeight);
        edtColor = findViewById(R.id.edtColor);

        // ÁNH XẠ LỊCH HẸN
        cvPetAppointment = findViewById(R.id.cvPetAppointment);
        tvApptName = findViewById(R.id.tvApptName);
        tvApptStatus = findViewById(R.id.tvApptStatus);
        tvApptDesc = findViewById(R.id.tvApptDesc);
        tvApptTime = findViewById(R.id.tvApptTime);

        // Ẩn đi mặc định, nếu có lịch mới hiện lên
        cvPetAppointment.setVisibility(View.GONE);// Ánh xạ nút Lịch sử hẹn
        tvAppointmentHistory = findViewById(R.id.tvAppointmentHistory);
        tvAppointmentHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, PetLichSuHenActivity.class);
            intent.putExtra("petId", petId);
            startActivity(intent);
        });

        // GỌI HÀM NÀY ĐỂ HIỂN THỊ LỊCH HẸN RA MÀN HÌNH
        loadPetAppointment();


        btnDelete = findViewById(R.id.btnDeletePet);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        MenuUser.setup(this, bottomNav);
        // ===================== CHỌN ẢNH TỪ GALLERY =====================
        btnUpload.setOnClickListener(v -> {
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
        btnCapture.setOnClickListener(v -> {
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

        //  load data
        loadPet();

        //  delete pet
        btnDelete.setOnClickListener(v -> deletePet());

        edtDob.setOnClickListener(v -> {
            if (!isEditing) return;

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        String date = d + "/" + (m + 1) + "/" + y;
                        edtDob.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            // chặn tương lai
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());

            dialog.show();
        });


        btnEditPet.setOnClickListener(v -> {
            if (!isEditing) {
                //  CHUYỂN SANG EDIT MODE
                enableEdit(true);
                btnEditPet.setText("Lưu");
                isEditing = true;

            } else {
                //  LƯU DỮ LIỆU
                updatePetInfo();
                enableEdit(false);
                btnEditPet.setText("Sửa");
                isEditing = false;
            }
        });

        btnAddAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(this, ThemChuongBaoActivity.class);
            intent.putExtra("petId", petId);
            startActivity(intent);
        });

        listenAlarms();

    }



    // ===================== CAMERA RESULT =====================
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {

                    Bitmap bitmap = uriToBitmap(imageUri);

                    if (bitmap != null) {
                        imgAvatar.setImageBitmap(bitmap);
                        updatePetImage(bitmap); //  quan trọng
                    }
                }
            });


    // ===================== GALLERY RESULT =====================
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {

                    imageUri = uri;

                    Bitmap bitmap = uriToBitmap(uri);

                    if (bitmap != null) {
                        imgAvatar.setImageBitmap(bitmap);
                        updatePetImage(bitmap); // 🔥 quan trọng
                    }
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

    private Bitmap uriToBitmap(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
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


    // ===============================
    // LOAD PET FROM FIRESTORE
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

                        //  set data
                        edtName.setText(name);
                        edtBreed.setText(breed);
                        edtColor.setText(color);
                        edtWeight.setText(weight != null ? String.valueOf(weight) +" kg" : "");


                        if (dob != null) {
                            edtDob.setText(formatDate(dob) + " (" + calculateAge(dob) + " tuổi)");
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
    //  FORMAT DATE
    // ===============================
    private String formatDate(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    // ===============================
    //  TÍNH TUỔI
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
    //  DELETE PET
    // ===============================
    private void deletePet() {
        if (userId == null || petId == null) return;

        db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .delete()
                .addOnSuccessListener(unused -> {

                    // 🔥 GIẢM PET COUNT
                    db.collection("users")
                            .document(userId)
                            .update("petCount", com.google.firebase.firestore.FieldValue.increment(-1));

                    Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePetImage(Bitmap bitmap) {

        String base64 = encodeToBase64(bitmap);

        Map<String, Object> map = new HashMap<>();
        map.put("imageBase64", base64);

        db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .update(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã cập nhật ảnh", Toast.LENGTH_SHORT).show();
                });
    }


    private void enableEdit(boolean enable) {

        edtName.setEnabled(enable);
        edtBreed.setEnabled(enable);
        edtDob.setEnabled(enable);
        edtWeight.setEnabled(enable);
        edtColor.setEnabled(enable);
    }

    private void updatePetInfo() {

        String name = edtName.getText().toString().trim();
        String breed = edtBreed.getText().toString().trim();
        String color = edtColor.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim();
        String dobStr = edtDob.getText().toString().trim();

        double weight = 0;
        try {
            weight = Double.parseDouble(weightStr);
        } catch (Exception e) {
            edtWeight.setError("Sai cân nặng");
            return;
        }

        Timestamp dob;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(dobStr);
            dob = new Timestamp(date);
        } catch (Exception e) {
            Toast.makeText(this, "Ngày không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("breed", breed);
        updates.put("color", color);
        updates.put("weight", weight);
        updates.put("dob", dob);

        db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    enableEdit(false);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void listenAlarms() {

        db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .collection("alarms")
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    alarmContainer.removeAllViews();

                    for (var doc : value.getDocuments()) {

                        String id = doc.getString("id");
                        String name = doc.getString("name");
                        String time = doc.getString("time");
                        String type = doc.getString("type");

                        addAlarmView(id, name, time, type);
                    }
                });
    }

    private void addAlarmView(String id, String name, String time, String type) {

        View view = getLayoutInflater().inflate(R.layout.item_alarm, null);

        TextView tvName = view.findViewById(R.id.tvAlarmName);
        TextView tvTime = view.findViewById(R.id.tvAlarmTime);
        ImageView btnDelete = view.findViewById(R.id.btnDeleteAlarm);

        tvName.setText(name);
        tvTime.setText(type + " • " + time);

        btnDelete.setOnClickListener(v -> {
            db.collection("users")
                    .document(userId)
                    .collection("pets")
                    .document(petId)
                    .collection("alarms")
                    .document(id)
                    .delete();
        });

        alarmContainer.addView(view);


    }
    private void loadPetAppointment() {
        if (petId == null) return;

        db.collection("LichHen")
                .whereEqualTo("petId", petId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        cvPetAppointment.setVisibility(View.GONE);
                        return;
                    }

                    // Đưa vào List để sắp xếp
                    java.util.List<com.example.petcareapp.data.model.LichHen> listAppt = new java.util.ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        com.example.petcareapp.data.model.LichHen appt = doc.toObject(com.example.petcareapp.data.model.LichHen.class);
                        if (appt != null) listAppt.add(appt);
                    }

                    if (listAppt.isEmpty()) return;

                    // Sắp xếp giảm dần theo thời gian (Lấy cái mới nhất lên đầu)
                    java.util.Collections.sort(listAppt, (o1, o2) -> {
                        if (o1.getThoiGianHen() == null || o2.getThoiGianHen() == null) return 0;
                        return o2.getThoiGianHen().toDate().compareTo(o1.getThoiGianHen().toDate());
                    });

                    // Lấy phần tử đầu tiên (Mới nhất)
                    com.example.petcareapp.data.model.LichHen latestAppt = listAppt.get(0);

                    cvPetAppointment.setVisibility(View.VISIBLE);
                    tvApptName.setText(latestAppt.getTenThuCung());
                    tvApptStatus.setText(latestAppt.getTrangThai());

                    // Nối tên dịch vụ
                    StringBuilder dvText = new StringBuilder();
                    if (latestAppt.getDanhSachDichVu() != null) {
                        for (com.example.petcareapp.data.model.DichVu dv : latestAppt.getDanhSachDichVu()) {
                            dvText.append(dv.getTenDichVu()).append(", ");
                        }
                        if (dvText.length() > 0) dvText.setLength(dvText.length() - 2);
                    }
                    tvApptDesc.setText(dvText.toString().isEmpty() ? "Không có dịch vụ" : dvText.toString());

                    if (latestAppt.getThoiGianHen() != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy", java.util.Locale.getDefault());
                        tvApptTime.setText(sdf.format(latestAppt.getThoiGianHen().toDate()));
                    }
                })
                .addOnFailureListener(e -> {
                    cvPetAppointment.setVisibility(View.GONE);
                });
    }
}
