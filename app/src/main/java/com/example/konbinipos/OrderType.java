package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OrderType extends AppCompatActivity {

    private LinearLayout dineIn, takeOut;
    private FirebaseAuth firebaseAuth;
    private TextView customerGreeting;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_type);

        // Component fetching
        dineIn = findViewById(R.id.dineIn_btn);
        takeOut = findViewById(R.id.takeOut_btn);
        customerGreeting = findViewById(R.id.customerGreeting);

        // firebase instance
        firebaseAuth = FirebaseAuth.getInstance();
        // get current user id
        String userId = firebaseAuth.getCurrentUser().getUid();

        // reference of the database : users > userId >
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // getting the values inside the reference of the database
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // users > userId > name
                String fullName = snapshot.child("name").getValue(String.class);
                String greeting = "Hello, " + fullName;
//                customerGreeting.setText("Hello, " + fullName);
                int helloLength = "Hello".length();
                String helloText = greeting.substring(0, helloLength);
                String userText = greeting.substring(helloLength + 2);

                //Create a spannableString
                SpannableString spannableString = new SpannableString(greeting);

                // Set the color for "Hello"
                ForegroundColorSpan helloColorSpan = new ForegroundColorSpan(Color.parseColor("#767171")); // Change the color as needed
                spannableString.setSpan(helloColorSpan, 0, helloLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Set the text style for "user" to bold
                StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD_ITALIC);
                spannableString.setSpan(boldStyleSpan, helloLength + 2, greeting.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Set the color for "user"
                ForegroundColorSpan userColorSpan = new ForegroundColorSpan(Color.parseColor("#B1464A")); // Change the color as needed
                spannableString.setSpan(userColorSpan, helloLength + 2, greeting.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Set the SpannableString to the TextView
                customerGreeting.setText(spannableString);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // error handling
                Toast.makeText(OrderType.this, "Error.", Toast.LENGTH_SHORT).show();
            }
        });


        // dine in was clicked
        dineIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef = FirebaseDatabase.getInstance().getReference("users"); //users >
                DatabaseReference userChildRef = userRef.child(userId); // users > userId >
                userChildRef.child("orderType").setValue("Dine In"); // users > userId > orderType : Dine In
                goToGohan();
            }
        });

        // take out was clicked
        takeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef = FirebaseDatabase.getInstance().getReference("users"); //users >
                DatabaseReference userChildRef = userRef.child(userId); // users > userId >
                userChildRef.child("orderType").setValue("Take Out"); // users > userId > orderType: Take Out
                goToGohan();
            }
        });
    }

    private void goToGohan(){
        Intent intent = new Intent(OrderType.this, Gohan.class);
        startActivity(intent);
    }
}