package com.choch.michaeldicioccio.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by michaeldicioccio on 9/7/17.
 */

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO Pull all JSON data and store into Realm database, then start MapActivity

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
