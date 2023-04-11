package com.prox.limeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Adapters.PositionAdapter;
import com.prox.limeapp.Dialogs.AddClassDialog;
import com.prox.limeapp.Dialogs.AddPositionDialog;
import com.prox.limeapp.Models.PositionModel;
import com.prox.limeapp.Prevalent.Prevalent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class PositionsActivity extends AppCompatActivity implements AddPositionDialog.AddPositionDialogListener {

    Toolbar toolbar;
    TextView default_txt,teacher_name,position;
    CircleImageView teacher_img;
    RecyclerView recyclerView;
    FloatingActionButton fab;

    PositionAdapter adapter;
    List<PositionModel> models = new ArrayList<>();

    DatabaseReference reference;

    AddPositionDialog addPositionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_positions);

        toolbar = findViewById(R.id.toolbar);
        default_txt = findViewById(R.id.default_text);
        teacher_img = findViewById(R.id.teacher_img);
        teacher_name = findViewById(R.id.teacher_name);
        position = findViewById(R.id.position_txt);
        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);

        reference = FirebaseDatabase.getInstance().getReference()
                .child("Positions");

        adapter = new PositionAdapter(models);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        loadUserInfo();

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        checkAndUpdateLayout();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addPositionDialog = new AddPositionDialog();
                addPositionDialog.show(getSupportFragmentManager(), "Add Class Dialog");

            }
        });
    }

    private void loadUserInfo() {
        teacher_name.setText(Prevalent.CURRENT_USER.getName());
        Picasso.get().load(Prevalent.CURRENT_USER.getProfileUrl())
                .placeholder(R.drawable.ic_blank_profile_picture)
                .into(teacher_img, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(Prevalent.CURRENT_USER.getProfileUrl())
                        .placeholder(R.drawable.ic_blank_profile_picture)
                        .into(teacher_img);
            }
        });
    }

    private void checkAndUpdateLayout() {
        if (Prevalent.CURRENT_USER.getSchoolId() != null){
            if (Prevalent.CURRENT_USER.getSchoolId().equals("School")) {
                default_txt.setVisibility(View.VISIBLE);
                teacher_name.setVisibility(View.GONE);
                teacher_img.setVisibility(View.GONE);
                position.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);

            } else {
                default_txt.setVisibility(View.GONE);
                teacher_name.setVisibility(View.VISIBLE);
                teacher_img.setVisibility(View.VISIBLE);
                position.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        models.clear();

        reference.child(Prevalent.SCHOOL.getSchoolId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        PositionModel positionModel = dataSnapshot.getValue(PositionModel.class);

                        models.add(positionModel);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void applyTexts(String name) {

        String positionId = UUID.randomUUID().toString();

        PositionModel positionModel = new PositionModel();
        positionModel.setPosition(name);
        positionModel.setPositionId(positionId);
        positionModel.setSchoolId(Prevalent.SCHOOL.getSchoolId());


        reference.child(Prevalent.SCHOOL.getSchoolId())
                .child(positionId).setValue(positionModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    models.add(positionModel);
                    adapter.notifyDataSetChanged();

                }

            }
        });
    }
}