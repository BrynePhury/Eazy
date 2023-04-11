package com.prox.limeapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prox.limeapp.Models.User;
import com.prox.limeapp.ProfileActivity;
import com.prox.limeapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    List<User> users;
    Context context;

    public ContactsAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contacts,parent,false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);
        Picasso.get().load(user.getProfileUrl())
                .placeholder(R.drawable.ic_blank_profile_picture)
                .into(holder.profileImg, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(user.getProfileUrl())
                                .placeholder(R.drawable.ic_blank_profile_picture)
                                .into(holder.profileImg);
                    }
                });
        holder.profileName.setText(user.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ProfileActivity.class).putExtra("uid",user.getUid()));
            }
        });
        if (user.getRole() != null){
            if (user.getRole().equals("Guardian")){
                holder.position_txt.setText("Guardian");

            } else if (user.getPosition() != null){
                holder.position_txt.setText(user.getPosition());

            } else {
                holder.position_txt.setText(user.getRole());

            }
        }

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImg;
        TextView profileName;
        TextView position_txt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImg = itemView.findViewById(R.id.activity_profile_single);
            profileName = itemView.findViewById(R.id.activity_title_single);
            position_txt = itemView.findViewById(R.id.position_txt);

        }
    }
}
