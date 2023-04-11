package com.prox.limeapp;

import static com.prox.limeapp.UploadActivity.UPLOAD_IMAGE_QUALITY;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.prox.limeapp.Adapters.ContactsAdapter;
import com.prox.limeapp.Dialogs.CreateSchoolDialog;
import com.prox.limeapp.Models.SchoolModel;
import com.prox.limeapp.Models.User;
import com.prox.limeapp.Prevalent.Prevalent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SchoolActivity extends AppCompatActivity implements CreateSchoolDialog.CreateSchoolListener {

    Toolbar toolbar;
    CardView create_card;
    TextView school_name_tv;
    CircleImageView school_img;
    MaterialButton options_btn;
    RecyclerView recyclerView;

    CreateSchoolDialog createSchoolDialog;
    ProgressDialog progressDialog;
    RelativeLayout schoolLayout;

    DatabaseReference reference;

    final int REQUEST_PROFILE = 777;

    ContactsAdapter adapter;
    List<User> users;

    Uri imageUri;
    byte[] imageCompressed;
    String imageUrl;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("School Images");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school);

        toolbar = findViewById(R.id.toolbar);
        create_card = findViewById(R.id.create);
        schoolLayout = findViewById(R.id.schoolLayout);
        school_name_tv = findViewById(R.id.school_name_tv);
        school_img = findViewById(R.id.school_img);
        options_btn = findViewById(R.id.options_btn);
        recyclerView = findViewById(R.id.recyclerView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        users = new ArrayList<>();
        adapter = new ContactsAdapter(users);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        reference = FirebaseDatabase.getInstance().getReference().child("Schools");

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        create_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createSchoolDialog = new CreateSchoolDialog();
                createSchoolDialog.show(getSupportFragmentManager(), "Create School Dialog");
            }
        });

        checkAndUpdateViews();

    }

    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Guardians");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    users.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        try {
                            User user = dataSnapshot.getValue(User.class);
                            if (user.getSchoolId() != null && Prevalent.CURRENT_USER.getSchoolId()
                                    != null && user.getUid() != null) {
                                if (!user.getUid().equals(Prevalent.CURRENT_USER.getUid())) {
                                    if (user.getSchoolId().equals(Prevalent.CURRENT_USER.getSchoolId()) && user.getRole().equals("Teacher")) {

                                        users.add(user);
                                        adapter.notifyDataSetChanged();


                                    }
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkAndUpdateViews() {
        if (Prevalent.CURRENT_USER.getSchoolId() != null) {

            if (Prevalent.CURRENT_USER.getSchoolId().equals("School")) {
                create_card.setVisibility(View.VISIBLE);
                schoolLayout.setVisibility(View.GONE);
            } else {
                create_card.setVisibility(View.GONE);
                schoolLayout.setVisibility(View.VISIBLE);
                getContent();

            }
        } else {
            create_card.setVisibility(View.VISIBLE);
            schoolLayout.setVisibility(View.VISIBLE);

        }
    }

    private void getContent() {
        school_name_tv.setText(Prevalent.SCHOOL.getSchoolName());
        Picasso.get().load(Prevalent.SCHOOL.getSchoolImg())
                .placeholder(R.drawable.ic_blank_profile_picture)
                .into(school_img, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(Prevalent.SCHOOL.getSchoolImg())
                                .placeholder(R.drawable.ic_blank_profile_picture)
                                .into(school_img);
                    }
                });

        options_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]{"Change School Picture", "Change School Name",};
                AlertDialog.Builder builder = new AlertDialog.Builder(SchoolActivity.this);
                builder.setTitle("Choose Options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        if (position == 0) {
                            pickImage(REQUEST_PROFILE);

                        } else if (position == 1) {
                            createDialog();

                        }
                    }
                });
                builder.show();
            }
        });

    }

    private void createDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.change_name_dialog, null, false);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.show();

        TextInputEditText name_et = view.findViewById(R.id.new_name_et);
        TextInputEditText password_et = view.findViewById(R.id.new_name_pass_et);
        MaterialButton save_btn = view.findViewById(R.id.save_btn);

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = password_et.getText().toString();
                String name = name_et.getText().toString();

                if (TextUtils.isEmpty(name)) {
                    name_et.setError("Required");
                    name_et.requestFocus();
                } else if (!password.equals(Prevalent.CURRENT_USER.getPassword())) {
                    password_et.setError("Incorrect");
                    password_et.requestFocus();
                } else {
                    progressDialog.show();
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            MutableLiveData<Boolean> found = new MutableLiveData<Boolean>();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                SchoolModel schoolModel = dataSnapshot.getValue(SchoolModel.class);

                                if (name.equals(schoolModel.getSchoolName())) {
                                    name_et.setError("Name is taken");
                                    name_et.requestFocus();
                                    progressDialog.dismiss();
                                    found.setValue(true);
                                }
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                        reference.child(Prevalent.SCHOOL.getSchoolId())
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Prevalent.SCHOOL.setSchoolName(name);
                                                            Prevalent.CURRENT_USER.setSchoolId(name);
                                                            reference.child(name).setValue(Prevalent.SCHOOL)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                FirebaseDatabase.getInstance().getReference().child("Users")
                                                                                        .child("Guardians").child(Prevalent.CURRENT_USER.getUid())
                                                                                        .setValue(Prevalent.CURRENT_USER)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                progressDialog.dismiss();
                                                                                                dialog.dismiss();
                                                                                                Toast.makeText(SchoolActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                                                                                                school_name_tv.setText(Prevalent.SCHOOL.getSchoolName());
                                                                                            }
                                                                                        });
                                                                            } else {
                                                                                Toast.makeText(SchoolActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                        else {
                                                            Toast.makeText(SchoolActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }

                            },5000);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

    }

    private void pickImage(int requestCode) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PROFILE) {

            imageUri = data.getData();


            try {
                Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                original.compress(Bitmap.CompressFormat.JPEG, UPLOAD_IMAGE_QUALITY, stream);
                imageCompressed = stream.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
            }

            uploadFile(imageCompressed, storageRef, imageUri);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadFile(byte[] bytes, StorageReference storageReference, Uri uri) {
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StorageReference filePath = storageReference.child(uri.getLastPathSegment() +
                Prevalent.SCHOOL.getSchoolName() + ".jpg");

        if (bytes != null) {
            final UploadTask uploadTask = filePath.putBytes(bytes);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(SchoolActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                            if (!task.isSuccessful()) {

                                throw task.getException();

                            } else {
//                                downloadImageUrl.add(filePath.getDownloadUrl().toString());
                                return filePath.getDownloadUrl();
                            }

                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                imageUrl = task.getResult().toString();
                                Prevalent.SCHOOL.setSchoolImg(task.getResult().toString());

                                Picasso.get().load(task.getResult().toString()).into(school_img, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(task.getResult().toString()).into(school_img);
                                    }
                                });

                                reference.child(Prevalent.SCHOOL.getSchoolName()).setValue(Prevalent.SCHOOL)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(SchoolActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });


                            }
                        }
                    });

                }
            });
        } else {
            Toast.makeText(this, "Something went wrong, try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void applyTexts(String name) {

        reference.child(name)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            SchoolModel schoolModel = snapshot.getValue(SchoolModel.class);
                            if (schoolModel.getSchoolName().equals(name)) {
                                createSchoolDialog.schoolNameInput.setError("Exists");
                            }
                        } else {
                            registerSchool(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void registerSchool(String name) {
        progressDialog.show();
        SchoolModel schoolModel = new SchoolModel();
        schoolModel.setSchoolId(name);
        schoolModel.setSchoolName(name);

        Prevalent.CURRENT_USER.setSchoolId(name);
        Prevalent.SCHOOL = schoolModel;

        reference.child(name).setValue(schoolModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child("Guardians");

                            userReference.child(Prevalent.CURRENT_USER.getUid())
                                    .setValue(Prevalent.CURRENT_USER).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                createSchoolDialog.dismiss();
                                                progressDialog.dismiss();
                                                create_card.setVisibility(View.GONE);
                                                schoolLayout.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}