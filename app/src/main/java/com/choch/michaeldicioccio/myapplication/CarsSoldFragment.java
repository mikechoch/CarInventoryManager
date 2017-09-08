package com.choch.michaeldicioccio.myapplication;

import android.app.Fragment;
import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import io.realm.Realm;


public class CarsSoldFragment extends Fragment {

    /* Attributes */
    private Realm realm;

    private static ArrayList<Vehicle> carsSoldArrayList;

    private RecyclerView carsSoldRecyclerView;
    private RelativeLayout noCarsSoldRelativeLayout;

    public static Toolbar toolbar;
    public static DrawerLayout drawer;
    public static ActionBarDrawerToggle toggle;
    public static CustomCarsRecyclerViewAdapter customCarsSoldRecyclerViewAdapter;

    public CarsSoldFragment() {
    }

    //    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_move_to_cars:
                //alert for confirm to move
                if (customCarsSoldRecyclerViewAdapter.getSelectedItemCount() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    //set message
                    builder.setMessage("Are you sure you want to move the selected cars?");

                    //when click on MOVE
                    builder.setPositiveButton("MOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            customCarsSoldRecyclerViewAdapter.setActionMode(false);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int position : customCarsSoldRecyclerViewAdapter
                                            .getSelectedItems()) {
                                        realm.where(Vehicle.class).equalTo("sold", true).findAll()
                                                .get(position).setSold(false);
                                        customCarsSoldRecyclerViewAdapter.removeAt(position);
                                    }
                                }
                            });

                            MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                    carsSoldArrayList.size(),
                                    noCarsSoldRelativeLayout);

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
                if (customCarsSoldRecyclerViewAdapter.getSelectedItemCount() > 0) {
                    //alert for confirm to delete
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    //set message
                    builder.setMessage("Are you sure you want to delete the selected cars?");

                    //when click on DELETE
                    builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            customCarsSoldRecyclerViewAdapter.setActionMode(false);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int position : customCarsSoldRecyclerViewAdapter
                                            .getSelectedItems()) {
                                        realm.where(Vehicle.class).equalTo("sold", true).findAll()
                                                .deleteFromRealm(position);
                                        customCarsSoldRecyclerViewAdapter.removeAt(position);
                                    }
                                }
                            });

                            MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                    carsSoldArrayList.size(),
                                    noCarsSoldRelativeLayout);

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

        View view = inflater.inflate(R.layout.cars_sold_fragment_layout, container, false);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                getActivity(),
                drawer,
                toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        Realm.init(getActivity());
        realm = Realm.getDefaultInstance();
        carsSoldArrayList = new ArrayList<>(realm.where(Vehicle.class).equalTo("sold", true)
                .findAll());

        noCarsSoldRelativeLayout = (RelativeLayout)
                view.findViewById(R.id.no_cars_sold_relative_layout);

        MainActivity.checkArrayIsZeroDisplayZeroIcon(
                carsSoldArrayList.size(),
                noCarsSoldRelativeLayout);

        carsSoldRecyclerView = (RecyclerView) view.findViewById(R.id.cars_sold_recycler_view);
        customCarsSoldRecyclerViewAdapter = new CustomCarsRecyclerViewAdapter(
                getActivity(),
                carsSoldArrayList,
                new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        checkedSelectedCount(false);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        checkedSelectedCount(true);                    }
                });

        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());

        carsSoldRecyclerView.setLayoutManager(mLayoutManager);
        carsSoldRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
                    final String vin = carsSoldArrayList.get(position).getVIN();
                    //TODO: update data on swipe
                    final Vehicle[] vehicle = {new Vehicle()};
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            vehicle[0] = realm.where(Vehicle.class).equalTo("sold", true).findAll()
                                    .get(position);
                            realm.where(Vehicle.class).equalTo("sold", true).findAll().get(position)
                                    .setSold(false);
                            customCarsSoldRecyclerViewAdapter.removeAt(position);
                            checkedSelectedCount(false);
                        }
                    });


                    CoordinatorLayout mainCoordinateLayout =
                            (CoordinatorLayout)
                                    getActivity().findViewById(R.id.main_activity_container);
                    Snackbar.make(mainCoordinateLayout, vin + " moved to Sold", Snackbar.LENGTH_SHORT)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //TODO: undo the swipe
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.where(Vehicle.class).equalTo("sold", false)
                                                    .equalTo("VIN", vehicle[0].getVIN())
                                                    .findFirst()
                                                    .setSold(true);
                                            customCarsSoldRecyclerViewAdapter.addAt(position, vehicle[0]);
                                        }
                                    });
                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                            .show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(carsSoldRecyclerView); //set swipe to recylcerview

        carsSoldRecyclerView.setAdapter(customCarsSoldRecyclerViewAdapter);
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
        customCarsSoldRecyclerViewAdapter.setActionMode(false);
        customCarsSoldRecyclerViewAdapter.clearSelections();
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(null);
        toolbar.setTitle("Sold Cars");
        toolbar.inflateMenu(R.menu.main_menu);

        return toolbar;
    }

    private void activateActionMode() {
        customCarsSoldRecyclerViewAdapter.setActionMode(true);
        customCarsSoldRecyclerViewAdapter.notifyDataSetChanged();
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_left_white_24dp);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.sold_cars_action_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateToolbarOnBackPressed(deactivateActionMode());
            }
        });
    }

    private void checkedSelectedCount(boolean long_clicked) {
        if (customCarsSoldRecyclerViewAdapter.isActionModeEnabled()) {
            if (customCarsSoldRecyclerViewAdapter.getSelectedItemCount() == 0) {
                updateToolbarOnBackPressed(deactivateActionMode());
                updateToolbarOnBackPressed(deactivateActionMode());
            }
        } else {
            if (long_clicked) {
                activateActionMode();
            }
        }
    }

    private void toast(String toast_string) {
        Toast.makeText(getActivity(), toast_string, Toast.LENGTH_SHORT).show();
    }
}
