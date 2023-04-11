package com.prox.limeapp.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Adapters.PostAdapter;
import com.prox.limeapp.Models.PostModel;
import com.prox.limeapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProfileFragment extends Fragment {
    Context context;
    RecyclerView newsFeed;
    TextView newsFeedProgressBar;

    int limit = 2;
    int offset = 0;
    boolean isFromStart = true;
    PostAdapter postAdapter;
    List<PostModel> postModels = new ArrayList<>();

    String uid = "0";
    String current_state = "0";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        newsFeed = view.findViewById(R.id.newsFeed);
        newsFeedProgressBar = view.findViewById(R.id.newsFeedProgressBar);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        newsFeed.setLayoutManager(layoutManager);
        postAdapter = new PostAdapter(postModels, context);
        uid = getArguments().getString("uid", "0");
        current_state = getArguments().getString("current_state", "0");
        newsFeed.setAdapter(postAdapter);

        /*newsFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int passVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition();

                if (passVisibleItems + visibleItemCount >= totalItemCount) {
                    isFromStart = false;
                    newsFeedProgressBar.setVisibility(View.VISIBLE);
                    offset = offset + limit;
                    loadProfilePosts();
                }

            }
        });*/


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        isFromStart = true;
        offset = 0;
        loadProfilePosts();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        postModels.clear();
        postAdapter.notifyDataSetChanged();
    }

    private void loadProfilePosts() {
        postModels.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference().child("Posts");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        PostModel postModel = dataSnapshot.getValue(PostModel.class);
                        if (postModel.getPostUserId().equals(uid)) {
                            postModels.add(postModel);
                            Collections.reverse(postModels);
                            postAdapter.notifyDataSetChanged();
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
