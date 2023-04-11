package com.prox.limeapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.prox.limeapp.Adapters.CommentAdapter;
import com.prox.limeapp.Models.CommentModel;
import com.prox.limeapp.Models.NotificationModel;
import com.prox.limeapp.Models.PostModel;
import com.prox.limeapp.Prevalent.Prevalent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    CircleImageView dialogAvatar;
    EditText comment_et;
    RecyclerView comment_rcy;
    ImageView sendImg;

    String postId;

    PostModel postModel = null;

    DatabaseReference commentReference;
    DatabaseReference postReference;

    CommentAdapter adapter;
    List<CommentModel> commentModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        dialogAvatar = findViewById(R.id.dialogAvatar);
        comment_et = findViewById(R.id.comment_edit);
        comment_rcy = findViewById(R.id.comment_rcy);
        sendImg = findViewById(R.id.sendImg);

        adapter = new CommentAdapter(commentModels);

        comment_rcy.setLayoutManager(new LinearLayoutManager(this));
        comment_rcy.setAdapter(adapter);

        postId = getIntent().getStringExtra("postId");

        commentReference = FirebaseDatabase.getInstance().getReference().child("Post Comments");
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");


        Picasso.get().load(Prevalent.CURRENT_USER.getProfileUrl())
                .placeholder(R.drawable.ic_blank_profile_picture)
                .into(dialogAvatar, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(Prevalent.CURRENT_USER.getProfileUrl())
                                .placeholder(R.drawable.ic_blank_profile_picture)
                                .into(dialogAvatar);
                    }
                });


        sendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comm = comment_et.getText().toString();
                if (comm.trim().isEmpty()) {
                    Toast.makeText(CommentsActivity.this, "Write Comment", Toast.LENGTH_SHORT).show();
                } else {
                    String commentId = UUID.randomUUID().toString();

                    CommentModel commentModel = new CommentModel();
                    commentModel.setComment(comm);
                    commentModel.setUid(Prevalent.CURRENT_USER.getUid());
                    commentModel.setProfileUrl(Prevalent.CURRENT_USER.getProfileUrl());
                    commentModel.setCommentId(commentId);
                    commentModel.setPostId(postId);
                    commentModel.setUserName(Prevalent.CURRENT_USER.getName());
                    commentModel.setTime(System.currentTimeMillis());

                    comment_et.getText().clear();

                    commentReference.child(commentId).setValue(commentModel)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    postModel.setHasComment(true);
                                    postModel.setCommentCount(postModel.getCommentCount() + 1);

                                    commentModels.add(commentModel);
                                    adapter.notifyDataSetChanged();

                                    postReference.child(postModel.getPostId())
                                            .setValue(postModel)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(CommentsActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                                    DatabaseReference notificationRef =
                                                            FirebaseDatabase.getInstance().getReference().child("Notifications");

                                                    String nid = UUID.randomUUID().toString();

                                                    if (!postModel.getPostUserId().equals(Prevalent.CURRENT_USER.getUid())) {
                                                        NotificationModel notificationModel = new NotificationModel();

                                                        notificationModel.setActivity("Profile");
                                                        notificationModel.setNid(nid);
                                                        notificationModel.setNotificationFrom(Prevalent.CURRENT_USER.getUid());
                                                        notificationModel.setNotificationTo(postModel.getPostUserId());
                                                        notificationModel.setSeen(false);
                                                        notificationModel.setTitle("Your post has a new comment");
                                                        notificationModel.setDescription(Prevalent.CURRENT_USER.getName() + " just commented your post");
                                                        notificationModel.setNotificationTime(System.currentTimeMillis());

                                                        notificationRef.child(nid).setValue(notificationModel);
                                                    }
                                                }
                                            });
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (postId != null) {
            postReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    postModel = snapshot.getValue(PostModel.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            commentModels.clear();

            commentReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CommentModel commentModel = dataSnapshot.getValue(CommentModel.class);

                        if (commentModel.getPostId().equals(postId)) {
                            commentModels.add(commentModel);
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
}