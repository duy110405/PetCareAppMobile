package com.example.petcareapp.data.repository;

import com.example.petcareapp.data.model.LichHen;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Repository xử lý các thao tác với dữ liệu lịch hẹn.
 *
 * Chức năng chính:
 * - Thêm lịch hẹn mới
 * - Lấy danh sách lịch hẹn theo người dùng
 * - Tách riêng logic làm việc với Firestore khỏi UI
 */
public class LichHenRepository {

    // Kết nối tới Firebase Firestore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Collection lưu danh sách lịch hẹn
    private final CollectionReference lichHenRef = db.collection("LichHen");

    /**
     * Thêm lịch hẹn mới vào Firestore.
     *
     * @param lichHen dữ liệu lịch hẹn cần lưu
     * @param listener callback trả về kết quả thành công / thất bại
     */
    public void addLichHen(LichHen lichHen, OnSuccessListener listener) {
        lichHenRef.document(lichHen.getId()).set(lichHen)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    /**
     * Lấy danh sách lịch hẹn của một người dùng.
     *
     * Kết quả được sắp xếp theo thời gian hẹn mới nhất.
     *
     * @param userId ID người dùng
     * @return Query để tiếp tục observe hoặc lấy dữ liệu
     */
    public Query getLichHenByUser(String userId) {
        return lichHenRef
                .whereEqualTo("userId", userId)
                .orderBy("thoiGianHen", Query.Direction.DESCENDING);
    }

    /**
     * Callback dùng để trả kết quả thao tác Firebase.
     */
    public interface OnSuccessListener {
        void onSuccess();
        void onFailure(String error);
    }
}