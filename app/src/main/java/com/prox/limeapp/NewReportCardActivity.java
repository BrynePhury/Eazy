package com.prox.limeapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.prox.limeapp.Models.ChildModel;
import com.prox.limeapp.Models.NotificationModel;
import com.prox.limeapp.Models.ReportCardModel;
import com.prox.limeapp.Prevalent.Prevalent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class NewReportCardActivity extends AppCompatActivity {

    ImageView reportCard_img;
    Button setImage;
    TextInputEditText childNameInput, marksInput, termName;
    MaterialButton saveCard;

    String childName, childId;

    Uri imageUri;

    final int PICK_IMAGE = 1090;

    boolean imageSelected = false;

    DatabaseReference reportCardReference;

    ReportCardModel reportCardModel;
    ProgressDialog progressDialog;

    byte[] image;

    ChildModel childModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_report_card);

        reportCard_img = findViewById(R.id.report_card_img);
        setImage = findViewById(R.id.add_image);
        childNameInput = findViewById(R.id.childName);
        marksInput = findViewById(R.id.marks_et);
        termName = findViewById(R.id.termName);
        saveCard = findViewById(R.id.save_btn);

        reportCardModel = new ReportCardModel();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        reportCardReference = FirebaseDatabase.getInstance().getReference().child("Report Cards");

        childName = getIntent().getStringExtra("childName");
        childId = getIntent().getStringExtra("childID");

        childNameInput.setText(childName);
        childNameInput.setEnabled(false);

        setImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        saveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String marks = marksInput.getText().toString();
                String term = termName.getText().toString();

                if (TextUtils.isEmpty(marks)) {
                    marksInput.setError("Required");
                    marksInput.requestFocus();

                } else if (TextUtils.isEmpty(term)) {
                    termName.setError("Required");
                    termName.requestFocus();

                } else if (!imageSelected) {
                    Toast.makeText(NewReportCardActivity.this, "Select Image", Toast.LENGTH_SHORT).show();


                } else {
                    reportCardModel.setMarks(marks);
                    reportCardModel.setTermInfo(term);
                    reportCardModel.setTeacherName(Prevalent.CURRENT_USER.getName());
                    uploadImage();
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Kids");

        reference.child(childId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childModel = snapshot.getValue(ChildModel.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveReportCard() {
        String cardId = UUID.randomUUID().toString();

        reportCardModel.setChildId(childId);
        reportCardModel.setChildName(childName);
        reportCardModel.setCardId(cardId);

        reportCardReference.child(cardId).setValue(reportCardModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
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
                    notificationModel.setTitle("Report Card Posted");
                    notificationModel.setDescription(childModel.getName() + "s report card has been posted");
                    notificationModel.setNotificationTime(System.currentTimeMillis());

                    notificationRef.child(nid).setValue(notificationModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(NewReportCardActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                            finish();
                        }
                    });

                } else {
                    Toast.makeText(NewReportCardActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                }
                progressDialog.dismiss();

            }
        });


    }

    private void uploadImage() {
        progressDialog.show();
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("Report Cards");

        StorageReference filePath = reference.child(imageUri.getLastPathSegment() +
                Prevalent.CURRENT_USER.getUid() + ".jpg");

        final UploadTask uploadTask = filePath.putBytes(image);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(NewReportCardActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

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

                            reportCardModel.setCardImgUrl(task.getResult().toString());

                            saveReportCard();


                        }
                    }
                });

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            reportCard_img.setImageURI(imageUri);
            try {
                Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                original.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                image = stream.toByteArray();
                imageSelected = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}