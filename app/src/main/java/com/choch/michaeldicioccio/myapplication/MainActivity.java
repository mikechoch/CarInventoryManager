package com.choch.michaeldicioccio.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    /* Attributes */
    public static ArrayList<String> carInventoryVinNumberBarCodes;
    public static ArrayList<String> soldCarInventoryVinNumberBarCodes;
    public static ArrayList<String> allVinNumberBarCodes;

    private MenuItem prevMenuItem;
    private FloatingActionButton fab;

    public static CustomFragmentPagerAdapter customFragmentPagerAdapter;
    public static ViewPager viewPager;
    public static BottomNavigationView bottomNavigationView;

    public static String string;

    public static Vibrator myVib;

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        carInventoryVinNumberBarCodes = new ArrayList<>();
        soldCarInventoryVinNumberBarCodes = new ArrayList<>();
        allVinNumberBarCodes = new ArrayList<>();

        fab = (FloatingActionButton) findViewById(R.id.add_car_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, 100);
                } else {
                    Intent scannerIntent = new Intent(MainActivity.this, ScannerActivity.class);
                    startActivity(scannerIntent);
                }
            }
        });

        customFragmentPagerAdapter = new CustomFragmentPagerAdapter(this, getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(customFragmentPagerAdapter);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.cars_bottom_bar_button:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.sold_cars_bottom_bar_button:
                                viewPager.setCurrentItem(1);
                                break;
                            case R.id.all_cars_bottom_bar_button:
                                viewPager.setCurrentItem(2);
                                break;
                        }
                        return false;
                    }
                });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                else
                {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }

                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

                switch (position) {
                    case 0:
                        setFragmentUI("Car Inventory", View.VISIBLE);
                        break;
                    case 1:
                        setFragmentUI("Sold Cars", View.INVISIBLE);
                        break;
                    case 2:
                        setFragmentUI("All Cars", View.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setFragmentUI("Car Inventory", View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.CAMERA)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Intent scannerIntent = new Intent(MainActivity.this, ScannerActivity.class);
                        startActivity(scannerIntent);
                    }
                }
            }
        }
    }

    /**
     *
     * @param action_bar_title
     * @param visibility
     */
    private void setFragmentUI(String action_bar_title, int visibility) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(action_bar_title);
        }
        fab.setVisibility(visibility);
    }

    /**
     *
     * @param n
     * @param relativeLayout
     */
    public static void checkArrayIsZeroDisplayZeroIcon(int n, RelativeLayout relativeLayout) {
        if (n > 0) {
            relativeLayout.setVisibility(View.INVISIBLE);
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

}
