package com.prox.limeapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.prox.limeapp.Services.MyJobService;

public class MainActivity extends AppCompatActivity {

    private final int SPLASH_TIME_OUT = 1000;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mFirebaseAuth = FirebaseAuth.getInstance();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            mFirebaseUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        if (mFirebaseUser.isEmailVerified()){

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child("Guardians");

                            reference.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    User mUser = snapshot.getValue(User.class);
                                    if (mUser.getRole().equals("Admin")) {
                                        Toast.makeText(MainActivity.this, "Invalid Account", Toast.LENGTH_SHORT).show();
                                        mFirebaseAuth.signOut();
                                        startActivity(new Intent(MainActivity.this,LogInActivity.class));
                                        finish();
                                        return;
                                    }
                                    Prevalent.CURRENT_USER = mUser;
                                    
                                    if (mUser.getSchoolId() != null) {
                                        if (mUser.getSchoolId().equals("School")) {
                                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                            finish();
                                        } else {
                                            DatabaseReference schoolRef = FirebaseDatabase.getInstance()
                                                    .getReference().child("Schools");

                                            schoolRef.child(mUser.getSchoolId())
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
                                                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                            finish();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                                                        }
                                                    });


                                        }
                                    } else {
                                        Prevalent.CURRENT_USER.setSchoolId("School");
                                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            startActivity(new Intent(MainActivity.this,VerifyEmailActivity.class).putExtra("login",true));
                        }
                    }
                }
            });


        }
        else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this,LogInActivity.class));
                    finish();
                }
            },SPLASH_TIME_OUT);
        }
    }
}