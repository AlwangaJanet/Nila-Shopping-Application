package com.android.NilaAp.Activity;

import static com.android.NilaAp.Fragment.HomeFragment.clearClicked;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.NilaAp.Fragment.HomeFragment;
import com.android.NilaAp.R;

public class SearchFiltersActivity extends AppCompatActivity implements View.OnClickListener {
    TextView clearFilters;
    LinearLayout categoryAlaCarte, categoryBento, categoryHandRoll, categoryBeverage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filters);

        initAll();

        clearFilters.setOnClickListener(this);

        categoryAlaCarte.setOnClickListener(this);
        categoryBento.setOnClickListener(this);
        categoryHandRoll.setOnClickListener(this);
        categoryBeverage.setOnClickListener(this);


    }

    private void initAll() {
        clearFilters = findViewById(R.id.id_clear_btn);
        categoryAlaCarte = findViewById(R.id.cat_alacart);
        categoryBento = findViewById(R.id.cat_bento);
        categoryHandRoll = findViewById(R.id.cat_handroll);
        categoryBeverage = findViewById(R.id.cat_beverage);
    }

    public void goBack(View view) {
        finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.cat_alacart) {
            HomeFragment.category = "Dresses Skirts";
        } else if (id == R.id.cat_bento) {
            HomeFragment.category = "Trousers";
        } else if (id == R.id.cat_handroll) {
            HomeFragment.category = "Shirts";
        } else if (id == R.id.cat_beverage) {
            HomeFragment.category = "Jumpsuits";
        } else if (id == R.id.id_clear_btn) {
            clearClicked();
        }

        HomeFragment.isCategorySeleted = true;
        HomeFragment.isFiltersApplied = true;
        finish();
        }
}