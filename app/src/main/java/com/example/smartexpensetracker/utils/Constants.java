package com.example.smartexpensetracker.utils;

public class Constants {
    // Replace with your key; for learning only — don't hardcode in production
    public static final String GEMINI_API_KEY = "Your Key";

    public static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;

    public static final String[] CATEGORIES = new String[] {
            "Food", "Travel", "Shopping", "Entertainment", "Bills", "Medical", "Other" , "Education", "Investments", "Assets" , "Liabilities" , "Gifts" , "Lending" , "Insurance" , "Interest Payments/Loans"
    };
}
