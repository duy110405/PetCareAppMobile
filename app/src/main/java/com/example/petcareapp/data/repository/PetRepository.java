package com.example.petcareapp.data.repository;


import com.example.petcareapp.data.model.Pet;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PetRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getPetsByUser(String userId, OnPetsCallback callback) {

        db.collection("users")
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
                            list.add(pet);
                        }
                    }

                    callback.onResult(list);
                });
    }

    public interface OnPetsCallback {
        void onResult(List<Pet> pets);
    }
}
