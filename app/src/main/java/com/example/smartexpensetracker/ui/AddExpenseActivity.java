package com.example.smartexpensetracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.example.smartexpensetracker.R;
import com.example.smartexpensetracker.database.ExpenseDatabase;
import com.example.smartexpensetracker.database.ExpenseEntity;
import com.example.smartexpensetracker.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddExpenseActivity extends AppCompatActivity {

    private TextInputEditText edtDescription, edtAmount;
    private Button btnCategorize;
    private ProgressBar progress;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        edtDescription = findViewById(R.id.edtDescription);
        edtAmount = findViewById(R.id.edtAmount);
        btnCategorize = findViewById(R.id.btnCategorize);
        progress = findViewById(R.id.progress);

        Button btnGoHistory = findViewById(R.id.btnGoHistory);
        Button btnGoDashboard = findViewById(R.id.btnGoDashboard);

        btnGoHistory.setOnClickListener(v -> {
            startActivity(new Intent(AddExpenseActivity.this, HistoryActivity.class));
        });

        btnGoDashboard.setOnClickListener(v -> {
            startActivity(new Intent(AddExpenseActivity.this, DashboardActivity.class));
        });


        btnCategorize.setOnClickListener(v -> onCategorizeClicked());
    }

    private void onCategorizeClicked() {
        String description = edtDescription.getText() != null ? edtDescription.getText().toString().trim() : "";
        String amountStr = edtAmount.getText() != null ? edtAmount.getText().toString().trim() : "";

        if (TextUtils.isEmpty(description)) {
            edtDescription.setError("Enter description");
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            edtAmount.setError("Enter amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException ex) {
            edtAmount.setError("Invalid number");
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnCategorize.setEnabled(false);

        callGeminiForCategory(description, new CategoryCallback() {
            @Override
            public void onResult(String category) {
                String mapped = sanitizeCategory(category);
                saveExpenseToDb(description, amount, mapped);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    btnCategorize.setEnabled(true);
                    Toast.makeText(AddExpenseActivity.this, "AI Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveExpenseToDb(String description, double amount, String category) {
        ExpenseEntity expense = new ExpenseEntity(description, category, amount, System.currentTimeMillis());

        Executors.newSingleThreadExecutor().execute(() -> {
            ExpenseDatabase.getInstance(getApplicationContext())
                    .expenseDao()
                    .insert(expense);

            runOnUiThread(() -> {
                progress.setVisibility(View.GONE);
                btnCategorize.setEnabled(true);
                Toast.makeText(AddExpenseActivity.this,
                        "Saved under: " + category, Toast.LENGTH_SHORT).show();

                edtDescription.setText("");
                edtAmount.setText("");
            });
        });
    }

    private interface CategoryCallback {
        void onResult(String category);
        void onError(String error);
    }

    private void callGeminiForCategory(String expenseText, CategoryCallback callback) {
        String prompt = "Classify this expense into one of these categories: " +
                String.join(", ", Constants.CATEGORIES) +
                ". Respond with only the single category word.\n\nExpense: \"" + expenseText + "\"";

        try {
            JSONObject root = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("parts", new JSONArray().put(new JSONObject().put("text", prompt)));
            contents.put(message);
            root.put("contents", contents);

            RequestBody body = RequestBody.create(root.toString(),
                    MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(Constants.GEMINI_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError("HTTP " + response.code());
                        return;
                    }

                    String res = response.body() != null ? response.body().string() : "";
                    try {
                        JSONObject obj = new JSONObject(res);
                        String text = null;
                        if (obj.has("candidates")) {
                            text = obj.getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");
                        } else if (obj.has("outputs")) {
                            text = obj.getJSONArray("outputs")
                                    .getJSONObject(0)
                                    .getString("content");
                        } else if (obj.has("choices")) {
                            text = obj.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");
                        } else {
                            text = obj.optString("text", null);
                        }

                        if (text == null) text = res;
                        final String finalText = text.trim();
                        callback.onResult(finalText);

                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private String sanitizeCategory(String raw) {
        if (raw == null) return "Other";
        String r = raw.trim().toLowerCase();
        r = r.replaceAll("[^a-zA-Z ]", "").trim();

        for (String cat : Constants.CATEGORIES) {
            if (cat == null) continue;
            String lc = cat.toLowerCase();
            if (r.equals(lc)) return cat;
            if (r.contains(lc)) return cat;
            if (lc.equals("bills") && (r.contains("bill") || r.contains("utility") || r.contains("electricity")))
                return cat;
            if (lc.equals("food") && (r.contains("restaurant") || r.contains("lunch") || r.contains("dinner") || r.contains("food")))
                return cat;
            if (lc.equals("travel") && (r.contains("taxi") || r.contains("uber") || r.contains("fuel") || r.contains("petrol") || r.contains("bus") || r.contains("train")))
                return cat;
            if (lc.equals("entertainment") && (r.contains("netflix") || r.contains("movie") || r.contains("spotify") || r.contains("entertainment")))
                return cat;
            if (lc.equals("shopping") && (r.contains("amazon") || r.contains("flipkart") || r.contains("shop") || r.contains("shopping")))
                return cat;
            if (lc.equals("medical") && (r.contains("hospital") || r.contains("doctor") || r.contains("medicine") || r.contains("medical")))
                return cat;
        }

        String[] tokens = r.split("\\s+");
        if (tokens.length > 0) {
            for (String cat : Constants.CATEGORIES) {
                if (tokens[0].equalsIgnoreCase(cat)) return cat;
            }
        }
        return "Other";
    }
}
