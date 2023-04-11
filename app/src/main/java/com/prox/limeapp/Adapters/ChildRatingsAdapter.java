package com.prox.limeapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prox.limeapp.Models.RatingModel;
import com.prox.limeapp.R;

import java.util.List;

public class ChildRatingsAdapter extends RecyclerView.Adapter<ChildRatingsAdapter.ViewHolder> {

    List<RatingModel> ratingModels;
    Context context;

    public ChildRatingsAdapter(List<RatingModel> ratingModels) {
        this.ratingModels = ratingModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_rating,parent,false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RatingModel ratingModel = ratingModels.get(position);

        holder.comment.setText(ratingModel.getComment());
        holder.name.setText(ratingModel.getUserName());
        holder.ratingBar.setRating(ratingModel.getRating());

    }

    @Override
    public int getItemCount() {
        return ratingModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView comment;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            comment = itemView.findViewById(R.id.comment);
            ratingBar = itemView.findViewById(R.id.ratingBar);


        }
    }
}
