package com.example.smartexpensetracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpensetracker.R;
import com.example.smartexpensetracker.database.ExpenseEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.VH> {

    private List<ExpenseEntity> list;
    private OnItemLongClickListener longClickListener;
    private OnItemClickListener clickListener; // NEW

    public ExpenseAdapter(List<ExpenseEntity> list) {
        this.list = list;
    }

    public void setList(List<ExpenseEntity> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public ExpenseEntity getItem(int pos) {
        if (list == null || pos < 0 || pos >= list.size()) return null;
        return list.get(pos);
    }

    public void removeAt(int pos) {
        if (list == null || pos < 0 || pos >= list.size()) return;
        list.remove(pos);
        notifyItemRemoved(pos);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ExpenseEntity e = list.get(position);
        holder.tvDesc.setText(e.description != null ? e.description : "");
        holder.tvCategory.setText(e.category != null ? e.category : "Other");
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", e.amount));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvTimestamp.setText(sdf.format(new Date(e.timestamp)));

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(e);
                return true;
            }
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(e);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDesc, tvCategory, tvAmount, tvTimestamp;
        VH(@NonNull View itemView) {
            super(itemView);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(ExpenseEntity expense);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        this.longClickListener = l;
    }

    // NEW: click listener
    public interface OnItemClickListener {
        void onItemClick(ExpenseEntity expense);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.clickListener = l;
    }
}
