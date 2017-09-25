package com.choch.michaeldicioccio.myapplication.CarContainerFragments;

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
import android.widget.Toast;

import com.choch.michaeldicioccio.myapplication.Activities.VehicleDetailActivity;
import com.choch.michaeldicioccio.myapplication.Vehicle.CustomCarsRecyclerViewAdapter;
import com.choch.michaeldicioccio.myapplication.Activities.MainActivity;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.RecyclerViewClickListener;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import java.util.ArrayList;

import io.realm.Realm;


public class AllCarsFragment extends Fragment {

    /* Attributes */
    private Realm realm;

    private ArrayList<Vehicle> allCarsArrayList;

    private RecyclerView allCarsRecyclerView;
    private RelativeLayout noAllCarsRelativeLayout;

    public static Toolbar toolbar;
    public static DrawerLayout drawer;
    public static ActionBarDrawerToggle toggle;
    public static CustomCarsRecyclerViewAdapter customAllCarsRecyclerViewAdapter;

    public AllCarsFragment() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_delete:
                if (customAllCarsRecyclerViewAdapter.getSelectedItemCount() > 0) {
                    //alert for confirm to delete
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    //set message
                    builder.setMessage("Are you sure you want to delete the selected cars?");

                    //when click on DELETE
                    builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            customAllCarsRecyclerViewAdapter.setActionMode(false);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (int position : customAllCarsRecyclerViewAdapter
                                            .getSelectedItems()) {
                                        realm.where(Vehicle.class).findAll().deleteFromRealm(
                                                position);
                                        customAllCarsRecyclerViewAdapter.removeAt(position);
                                    }
                                }
                            });

                            MainActivity.checkArrayIsZeroDisplayZeroIcon(
                                    allCarsArrayList.size(),
                                    noAllCarsRelativeLayout);

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

        View view = inflater.inflate(R.layout.all_cars_fragment_layout, container, false);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                getActivity(),
                drawer,
                toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        Realm.init(getActivity());
        realm = Realm.getDefaultInstance();
        allCarsArrayList = new ArrayList<>(realm.where(Vehicle.class).findAll());

        noAllCarsRelativeLayout = (RelativeLayout)
                view.findViewById(R.id.no_all_cars_relative_layout);

        MainActivity.checkArrayIsZeroDisplayZeroIcon(
                allCarsArrayList.size(),
                noAllCarsRelativeLayout);

        allCarsRecyclerView = (RecyclerView) view.findViewById(R.id.all_cars_recycler_view);
        customAllCarsRecyclerViewAdapter = new CustomCarsRecyclerViewAdapter(
                getActivity(),
                allCarsArrayList,
                new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if (!customAllCarsRecyclerViewAdapter.isActionModeEnabled()) {
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

        allCarsRecyclerView.setLayoutManager(mLayoutManager);
        allCarsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        allCarsRecyclerView.setAdapter(customAllCarsRecyclerViewAdapter);
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
        customAllCarsRecyclerViewAdapter.setActionMode(false);
        customAllCarsRecyclerViewAdapter.clearSelections();
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(null);
        toolbar.setTitle("All Cars");
        toolbar.inflateMenu(R.menu.main_menu);

        return toolbar;
    }

    private void activateActionMode() {
        customAllCarsRecyclerViewAdapter.setActionMode(true);
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_left_white_24dp);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.all_cars_action_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateToolbarOnBackPressed(deactivateActionMode());
            }
        });
    }

    private void checkedSelectedCount(boolean long_clicked) {
        if (customAllCarsRecyclerViewAdapter.isActionModeEnabled()) {
            if (customAllCarsRecyclerViewAdapter.getSelectedItemCount() == 0) {
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
        vehicleDetailIntent.putExtra("vin", allCarsArrayList.get(position).getVin());
        startActivity(vehicleDetailIntent);
    }

    private void toast(String toast_string) {
        Toast.makeText(getActivity(), Html.fromHtml("<b>" + toast_string + "</b>"), Toast.LENGTH_SHORT).show();
    }
}
