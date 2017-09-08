package com.choch.michaeldicioccio.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int current_nav_item_selected;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton typeVinFloatingActionButton, scanVinFloatingActionButton;
    private NavigationView navigationView;

    public static Vibrator vibrator;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupFloatingActionButtonMenu();

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        getFragmentManager().beginTransaction().replace(
                R.id.main_activity_content,
                new CarsFragment()).commit();

        updateToolbarOnBackPressed(toolbar);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_inventory);
        current_nav_item_selected = R.id.nav_inventory;
    }

    @Override
    public void onResume(){
        super.onResume();
        updateToolbarOnBackPressed(CarsFragment.deactivateActionMode());
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else if (floatingActionMenu.isOpened()) {
            floatingActionMenu.close(true);

        } else if (getSupportActionBar().getTitle().equals("")) {
            switch (current_nav_item_selected) {
                case R.id.nav_inventory:
                    updateToolbarOnBackPressed(CarsFragment.deactivateActionMode());
                    break;
                case R.id.nav_sold_cars:
                    updateToolbarOnBackPressed(CarsSoldFragment.deactivateActionMode());
                    break;
                case R.id.nav_all_cars:
                    updateToolbarOnBackPressed(AllCarsFragment.deactivateActionMode());
                    break;
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 200) && (resultCode == ScannerActivity.RESULT_OK)) {
            if (floatingActionMenu.isOpened()) {
                floatingActionMenu.close(false);
            }
            navigationView.setCheckedItem(R.id.nav_inventory);
            getFragmentManager().beginTransaction().replace(R.id.main_activity_content, new CarsFragment()).commit();
        }
    }

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
                            Intent scannerIntent = new Intent(this, ScannerActivity.class);
                            startActivity(scannerIntent);
                        }
                    }
                }
                break;
        }
    }

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
                createTypeVinAlertDialog().show();
                break;
            case R.id.nav_scan_car:
                startScannerActivity();
                break;
            case R.id.nav_inventory:
                current_nav_item_selected = id;
                getFragmentManager().beginTransaction().replace(R.id.main_activity_content, new CarsFragment()).commit();
                break;
            case R.id.nav_sold_cars:
                current_nav_item_selected = id;
                getFragmentManager().beginTransaction().replace(R.id.main_activity_content, new CarsSoldFragment()).commit();
                break;
            case R.id.nav_all_cars:
                current_nav_item_selected = id;
                getFragmentManager().beginTransaction().replace(R.id.main_activity_content, new AllCarsFragment()).commit();
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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

    private View.OnClickListener onButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == typeVinFloatingActionButton) {
                    createTypeVinAlertDialog().show();
                } else {
                    startScannerActivity();
                }
                floatingActionMenu.close(true);
            }
        };
    }

    private AlertDialog createTypeVinAlertDialog() {
        final AlertDialog.Builder typeVinAlertDialogBuilder = new AlertDialog.Builder(this);

        RelativeLayout typeVinRelativeLayout = createTypeVinView();
        final EditText[] editText = {(EditText) typeVinRelativeLayout.findViewById(R.id.type_vin_edit_text)};
        final boolean[] ok_clicked = {false};

        typeVinAlertDialogBuilder.setView(typeVinRelativeLayout);
        typeVinAlertDialogBuilder.setPositiveButton("ADD VIN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ok_clicked[0] = true;
                dialog.dismiss();
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (ok_clicked[0]) {
                    ok_clicked[0] = false;
                    if (editText[0].getText().length() != 17) {
                        toast("VIN # must be 17 characters");
                        RelativeLayout typeVinRelativeLayout = createTypeVinView();
                        editText[0] = (EditText) typeVinRelativeLayout.findViewById(R.id.type_vin_edit_text);
                        setupAlertDialogActionButtonColors(typeVinAlertDialogBuilder.setView(typeVinRelativeLayout).create()).show();
                    } else {

                    }
                }
            }
        });

        AlertDialog typeVinAlertDialog = typeVinAlertDialogBuilder.create();
        return setupAlertDialogActionButtonColors(typeVinAlertDialog);
    }

    private RelativeLayout createTypeVinView() {
        RelativeLayout typeVinRelativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.type_vin_alert_dialog_layout, null);
        final EditText editText = (EditText) typeVinRelativeLayout.findViewById(R.id.type_vin_edit_text);
        editText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }
            @Override
            public void afterTextChanged(Editable et) {
                String s = et.toString();
                if(!s.equals(s.toUpperCase())) {
                    s = s.toUpperCase();
                    editText.setText(s);
                    editText.setSelection(editText.getText().length());
                }

                if (editText.getText().length() == 17) {
                    editText.setTextColor(Color.BLACK);
                    editText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_17));
                } else {
                    editText.setTextColor(getResources().getColor(R.color.colorIconNotActivated));
                    editText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
                }
            }
        });

        return typeVinRelativeLayout;
    }

    private AlertDialog setupAlertDialogActionButtonColors(final AlertDialog alertDialog) {
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
                        getResources().getColor(R.color.colorPrimary));
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(
                        Color.BLACK);
            }
        });
        return alertDialog;
    }

    private void startScannerActivity() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        } else {
            Intent scannerIntent = new Intent(this, ScannerActivity.class);
            startActivityForResult(scannerIntent, 200);
        }
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

    private void toast(String toast_string) {
        Toast.makeText(MainActivity.this, toast_string, Toast.LENGTH_SHORT).show();
    }
}
