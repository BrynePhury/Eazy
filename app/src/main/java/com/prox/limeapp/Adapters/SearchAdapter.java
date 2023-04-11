package com.prox.limeapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prox.limeapp.Models.User;
import com.prox.limeapp.ProfileActivity;
import com.prox.limeapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    Context context;
    List<User> users;

    public SearchAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);
        holder.userName.setText(user.getName());
        Picasso.get().load(user.getProfileUrl()).placeholder(R.drawable.ic_blank_profile_picture)
                .networkPolicy(NetworkPolicy.OFFLINE).into(holder.userImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(user.getProfileUrl())
                        .placeholder(R.drawable.ic_blank_profile_picture).into(holder.userImage);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ProfileActivity.class).putExtra("uid",user.getUid()));
            }
        });
        holder.position_txt.setText(user.getRole());

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView userImage;
        TextView userName;
        TextView position_txt;
        RelativeLayout topRel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            position_txt = itemView.findViewById(R.id.position_txt);
            topRel = itemView.findViewById(R.id.top_rel);

        }
    }
}
