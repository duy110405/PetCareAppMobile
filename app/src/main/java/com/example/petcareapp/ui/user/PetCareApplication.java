package com.example.petcareapp.ui.user;

import android.app.Application;

import com.example.petcareapp.utils.ThemeManager;

public class PetCareApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ThemeManager.init(this);
    }
}