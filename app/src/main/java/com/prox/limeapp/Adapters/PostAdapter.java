package com.prox.limeapp.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.CommentsActivity;
import com.prox.limeapp.Fragments.NewsFeedFragment;
import com.prox.limeapp.Models.NotificationModel;
import com.prox.limeapp.Models.PostLikeModel;
import com.prox.limeapp.Models.PostModel;
import com.prox.limeapp.Models.SchoolModel;
import com.prox.limeapp.Models.User;
import com.prox.limeapp.PhotoViewActivity;
import com.prox.limeapp.Prevalent.Prevalent;
import com.prox.limeapp.ProfileActivity;
import com.prox.limeapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    List<PostModel> posts;
    Context context;
    boolean liked = false;
    DatabaseReference likeReference = FirebaseDatabase.getInstance().getReference().child("Post Likes");
    DatabaseReference postReference = FirebaseDatabase.getInstance().getReference().child("Posts");

    public PostAdapter(List<PostModel> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PostModel postModel = posts.get(position);
        final int pos = position;

        System.out.println(postModel.getPost());

        if (Prevalent.CURRENT_USER.getUid().equals(postModel.getPostUserId())) {
            holder.more_img.setVisibility(View.VISIBLE);
        } else if (Prevalent.CURRENT_USER.getRole().equals("Admin")) {
            if (postModel.getSchoolId().equals(Prevalent.CURRENT_USER.getSchoolId())) {
                holder.more_img.setVisibility(View.VISIBLE);

            }

        } else {
            holder.more_img.setVisibility(View.GONE);
        }

        holder.peopleName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ProfileActivity.class)
                        .putExtra("uid", postModel.getPostUserId()));
            }
        });

        checkIfLiked(postModel, holder.likeImg);

        if (postModel.getLikeCount() < 1) {
            holder.likeText.setText("0 likes");
        } else if (postModel.getLikeCount() == 1) {
            holder.likeText.setText(postModel.getLikeCount() + " like");
        } else if (postModel.getLikeCount() > 1) {
            holder.likeText.setText(postModel.getLikeCount() + " likes");
        }

        if (postModel.getCommentCount() < 1) {
            holder.commentText.setText("0 Comments");
        } else if (postModel.getCommentCount() == 1) {
            holder.commentText.setText(postModel.getCommentCount() + " Comment");
        } else if (postModel.getCommentCount() > 1) {
            holder.commentText.setText(postModel.getCommentCount() + " Comments");
        }

        if (postModel.getPost() != null && postModel.getPost().length() > 0) {
            holder.post.setText(postModel.getPost());

            holder.post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.post.getMaxLines() == 3) {
                        holder.post.setMaxLines(100);

                    } else {
                        holder.post.setMaxLines(3);
                    }
                }
            });
        } else {
            holder.post.setVisibility(View.GONE);
        }
        holder.peopleName.setText(postModel.getName());

        if (postModel.getPrivacy() == 0) {
            holder.privacyLevel.setImageResource(R.drawable.ic_public);
        } else if (postModel.getPrivacy() == 1) {
            holder.privacyLevel.setImageResource(R.drawable.ic_only_me);
        } else if (postModel.getPrivacy() == 2) {
            holder.privacyLevel.setImageResource(R.drawable.ic_teachers);
        }

        //Getting Images
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Guardians");

        reference.child(postModel.getPostUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (!user.getRole().equals("Admin")) {
                        Picasso.get().load(user.getProfileUrl())
                                .placeholder(R.drawable.ic_blank_profile_picture).into(holder.peopleImg, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(user.getProfileUrl())
                                                .placeholder(R.drawable.ic_blank_profile_picture).into(holder.peopleImg);
                                    }
                                });

                    } else {
                        DatabaseReference schoolRef = FirebaseDatabase.getInstance()
                                .getReference().child("Schools");

                        schoolRef.child(user.getSchoolId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    SchoolModel schoolModel = snapshot.getValue(SchoolModel.class);

                                    Picasso.get().load(schoolModel.getSchoolImg())
                                            .placeholder(R.drawable.ic_blank_profile_picture).into(holder.peopleImg, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Picasso.get().load(schoolModel.getSchoolImg())
                                                            .placeholder(R.drawable.ic_blank_profile_picture).into(holder.peopleImg);
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (postModel.getStatusImage() != null &&
                !postModel.getStatusImage().isEmpty() &&
                !postModel.getStatusImage().equals("0")) {
            Picasso.get().load(postModel.getStatusImage())
                    .placeholder(R.drawable.ic_blank_profile_picture).into(holder.postImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(postModel.getStatusImage())
                                    .placeholder(R.drawable.ic_blank_profile_picture).into(holder.postImage);
                        }
                    });

            holder.postImage.setVisibility(View.VISIBLE);
        } else {
            holder.postImage.setVisibility(View.GONE);

        }
        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, PhotoViewActivity.class)
                        .putExtra("image", postModel.getStatusImage()));
            }
        });


        SimpleDateFormat format = new SimpleDateFormat("HH:mm, dd MMM");
        Date date = new Date(postModel.getStatusTime());
        holder.date.setText(format.format(date));

        holder.likeSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (liked) {
                    liked = false;
                    holder.likeImg.setColorFilter(ContextCompat.getColor(context, R.color.teal_200),
                            PorterDuff.Mode.SRC_IN);
                    holder.likeText.setTextColor(ContextCompat.getColor(context, R.color.teal_200));
                    postModel.setLikeCount(postModel.getLikeCount() - 1);
                    likeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                PostLikeModel likeModel = dataSnapshot.getValue(PostLikeModel.class);
                                assert likeModel != null;
                                if (likeModel.getPostId().equals(postModel.getPostId())) {
                                    if (likeModel.getUid().equals(Prevalent.CURRENT_USER.getUid())) {
                                        likeReference.child(likeModel.getLikeId()).removeValue();

                                        postReference.child(postModel.getPostId())
                                                .setValue(postModel);

                                        if (postModel.getLikeCount() < 1) {
                                            holder.likeText.setText("0 likes");
                                        } else if (postModel.getLikeCount() == 1) {
                                            holder.likeText.setText(postModel.getLikeCount() + "like");
                                        } else if (postModel.getLikeCount() > 1) {
                                            holder.likeText.setText(postModel.getLikeCount() + "likes");
                                        }

                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {
                    liked = true;
                    holder.likeImg.setColorFilter(ContextCompat.getColor(context, R.color.gold), PorterDuff.Mode.SRC_IN);
                    holder.likeText.setTextColor(ContextCompat.getColor(context, R.color.gold));

                    String likeId = UUID.randomUUID().toString();
                    PostLikeModel likeModel = new PostLikeModel();
                    likeModel.setPostId(postModel.getPostId());
                    likeModel.setLikeId(likeId);
                    likeModel.setUid(Prevalent.CURRENT_USER.getUid());

                    likeReference.child(likeId).setValue(likeModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (postModel.isHasLike()) {
                                postModel.setLikeCount(postModel.getLikeCount() + 1);
                            } else {
                                postModel.setHasLike(true);
                                postModel.setLikeCount(1);
                            }
                            if (postModel.getLikeCount() < 1) {
                                holder.likeText.setText("0 likes");
                            } else if (postModel.getLikeCount() == 1) {
                                holder.likeText.setText(postModel.getLikeCount() + " like");
                            } else if (postModel.getLikeCount() > 1) {
                                holder.likeText.setText(postModel.getLikeCount() + " likes");
                            }
                            postReference.child(postModel.getPostId()).setValue(postModel)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "Liked", Toast.LENGTH_SHORT).show();
                                            if (!postModel.getPostUserId().equals(Prevalent.CURRENT_USER.getUid())) {
                                                DatabaseReference notificationRef =
                                                        FirebaseDatabase.getInstance().getReference().child("Notifications");

                                                String nid = UUID.randomUUID().toString();

                                                NotificationModel notificationModel = new NotificationModel();

                                                notificationModel.setActivity("Profile");
                                                notificationModel.setNid(nid);
                                                notificationModel.setNotificationFrom(Prevalent.CURRENT_USER.getUid());
                                                notificationModel.setNotificationTo(postModel.getPostUserId());
                                                notificationModel.setSeen(false);
                                                notificationModel.setTitle("Your post has a new like");
                                                notificationModel.setDescription(Prevalent.CURRENT_USER.getName() + " just liked your post");
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
        holder.commentSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, CommentsActivity.class).putExtra("postId", postModel.getPostId()));
            }
        });

        holder.more_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                CharSequence[] options = new CharSequence[]{"Delete Post"};

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setMessage("Deleting...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        postReference.child(postModel.getPostId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                NewsFeedFragment.postModels.remove(postModel);
                                notifyItemRemoved(pos);
                            }
                        });
                    }
                });

                builder.show();
            }
        });

    }

    private void checkIfLiked(PostModel postModel, ImageView likeImg) {
        likeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PostLikeModel likeModel = dataSnapshot.getValue(PostLikeModel.class);
                    assert likeModel != null;
                    if (likeModel.getPostId().equals(postModel.getPostId())) {
                        if (likeModel.getUid().equals(Prevalent.CURRENT_USER.getUid())) {
                            liked = true;
                            likeImg.setColorFilter(ContextCompat.getColor(context, R.color.gold), PorterDuff.Mode.SRC_IN);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView peopleImg;
        TextView peopleName;
        TextView date;
        ImageView privacyLevel;
        TextView post;
        ImageView postImage;
        ImageView likeImg;
        TextView likeText;
        ImageView commentImg;
        TextView commentText;
        LinearLayout likeSection;
        LinearLayout commentSection;
        ImageView more_img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            peopleImg = itemView.findViewById(R.id.people_image);
            peopleName = itemView.findViewById(R.id.people_name);
            date = itemView.findViewById(R.id.date);
            privacyLevel = itemView.findViewById(R.id.privacy_img);
            post = itemView.findViewById(R.id.post);
            postImage = itemView.findViewById(R.id.status_image);
            likeImg = itemView.findViewById(R.id.like_img);
            likeText = itemView.findViewById(R.id.like_txt);
            commentImg = itemView.findViewById(R.id.comment_img);
            commentText = itemView.findViewById(R.id.comment_txt);
            likeSection = itemView.findViewById(R.id.like_section);
            commentSection = itemView.findViewById(R.id.comment_section);
            more_img = itemView.findViewById(R.id.more_img);

        }
    }
}
