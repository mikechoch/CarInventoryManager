package com.choch.michaeldicioccio.myapplication.Vehicle;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.choch.michaeldicioccio.myapplication.Default;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.RecyclerViewClickListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CustomVehiclesRecyclerViewAdapter extends RecyclerView.Adapter<CustomVehiclesRecyclerViewAdapter.MyViewHolder> {

    /* Globals */
    private Context context;
    private ArrayList<Vehicle> vehicleArrayList;
    private RecyclerViewClickListener listener;
    private SparseBooleanArray selectedItems;
    private boolean actionModeEnabled = false;

    private DecimalFormat df = new DecimalFormat(Default.DOUBLE_FORMAT.getObject().toString());
    private DateFormat dateFormat = new SimpleDateFormat(Default.DATE_FORMAT.getObject().toString());

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView itemTitleTextView;
        public TextView itemVinTetxView;
        public TextView itemOwnedStatusTextView;
        public RelativeLayout itemOwnedStatusRelativeLayout;
        public TextView itemDateTextView;
        public TextView itemPriceTextView;
        public TextView itemProfitTextView;
        public RelativeLayout itemRelativeLayout;

        public MyViewHolder(View view) {
            super(view);
            this.view = view;
            itemTitleTextView = (TextView) view.findViewById(R.id.year_make_model_text_view);
            itemVinTetxView = (TextView) view.findViewById(R.id.vin_edit_text);
            itemOwnedStatusTextView = (TextView) view.findViewById(R.id.owned_status_title_view);
            itemOwnedStatusRelativeLayout = (RelativeLayout) view.findViewById(R.id.owned_status_relative_layout);
            itemDateTextView = (TextView) view.findViewById(R.id.date_text_view);
            itemPriceTextView = (TextView) view.findViewById(R.id.price_text_view);
            itemProfitTextView  =(TextView) view.findViewById(R.id.price_profit_text_view);
            itemRelativeLayout = (RelativeLayout) view.findViewById(R.id.car_item_relative_layout);
        }
    }

    public CustomVehiclesRecyclerViewAdapter(Context context, ArrayList<Vehicle> vehicleArrayList, RecyclerViewClickListener listener) {
        this.context = context;
        this.vehicleArrayList = vehicleArrayList;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vehicle_item_relative_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Vehicle vehicle = vehicleArrayList.get(position);
        holder.itemTitleTextView.setText(vehicle.getYear() + " "
                + vehicle.getMake() + " "
                + vehicle.getModel());
        holder.itemVinTetxView.setText(vehicle.getVin());
        if (vehicle.isSold()) {
            holder.itemOwnedStatusTextView.setText("Sold");
            holder.itemOwnedStatusRelativeLayout.setBackgroundColor(context.getResources().getColor(R.color.colorIconNotActivated));

            if (vehicle.getSellDate() != null) {
                holder.itemDateTextView.setVisibility(View.VISIBLE);
                holder.itemDateTextView.setText(dateFormat.format(vehicle.getSellDate()));
            } else {
                holder.itemDateTextView.setVisibility(View.GONE);
            }

            if (vehicle.hasSoldPriceBeenSetBefore()) {
                String[] price_sold = df.format(vehicle.getPriceSold()).split("\\.");
                holder.itemPriceTextView.setText(Html.fromHtml("$" + price_sold[0] + "<small><small>" + "." + price_sold[1] + "</small></small>"));
                holder.itemPriceTextView.setTextColor(context.getResources().getColor(R.color.colorIconNotActivated));
                holder.itemPriceTextView.setTextSize(18);

                Double profit = vehicle.getPriceSold() - vehicle.getPricePaid();

                for (int i  = 0; i < vehicle.getExpenseCount(); i++) {
                    profit -= vehicle.getExpenseAt(i).getPrice();
                }

                if (vehicle.hasPaidPriceBeenSetBefore() && vehicle.hasSoldPriceBeenSetBefore()) {
                    holder.itemProfitTextView.setVisibility(View.VISIBLE);

                    String[] price_profit = df.format(Math.abs(profit)).split("\\.");
                    holder.itemProfitTextView.setText(Html.fromHtml("$" + price_profit[0] + "<small><small>" + "." + price_profit[1] + "</small></small>"));
                    if (profit < 0) {
                        holder.itemProfitTextView.setTextColor(context.getResources().getColor(R.color.colorInvalidVin));
                    } else {
                        holder.itemProfitTextView.setTextColor(context.getResources().getColor(R.color.colorValidVin));
                    }
                } else {
                    holder.itemProfitTextView.setVisibility(View.GONE);
                }
            } else {
                holder.itemPriceTextView.setText("Add Price Sold");
                holder.itemPriceTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                holder.itemPriceTextView.setTextSize(15);

                holder.itemProfitTextView.setText("");
            }

        } else {
            holder.itemOwnedStatusTextView.setText("Owned");
            holder.itemProfitTextView.setVisibility(View.GONE);
            holder.itemOwnedStatusRelativeLayout.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));

            if (vehicle.getBuyDate() != null) {
                holder.itemDateTextView.setVisibility(View.VISIBLE);
                holder.itemDateTextView.setText(dateFormat.format(vehicle.getBuyDate()));
            } else {
                holder.itemDateTextView.setVisibility(View.GONE);
            }

            if (vehicle.hasPaidPriceBeenSetBefore()) {
                String[] price_paid = df.format(vehicle.getPricePaid()).split("\\.");
                holder.itemPriceTextView.setText(Html.fromHtml("$" + price_paid[0] + "<small><small>" + "." + price_paid[1] + "</small></small>"));
                holder.itemPriceTextView.setTextColor(context.getResources().getColor(R.color.colorIconNotActivated));
                holder.itemPriceTextView.setTextSize(18);
            } else {
                holder.itemPriceTextView.setText("Add Price Paid");
                holder.itemPriceTextView.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                holder.itemPriceTextView.setTextSize(15);
            }

        }

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
        return vehicleArrayList.size();
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
        vehicleArrayList.add(position, vehicle);
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
        vehicleArrayList.remove(position);
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

    public Vehicle getVehicleAt(int position) {
        return vehicleArrayList.get(position);
    }

    public void setDataSet(ArrayList<Vehicle> carArrayList) {
        this.vehicleArrayList = carArrayList;
    }

    public void sortDataSetByMake() {
        System.out.println(vehicleArrayList);
        Collections.sort(vehicleArrayList, new Comparator<Vehicle>() {
            public int compare(Vehicle s1, Vehicle s2) {
                return s1.getMake().compareTo(s2.getMake());
            }
        });

        notifyItemRangeChanged(0, getItemCount());
    }

    public void sortDataSetByDate() {
        ArrayList<Vehicle> noDateSetVehicles = new ArrayList<>();
        for (int i = vehicleArrayList.size()-1; i > -1; i--) {
            Vehicle temp_vehicle = vehicleArrayList.get(i);
            if (temp_vehicle.isSold() && temp_vehicle.getSellDate() == null) {
                vehicleArrayList.remove(temp_vehicle);
                noDateSetVehicles.add(temp_vehicle);
            } else if (!temp_vehicle.isSold() && temp_vehicle.getBuyDate() == null) {
                vehicleArrayList.remove(temp_vehicle);
                noDateSetVehicles.add(temp_vehicle);
            }
        }

        Collections.sort(vehicleArrayList, new Comparator<Vehicle>() {
            public int compare(Vehicle s1, Vehicle s2) {
                if (s1.isSold() && s2.isSold()) {
                    return s1.getSellDate().compareTo(s2.getSellDate());
                } else if (s1.isSold()) {
                    return s1.getSellDate().compareTo(s2.getBuyDate());
                } else if (s2.isSold()) {
                    return s1.getBuyDate().compareTo(s2.getSellDate());
                }
                return s1.getBuyDate().compareTo(s2.getBuyDate());
            }
        });

        vehicleArrayList.addAll(noDateSetVehicles);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void sortDataSetByModel() {
        System.out.println(vehicleArrayList);
        Collections.sort(vehicleArrayList, new Comparator<Vehicle>() {
            public int compare(Vehicle s1, Vehicle s2) {
                return s1.getModel().compareTo(s2.getModel());
            }
        });

        notifyItemRangeChanged(0, getItemCount());
    }
}

