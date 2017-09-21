package com.choch.michaeldicioccio.myapplication.Vehicle;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.choch.michaeldicioccio.myapplication.Defaults;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.RecyclerViewClickListener;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class CustomExpensesRecyclerViewAdapter extends RecyclerView.Adapter<CustomExpensesRecyclerViewAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Expense> expenseArrayList;
    private RecyclerViewClickListener listener;
    private SparseBooleanArray selectedItems;
    private boolean actionModeEnabled = false;

    private DecimalFormat df = new DecimalFormat(Defaults.DOUBLE_FORMAT.getObject().toString());

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView itemTitleTextView;
        public TextView itemPriceTextView;
        public RelativeLayout itemRelativeLayout;

        public MyViewHolder(View view) {
            super(view);
            this.view = view;
            itemTitleTextView = (TextView) view.findViewById(R.id.expense_title);
            itemPriceTextView = (TextView) view.findViewById(R.id.expense_price);
            itemRelativeLayout = (RelativeLayout) view.findViewById(R.id.expense_relative_layout);

        }
    }

    public CustomExpensesRecyclerViewAdapter(Context context, ArrayList<Expense> expenseArrayList, RecyclerViewClickListener listener) {
        this.context = context;
        this.expenseArrayList = expenseArrayList;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item_relative_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Expense expense = expenseArrayList.get(position);

        holder.itemTitleTextView.setText(expense.getTitle());
        holder.itemPriceTextView.setText(df.format(expense.getPrice()));

        // apply click events
        applyClickEvents(holder, position);
    }

    private void applyClickEvents(final MyViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(view, position);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseArrayList.size();
    }

    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public ArrayList<Expense> getAdapterArray() {
        return expenseArrayList;
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(getSelectedItemCount());
        for (int i = getSelectedItemCount() - 1; i > -1; i--) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void setActionMode(boolean actionMode) {
        this.actionModeEnabled = actionMode;
    }

    public boolean isActionModeEnabled() {
        return this.actionModeEnabled;
    }

    public void addAt(int position, Expense expense) {
        expenseArrayList.add(position, expense);
        notifyItemInserted(position);

        for (int i = getSelectedItemCount() - 1; i > -1; i--) {
            int key = selectedItems.keyAt(i);
            if (key >= position) {
                toggleSelection(key);
                toggleSelection(key + 1);
            }
        }
    }

    public void removeAt(int position) {
        expenseArrayList.remove(position);
        notifyDataSetChanged();

        if (selectedItems.get(position, false)) {
            toggleSelection(position);
        }

        for (int i = 0; i < getSelectedItemCount(); i++) {
            int key = selectedItems.keyAt(i);
            if (key > position) {
                toggleSelection(key);
                toggleSelection(key - 1);
            }
        }
    }
}

