package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        String currentUserID = user.getUid();

        order_place = findViewById(R.id.cart_items);
        backBtn = findViewById(R.id.backBtn);
        checkout = findViewById(R.id.checkout);
        finalPayment = findViewById(R.id.finalPrice);
        orderType = findViewById(R.id.orderType);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");

        userRef.child(currentUserID).child("orderType").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String userOrderType = snapshot.getValue(String.class);
                    orderType.setText("ORDER TYPE : " + userOrderType);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userRef.child(currentUserID).child("cartItems").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot itemSnapshot : snapshot.getChildren()){
                    String itemID = itemSnapshot.getKey();
                    String productId = itemSnapshot.child("productID").getValue(String.class);
                    String categoryKey = itemSnapshot.child("categoryKey").getValue(String.class);
                    int quantity = itemSnapshot.child("quantity").getValue(Integer.class);

//                    System.out.println(itemID);
//                    System.out.println(productId);
//                    System.out.println(quantity);

                    DatabaseReference productRef = database.getReference("products").child(categoryKey).child(productId);
                    productRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String engName = snapshot.child("engName").getValue(String.class);
                                String japName = snapshot.child("japName").getValue(String.class);
                                String image = snapshot.child("image").getValue(String.class);
                                int price = snapshot.child("price").getValue(Integer.class);

                                // Create LinearLayout to hold product details
                                LinearLayout productLayout = new LinearLayout(Orders.this);
                                productLayout.setOrientation(LinearLayout.VERTICAL);
                                productLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                                // Create TextView to display product name and price
                                TextView prodEngName = new TextView(Orders.this);
                                prodEngName.setText(engName);

                                TextView prodJapName = new TextView(Orders.this);
                                prodJapName.setText(japName);

                                TextView prodPrice = new TextView(Orders.this);
                                prodPrice.setText("Price: ₱" + price);

                                TextView prodQty = new TextView(Orders.this);
                                prodQty.setText(Integer.toString(quantity));

                                int tProdPrice = price * quantity;

                                TextView totalProdPrice = new TextView(Orders.this);
                                totalProdPrice.setText("Total: ₱" + tProdPrice);

                                finalPaymentPrice = finalPaymentPrice + tProdPrice;
                                System.out.println(finalPaymentPrice);
                                finalPayment.setText("GRAND TOTAL ₱: " + finalPaymentPrice);

                                // Create ImageView to display product image
                                ImageView prodImg = new ImageView(Orders.this);
                                Picasso.get().load(image).into(prodImg);

                                productList.put(engName + "(" + japName + ")", "x" + quantity + " " + tProdPrice);
                                checkOutOrderData.put("totalPrice", finalPaymentPrice);
                                checkOutOrderData.put("productList", productList);


                                // Add TextView and ImageView to the product LinearLayout
                                productLayout.addView(prodImg);
                                productLayout.addView(prodEngName);
                                productLayout.addView(prodJapName);
                                productLayout.addView(prodPrice);
                                productLayout.addView(prodQty);
                                productLayout.addView(totalProdPrice);

                                order_place.addView(productLayout);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkOutOrderId = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID).child("checkout_orders").push().getKey();
                DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);
                DatabaseReference checkOutOrderRef = currentUserRef.child("checkout_orders").child(checkOutOrderId);

                currentUserRef.child("cartItems").removeValue();

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