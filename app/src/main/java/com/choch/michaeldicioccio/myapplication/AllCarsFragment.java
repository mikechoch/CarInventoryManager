package com.choch.michaeldicioccio.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class AllCarsFragment extends Fragment {

    /* Attributes */
    public static RecyclerView allCarsRecyclerView;
    public static CustomCarsRecyclerViewAdapter customAllCarsRecyclerViewAdapter;
    public static RelativeLayout noAllCarsRelativeLayout;

    public AllCarsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.all_cars_fragment_layout, container, false);

        noAllCarsRelativeLayout = (RelativeLayout) view.findViewById(R.id.no_all_cars_relative_layout);
        MainActivity.checkArrayIsZeroDisplayZeroIcon(MainActivity.allVinNumberBarCodes.size(), AllCarsFragment.noAllCarsRelativeLayout);

        allCarsRecyclerView = (RecyclerView) view.findViewById(R.id.all_cars_recycler_view);
        customAllCarsRecyclerViewAdapter = new CustomCarsRecyclerViewAdapter(getActivity(), MainActivity.allVinNumberBarCodes);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        allCarsRecyclerView.setLayoutManager(mLayoutManager);
        allCarsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        allCarsRecyclerView.setAdapter(customAllCarsRecyclerViewAdapter);

        return view;
    }


}
