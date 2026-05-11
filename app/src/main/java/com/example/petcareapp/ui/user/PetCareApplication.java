package com.example.petcareapp.ui.user;

import android.app.Application;

import com.example.petcareapp.utils.ThemeManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

public class PetCareApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        ThemeManager.init(this);
    }
}