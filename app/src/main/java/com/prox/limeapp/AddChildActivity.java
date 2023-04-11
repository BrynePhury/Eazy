package com.prox.limeapp;

import static com.prox.limeapp.UploadActivity.UPLOAD_IMAGE_QUALITY;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import com.prox.limeapp.Models.ChildModel;
import com.prox.limeapp.Models.ClassModel;
import com.prox.limeapp.Models.NotificationModel;
import com.prox.limeapp.Models.SchoolModel;
import com.prox.limeapp.Prevalent.Prevalent;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class AddChildActivity extends AppCompatActivity {

    TextView selectSchool;
    TextView selectClass;
    TextView dateOfBirth;
    CircleImageView profileImage;
    TextInputEditText firstNameInput;
    TextInputEditText lastNameInput;
    TextInputLayout exam_no_layout;
    TextInputEditText exam_numberInput;


    MaterialButton add_btn;
    Spinner select_grade;
    boolean dateSet, schoolSet, imageSelected;

    List<String> schoolList;
    List<SchoolModel> schoolModels;
    List<String> classList;
    List<ClassModel> classModels;
    SchoolModel schoolModel;
    ClassModel classModel;

    Dialog dialog;

    int PICK_IMAGE = 100;

    ChildModel childModel;
    byte[] image = null;

    Uri imageUri;
    ProgressDialog progressDialog;

    boolean isEdit;
    String childIdentification;

    DatabaseReference kidsReference;

    ArrayList<Uri> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        selectSchool = findViewById(R.id.selectSchool);
        selectClass = findViewById(R.id.selectClass);
        dateOfBirth = findViewById(R.id.dateOfBirthTxt);
        profileImage = findViewById(R.id.profile_image);
        firstNameInput = findViewById(R.id.firstName);
        lastNameInput = findViewById(R.id.lastName);
        add_btn = findViewById(R.id.add_btn);
        select_grade = findViewById(R.id.select_grade);
        exam_no_layout = findViewById(R.id.exam_no_layout);
        exam_numberInput = findViewById(R.id.exam_number);

        kidsReference = FirebaseDatabase.getInstance().getReference().child("Kids");

        childModel = new ChildModel();
        schoolModel = new SchoolModel();
        classModel = new ClassModel();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        schoolList = new ArrayList<>();
        schoolModels = new ArrayList<>();
        classModels = new ArrayList<>();
        classList = new ArrayList<>();

        isEdit = getIntent().getBooleanExtra("isEdit", false);

        if (isEdit) {
            childIdentification = getIntent().getStringExtra("childID");

            kidsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ChildModel model = dataSnapshot.getValue(ChildModel.class);

                        if (model.getChildId().equals(childIdentification)) {
                            childModel = model;
                            displayInfo(childModel);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        addSchoolsToList(schoolList, schoolModels);

        selectSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(AddChildActivity.this);

                dialog.setContentView(R.layout.dialog_school_spinner);
                dialog.getWindow().setLayout(650, 800);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                EditText editText = dialog.findViewById(R.id.edit_text);
                ListView listView = dialog.findViewById(R.id.list_view);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddChildActivity.this,
                        android.R.layout.simple_list_item_1, schoolList);

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
                        selectSchool.setText(adapter.getItem(i));
                        childModel.setSchoolId(adapter.getItem(i));
                        for (SchoolModel model : schoolModels) {
                            if (model.getSchoolName().equals(adapter.getItem(i))) {
                                schoolModel = model;
                                addClassesToList(classList, classModels);
                            }
                        }
                        schoolSet = true;
                        dialog.dismiss();
                    }
                });
            }
        });

        selectClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(AddChildActivity.this);

                dialog.setContentView(R.layout.dialog_class_spinner);
                dialog.getWindow().setLayout(650, 800);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                EditText editText = dialog.findViewById(R.id.edit_text);
                ListView listView = dialog.findViewById(R.id.list_view);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddChildActivity.this,
                        android.R.layout.simple_list_item_1, classList);

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
                        selectClass.setText(adapter.getItem(i));
                        for (ClassModel model : classModels) {
                            if (model.getClassName().equals(adapter.getItem(i))) {
                                childModel.setClassId(model.getClassId());
                                classModel = model;
                            }
                        }
                        schoolSet = true;
                        dialog.dismiss();
                    }
                });
            }
        });

        select_grade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String grade = select_grade.getSelectedItem().toString();
                if (grade.equals("Grade 8") ||
                        grade.equals("Grade 9") ||
                        grade.equals("Grade 10") ||
                        grade.equals("Grade 11") ||
                        grade.equals("Grade 12")) {

                    exam_no_layout.setVisibility(View.VISIBLE);

                } else {
                    exam_no_layout.setVisibility(View.GONE);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fn = firstNameInput.getText().toString();
                String ln = lastNameInput.getText().toString();

                if (fn.isEmpty()) {
                    firstNameInput.setError("Required");
                    firstNameInput.requestFocus();

                } else if (ln.isEmpty()) {
                    lastNameInput.setError("Required");
                    lastNameInput.requestFocus();

                } else if (!isEdit) {
                    if (!schoolSet) {
                        Toast.makeText(AddChildActivity.this, "Select School", Toast.LENGTH_SHORT).show();
                    } else if (!dateSet) {
                        Toast.makeText(AddChildActivity.this, "Set D.O.B", Toast.LENGTH_SHORT).show();
                    } else if (!imageSelected) {
                        Toast.makeText(AddChildActivity.this, "Select Image", Toast.LENGTH_SHORT).show();
                    } else {
                        childModel.setName(fn + " " + ln);
                        progressDialog.show();
                        uploadImage();
                    }

                } else {
                    childModel.setName(fn + " " + ln);
                    progressDialog.show();
                    uploadImage();
                }
            }
        });

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        AddChildActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                //Making a calender instance to save the picked date
                                Calendar c = Calendar.getInstance();
                                c.set(Calendar.YEAR, year);
                                c.set(Calendar.MONTH, month);
                                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                c.set(Calendar.HOUR_OF_DAY, 0);
                                c.set(Calendar.MINUTE, 0);
                                c.set(Calendar.SECOND, 0);
                                Date date = c.getTime();

                                //formatting and displaying the selected date
                                SimpleDateFormat format = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                                String formattedDate = format.format(date);
                                dateOfBirth.setText(formattedDate);

                                childModel.setDoB(date.getTime());
                                dateSet = true;

                            }

                        }, year, month, day
                );
                datePickerDialog.show();

            }
        });


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage(PICK_IMAGE);
            }
        });

    }

    private void pickImage(int requestCode) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        Dexter.withContext(AddChildActivity.this).withPermission(permissions[0])
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        Dexter.withContext(AddChildActivity.this).withPermission(permissions[1])
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
                                                .pickPhoto(AddChildActivity.this,requestCode);


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



    private void displayInfo(ChildModel childModel) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(childModel.getDoB());

        SimpleDateFormat format = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        String formattedDate = format.format(calendar.getTime());

        selectSchool.setText(childModel.getSchoolId());
        dateOfBirth.setText(formattedDate);
        Picasso.get().load(childModel.getProfileUrl())
                .placeholder(R.drawable.ic_blank_profile_picture)
                .into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(childModel.getProfileUrl())
                                .placeholder(R.drawable.ic_blank_profile_picture)
                                .into(profileImage);
                    }
                });
        String[] names = childModel.getName().split(" ");
        firstNameInput.setText(names[0]);
        lastNameInput.setText(names[1]);
        select_grade.setSelection(getPosition(childModel));
        exam_numberInput.setText(childModel.getExamNo());
    }

    private int getPosition(ChildModel childModel) {
        if (childModel.getGrade().equals("Grade 1")) {
            return 1;
        } else if (childModel.getGrade().equals("Grade 2")) {
            return 2;
        } else if (childModel.getGrade().equals("Grade 3")) {
            return 3;
        } else if (childModel.getGrade().equals("Grade 4")) {
            return 4;
        } else if (childModel.getGrade().equals("Grade 5")) {
            return 5;
        } else if (childModel.getGrade().equals("Grade 6")) {
            return 6;
        } else if (childModel.getGrade().equals("Grade 7")) {
            return 7;
        } else if (childModel.getGrade().equals("Grade 8")) {
            return 8;
        } else if (childModel.getGrade().equals("Grade 9")) {
            return 9;
        } else if (childModel.getGrade().equals("Grade 10")) {
            return 10;
        } else if (childModel.getGrade().equals("Grade 11")) {
            return 11;
        } else if (childModel.getGrade().equals("Grade 12")) {
            return 12;
        } else if (childModel.getGrade().equals("Baby Class")) {
            return 13;
        } else if (childModel.getGrade().equals("Middle Class")) {
            return 14;
        } else if (childModel.getGrade().equals("Big Class")) {
            return 15;
        } else {
            return 0;
        }
    }

    private void addSchoolsToList(List<String> list, List<SchoolModel> schoolModels) {
        list.clear();
        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference().child("Schools");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SchoolModel schoolModel = dataSnapshot.getValue(SchoolModel.class);

                    list.add(schoolModel.getSchoolName());
                    schoolModels.add(schoolModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addClassesToList(List<String> list, List<ClassModel> classes) {
        list.clear();
        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference().child("Classes");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ClassModel classModel = dataSnapshot.getValue(ClassModel.class);

                    if (classModel.getSchoolId().equals(schoolModel.getSchoolId())) {
                        list.add(classModel.getClassName());
                        classModels.add(classModel);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void uploadImage() {
        if (isEdit && !imageSelected) {
            saveChildModel();

        } else {
            StorageReference reference = FirebaseStorage.getInstance().getReference().child("Kids Images");

            StorageReference filePath = reference.child(imageUri.getLastPathSegment() +
                    Prevalent.CURRENT_USER.getUid() + ".jpg");

            final UploadTask uploadTask = filePath.putBytes(image);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(AddChildActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

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

                                childModel.setProfileUrl(task.getResult().toString());

                                saveChildModel();

                            }
                        }
                    });

                }
            });

        }
    }

    private void saveChildModel() {

        String grade = select_grade.getSelectedItem().toString();

        String childId = "";
        if (isEdit) {
            childId = childIdentification;
        } else {
            childId = UUID.randomUUID().toString();
        }
        childModel.setChildId(childId);
        if (!isEdit) {
            childModel.setStatus("Pending");
        }
        childModel.setGrade(select_grade.getSelectedItem().toString());
        childModel.setGuardianId(Prevalent.CURRENT_USER.getUid());
        childModel.setAdminId(schoolModel.getAdminId());
        if (grade.equals("Grade 8") ||
                grade.equals("Grade 9") ||
                grade.equals("Grade 10") ||
                grade.equals("Grade 11") ||
                grade.equals("Grade 12")) {
            childModel.setExamNo(exam_numberInput.getText().toString());
        }

        kidsReference.child(childId).setValue(childModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    DatabaseReference notificationRef =
                            FirebaseDatabase.getInstance().getReference().child("Notifications");

                    String notID = UUID.randomUUID().toString();

                    NotificationModel noteModel = new NotificationModel();

                    noteModel.setActivity("My Class");
                    noteModel.setNid(notID);
                    noteModel.setNotificationFrom(Prevalent.CURRENT_USER.getUid());
                    noteModel.setNotificationTo(classModel.getTeacherId());
                    noteModel.setSeen(false);
                    noteModel.setTitle("Class Entry Request");
                    noteModel.setDescription(childModel.getName() + " just requested entry to your class");
                    noteModel.setNotificationTime(System.currentTimeMillis());

                    notificationRef.child(notID).setValue(noteModel)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    onBackPressed();
                                }
                            });


                } else {
                    Toast.makeText(AddChildActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == PICK_IMAGE) {

                images = data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);

                imageUri = images.get(0);
                profileImage.setImageURI(imageUri);

                try {
                    Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    original.compress(Bitmap.CompressFormat.JPEG, UPLOAD_IMAGE_QUALITY, stream);
                    image = stream.toByteArray();
                    imageSelected = true;

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}