package com.example.smartexpensetracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class ExpenseEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String description;
    public String category;
    public double amount;
    public long timestamp;

    public ExpenseEntity(String description, String category, double amount, long timestamp) {
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}
