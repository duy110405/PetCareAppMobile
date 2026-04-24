package com.example.petcareapp.ui.admin.lichhen;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.ChiNhanh;
import com.example.petcareapp.data.model.LichHen;
import com.example.petcareapp.utils.MenuAdmin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Màn hình quản lý danh sách lịch hẹn dành cho Admin.
 *
 * Chức năng:
 * - Xem toàn bộ lịch hẹn
 * - Lọc theo trạng thái
 * - Lọc theo chi nhánh
 * - Lọc theo ngày
 * - Thống kê nhanh số lượng lịch hẹn
 */
public class AdminLichHenActivity extends AppCompatActivity {

    // ===== Bộ lọc =====
    private AutoCompleteTextView spStatus, spBranch;
    private Button btnPickDate, btnFilter;

    // ===== UI hiển thị =====
    private View tvEmpty;
    private TextView tvCount;
    private RecyclerView recyclerView;

    // ===== Dashboard thống kê =====
    private TextView tvPendingCount;
    private TextView tvApprovedCount;
    private TextView tvCancelledCount;
    private TextView tvTotalCount;

    // ===== Adapter + Data =====
    private AdminLichHenAdapter adapter;
    private final List<LichHen> list = new ArrayList<>();
    private final List<String> branchNames = new ArrayList<>();

