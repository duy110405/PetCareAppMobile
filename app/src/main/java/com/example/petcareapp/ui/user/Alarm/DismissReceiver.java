package com.example.petcareapp.ui.user.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DismissReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String alarmId = intent.getStringExtra("alarmId");

        if (alarmId == null) return;

        // Hủy alarm
        Intent i = new Intent(context, AlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                alarmId.hashCode(),
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pi);
    }
}
