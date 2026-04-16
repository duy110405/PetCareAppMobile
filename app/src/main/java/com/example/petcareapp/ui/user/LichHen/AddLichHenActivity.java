package com.example.petcareapp.ui.user.LichHen;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.ChiNhanh;
import com.example.petcareapp.data.model.LichHen;
import com.example.petcareapp.data.model.Pet;
import com.example.petcareapp.ui.user.Pet.PetViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.petcareapp.ui.user.UChiNhanhViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddLichHenActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerPet, spinnerBranch;
    private MaterialCardView cardDichVu1, cardDichVu2, cardDichVu3;
    private TextView tvTotalPrice;
    private TextInputEditText edtNotes;
    private MaterialButton btnCancel, btnAddAppointmentSubmit;
    private ImageView btnBack;

    private List<Pet> petList = new ArrayList<>();
    private List<ChiNhanh> branchList = new ArrayList<>();

    // Biến lưu trữ lựa chọn của người dùng
    private Pet selectedPet = null;
    private ChiNhanh selectedBranch = null;
    private String selectedService = "";
    private int totalPrice = 0;
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_them_lich_hen);

        // 1. Ánh xạ View
        spinnerPet = findViewById(R.id.spinnerPet);
        spinnerBranch = findViewById(R.id.spinnerBranch);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        edtNotes = findViewById(R.id.edtNotes);
        btnCancel = findViewById(R.id.btnCancel);
        btnAddAppointmentSubmit = findViewById(R.id.btnAddAppointmentSubmit);
        btnBack = findViewById(R.id.btnBack);

        cardDichVu1 = findViewById(R.id.cardDichVu1);
        cardDichVu2 = findViewById(R.id.cardDichVu2);
        cardDichVu3 = findViewById(R.id.cardDichVu3);

        // 2. Setup sự kiện
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnAddAppointmentSubmit.setOnClickListener(v -> thucHienDatLich());

        setupDichVu();
        setupGioHen();
        loadDataForSpinners();
    }

    private void loadDataForSpinners() {
        String userId = FirebaseAuth.getInstance().getUid();

        // Load Thú cưng
        PetViewModel petViewModel = new ViewModelProvider(this).get(PetViewModel.class);
        if (userId != null) {
            petViewModel.loadPets(userId);
            petViewModel.getPets().observe(this, pets -> {
                petList = pets;
                List<String> names = new ArrayList<>();
                for (Pet p : pets) names.add(p.getName());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                spinnerPet.setAdapter(adapter);
                spinnerPet.setOnItemClickListener((parent, view, position, id) -> selectedPet = petList.get(position));
            });
        }

        // Load Chi nhánh
        // Sửa lại đoạn này trong AddLichHenActivity.java
        UChiNhanhViewModel branchViewModel = new ViewModelProvider(this).get(UChiNhanhViewModel.class);

// Thay vì gọi loadChiNhanh() (không tồn tại), hãy gọi hàm bạn đã viết:
        branchViewModel.getDanhSachChiNhanh().observe(this, branches -> {
            if (branches != null) {
                this.branchList = branches;
                List<String> names = new ArrayList<>();
                for (ChiNhanh c : branches) {
                    names.add(c.getTenChiNhanh());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, names);
                spinnerBranch.setAdapter(adapter);

                spinnerBranch.setOnItemClickListener((parent, view, position, id) -> {
                    selectedBranch = branchList.get(position);
                });
            }
        });
    }

    private void setupDichVu() {
        // Logic chọn dịch vụ và cập nhật giá tiền
        cardDichVu1.setOnClickListener(v -> selectService(cardDichVu1, "Khám tổng quát", 200000));
        cardDichVu2.setOnClickListener(v -> selectService(cardDichVu2, "Tiêm phòng", 500000));
        cardDichVu3.setOnClickListener(v -> selectService(cardDichVu3, "Spa", 200000));
    }

    private void selectService(MaterialCardView selectedCard, String serviceName, int price) {
        // Reset màu tất cả các thẻ
        cardDichVu1.setStrokeColor(Color.parseColor("#E0E0E0"));
        cardDichVu2.setStrokeColor(Color.parseColor("#E0E0E0"));
        cardDichVu3.setStrokeColor(Color.parseColor("#E0E0E0"));

        // Highlight thẻ được chọn
        selectedCard.setStrokeColor(Color.parseColor("#2E64FE")); // Màu xanh dương
        selectedCard.setStrokeWidth(3);

        selectedService = serviceName;
        totalPrice = price;
        tvTotalPrice.setText("Giá : " + String.format("%,d đ", totalPrice));
    }

    private void setupGioHen() {
        // Gán mảng ID của các nút giờ (Bạn cần đảm bảo đã thêm ID này vào XML)
        int[] timeButtonIds = {R.id.btnTime8, R.id.btnTime9, R.id.btnTime10, R.id.btnTime11,
                R.id.btnTime12, R.id.btnTime13, R.id.btnTime14, R.id.btnTime15,
                R.id.btnTime16, R.id.btnTime17, R.id.btnTime18, R.id.btnTime19};

        List<MaterialButton> timeButtons = new ArrayList<>();

        for (int id : timeButtonIds) {
            MaterialButton btn = findViewById(id);
            if(btn != null) {
                timeButtons.add(btn);
                btn.setOnClickListener(v -> {
                    // Đổi màu tất cả nút về mặc định
                    for (MaterialButton b : timeButtons) {
                        b.setBackgroundColor(Color.TRANSPARENT);
                        b.setTextColor(Color.parseColor("#333333"));
                    }
                    // Đổi màu nút được chọn
                    btn.setBackgroundColor(Color.parseColor("#66BB6A")); // Màu xanh lá
                    btn.setTextColor(Color.WHITE);
                    selectedTime = btn.getText().toString();
                });
            }
        }
    }

    private void thucHienDatLich() {
        if (selectedPet == null || selectedBranch == null || selectedService.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ thú cưng, dịch vụ, chi nhánh và giờ!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();
        String lichHenId = db.collection("LichHen").document().getId();

        // Xử lý tạo Timestamp. Tạm thời sử dụng ngày hôm nay + giờ được chọn
        Calendar calendar = Calendar.getInstance();
        try {
            String[] timeParts = selectedTime.replace(" ", "").split(":");
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tạo object LichHen
        LichHen lichHen = new LichHen(
                lichHenId,
                userId,
                selectedPet.getId(),
                selectedBranch.getId(),
                new Timestamp(calendar.getTime()),
                selectedService + " - " + edtNotes.getText().toString(), // Gộp tên dịch vụ và ghi chú
                "Chờ xác nhận",
                selectedPet.getName(),
                selectedBranch.getTenChiNhanh()
        );

        // Lưu vào Firestore
        db.collection("LichHen").document(lichHenId).set(lichHen)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đặt lịch thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}