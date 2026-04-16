package com.example.petcareapp.ui.user.Alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

public class NotificationHelper {
    public static final String CHANNEL_ID = "pet_alarm_channel";

    public static void createChannel(Context context) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pet Alarm",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Thông báo chuông báo thú cưng");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
