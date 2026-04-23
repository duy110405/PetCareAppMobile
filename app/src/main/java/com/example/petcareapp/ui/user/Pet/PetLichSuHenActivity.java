package com.example.petcareapp.ui.user.Pet;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcareapp.R;
import com.example.petcareapp.data.model.LichHen;
import com.example.petcareapp.ui.user.LichHen.LichHenAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PetLichSuHenActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LichHenAdapter adapter;
    private String petId;
    private List<LichHen> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_lich_su_hen);

        petId = getIntent().getStringExtra("petId");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tái sử dụng Adapter của User
        adapter = new LichHenAdapter();
        recyclerView.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        if (petId == null) return;

        FirebaseFirestore.getInstance().collection("LichHen")
                .whereEqualTo("petId", petId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    list.clear();
                    for (var doc : snapshot.getDocuments()) {
                        LichHen item = doc.toObject(LichHen.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            list.add(item);
                        }
                    }

                    // Sắp xếp lịch sử khám: Cái nào gần đây nhất hiển thị lên đầu
                    Collections.sort(list, (o1, o2) -> {
                        if(o1.getThoiGianHen() == null || o2.getThoiGianHen() == null) return 0;
                        return o2.getThoiGianHen().toDate().compareTo(o1.getThoiGianHen().toDate());
                    });

                    adapter.setData(list);

                    if (list.isEmpty()) {
                        Toast.makeText(this, "Thú cưng này chưa có lịch sử hẹn nào", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show());
    }
}