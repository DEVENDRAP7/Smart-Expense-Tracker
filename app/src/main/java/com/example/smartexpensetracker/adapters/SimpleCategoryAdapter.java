package com.example.smartexpensetracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpensetracker.R;
import com.example.smartexpensetracker.models.CategoryAmount;

import java.util.List;
import java.util.Locale;

public class SimpleCategoryAdapter extends RecyclerView.Adapter<SimpleCategoryAdapter.VH> {

    private List<CategoryAmount> list;

    public SimpleCategoryAdapter(List<CategoryAmount> list) {
        this.list = list;
    }

    public void setList(List<CategoryAmount> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SimpleCategoryAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleCategoryAdapter.VH holder, int position) {
        CategoryAmount ca = list.get(position);
        holder.tvCat.setText(ca.category);
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", ca.amount));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCat, tvAmount;
        VH(@NonNull View itemView) {
            super(itemView);
            tvCat = itemView.findViewById(R.id.tvCatName);
            tvAmount = itemView.findViewById(R.id.tvCatAmount);
        }
    }
}
