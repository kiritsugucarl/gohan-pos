package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class Gohan extends AppCompatActivity {

    FirebaseAuth auth;
    Button logout_btn;
    FirebaseUser user;
    LinearLayout product_place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konbini);

        auth = FirebaseAuth.getInstance();
        logout_btn = findViewById(R.id.logout);
        product_place = findViewById(R.id.product_place);


        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("products");

        databaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                        String category = dataSnapshot.getKey();

                        for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                            String image = productSnapshot.child("image").getValue(String.class);
                            String engName = productSnapshot.child("engName").getValue(String.class);
                            String japName = productSnapshot.child("japName").getValue(String.class);
                            int price = productSnapshot.child("price").getValue(Integer.class);

//                            Log.d("FirebaseData", "Category: " + category);
//                            Log.d("FirebaseData", "Image URL: " + image);
//                            Log.d("FirebaseData", "Eng Name: " + engName);
//                            Log.d("FirebaseData", "Jap Name: " + japName);
//                            Log.d("FirebaseData", "Price: " + price);

                            // Create LinearLayout to hold product details
                            LinearLayout productLayout = new LinearLayout(Gohan.this);
                            productLayout.setOrientation(LinearLayout.VERTICAL);
                            productLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            // Create TextView to display product name and price
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

                            product_place.addView(productLayout);

                            productLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String productId = productSnapshot.getKey();
                                    String categoryKey = productSnapshot.child("categoryKey").getValue(String.class);
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

        user = auth.getCurrentUser();
        if(user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        else{
            System.out.println("Test");
        }

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