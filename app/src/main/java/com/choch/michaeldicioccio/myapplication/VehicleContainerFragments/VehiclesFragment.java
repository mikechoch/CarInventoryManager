package com.choch.michaeldicioccio.myapplication.VehicleContainerFragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
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


public class VehiclesFragment extends Fragment {

    /* Attributes */
    private Realm realm;

    private Sorting sorting;
    private ArrayList<Vehicle> vehicleInventoryArrayList;

    private RecyclerView vehiclesRecyclerView;
    private RelativeLayout noVehiclesRelativeLayout;

    private ProgressBar progressBar;
    private FrameLayout frameLayout;

    private TextView sortButtonTextView, vehicleCountTextView;
    private RelativeLayout sortButtonRelativeLayout;

    public static Toolbar toolbar;
    public static DrawerLayout drawer;
    public static ActionBarDrawerToggle toggle;
    public static CustomVehiclesRecyclerViewAdapter customVehiclesRecyclerViewAdapter;

    /* Constructor */
    public VehiclesFragment() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_move_to_sold:
                //alert for confirm to move
                if (customVehiclesRecyclerViewAdapter.getSelectedItemCount() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    //set message
                    builder.setMessage("Are you sure you want to move the selected cars?");

                    //when click on MOVE
                    builder.setPositiveButton("MOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            customVehiclesRecyclerViewAdapter.setActionMode(false);
                            realm.executeTransaction(new Realm.Transaction() {
                                 @Override
                                 public void execute(Realm realm) {
                                     for (int position : customVehiclesRecyclerViewAdapter
                                             .getSelectedItems()) {
                                         realm.where(Vehicle.class).equalTo("sold", false).findAll()
                                                 .get(position).setSold(true);
                                         customVehiclesRecyclerViewAdapter.removeAt(position);
                                     }
                                 }
                             });

                            vehicleCountTextView.setText("Vehicles (" + vehicleInventoryArrayList.size() + ")");

                            MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                    vehicleInventoryArrayList.size(),
                                    noVehiclesRelativeLayout);

                            updateToolbar(deactivateActionMode());
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
                    toast("Select cars to move");
                }
                return true;

