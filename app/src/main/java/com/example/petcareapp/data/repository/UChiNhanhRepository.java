package com.example.petcareapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.petcareapp.data.model.ChiNhanh;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UChiNhanhRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Lấy danh sách chi nhánh
    public void layDanhSachChiNhanh(MutableLiveData<List<ChiNhanh>> listLiveData) {
        db.collection("ChiNhanh").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            List<ChiNhanh> list = new ArrayList<>();
            // Duyệt qua tất cả dữ liệu lấy về
            for (var doc : value.getDocuments()) {
                ChiNhanh cn = doc.toObject(ChiNhanh.class);
                if (cn != null) list.add(cn);
            }
            listLiveData.setValue(list); // Đẩy về ViewModel
        });
    }
}
