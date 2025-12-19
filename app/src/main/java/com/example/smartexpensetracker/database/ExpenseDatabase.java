package com.example.smartexpensetracker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ExpenseEntity.class}, version = 1, exportSchema = false)
public abstract class ExpenseDatabase extends RoomDatabase {

    private static final String DB_NAME = "expense_db";
    private static volatile ExpenseDatabase instance;

    public abstract ExpenseDao expenseDao();

    public static ExpenseDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (ExpenseDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    ExpenseDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
