package com.example.petcareapp.data.repository;


import com.example.petcareapp.data.model.Pet;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class PetRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listener;

    public void getPetsByUser(String userId, OnPetsCallback callback) {

        // tránh duplicate listener
        if (listener != null) {
            listener.remove();
        }

        listener = db.collection("users")
                .document(userId)
                .collection("pets")
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null || snapshots == null) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    List<Pet> list = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {

                        Pet pet = doc.toObject(Pet.class);

                        if (pet != null) {
                            pet.setId(doc.getId()); // QUAN TRỌNG NHẤT
                            list.add(pet);
                        }
                    }

                    callback.onResult(list);
                });
    }

    public void removeListener() {
        if (listener != null) {
            listener.remove();
        }
    }

    public interface OnPetsCallback {
        void onResult(List<Pet> pets);
    }
}
