package com.example.petcareapp.ui.user.Alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        android.util.Log.d("ALARM_TEST", "RECEIVER FIRED");
        android.widget.Toast.makeText(context, "ALARM FIRED", android.widget.Toast.LENGTH_SHORT).show();

        String name = intent.getStringExtra("name");
        String desc = intent.getStringExtra("desc");
        String alarmId = intent.getStringExtra("alarmId");

        if (name == null) name = "Chuông báo";
        if (desc == null) desc = "Đến giờ rồi!";

        NotificationHelper.createChannel(context);

        Intent dismissIntent = new Intent(context, DismissReceiver.class);
        dismissIntent.putExtra("alarmId", alarmId);

        PendingIntent dismissPI = PendingIntent.getBroadcast(
                context,
                alarmId.hashCode(),
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // 👈 FIX ICON
                .setContentTitle(name)
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Tắt", dismissPI)
                .build();

        nm.notify((int) System.currentTimeMillis(), notification);
    }

}

