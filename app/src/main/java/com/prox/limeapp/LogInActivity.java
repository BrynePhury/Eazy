package com.prox.limeapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prox.limeapp.Models.SchoolModel;
import com.prox.limeapp.Models.User;
import com.prox.limeapp.Prevalent.Prevalent;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "GOOGLEACTIVITY";
    private static final int RC_SIGN_IN = 9001;
    ProgressDialog progressDialog;
    private FirebaseAuth mFirebaseAuth;

    private TextView sign_up_txt,signIn_forgot_password;
    private TextInputEditText signIn_phone_et;
    private TextInputEditText signIn_password_et;
    private MaterialButton sign_in_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        sign_up_txt = findViewById(R.id.sign_up_txt);
        signIn_forgot_password =findViewById(R.id.signIn_forgot_password);
        signIn_phone_et = findViewById(R.id.signIn_phone_et);
        signIn_password_et = findViewById(R.id.signIn_password_et);
        sign_in_btn = findViewById(R.id.sign_in_btn);

        mFirebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        sign_up_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogInActivity.this, SignUpActivity.class));
            }
        });

        sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateDetails();
            }
        });

        signIn_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this,ForgotPassword.class));
            }
        });


    }

    private void validateDetails() {
        String phone_email = signIn_phone_et.getText().toString();
        String password = signIn_password_et.getText().toString();

        if (TextUtils.isEmpty(phone_email)) {
            signIn_phone_et.setError("Required");
            signIn_phone_et.requestFocus();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(phone_email).matches()) {
            signIn_phone_et.setError("Invalid");
            signIn_phone_et.requestFocus();

        } else if (TextUtils.isEmpty(password)) {
            signIn_password_et.setError("Required");
            signIn_password_et.requestFocus();

        } else {
            signIn(phone_email, password);
        }

    }

    private void signIn(String phone_email, String password) {
        progressDialog.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Guardians");

        mFirebaseAuth.signInWithEmailAndPassword(phone_email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        reference.child(mFirebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                if (user.getRole().equals("Admin")) {
                                    progressDialog.dismiss();
                                    Toast.makeText(LogInActivity.this, "Invalid Account", Toast.LENGTH_SHORT).show();
                                    mFirebaseAuth.signOut();
                                    return;
                                }
                                Prevalent.CURRENT_USER = user;
                                user.setPassword(password);

                                if (user.getSchoolId() != null) {
                                    if (user.getSchoolId().equals("School")) {
                                        startActivity(new Intent(LogInActivity.this, HomeActivity.class));
                                        finish();
                                    } else {
                                        DatabaseReference schoolRef = FirebaseDatabase.getInstance()
                                                .getReference().child("Schools");

                                        schoolRef.child(user.getSchoolId())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            Prevalent.SCHOOL = snapshot.getValue(SchoolModel.class);
                                                            Prevalent.SCHOOL.setAdminId(Prevalent.CURRENT_USER.getUid());

                                                        } else {
                                                            Prevalent.SCHOOL = new SchoolModel();
                                                            Prevalent.SCHOOL.setSchoolName("Error");

                                                        }
                                                        progressDialog.dismiss();
                                                        Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Toast.makeText(LogInActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }
                                                });


                                    }
                                } else {
                                    Prevalent.CURRENT_USER.setSchoolId("School");
                                    startActivity(new Intent(LogInActivity.this, HomeActivity.class));
                                    finish();
                                }



                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }


}