package com.choch.michaeldicioccio.myapplication.VehicleContainerFragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.choch.michaeldicioccio.myapplication.Activities.VehicleDetailActivity;
import com.choch.michaeldicioccio.myapplication.Sort.Sorting;
import com.choch.michaeldicioccio.myapplication.Vehicle.CustomVehiclesRecyclerViewAdapter;
import com.choch.michaeldicioccio.myapplication.Activities.MainActivity;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.RecyclerViewClickListener;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import java.util.ArrayList;

import io.realm.Realm;


public class AllVehiclesFragment extends Fragment {

    /* Attributes */
    private Realm realm;

    private Sorting sorting;
    private ArrayList<Vehicle> allVehiclesArrayList;

    private RecyclerView allVehiclesRecyclerView;
    private RelativeLayout noAllVehiclesRelativeLayout;

    private TextView sortButtonTextView, vehicleCountTextView;
    private RelativeLayout sortButtonRelativeLayout;

    public static Toolbar toolbar;
    public static DrawerLayout drawer;
    public static ActionBarDrawerToggle toggle;
    public static CustomVehiclesRecyclerViewAdapter customAllVehiclesRecyclerViewAdapter;

    public AllVehiclesFragment() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_delete:
                if (customAllVehiclesRecyclerViewAdapter.getSelectedItemCount() > 0) {
                    //alert for confirm to delete
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    //set message
                    builder.setMessage("Are you sure you want to delete the selected cars?");

                    //when click on DELETE
                    builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            customAllVehiclesRecyclerViewAdapter.setActionMode(false);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int position : customAllVehiclesRecyclerViewAdapter
                                            .getSelectedItems()) {
                                        realm.where(Vehicle.class).findAll().deleteFromRealm(
                                                position);
                                        customAllVehiclesRecyclerViewAdapter.removeAt(position);
                                    }
                                }
                            });

                            vehicleCountTextView.setText("Vehicles (" + allVehiclesArrayList.size() + ")");

                            MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                    allVehiclesArrayList.size(),
                                    noAllVehiclesRelativeLayout);

                            updateToolbarOnBackPressed(deactivateActionMode());
                            dialogInterface.dismiss();
                        }
                        //not removing items if cancel is done
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.cancel();
                        }
                    }).setCancelable(false);

                    final AlertDialog dialog = builder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
                                    getResources().getColor(R.color.colorPrimary));

                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(
                                    Color.BLACK);
                        }
                    });
                    dialog.show();
                } else {
                    toast("Select cars to delete");
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.all_vehicles_fragment_layout, container, false);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                getActivity(),
                drawer,
                toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        Realm.init(getActivity());
        realm = Realm.getDefaultInstance();
        allVehiclesArrayList = new ArrayList<>(realm.copyFromRealm(realm.where(Vehicle.class)
                .findAll()));

        sorting = realm.where(Sorting.class).findFirst();

        vehicleCountTextView = (TextView) view.findViewById(R.id.vehicle_count_text_view);
        vehicleCountTextView.setText("Vehicles (" + allVehiclesArrayList.size() + ")");
        sortButtonTextView = (TextView) view.findViewById(R.id.sort_text_view);
        sortButtonRelativeLayout = (RelativeLayout) view.findViewById(R.id.sort_button);

        sortButtonTextView.setText(sorting.getSortType());
        sortButtonRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog sortAlertDialog = sorting.createSortAlertDialog(getActivity());
                sortAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        sortRecyclerView();
                    }
                });
                sortAlertDialog.show();
            }
        });

        noAllVehiclesRelativeLayout = (RelativeLayout)
                view.findViewById(R.id.no_all_cars_relative_layout);

        MainActivity.checkArrayIsZeroDisplayZeroIcon(
                allVehiclesArrayList.size(),
                noAllVehiclesRelativeLayout);

        allVehiclesRecyclerView = (RecyclerView) view.findViewById(R.id.all_cars_recycler_view);
        customAllVehiclesRecyclerViewAdapter = new CustomVehiclesRecyclerViewAdapter(
                getActivity(),
                allVehiclesArrayList,
                new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if (!customAllVehiclesRecyclerViewAdapter.isActionModeEnabled()) {
                            startVehicleDetailActivity(position);
                        } else {
                            checkedSelectedCount(false);
                        }
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        checkedSelectedCount(true);
                    }
                });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        allVehiclesRecyclerView.setLayoutManager(mLayoutManager);
        allVehiclesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        allVehiclesRecyclerView.setAdapter(customAllVehiclesRecyclerViewAdapter);
        sortRecyclerView();

        updateToolbarOnBackPressed(deactivateActionMode());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 202) {
            if (resultCode == Activity.RESULT_OK) {
                allVehiclesArrayList = new ArrayList<>(realm.copyFromRealm(realm.where(Vehicle.class)
                        .findAll()));
                customAllVehiclesRecyclerViewAdapter.setDataSet(allVehiclesArrayList);
                sortRecyclerView();
            }

            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    private void sortRecyclerView() {
        switch(sorting.getSortIndex()) {
            case 0:
                customAllVehiclesRecyclerViewAdapter.sortDataSetByMake();
                break;
            case 1:
                customAllVehiclesRecyclerViewAdapter.sortDataSetByModel();
                break;
            case 2:
                customAllVehiclesRecyclerViewAdapter.sortDataSetByDate();
                break;
        }
        sortButtonTextView.setText(sorting.getSortType());
    }

    private void updateToolbarOnBackPressed(Toolbar toolbar) {
        drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                getActivity(),
                drawer,
                toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    public static Toolbar deactivateActionMode() {
        customAllVehiclesRecyclerViewAdapter.setActionMode(false);
        customAllVehiclesRecyclerViewAdapter.clearSelections();
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(null);
        toolbar.setTitle("All Vehicles");
        toolbar.inflateMenu(R.menu.main_menu);

        return toolbar;
    }

    private void activateActionMode() {
        customAllVehiclesRecyclerViewAdapter.setActionMode(true);
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_left_white_24dp);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.all_vehicles_action_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateToolbarOnBackPressed(deactivateActionMode());
            }
        });
    }

    private void checkedSelectedCount(boolean long_clicked) {
        if (customAllVehiclesRecyclerViewAdapter.isActionModeEnabled()) {
            if (customAllVehiclesRecyclerViewAdapter.getSelectedItemCount() == 0) {
                updateToolbarOnBackPressed(deactivateActionMode());
            }
        } else {
            if (long_clicked) {
                activateActionMode();
            }
        }
    }

    private void startVehicleDetailActivity(int position) {
        Intent vehicleDetailIntent = new Intent(getActivity().getApplicationContext(), VehicleDetailActivity.class);
        vehicleDetailIntent.putExtra("vin", allVehiclesArrayList.get(position).getVin());
        startActivityForResult(vehicleDetailIntent, 202);
    }

    private void toast(String toast_string) {
        Toast.makeText(getActivity(), Html.fromHtml("<b>" + toast_string + "</b>"), Toast.LENGTH_SHORT).show();
    }
}
