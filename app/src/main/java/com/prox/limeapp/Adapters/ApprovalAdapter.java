package com.prox.limeapp.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.KidsProfileActivity;
import com.prox.limeapp.Models.Approval;
import com.prox.limeapp.Models.ChildModel;
import com.prox.limeapp.Models.User;
import com.prox.limeapp.ProfileActivity;
import com.prox.limeapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ApprovalAdapter extends RecyclerView.Adapter<ApprovalAdapter.ViewHolder> {

    List<Approval> approvals;
    Context context;

    ProgressDialog progressDialog;

    public ApprovalAdapter(List<Approval> approvals) {
        this.approvals = approvals;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contacts, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Approval approval = approvals.get(position);
        final int p = position;

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        holder.name.setText(approval.getUserName());
        Picasso.get().load(approval.getProfileImg())
                .placeholder(R.drawable.ic_blank_profile_picture)
                .into(holder.profile_img, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(approval.getProfileImg())
                                .placeholder(R.drawable.ic_blank_profile_picture)
                                .into(holder.profile_img);
                    }
                });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CharSequence options[] = new CharSequence[]{"Approve", "Disapprove",};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        progressDialog.show();
                        if (position == 0) {

                            if (approval.getPosition().equals("Pupil")) {
                                DatabaseReference databaseReference =
                                        FirebaseDatabase.getInstance().getReference().child("Kids");

                                databaseReference.child(approval.getUserId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                ChildModel model = snapshot.getValue(ChildModel.class);
                                                assert model != null;
                                                model.setStatus("Approved");
                                                databaseReference.child(approval.getUserId())
                                                        .setValue(model)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                                                            .child("Users").child("Guardians");

                                                                    reference.child(approval.getParentId())
                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                    User user = snapshot.getValue(User.class);

                                                                                    if (user.getSchoolId() != null) {
                                                                                        if (user.getSchoolId().equals("School")) {
                                                                                            user.setSchoolId(model.getSchoolId());

                                                                                        } else if (user.getSecondSchoolId() == null) {
                                                                                            user.setSecondSchoolId(model.getSchoolId());

                                                                                        } else if (user.getThirdSchoolId() == null) {
                                                                                            user.setThirdSchoolId(model.getSchoolId());

                                                                                        } else if (user.getFourthSchoolId() == null) {
                                                                                            user.setFourthSchoolId(model.getSchoolId());


                                                                                        } else if (user.getFifthSchoolId() == null) {
                                                                                            user.setFifthSchoolId(model.getSchoolId());
                                                                                            user.setChecker(1);

                                                                                        } else {
                                                                                            if (user.getChecker() == 1) {
                                                                                                user.setSchoolId(model.getSchoolId());
                                                                                                user.setChecker(2);

                                                                                            } else if (user.getChecker() == 2) {
                                                                                                user.setSecondSchoolId(model.getSchoolId());
                                                                                                user.setChecker(3);

                                                                                            } else if (user.getChecker() == 3) {
                                                                                                user.setThirdSchoolId(model.getSchoolId());
                                                                                                user.setChecker(4);

                                                                                            } else if (user.getChecker() == 4) {
                                                                                                user.setFourthSchoolId(model.getSchoolId());
                                                                                                user.setChecker(5);

                                                                                            } else if (user.getChecker() == 5) {
                                                                                                user.setFifthSchoolId(model.getSchoolId());
                                                                                                user.setChecker(1);

                                                                                            }
                                                                                        }
                                                                                    } else {
                                                                                        user.setSchoolId(model.getSchoolId());

                                                                                    }

                                                                                    reference.child(approval.getParentId())
                                                                                            .setValue(user)
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        DatabaseReference databaseReference =
                                                                                                                FirebaseDatabase.getInstance().getReference().child("Approvals");

                                                                                                        approval.setSeen(true);
                                                                                                        databaseReference.child(approval.getAid())
                                                                                                                .setValue(approval)
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        if (task.isSuccessful()) {
                                                                                                                            try {
                                                                                                                                progressDialog.dismiss();
                                                                                                                                approvals.remove(p);
                                                                                                                                notifyItemRemoved(p);
                                                                                                                            } catch (Exception e) {
                                                                                                                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

                                                                                                                            }
                                                                                                                        }

                                                                                                                    }
                                                                                                                });


                                                                                                    }

                                                                                                }
                                                                                            });


                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                }
                                                                            });

                                                                }

                                                            }
                                                        });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                            } else if (approval.getPosition().equals("Teacher")) {

                                DatabaseReference databaseReference =
                                        FirebaseDatabase.getInstance().getReference().child("Users").child("Guardians");

                                databaseReference.child(approval.getUserId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                User user = snapshot.getValue(User.class);
                                                assert user != null;
                                                user.setState("Approved");
                                                databaseReference.child(approval.getUserId())
                                                        .setValue(user)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {
                                                                    DatabaseReference databaseReference =
                                                                            FirebaseDatabase.getInstance().getReference().child("Approvals");

                                                                    approval.setSeen(true);
                                                                    databaseReference.child(approval.getAid())
                                                                            .setValue(approval)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        try {
                                                                                            progressDialog.dismiss();
                                                                                            approvals.remove(p);
                                                                                            notifyItemRemoved(p);
                                                                                            Toast.makeText(context, "Done!", Toast.LENGTH_SHORT).show();
                                                                                        } catch (Exception e) {
                                                                                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

                                                                                        }
                                                                                    }

                                                                                }
                                                                            });
                                                                } else {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                                }


                                                            }
                                                        });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                            }
                        } else if (position == 1) {
                            DatabaseReference databaseReference =
                                    FirebaseDatabase.getInstance().getReference().child("Approvals");

                            approval.setSeen(true);
                            databaseReference.child(approval.getAid())
                                    .setValue(approval)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                try {
                                                    progressDialog.dismiss();
                                                    approvals.remove(p);
                                                    notifyItemRemoved(p);
                                                } catch (Exception e) {
                                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

                                                }
                                            }

                                        }
                                    });
                        }
                    }
                });
                builder.show();

                return false;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (approval.getPosition().equals("Teacher")){
                    context.startActivity(new Intent(context, ProfileActivity.class)
                            .putExtra("uid",approval.getUserId()));
                } else if (approval.getPosition().equals("Pupil")){
                    context.startActivity(new Intent(context, KidsProfileActivity.class)
                            .putExtra("childId",approval.getUserId()));
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return approvals.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile_img;
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_img = itemView.findViewById(R.id.activity_profile_single);
            name = itemView.findViewById(R.id.activity_title_single);

        }

    }
}
