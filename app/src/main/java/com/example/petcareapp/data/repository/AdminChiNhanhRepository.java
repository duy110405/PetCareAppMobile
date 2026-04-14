package com.example.petcareapp.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.petcareapp.data.model.ChiNhanh;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminChiNhanhRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Hàm thêm chi nhánh lên mạng
    public void themChiNhanhLenFirebase(ChiNhanh chiNhanh, MutableLiveData<String> trangThaiThem) {
        // Tạo một bảng dữ liệu tên là "ChiNhanh" trên Firebase
        db.collection("ChiNhanh")
                .add(chiNhanh)
                .addOnSuccessListener(documentReference -> {
                    // Lấy mã ID do Firebase tự sinh ra, lưu ngược lại vào đối tượng
                    chiNhanh.setId(documentReference.getId());
                    db.collection("ChiNhanh").document(chiNhanh.getId()).set(chiNhanh);

                    trangThaiThem.setValue("Thêm chi nhánh thành công!");
                })
                .addOnFailureListener(e -> {
                    trangThaiThem.setValue("Lỗi: " + e.getMessage());
                });
    }
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

    // Hàm Cập nhật chi nhánh
    public void suaChiNhanhTrenFirebase(ChiNhanh chiNhanh, MutableLiveData<String> trangThai) {
        db.collection("ChiNhanh").document(chiNhanh.getId()).set(chiNhanh)
                .addOnSuccessListener(aVoid -> trangThai.setValue("Cập nhật thành công!"))
                .addOnFailureListener(e -> trangThai.setValue("Lỗi sửa: " + e.getMessage()));
    }

    //Hàm Xóa chi nhánh
    public void xoaChiNhanhTrenFirebase(String idChiNhanh, MutableLiveData<String> trangThai) {
        db.collection("ChiNhanh").document(idChiNhanh).delete()
                .addOnSuccessListener(aVoid -> trangThai.setValue("Đã xóa chi nhánh!"))
                .addOnFailureListener(e -> trangThai.setValue("Lỗi xóa: " + e.getMessage()));
    }
}
