package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class Konbini extends AppCompatActivity {

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
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        DatabaseReference getImage = databaseReference.child("products");

        getImage.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                            String image = productSnapshot.child("image").getValue(String.class);
                            String name = productSnapshot.child("name").getValue(String.class);
                            double price = productSnapshot.child("price").getValue(Double.class);

                            Log.d("FirebaseData", "Image URL: " + image);
                            Log.d("FirebaseData", "Name: " + name);
                            Log.d("FirebaseData", "Price: " + price);

                            // Create LinearLayout to hold product details
                            LinearLayout productLayout = new LinearLayout(Konbini.this);
                            productLayout.setOrientation(LinearLayout.VERTICAL);
                            productLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            // Create TextView to display product name and price
                            TextView prodName = new TextView(Konbini.this);
                            prodName.setText(name);

                            TextView prodPrice = new TextView(Konbini.this);
                            prodPrice.setText("$" + price);

                            // Create ImageView to display product image
                            ImageView prodImg = new ImageView(Konbini.this);
                            Picasso.get().load(image).into(prodImg);

                            // Add TextView and ImageView to the product LinearLayout
                            productLayout.addView(prodName);
                            productLayout.addView(prodPrice);
                            productLayout.addView(prodImg);

                            product_place.addView(productLayout);
                        }
                    }




/*
                        for(DataSnapshot productSnapshot : dataSnapshot.getChildren())
                        {
                            String productID = productSnapshot.getKey();
                            int count = productSnapshot.child("count").getValue(Integer.class);
                            String image = productSnapshot.child("image").getValue(String.class);
                            String name = productSnapshot.child("name").getValue(String.class);
                            double price = productSnapshot.child("price").getValue(Double.class);

                            Log.d("FirebaseData", "Product ID: " + productID);
                            Log.d("FirebaseData", "Count: " + count);
                            Log.d("FirebaseData", "Image URL: " + image);
                            Log.d("FirebaseData", "Name: " + name);
                            Log.d("FirebaseData", "Price: " + price);
                        }
*/
/*
                        String link = dataSnapshot.getValue(
                                String.class
                        );
                        Picasso.get().load(link).into(testImage);
                        System.out.println(link);
                        Toast.makeText(Konbini.this, "aint no way", Toast.LENGTH_SHORT).show();
*/

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Konbini.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        user = auth.getCurrentUser();
        if(user == null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}