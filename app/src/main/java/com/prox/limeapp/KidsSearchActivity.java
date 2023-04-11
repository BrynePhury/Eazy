package com.prox.limeapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class KidsSearchActivity extends AppCompatActivity {

    RecyclerView searchRecycler;
    Toolbar toolbar;
    KidsAdapter searchAdapter;
    List<ChildModel> children = new ArrayList<>();

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kids_search);

        searchRecycler = findViewById(R.id.search_recycler);
        toolbar = findViewById(R.id.toolbar);

        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Kids");

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        searchAdapter = new KidsAdapter(children);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(KidsSearchActivity.this);
        searchRecycler.setLayoutManager(layoutManager);
        searchRecycler.setAdapter(searchAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_view, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search1).getActionView();

        searchView.setIconified(false);
        ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text))
                .setTextColor(getResources().getColor(R.color.teal_200));
        ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text))
                .setHintTextColor(getResources().getColor(R.color.teal_200));
        ((ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn))
                .setImageResource(R.drawable.ic_close);
        searchView.setQueryHint("Search Kids ");
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFromDb(query, true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    searchFromDb(newText, false);
                } else {
                    children.clear();
                    searchAdapter.notifyDataSetChanged();
                }

                return true;
            }
        });
        return true;
    }

    private void searchFromDb(String query, boolean b) {
        children.clear();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                children.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    ChildModel child = dataSnapshot.getValue(ChildModel.class);
                    if (child.getName() != null) {
                        if (child.getSchoolId().equals(Prevalent.CURRENT_USER.getSchoolId())) {
                            if (child.getName().contains(query)) {
                                children.add(child);
                                searchAdapter.notifyDataSetChanged();

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