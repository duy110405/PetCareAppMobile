package com.example.petcareapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_AUTO = "auto_theme";

    public static void setAutoTheme(Context context, boolean enable) {
        SharedPreferences pref =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        pref.edit().putBoolean(KEY_AUTO, enable).apply();
    }

    public static boolean isAutoTheme(Context context) {
        SharedPreferences pref =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return pref.getBoolean(KEY_AUTO, false);
    }

    public static void setDarkMode(Context context, boolean dark) {
        SharedPreferences pref =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        pref.edit().putBoolean(KEY_DARK_MODE, dark).apply();

        applyTheme(dark);
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences pref =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return pref.getBoolean(KEY_DARK_MODE, false);
    }

    public static void applyTheme(boolean dark) {
        AppCompatDelegate.setDefaultNightMode(
                dark
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static void init(Context context) {
        applyTheme(isDarkMode(context));
    }
}