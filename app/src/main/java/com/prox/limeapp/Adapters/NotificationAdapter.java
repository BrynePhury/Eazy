package com.prox.limeapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prox.limeapp.ApprovalsActivity;
import com.prox.limeapp.Models.NotificationModel;
import com.prox.limeapp.MyClassActivity;
import com.prox.limeapp.Prevalent.Prevalent;
import com.prox.limeapp.ProfileActivity;
import com.prox.limeapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    List<NotificationModel> models;
    Context context;

    public NotificationAdapter(List<NotificationModel> models) {
        this.models = models;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_layout, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel model = models.get(position);

        holder.title.setText(model.getDescription());

        SimpleDateFormat format = new SimpleDateFormat("HH:mm, dd MMM");
        Date date = new Date(model.getNotificationTime());
        holder.time.setText(format.format(date));

        if (model.isSeen()) {
            holder.view.setVisibility(View.GONE);
        } else {
            holder.view.setVisibility(View.VISIBLE);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (model.getActivity().equals("Approvals")) {
                    context.startActivity(new Intent(context, ApprovalsActivity.class));

                } else if (model.getActivity().equals("My Class")) {
                    context.startActivity(new Intent(context, MyClassActivity.class));

                } else if (model.getActivity().equals("Profile")) {
                    context.startActivity(new Intent(context, ProfileActivity.class)
                            .putExtra("uid", Prevalent.CURRENT_USER.getUid()));
                }
                model.setSeen(true);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("Notifications");

                reference.child(model.getNid()).setValue(model);
            }
        });

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView time;
        View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.not_title);
            time = itemView.findViewById(R.id.not_time);
            view = itemView.findViewById(R.id.seen_view);

        }
    }
}
