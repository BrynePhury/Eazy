package com.prox.limeapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.prox.limeapp.Models.PostModel;
import com.prox.limeapp.Prevalent.Prevalent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class UploadActivity extends AppCompatActivity {

    FloatingActionButton add_image;
    CircleImageView dialogAvatar;
    EditText status_edit;
    Spinner privacy_spinner;
    TextView postBtnTxt;
    Toolbar toolbar;
    ImageView imageView;

    boolean isImageSelected;
    ProgressDialog progressDialog;
    Bitmap bitmap;
    int privacyLevel;
    private String imageUrl = "";
    private String videoUrl = "";
    private String thumbnailUrl = "";

    int PICK_IMAGE = 100;

    byte[] image = null;
    Uri imageUri = null;

    ArrayList<Uri> images = new ArrayList<>();

    public static int UPLOAD_IMAGE_QUALITY = 30;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    final int STATUS_ONLY = 0, STATUS_PHOTO = 1, PHOTO_ONLY = 2, STATUS_VIDEO = 3, VIDEO_ONLY = 4;

    /*
    0 -> Contacts
    1 -> Only Me
    2 -> Public

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        add_image = findViewById(R.id.add_image);
        dialogAvatar = findViewById(R.id.dialogAvatar);
        status_edit = findViewById(R.id.status_edit);
        privacy_spinner = findViewById(R.id.privacy_spinner);
        postBtnTxt = findViewById(R.id.postBtnTxt);
        toolbar = findViewById(R.id.toolbar);
        imageView = findViewById(R.id.image);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Picasso.get().load(Prevalent.CURRENT_USER.getProfileUrl())
                .placeholder(R.drawable.ic_blank_profile_picture).into(dialogAvatar, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(Prevalent.CURRENT_USER.getProfileUrl()).into(dialogAvatar);
                    }
                });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading...");

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");

        privacy_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                privacyLevel = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                privacyLevel = 0;

            }
        });


        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                someActivityResultLauncher.launch(intent);

                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

                Dexter.withContext(UploadActivity.this).withPermission(permissions[0])
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                                Dexter.withContext(UploadActivity.this).withPermission(permissions[1])
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
                                                        .pickPhoto(UploadActivity.this);


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
        });

        postBtnTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPost();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {

                images = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);

                imageUri = images.get(0);


                imageView.setImageURI(imageUri);
                imageView.setVisibility(View.VISIBLE);
                isImageSelected = true;

                try {
                    Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    original.compress(Bitmap.CompressFormat.JPEG, UPLOAD_IMAGE_QUALITY, stream);
                    image = stream.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    private void uploadPost() {
        String status = status_edit.getText().toString();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        progressDialog.show();


        if (status.trim().length() > 0 && isImageSelected) {

            StorageReference filePath = storageReference.child("Post Images").child(imageUri.getLastPathSegment() +
                    uid + ".jpg");

            if (image != null) {
                final UploadTask uploadTask = filePath.putBytes(image);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(UploadActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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

                                    savePostInfo(status, uid, STATUS_PHOTO);
                                }

                            }
                        });

                    }
                });
            } else {
                Toast.makeText(this, "Something went wrong, try again", Toast.LENGTH_SHORT).show();
            }


        } else if (status.trim().length() == 0 && isImageSelected) {
            progressDialog.show();

            StorageReference filePath = storageReference.child("Post Images").child(imageUri.getLastPathSegment() +
                    uid + ".jpg");

            if (image != null) {
                final UploadTask uploadTask = filePath.putBytes(image);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(UploadActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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

                                    savePostInfo(status, uid, PHOTO_ONLY);
                                }

                            }
                        });

                    }
                });
            } else {
                Toast.makeText(this, "Something went wrong, try again", Toast.LENGTH_SHORT).show();
            }


        } else if (status.trim().length() > 0 && !isImageSelected) {
            progressDialog.show();

            savePostInfo(status, uid, STATUS_ONLY);

        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Please write a post", Toast.LENGTH_SHORT).show();
        }

    }


    private void savePostInfo(String status, String uid, int type) {

        String postID = UUID.randomUUID().toString();

        PostModel postModel = new PostModel();
        if (type == STATUS_ONLY || type == STATUS_PHOTO || type == STATUS_VIDEO) {
            postModel.setPost(status);
        }
        if (Prevalent.CURRENT_USER.getRole() != null && Prevalent.CURRENT_USER.getRole().equals("Admin")) {
            postModel.setName(Prevalent.SCHOOL.getSchoolName());
            postModel.setProfileUrl(Prevalent.SCHOOL.getSchoolImg());

        } else {
            postModel.setName(Prevalent.CURRENT_USER.getName());
            postModel.setProfileUrl(Prevalent.CURRENT_USER.getProfileUrl());

        }
        postModel.setSchoolId(Prevalent.CURRENT_USER.getSchoolId());
        postModel.setPostId(postID);
        postModel.setPostUserId(uid);
        postModel.setPrivacy(privacyLevel);
        postModel.setHasComment(false);
        postModel.setHasLike(false);
        postModel.setTimeStamp(System.currentTimeMillis());
        if (type == STATUS_PHOTO || type == PHOTO_ONLY) {
            postModel.setStatusImage(imageUrl);
        } else {
            postModel.setStatusImage("0");
        }
        if (type == VIDEO_ONLY || type == STATUS_VIDEO) {
            postModel.setVideoUrl(videoUrl);
            postModel.setThumbnail(thumbnailUrl);
            postModel.setVideoPost(true);
        }
        postModel.setStatusTime(System.currentTimeMillis());

        databaseReference.child(postID).setValue(postModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            startActivity(new Intent(UploadActivity.this, HomeActivity.class));
                            finish();
                            progressDialog.dismiss();

                            Toast.makeText(UploadActivity.this, "Posted", Toast.LENGTH_SHORT).show();

                        } else {

                            progressDialog.dismiss();

                            Toast.makeText(UploadActivity.this, "Something went wrong, try again", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//
//        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
//
//            imageUri = data.getData();
//            imageView.setImageURI(imageUri);
//            imageView.setVisibility(View.VISIBLE);
//            isImageSelected = true;
//
//            try {
//                Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                original.compress(Bitmap.CompressFormat.JPEG, UPLOAD_IMAGE_QUALITY, stream);
//                image = stream.toByteArray();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//
//
//    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();

                        if (data != null) {

                            imageUri = data.getData();
                            imageView.setImageURI(imageUri);
                            imageView.setVisibility(View.VISIBLE);
                            isImageSelected = true;

                            try {
                                Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                original.compress(Bitmap.CompressFormat.JPEG, UPLOAD_IMAGE_QUALITY, stream);
                                image = stream.toByteArray();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }
            });

}