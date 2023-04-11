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
import com.prox.limeapp.AddChildActivity;
import com.prox.limeapp.KidsProfileActivity;
import com.prox.limeapp.Models.ChildModel;
import com.prox.limeapp.Models.ClassModel;
import com.prox.limeapp.Models.NotificationModel;
import com.prox.limeapp.Models.User;
import com.prox.limeapp.Prevalent.Prevalent;
import com.prox.limeapp.ProfileActivity;
import com.prox.limeapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class KidsAdapter extends RecyclerView.Adapter<KidsAdapter.ViewHolder> {

    List<ChildModel> childModels;
    Context context;

    ProgressDialog progressDialog;
    DatabaseReference reference;


    public KidsAdapter(List<ChildModel> childModels) {
        this.childModels = childModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child, parent, false);
        context = parent.getContext();

        reference = FirebaseDatabase.getInstance().getReference().child("Kids");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int p = position;

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        final ChildModel childModel = childModels.get(position);
        Picasso.get().load(childModel.getProfileUrl())
                .placeholder(R.drawable.ic_blank_profile_picture)
                .into(holder.profileImg, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(childModel.getProfileUrl())
                                .placeholder(R.drawable.ic_blank_profile_picture)
                                .into(holder.profileImg);
                    }
                });
        holder.profileName.setText(childModel.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (childModel.isApproved() || Prevalent.CURRENT_USER.getRole().equals("Guardian")) {
                    context.startActivity(new Intent(context, KidsProfileActivity.class)
                            .putExtra("childId", childModel.getChildId()));
                } else {
                    CharSequence options[] = new CharSequence[]{"Approve", "Disapprove"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            progressDialog.show();
                            if (position == 0) {

                                childModel.setApproved(true);
                                childModel.setClassTeacherId(Prevalent.CURRENT_USER.getUid());

                                reference.child(childModel.getChildId()).setValue(childModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                                    .child("Users").child("Guardians");

                                            reference.child(childModel.getGuardianId())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            User user = snapshot.getValue(User.class);
                                                            user.setState("Approved");

                                                            if (user.getSchoolId() != null) {
                                                                if (user.getSchoolId() == null ||
                                                                        user.getSchoolId().equals("School")
                                                                        || user.getSchoolId().equals("")) {
                                                                    user.setSchoolId(childModel.getSchoolId());
                                                                    user.setChecker(2);


                                                                } else if (user.getSecondSchoolId() == null ||
                                                                        user.getSecondSchoolId().equals("")) {
                                                                    user.setSecondSchoolId(childModel.getSchoolId());
                                                                    user.setChecker(3);


                                                                } else if (user.getThirdSchoolId() == null ||
                                                                        user.getThirdSchoolId().equals("")) {
                                                                    user.setThirdSchoolId(childModel.getSchoolId());
                                                                    user.setChecker(4);


                                                                } else if (user.getFourthSchoolId() == null ||
                                                                        user.getFourthSchoolId().equals("")) {
                                                                    user.setFourthSchoolId(childModel.getSchoolId());
                                                                    user.setChecker(5);

                                                                } else if (user.getFifthSchoolId() == null
                                                                        || user.getFifthSchoolId().equals("")) {
                                                                    user.setFifthSchoolId(childModel.getSchoolId());
                                                                    user.setChecker(1);

                                                                } else {
                                                                    if (user.getChecker() == 1) {
                                                                        user.setSchoolId(childModel.getSchoolId());
                                                                        user.setChecker(2);

                                                                    } else if (user.getChecker() == 2) {
                                                                        user.setSecondSchoolId(childModel.getSchoolId());
                                                                        user.setChecker(3);

                                                                    } else if (user.getChecker() == 3) {
                                                                        user.setThirdSchoolId(childModel.getSchoolId());
                                                                        user.setChecker(4);

                                                                    } else if (user.getChecker() == 4) {
                                                                        user.setFourthSchoolId(childModel.getSchoolId());
                                                                        user.setChecker(5);

                                                                    } else if (user.getChecker() == 5) {
                                                                        user.setFifthSchoolId(childModel.getSchoolId());
                                                                        user.setChecker(1);

                                                                    }
                                                                }
                                                            } else {
                                                                user.setSchoolId(childModel.getSchoolId());

                                                            }

                                                            reference.child(childModel.getGuardianId())
                                                                    .setValue(user)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {

                                                                                DatabaseReference classReference =
                                                                                        FirebaseDatabase.getInstance().getReference().child("Classes");
                                                                                classReference.child(Prevalent.CURRENT_USER.getClassId())
                                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                            @Override
                                                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                                ClassModel classModel = snapshot.getValue(ClassModel.class);
                                                                                                if (classModel != null) {
                                                                                                    int count;
                                                                                                        count = classModel.getStudentCount() + 1;

                                                                                                    classModel.setStudentCount(count);

                                                                                                    classReference.child(Prevalent.CURRENT_USER.getClassId())
                                                                                                            .setValue(classModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if (task.isSuccessful()){

                                                                                                                        DatabaseReference notificationRef =
                                                                                                                                FirebaseDatabase.getInstance().getReference().child("Notifications");

                                                                                                                        String nid = UUID.randomUUID().toString();

                                                                                                                        NotificationModel notificationModel = new NotificationModel();

                                                                                                                        notificationModel.setActivity("My Kids");
                                                                                                                        notificationModel.setNid(nid);
                                                                                                                        notificationModel.setNotificationFrom(Prevalent.CURRENT_USER.getUid());
                                                                                                                        notificationModel.setNotificationTo(childModel.getGuardianId());
                                                                                                                        notificationModel.setSeen(false);
                                                                                                                        notificationModel.setTitle("Child Approved");
                                                                                                                        notificationModel.setDescription(childModel.getName() + "has been approved");
                                                                                                                        notificationModel.setNotificationTime(System.currentTimeMillis());

                                                                                                                        notificationRef.child(nid).setValue(notificationModel);

                                                                                                                        String nidd = UUID.randomUUID().toString();

                                                                                                                        NotificationModel noteModel = new NotificationModel();

                                                                                                                        notificationModel.setActivity("");
                                                                                                                        notificationModel.setNid(nid);
                                                                                                                        notificationModel.setNotificationFrom(Prevalent.CURRENT_USER.getUid());
                                                                                                                        notificationModel.setNotificationTo(childModel.getAdminId());
                                                                                                                        notificationModel.setSeen(false);
                                                                                                                        notificationModel.setTitle("Child Approved");
                                                                                                                        notificationModel.setDescription(childModel.getName() + "has been approved");
                                                                                                                        notificationModel.setNotificationTime(System.currentTimeMillis());

                                                                                                                        notificationRef.child(nidd).setValue(notificationModel);
                                                                                                                        progressDialog.dismiss();
                                                                                                                        holder.approvalTxt.setText("");
                                                                                                                        Toast.makeText(context, "Approved", Toast.LENGTH_SHORT).show();
                                                                                                                    }
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
                                                                    });


                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else if (position == 1) {
                                childModel.setApproved(false);
                                childModel.setClassId("");


                                reference.child(childModel.getChildId()).setValue(childModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            try {
                                                childModels.remove(p);
                                                notifyItemRemoved(p);
                                            } catch (Exception e) {

                                            }
                                            progressDialog.dismiss();
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                        }
                    });
                    builder.show();
                }
            }
        });

        if (Prevalent.CURRENT_USER.getRole().equals("Guardian") && !childModel.isApproved()) {
            holder.approvalTxt.setText("Awaiting Approval");
        } else if (!childModel.isApproved()) {
            holder.approvalTxt.setText("Approval Needed");

        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (Prevalent.CURRENT_USER.getRole().equals("Guardian")) {
                    CharSequence options[] = new CharSequence[]{"Edit", "Delete", "View Teacher"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            switch (position) {
                                case 0:
                                    Intent intent = new Intent(context, AddChildActivity.class);
                                    intent.putExtra("childID", childModel.getChildId());
                                    intent.putExtra("isEdit", true);
                                    context.startActivity(intent);
                                    break;
                                case 1:
                                    progressDialog.show();
                                    reference.child(childModel.getChildId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                try {
                                                    childModels.remove(p);
                                                    notifyItemRemoved(p);
                                                } catch (Exception e) {

                                                }
                                                progressDialog.dismiss();
                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                case 2:
                                    if (childModel.getClassTeacherId() != null) {
                                        Intent i = new Intent(context, ProfileActivity.class);
                                        i.putExtra("uid", childModel.getClassTeacherId());
                                        context.startActivity(i);
                                    } else {
                                        Toast.makeText(context, "Teacher Not Assigned", Toast.LENGTH_SHORT).show();
                                    }
                            }

                        }
                    });
                    builder.show();

                } else if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {
                    int num;

                    CharSequence options[];
                    if (childModel.getClassId() != null) {
                        if (childModel.getClassId().equals(Prevalent.CURRENT_USER.getClassId())) {
                            options = new CharSequence[]{"Remove From My Class", "View Guardian"};
                            num = 1;
                        } else {
                            options = new CharSequence[]{"Add To My Class", " View Guardian"};
                            num = 0;
                        }
                    } else {
                        options = new CharSequence[]{"Add To My Class", "View Guardian"};
                        num = 0;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            switch (position) {
                                case 0:
                                    progressDialog.show();
                                    if (num == 0) {
                                        childModel.setClassId(Prevalent.CURRENT_USER.getClassId());
                                        childModel.setClassTeacherId(Prevalent.CURRENT_USER.getUid());

                                    } else if (num == 1) {
                                        childModel.setClassId("");
                                        childModel.setClassTeacherId("");

                                    }
                                    reference.child(childModel.getChildId())
                                            .setValue(childModel)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        DatabaseReference classReference =
                                                                FirebaseDatabase.getInstance().getReference().child("Classes");
                                                        classReference.child(Prevalent.CURRENT_USER.getClassId())
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        ClassModel classModel = snapshot.getValue(ClassModel.class);
                                                                        if (classModel != null) {
                                                                            int count;
                                                                            if (num == 0) {
                                                                                count = classModel.getStudentCount() + 1;
                                                                            } else {
                                                                                count = classModel.getStudentCount() - 1;

                                                                            }
                                                                            classModel.setStudentCount(count);

                                                                            classReference.child(Prevalent.CURRENT_USER.getClassId())
                                                                                    .setValue(classModel);

                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });

                                                        try {
                                                            childModels.remove(p);
                                                            notifyItemRemoved(p);
                                                        } catch (Exception e) {

                                                        }
                                                        progressDialog.dismiss();
                                                    } else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                    break;
                                case 1:
                                    if (childModel.getGuardianId() != null) {
                                        Intent i = new Intent(context, ProfileActivity.class);
                                        i.putExtra("uid", childModel.getGuardianId());
                                        context.startActivity(i);
                                    } else {
                                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                            }
                        }
                    });
                    builder.show();

                }
                return false;
            }

        });

    }

    @Override
    public int getItemCount() {
        return childModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImg;
        TextView profileName;
        TextView approvalTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImg = itemView.findViewById(R.id.activity_profile_single);
            profileName = itemView.findViewById(R.id.activity_title_single);
            approvalTxt = itemView.findViewById(R.id.activity_single);

        }
    }
}
