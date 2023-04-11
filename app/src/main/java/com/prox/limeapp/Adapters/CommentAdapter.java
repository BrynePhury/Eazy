package com.prox.limeapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prox.limeapp.Models.CommentModel;
import com.prox.limeapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    List<CommentModel> commentModels;
    Context context;

    public CommentAdapter(List<CommentModel> commentModels) {
        this.commentModels = commentModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment,parent,false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CommentModel commentModel = commentModels.get(position);

        holder.usersName.setText(commentModel.getUserName());
        holder.comment.setText(commentModel.getComment());
        Picasso.get().load(commentModel.getProfileUrl())
                .placeholder(R.drawable.ic_blank_profile_picture)
                .into(holder.profileImg, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(commentModel.getProfileUrl())
                                .placeholder(R.drawable.ic_blank_profile_picture)
                                .into(holder.profileImg);

                    }
                });

        SimpleDateFormat format = new SimpleDateFormat("HH:mm, dd MMM");
        Date date = new Date(commentModel.getTime());
        holder.time.setText(format.format(date));


    }

    @Override
    public int getItemCount() {
        return commentModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImg;
        TextView usersName;
        TextView comment;
        TextView time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImg = itemView.findViewById(R.id.dialogAvatar);
            usersName = itemView.findViewById(R.id.comment_name);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.comment_date);
        }
    }
}
