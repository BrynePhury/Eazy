package com.prox.limeapp.Utility;



import static com.prox.limeapp.MyApplication.NOTE_CHANNEL;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.prox.limeapp.HomeActivity;
import com.prox.limeapp.R;


public class NotificationHelperClass {

    public static void sendNotification(Context context, String title,String description){

        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra("isIntent",true);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,20,intent,PendingIntent.FLAG_CANCEL_CURRENT);

        Notification taskNotification = new NotificationCompat.Builder(context, NOTE_CHANNEL)
                .setSmallIcon(R.drawable.ic_pen)
                .setContentTitle(title)
                .setContentText(description)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        manager.notify(101,taskNotification);

    }

}
