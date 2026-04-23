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
import com.example.petcareapp.data.model.DichVu;
import com.example.petcareapp.data.model.LichHen;
import com.example.petcareapp.data.model.Pet;
import com.example.petcareapp.ui.user.Pet.PetViewModel;
import com.example.petcareapp.ui.user.TimPhong.UChiNhanhViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private Pet selectedPet = null;
    private ChiNhanh selectedBranch = null;

    private final List<DichVu> selectedServices = new ArrayList<>();

    private int totalPrice = 0;
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_them_lich_hen);

        initViews();
        setupEvents();
        setupDichVu();
        setupGioHen();
        loadDataForSpinners();
    }

    private void initViews() {
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
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnAddAppointmentSubmit.setOnClickListener(v -> thucHienDatLich());
    }

    private void loadDataForSpinners() {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
            PetViewModel petViewModel =
                    new ViewModelProvider(this).get(PetViewModel.class);

            petViewModel.loadPets(userId);

            petViewModel.getPets().observe(this, pets -> {
                petList = pets;

                List<String> names = new ArrayList<>();
                for (Pet p : pets) {
                    names.add(p.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        names
                );

                spinnerPet.setAdapter(adapter);

                spinnerPet.setOnItemClickListener(
                        (parent, view, position, id) ->
                                selectedPet = petList.get(position)
                );
            });
        }

        UChiNhanhViewModel branchViewModel =
                new ViewModelProvider(this).get(UChiNhanhViewModel.class);

        branchViewModel.getDanhSachChiNhanh().observe(this, branches -> {
            if (branches == null) return;

            branchList = branches;

            List<String> names = new ArrayList<>();
            for (ChiNhanh c : branches) {
                names.add(c.getTenChiNhanh());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    names
            );

            spinnerBranch.setAdapter(adapter);

            spinnerBranch.setOnItemClickListener(
                    (parent, view, position, id) ->
                            selectedBranch = branchList.get(position)
            );
        });
    }

    private void setupDichVu() {
        setupServiceCard(cardDichVu1, "Khám tổng quát", 200000);
        setupServiceCard(cardDichVu2, "Tiêm phòng", 500000);
        setupServiceCard(cardDichVu3, "Spa", 200000);
    }

    private void setupServiceCard(
            MaterialCardView card,
            String ten,
            int gia
    ) {
        card.setOnClickListener(v -> {
            boolean exists = false;

            for (DichVu dv : selectedServices) {
                if (dv.getTen().equals(ten)) {
                    exists = true;
                    break;
                }
            }

            if (exists) {
                removeService(ten);
                card.setStrokeColor(Color.parseColor("#E0E0E0"));
                card.setStrokeWidth(1);
            } else {
                selectedServices.add(new DichVu(ten, gia));
                card.setStrokeColor(Color.parseColor("#2E64FE"));
                card.setStrokeWidth(3);
            }

            calculateTotalPrice();
        });
    }

    private void removeService(String ten) {
        for (int i = 0; i < selectedServices.size(); i++) {
            if (selectedServices.get(i).getTen().equals(ten)) {
                selectedServices.remove(i);
                break;
            }
        }
    }

    private void calculateTotalPrice() {
        totalPrice = 0;

        for (DichVu dv : selectedServices) {
            totalPrice += dv.getGia();
        }

        tvTotalPrice.setText(
                "Giá : " + String.format("%,d đ", totalPrice)
        );
    }

    private void setupGioHen() {
        int[] timeButtonIds = {
                R.id.btnTime8,
                R.id.btnTime9,
                R.id.btnTime10,
                R.id.btnTime11,
                R.id.btnTime12,
                R.id.btnTime13,
                R.id.btnTime14,
                R.id.btnTime15,
                R.id.btnTime16,
                R.id.btnTime17,
                R.id.btnTime18,
                R.id.btnTime19
        };

        List<MaterialButton> timeButtons = new ArrayList<>();

        for (int id : timeButtonIds) {
            MaterialButton btn = findViewById(id);

            if (btn != null) {
                timeButtons.add(btn);

                btn.setOnClickListener(v -> {
                    for (MaterialButton b : timeButtons) {
                        b.setBackgroundColor(Color.TRANSPARENT);
                        b.setTextColor(Color.parseColor("#333333"));
                    }

                    btn.setBackgroundColor(
                            Color.parseColor("#66BB6A")
                    );
                    btn.setTextColor(Color.WHITE);

                    selectedTime = btn.getText().toString();
                });
            }
        }
    }

    private void thucHienDatLich() {
        if (selectedPet == null
                || selectedBranch == null
                || selectedServices.isEmpty()
                || selectedTime.isEmpty()) {

            Toast.makeText(
                    this,
                    "Vui lòng chọn đầy đủ thông tin!",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Toast.makeText(
                    this,
                    "Người dùng chưa đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String lichHenId =
                db.collection("LichHen").document().getId();

        Calendar calendar = Calendar.getInstance();

        try {
            String[] timeParts =
                    selectedTime.replace(" ", "").split(":");

            calendar.set(
                    Calendar.HOUR_OF_DAY,
                    Integer.parseInt(timeParts[0])
            );

            calendar.set(
                    Calendar.MINUTE,
                    Integer.parseInt(timeParts[1])
            );

            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Lỗi định dạng giờ",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        LichHen lichHen = new LichHen();

        lichHen.setId(lichHenId);
        lichHen.setUserId(userId);

        lichHen.setPetId(selectedPet.getId());
        lichHen.setChiNhanhId(selectedBranch.getId());

        lichHen.setThoiGianHen(
                new Timestamp(calendar.getTime())
        );

        lichHen.setDanhSachDichVu(selectedServices);

        lichHen.setTongTien(totalPrice);

        lichHen.setGhiChu(
                edtNotes.getText().toString().trim()
        );

        lichHen.setLyDoTuChoi("");

        lichHen.setTrangThai("Chờ duyệt");

        lichHen.setTenThuCung(
                selectedPet.getName()
        );

        lichHen.setTenChiNhanh(
                selectedBranch.getTenChiNhanh()
        );

        db.collection("LichHen")
                .document(lichHenId)
                .set(lichHen)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            this,
                            "Đặt lịch thành công!",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Lỗi: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}