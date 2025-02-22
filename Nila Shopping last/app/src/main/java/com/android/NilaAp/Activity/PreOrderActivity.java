package com.android.NilaAp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.NilaAp.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.NilaAp.Adapter.PreOrderProductsAdapter;
import com.android.NilaAp.Model.Product;
import com.android.NilaAp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PreOrderActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView noJokeText;
    private ImageView filtersBtn;

    private static PreOrderProductsAdapter mAdapter;
    private static RecyclerView recyclerView;
    private static ArrayList<Product> productArrayList;
    private ArrayList<Product> tempList;

    private DatabaseReference myRootRef;
    public static String category = "";

    private static EditText nameInput;
    public static boolean isCategorySeleted = false;
    public static boolean isFiltersApplied = false;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_order);

        productArrayList = new ArrayList<>();
        tempList = new ArrayList<>();
        recyclerView = findViewById(R.id.pre_product_list);
        progressBar = findViewById(R.id.pre_spin_progress_bar);
        noJokeText = findViewById(R.id.pre_no_product);
        nameInput = findViewById(R.id.pre_name_input);
        filtersBtn = findViewById(R.id.filters_btn);

        context = this;

        myRootRef = FirebaseDatabase.getInstance().getReference();

        mAdapter = new PreOrderProductsAdapter(productArrayList, this, false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        settingClickListeners();

        getDataFromFirebase();

        searchFunc();
    }

    private void ApplyFilters() {
        if (tempList.size() > 0) {
            tempList.clear();
        }
        if (isCategorySeleted) {
            for (Product element : productArrayList) {
                if (element.getCategory().equals(category)) {
                    tempList.add(element);
                }
            }
        }

        if (tempList.size() != 0) {
            recyclerView.setVisibility(View.VISIBLE);
            noJokeText.setVisibility(View.GONE);

            mAdapter = new PreOrderProductsAdapter(tempList, this, false);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        } else {
            recyclerView.setVisibility(View.GONE);
            noJokeText.setVisibility(View.VISIBLE);
        }
    }

    private void searchFunc() {
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    if (productArrayList.size() != 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        noJokeText.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        noJokeText.setVisibility(View.VISIBLE);
                    }

                    mAdapter = new PreOrderProductsAdapter(productArrayList, PreOrderActivity.this, false);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                } else {
                    ArrayList<Product> clone = new ArrayList<>();
                    for (Product element : productArrayList) {
                        if (element.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                            clone.add(element);
                        }
                    }
                    if (clone.size() != 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        noJokeText.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        noJokeText.setVisibility(View.VISIBLE);
                    }

                    mAdapter = new PreOrderProductsAdapter(clone, PreOrderActivity.this, false);
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void settingClickListeners() {
        filtersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PreOrderActivity.this, SearchFiltersActivity.class));
            }
        });
    }

    public void getDataFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        final int[] counter = {0};
        myRootRef.child("Pre-Order Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Product product = child.getValue(Product.class);
                        if (product != null) {
                            productArrayList.add(product);
                        }
                        counter[0]++;
                        if (counter[0] == dataSnapshot.getChildrenCount()) {
                            setData();
                            progressBar.setVisibility(View.GONE);
                        }
                        Log.d("ShowEventInfo:", String.valueOf(product));
                    }
                } else {
                    noJokeText.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public static void clearClicked() {
        isCategorySeleted = false;
        isFiltersApplied = false;

        mAdapter = new PreOrderProductsAdapter(productArrayList, (Activity) context, false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        nameInput.setText("");
    }

    private void setData() {
        if (productArrayList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            noJokeText.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            noJokeText.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFiltersApplied) {
            ApplyFilters();
        }
    }
}
