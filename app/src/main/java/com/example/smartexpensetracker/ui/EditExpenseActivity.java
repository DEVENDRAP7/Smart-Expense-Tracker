package com.example.smartexpensetracker.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.example.smartexpensetracker.R;
import com.example.smartexpensetracker.database.ExpenseDatabase;
import com.example.smartexpensetracker.database.ExpenseEntity;
import com.example.smartexpensetracker.utils.Constants;

import java.util.concurrent.Executors;

public class EditExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_EXPENSE_ID = "extra_expense_id";

    private TextInputEditText edtDesc, edtAmount;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel;

    private ExpenseEntity current;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        edtDesc = findViewById(R.id.edtEditDescription);
        edtAmount = findViewById(R.id.edtEditAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSaveEdit);
        btnCancel = findViewById(R.id.btnCancelEdit);

        // setup spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Constants.CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        int expenseId = getIntent().getIntExtra(EXTRA_EXPENSE_ID, -1);
        if (expenseId == -1) {
            Toast.makeText(this, "Invalid expense", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // load expense from DB
        Executors.newSingleThreadExecutor().execute(() -> {
            ExpenseEntity e = ExpenseDatabase.getInstance(getApplicationContext()).expenseDao().getById(expenseId);
            if (e == null) {
                runOnUiThread(() -> {
                    Toast.makeText(EditExpenseActivity.this, "Expense not found", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            current = e;
            runOnUiThread(() -> populateFields(e));
        });

        btnSave.setOnClickListener(v -> onSaveClicked());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void populateFields(ExpenseEntity e) {
        edtDesc.setText(e.description);
        edtAmount.setText(String.valueOf(e.amount));
        // set spinner position
        for (int i = 0; i < Constants.CATEGORIES.length; i++) {
            if (Constants.CATEGORIES[i].equalsIgnoreCase(e.category)) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void onSaveClicked() {
        if (current == null) return;

        String desc = edtDesc.getText() != null ? edtDesc.getText().toString().trim() : "";
        String amtStr = edtAmount.getText() != null ? edtAmount.getText().toString().trim() : "";

        if (TextUtils.isEmpty(desc)) {
            edtDesc.setError("Enter description");
            return;
        }
        if (TextUtils.isEmpty(amtStr)) {
            edtAmount.setError("Enter amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
        } catch (NumberFormatException ex) {
            edtAmount.setError("Invalid number");
            return;
        }

        String cat = (String) spinnerCategory.getSelectedItem();

        // update object and DB
        current.description = desc;
        current.amount = amount;
        current.category = cat;

        Executors.newSingleThreadExecutor().execute(() -> {
            ExpenseDatabase.getInstance(getApplicationContext()).expenseDao().update(current);
            runOnUiThread(() -> {
                Toast.makeText(EditExpenseActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
