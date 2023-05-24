package com.example.konbinipos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;


public class Gohan extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private ImageView menuToggle;
    private LinearLayout product_place;
    private DrawerLayout drawerLayout;

    // firebase database reference
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference productRef = firebaseDatabase.getReference("products"); // products >

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konbini);

        // firebase instance
        auth = FirebaseAuth.getInstance();

        // component fetching
//        logout_btn = findViewById(R.id.logout);
        product_place = findViewById(R.id.product_place);
        menuToggle = findViewById(R.id.menuToggle);
        drawerLayout = findViewById(R.id.drawerLayout);

        // if no user check
        user = auth.getCurrentUser();
        if(user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }else{
            NavigationView navigationView = findViewById(R.id.navigation_view);
            View headerView = navigationView.getHeaderView(0);
            TextView currentUserName = headerView.findViewById(R.id.userName);
            TextView currentUserEmail = headerView.findViewById(R.id.userEmail);

            currentUserName.setText(user.getDisplayName());
            currentUserEmail.setText(user.getEmail());
        }

        fetchItemsByCategory("all");

        menuToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    public void categoryChosen(MenuItem item){
        int itemID = item.getItemId();

        if(itemID == R.id.allFood){
            fetchItemsByCategory("all");
        }
        else if(itemID == R.id.donburi){
            fetchItemsByCategory("donburi");
        }
        else if(itemID == R.id.ramen){
            fetchItemsByCategory("ramen");
        }
        else if(itemID == R.id.soup){
            fetchItemsByCategory("soup");
        }
        else if(itemID == R.id.sushi){
            fetchItemsByCategory("sushi");
        }
        else if(itemID == R.id.yakitori){
            fetchItemsByCategory("yakitori");
        }
        else if(itemID == R.id.takoyaki){
            fetchItemsByCategory("takoyaki");
        }
        else if(itemID == R.id.sides){
            fetchItemsByCategory("sides");
        }
        else if(itemID == R.id.desserts){
            fetchItemsByCategory("dessert");
        }

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void logOut(MenuItem item){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    private void fetchItemsByCategory(String category){

        //remove all other views
        product_place.removeAllViews();

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float scale = getResources().getDisplayMetrics().density;
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryName = categorySnapshot.getKey();

                    if (category.equals("all") || category.equals(categoryName)) {
                        // Create and add the category label
                        TextView categoryTitle = new TextView(Gohan.this);
                        categoryTitle.setText(categoryName.toUpperCase());
                        categoryTitle.setAllCaps(true);
                        categoryTitle.setTypeface(null, Typeface.BOLD);
                        categoryTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        categoryTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                        product_place.addView(categoryTitle);

                        // Create a flexbox layout for the products
                        FlexboxLayout flexboxLayout = new FlexboxLayout(Gohan.this);
                        flexboxLayout.setFlexWrap(FlexWrap.WRAP);
                        flexboxLayout.setJustifyContent(JustifyContent.SPACE_BETWEEN);
                        int xPadding = 30; // Replace with your desired width in dp
                        int yPadding = 20; // Replace with your desired height in dp
                        int xPx = (int) (xPadding * scale + 0.5f);
                        int yPx = (int) (yPadding * scale + 0.5f);
                        flexboxLayout.setPadding(xPx, yPx, xPx, 0);

//                        // Convert dp to pixels
//
//                        int pixels = (int) (70 * scale + 0.5f);
//
//                        // Set margin using pixels
//                        FlexboxLayout.LayoutParams flexboxLayoutParams = new FlexboxLayout.LayoutParams(
//                                FlexboxLayout.LayoutParams.MATCH_PARENT,
//                                FlexboxLayout.LayoutParams.WRAP_CONTENT
//                        );
//                        flexboxLayoutParams.setMargins(0, 0, 0, pixels);
//                        flexboxLayout.setLayoutParams(flexboxLayoutParams);

                        product_place.addView(flexboxLayout);

                        // Iterate over the products in the category
                        for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
                            // Fetch product details
                            String engName = productSnapshot.child("engName").getValue(String.class);
                            String japName = productSnapshot.child("japName").getValue(String.class);
                            String image = productSnapshot.child("image").getValue(String.class);
                            int price = productSnapshot.child("price").getValue(Integer.class);

                            // Create LinearLayout to hold product details
                            LinearLayout productLayout = new LinearLayout(Gohan.this);
                            productLayout.setOrientation(LinearLayout.VERTICAL);

                            // Set background color
                            productLayout.setBackgroundResource(R.drawable.rounded_texteditor);

                            // Set padding
                            int paddingInPixels = (int) (4 * getResources().getDisplayMetrics().density); // Convert 8dp to pixels
                            productLayout.setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels);

                            // Convert dp to pixels
                            int marginPixels = (int) (40 * scale + 0.5f);

                            // Set margins for the productLayout
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            layoutParams.setMargins(0, 0, 0, marginPixels);
                            productLayout.setLayoutParams(layoutParams);

                            // Create TextView to display product name and price
                            TextView prodEngName = new TextView(Gohan.this);
                            prodEngName.setText(engName);
                            prodEngName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            prodEngName.setTypeface(null, Typeface.BOLD);
                            prodEngName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            prodEngName.setTextColor(Color.parseColor("#767171"));

                            TextView prodJapName = new TextView(Gohan.this);
                            prodJapName.setText("(" + japName + ")");
                            prodJapName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            prodJapName.setTypeface(null, Typeface.BOLD_ITALIC);
                            prodJapName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            prodJapName.setTextColor(Color.parseColor("#767171"));

                            TextView prodPrice = new TextView(Gohan.this);
                            prodPrice.setText("₱" + price);
                            prodPrice.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            prodPrice.setTextColor(Color.parseColor("#B1464A"));

                            // Create ImageView to display product image
                            ImageView prodImg = new ImageView(Gohan.this);

                            int widthInDp = 150; // Replace with your desired width in dp
                            int heightInDp = 150; // Replace with your desired height in dp
                            int widthInPixels = (int) (widthInDp * scale + 0.5f);
                            int heightInPixels = (int) (heightInDp * scale + 0.5f);

                            prodImg.setLayoutParams(new LinearLayout.LayoutParams(widthInPixels, heightInPixels));
                            Picasso.get().load(image).into(prodImg);

                            // Add TextView and ImageView to the product LinearLayout
                            productLayout.addView(prodImg);
                            productLayout.addView(prodEngName);
                            productLayout.addView(prodJapName);
                            productLayout.addView(prodPrice);

                            // Add the product to the flexbox layout
                            flexboxLayout.addView(productLayout);

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}