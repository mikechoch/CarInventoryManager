package com.choch.michaeldicioccio.myapplication.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.choch.michaeldicioccio.myapplication.Sort.Sort;
import com.choch.michaeldicioccio.myapplication.Sort.Sorting;
import com.choch.michaeldicioccio.myapplication.VehicleContainerFragments.AllVehiclesFragment;
import com.choch.michaeldicioccio.myapplication.VehicleContainerFragments.VehiclesFragment;
import com.choch.michaeldicioccio.myapplication.VehicleContainerFragments.VehiclesSoldFragment;
import com.choch.michaeldicioccio.myapplication.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import io.realm.Realm;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /* Globals */
    private Realm realm;

    public static int current_nav_item_selected;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private ConstraintLayout constraintLayout;

    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton typeVinFloatingActionButton, scanVinFloatingActionButton;
    private NavigationView navigationView;

    public static Vibrator vibrator;
    public static String vehicleSortType;
    public static String[] vehicleSortTypes;

    /**
     * inflates the menu for the current activity
     * @param menu - current menu inflated in the actionbar/toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * creates the actions for each item in the actionbar/toolbar menu
     * @param item - current item selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startSettingsActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * method called to perform basic application startup logic that should happen only once
     * for the entire life of the activity
     * @param savedInstanceState - access for cached variables
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Realm.init(this);
        realm = Realm.getDefaultInstance();
        if (realm.where(Sorting.class).findFirst() == null) {
            final Sorting sorting = new Sorting();
            vehicleSortType = sorting.getSortType();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insert(sorting);
                }
            });
        } else {
            vehicleSortType = realm.copyFromRealm(realm.where(Sorting.class).findFirst()).getSortType();
        }

        int count = 0;
        vehicleSortTypes = new String[Sort.values().length];
        for (Sort sort : Sort.values()) {
            vehicleSortTypes[count++] = sort.getSortType();
        }

        setupFloatingActionButtonMenu();

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        getFragmentManager().beginTransaction().replace(
                R.id.main_activity_content,
                new VehiclesFragment()).commit();

        updateToolbarOnBackPressed(toolbar);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        current_nav_item_selected = R.id.nav_inventory;
        navigationView.setCheckedItem(current_nav_item_selected);

    }

    /**
     *  when the activity enters the Resumed state, it comes to the foreground, and then the
     *  system invokes the onResume() callback
     *  Used to select and show the correct Fragment by checking which NavDrawer item is selected
     */
    @Override
    public void onResume(){
        super.onResume();
        int checked_item = 0;
        Menu menu = navigationView.getMenu().getItem(1).getSubMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isChecked()) {
                checked_item = i;
            }
        }

        switch (checked_item) {
            case 0:
                updateToolbarOnBackPressed(VehiclesFragment.deactivateActionMode());
                break;
            case 1:
                updateToolbarOnBackPressed(VehiclesSoldFragment.deactivateActionMode());
                break;
            case 2:
                updateToolbarOnBackPressed(AllVehiclesFragment.deactivateActionMode());
                break;
        }

    }

    /**
     * logic when the bottom nav bar back button is pressed
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else if (floatingActionMenu.isOpened()) {
            floatingActionMenu.close(true);

        } else if (getSupportActionBar().getTitle().equals("")) {
            updateCurrentToolBarView(current_nav_item_selected);

        } else {
            super.onBackPressed();
        }
    }

    /**
     * handles permission request results, such as, Camera permission Alert Dialog appears and
     * allow is clicked
     * @param requestCode - request code for the permission
     * @param permissions - array of string permissions
     * @param grantResults - array of grant results linked to the same location of the permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            Intent scannerIntent = new Intent(this, ScanVinActivity.class);
                            startActivity(scannerIntent);
                        }
                    }
                }
                break;
        }
    }

    /**
     * handles logic for when a specific item in the nav drawer is selected
     * @param item - represents which item was selected
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (floatingActionMenu.isOpened()) {
            floatingActionMenu.close(false);
        }

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_type_car:
                updateCurrentToolBarView(current_nav_item_selected);
                startTypeVinActivity();
                break;
            case R.id.nav_scan_car:
                updateCurrentToolBarView(current_nav_item_selected);
                startScanVinActivity();
                break;
            case R.id.nav_inventory:
                current_nav_item_selected = id;
                new FragmentLoaderTask().execute(0);
//                getFragmentManager().beginTransaction().replace(R.id.main_activity_content, new VehiclesFragment()).commit();
                break;
            case R.id.nav_sold_cars:
                current_nav_item_selected = id;
                new FragmentLoaderTask().execute(1);
//                getFragmentManager().beginTransaction().replace(R.id.main_activity_content, new VehiclesSoldFragment()).commit();
                break;
            case R.id.nav_all_cars:
                current_nav_item_selected = id;
                new FragmentLoaderTask().execute(2);
//                getFragmentManager().beginTransaction().replace(R.id.main_activity_content, new AllVehiclesFragment()).commit();
                break;
            case R.id.nav_settings:
                updateCurrentToolBarView(current_nav_item_selected);
                startSettingsActivity();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * update the fragment toolbar with the current showing fragment
     * @param current - current frag selected
     */
    private void updateCurrentToolBarView(int current) {
        switch (current) {
            case R.id.nav_inventory:
                updateToolbarOnBackPressed(VehiclesFragment.deactivateActionMode());
                break;
            case R.id.nav_sold_cars:
                updateToolbarOnBackPressed(VehiclesSoldFragment.deactivateActionMode());
                break;
            case R.id.nav_all_cars:
                updateToolbarOnBackPressed(AllVehiclesFragment.deactivateActionMode());
                break;
        }
    }

    /**
     * update the navigation drawer when coming back
     * @param toolbar - current toolbar
     */
    private void updateToolbarOnBackPressed(Toolbar toolbar) {
        drawer = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * sets up the fab menu and the fab menu items view, on click
     */
    private void setupFloatingActionButtonMenu() {
        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        typeVinFloatingActionButton = (FloatingActionButton) findViewById(R.id.type_vin_fab);
        scanVinFloatingActionButton = (FloatingActionButton) findViewById(R.id.scan_vin_fab);

        floatingActionMenu.setMenuButtonColorNormalResId(R.color.colorPrimary);
        floatingActionMenu.setMenuButtonColorPressedResId(R.color.colorPrimary);
        floatingActionMenu.getMenuIconView().setImageResource(R.drawable.plus);

        typeVinFloatingActionButton.setColorNormalResId(R.color.colorPrimary);
        typeVinFloatingActionButton.setColorPressedResId(R.color.colorPrimary);
        typeVinFloatingActionButton.setColorFilter(getResources().getColor(R.color.colorAccent));

        scanVinFloatingActionButton.setColorNormalResId(R.color.colorPrimary);
        scanVinFloatingActionButton.setColorPressedResId(R.color.colorPrimary);

        //handling menu status (open or close)
        floatingActionMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {
                    floatingActionMenu.setClickable(true);
                } else {
                    floatingActionMenu.setClickable(false);
                }
            }
        });

        //handling each floating action button clicked
        typeVinFloatingActionButton.setOnClickListener(onButtonClick());
        scanVinFloatingActionButton.setOnClickListener(onButtonClick());

        floatingActionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (floatingActionMenu.isOpened()) {
                    floatingActionMenu.close(true);
                }
            }
        });

        floatingActionMenu.setClickable(false);
    }

    /**
     * checks item in the fab menu is clicked
     * performs the action on fab menu item clicked
     */
    private View.OnClickListener onButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCurrentToolBarView(current_nav_item_selected);
                // mini fab click event handler
                int id  = view.getId();
                switch(id) {
                    case R.id.type_vin_fab:
                        startTypeVinActivity();
                        break;
                    case R.id.scan_vin_fab:
                        startScanVinActivity();
                        break;
                }
                floatingActionMenu.close(true);
            }
        };
    }

    /**
     * start the TypeVinActivity
     */
    private void startTypeVinActivity() {
        Intent typeVinIntent = new Intent(this, TypeVinActivity.class);
        startActivity(typeVinIntent);
    }

    /**
     * start the ScanVinActivity
     */
    private void startScanVinActivity() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        } else {
            Intent scanVinIntent = new Intent(this, ScanVinActivity.class);
            startActivity(scanVinIntent);
        }
    }

    /**
     * start the SettingsActivity
     */
    private void startSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * if the current RecyclerView's size greater than 0 show the empty RecyclerView layout
     * otherwise hide it
     * @param n - size of RecyclerView
     * @param relativeLayout - empty RecyclerView layout
     */
    public static void checkArrayIsZeroDisplayZeroIcon(int n, RelativeLayout relativeLayout) {
        if (n > 0) {
            relativeLayout.setVisibility(View.INVISIBLE);
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ASyncTask for loading fragments
     * Hides UI elements, shows progress spinner and then displays fragment UI
     */
    private class FragmentLoaderTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            frameLayout = (FrameLayout) findViewById(R.id.progress_bar_layout);
            constraintLayout = (ConstraintLayout) findViewById(R.id.main_activity_content);
            progressBar.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.VISIBLE);
            frameLayout.setClickable(true);
            constraintLayout.setVisibility(View.GONE);
        }


        @Override
        protected Void doInBackground(Integer... params) {
            switch (params[0]) {
                case 0:
                    getFragmentManager().beginTransaction().replace(R.id.main_activity_content, new VehiclesFragment()).commit();
                    break;
                case 1:
                    getFragmentManager().beginTransaction().replace(R.id.main_activity_content, new VehiclesSoldFragment()).commit();
                    break;
                case 2:
                    getFragmentManager().beginTransaction().replace(R.id.main_activity_content, new AllVehiclesFragment()).commit();
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            constraintLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            frameLayout.setVisibility(View.GONE);
            frameLayout.setClickable(false);
        }
    }

    /**
     * shortcut for toasting message to user
     * @param toast_string - String to toast
     */
    private void toast(String toast_string) {
        Toast.makeText(this, toast_string, Toast.LENGTH_LONG).show();
    }
}
