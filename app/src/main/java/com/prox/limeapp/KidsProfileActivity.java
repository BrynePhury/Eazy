package com.prox.limeapp;

import static com.prox.limeapp.UploadActivity.UPLOAD_IMAGE_QUALITY;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.prox.limeapp.Adapters.ReportCardAdapter;
import com.prox.limeapp.Models.ChildModel;
import com.prox.limeapp.Models.ReportCardModel;
import com.prox.limeapp.Prevalent.Prevalent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class KidsProfileActivity extends AppCompatActivity {

    CircleImageView profileImg;
    TextView childNameTxt, reviews_txt;
    RecyclerView recyclerView;
    Toolbar toolbar;
    RatingBar childRating;
    CollapsingToolbarLayout collapsingToolbarLayout;
    MaterialButton button;
    RelativeLayout relativeLayout;
    FloatingActionButton fab;

    DatabaseReference reference;
    DatabaseReference reportCardReference;
    String childId;

    ReportCardAdapter adapter;
    List<ReportCardModel> models;

    final int CHILD_IMAGE = 700;
    Uri profileImageUri;
    byte[] profileCompressedImage;

    ArrayList<Uri> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kids_profile);

        childNameTxt = findViewById(R.id.childName);
        profileImg = findViewById(R.id.childProfileImg);
        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        button = findViewById(R.id.options_btn);
        reviews_txt = findViewById(R.id.reviews_txt);
        childRating = findViewById(R.id.childRating);
        relativeLayout = findViewById(R.id.relative);
        fab = findViewById(R.id.fab);

        if (!Prevalent.CURRENT_USER.getRole().equals("Guardian")) {
            button.setVisibility(View.GONE);
        }

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        collapsingToolbarLayout.setTitle("");
        getSupportActionBar().setTitle("");

        models = new ArrayList<>();
        adapter = new ReportCardAdapter(models);

        childId = getIntent().getStringExtra("childId");
        reference = FirebaseDatabase.getInstance().getReference().child("Kids");
        reportCardReference = FirebaseDatabase.getInstance().getReference().child("Report Cards");


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence[] options = new CharSequence[]{"Change Profile Image"};
                AlertDialog.Builder builder = new AlertDialog.Builder(KidsProfileActivity.this);
                builder.setTitle("Choose Options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {

                        pickImage(CHILD_IMAGE);
                    }
                });
                builder.show();
            }
        });



        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KidsProfileActivity.this, RatingsActivity.class);
                intent.putExtra("childId", childId);
                startActivity(intent);
            }
        });
    }

    private void pickImage(int requestCode) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        Dexter.withContext(KidsProfileActivity.this).withPermission(permissions[0])
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        Dexter.withContext(KidsProfileActivity.this).withPermission(permissions[1])
                                .withListener(new PermissionListener() {
                                    @Override
                                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                                        FilePickerBuilder.getInstance()
                                                .setActivityTitle("Select Image")
                                                .setSpan(FilePickerConst.SPAN_TYPE.FOLDER_SPAN, 3)
                                                .setSpan(FilePickerConst.SPAN_TYPE.DETAIL_SPAN, 4)
                                                .setMaxCount(1)
                                                .setSelectedFiles(images)
                                                .setActivityTheme(R.style.CustomTheme)
                                                .pickPhoto(KidsProfileActivity.this,requestCode);


                                    }

                                    @Override
                                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                                    }

                                    @Override
                                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                        permissionToken.continuePermissionRequest();
                                    }
                                }).check();


                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == CHILD_IMAGE) {

                images = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);

                profileImageUri = images.get(0);


                try {
                    Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), profileImageUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    original.compress(Bitmap.CompressFormat.JPEG, UPLOAD_IMAGE_QUALITY, stream);
                    profileCompressedImage = stream.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                uploadFile(profileCompressedImage, profileImageUri);

            }
        }
    }

    private void uploadFile(byte[] bytes, Uri uri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StorageReference reference = FirebaseStorage.getInstance().getReference().child("Kids Images");

        StorageReference filePath = reference.child(uri.getLastPathSegment() +
                childId + ".jpg");

        if (bytes != null) {
            final UploadTask uploadTask = filePath.putBytes(bytes);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(KidsProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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


                                Picasso.get().load(task.getResult().toString()).into(profileImg, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Picasso.get().load(task.getResult().toString()).into(profileImg);
                                        }
                                    });

                                DatabaseReference kidsReference = FirebaseDatabase.getInstance().getReference().child("Kids");

                                kidsReference.child(childId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ChildModel childModel = snapshot.getValue(ChildModel.class);
                                        childModel.setProfileUrl(task.getResult().toString());

                                        kidsReference.child(childId).setValue(childModel)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        progressDialog.dismiss();
                                                        Toast.makeText(KidsProfileActivity.this, "Done", Toast.LENGTH_SHORT).show();
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
            });
        } else {
            Toast.makeText(this, "Something went wrong, try again", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChildModel model = dataSnapshot.getValue(ChildModel.class);

                    if (model.getChildId() != null) {

                        if (model.getChildId().equals(childId)) {

                            Picasso.get().load(model.getProfileUrl())
                                    .placeholder(R.drawable.ic_blank_profile_picture)
                                    .into(profileImg, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Picasso.get().load(model.getProfileUrl())
                                                    .placeholder(R.drawable.ic_blank_profile_picture)
                                                    .into(profileImg);
                                        }
                                    });

                            childNameTxt.setText(model.getName());
                            childRating.setRating(model.getRating());
                            reviews_txt.setText("(" + model.getRatingCount() + ")");

                            profileImg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(KidsProfileActivity.this
                                            , PhotoViewActivity.class).putExtra("image", model.getProfileUrl()));
                                }
                            });
                            if (Prevalent.CURRENT_USER.getRole().equals("Teacher")) {

                                if (Prevalent.CURRENT_USER.getClassId().equals(model.getClassId())) {
                                    fab.setVisibility(View.VISIBLE);
                                }
                            }
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(KidsProfileActivity.this, NewReportCardActivity.class);
                                    intent.putExtra("childName",model.getName());
                                    intent.putExtra("childID",model.getChildId());
                                    startActivity(intent);

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        models.clear();

        reportCardReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ReportCardModel reportCardModel = dataSnapshot.getValue(ReportCardModel.class);

                    if (reportCardModel.getChildId().equals(childId)){
                        models.add(reportCardModel);
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