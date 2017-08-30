package com.choch.michaeldicioccio.myapplication;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class CarsSoldFragment extends Fragment {

    /* Attributes */
    public static RecyclerView carsSoldRecyclerView;
    public static CustomCarsRecyclerViewAdapter customCarsSoldRecyclerViewAdapter;
    public static RelativeLayout noCarsSoldRelativeLayout;

    public CarsSoldFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cars_sold_fragment_layout, container, false);

        noCarsSoldRelativeLayout = (RelativeLayout) view.findViewById(R.id.no_cars_sold_relative_layout);
        MainActivity.checkArrayIsZeroDisplayZeroIcon(MainActivity.soldCarInventoryVinNumberBarCodes.size(), CarsSoldFragment.noCarsSoldRelativeLayout);

        carsSoldRecyclerView = (RecyclerView) view.findViewById(R.id.cars_sold_recycler_view);
        customCarsSoldRecyclerViewAdapter = new CustomCarsRecyclerViewAdapter(getActivity(), MainActivity.soldCarInventoryVinNumberBarCodes);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        carsSoldRecyclerView.setLayoutManager(mLayoutManager);
        carsSoldRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition(); //get position which is swipe
                if (direction == ItemTouchHelper.LEFT | direction == ItemTouchHelper.RIGHT) {    //if swipe left or right
                    final String Vin = MainActivity.soldCarInventoryVinNumberBarCodes.get(position);

                    CarsSoldFragment.customCarsSoldRecyclerViewAdapter.removeAt(position);
                    CarsFragment.customCarsRecyclerViewAdapter.add(Vin);

                    MainActivity.checkArrayIsZeroDisplayZeroIcon(MainActivity.carInventoryVinNumberBarCodes.size(), CarsFragment.noCarsRelativeLayout);
                    MainActivity.checkArrayIsZeroDisplayZeroIcon(MainActivity.soldCarInventoryVinNumberBarCodes.size(), CarsSoldFragment.noCarsSoldRelativeLayout);

                    CoordinatorLayout mainCoordinateLayout = (CoordinatorLayout) getActivity().findViewById(R.id.main_coordinate_layout);
                    Snackbar.make(mainCoordinateLayout, Vin + " moved to Cars", Snackbar.LENGTH_SHORT)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CarsFragment.customCarsRecyclerViewAdapter.removeLast();
                                    CarsSoldFragment.customCarsSoldRecyclerViewAdapter.addAt(position, Vin);

                                    MainActivity.checkArrayIsZeroDisplayZeroIcon(MainActivity.carInventoryVinNumberBarCodes.size(), CarsFragment.noCarsRelativeLayout);
                                    MainActivity.checkArrayIsZeroDisplayZeroIcon(MainActivity.soldCarInventoryVinNumberBarCodes.size(), CarsSoldFragment.noCarsSoldRelativeLayout);
                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                            .show();

//                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); //alert for confirm to delete
//                    builder.setMessage("Are you sure to delete?");    //set message
//
//                    builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() { //when click on DELETE
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int which) {
//                            customCarsSoldRecyclerViewAdapter.notifyItemRemoved(position);    //item removed from recylcerview
//                            MainActivity.carInventoryVinNumberBarCodes.add(MainActivity.soldCarInventoryVinNumberBarCodes.get(position));
//                            MainActivity.soldCarInventoryVinNumberBarCodes.removeLast(position);  //then removeLast item
//                            MainActivity.customFragmentPagerAdapter.notifyDataSetChanged();
//                            dialogInterface.dismiss();
//                        }
//                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {  //not removing items if cancel is done
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int which) {
//                            customCarsSoldRecyclerViewAdapter.notifyItemRemoved(position + 1);    //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
//                            customCarsSoldRecyclerViewAdapter.notifyItemRangeChanged(position, customCarsSoldRecyclerViewAdapter.getItemCount());   //notifies the RecyclerView Adapter that positions of element in adapter has been changed from position(removed element index to end of list), please update it.
//                            dialogInterface.cancel();
//                        }
//                    }).setCancelable(false);
//
//                    final AlertDialog dialog = builder.create();
//                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                        @Override
//                        public void onShow(DialogInterface dialogInterface) {
//                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
//                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
//                        }
//                    });
//                    dialog.show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(carsSoldRecyclerView); //set swipe to recylcerview

        carsSoldRecyclerView.setAdapter(customCarsSoldRecyclerViewAdapter);

        return view;
    }
}
