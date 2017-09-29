package com.choch.michaeldicioccio.myapplication.Vehicle;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.choch.michaeldicioccio.myapplication.Default;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.RecyclerViewClickListener;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class CustomExpensesRecyclerViewAdapter extends RecyclerView.Adapter<CustomExpensesRecyclerViewAdapter.MyViewHolder> {

    /* Attributes */
    private ArrayList<Expense> expenseArrayList;
    private RecyclerViewClickListener listener;

    private DecimalFormat df = new DecimalFormat(Default.DOUBLE_FORMAT.getObject().toString());

    /**
     * MyViewHolder class
     * defines all UI elements to be modified for showing expense info
     */
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

    /* Constructor */
    public CustomExpensesRecyclerViewAdapter(ArrayList<Expense> expenseArrayList, RecyclerViewClickListener listener) {
        this.expenseArrayList = expenseArrayList;
        this.listener = listener;
    }

    /**
     * defines the layout for each item
     * @return - the MyViewHolder for each item
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item_relative_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    /**
     * handles what is going to be shown in each MyViewHolder container
     */
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Expense expense = expenseArrayList.get(position);

        holder.itemTitleTextView.setText(expense.getTitle());
        holder.itemPriceTextView.setText(("$" + df.format(expense.getPrice())));

        // apply click events
        applyClickEvents(holder, position);
    }

    /**
     * handles click events on Expenses
     * @param holder - MyViewHolder representing container of the item view clicked
     * @param position - position of the item clicked
     */
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

    /**
     * returns item count in data set
     */
    @Override
    public int getItemCount() {
        return expenseArrayList.size();
    }

    /**
     * add an object from the data set at the position provided
     */
    public void addAt(int position, Expense expense) {
        expenseArrayList.add(position, expense);
        notifyItemInserted(position);
    }

    /**
     * remove an object from the data set at the position provided
     */
    public void removeAt(int position) {
        expenseArrayList.remove(position);
        notifyDataSetChanged();
    }
}

