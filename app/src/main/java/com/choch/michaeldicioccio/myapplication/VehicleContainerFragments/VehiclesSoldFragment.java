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


public class VehiclesSoldFragment extends Fragment {

    /* Attributes */
    private Realm realm;

    private Sorting sorting;
    private static ArrayList<Vehicle> vehiclesSoldArrayList;

    private RecyclerView vehiclesSoldRecyclerView;
    private RelativeLayout noVehiclesSoldRelativeLayout;

    private TextView sortButtonTextView, vehicleCountTextView;
    private RelativeLayout sortButtonRelativeLayout;

    public static Toolbar toolbar;
    public static DrawerLayout drawer;
    public static ActionBarDrawerToggle toggle;
    public static CustomVehiclesRecyclerViewAdapter customVehiclesSoldRecyclerViewAdapter;


    public VehiclesSoldFragment() {
    }

    //    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_move_to_cars:
                //alert for confirm to move
                if (customVehiclesSoldRecyclerViewAdapter.getSelectedItemCount() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    //set message
                    builder.setMessage("Are you sure you want to move the selected cars?");

                    //when click on MOVE
                    builder.setPositiveButton("MOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            customVehiclesSoldRecyclerViewAdapter.setActionMode(false);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int position : customVehiclesSoldRecyclerViewAdapter
                                            .getSelectedItems()) {
                                        Vehicle vehicle = realm.where(Vehicle.class)
                                                .equalTo("sold", true).findAll().get(position);
                                        vehicle.setSold(false);
                                        vehicle.setSellDate(null);
                                        vehicle.setSoldPriceBeenSetBefore(false);
                                        vehicle.getVehicleBuyer().setName("");
                                        vehicle.getVehicleBuyer().setPhoneNumber("");
                                        vehicle.getVehicleBuyer().setEmail("");
                                        customVehiclesSoldRecyclerViewAdapter.removeAt(position);
                                    }
                                }
                            });

                            vehicleCountTextView.setText("Vehicles (" + vehiclesSoldArrayList.size() + ")");

                            MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                    vehiclesSoldArrayList.size(),
                                    noVehiclesSoldRelativeLayout);

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
                    toast("Select cars to move");
                }
                return true;

            case R.id.action_delete:
                if (customVehiclesSoldRecyclerViewAdapter.getSelectedItemCount() > 0) {
                    //alert for confirm to delete
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    //set message
                    builder.setMessage("Are you sure you want to delete the selected cars?");

                    //when click on DELETE
                    builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            customVehiclesSoldRecyclerViewAdapter.setActionMode(false);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int position : customVehiclesSoldRecyclerViewAdapter
                                            .getSelectedItems()) {
                                        realm.where(Vehicle.class).equalTo("sold", true).findAll()
                                                .deleteFromRealm(position);
                                        customVehiclesSoldRecyclerViewAdapter.removeAt(position);
                                    }
                                }
                            });

                            vehicleCountTextView.setText("Vehicles (" + vehiclesSoldArrayList.size() + ")");

                            MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                    vehiclesSoldArrayList.size(),
                                    noVehiclesSoldRelativeLayout);

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

        View view = inflater.inflate(R.layout.vehicles_sold_fragment_layout, container, false);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                getActivity(),
                drawer,
                toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        Realm.init(getActivity());
        realm = Realm.getDefaultInstance();
        vehiclesSoldArrayList = new ArrayList<>(realm.copyFromRealm(realm.where(Vehicle.class)
                .equalTo("sold", true)
                .findAll()));

        sorting = realm.where(Sorting.class).findFirst();

        vehicleCountTextView = (TextView) view.findViewById(R.id.vehicle_count_text_view);
        vehicleCountTextView.setText("Vehicles (" + vehiclesSoldArrayList.size() + ")");
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

        noVehiclesSoldRelativeLayout = (RelativeLayout)
                view.findViewById(R.id.no_cars_sold_relative_layout);

        MainActivity.checkArrayIsZeroDisplayZeroIcon(
                vehiclesSoldArrayList.size(),
                noVehiclesSoldRelativeLayout);

        vehiclesSoldRecyclerView = (RecyclerView) view.findViewById(R.id.cars_sold_recycler_view);
        customVehiclesSoldRecyclerViewAdapter = new CustomVehiclesRecyclerViewAdapter(
                getActivity(),
                vehiclesSoldArrayList,
                new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if (!customVehiclesSoldRecyclerViewAdapter.isActionModeEnabled()) {
                            startVehicleDetailActivity(position);
                        } else {
                            checkedSelectedCount(false);
                        }
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        checkedSelectedCount(true);                    }
                });

        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());

        vehiclesSoldRecyclerView.setLayoutManager(mLayoutManager);
        vehiclesSoldRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
                    final String vin = customVehiclesSoldRecyclerViewAdapter.getVehicleAt(position).getVin();
                    final Vehicle[] vehicle = {new Vehicle(), new Vehicle()};
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            vehicle[0] = realm.where(Vehicle.class).equalTo("vin", vin).findFirst();
                            vehicle[1] = realm.copyFromRealm(vehicle[0]);
                            vehicle[0].setSold(false);
                            vehicle[0].setSellDate(null);
                            vehicle[0].setSoldPriceBeenSetBefore(false);
                            vehicle[0].getVehicleBuyer().setName("");
                            vehicle[0].getVehicleBuyer().setPhoneNumber("");
                            vehicle[0].getVehicleBuyer().setEmail("");

                            customVehiclesSoldRecyclerViewAdapter.removeAt(position);
                            checkedSelectedCount(false);
                        }
                    });

                    vehicleCountTextView.setText("Vehicles (" + vehiclesSoldArrayList.size() + ")");

                    MainActivity.checkArrayIsZeroDisplayZeroIcon(
                            vehiclesSoldArrayList.size(),
                            noVehiclesSoldRelativeLayout);

                    CoordinatorLayout mainCoordinateLayout =
                            (CoordinatorLayout)
                                    getActivity().findViewById(R.id.main_activity_container);
                    String vehicle_string = vehicle[0].getYear() + " "
                            + vehicle[0].getMake() + " "
                            + vehicle[0].getModel();
                    Snackbar.make(mainCoordinateLayout, vehicle_string + " moved to Inventory", Snackbar.LENGTH_SHORT)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (MainActivity.current_nav_item_selected == R.id.nav_sold_cars) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                vehicle[0].setSold(true);
                                                vehicle[0].setSellDate(vehicle[1].getSellDate());
                                                vehicle[0].setPriceSold(vehicle[1].getPriceSold());
                                                vehicle[0].getVehicleBuyer().setName(vehicle[1].getVehicleBuyer().getName());
                                                vehicle[0].getVehicleBuyer().setPhoneNumber(vehicle[1].getVehicleBuyer().getPhoneNumber());
                                                vehicle[0].getVehicleBuyer().setEmail(vehicle[1].getVehicleBuyer().getEmail());
                                                customVehiclesSoldRecyclerViewAdapter.addAt(position, vehicle[0]);
                                            }
                                        });

                                        vehicleCountTextView.setText("Vehicles (" + vehiclesSoldArrayList.size() + ")");

                                        MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                                vehiclesSoldArrayList.size(),
                                                noVehiclesSoldRelativeLayout);
                                    }
                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                            .show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(vehiclesSoldRecyclerView);

        vehiclesSoldRecyclerView.setAdapter(customVehiclesSoldRecyclerViewAdapter);
        sortRecyclerView();

        updateToolbarOnBackPressed(deactivateActionMode());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 201) {
            if (resultCode == Activity.RESULT_OK) {
                vehiclesSoldArrayList = new ArrayList<>(realm.copyFromRealm(realm.where(Vehicle.class)
                        .equalTo("sold", true)
                        .findAll()));
                customVehiclesSoldRecyclerViewAdapter.setDataSet(vehiclesSoldArrayList);
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
                customVehiclesSoldRecyclerViewAdapter.sortDataSetByMake();
                break;
            case 1:
                customVehiclesSoldRecyclerViewAdapter.sortDataSetByModel();
                break;
            case 2:
                customVehiclesSoldRecyclerViewAdapter.sortDataSetByDate();
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
        customVehiclesSoldRecyclerViewAdapter.setActionMode(false);
        customVehiclesSoldRecyclerViewAdapter.clearSelections();
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(null);
        toolbar.setTitle("Sold Vehicles");
        toolbar.inflateMenu(R.menu.main_menu);

        return toolbar;
    }

    private void activateActionMode() {
        customVehiclesSoldRecyclerViewAdapter.setActionMode(true);
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_left_white_24dp);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.sold_vehicles_action_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateToolbarOnBackPressed(deactivateActionMode());
            }
        });
    }

    private void checkedSelectedCount(boolean long_clicked) {
        if (customVehiclesSoldRecyclerViewAdapter.isActionModeEnabled()) {
            if (customVehiclesSoldRecyclerViewAdapter.getSelectedItemCount() == 0) {
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
        vehicleDetailIntent.putExtra("vin", vehiclesSoldArrayList.get(position).getVin());
        startActivityForResult(vehicleDetailIntent, 201);
    }

    private void toast(String toast_string) {
        Toast.makeText(getActivity(), Html.fromHtml("<b>" + toast_string + "</b>"), Toast.LENGTH_SHORT).show();
    }
}
