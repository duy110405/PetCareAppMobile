package com.example.petcareapp.ui.user.Pet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcareapp.R;
import com.example.petcareapp.ui.user.Alarm.AlarmReceiver;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.pm.PackageManager;
import android.os.Build;


public class ThemChuongBaoActivity extends AppCompatActivity {

    private EditText edtName, edtDescription;
    private TextView edtTime;
    private MaterialButton btnDaily, btnOnce, btnAdd, btnCancel;

    private FirebaseFirestore db;
    private String userId, petId;

    private String selectedType = "Hằng ngày";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_them_chuong_bao);

        requestNotificationPermission();

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        petId = getIntent().getStringExtra("petId");

        edtName = findViewById(R.id.edtAlarmName);
        edtDescription = findViewById(R.id.edtAlarmDescription);
        edtTime = findViewById(R.id.edtAlarmTime);

        btnDaily = findViewById(R.id.btnTypeDaily);
        btnOnce = findViewById(R.id.btnTypeOnce);
        btnAdd = findViewById(R.id.btnAddAlarmSubmit);
        btnCancel = findViewById(R.id.btnCancelAlarm);

        // ================= CHỌN GIỜ =================
        edtTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            TimePickerDialog dialog = new TimePickerDialog(this,
                    (view, hour, minute) -> {
                        selectedTime = String.format("%02d:%02d", hour, minute);
                        edtTime.setText(selectedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);

            dialog.show();
        });

        updateTypeUI();


        // ================= TYPE =================
        btnDaily.setOnClickListener(v -> {
            selectedType = "Hằng ngày";
            updateTypeUI();
        });

        btnOnce.setOnClickListener(v -> {
            selectedType = "1 lần";
            updateTypeUI();
        });


        // ================= CANCEL =================
        btnCancel.setOnClickListener(v -> finish());

        // ================= ADD =================
        btnAdd.setOnClickListener(v -> saveAlarm());
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }


    private void updateTypeUI() {

        if (selectedType.equals("Hằng ngày")) {

            btnDaily.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.success))
            );
            btnDaily.setTextColor(getResources().getColor(android.R.color.white));

            btnOnce.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.white))
            );
            btnOnce.setTextColor(getResources().getColor(android.R.color.black));

        } else {

            btnOnce.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.success))
            );
            btnOnce.setTextColor(getResources().getColor(android.R.color.white));

            btnDaily.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.white))
            );
            btnDaily.setTextColor(getResources().getColor(android.R.color.black));
        }
    }


    private void saveAlarm() {

        String name = edtName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError("Nhập tên chuông");
            return;
        }

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Chọn giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        String alarmId = db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .collection("alarms")
                .document()
                .getId();

        Map<String, Object> alarm = new HashMap<>();
        alarm.put("id", alarmId);
        alarm.put("name", name);
        alarm.put("time", selectedTime);
        alarm.put("type", selectedType);
        alarm.put("description", desc);

        db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .collection("alarms")
                .document(alarmId)
                .set(alarm)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã thêm chuông báo", Toast.LENGTH_SHORT).show();
                    scheduleAlarm(alarmId, name, desc, selectedTime, selectedType);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void scheduleAlarm(String alarmId, String name, String desc, String time, String type) {

        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // 🔥 FIX: luôn đảm bảo thời gian trong tương lai
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("name", name);
        intent.putExtra("desc", desc);
        intent.putExtra("alarmId", alarmId);

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                alarmId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // 🔥 FIX QUAN TRỌNG
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pi
        );

        Toast.makeText(this, "Alarm đã set", Toast.LENGTH_SHORT).show();
    }


}
