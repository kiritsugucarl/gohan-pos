package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Random;

public class Receipt extends AppCompatActivity {

    private Random random = new Random();
    private int orderId = random.nextInt(100);
    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextView orderNum, finalPriceTV;
    private LinearLayout orderList;
    private Button completeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        // firebase instance
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // get current uid
        String currentUserID = user.getUid();

        // component fetching
        orderNum = findViewById(R.id.orderNum);
        orderList = findViewById(R.id.orderList);
        finalPriceTV = findViewById(R.id.finalPrice);
        completeBtn = findViewById(R.id.complete);

        // firebase database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(currentUserID).child("checkout_orders");

        // set random order number to emulate real life queueing
        orderNum.setText(Integer.toString(orderId));

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot checkOutOrderSnapshot : snapshot.getChildren()){
                    String checkOutKey = checkOutOrderSnapshot.getKey(); // getting key to fetch specific data

                    DatabaseReference checkOutRef = database.getReference("users").child(currentUserID)
                            .child("checkout_orders").child(checkOutKey); // users > currentUserID > checkout_orders > checkOutKey
                    checkOutRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                int totalPrice = snapshot.child("totalPrice").getValue(Integer.class);
                                finalPriceTV.setText("Final Payment : P" + totalPrice);
                                HashMap<String, String> productList = new HashMap<>();

                                // retrieval of hashmap
                                for (DataSnapshot productSnapshot : snapshot.child("productList").getChildren()) {
                                    String productName = productSnapshot.getKey();
                                    String productDetails = productSnapshot.getValue(String.class);
                                    productList.put(productName, productDetails);
                                }

                                // deserializing hashmap
                                for (String productName : productList.keySet()) {
                                    String productDetails = productList.get(productName);

                                    TextView productInfo = new TextView(Receipt.this);
                                    productInfo.setText(productName + productDetails);

                                    orderList.addView(productInfo);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Receipt.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Receipt.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        // complete buttonc licked
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference checkoutOrdersRef = FirebaseDatabase.getInstance().getReference().child("users")
                        .child(currentUserID).child("checkout_orders"); // users > currentUserID > checkout_orders

                // Remove the checkout_orders node
                checkoutOrdersRef.removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Removal successful
                                Intent intent = new Intent(Receipt.this, OrderType.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Removal failed
                                Toast.makeText(Receipt.this, "Failed to remove checkout orders", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }
}