            case R.id.action_delete:
                if (customVehiclesRecyclerViewAdapter.getSelectedItemCount() > 0) {
                    //alert for confirm to delete
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    //set message
                    builder.setMessage("Are you sure you want to delete the selected cars?");

                    //when click on DELETE
                    builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            customVehiclesRecyclerViewAdapter.setActionMode(false);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int position : customVehiclesRecyclerViewAdapter
                                            .getSelectedItems()) {
                                        realm.where(Vehicle.class)
                                                .equalTo("sold", false)
                                                .findAll()
                                                .deleteFromRealm(position);
                                        customVehiclesRecyclerViewAdapter.removeAt(position);
                                    }
                                }
                            });

                            vehicleCountTextView.setText("Vehicles (" + vehicleInventoryArrayList.size() + ")");

                            MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                    vehicleInventoryArrayList.size(),
                                    noVehiclesRelativeLayout);

                            updateToolbar(deactivateActionMode());
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

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.vehicles_fragment_layout, container, false);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        Realm.init(getActivity());
        realm = Realm.getDefaultInstance();
        vehicleInventoryArrayList = new ArrayList<>(realm.copyFromRealm(realm.where(Vehicle.class)
                .equalTo("sold", false)
                .findAll()));

        sorting = realm.where(Sorting.class).findFirst();

        vehicleCountTextView = (TextView) view.findViewById(R.id.vehicle_count_text_view);
        vehicleCountTextView.setText("Vehicles (" + vehicleInventoryArrayList.size() + ")");
        sortButtonTextView = (TextView) view.findViewById(R.id.sort_text_view);
        sortButtonRelativeLayout = (RelativeLayout) view.findViewById(R.id.sort_button);

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

        noVehiclesRelativeLayout = (RelativeLayout) view.findViewById(R.id.no_cars_relative_layout);
        noVehiclesRelativeLayout.setVisibility(View.VISIBLE);
        MainActivity.checkArrayIsZeroDisplayZeroIcon(
                vehicleInventoryArrayList.size(),
                noVehiclesRelativeLayout);

        vehiclesRecyclerView = (RecyclerView) view.findViewById(R.id.cars_recycler_view);
        customVehiclesRecyclerViewAdapter = new CustomVehiclesRecyclerViewAdapter(
                getActivity(),
                vehicleInventoryArrayList,
                new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if (!customVehiclesRecyclerViewAdapter.isActionModeEnabled()) {
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

        vehiclesRecyclerView.setLayoutManager(mLayoutManager);
        vehiclesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(
                    RecyclerView recyclerView,
                    RecyclerView.ViewHolder viewHolder,
                    RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT | direction == ItemTouchHelper.RIGHT) {
                    final String vin = customVehiclesRecyclerViewAdapter.getVehicleAt(position).getVin();
                    final Vehicle[] vehicle = {new Vehicle()};
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            vehicle[0] = realm.where(Vehicle.class).equalTo("vin", vin).findFirst();
                            vehicle[0].setSold(true);

                            customVehiclesRecyclerViewAdapter.removeAt(position);
                            checkedSelectedCount(false);
                        }
                    });

                    vehicleCountTextView.setText("Vehicles (" + vehicleInventoryArrayList.size() + ")");

                    MainActivity.checkArrayIsZeroDisplayZeroIcon(
                            vehicleInventoryArrayList.size(),
                            noVehiclesRelativeLayout);

                    CoordinatorLayout mainCoordinateLayout =
                            (CoordinatorLayout)
                                    getActivity().findViewById(R.id.main_activity_container);
                    String vehicle_string = vehicle[0].getYear() + " "
                            + vehicle[0].getMake() + " "
                            + vehicle[0].getModel();
                    Snackbar.make(mainCoordinateLayout, vehicle_string + " moved to Sold", Snackbar.LENGTH_SHORT)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (MainActivity.current_nav_item_selected == R.id.nav_inventory) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                vehicle[0].setSold(false);

                                                customVehiclesRecyclerViewAdapter.addAt(
                                                        position,
                                                        vehicle[0]);
                                            }
                                        });

                                        vehicleCountTextView.setText("Vehicles (" + vehicleInventoryArrayList.size() + ")");

                                        MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                                vehicleInventoryArrayList.size(),
                                                noVehiclesRelativeLayout);
                                    }
                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                            .show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(vehiclesRecyclerView);

        vehiclesRecyclerView.setAdapter(customVehiclesRecyclerViewAdapter);
        sortRecyclerView();

        updateToolbar(deactivateActionMode());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200) {
            if (resultCode == Activity.RESULT_OK) {
                vehicleInventoryArrayList = new ArrayList<>(realm.copyFromRealm(realm.where(Vehicle.class)
                        .equalTo("sold", false)
                        .findAll()));
                customVehiclesRecyclerViewAdapter.setDataSet(vehicleInventoryArrayList);
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
                customVehiclesRecyclerViewAdapter.sortDataSetByMake();
                break;
            case 1:
                customVehiclesRecyclerViewAdapter.sortDataSetByModel();
                break;
            case 2:
                customVehiclesRecyclerViewAdapter.sortDataSetByDate();
                break;
        }
        sortButtonTextView.setText(sorting.getSortType());
    }

    private void updateToolbar(Toolbar toolbar) {
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
        customVehiclesRecyclerViewAdapter.setActionMode(false);
        customVehiclesRecyclerViewAdapter.clearSelections();
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(null);
        toolbar.setTitle("Vehicle Inventory");
        toolbar.inflateMenu(R.menu.main_menu);

        return toolbar;
    }

    public void activateActionMode() {
        customVehiclesRecyclerViewAdapter.setActionMode(true);
        toolbar.getMenu().clear();
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.vehicles_action_menu);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_left_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateToolbar(deactivateActionMode());
            }
        });
    }

    private void checkedSelectedCount(boolean long_clicked) {
        if (customVehiclesRecyclerViewAdapter.isActionModeEnabled()) {
            if (customVehiclesRecyclerViewAdapter.getSelectedItemCount() == 0) {
                updateToolbar(deactivateActionMode());
            }
        } else {
            if (long_clicked) {
                activateActionMode();
            }
        }
    }

    private void startVehicleDetailActivity(int position) {
        Intent vehicleDetailIntent = new Intent(getActivity().getApplicationContext(), VehicleDetailActivity.class);
        vehicleDetailIntent.putExtra("vin", vehicleInventoryArrayList.get(position).getVin());
        startActivityForResult(vehicleDetailIntent, 200);
    }

    private void toast(String toast_string) {
        Toast.makeText(getActivity(), Html.fromHtml("<b>" + toast_string + "</b>"), Toast.LENGTH_SHORT).show();
    }
}
