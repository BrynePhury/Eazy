package com.prox.limeapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MyApplication extends Application {
    public static final String NOTE_CHANNEL = "NotificationsChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();

    }

    private void createNotificationChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel taskChannel = new NotificationChannel(
                    NOTE_CHANNEL,
                    "Main Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            taskChannel.enableVibration(true);
            taskChannel.setDescription("This is the channel that all notifications come through");


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(taskChannel);
        }

    }
}
