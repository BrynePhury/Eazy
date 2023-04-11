package com.prox.limeapp.Adapters;

import android.app.Dialog;
import android.content.Context;
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
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Models.ClassModel;
import com.prox.limeapp.Models.PositionModel;
import com.prox.limeapp.Models.User;
import com.prox.limeapp.Prevalent.Prevalent;
import com.prox.limeapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.ViewHolder> {

    List<PositionModel> models;
    Context context;

    List<String> list;
    List<User> teachers;

    Dialog dialog;

    DatabaseReference userReference;
    DatabaseReference positionsReference;

    public PositionAdapter(List<PositionModel> models) {
        this.models = models;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_position, parent, false);
        context = parent.getContext();
        positionsReference = FirebaseDatabase.getInstance().getReference().child("Positions");

        userReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Guardians");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PositionModel model = models.get(position);

        holder.teacher_name.setText(model.getTeacherName());
        holder.position.setText(model.getPosition());
        Picasso.get().load(model.getTeacherProfile()).placeholder(R.drawable.ic_blank_profile_picture).into(holder.teacher_img, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(model.getTeacherProfile()).placeholder(R.drawable.ic_blank_profile_picture).into(holder.teacher_img);
            }
        });

        list = new ArrayList<>();
        teachers = new ArrayList<>();

        populateLists(list, teachers);

        holder.change_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                            if (user.getName().equals(adapter.getItem(i))) {
                                model.setTeacherProfile(user.getProfileUrl());
                                model.setTeacherName(user.getName());

                                user.setPosition(model.getPosition());

                                positionsReference.child(model.getSchoolId())
                                        .child(model.getPositionId())
                                        .setValue(model)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    userReference.child(user.getUid()).setValue(user)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    dialog.dismiss();
                                                                    adapter.notifyDataSetChanged();
                                                                    loadImage(model, holder.teacher_img);
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
                    if (user.getRole() != null) {
                        if (user.getRole().equals("Teacher")) {
                            if (user.getSchoolId().equals(Prevalent.CURRENT_USER.getSchoolId())) {
                                teachers.add(user);
                                list.add(user.getName());
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

    private void loadImage(PositionModel model, CircleImageView teacherImage) {
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

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView teacher_img;
        TextView teacher_name;
        TextView position;
        MaterialButton change_position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            teacher_img = itemView.findViewById(R.id.teacher_img);
            teacher_name = itemView.findViewById(R.id.teacher_name);
            position = itemView.findViewById(R.id.position_txt);
            change_position = itemView.findViewById(R.id.change_btn);
        }
    }
}
