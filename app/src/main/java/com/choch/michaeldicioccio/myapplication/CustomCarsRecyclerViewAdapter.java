package com.choch.michaeldicioccio.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CustomCarsRecyclerViewAdapter extends RecyclerView.Adapter<CustomCarsRecyclerViewAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Vehicle> carArrayList;
    private RecyclerViewClickListener listener;
    private SparseBooleanArray selectedItems;
    private boolean actionModeEnabled = false;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView title;
        public RelativeLayout mainRelativeLayout;

        public MyViewHolder(View view) {
            super(view);
            this.view = view;
            title = (TextView) view.findViewById(R.id.car_item_title_text_view);
            mainRelativeLayout = (RelativeLayout) view.findViewById(R.id.car_item_relative_layout);
        }
    }

    public CustomCarsRecyclerViewAdapter(Context context, ArrayList<Vehicle> carArrayList, RecyclerViewClickListener listener) {
        this.context = context;
        this.carArrayList = carArrayList;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.car_item_relative_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Vehicle vehicle = carArrayList.get(position);
        holder.title.setText(vehicle.getVIN());

        if (actionModeEnabled) {
            holder.itemView.setSelected(selectedItems.get(position, false));
            if (holder.itemView.isSelected()) {
                holder.itemView.setBackgroundColor(Color.parseColor("#FFB300"));
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        } else {
            holder.itemView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.car_item_selector));
        }

        // apply click events
        applyClickEvents(holder, position);
    }

    private void applyClickEvents(final MyViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionModeEnabled) {
                    toggleSelection(position);
                }
                listener.onClick(view, position);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                toggleSelection(position);
                listener.onLongClick(view, position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return carArrayList.size();
    }

    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
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

    public void addAt(int position, Vehicle vehicle) {
        carArrayList.add(position, vehicle);
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
        carArrayList.remove(position);
        notifyItemRemoved(position);

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

