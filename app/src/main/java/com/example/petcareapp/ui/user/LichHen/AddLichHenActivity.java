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
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddLichHenActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerPet, spinnerBranch, spinnerVoucher;
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

    private List<com.example.petcareapp.data.model.KhoVoucher> userKhoVoucherList = new ArrayList<>();
    private List<com.example.petcareapp.data.model.Voucher> userVoucherDetails = new ArrayList<>();

    private com.example.petcareapp.data.model.KhoVoucher selectedKhoVoucher = null;
    private com.example.petcareapp.data.model.Voucher selectedVoucherDetail = null;


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
        spinnerVoucher = findViewById(R.id.spinnerVoucher);
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
        // Cài đặt RecyclerView trước
        rcvServices.setLayoutManager(new LinearLayoutManager(this));
        serviceAdapter = new ServiceAdapter();
        rcvServices.setAdapter(serviceAdapter);

        // Gọi hàm tải dữ liệu từ Firebase
        loadDichVuTuFirebase();
    }

    private void loadDichVuTuFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Truy vấn vào bảng "DichVu" (bảng mà Admin đã thêm)
        db.collection("DichVu")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allServices.clear(); // Xóa list cũ cho chắc

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        DichVu dv = doc.toObject(DichVu.class);
                        if (dv != null) {
                            dv.setId(doc.getId()); // Gắn ID của Firebase vào Object
                            allServices.add(dv);
                        }
                    }

                    // Báo cho Adapter biết là có data mới để vẽ lại màn hình
                    serviceAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dịch vụ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
        if (selectedVoucherDetail != null) {
            totalPrice -= selectedVoucherDetail.getSoTienGiam();
        }
        // Đảm bảo tổng tiền không bị âm
        if (totalPrice < 0) {
            totalPrice = 0;
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

        // Gọi hàm load voucher sau khi khởi tạo
        loadUserVouchers();
    }

    // Hàm tải Voucher của User hiện tại từ Firebase
    private void loadUserVouchers() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Lấy toàn bộ bảng Voucher (để lấy tên và số tiền giảm)
        db.collection("Voucher").get().addOnSuccessListener(voucherSnap -> {
            List<com.example.petcareapp.data.model.Voucher> allVouchers = new ArrayList<>();
            for (DocumentSnapshot doc : voucherSnap.getDocuments()) {
                com.example.petcareapp.data.model.Voucher v = doc.toObject(com.example.petcareapp.data.model.Voucher.class);
                if (v != null) {
                    v.setIdVoucher(doc.getId());
                    allVouchers.add(v);
                }
            }

            // 2. Lấy danh sách Voucher TRONG KHO của user mà "Chưa dùng"
            db.collection("KhoVoucher")
                    .whereEqualTo("idUser", userId)
                    .whereEqualTo("trangThai", "Chưa dùng")
                    .get()
                    .addOnSuccessListener(khoSnap -> {
                        userKhoVoucherList.clear();
                        userVoucherDetails.clear();
                        List<String> displayNames = new ArrayList<>();

                        // Thêm tùy chọn mặc định đầu tiên
                        displayNames.add("-- Không dùng Voucher --");

                        for (DocumentSnapshot doc : khoSnap.getDocuments()) {
                            com.example.petcareapp.data.model.KhoVoucher kv = doc.toObject(com.example.petcareapp.data.model.KhoVoucher.class);
                            if (kv != null) {
                                kv.setId(doc.getId());

                                // Ghép dữ liệu từ KhoVoucher với bảng Voucher gốc
                                for (com.example.petcareapp.data.model.Voucher v : allVouchers) {
                                    if (v.getIdVoucher().equals(kv.getIdVoucher())) {
                                        userKhoVoucherList.add(kv);
                                        userVoucherDetails.add(v);
                                        displayNames.add(v.getTenVoucher() + " (Giảm " + String.format("%,d", v.getSoTienGiam()) + "đ)");
                                        break;
                                    }
                                }
                            }
                        }

                        // Đưa dữ liệu lên Spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, displayNames);
                        spinnerVoucher.setAdapter(adapter);

                        // Bắt sự kiện khi User chọn Voucher
                        spinnerVoucher.setOnItemClickListener((parent, view, position, id) -> {
                            if (position == 0) {
                                // Nếu chọn "Không dùng"
                                selectedKhoVoucher = null;
                                selectedVoucherDetail = null;
                            } else {
                                // Trừ đi 1 vì index 0 là dòng "Không dùng Voucher"
                                selectedKhoVoucher = userKhoVoucherList.get(position - 1);
                                selectedVoucherDetail = userVoucherDetails.get(position - 1);
                            }
                            // Gọi lại hàm tính tiền để trừ đi số tiền giảm
                            calculateTotalPrice();
                        });
                    });
        });
    }

    private void thucHienDatLich() {
        if (!isInputValid()) {
            showToast("Vui lòng chọn đầy đủ thông tin!");
            return;
        }

        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            showToast("Người dùng chưa đăng nhập");
            return;
        }

        loadUserInfoAndCreateAppointment(userId);
    }

    private boolean isInputValid() {
        return selectedPet != null
                && selectedBranch != null
                && !selectedServices.isEmpty()
                && !selectedTime.isEmpty()
                && edtSelectDate.getText() != null
                && !edtSelectDate.getText().toString().trim().isEmpty();
    }

    private void loadUserInfoAndCreateAppointment(String userId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    String tenChu = getUserName(document);
                    String soDienThoai = getUserPhone(document);

                    createAppointment(
                            userId,
                            tenChu,
                            soDienThoai
                    );
                })
                .addOnFailureListener(e ->
                        showToast("Không thể tải thông tin người dùng")
                );
    }

    private String getUserName(DocumentSnapshot document) {
        if (document.exists()) {
            String name = document.getString("hoTen");
            return name != null ? name : "Khách hàng";
        }
        return "Khách hàng";
    }

    private String getUserPhone(DocumentSnapshot document) {
        if (document.exists()) {
            String phone = document.getString("soDienThoai");
            return phone != null ? phone : "Chưa cập nhật";
        }
        return "Chưa cập nhật";
    }

    private void createAppointment(
            String userId,
            String tenChu,
            String soDienThoai
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String lichHenId =
                db.collection("LichHen")
                        .document()
                        .getId();

        Timestamp thoiGianHen = buildAppointmentTime();

        if (thoiGianHen == null) {
            showToast("Lỗi định dạng giờ");
            return;
        }

        LichHen lichHen = buildLichHen(
                lichHenId,
                userId,
                tenChu,
                soDienThoai,
                thoiGianHen
        );

        saveAppointment(db, lichHenId, lichHen);
    }

    private Timestamp buildAppointmentTime() {
        try {
            Calendar calendar =
                    (Calendar) selectedDate.clone();

            String[] timeParts =
                    selectedTime.replace(" ", "")
                            .split(":");

            calendar.set(
                    Calendar.HOUR_OF_DAY,
                    Integer.parseInt(timeParts[0])
            );

            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            return new Timestamp(calendar.getTime());

        } catch (Exception e) {
            return null;
        }
    }

    private LichHen buildLichHen(
            String lichHenId,
            String userId,
            String tenChu,
            String soDienThoai,
            Timestamp thoiGianHen
    ) {
        LichHen lichHen = new LichHen();

        lichHen.setId(lichHenId);
        lichHen.setUserId(userId);
        lichHen.setPetId(selectedPet.getId());
        lichHen.setChiNhanhId(selectedBranch.getId());
        lichHen.setThoiGianHen(thoiGianHen);

        lichHen.setDanhSachDichVu(selectedServices);
        lichHen.setTongTien(totalPrice);

        String ghiChuGoc = edtNotes.getText() != null ? edtNotes.getText().toString().trim() : "";

        // Nếu khách có xài Voucher, nối thêm câu thông báo vào ghi chú
        if (selectedVoucherDetail != null) {
            String thongTinVoucher = "\n[Đã áp dụng: " + selectedVoucherDetail.getTenVoucher() +
                    " - Giảm " + String.format("%,d đ", selectedVoucherDetail.getSoTienGiam()) + "]";

            ghiChuGoc = ghiChuGoc + thongTinVoucher;
        }

        lichHen.setGhiChu(ghiChuGoc.trim());
        lichHen.setLyDoTuChoi("");
        lichHen.setTrangThai("Chờ duyệt");

        lichHen.setTenThuCung(selectedPet.getName());
        lichHen.setTenChiNhanh(
                selectedBranch.getTenChiNhanh()
        );

        lichHen.setTenChuThuCung(tenChu);
        lichHen.setSoDienThoai(soDienThoai);

        return lichHen;
    }

    private void saveAppointment(
            FirebaseFirestore db,
            String lichHenId,
            LichHen lichHen
    ) {
        // Sử dụng WriteBatch để thực hiện 2 lệnh cùng lúc một cách an toàn
        WriteBatch batch = db.batch();

        // Lệnh 1: Lưu Lịch hẹn vào Database
        batch.set(db.collection("LichHen").document(lichHenId), lichHen);

        // Lệnh 2: Nếu có xài Voucher -> Chuyển trạng thái Voucher đó thành "Đã dùng"
        if (selectedKhoVoucher != null) {
            batch.update(db.collection("KhoVoucher").document(selectedKhoVoucher.getId()), "trangThai", "Đã dùng");
        }

        // Chạy đồng thời
        batch.commit()
                .addOnSuccessListener(unused -> {
                    showToast("Đặt lịch thành công!");
                    finish();
                })
                .addOnFailureListener(e ->
                        showToast("Lỗi: " + e.getMessage())
                );
    }

    private void showToast(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
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
            holder.tvServiceName.setText(dv.getTenDichVu());
            holder.tvServicePrice.setText(String.format("%,d đ", (int) dv.getGia()));

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