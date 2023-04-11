package com.prox.limeapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.prox.limeapp.Models.Approval;
import com.prox.limeapp.Models.NotificationModel;
import com.prox.limeapp.Models.SchoolModel;
import com.prox.limeapp.Models.User;
import com.prox.limeapp.Prevalent.Prevalent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "GOOGLEACTIVITY";
    private static final int RC_SIGN_IN = 9001;
    ProgressDialog progressDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private MaterialButton mSignInButton;
    private TextInputEditText signUp_fn_et;
    private TextInputEditText signUp_ln_et;
    private TextInputEditText signUp_email_et;
    private TextInputEditText signUp_password_et;
    private TextInputEditText signUp_re_password_et;
    private TextInputEditText signUp_phone_et;
    private RadioGroup radioGroup;
    private MaterialButton sign_up_btn;
    private TextView selectSchool;

    boolean schoolSet;


    User user;

    Dialog dialog;

    List<SchoolModel> schoolModels;
    List<String> list;

    SchoolModel schoolModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mSignInButton = findViewById(R.id.google_sign_in_button);
        mFirebaseAuth = FirebaseAuth.getInstance();

        selectSchool = findViewById(R.id.selectSchool);
        signUp_fn_et = findViewById(R.id.signUp_fn_et);
        signUp_ln_et = findViewById(R.id.signUp_ln_et);
        signUp_email_et = findViewById(R.id.signUp_email_et);
        signUp_phone_et = findViewById(R.id.signUp_phone_et);
        radioGroup = findViewById(R.id.radioGroup);
        signUp_password_et = findViewById(R.id.signUp_password_et);
        signUp_re_password_et = findViewById(R.id.signUp_re_password_et);
        sign_up_btn = findViewById(R.id.sign_up_btn);

        user = new User();

        list = new ArrayList<>();
        schoolModels = new ArrayList<>();

        addSchoolsToList(list, schoolModels);

        GoogleSignInOptions geo = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("173102547138-nm6rbhh9ccdtjfopgmpvupo601n237v9.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, geo);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateDetails();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.guardian_radio_btn) {
                    user.setRole("Guardian");
                    selectSchool.setVisibility(View.GONE);

                } else if (i == R.id.teacher_radio_btn) {
                    user.setRole("Teacher");
                    selectSchool.setVisibility(View.VISIBLE);

                }
            }
        });

        selectSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(SignUpActivity.this);

                dialog.setContentView(R.layout.dialog_school_spinner);
                dialog.getWindow().setLayout(650, 800);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                EditText editText = dialog.findViewById(R.id.edit_text);
                ListView listView = dialog.findViewById(R.id.list_view);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(SignUpActivity.this,
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
                        selectSchool.setText(adapter.getItem(i));
                        user.setSchoolId(adapter.getItem(i));
                        for (SchoolModel model : schoolModels) {
                            if (model.getSchoolName().equals(adapter.getItem(i))) {
                                schoolModel = model;
                            }
                        }
                        schoolSet = true;
                        dialog.dismiss();
                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        list.clear();
        addSchoolsToList(list,schoolModels);
    }

    private void addSchoolsToList(List<String> list, List<SchoolModel> schoolModels) {

        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference().child("Schools");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
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

    private void validateDetails() {
        String fn = signUp_fn_et.getText().toString();
        String ln = signUp_ln_et.getText().toString();
        String email = signUp_email_et.getText().toString();
        String phone = signUp_phone_et.getText().toString();
        String password = signUp_password_et.getText().toString();
        String re_password = signUp_re_password_et.getText().toString();

        if (TextUtils.isEmpty(fn)) {
            signUp_fn_et.setError("Required");
            signUp_fn_et.requestFocus();

        } else if (TextUtils.isEmpty(ln)) {
            signUp_ln_et.setError("Required");
            signUp_ln_et.requestFocus();

        } else if (TextUtils.isEmpty(email)) {
            signUp_email_et.setError("Required");
            signUp_email_et.requestFocus();

        }else if (TextUtils.isEmpty(phone)) {
            signUp_phone_et.setError("Required");
            signUp_phone_et.requestFocus();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signUp_email_et.setError("Invalid");
            signUp_email_et.requestFocus();

        } else if (TextUtils.isEmpty(password)) {
            signUp_password_et.setError("Required");
            signUp_password_et.requestFocus();

        } else if (TextUtils.isEmpty(re_password)) {
            signUp_re_password_et.setError("Required");
            signUp_re_password_et.requestFocus();

        } else if (radioGroup.getCheckedRadioButtonId() == -1) {

            Toast.makeText(this, "Select a role", Toast.LENGTH_SHORT).show();

        } else {
            signUp(fn, ln, email, password,phone);
        }

    }

    private void signUp(String fn, String ln, String email, String password,String phone) {
        progressDialog.show();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            user.setName(fn + " " + ln);
                            user.setUid(mFirebaseAuth.getCurrentUser().getUid());
                            user.setParentId(UUID.randomUUID().toString());
                            user.setEmail(email);
                            user.setPassword(password);
                            user.setPhone(phone);
                            user.setState("Pending");
                            if (schoolModel != null) {
                                user.setSchoolId(schoolModel.getSchoolId());
                            }

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child("Guardians");

                            reference.child(mFirebaseAuth.getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                if (schoolModel != null) {
                                                    DatabaseReference notificationRef =
                                                            FirebaseDatabase.getInstance().getReference().child("Notifications");

                                                    String nid = UUID.randomUUID().toString();

                                                    NotificationModel notificationModel = new NotificationModel();

                                                    notificationModel.setActivity("Approvals");
                                                    notificationModel.setNid(nid);
                                                    notificationModel.setNotificationFrom(user.getUid());
                                                    notificationModel.setNotificationTo(schoolModel.getAdminId());
                                                    notificationModel.setSeen(false);
                                                    notificationModel.setTitle("Teacher Approval");
                                                    notificationModel.setDescription(user.getName() + " has requested approval");
                                                    notificationModel.setNotificationTime(System.currentTimeMillis());

                                                    notificationRef.child(nid).setValue(notificationModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {

                                                                DatabaseReference databaseReference =
                                                                        FirebaseDatabase.getInstance().getReference().child("Approvals");

                                                                String aid = UUID.randomUUID().toString();

                                                                Approval approval = new Approval();
                                                                approval.setAid(aid);
                                                                approval.setParentId(user.getUid());
                                                                approval.setProfileImg(user.getProfileUrl());
                                                                approval.setPosition("Teacher");
                                                                approval.setUserName(user.getName());
                                                                approval.setUserId(user.getUid());
                                                                approval.setSchoolId(schoolModel.getSchoolId());

                                                                databaseReference.child(aid).setValue(approval).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            progressDialog.dismiss();
                                                                            Prevalent.CURRENT_USER = user;
                                                                            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                                                                            Intent intent = new Intent(SignUpActivity.this, VerifyEmailActivity.class);
                                                                            intent.putExtra("login", false);
                                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            startActivity(intent);
                                                                        }
                                                                    }
                                                                });

                                                            } else {
                                                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                                } else {
                                                    progressDialog.dismiss();
                                                    Prevalent.CURRENT_USER = user;
                                                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                                                    Intent intent = new Intent(SignUpActivity.this, VerifyEmailActivity.class);
                                                    intent.putExtra("login", false);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });


                        } else {
                            progressDialog.dismiss();

                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void signIn() {
        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(googleSignInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            progressDialog.show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google sign in was successful, authenticate with firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                //Google sign in failed
                Log.v(TAG, "Google siogn in failed");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle" + account.getId());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Guardians");

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "SignInWithCredential|Success");
                            FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();


                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean isFound = false;
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        User mUser = dataSnapshot.getValue(User.class);

                                        if (currentUser.getUid() != null) {
                                            if (currentUser.getUid().equals(mUser.getUid())) {
                                                mUser.setSchoolId("School");
                                                Prevalent.CURRENT_USER = mUser;
                                                progressDialog.dismiss();
                                                isFound = true;
                                                if (!currentUser.isEmailVerified()) {
                                                    currentUser.sendEmailVerification();

                                                    startActivity(new Intent(SignUpActivity.this, VerifyEmailActivity.class)
                                                            .putExtra("login", false));
                                                    finish();
                                                }
                                            }
                                        }
                                    }
                                    boolean finalIsFound = isFound;
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!finalIsFound) {

                                                FirebaseMessaging.getInstance().getToken()
                                                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<String>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<String> task) {
                                                                if (!task.isSuccessful()) {
                                                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                                                    return;
                                                                }

                                                                // Get new FCM registration token
                                                                String userToken = task.getResult();
                                                                String uid = currentUser.getUid();
                                                                String name = currentUser.getDisplayName();
                                                                String email = currentUser.getEmail();
                                                                String photoUrl = currentUser.getPhotoUrl().toString();

                                                                user.setUid(uid);
                                                                user.setName(name);
                                                                user.setEmail(email);
                                                                user.setUserToken(userToken);
                                                                user.setProfileUrl(photoUrl);

                                                                String[] arr = name.split(" ");

                                                                signUp_email_et.setText(email);
                                                                signUp_fn_et.setText(arr[0]);
                                                                signUp_ln_et.setText(arr[1]);
                                                                mFirebaseAuth.getCurrentUser().delete();
                                                                Toast.makeText(SignUpActivity.this, "We still need a password", Toast.LENGTH_SHORT).show();

                                                                progressDialog.dismiss();

                                                            }
                                                        });
                                            }
                                        }
                                    }, 2500);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        } else {
                            Log.d(TAG, "SignInWithCredential|Failure", task.getException());
                            progressDialog.dismiss();
                        }

                    }
                });

    }
}