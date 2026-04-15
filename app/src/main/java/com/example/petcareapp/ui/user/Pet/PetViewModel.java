package com.example.petcareapp.ui.user.Pet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.petcareapp.data.model.Pet;
import com.example.petcareapp.data.repository.PetRepository;


import java.util.List;

public class PetViewModel extends ViewModel {
    private final MutableLiveData<List<Pet>> petsLiveData = new MutableLiveData<>();
    private final PetRepository repository = new PetRepository();

    public LiveData<List<Pet>> getPets() {
        return petsLiveData;
    }

    public void loadPets(String userId) {

        repository.getPetsByUser(userId, pets -> {
            petsLiveData.setValue(pets);
        });
    }
}