    // Ngày được chọn để lọc
    private Date selectedDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lich_hen);

        // Khởi tạo view
        initView();

        // Thiết lập dữ liệu bộ lọc trạng thái
        setupStatusSpinner();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Gán sự kiện
        setupEvent();

        // Load dữ liệu ban đầu
        loadBranches();
        loadAppointments();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        MenuAdmin.setup(this, bottomNav);
    }

    /**
     * Thu gọn phần filter sau khi người dùng áp dụng bộ lọc.
     */
    private void collapseFilter(String status, String branch) {
        findViewById(R.id.filterContainer).setVisibility(View.GONE);
        findViewById(R.id.tvFilterSummary).setVisibility(View.VISIBLE);

        TextView tv = findViewById(R.id.tvFilterSummary);
        tv.setText("Đang lọc: " + status + " • " + branch);
    }

    /**
     * Ánh xạ các thành phần giao diện.
     */
    private void initView() {
        spStatus = findViewById(R.id.spStatus);
        spBranch = findViewById(R.id.spBranch);

        btnPickDate = findViewById(R.id.btnPickDate);
        btnFilter = findViewById(R.id.btnFilter);

        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView = findViewById(R.id.recyclerView);
        tvCount = findViewById(R.id.tvCount);

        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvApprovedCount = findViewById(R.id.tvApprovedCount);
        tvCancelledCount = findViewById(R.id.tvCancelledCount);
        tvTotalCount = findViewById(R.id.tvTotalCount);
    }

    /**
     * Cập nhật dashboard thống kê nhanh.
     */
    private void updateDashboard() {
        int pending = 0;
        int approved = 0;
        int cancelled = 0;

        for (LichHen item : list) {
            String status = item.getTrangThai();

            if ("Chờ duyệt".equals(status)) {
                pending++;
            } else if ("Đã xác nhận".equals(status)) {
                approved++;
            } else if ("Đã hủy".equals(status)) {
                cancelled++;
            }
        }

        tvPendingCount.setText(pending + "\nChờ");
        tvApprovedCount.setText(approved + "\nDuyệt");
        tvCancelledCount.setText(cancelled + "\nHủy");
        tvTotalCount.setText(list.size() + "\nTổng");
    }

    /**
     * Cập nhật text số lượng kết quả sau khi lọc.
     */
    private void updateCountText() {
        int count = list.size();

        String status = spStatus.getText().toString().trim();

        if (status.isEmpty()) {
            status = "Tất cả";
        }

        if ("Tất cả".equals(status)) {
            tvCount.setText(count + " lịch hẹn");
        } else {
            tvCount.setText(count + " lịch " + status.toLowerCase());
        }

        // Đổi màu theo trạng thái
        if ("Chờ duyệt".equals(status)) {
            tvCount.setTextColor(Color.parseColor("#FFA000"));
        } else if ("Đã xác nhận".equals(status)) {
            tvCount.setTextColor(Color.parseColor("#4CAF50"));
        } else if ("Đã hủy".equals(status)) {
            tvCount.setTextColor(Color.parseColor("#9E9E9E"));
        } else {
            tvCount.setTextColor(Color.parseColor("#333333"));
        }
    }

    /**
     * Thiết lập danh sách trạng thái cho bộ lọc.
     */
    private void setupStatusSpinner() {
        String[] statuses = {
                "Tất cả",
                "Chờ duyệt",
                "Đã xác nhận",
                "Đã hủy",
                "Hoàn thành"
        };

        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        statuses
                );

        spStatus.setAdapter(statusAdapter);

        // Giá trị mặc định
        spStatus.setText("Tất cả", false);
    }

    /**
     * Tải danh sách chi nhánh từ Firebase.
     */
    private void loadBranches() {
        FirebaseFirestore.getInstance()
                .collection("ChiNhanh")
                .get()
                .addOnSuccessListener(snapshot -> {

                    branchNames.clear();
                    branchNames.add("Tất cả");

                    for (var doc : snapshot.getDocuments()) {
                        ChiNhanh cn = doc.toObject(ChiNhanh.class);

                        if (cn != null && cn.getTenChiNhanh() != null) {
                            branchNames.add(cn.getTenChiNhanh());
                        }
                    }

                    ArrayAdapter<String> branchAdapter =
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    branchNames
                            );

                    spBranch.setAdapter(branchAdapter);
                    spBranch.setText("Tất cả", false);
                });
    }

    /**
     * Thiết lập RecyclerView và adapter.
     */
    private void setupRecyclerView() {
        adapter = new AdminLichHenAdapter(this, list);

        // Reload lại danh sách khi trạng thái thay đổi
        adapter.setOnStatusChangedListener(this::loadAppointments);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);
    }

    /**
     * Gán sự kiện cho các nút thao tác.
     */
    private void setupEvent() {
        btnPickDate.setOnClickListener(v -> showDatePicker());

        btnFilter.setOnClickListener(v -> {

            loadAppointments();

            String status = spStatus.getText().toString().trim();
            if (status.isEmpty()) status = "Tất cả";

            String branch = spBranch.getText().toString().trim();
            if (branch.isEmpty()) branch = "Tất cả";

            collapseFilter(status, branch);
        });

        // Click để mở lại phần filter
        findViewById(R.id.tvFilterSummary).setOnClickListener(v -> {
            findViewById(R.id.filterContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.tvFilterSummary).setVisibility(View.GONE);
        });
    }

    /**
     * Hiển thị DatePicker để lọc theo ngày.
     */
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        // Nếu đã chọn ngày trước đó thì mở đúng ngày đó
        if (selectedDate != null) {
            cal.setTime(selectedDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(year, month, dayOfMonth);

                    selectedDate = cal.getTime();

                    btnPickDate.setText(
                            String.format(
                                    "Ngày: %02d/%02d/%d",
                                    dayOfMonth,
                                    month + 1,
                                    year
                            )
                    );
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        // Nút reset bộ lọc ngày
        dialog.setButton(
                DatePickerDialog.BUTTON_NEUTRAL,
                "Tất cả",
                (d, which) -> {
                    selectedDate = null;
                    btnPickDate.setText("Ngày: Tất cả");
                }
        );

        dialog.show();
    }

    /**
     * Tải danh sách lịch hẹn theo bộ lọc hiện tại.
     */
    private void loadAppointments() {

        // Query mặc định: mới nhất lên đầu
        Query query = FirebaseFirestore.getInstance()
                .collection("LichHen")
                .orderBy("thoiGianHen", Query.Direction.DESCENDING);

        // ===== Lọc theo ngày =====
        if (selectedDate != null) {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(selectedDate);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);

            Date startOfDay = startCal.getTime();

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(selectedDate);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);

            Date endOfDay = endCal.getTime();

            query = query
                    .whereGreaterThanOrEqualTo(
                            "thoiGianHen",
                            startOfDay
                    )
                    .whereLessThanOrEqualTo(
                            "thoiGianHen",
                            endOfDay
                    );
        }

        // ===== Lọc theo trạng thái và chi nhánh =====
        String status = spStatus.getText().toString().trim();
        if (status.isEmpty()) status = "Tất cả";

        String branch = spBranch.getText().toString().trim();
        if (branch.isEmpty()) branch = "Tất cả";

        if (!status.equals("Tất cả")) {
            query = query.whereEqualTo("trangThai", status);
        }

        if (!branch.equals("Tất cả")) {
            query = query.whereEqualTo("tenChiNhanh", branch);
        }

        // ===== Thực thi query =====
        query.get().addOnSuccessListener(snapshot -> {
            list.clear();

            for (var doc : snapshot.getDocuments()) {
                LichHen item = doc.toObject(LichHen.class);

                if (item == null) continue;

                item.setId(doc.getId());
                list.add(item);
            }

            adapter.updateList(list);

            updateCountText();
            updateDashboard();

            tvEmpty.setVisibility(
                    list.isEmpty() ? TextView.VISIBLE : TextView.GONE
            );

        }).addOnFailureListener(e -> {
            Toast.makeText(
                    this,
                    "Lỗi tải dữ liệu. Cần tạo Index Firebase!",
                    Toast.LENGTH_LONG
            ).show();
        });
    }
}