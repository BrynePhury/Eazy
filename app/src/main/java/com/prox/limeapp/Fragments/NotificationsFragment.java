package com.prox.limeapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Adapters.NotificationAdapter;
import com.prox.limeapp.HomeActivity;
import com.prox.limeapp.Models.NotificationModel;
import com.prox.limeapp.Prevalent.Prevalent;
import com.prox.limeapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class NotificationsFragment extends Fragment {
    Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    RecyclerView recyclerView;
    TextView default_text;

    List<NotificationModel> models = new ArrayList<>();

    NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.notification_rcy);
        default_text = view.findViewById(R.id.default_textView);

        default_text.setVisibility(View.GONE);

        adapter = new NotificationAdapter(models);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        HomeActivity.bottomNavigationView.removeBadge(R.id.bottom_nav_notifications);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        DatabaseReference notificationRef =
                FirebaseDatabase.getInstance().getReference().child("Notifications");


        notificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                models.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NotificationModel notificationModel = dataSnapshot.getValue(NotificationModel.class);
                    assert notificationModel != null;
                    if (notificationModel.getNotificationTo() != null) {
                        if (!models.contains(notificationModel) &&
                                notificationModel.getNotificationTo().equals(Prevalent.CURRENT_USER.getUid())) {
                            models.add(notificationModel);
                        }
                    }

                }
                Collections.sort(models, new Comparator<NotificationModel>() {
                    @Override
                    public int compare(NotificationModel o1, NotificationModel o2) {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(o1.getNotificationTime());

                        Date o1Date = c.getTime();

                        c.setTimeInMillis(o2.getNotificationTime());

                        Date o2Date = c.getTime();

                        return o2Date.compareTo(o1Date);
                    }
                });
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
