package com.example.petcareapp.ui.user.LichHen;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddLichHenActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerPet, spinnerBranch;
    private RecyclerView rcvServices, rcvTimeSlots;
    private TextView tvTotalPrice;
    private TextInputEditText edtNotes, edtSelectDate;
    private MaterialButton btnCancel, btnAddAppointmentSubmit;
    private ImageView btnBack;

    private List<Pet> petList = new ArrayList<>();
    private List<ChiNhanh> branchList = new ArrayList<>();

    private Pet selectedPet = null;
    private ChiNhanh selectedBranch = null;

    private final List<DichVu> allServices = new ArrayList<>();
    private final List<DichVu> selectedServices = new ArrayList<>();

    private int totalPrice = 0;
    private String selectedTime = "";
    private Calendar selectedDate = Calendar.getInstance();

    private ServiceAdapter serviceAdapter;
    private TimeSlotAdapter timeSlotAdapter;

    private static final int MAX_SLOT = 3;

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
        rcvServices = findViewById(R.id.rcvServices);
        rcvTimeSlots = findViewById(R.id.rcvTimeSlots);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        edtNotes = findViewById(R.id.edtNotes);
        edtSelectDate = findViewById(R.id.edtSelectDate);
        btnCancel = findViewById(R.id.btnCancel);
        btnAddAppointmentSubmit = findViewById(R.id.btnAddAppointmentSubmit);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        // Mở DatePicker khi nhấn vào Text Input
        edtSelectDate.setOnClickListener(v -> showDatePicker());

        btnAddAppointmentSubmit.setOnClickListener(v -> thucHienDatLich());
    }

    private void showDatePicker() {
        if (selectedBranch == null) {
            Toast.makeText(this, "Vui lòng chọn chi nhánh trước", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    String text = String.format("%02d/%02d/%d", dayOfMonth, (month + 1), year);
                    edtSelectDate.setText(text);

                    // Reset selected time
                    selectedTime = "";
                    loadAvailableTimeSlots();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void setupDichVu() {
        allServices.add(new DichVu("Khám tổng quát (khám sức khỏe định kỳ)", 200000));
        allServices.add(new DichVu("Tiêm phòng (phòng dại, truyền nhiễm)", 500000));
        allServices.add(new DichVu("Spa (tắm, cắt tỉa lông, làm đẹp)", 200000));

        rcvServices.setLayoutManager(new LinearLayoutManager(this));
        serviceAdapter = new ServiceAdapter();
        rcvServices.setAdapter(serviceAdapter);
    }

    private void setupGioHen() {
        List<String> times = new ArrayList<>();
        for (int i = 8; i <= 19; i++) {
            times.add(i + ":00");
        }

        rcvTimeSlots.setLayoutManager(new GridLayoutManager(this, 4)); // 4 cột
        timeSlotAdapter = new TimeSlotAdapter(times);
        rcvTimeSlots.setAdapter(timeSlotAdapter);
    }

    private void loadAvailableTimeSlots() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Calendar start = (Calendar) selectedDate.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 1);

        db.collection("LichHen")
                .whereEqualTo("chiNhanhId", selectedBranch.getId())
                .whereGreaterThanOrEqualTo("thoiGianHen", new Timestamp(start.getTime()))
                .whereLessThan("thoiGianHen", new Timestamp(end.getTime()))
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<Integer, Integer> slotCount = new HashMap<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        LichHen lh = doc.toObject(LichHen.class);
                        if (lh != null && !lh.getTrangThai().equals("Đã hủy")) {
                            int hour = lh.getThoiGianHen().toDate().getHours();
                            slotCount.put(hour, slotCount.getOrDefault(hour, 0) + 1);
                        }
                    }
                    timeSlotAdapter.updateAvailability(slotCount);
                });
    }

    private void calculateTotalPrice() {
        totalPrice = 0;
        for (DichVu dv : selectedServices) {
            totalPrice += dv.getGia();
        }
        tvTotalPrice.setText(String.format("%,d đ", totalPrice));
    }

    private void loadDataForSpinners() {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
            PetViewModel petViewModel = new ViewModelProvider(this).get(PetViewModel.class);
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

        UChiNhanhViewModel branchViewModel = new ViewModelProvider(this).get(UChiNhanhViewModel.class);
        branchViewModel.getDanhSachChiNhanh().observe(this, branches -> {
            if (branches == null) return;
            branchList = branches;
            List<String> names = new ArrayList<>();
            for (ChiNhanh c : branches) names.add(c.getTenChiNhanh());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
            spinnerBranch.setAdapter(adapter);
            spinnerBranch.setOnItemClickListener((parent, view, position, id) -> {
                selectedBranch = branchList.get(position);
                edtSelectDate.setText(""); // Xóa ngày cũ khi đổi chi nhánh
                selectedTime = "";
                timeSlotAdapter.resetSelection();
            });
        });
    }

    private void thucHienDatLich() {
        if (selectedPet == null || selectedBranch == null || selectedServices.isEmpty() || selectedTime.isEmpty() || edtSelectDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            String tenChu = documentSnapshot.exists() ? documentSnapshot.getString("hoTen") : "Khách hàng";
            String sdt = documentSnapshot.exists() ? documentSnapshot.getString("soDienThoai") : "Chưa cập nhật";

            String lichHenId = db.collection("LichHen").document().getId();
            Calendar calendar = (Calendar) selectedDate.clone();

            try {
                String[] timeParts = selectedTime.replace(" ", "").split(":");
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi định dạng giờ", Toast.LENGTH_SHORT).show();
                return;
            }

            LichHen lichHen = new LichHen();
            lichHen.setId(lichHenId);
            lichHen.setUserId(userId);
            lichHen.setPetId(selectedPet.getId());
            lichHen.setChiNhanhId(selectedBranch.getId());
            lichHen.setThoiGianHen(new Timestamp(calendar.getTime()));
            lichHen.setDanhSachDichVu(selectedServices);
            lichHen.setTongTien(totalPrice);
            lichHen.setGhiChu(edtNotes.getText().toString().trim());
            lichHen.setLyDoTuChoi("");
            lichHen.setTrangThai("Chờ duyệt");
            lichHen.setTenThuCung(selectedPet.getName());
            lichHen.setTenChiNhanh(selectedBranch.getTenChiNhanh());
            lichHen.setTenChuThuCung(tenChu);
            lichHen.setSoDienThoai(sdt);

            db.collection("LichHen").document(lichHenId).set(lichHen)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đặt lịch thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    // ================= ADAPTERS INNER CLASSES =================

    // 1. Adapter cho Dịch vụ
    private class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_select, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DichVu dv = allServices.get(position);
            holder.tvServiceName.setText(dv.getTen());
            holder.tvServicePrice.setText(String.format("%,d đ", dv.getGia()));

            boolean isSelected = selectedServices.contains(dv);
            holder.chkService.setChecked(isSelected);
            holder.cardService.setStrokeColor(isSelected ? Color.parseColor("#2E64FE") : Color.parseColor("#E0E0E0"));
            holder.cardService.setStrokeWidth(isSelected ? 3 : 1);

            holder.cardService.setOnClickListener(v -> {
                if (isSelected) {
                    selectedServices.remove(dv);
                } else {
                    selectedServices.add(dv);
                }
                calculateTotalPrice();
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() { return allServices.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardService;
            CheckBox chkService;
            TextView tvServiceName, tvServicePrice;
            ViewHolder(View v) {
                super(v);
                cardService = v.findViewById(R.id.cardService);
                chkService = v.findViewById(R.id.chkService);
                tvServiceName = v.findViewById(R.id.tvServiceName);
                tvServicePrice = v.findViewById(R.id.tvServicePrice);
            }
        }
    }

    // 2. Adapter cho Giờ hẹn
    private class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
        private final List<String> times;
        private Map<Integer, Integer> slotCount = new HashMap<>();

        TimeSlotAdapter(List<String> times) { this.times = times; }

        void updateAvailability(Map<Integer, Integer> counts) {
            this.slotCount = counts;
            notifyDataSetChanged();
        }

        void resetSelection() {
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String time = times.get(position);
            holder.tvTime.setText(time);

            int hour = Integer.parseInt(time.split(":")[0]);
            int count = slotCount.getOrDefault(hour, 0);
            boolean isFull = count >= MAX_SLOT;
            boolean isSelected = time.equals(selectedTime);

            if (isFull) {
                holder.cardTimeSlot.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
                holder.tvTime.setTextColor(Color.parseColor("#BDBDBD"));
                holder.cardTimeSlot.setStrokeColor(Color.TRANSPARENT);
                holder.itemView.setEnabled(false);
            } else if (isSelected) {
                holder.cardTimeSlot.setCardBackgroundColor(Color.parseColor("#2E64FE"));
                holder.tvTime.setTextColor(Color.WHITE);
                holder.cardTimeSlot.setStrokeColor(Color.TRANSPARENT);
                holder.itemView.setEnabled(true);
            } else {
                holder.cardTimeSlot.setCardBackgroundColor(Color.TRANSPARENT);
                holder.tvTime.setTextColor(Color.parseColor("#333333"));
                holder.cardTimeSlot.setStrokeColor(Color.parseColor("#E0E0E0"));
                holder.itemView.setEnabled(true);
            }

            holder.itemView.setOnClickListener(v -> {
                if (!isFull) {
                    selectedTime = time;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() { return times.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardTimeSlot;
            TextView tvTime;
            ViewHolder(View v) {
                super(v);
                cardTimeSlot = v.findViewById(R.id.cardTimeSlot);
                tvTime = v.findViewById(R.id.tvTime);
            }
        }
    }
}