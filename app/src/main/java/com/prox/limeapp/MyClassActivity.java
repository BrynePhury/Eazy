package com.prox.limeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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

public class MyClassActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Toolbar toolbar;
    FloatingActionButton fab;

    KidsAdapter adapter;
    List<ChildModel> childModels;

    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_class);

        reference = FirebaseDatabase.getInstance().getReference().child("Kids");

        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);


        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        childModels = new ArrayList<>();
        adapter = new KidsAdapter(childModels);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Prevalent.CURRENT_USER.getClassId() != null){
                startActivity(new Intent(MyClassActivity.this, KidsSearchActivity.class));
            }else {
                Toast.makeText(MyClassActivity.this, "Class Not Assigned", Toast.LENGTH_SHORT).show();
                }
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

                    if (model.getClassId() != null
                            && Prevalent.CURRENT_USER.getClassId() != null) {

                        if (model.getClassId().equals(Prevalent.CURRENT_USER.getClassId())) {
                            childModels.add(model);
                            adapter.notifyDataSetChanged();

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