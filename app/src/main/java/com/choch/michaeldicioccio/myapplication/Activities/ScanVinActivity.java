package com.choch.michaeldicioccio.myapplication.Activities;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.choch.michaeldicioccio.myapplication.Json.VinErrorCheck;
import com.choch.michaeldicioccio.myapplication.MessageStrings;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import io.realm.Realm;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;


public class ScanVinActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    /* Globals */
    private Realm realm;
    private ZBarScannerView zBarScannerView;

    /**
     * inflates the menu for the current activity
     * @param menu - current menu inflated in the actionbar/toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan_menu, menu);
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
            case R.id.action_flash:
                if (zBarScannerView.getFlash()) {
                    AnimatedVectorDrawable drawable = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.flash_off_anim);
                    item.setIcon(drawable);
                    drawable.start();
                    zBarScannerView.setFlash(false);
                } else {
                    AnimatedVectorDrawable drawable = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.flash_on_anim);
                    item.setIcon(drawable);
                    drawable.start();
                    zBarScannerView.setFlash(true);
                }
                break;
            case android.R.id.home:
                zBarScannerView.stopCamera();
                finish();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner_activity_layout); // Set the scanner view as the content view

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        zBarScannerView = (ZBarScannerView) findViewById(R.id.scanner_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    /**
     *  when the activity enters the Resumed state, it comes to the foreground, and then the
     *  system invokes the onResume() callback
     *  Turn ZBarScanner on
     */
    @Override
    public void onResume() {
        super.onResume();
        zBarScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        Handler handler = new Handler();
        handler.postDelayed(task, 450);
    }

    /**
     *  when the activity enters the Paused state, it hides the foreground, and then the
     *  system invokes the onPause() callback
     *  Turn the ZBarScanner off
     */
    @Override
    public void onPause() {
        super.onPause();
        zBarScannerView.stopCamera();
    }

    /**
     * logic when the bottom nav bar back button is pressed
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        zBarScannerView.stopCamera();
        finish();
    }

    /**
     * handles the event of a result from a successfully scanned barcode
     * @param rawResult - scanned result from the ZBarSacnner
     */
    @Override
    public void handleResult(Result rawResult) {
        MainActivity.vibrator.vibrate(50);
        VinErrorCheck vinErrorCheck = new VinErrorCheck(rawResult.getContents());

        if (vinErrorCheck.verifyValidVin(rawResult.getBarcodeFormat().getName())) {
            String vin = vinErrorCheck.getVin();

            if (realm.where(Vehicle.class).equalTo("Vin", vin).findFirst() == null) {
                startTypeVinActivityFromScanVinActivity(vin);

            } else {
                toastResumeCamera(MessageStrings.VIN_ALREADY_SCANNED.getMessage());
            }
        } else {
            toastResumeCamera(vinErrorCheck.getErrorMessage());
        }
    }

    /**
     * start the TypeVinActivity with the VIN scanned value as a string extra
     */
    private void startTypeVinActivityFromScanVinActivity(String vin) {
        Intent typeVinIntent = new Intent(this, TypeVinActivity.class);
        typeVinIntent.putExtra("ScannedVin", vin);
        startActivity(typeVinIntent);
    }

    /**
     * easier way to toast an error when the scanner tries scanning a barcode
     * @param string - string to toast
     */
    private void toastResumeCamera(String string) {
        Toast toast = Toast.makeText(this, Html.fromHtml("<b>" +  string + " <font color=\"red\"><big>&#x2717;</big></font></b>"), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 200);
        toast.show();
        zBarScannerView.resumeCameraPreview(this);
    }

    /**
     * task to handle turning on the camera with control
     */
    private Runnable task = new Runnable() {
        public void run() {
            zBarScannerView.startCamera();
        }
    };

}
