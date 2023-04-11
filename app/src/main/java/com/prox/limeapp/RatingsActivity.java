package com.prox.limeapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Adapters.ChildRatingsAdapter;
import com.prox.limeapp.Models.ChildModel;
import com.prox.limeapp.Models.RatingModel;
import com.prox.limeapp.Prevalent.Prevalent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class RatingsActivity extends AppCompatActivity {

    CircleImageView dialogAvatar;
    EditText comment_et;
    RecyclerView comment_rcy;
    RatingBar ratingBar;
    LinearLayout layout;


    ChildModel childModel = null;

    DatabaseReference ratingReference;
    DatabaseReference childReference;

    String childId;

    int count;
    Float[] total = {Float.valueOf(0)};

    List<RatingModel> ratingModels = new ArrayList<>();

    ChildRatingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);


        comment_et = findViewById(R.id.comment_edit);
        comment_rcy = findViewById(R.id.rating_rcy);
        ratingBar = findViewById(R.id.ratingBar);
        layout = findViewById(R.id.layout);

        if (Prevalent.CURRENT_USER.getRole().equals("Guardian")) {
            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.VISIBLE);
        }


        adapter = new ChildRatingsAdapter(ratingModels);

        comment_rcy.setLayoutManager(new LinearLayoutManager(this));
        comment_rcy.setAdapter(adapter);

        childId = getIntent().getStringExtra("childId");

        ratingReference = FirebaseDatabase.getInstance().getReference().child("Ratings");
        childReference = FirebaseDatabase.getInstance().getReference().child("Kids");


        comment_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String ratingId = UUID.randomUUID().toString();

                RatingModel ratingModel = new RatingModel();
                ratingModel.setComment(textView.getText().toString());
                ratingModel.setUid(Prevalent.CURRENT_USER.getUid());
                ratingModel.setProfileUrl(Prevalent.CURRENT_USER.getProfileUrl());
                ratingModel.setRatingId(ratingId);
                ratingModel.setChildId(childId);
                ratingModel.setRating(ratingBar.getRating());
                ratingModel.setUserName(Prevalent.CURRENT_USER.getName());
                ratingModel.setTime(System.currentTimeMillis());

                textView.setText("");

                ratingReference.child(ratingId).setValue(ratingModel)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                childModel.setRatingCount(childModel.getRatingCount() + 1);


                                ratingModels.add(ratingModel);
                                adapter.notifyDataSetChanged();
                                float total = 0;
                                int count = ratingModels.size();
                                for (RatingModel model : ratingModels) {
                                    total = total + model.getRating();
                                }

                                float finalTotal = total;
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        float averageRate = finalTotal / count;
                                        childModel.setRating(averageRate);
                                        childReference.child(childId)
                                                .setValue(childModel)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(RatingsActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }, 800);
                            }
                        });
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ratingModels.clear();

        childReference.child(childId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childModel = snapshot.getValue(ChildModel.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ratingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RatingModel ratingModel = dataSnapshot.getValue(RatingModel.class);

                    if (ratingModel.getChildId().equals(childId)) {
                        ratingModels.add(ratingModel);
                        adapter.notifyDataSetChanged();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}