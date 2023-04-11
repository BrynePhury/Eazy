package com.prox.limeapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    TextInputEditText email_input;
    MaterialButton send_email;
    ProgressBar progressBar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email_input = findViewById(R.id.email_address_input);
        send_email = findViewById(R.id.send_email);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        send_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendResetEmail();
            }
        });


    }

    private void sendResetEmail() {
        String email = email_input.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            email_input.setError("Required");
            email_input.requestFocus();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_input.setError("Invalid Email");
            email_input.requestFocus();

        } else {
            progressBar.setVisibility(View.VISIBLE);
            send_email.setEnabled(false);
            send_email.setBackgroundColor(getColor(R.color.secondary_text));

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPassword.this, "Done! Check your email", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);

                    } else {
                        send_email.setEnabled(true);
                        send_email.setBackgroundColor(getColor(R.color.gold));

                        Toast.makeText(ForgotPassword.this, "Something went wrong", Toast.LENGTH_LONG).show();

                    }

                }
            });
        }
    }


}