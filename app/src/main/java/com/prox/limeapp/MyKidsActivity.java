package com.prox.limeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Adapters.KidsAdapter;
import com.prox.limeapp.Models.ChildModel;
import com.prox.limeapp.Prevalent.Prevalent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyKidsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Toolbar toolbar;
    FloatingActionButton fab;

    DatabaseReference reference;

    List<ChildModel> childModels;

    KidsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_kids);

        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        reference = FirebaseDatabase.getInstance().getReference().child("Kids");

        childModels = new ArrayList<>();
        adapter = new KidsAdapter(childModels);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MyKidsActivity.this, AddChildActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        childModels.clear();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChildModel model = dataSnapshot.getValue(ChildModel.class);

                    if (model.getGuardianId() != null) {

                        if (model.getGuardianId().equals(Prevalent.CURRENT_USER.getUid())) {
                            childModels.add(model);
                            adapter.notifyDataSetChanged();

                        } else if (model.getSecondGuardianId() != null) {
                            
                            if (model.getSecondGuardianId().equals(Prevalent.CURRENT_USER.getUid())) {
                                childModels.add(model);
                                adapter.notifyDataSetChanged();

                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}