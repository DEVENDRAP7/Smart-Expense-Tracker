package com.example.smartexpensetracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpensetracker.R;
import com.example.smartexpensetracker.adapters.ExpenseAdapter;
import com.example.smartexpensetracker.database.ExpenseDatabase;
import com.example.smartexpensetracker.database.ExpenseEntity;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rv;
    private Button btn;
    private ExpenseAdapter adapter;
    private TextView tvEmpty, tvTotal;
    private View rootView; // for Snackbar

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rootView = findViewById(android.R.id.content);
        rv = findViewById(R.id.rvExpenses);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTotal = findViewById(R.id.tvTotal);
        btn= findViewById(R.id.Back);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, AddExpenseActivity.class);
                startActivity(intent);
            }
        });
        adapter = new ExpenseAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        adapter.setOnItemLongClickListener(expense -> {
            new AlertDialog.Builder(HistoryActivity.this)
                    .setTitle("Delete")
                    .setMessage("Delete this expense?\n\n" + expense.description + "\n₹" + expense.amount)
                    .setPositiveButton("Delete", (dialog, which) -> deleteExpense(expense))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        adapter.setOnItemClickListener(expense -> {
            Intent i = new Intent(HistoryActivity.this, EditExpenseActivity.class);
            i.putExtra(EditExpenseActivity.EXTRA_EXPENSE_ID, expense.id);
            startActivity(i);
        });


        // attach swipe-to-delete
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false; // we are not moving items
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                ExpenseEntity removed = adapter.getItem(pos);

                // remove from adapter immediately
                adapter.removeAt(pos);

                // update total/empty state
                updateEmptyAndTotalAfterRemoval();

                // delete from DB on background thread
                Executors.newSingleThreadExecutor().execute(() -> {
                    ExpenseDatabase.getInstance(getApplicationContext()).expenseDao().delete(removed);
                });

                // show snackbar with undo
                Snackbar.make(rootView, "Expense deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            // re-insert to DB and refresh list
                            Executors.newSingleThreadExecutor().execute(() -> {
                                ExpenseDatabase.getInstance(getApplicationContext()).expenseDao().insert(removed);
                                // refresh UI on main thread - use Activity's runOnUiThread
                                HistoryActivity.this.runOnUiThread(() -> loadExpenses());
                            });
                        })
                        .show();
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rv);

        loadExpenses();
    }

    private void loadExpenses() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ExpenseEntity> list = ExpenseDatabase.getInstance(getApplicationContext())
                    .expenseDao()
                    .getAllExpenses();

            runOnUiThread(() -> {
                if (list == null || list.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                    tvTotal.setText("Total: ₹0.00");
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    adapter.setList(list);
                    tvTotal.setText("Total: ₹" + String.format("%.2f", calculateTotal(list)));
                }
            });
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses(); // refresh after possible edits
    }


    private void updateEmptyAndTotalAfterRemoval() {
        // recalc using adapter's data
        List<ExpenseEntity> current = new ArrayList<>();
        // no direct getter for list, so we rebuild by reading adapter positions
        int cnt = adapter.getItemCount();
        for (int i = 0; i < cnt; i++) {
            ExpenseEntity e = adapter.getItem(i);
            if (e != null) current.add(e);
        }

        if (current.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            tvTotal.setText("Total: ₹0.00");
        } else {
            tvEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            tvTotal.setText("Total: ₹" + String.format("%.2f", calculateTotal(current)));
        }
    }

    private double calculateTotal(List<ExpenseEntity> list) {
        double sum = 0;
        for (ExpenseEntity e : list) sum += e.amount;
        return sum;
    }

    private void deleteExpense(ExpenseEntity e) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ExpenseDatabase.getInstance(getApplicationContext()).expenseDao().delete(e);

            runOnUiThread(() -> {
                Toast.makeText(HistoryActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                loadExpenses();
            });
        });
    }
}
