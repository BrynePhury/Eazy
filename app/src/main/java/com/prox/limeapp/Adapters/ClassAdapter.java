package com.prox.limeapp.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
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
import com.prox.limeapp.Models.ClassModel;
import com.prox.limeapp.Models.User;
import com.prox.limeapp.Prevalent.Prevalent;
import com.prox.limeapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {

    List<ClassModel> classModels;
    Context context;

    List<String> list;
    List<User> teachers;

    Dialog dialog;

    DatabaseReference classReference;
    DatabaseReference userReference;

    public ClassAdapter(List<ClassModel> classModels) {
        this.classModels = classModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        context = parent.getContext();

        classReference = FirebaseDatabase.getInstance().getReference().child("Classes");

        userReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Guardians");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ClassModel model = classModels.get(position);

        holder.className.setText(model.getClassName());
        holder.teacherName.setText(model.getTeacherName());

        list = new ArrayList<>();
        teachers = new ArrayList<>();

        populateLists(list, teachers);


        holder.classCount.setText(model.getStudentCount() + " Members");


        loadImage(model, holder.teacherImage);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CharSequence[] options = new CharSequence[]{"Change Class Teacher", "Delete"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose Options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        if (position == 0) {

                            dialog = new Dialog(context);

                            dialog.setContentView(R.layout.dialog_teacher_spinner);
                            dialog.getWindow().setLayout(650, 800);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.show();

                            EditText editText = dialog.findViewById(R.id.edit_text);
                            ListView listView = dialog.findViewById(R.id.list_view);

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                                    android.R.layout.simple_list_item_1, list);

                            listView.setAdapter(adapter);

                            editText.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    adapter.getFilter().filter(charSequence);
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {

                                }
                            });

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    for (User user : teachers) {
                                        ProgressDialog progressDialog = new ProgressDialog(context);
                                        progressDialog.setMessage("Loading...");
                                        progressDialog.show();

                                        if (user.getName().equals(adapter.getItem(i))) {
                                            model.setTeacherProfile(user.getProfileUrl());
                                            model.setTeacherName(user.getName());
                                            model.setTeacherId(user.getUid());
                                            user.setClassId(model.getClassId());

                                            classReference.child(model.getClassId())
                                                    .setValue(model)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {

                                                                userReference.child(user.getUid()).setValue(user)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                progressDialog.dismiss();
                                                                                dialog.dismiss();
                                                                                adapter.notifyDataSetChanged();
                                                                                loadImage(model, holder.teacherImage);
                                                                                Toast.makeText(context, "Done!", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });


                                                            }

                                                        }
                                                    });
                                            break;
                                        }
                                    }
                                    dialog.dismiss();
                                }
                            });

                        } else if (position == 1) {
                            Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                builder.show();
                return false;
            }
        });
    }

    private void loadImage(ClassModel model, CircleImageView teacherImage) {
        Picasso.get().load(model.getTeacherProfile())
                .placeholder(R.drawable.ic_blank_profile_picture)
                .into(teacherImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(model.getTeacherProfile())
                                .placeholder(R.drawable.ic_blank_profile_picture)
                                .into(teacherImage);
                    }
                });

    }

    private void populateLists(List<String> list, List<User> teachers) {

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;

                    if (user.getRole() != null &&
                            user.getRole().equals("Teacher")) {
                        if (user.getSchoolId() != null &&
                                user.getSchoolId().equals(Prevalent.CURRENT_USER.getSchoolId())) {
                            teachers.add(user);
                            list.add(user.getName());
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
        return classModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView teacherImage;
        TextView className;
        TextView classCount;
        TextView teacherName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            classCount = itemView.findViewById(R.id.classCount);
            teacherName = itemView.findViewById(R.id.teacherName);
            className = itemView.findViewById(R.id.className);
            teacherImage = itemView.findViewById(R.id.teacher_profile);
        }
    }
}
