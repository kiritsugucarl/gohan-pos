package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;


import java.util.HashMap;

public class Orders extends AppCompatActivity {

    private Button backBtn, checkout;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private LinearLayout order_place;
    private TextView finalPayment, orderType;
    private HashMap<String, Object> checkOutOrderData = new HashMap<>();
    private HashMap<String, String> productList = new HashMap<>();
    private  int finalPaymentPrice = 0;
    private int tProdPrice = 0;
    private String engName, japName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // firebase instance
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // get current user id
        String currentUserID = user.getUid();

        // component fetching
        order_place = findViewById(R.id.cart_items);
        backBtn = findViewById(R.id.backBtn);
        checkout = findViewById(R.id.checkout);
        finalPayment = findViewById(R.id.finalPrice);
        orderType = findViewById(R.id.orderType);

        // database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users"); // users >

        // users > currentUserID > orderType
        userRef.child(currentUserID).child("orderType").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    // show order type
                    String userOrderType = snapshot.getValue(String.class);
                    orderType.setText("ORDER TYPE : " + userOrderType);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Orders.this, "Error. " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // users > currentUserID > cartItems
        userRef.child(currentUserID).child("cartItems").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot itemSnapshot : snapshot.getChildren()){
                    String itemID = itemSnapshot.getKey(); // might be used later
                    String productId = itemSnapshot.child("productID").getValue(String.class);
                    String categoryKey = itemSnapshot.child("categoryKey").getValue(String.class);
                    int quantity = itemSnapshot.child("quantity").getValue(Integer.class);

                    DatabaseReference productRef = database.getReference("products").child(categoryKey).child(productId);

                    productRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {

                                // retrieve data
                                engName = snapshot.child("engName").getValue(String.class);
                                japName = snapshot.child("japName").getValue(String.class);
                                String image = snapshot.child("image").getValue(String.class);
                                int price = snapshot.child("price").getValue(Integer.class);

                                // Create LinearLayout to hold product details
                                LinearLayout productLayout = new LinearLayout(Orders.this);
                                productLayout.setOrientation(LinearLayout.VERTICAL);
                                productLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                productLayout.setGravity(Gravity.CENTER);
                                View productView = LayoutInflater.from(Orders.this).inflate(R.layout.product_info, productLayout, false);

                                // Find views within the inflated layout
                                ImageView prodImg = productView.findViewById(R.id.prodImg);
                                TextView prodEngName = productView.findViewById(R.id.prodEngName);
                                TextView prodJapName = productView.findViewById(R.id.prodJapName);
                                TextView prodPrice = productView.findViewById(R.id.prodPrice);
                                TextView prodQty = productView.findViewById(R.id.prodQty);
                                TextView totalProdPrice = productView.findViewById(R.id.totalProdPrice);

                                // Set the values for each view
                                prodEngName.setText(engName);
                                prodJapName.setText("(" + japName + ")");
                                prodPrice.setText("Price: ₱" + price);
                                prodQty.setText("Quantity : " + quantity);
                                tProdPrice = price * quantity;
                                totalProdPrice.setText("Total: ₱" + tProdPrice);
                                finalPaymentPrice = finalPaymentPrice + tProdPrice;
                                System.out.println(finalPaymentPrice);
                                finalPayment.setText("GRAND TOTAL ₱: " + finalPaymentPrice);

                                // Load image using Picasso into the ImageView
                                Picasso.get().load(image).into(prodImg);

                                // Add the inflated layout to the parent LinearLayout
                                productLayout.addView(productView);
                                order_place.addView(productLayout);

                                productList.put(engName + "(" + japName + ")", "x" + quantity + " " + tProdPrice);
                                checkOutOrderData.put("totalPrice", finalPaymentPrice);
                                checkOutOrderData.put("productList", productList);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Orders.this, "Error.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Orders.this, "Error.", Toast.LENGTH_SHORT).show();
            }
        });


        // back button pressed
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // checkout pressed
        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkOutOrderId = FirebaseDatabase.getInstance().getReference().child("users")
                        .child(currentUserID).child("checkout_orders").push().getKey(); // generate a uid
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().
                        child("users").child(currentUserID); // users > currentUserID
                DatabaseReference checkOutOrderRef = currentUserRef.child("checkout_orders").child(checkOutOrderId); //users > currentUserID > checkout_orders

                currentUserRef.child("cartItems").removeValue(); // delete cart items once checkout

                checkOutOrderRef.setValue(checkOutOrderData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Orders.this, "Checkout successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Orders.this, Receipt.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });

    }
}