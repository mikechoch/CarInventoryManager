package com.choch.michaeldicioccio.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class SplashScreenActivity extends AppCompatActivity {

    /**
     * method called to perform basic application startup logic that should happen only once
     * for the entire life of the activity
     * @param savedInstanceState - access for cached variables
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: add any code you want to do while the splash screen is showing before the MainActivity is shown

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
