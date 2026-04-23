package com.example.petcareapp.ui.user.LichHen;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareapp.data.model.LichHen;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel quản lý dữ liệu lịch hẹn của người dùng
 *
 * Chức năng:
 * - Tải danh sách lịch hẹn theo userId
 * - Lắng nghe thay đổi realtime từ Firestore
 * - Cập nhật LiveData cho UI
 */
public class LichHenViewModel extends ViewModel {

    /**
     * LiveData chứa danh sách lịch hẹn
     */
    private final MutableLiveData<List<LichHen>> appointments =
            new MutableLiveData<>(new ArrayList<>());

    /**
     * Listener realtime Firestore
     * Dùng để remove khi ViewModel bị destroy
     */
    private ListenerRegistration listenerRegistration;

    /**
     * Expose LiveData ra ngoài cho Activity / Fragment observe
     */
    public LiveData<List<LichHen>> getAppointments() {
        return appointments;
    }

    /**
     * Tải danh sách lịch hẹn theo userId
     *
     * @param userId id của user hiện tại
     */
    public void loadAppointments(String userId) {

        // Validate input
        if (userId == null || userId.trim().isEmpty()) {
            appointments.setValue(new ArrayList<>());
            return;
        }

        // Remove listener cũ nếu có
        removeListener();

        listenerRegistration = getAppointmentQuery(userId)
                .addSnapshotListener((snapshot, error) -> {

                    // Có lỗi Firestore
                    if (error != null) {
                        appointments.setValue(new ArrayList<>());
                        return;
                    }

                    // Không có dữ liệu
                    if (snapshot == null) {
                        appointments.setValue(new ArrayList<>());
                        return;
                    }

                    // Update LiveData
                    appointments.setValue(
                            snapshot.toObjects(LichHen.class)
                    );
                });
    }

    /**
     * Tạo query lấy danh sách lịch hẹn
     */
    private Query getAppointmentQuery(String userId) {
        return FirebaseFirestore.getInstance()
                .collection("LichHen")
                .whereEqualTo("userId", userId)
                .orderBy(
                        "thoiGianHen",
                        Query.Direction.DESCENDING
                );
    }

    /**
     * Gỡ listener cũ để tránh memory leak
     */
    private void removeListener() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    /**
     * Tự động gọi khi ViewModel bị hủy
     * Rất quan trọng để tránh leak Firestore listener
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        removeListener();
    }
}