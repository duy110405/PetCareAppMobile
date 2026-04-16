package com.example.petcareapp.ui.user.LichHen;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.petcareapp.data.model.LichHen;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class LichHenViewModel extends ViewModel {
    private final MutableLiveData<List<LichHen>> appointments = new MutableLiveData<>();

    public LiveData<List<LichHen>> getAppointments() { return appointments; }

    public void loadAppointments(String userId) {
        FirebaseFirestore.getInstance().collection("LichHen")
                .whereEqualTo("userId", userId)
                .orderBy("thoiGianHen", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        appointments.setValue(value.toObjects(LichHen.class));
                    }
                });
    }
}
