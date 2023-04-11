package com.prox.limeapp.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.prox.limeapp.Adapters.PostAdapter;
import com.prox.limeapp.HomeActivity;
import com.prox.limeapp.Models.PostModel;
import com.prox.limeapp.Prevalent.Prevalent;
import com.prox.limeapp.ProfileActivity;
import com.prox.limeapp.R;
import com.prox.limeapp.UploadActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsFeedFragment extends Fragment {
    Context context;
    RecyclerView newsFeed;
    TextView default_textView;

    int limit = 2;
    int offset = 0;
    boolean isFromStart = true;
    PostAdapter postAdapter;
    public static List<PostModel> postModels = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);

        default_textView = view.findViewById(R.id.default_textView);
        newsFeed = view.findViewById(R.id.newsFeed);
        EditText statusEdit = view.findViewById(R.id.status_edit);
        CircleImageView avatar = view.findViewById(R.id.avatar);
        CardView card = view.findViewById(R.id.card);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        newsFeed.setLayoutManager(layoutManager);
        postAdapter = new PostAdapter(postModels, context);
        newsFeed.setAdapter(postAdapter);

        statusEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(getContext())
                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                if (Prevalent.CURRENT_USER.getSchoolId() != null && !Prevalent.CURRENT_USER.getSchoolId().equals("School")) {
                                    if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
                                        if (Prevalent.CURRENT_USER.getState().equals("Approved")) {
                                            startActivity(new Intent(getContext(), UploadActivity.class));

                                        } else {
                                            Toast.makeText(getContext(), "Waiting for admin approval", Toast.LENGTH_SHORT).show();

                                        }
                                    } else {
                                        startActivity(new Intent(getContext(), UploadActivity.class));
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Register School First", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(getContext())
                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                if (Prevalent.CURRENT_USER.getSchoolId() != null && !Prevalent.CURRENT_USER.getSchoolId().equals("School")) {
                                    if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
                                        if (Prevalent.CURRENT_USER.getState().equals("Approved")) {
                                            startActivity(new Intent(getContext(), UploadActivity.class));

                                        } else {
                                            Toast.makeText(getContext(), "Waiting for admin approval", Toast.LENGTH_SHORT).show();

                                        }
                                    } else {
                                        startActivity(new Intent(getContext(), UploadActivity.class));
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Register School First", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ProfileActivity.class)
                        .putExtra("uid", Prevalent.CURRENT_USER.getUid()));
            }
        });

        Picasso.get()
                .load(Prevalent.CURRENT_USER.getProfileUrl()).into(avatar, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get()
                                .load(Prevalent.CURRENT_USER.getProfileUrl()).into(avatar);
                    }
                });

        if (Prevalent.CURRENT_USER.getRole().equals("Guardian")){
            card.setVisibility(View.GONE);
        }

        if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
            if (Prevalent.CURRENT_USER.getState().equals("Approved")) {
                loadProfilePosts();

            }
        } else {
            loadProfilePosts();

        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        isFromStart = true;
        offset = 0;
        if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
            if (Prevalent.CURRENT_USER.getState().equals("Approved")) {
                loadProfilePosts();

            }
        } else {
            loadProfilePosts();

        }

    }

    @Override
    public void onPause() {
        super.onPause();
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
                        if (postModel.getSchoolId() != null) {
                            if (postModel.getSchoolId().equals(Prevalent.CURRENT_USER.getSchoolId())) {
                                if (postModel.getPrivacy() == 0) {
                                    if (!containsPost(postModels, postModel.getPostId())) {
                                        postModels.add(postModel);
                                        default_textView.setVisibility(View.GONE);
                                    }
                                } else if (postModel.getPrivacy() == 1) {
                                    if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
                                        if (!containsPost(postModels, postModel.getPostId())) {
                                            postModels.add(postModel);
                                            default_textView.setVisibility(View.GONE);
                                        }
                                    }
                                } else if (postModel.getPrivacy() == 2) {
                                    if (postModel.getPostUserId().equals(Prevalent.CURRENT_USER.getUid())) {
                                        if (!containsPost(postModels, postModel.getPostId())) {
                                            postModels.add(postModel);
                                            default_textView.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                            if (postModel.getSchoolId().equals(Prevalent.CURRENT_USER.getSecondSchoolId())) {
                                if (postModel.getPrivacy() == 0) {
                                    if (!containsPost(postModels, postModel.getPostId())) {
                                        postModels.add(postModel);
                                        default_textView.setVisibility(View.GONE);
                                    }
                                } else if (postModel.getPrivacy() == 1) {
                                    if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
                                        if (!containsPost(postModels, postModel.getPostId())) {
                                            postModels.add(postModel);
                                            default_textView.setVisibility(View.GONE);
                                        }
                                    }
                                } else if (postModel.getPrivacy() == 2) {
                                    if (postModel.getPostUserId().equals(Prevalent.CURRENT_USER.getUid())) {
                                        if (!containsPost(postModels, postModel.getPostId())) {
                                            postModels.add(postModel);
                                            default_textView.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                            if (postModel.getSchoolId().equals(Prevalent.CURRENT_USER.getThirdSchoolId())) {
                                if (postModel.getPrivacy() == 0) {
                                    if (!containsPost(postModels, postModel.getPostId())) {
                                        postModels.add(postModel);
                                        default_textView.setVisibility(View.GONE);
                                    }
                                } else if (postModel.getPrivacy() == 1) {
                                    if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
                                        if (!containsPost(postModels, postModel.getPostId())) {
                                            postModels.add(postModel);
                                            default_textView.setVisibility(View.GONE);
                                        }
                                    }
                                } else if (postModel.getPrivacy() == 2) {
                                    if (postModel.getPostUserId().equals(Prevalent.CURRENT_USER.getUid())) {
                                        if (!containsPost(postModels, postModel.getPostId())) {
                                            postModels.add(postModel);
                                            default_textView.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                            if (postModel.getSchoolId().equals(Prevalent.CURRENT_USER.getFourthSchoolId())) {
                                if (postModel.getPrivacy() == 0) {
                                    if (!containsPost(postModels, postModel.getPostId())) {
                                        postModels.add(postModel);
                                        default_textView.setVisibility(View.GONE);
                                    }
                                } else if (postModel.getPrivacy() == 1) {
                                    if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
                                        if (!containsPost(postModels, postModel.getPostId())) {
                                            postModels.add(postModel);
                                            default_textView.setVisibility(View.GONE);
                                        }
                                    }
                                } else if (postModel.getPrivacy() == 2) {
                                    if (postModel.getPostUserId().equals(Prevalent.CURRENT_USER.getUid())) {
                                        if (!containsPost(postModels, postModel.getPostId())) {
                                            postModels.add(postModel);
                                            default_textView.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                            if (postModel.getSchoolId().equals(Prevalent.CURRENT_USER.getFifthSchoolId())) {
                                if (postModel.getPrivacy() == 0) {
                                    if (!containsPost(postModels, postModel.getPostId())) {
                                        postModels.add(postModel);
                                        default_textView.setVisibility(View.GONE);
                                    }
                                } else if (postModel.getPrivacy() == 1) {
                                    if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
                                        if (!containsPost(postModels, postModel.getPostId())) {
                                            postModels.add(postModel);
                                            default_textView.setVisibility(View.GONE);
                                        }
                                    }
                                } else if (postModel.getPrivacy() == 2) {
                                    if (postModel.getPostUserId().equals(Prevalent.CURRENT_USER.getUid())) {
                                        if (!containsPost(postModels, postModel.getPostId())) {
                                            postModels.add(postModel);
                                            default_textView.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Collections.sort(postModels, new Comparator<PostModel>() {
                        @Override
                        public int compare(PostModel o1, PostModel o2) {
                            Calendar c = Calendar.getInstance();
                            c.setTimeInMillis(o1.getTimeStamp());

                            Date o1Date = c.getTime();

                            c.setTimeInMillis(o2.getTimeStamp());

                            Date o2Date = c.getTime();

                            return o2Date.compareTo(o1Date);
                        }
                    });
                    postAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean containsPost(List<PostModel> list, String id) {
        return list.stream().map(PostModel::getPostId).anyMatch(id::equals);

    }


}
