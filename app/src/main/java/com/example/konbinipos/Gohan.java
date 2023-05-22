package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


public class Gohan extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button logout_btn;
    private FirebaseUser user;
    private LinearLayout product_place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konbini);

        // firebase instance
        auth = FirebaseAuth.getInstance();

        // component fetching
        logout_btn = findViewById(R.id.logout);
        product_place = findViewById(R.id.product_place);

        // firebase database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("products"); // products >

        databaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                        String category = dataSnapshot.getKey(); // will be used when kayla design is here

                        // iterate every product
                        for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                            String image = productSnapshot.child("image").getValue(String.class);
                            String engName = productSnapshot.child("engName").getValue(String.class);
                            String japName = productSnapshot.child("japName").getValue(String.class);
                            int price = productSnapshot.child("price").getValue(Integer.class);

                            // Create LinearLayout to hold product details
                            LinearLayout productLayout = new LinearLayout(Gohan.this);
                            productLayout.setOrientation(LinearLayout.VERTICAL);
                            productLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            // Create TextView to display product name and price and details
                            TextView prodEngName = new TextView(Gohan.this);
                            prodEngName.setText(engName);

                            TextView prodJapName = new TextView(Gohan.this);
                            prodJapName.setText(japName);

                            TextView prodPrice = new TextView(Gohan.this);
                            prodPrice.setText("₱" + price);

                            // Create ImageView to display product image
                            ImageView prodImg = new ImageView(Gohan.this);
                            Picasso.get().load(image).into(prodImg);

                            // Add TextView and ImageView to the product LinearLayout
                            productLayout.addView(prodImg);
                            productLayout.addView(prodEngName);
                            productLayout.addView(prodJapName);
                            productLayout.addView(prodPrice);

                            // Add everything to the layout
                            product_place.addView(productLayout);

                            // make the layout clickable
                            productLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String productId = productSnapshot.getKey();
                                    String categoryKey = productSnapshot.child("categoryKey").getValue(String.class); // product > categoryKey

                                    Intent intent = new Intent(Gohan.this, FoodInfo.class);
                                    intent.putExtra("productID", productId);
                                    intent.putExtra("categoryKey", categoryKey);
                                    startActivity(intent);
                                }
                            });
                        }
                    }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //empty
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //empty
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //empty
            }

            @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Gohan.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // if no user check
        user = auth.getCurrentUser();
        if(user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        // logout button clicked
        logout_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

    }

}