package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
                customerGreeting.setText("Hello, " + fullName);

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