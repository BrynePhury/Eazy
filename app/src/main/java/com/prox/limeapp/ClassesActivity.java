package com.prox.limeapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Adapters.ClassAdapter;
import com.prox.limeapp.Dialogs.AddClassDialog;
import com.prox.limeapp.Models.ClassModel;
import com.prox.limeapp.Prevalent.Prevalent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClassesActivity extends AppCompatActivity implements AddClassDialog.AddClassDialogListener {

    FloatingActionButton fab;
    TextView default_txt;
    RecyclerView recyclerView;
    Toolbar toolbar;

    AddClassDialog addClassDialog;

    ProgressDialog progressDialog;

    ClassAdapter adapter;
    List<ClassModel> classModels = new ArrayList<>();

    DatabaseReference classReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);

        fab = findViewById(R.id.fab);
        default_txt = findViewById(R.id.default_text);
        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);

        adapter = new ClassAdapter(classModels);

        classReference = FirebaseDatabase.getInstance().getReference().child("Classes");

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addClassDialog = new AddClassDialog();
                addClassDialog.show(getSupportFragmentManager(), "Add Class Dialog");

            }
        });

        checkAndUpdateLayout();
    }

    private void checkAndUpdateLayout() {
        if (Prevalent.CURRENT_USER.getSchoolId() != null) {
            if (Prevalent.CURRENT_USER.getSchoolId().equals("School")) {
                fab.setVisibility(View.GONE);
                default_txt.setVisibility(View.VISIBLE);
            } else {
                fab.setVisibility(View.VISIBLE);
                default_txt.setVisibility(View.GONE);

            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        classModels.clear();
        classReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ClassModel classModel = dataSnapshot.getValue(ClassModel.class);

                    if (classModel.getSchoolId().equals(Prevalent.CURRENT_USER.getSchoolId())){
                        classModels.add(classModel);
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
        addClassDialog.dismiss();
        progressDialog.show();

        String cid = UUID.randomUUID().toString();

        ClassModel model = new ClassModel();
        model.setClassId(cid);
        model.setSchoolId(Prevalent.CURRENT_USER.getSchoolId());
        model.setAdminId(Prevalent.CURRENT_USER.getUid());
        model.setClassName(name);

        classReference.child(cid)
                .setValue(model)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            classModels.add(model);
                            adapter.notifyDataSetChanged();

                            Toast.makeText(ClassesActivity.this, "Done!", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }
}