package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class FoodInfo extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private String productID, categoryKey;
    private DatabaseReference productRef, userRef, cartRef;
    private String engName, japName, image,orderType;
    private int price;
    private int prodQty;

    private TextView englishName, japaneseName, prodPrice, priceDisplay;
    private EditText prodCount;
    private ImageView prodImg;

    private Button backBtn, addToOrder, viewOrders, addQty, subQty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_info);

        // fetch intent
        Intent intent = getIntent();
        if(intent != null){
            productID = intent.getStringExtra("productID");
            categoryKey = intent.getStringExtra("categoryKey");
        }

        // firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        String fullName = firebaseAuth.getCurrentUser().getDisplayName(); // might be used later

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId); // users > userId

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderType = snapshot.child("orderType").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodInfo.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        // component fetching
        englishName = findViewById(R.id.prodName);
        japaneseName = findViewById(R.id.prodJapName);
        prodPrice = findViewById(R.id.prodPrice);
        prodImg = findViewById(R.id.prodImg);
        backBtn = findViewById(R.id.backBtn);
        addToOrder = findViewById(R.id.addToOrder);
        priceDisplay = findViewById(R.id.priceDisplay);
        viewOrders = findViewById(R.id.viewOrders);
        addQty = findViewById(R.id.plusQty);
        subQty = findViewById(R.id.minusQty);
        prodCount = findViewById(R.id.quantity);

        // quantity of products
        prodQty = Integer.parseInt(prodCount.getText().toString());

        productRef = FirebaseDatabase.getInstance().getReference().child("products")
                .child(categoryKey).child(productID); // products > categoryKey > productID

        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // fetch item details...
                engName = snapshot.child("engName").getValue(String.class);
                japName = snapshot.child("japName").getValue(String.class);
                price = snapshot.child("price").getValue(Integer.class);
                image = snapshot.child("image").getValue(String.class);

                // and set it in the TextView and ImageView
                englishName.setText(engName);
                japaneseName.setText("(" + japName + ")");
                prodPrice.setText(getString(R.string.peso_sign) + price);
                Picasso.get().load(image).into(prodImg);
                setPrice(price, prodQty);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodInfo.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });


        // back button pressed
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // add to order pressed
        addToOrder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String productToAdd = productID;
                            int qtyToAdd = Integer.parseInt(prodCount.getText().toString());

                            // Hashing details
                            Map<String, Object> cartItem = new HashMap<>();
                            cartItem.put("productID", productToAdd);
                            cartItem.put("quantity", qtyToAdd);
                            cartItem.put("categoryKey", categoryKey);

                            cartRef = userRef.child("cartItems").push();
                            cartRef.setValue(cartItem);

                            Toast.makeText(FoodInfo.this, "Added!", Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FoodInfo.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // view orders clicked
        viewOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodInfo.this, Orders.class);
                startActivity(intent);
                finish();
            }
        });

        // minus button clicked
        subQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(prodQty > 1){
                    prodQty = prodQty - 1;
                    prodCount.setText(Integer.toString(prodQty));
                    setPrice(price, prodQty);
                }
            }
        });

        // add button clicked
        addQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prodQty = prodQty + 1;
                prodCount.setText(Integer.toString(prodQty));
                setPrice(price, prodQty);
            }
        });
    }

    // set price function for shortcut
    private Integer setPrice(int price, int quantity){
        int finalPrice = price * quantity;
        priceDisplay.setText(getString(R.string.price) + finalPrice);

        return finalPrice;
    }
}