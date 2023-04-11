package com.prox.limeapp;

import static com.prox.limeapp.UploadActivity.UPLOAD_IMAGE_QUALITY;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
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
import com.prox.limeapp.Adapters.ProfilePageViewAdapter;
import com.prox.limeapp.Models.User;
import com.prox.limeapp.Prevalent.Prevalent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class ProfileActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    ImageView profileCover;
    CircleImageView profilePhoto;
    MaterialButton profileOptions;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    ViewPager viewPagerProfile;

    ProfilePageViewAdapter profilePageViewAdapter;

    String uid = "0";
    String profileUrl = "", coverUrl = "";
    int current_state = 0;
    String phone = "";

    Uri profileImageUri;
    byte[] profileCompressedImage = null;
    Uri coverImageUri;
    byte[] coverCompressedImage = null;

    View v;

    private StorageReference coverStorageReference;
    private StorageReference profileStorageReference;
    private DatabaseReference databaseReference;

    ArrayList<Uri> images = new ArrayList<>();


    /*
    0 = Profile is loading...
    1 = two people are friends (unfriend)
    2 = This person has sent a contact request (cancel sent request)
    3 = This person has received a friend request from this person (Reject or accept request)
    4 = Person unknown (you can send friend request)
    5 = Own profile
    */

    ProgressDialog progressDialog;
    AlertDialog.Builder b;
    AlertDialog d;

    int imageUploadType = 0;
    final int PICK_PROFILE_PHOTO = 2000, PICK_COVER_PHOTO = 4888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);


        profileCover = findViewById(R.id.profile_cover);
        profilePhoto = findViewById(R.id.profile_image);
        profileOptions = findViewById(R.id.profile_option_btn);
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        viewPagerProfile = findViewById(R.id.viewPager_profile);

        profileStorageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        coverStorageReference = FirebaseStorage.getInstance().getReference().child("Cover Images");
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Guardians");

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        uid = getIntent().getStringExtra("uid");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equalsIgnoreCase(uid)) {
            current_state = 5;
            profileOptions.setText("Edit Profile");
            loadProfile(uid);

        } else {
            loadOthersProfile(uid);
            profileOptions.setText("Options");
            current_state = 1;
        }


        profileOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileOptions.setEnabled(false);
                if (current_state == 5) {
                    CharSequence options[] = new CharSequence[]{"Change Profile Picture", "Change Cover Picture","Change Massage Number"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setOnDismissListener(ProfileActivity.this);
                    builder.setTitle("Choose Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            if (position == 0) {
                                imageUploadType = 1;
                                pickImage(PICK_PROFILE_PHOTO);
                            } else if (position == 1) {
                                imageUploadType = 0;
                                pickImage(PICK_COVER_PHOTO);
                            }else if (position == 2) {

                                b = new AlertDialog.Builder(ProfileActivity.this);
                                v = LayoutInflater.from(ProfileActivity.this)
                                        .inflate(R.layout.set_number_dialog,null,false);
                                b.setView(v);
                                d = b.show();


                            }
                        }
                    });
                    builder.show();

                } else if (current_state == 1) {

                    CharSequence options[] = new CharSequence[]{"Message"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setOnDismissListener(ProfileActivity.this);
                    builder.setTitle("Choose Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {

                            Intent i = new Intent(Intent.ACTION_VIEW);
                            try{
                                String url = "https://api.whatsapp.com/send?phone="+ phone +"&text+" + URLEncoder.encode("","UTF-8");
                                i.setPackage("com.whatsapp");
                                i.setData(Uri.parse(url));
                                startActivity(i);

                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.show();

                }
            }
        });

    }
    public void changeNumber(View view){
        TextInputEditText passwordEt = v.findViewById(R.id.password_et);
        TextInputEditText numberEt = v.findViewById(R.id.phone_et);

        String password = passwordEt.getText().toString();
        String phone = numberEt.getText().toString();

        if (TextUtils.isEmpty(password)){
            passwordEt.setError("Required");
            passwordEt.requestFocus();

        } else if (TextUtils.isEmpty(phone)){
            numberEt.setError("Required");
            numberEt.requestFocus();

        } else if (!Prevalent.CURRENT_USER.getPassword().equals(password)){
            passwordEt.setError("Incorrect Password");
            passwordEt.requestFocus();

        } else if (phone.length() < 10){
            numberEt.setError("Invalid");
            numberEt.requestFocus();
        } else {
            Prevalent.CURRENT_USER.setPhone(phone);
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("Loading...");
            dialog.show();

            DatabaseReference r = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child("Guardians");

            r.child(Prevalent.CURRENT_USER.getUid()).setValue(Prevalent.CURRENT_USER).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        dialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                        d.dismiss();
                    }
                }
            });
        }

    }


    private void pickImage(int requestCode) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        Dexter.withContext(ProfileActivity.this).withPermission(permissions[0])
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        Dexter.withContext(ProfileActivity.this).withPermission(permissions[1])
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
                                                .pickPhoto(ProfileActivity.this,requestCode);


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

    private void loadProfile(String uid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().
                child("Users").child("Guardians");
        reference.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);

                            showUserData(user);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();

                    }
                });

    }


    private void showUserData(User user) {

        profilePageViewAdapter = new ProfilePageViewAdapter(getSupportFragmentManager(), 1
                , user.getUid(), String.valueOf(current_state));
        viewPagerProfile.setAdapter(profilePageViewAdapter);

        profileUrl = user.getProfileUrl();
        coverUrl = user.getCoverUrl();
        phone = "+26" + user.getPhone();

        collapsingToolbarLayout.setTitle(user.getName());
        if (profileUrl !=null) {
            Picasso.get().load(profileUrl).placeholder(R.drawable.ic_blank_profile_picture)
                    .into(profilePhoto, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(profileUrl).placeholder(R.drawable.ic_blank_profile_picture)
                            .into(profilePhoto);
                }
            });
            profilePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ProfileActivity.this
                            ,PhotoViewActivity.class).putExtra("image",profileUrl));
                }
            });
        }
        if (coverUrl != null) {
            Picasso.get().load(coverUrl).into(profileCover, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(coverUrl).into(profileCover);
                }
            });
            profileCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ProfileActivity.this
                            ,PhotoViewActivity.class).putExtra("image",coverUrl));
                }
            });
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        profileOptions.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == PICK_PROFILE_PHOTO) {

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

                uploadFile(profileCompressedImage, profileStorageReference, profileImageUri);

            } else if (requestCode == PICK_COVER_PHOTO) {

                images = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);

                coverImageUri = images.get(0);


                try {
                    Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), coverImageUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    original.compress(Bitmap.CompressFormat.JPEG, UPLOAD_IMAGE_QUALITY, stream);
                    coverCompressedImage = stream.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                uploadFile(coverCompressedImage, coverStorageReference, coverImageUri);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    String profileImageUrl;
    String coverImageUrl;

    private void uploadFile(byte[] bytes, StorageReference reference, Uri uri) {
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StorageReference filePath = reference.child(uri.getLastPathSegment() +
                uid + ".jpg");

        if (bytes != null) {
            final UploadTask uploadTask = filePath.putBytes(bytes);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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

                                if (imageUploadType == 1) {
                                    profileImageUrl = task.getResult().toString();
                                    Prevalent.CURRENT_USER.setProfileUrl(task.getResult().toString());

                                    Picasso.get().load(task.getResult().toString()).into(profilePhoto, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Picasso.get().load(task.getResult().toString()).into(profilePhoto);
                                        }
                                    });
                                } else if (imageUploadType == 0) {
                                    coverImageUrl = task.getResult().toString();
                                    Prevalent.CURRENT_USER.setCoverUrl(task.getResult().toString());

                                    Picasso.get().load(task.getResult().toString()).into(profileCover, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Picasso.get().load(task.getResult().toString()).into(profileCover);
                                        }
                                    });
                                }

                                databaseReference.child(uid).setValue(Prevalent.CURRENT_USER)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileActivity.this, "Done", Toast.LENGTH_SHORT).show();
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

    private void loadOthersProfile(String uid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().
                child("Users").child("Guardians");
        reference.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);

                            showUserData(user);

                        } else {
                            Toast.makeText(ProfileActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();

                    }
                });
    }
}