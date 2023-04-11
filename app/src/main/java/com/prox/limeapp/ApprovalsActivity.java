package com.prox.limeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Adapters.ApprovalAdapter;
import com.prox.limeapp.Models.Approval;
import com.prox.limeapp.Prevalent.Prevalent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApprovalsActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView default_txt;
    RecyclerView recyclerView;

    public static List<Approval> approvals = new ArrayList<>();
    ApprovalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approvals);

        toolbar = findViewById(R.id.toolbar);
        default_txt = findViewById(R.id.default_text);
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new ApprovalAdapter(approvals);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        checkAndUpdateLayout();

    }

    @Override
    protected void onStart() {
        super.onStart();
        approvals.clear();

        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference().child("Approvals");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Approval approval = dataSnapshot.getValue(Approval.class);

                    if (approval.getSchoolId().equals(Prevalent.CURRENT_USER.getSchoolId())){
                        if (!approval.isSeen()){
                            approvals.add(approval);
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

    private void checkAndUpdateLayout() {
        if (Prevalent.CURRENT_USER.getSchoolId() != null){
            if (Prevalent.CURRENT_USER.getSchoolId().equals("School")) {
                default_txt.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                default_txt.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

            }
        }

    }
}