package com.example.petcareapp.ui.user.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getUid();

            if (userId == null) return;

            db.collection("users")
                    .document(userId)
                    .collection("pets")
                    .get()
                    .addOnSuccessListener(pets -> {

                        for (var pet : pets) {

                            db.collection("users")
                                    .document(userId)
                                    .collection("pets")
                                    .document(pet.getId())
                                    .collection("alarms")
                                    .get()
                                    .addOnSuccessListener(alarms -> {

                                        for (var doc : alarms) {

                                            String time = doc.getString("time");
                                            String name = doc.getString("name");
                                            String desc = doc.getString("description");
                                            String type = doc.getString("type");
                                            String alarmId = doc.getString("id");

                                            if (time == null) continue;

                                            schedule(context, alarmId, name, desc, time, type);
                                        }
                                    });
                        }
                    });
        }
    }

    private void schedule(Context context, String alarmId, String name, String desc, String time, String type) {

        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("name", name);
        intent.putExtra("desc", desc);
        intent.putExtra("alarmId", alarmId);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                alarmId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pi
        );
    }

}
