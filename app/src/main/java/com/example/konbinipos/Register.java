package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    private TextInputEditText firstName, lastName, editEmail, editPassword;
    private TextView clickToLogin;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Instance
        mAuth = FirebaseAuth.getInstance();

        // Declaration of the Components
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        editEmail = findViewById(R.id.email);
        editPassword = findViewById(R.id.password);
        buttonRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        clickToLogin = findViewById(R.id.loginNow);

        String text = "Registered? Click to login";
        String firstPortion = "Registered?";
        String secondPortion = "Click to login";

        // Create a SpannableString
        SpannableString spannableString = new SpannableString(text);

        // Set the color for the first portion ("Registered?")
        ForegroundColorSpan firstColorSpan = new ForegroundColorSpan(Color.parseColor("#767171")); // Change the color as needed
        spannableString.setSpan(firstColorSpan, 0, firstPortion.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the color for the second portion ("Click to login")
        ForegroundColorSpan secondColorSpan = new ForegroundColorSpan(Color.parseColor("#B1464A")); // Change the color as needed
        spannableString.setSpan(secondColorSpan, firstPortion.length() + 1, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the SpannableString to the TextView
        clickToLogin.setText(spannableString);
        // Go to Login Activity
        clickToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Register
        buttonRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                registerUser();
            }
        });
    }

    private void registerUser(){
        // Add loading bar
        progressBar.setVisibility(View.VISIBLE);

        String email, password;

        // Pass in the details
        email = editEmail.getText().toString().trim();
        password = editPassword.getText().toString();
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();

        // If the field is empty checkers
        if(TextUtils.isEmpty(fName)){
            Toast.makeText(Register.this, "Please enter your first name", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(lName)){
            Toast.makeText(Register.this, "Please enter your last name", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(email)){
            Toast.makeText(Register.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(Register.this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase API to create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        // remove loading
                        progressBar.setVisibility(View.GONE);
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null){
                            String fullName = fName + " " + lName;
                            String userId = mAuth.getCurrentUser().getUid();

                            //add user to database
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                            DatabaseReference userChildRef = usersRef.child(userId);

                            //creating child and its components
                            userChildRef.child("name").setValue(fullName);

                            //setting name in firebase auth
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if(updateTask.isSuccessful()){
                                            Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(Register.this, OrderType.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else{
                                            Toast.makeText(Register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
    }
}