package com.choch.michaeldicioccio.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;


public class ScannerActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    /* Attributes */
    private ZBarScannerView mScannerView;

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        return true;
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_flash:
                if (mScannerView.getFlash()) {
                    AnimatedVectorDrawable drawable = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.flash_off_anim);
                    item.setIcon(drawable);
                    drawable.start();
                    mScannerView.setFlash(false);
                } else {
                    AnimatedVectorDrawable drawable = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.flash_on_anim);
                    item.setIcon(drawable);
                    drawable.start();
                    mScannerView.setFlash(true);
                }
                break;
            case android.R.id.home:
                mScannerView.stopCamera();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param state
     */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.scanner_activity_layout); // Set the scanner view as the content view

        mScannerView = (ZBarScannerView) findViewById(R.id.scanner_view);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.scanner_relative_layout);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    /**
     *
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mScannerView.stopCamera();
        finish();
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        Handler handler = new Handler();
        handler.postDelayed(task, 450);
    }

    /**
     *
     */
    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    /**
     *
     * @param rawResult
     */
    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        MainActivity.myVib.vibrate(50);

        CarsFragment.customCarsRecyclerViewAdapter.add(rawResult.getContents());
        AllCarsFragment.customAllCarsRecyclerViewAdapter.add(rawResult.getContents());

        MainActivity.checkArrayIsZeroDisplayZeroIcon(MainActivity.carInventoryVinNumberBarCodes.size(), CarsFragment.noCarsRelativeLayout);
        MainActivity.checkArrayIsZeroDisplayZeroIcon(MainActivity.allVinNumberBarCodes.size(), AllCarsFragment.noAllCarsRelativeLayout);

        finish();
    }

    private Runnable task = new Runnable() {
        public void run() {
            mScannerView.startCamera();
        }
    };
}
