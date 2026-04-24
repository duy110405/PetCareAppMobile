package com.example.petcareapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.petcareapp.data.model.DichVu;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DichVuRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MutableLiveData<List<DichVu>> getAllDichVu() {
        MutableLiveData<List<DichVu>> liveData = new MutableLiveData<>();

        db.collection("DichVu")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    List<DichVu> list = new ArrayList<>();
                    if (value != null) {
                        for (var doc : value.getDocuments()) {
                            DichVu dv = doc.toObject(DichVu.class);
                            if (dv != null) {
                                dv.setId(doc.getId());
                                list.add(dv);
                            }
                        }
                    }
                    liveData.setValue(list);
                });

        return liveData;
    }

    public void addDichVu(DichVu dv, Callback callback) {
        if (dv.getId() == null) {
            dv.setId(db.collection("DichVu").document().getId());
        }

        db.collection("DichVu").document(dv.getId())
                .set(dv)
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateDichVu(DichVu dv, Callback callback) {
        db.collection("DichVu").document(dv.getId())
                .set(dv)
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteDichVu(String id, Callback callback) {
        db.collection("DichVu").document(id)
                .delete()
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface Callback {
        void onSuccess();
        void onFailure(String err);
    }
}