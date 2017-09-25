package com.choch.michaeldicioccio.myapplication.CarContainerFragments;

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
import android.widget.Toast;


import com.choch.michaeldicioccio.myapplication.Activities.VehicleDetailActivity;
import com.choch.michaeldicioccio.myapplication.Vehicle.CustomCarsRecyclerViewAdapter;
import com.choch.michaeldicioccio.myapplication.Activities.MainActivity;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.RecyclerViewClickListener;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import java.util.ArrayList;

import io.realm.Realm;


public class CarsFragment extends Fragment {

    /* Attributes */
    private Realm realm;

    private ArrayList<Vehicle> carInventoryArrayList;

    private RecyclerView carsRecyclerView;
    private RelativeLayout noCarsRelativeLayout;

    private ProgressBar progressBar;
    private FrameLayout frameLayout;

    public static Toolbar toolbar;
    public static DrawerLayout drawer;
    public static ActionBarDrawerToggle toggle;
    public static CustomCarsRecyclerViewAdapter customCarsRecyclerViewAdapter;

    /* Constructor */
    public CarsFragment() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_move_to_sold:
                //alert for confirm to move
                if (customCarsRecyclerViewAdapter.getSelectedItemCount() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    //set message
                    builder.setMessage("Are you sure you want to move the selected cars?");

                    //when click on MOVE
                    builder.setPositiveButton("MOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            customCarsRecyclerViewAdapter.setActionMode(false);
                            realm.executeTransaction(new Realm.Transaction() {
                                 @Override
                                 public void execute(Realm realm) {
                                     for (int position : customCarsRecyclerViewAdapter
                                             .getSelectedItems()) {
                                         realm.where(Vehicle.class).equalTo("sold", false).findAll()
                                                 .get(position).setSold(true);
                                         customCarsRecyclerViewAdapter.removeAt(position);
                                     }
                                 }
                             });

                            MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                    carInventoryArrayList.size(),
                                    noCarsRelativeLayout);

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
                if (customCarsRecyclerViewAdapter.getSelectedItemCount() > 0) {
                    //alert for confirm to delete
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    //set message
                    builder.setMessage("Are you sure you want to delete the selected cars?");

                    //when click on DELETE
                    builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            customCarsRecyclerViewAdapter.setActionMode(false);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int position : customCarsRecyclerViewAdapter
                                            .getSelectedItems()) {
                                        realm.where(Vehicle.class)
                                                .equalTo("sold", false)
                                                .findAll()
                                                .deleteFromRealm(position);
                                        customCarsRecyclerViewAdapter.removeAt(position);
                                    }
                                }
                            });

                            MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                    carInventoryArrayList.size(),
                                    noCarsRelativeLayout);

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

        View view = inflater.inflate(R.layout.cars_fragment_layout, container, false);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        Realm.init(getActivity());
        realm = Realm.getDefaultInstance();
        carInventoryArrayList = new ArrayList<>(realm.where(Vehicle.class).equalTo("sold", false)
                .findAll());

        noCarsRelativeLayout = (RelativeLayout) view.findViewById(R.id.no_cars_relative_layout);
        noCarsRelativeLayout.setVisibility(View.VISIBLE);
        MainActivity.checkArrayIsZeroDisplayZeroIcon(
                carInventoryArrayList.size(),
                noCarsRelativeLayout);

        carsRecyclerView = (RecyclerView) view.findViewById(R.id.cars_recycler_view);
        customCarsRecyclerViewAdapter = new CustomCarsRecyclerViewAdapter(
                getActivity(),
                carInventoryArrayList,
                new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if (!customCarsRecyclerViewAdapter.isActionModeEnabled()) {
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

        carsRecyclerView.setLayoutManager(mLayoutManager);
        carsRecyclerView.setItemAnimator(new DefaultItemAnimator());

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
                    final String vin = carInventoryArrayList.get(position).getVin();
                    //TODO: update data on swipe
                    final Vehicle[] vehicle = {new Vehicle()};
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            vehicle[0] = realm.where(Vehicle.class).equalTo("sold", false).findAll()
                                    .get(position);
                            realm.where(Vehicle.class).equalTo("sold", false).findAll().get(position)
                                    .setSold(true);

                            customCarsRecyclerViewAdapter.removeAt(position);
                            checkedSelectedCount(false);
                        }
                    });

                    MainActivity.checkArrayIsZeroDisplayZeroIcon(
                            carInventoryArrayList.size(),
                            noCarsRelativeLayout);

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
                                                realm.where(Vehicle.class)
                                                        .equalTo("sold", true)
                                                        .equalTo("vin", vehicle[0].getVin())
                                                        .findFirst()
                                                        .setSold(false);

                                                customCarsRecyclerViewAdapter.addAt(
                                                        position,
                                                        vehicle[0]);
                                            }
                                        });

                                        MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                                carInventoryArrayList.size(),
                                                noCarsRelativeLayout);
                                    }
                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                            .show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(carsRecyclerView); //set swipe to recylcerview

        carsRecyclerView.setAdapter(customCarsRecyclerViewAdapter);
        updateToolbarOnBackPressed(deactivateActionMode());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
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
        customCarsRecyclerViewAdapter.setActionMode(false);
        customCarsRecyclerViewAdapter.clearSelections();
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(null);
        toolbar.setTitle("Car Inventory");
        toolbar.inflateMenu(R.menu.main_menu);

        return toolbar;
    }

    public void activateActionMode() {
        customCarsRecyclerViewAdapter.setActionMode(true);
        toolbar.getMenu().clear();
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.cars_action_menu);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_left_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateToolbarOnBackPressed(deactivateActionMode());
            }
        });
    }

    private void checkedSelectedCount(boolean long_clicked) {
        if (customCarsRecyclerViewAdapter.isActionModeEnabled()) {
            if (customCarsRecyclerViewAdapter.getSelectedItemCount() == 0) {
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
        vehicleDetailIntent.putExtra("vin", carInventoryArrayList.get(position).getVin());
        startActivity(vehicleDetailIntent);
    }

    private void toast(String toast_string) {
        Toast.makeText(getActivity(), Html.fromHtml("<b>" + toast_string + "</b>"), Toast.LENGTH_SHORT).show();
    }
}
