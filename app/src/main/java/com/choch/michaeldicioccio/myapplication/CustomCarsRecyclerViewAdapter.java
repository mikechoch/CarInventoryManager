package com.choch.michaeldicioccio.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import java.util.List;


public class CustomCarsRecyclerViewAdapter extends RecyclerView.Adapter<CustomCarsRecyclerViewAdapter.MyViewHolder> {

    Context context;
    List<String> carsList;
    
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


    public CustomCarsRecyclerViewAdapter(Context context, List<String> carsList) {
        this.context = context;
        this.carsList = carsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.car_item_relative_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final String car = carsList.get(position);
        holder.title.setText(car);
        holder.mainRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Main View Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        holder.mainRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity.myVib.vibrate(50);
                Toast.makeText(context, "Main View Long Clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return carsList.size();
    }

    public void add(String Vin) {
        notifyItemInserted(carsList.size());
        carsList.add(Vin);
    }

    public void addAt(int position, String Vin) {
        notifyItemInserted(position);
        carsList.add(position, Vin);
    }

    public void removeLast() {
        notifyItemRemoved(carsList.size()-1);
        carsList.remove(carsList.size()-1);
    }

    public void removeAt(int position) {
        notifyItemRemoved(position);
        carsList.remove(position);
    }
}

