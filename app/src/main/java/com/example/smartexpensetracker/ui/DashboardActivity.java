package com.example.smartexpensetracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpensetracker.R;
import com.example.smartexpensetracker.adapters.SimpleCategoryAdapter;
import com.example.smartexpensetracker.database.ExpenseDatabase;
import com.example.smartexpensetracker.database.ExpenseEntity;
import com.example.smartexpensetracker.models.CategoryAmount;
import com.example.smartexpensetracker.utils.Constants;
import com.example.smartexpensetracker.views.PieChartView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private PieChartView pie;
    private Button hist,exp;
    private RecyclerView rvCategoryList;
    private TextView tvEmpty;
    private SimpleCategoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        pie = findViewById(R.id.customPie);
        rvCategoryList = findViewById(R.id.rvCategoryList);
        tvEmpty = findViewById(R.id.tvEmptyDashboard);
        hist = findViewById(R.id.hist);
        exp = findViewById(R.id.exp);

        hist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        exp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddExpenseActivity.class);
                startActivity(intent);
            }
        });

        rvCategoryList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleCategoryAdapter(new ArrayList<>());
        rvCategoryList.setAdapter(adapter);

        loadAndRenderData();
    }

    private void loadAndRenderData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ExpenseEntity> list = ExpenseDatabase.getInstance(getApplicationContext()).expenseDao().getAllExpenses();

            Map<String, Double> totals = new HashMap<>();
            for (String c : Constants.CATEGORIES) totals.put(c, 0.0);

            if (list != null) {
                for (ExpenseEntity e : list) {
                    String cat = e.category != null ? e.category : "Other";
                    if (!totals.containsKey(cat)) {
                        totals.put("Other", totals.getOrDefault("Other", 0.0) + e.amount);
                    } else {
                        totals.put(cat, totals.get(cat) + e.amount);
                    }
                }
            }

            // build category list for adapter & pie map
            final Map<String, Double> pieMap = new HashMap<>();
            final List<CategoryAmount> catList = new ArrayList<>();
            for (String cat : Constants.CATEGORIES) {
                double v = totals.getOrDefault(cat, 0.0);
                if (v > 0) {
                    pieMap.put(cat, v);
                    catList.add(new CategoryAmount(cat, v));
                }
            }

            final boolean empty = pieMap.isEmpty();

            runOnUiThread(() -> {
                if (empty) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    pie.setVisibility(View.GONE);
                    rvCategoryList.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    pie.setVisibility(View.VISIBLE);
                    rvCategoryList.setVisibility(View.VISIBLE);

                    pie.setData(pieMap);
                    pie.setCenterText("Spending");
                    adapter.setList(catList);
                }
            });
        });
    }
}
