package com.example.petcareapp.ui.admin.lichhen;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.ChiNhanh;
import com.example.petcareapp.data.model.LichHen;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AdminLichHenActivity extends AppCompatActivity {

    private Spinner spStatus, spBranch;
    private Button btnPickDate, btnFilter;
    private TextView tvEmpty;
    private TextView tvCount;
    private RecyclerView recyclerView;
    private AdminLichHenAdapter adapter;

    private final List<LichHen> list = new ArrayList<>();
    private final List<String> branchNames = new ArrayList<>();

    private Date selectedDate = new Date();

    private TextView tvPendingCount;
    private TextView tvApprovedCount;
    private TextView tvCancelledCount;
    private TextView tvTotalCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lich_hen);

        initView();
        setupStatusSpinner();
        setupRecyclerView();
        setupEvent();

        loadBranches();
        loadAppointments();
    }
    private void collapseFilter(String status, String branch) {

        findViewById(R.id.filterContainer).setVisibility(View.GONE);
        findViewById(R.id.tvFilterSummary).setVisibility(View.VISIBLE);

        TextView tv = findViewById(R.id.tvFilterSummary);
        tv.setText("Đang lọc: " + status + " • " + branch);
    }

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
    private void updateCountText() {
        int count = list.size();

        String status = "Tất cả";

        if (spStatus.getSelectedItem() != null) {
            status = spStatus.getSelectedItem().toString();
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
    }

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
                });
    }

    private void setupRecyclerView() {
        adapter = new AdminLichHenAdapter(this, list);

        adapter.setOnStatusChangedListener(() -> {
            loadAppointments();
        });

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);
    }

    private void setupEvent() {
        btnPickDate.setOnClickListener(v -> showDatePicker());

        btnFilter.setOnClickListener(v -> {

            loadAppointments();

            String status = spStatus.getSelectedItem().toString();
            String branch = spBranch.getSelectedItem().toString();

            collapseFilter(status, branch);
        });

        findViewById(R.id.tvFilterSummary).setOnClickListener(v -> {
            findViewById(R.id.filterContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.tvFilterSummary).setVisibility(View.GONE);
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(year, month, dayOfMonth);
                    selectedDate = cal.getTime();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void loadAppointments() {

        Query query = FirebaseFirestore.getInstance()
                .collection("LichHen")
                .orderBy("thoiGianHen", Query.Direction.DESCENDING);

        String status = "Tất cả";
        String branch = "Tất cả";

        if (spStatus.getSelectedItem() != null) {
            status = spStatus.getSelectedItem().toString();
        }

        if (spBranch.getSelectedItem() != null) {
            branch = spBranch.getSelectedItem().toString();
        }

        if (!status.equals("Tất cả")) {
            query = query.whereEqualTo("trangThai", status);
        }

        if (!branch.equals("Tất cả")) {
            query = query.whereEqualTo("tenChiNhanh", branch);
        }

        query.get().addOnSuccessListener(snapshot -> {

            list.clear();
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);

            for (var doc : snapshot.getDocuments()) {
                LichHen item = doc.toObject(LichHen.class);

                if (item == null || item.getThoiGianHen() == null) continue;

                Calendar itemCal = Calendar.getInstance();
                itemCal.setTime(item.getThoiGianHen().toDate());

                boolean sameDate =
                        itemCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR)
                                && itemCal.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH)
                                && itemCal.get(Calendar.DAY_OF_MONTH) == selectedCal.get(Calendar.DAY_OF_MONTH);

                if (sameDate) {
                    item.setId(doc.getId());
                    list.add(item);
                }
            }

            adapter.updateList(list);
            updateCountText();
            updateDashboard();

            tvEmpty.setVisibility(
                    list.isEmpty() ? TextView.VISIBLE : TextView.GONE
            );
        });
    }

}