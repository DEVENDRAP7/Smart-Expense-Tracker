package com.example.smartexpensetracker.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(ExpenseEntity expense);

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    List<ExpenseEntity> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    List<ExpenseEntity> getExpensesBetween(long from, long to);

    @Delete
    void delete(ExpenseEntity expense);

    @Query("DELETE FROM expenses")
    void deleteAll();

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    ExpenseEntity getById(int id);

    // NEW: update an expense
    @Update
    void update(ExpenseEntity expense);
}
