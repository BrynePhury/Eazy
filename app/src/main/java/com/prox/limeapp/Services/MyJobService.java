package com.prox.limeapp.Services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Models.NotificationModel;
import com.prox.limeapp.Prevalent.Prevalent;
import com.prox.limeapp.Utility.NotificationHelperClass;

public class MyJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Notifications");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NotificationModel model = dataSnapshot.getValue(NotificationModel.class);

                    if (Prevalent.CURRENT_USER != null) {
                        if (model.getNotificationTo() != null) {
                            if (model.getNotificationTo().equals(Prevalent.CURRENT_USER.getUid())) {
                                if (!model.isSeen()) {
                                    NotificationHelperClass.sendNotification(getApplicationContext(),
                                            model.getTitle(), model.getDescription());
                                }
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
