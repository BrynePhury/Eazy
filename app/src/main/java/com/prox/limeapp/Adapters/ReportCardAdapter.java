package com.prox.limeapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prox.limeapp.Models.ReportCardModel;
import com.prox.limeapp.PhotoViewActivity;
import com.prox.limeapp.R;

import org.w3c.dom.Text;

import java.util.List;

public class ReportCardAdapter extends RecyclerView.Adapter<ReportCardAdapter.ViewHolder> {

    List<ReportCardModel> reportCardModels;
    Context context;

    public ReportCardAdapter(List<ReportCardModel> reportCardModels) {
        this.reportCardModels = reportCardModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_card, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ReportCardModel model = reportCardModels.get(position);

        holder.name.setText(model.getChildName());
        holder.termName.setText(model.getTermInfo());
        holder.marks.setText(model.getMarks());

        holder.viewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, PhotoViewActivity.class)
                        .putExtra("image",model.getCardImgUrl()));
            }
        });


    }

    @Override
    public int getItemCount() {
        return reportCardModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView marks;
        TextView viewCard;
        TextView termName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.report_card_name);
            marks = itemView.findViewById(R.id.marks);
            viewCard = itemView.findViewById(R.id.view_txt);
            termName = itemView.findViewById(R.id.termName);



        }

    }
}
