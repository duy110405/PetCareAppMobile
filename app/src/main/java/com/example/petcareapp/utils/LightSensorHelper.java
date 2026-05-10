package com.example.petcareapp.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.appcompat.app.AppCompatDelegate;

public class LightSensorHelper implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean enabled;
    private Context context;

    public LightSensorHelper(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void register() {
        sensorManager.unregisterListener(this);

        if (enabled && lightSensor != null) {
            sensorManager.registerListener(
                    this,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_UI
            );
        }
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (!enabled) return;

        float lux = event.values[0];

        boolean dark = lux < 30;

        AppCompatDelegate.setDefaultNightMode(
                dark
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
    public void syncState(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}