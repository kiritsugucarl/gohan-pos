package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_type);

        dineIn = findViewById(R.id.dineIn_btn);
        takeOut = findViewById(R.id.takeOut_btn);
        customerGreeting = findViewById(R.id.customerGreeting);

        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullName = snapshot.child("name").getValue(String.class);
                Log.d("CurUserName", "Name : " + fullName);
                customerGreeting.setText("Hello, " + fullName);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // error handling
            }
        });


        dineIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef = FirebaseDatabase.getInstance().getReference("users");
                DatabaseReference userChildRef = userRef.child(userId);
                userChildRef.child("orderType").setValue("Dine In");
                goToGohan();
            }
        });

        takeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef = FirebaseDatabase.getInstance().getReference("users");
                DatabaseReference userChildRef = userRef.child(userId);
                userChildRef.child("orderType").setValue("Take Out");
                goToGohan();
            }
        });
    }

    private void goToGohan(){
        Intent intent = new Intent(OrderType.this, Gohan.class);
        startActivity(intent);
    }
}