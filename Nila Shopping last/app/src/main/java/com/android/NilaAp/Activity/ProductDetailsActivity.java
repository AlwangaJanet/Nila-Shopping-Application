package com.android.NilaAp.Activity;

import static com.android.NilaAp.Fragment.PreOrderFragment.context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.NilaAp.Model.Order;
import com.android.NilaAp.Model.Product;
import com.android.NilaAp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class ProductDetailsActivity extends AppCompatActivity {

    private CardView addToCartBtn;
    private ImageView productImg;
    private TextView plusBTn,minusBtn,quantityTV;
    private TextView productName,productDescription,price;
    Product product, preOrderProduct;
    RatingBar productRatingBar;
    Button saveRatingButton;

    int quantity=1;

    private Order order;
    CheckBox smallCheckBox, mediumCheckBox, largeCheckBox, extraLargeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        initAll();
        initViews(); // Call to initialize UI views
        ClickListeners();

        product= (Product) getIntent().getSerializableExtra("product");
        preOrderProduct = (Product) getIntent().getSerializableExtra("Pre-Order Product");

        // Initialize checkboxes for sizes
        smallCheckBox = findViewById(R.id.details_size_small_checkbox);
        mediumCheckBox = findViewById(R.id.details_size_medium_checkbox);
        largeCheckBox = findViewById(R.id.details_size_large_checkbox);
        extraLargeCheckBox = findViewById(R.id.details_size_extra_large_checkbox);
        
       

        // Set up click listeners for checkboxes to allow only one size selection
        setUpSizeSelection();

        // Retrieve available sizes and update checkboxes
        updateSizeCheckboxes(product.getSizes());

//        CheckBox smallCheckBox, mediumCheckBox, largeCheckBox, extraLargeCheckBox;
        if(product.getPhotoUrl()!=null){
            if(!product.getPhotoUrl().equals("")){
                Picasso.get().load(product.getPhotoUrl()).placeholder(R.drawable.icon).into(productImg);
            }
        }
        productName.setText(product.getName());
        productDescription.setText(product.getDescription());
        price.setText("Ksh"+product.getPrice());

    }

    private void updateSizeCheckboxes(List<String> availableSizes) {
        if (availableSizes != null) {
            for (String size : availableSizes) {
                switch (size) {
                    case "Small":
                        smallCheckBox.setChecked(true);
                        break;
                    case "Medium":
                        mediumCheckBox.setChecked(true);
                        break;
                    case "Large":
                        largeCheckBox.setChecked(true);
                        break;
                    case "Extra Large":
                        extraLargeCheckBox.setChecked(true);
                        break;
                    // Add more cases if needed for additional sizes
                }
            }
        }
    }

    private void setUpSizeSelection() {
        smallCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!product.getSizes().contains("Small")) {
                    smallCheckBox.setChecked(false);
                    Toast.makeText(ProductDetailsActivity.this, "Small size is not available", Toast.LENGTH_SHORT).show();
                } else {
                    if (smallCheckBox.isChecked()) {
                        mediumCheckBox.setChecked(false);
                        largeCheckBox.setChecked(false);
                        extraLargeCheckBox.setChecked(false);
                    }
                }
            }
        });

        mediumCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!product.getSizes().contains("Medium")) {
                    mediumCheckBox.setChecked(false);
                    Toast.makeText(ProductDetailsActivity.this, "Medium size is not available", Toast.LENGTH_SHORT).show();
                } else {
                    if (mediumCheckBox.isChecked()) {
                        smallCheckBox.setChecked(false);
                        largeCheckBox.setChecked(false);
                        extraLargeCheckBox.setChecked(false);
                    }
                }
            }
        });

        largeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!product.getSizes().contains("Large")) {
                    largeCheckBox.setChecked(false);
                    Toast.makeText(ProductDetailsActivity.this, "Large size is not available", Toast.LENGTH_SHORT).show();
                } else {
                    if (largeCheckBox.isChecked()) {
                        smallCheckBox.setChecked(false);
                        mediumCheckBox.setChecked(false);
                        extraLargeCheckBox.setChecked(false);
                    }
                }
            }
        });

        extraLargeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!product.getSizes().contains("Extra Large")) {
                    extraLargeCheckBox.setChecked(false);
                    Toast.makeText(ProductDetailsActivity.this, "Extra Large size is not available", Toast.LENGTH_SHORT).show();
                } else {
                    if (extraLargeCheckBox.isChecked()) {
                        smallCheckBox.setChecked(false);
                        mediumCheckBox.setChecked(false);
                        largeCheckBox.setChecked(false);
                    }
                }
            }
        });
    }

    private boolean isMultipleSizesSelected() {
        int count = 0;
        if (smallCheckBox.isChecked()) count++;
        if (mediumCheckBox.isChecked()) count++;
        if (largeCheckBox.isChecked()) count++;
        if (extraLargeCheckBox.isChecked()) count++;
        return count > 1;
    }



    private void ClickListeners() {
        plusBTn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity+=1;
                quantityTV.setText(String.valueOf(quantity));
            }
        });

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(quantity>1){
                    quantity-=1;
                    quantityTV.setText(String.valueOf(quantity));
                }
            }
        });

        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMultipleSizesSelected()) {
                    Toast.makeText(ProductDetailsActivity.this, "Please choose only one size", Toast.LENGTH_SHORT).show();
                } else {
                    // Proceed with adding to cart
                    boolean isInCart=false;
                    for(int i=0;i<order.getCartProductList().size();i++){
                        if(product.getProductId().equals(order.getCartProductList().get(i).getProductId())){
                            isInCart=true;
                            break;
                        }
                    }
                    // Check if any size checkbox is selected
                    if (!smallCheckBox.isChecked() && !mediumCheckBox.isChecked() && !largeCheckBox.isChecked() && !extraLargeCheckBox.isChecked()) {
                        Toast.makeText(ProductDetailsActivity.this, "Please select a size", Toast.LENGTH_SHORT).show();
                        return; // Exit onClick without adding to cart
                    }
                    if(!isInCart){
                        // Set the selected size to the product
                        List<String> selectedSizes = new ArrayList<>();
                        if (smallCheckBox.isChecked()) {
                            selectedSizes.add("Small");
                        }
                        if (mediumCheckBox.isChecked()) {
                            selectedSizes.add("Medium");
                        }
                        if (largeCheckBox.isChecked()) {
                            selectedSizes.add("Large");
                        }
                        if (extraLargeCheckBox.isChecked()) {
                            selectedSizes.add("Extra Large");
                        }

                        // Set selected sizes to the product
                        product.setSizes(selectedSizes);
                        product.setQuantityInCart(quantity);
                        order.addProduct(product);
                        Log.d("testorder",order.getTotalPrice()+ "");
                        Paper.book().delete("order");
                        Paper.book().write("order",order);
                        Toast.makeText(ProductDetailsActivity.this,"Added to cart",Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else{
                        Toast.makeText(ProductDetailsActivity.this,"Already in cart",Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }

    private void initAll() {
        addToCartBtn=findViewById(R.id.add_to_cart_btn);
        productImg=findViewById(R.id.product_img);
        plusBTn=findViewById(R.id.plus_btn);
        minusBtn=findViewById(R.id.minus_btn);
        quantityTV=findViewById(R.id.quantity_tv);
        productName=findViewById(R.id.product_name);
        price=findViewById(R.id.product_price);
        productDescription=findViewById(R.id.product_description);

        product=new Product();

        preOrderProduct = new Product();

        order=new Order();

        if(Paper.book().read("order")!=null){
            order=Paper.book().read("order");
        }

    }

    public void goBack(View view) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Paper.book().read("order")!=null){
            order=Paper.book().read("order");
        }
    }
    
     //Initialize UI elements and set up click listeners
    private void initViews() {
        productRatingBar = findViewById(R.id.product_rating_bar);
        saveRatingButton = findViewById(R.id.save_rating_button);

        saveRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProductRatingToDatabase(productRatingBar.getRating());
            }
        });
    }

    // Save product rating to the database
    private void saveProductRatingToDatabase(float rating) {
        // Update the rating field of the current product
        product.setRating(rating);

        // Get a reference to the product entry in the Firebase Realtime Database
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference()
                .child("Products")
                .child(product.getProductId());

        // Update the rating field of the product entry
        productRef.child("rating").setValue(rating)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Database update successful
                        Toast.makeText(ProductDetailsActivity.this, "Rating saved successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Database update failed
                        Log.e("ProductDetailsActivity", "Failed to save rating: " + e.getMessage());
                        Toast.makeText(ProductDetailsActivity.this, "Failed to save rating", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}