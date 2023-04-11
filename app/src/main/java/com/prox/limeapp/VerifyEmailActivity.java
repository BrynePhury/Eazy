package com.prox.limeapp;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class VerifyEmailActivity extends AppCompatActivity {

    TextView verificationMessage;
    Button resendEmailBtn;

    FirebaseAuth mAuth;

    boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();

        isLogin = getIntent().getBooleanExtra("login",false);

        verificationMessage = findViewById(R.id.verify_text);
        resendEmailBtn = findViewById(R.id.resend_email_btn);

        if(!isLogin){
            resendEmailBtn.setEnabled(false);
            resendEmailBtn.setBackgroundColor(getResources().getColor(R.color.teal_200));
        }

        resendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            resendEmailBtn.setEnabled(false);
                            resendEmailBtn.setBackgroundColor(getResources().getColor(R.color.teal_200));

                            verificationMessage.setText("Please verify email, /n restart app after verification");

                            Toast.makeText(VerifyEmailActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();


                        } else {
                            Toast.makeText(VerifyEmailActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

    }
}