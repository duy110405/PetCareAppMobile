package com.example.petcareapp.data.repository;

import com.example.petcareapp.data.model.LichHen;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class LichHenRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference lichHenRef = db.collection("LichHen");

    public void addLichHen(LichHen lichHen, OnSuccessListener listener) {
        lichHenRef.document(lichHen.getId()).set(lichHen)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public Query getLichHenByUser(String userId) {
        return lichHenRef.whereEqualTo("userId", userId).orderBy("thoiGianHen", Query.Direction.DESCENDING);
    }

    public interface OnSuccessListener {
        void onSuccess();
        void onFailure(String error);
    }
}
