package com.choch.michaeldicioccio.myapplication.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.choch.michaeldicioccio.myapplication.Defaults;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import java.text.DecimalFormat;

import io.realm.Realm;

/**
 * Created by michaeldicioccio on 9/14/17.
 */

public class VehicleDetailActivity extends AppCompatActivity {

    private Realm realm;
    private boolean editModeEnabled = false;
    private String toolbarTitle;

    private Vehicle vehicle;

    private ProgressBar progressBar;
    private FrameLayout frameLayout;

    private Toolbar toolbar;
    
    private TextView pricePaidLabelTextView;
    private TextView priceSoldLabelTextView;

    private EditText vinEditText;
    private EditText yearEditText;
    private EditText makeEditText;
    private EditText modelEditText;
    private EditText trimEditText;
    private EditText styleEditText;
    private EditText engineEditText;

    private EditText pricePaidEditText;
    private EditText priceSoldEditText;

    private EditText descriptionEditText;

    private CardView saveVehicleButton;
    private RelativeLayout saveVehicleRelativeLayout;
    private CardView savReturnVehicleButton;
    private RelativeLayout saveReturnVehicleRelativeLayout;

    private DecimalFormat df = new DecimalFormat(Defaults.DOUBLE_FORMAT.getObject().toString());
    private boolean return_main = false;

    /**
     * inflates the menu for the current activity
     * @param menu - current menu inflated in the actionbar/toolbar
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vehicle_detail_menu, menu);
        return true;
    }

    /**
     * creates the actions for each item in the actionbar/toolbar menu
     * @param item - current item selected
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            activateEditMode();
            return true;
        } else if (id == R.id.action_save) {
            String[] strings = {
                    vinEditText.getText().toString(),
                    yearEditText.getText().toString(),
                    makeEditText.getText().toString(),
                    modelEditText.getText().toString(),
                    trimEditText.getText().toString(),
                    styleEditText.getText().toString(),
                    engineEditText.getText().toString(),
                    descriptionEditText.getText().toString(),
                    pricePaidEditText.getText().toString(),
                    priceSoldEditText.getText().toString()};
            new EditInfoTask().execute(strings);
            return true;
        } else if (id == android.R.id.home) {
            if (editModeEnabled) {
                deactivateEditMode();
            } else {
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_detail_activity);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getStringExtra("Vin") != null) {
            vehicle = realm.where(Vehicle.class).equalTo("Vin", getIntent().getStringExtra("Vin")).findFirst();
            toolbarTitle = vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel();
            getSupportActionBar().setTitle(toolbarTitle);
        }

        pricePaidLabelTextView = (TextView) findViewById(R.id.price_paid_title_text_view);
        priceSoldLabelTextView = (TextView) findViewById(R.id.price_sold_title_text_view);

        pricePaidLabelTextView.setText(Html.fromHtml("<b>Price paid:</b> <big>$</big>"));
        priceSoldLabelTextView.setText(Html.fromHtml("<b>Price sold:</b> <big>$</big>"));

        vinEditText = (EditText) findViewById(R.id.vin_edit_text);
        yearEditText = (EditText) findViewById(R.id.year_edit_text);
        makeEditText = (EditText) findViewById(R.id.make_edit_text);
        modelEditText = (EditText) findViewById(R.id.model_edit_text);
        trimEditText = (EditText) findViewById(R.id.trim_edit_text);
        styleEditText = (EditText) findViewById(R.id.style_edit_text);
        engineEditText = (EditText) findViewById(R.id.engine_edit_text);
        pricePaidEditText = (EditText) findViewById(R.id.price_paid_edit_text);
        priceSoldEditText = (EditText) findViewById(R.id.price_sold_edit_text);
        descriptionEditText = (EditText) findViewById(R.id.description_edit_text);

        vinEditText.setEnabled(false);
        yearEditText.setEnabled(false);
        makeEditText.setEnabled(false);
        modelEditText.setEnabled(false);
        trimEditText.setEnabled(false);
        styleEditText.setEnabled(false);
        engineEditText.setEnabled(false);
        descriptionEditText.setEnabled(false);
        pricePaidEditText.setEnabled(false);
        priceSoldEditText.setEnabled(false);

        vinEditText.setText(vehicle.getVin());
        yearEditText.setText(vehicle.getYear());
        makeEditText.setText(vehicle.getMake());
        modelEditText.setText(vehicle.getModel());
        trimEditText.setText(vehicle.getTrim());
        styleEditText.setText(vehicle.getStyle());
        engineEditText.setText(vehicle.getEngine());
        descriptionEditText.setText(vehicle.getDescription());
        pricePaidEditText.setText(df.format(vehicle.getPricePaid()));
        if (vehicle.hasSoldPriceBeenSetBefore()) {
            priceSoldEditText.setText(df.format(vehicle.getPriceSold()));
        } else {
            priceSoldEditText.setText("");
        }

        saveVehicleButton = (CardView) findViewById(R.id.save_vehicle_button_container);
        saveVehicleRelativeLayout = (RelativeLayout) findViewById(R.id.save_vehicle_button);
        saveVehicleRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editModeEnabled) {
                    String[] strings = {
                            vinEditText.getText().toString(),
                            yearEditText.getText().toString(),
                            makeEditText.getText().toString(),
                            modelEditText.getText().toString(),
                            trimEditText.getText().toString(),
                            styleEditText.getText().toString(),
                            engineEditText.getText().toString(),
                            descriptionEditText.getText().toString(),
                            pricePaidEditText.getText().toString(),
                            priceSoldEditText.getText().toString()};
                    new EditInfoTask().execute(strings);
                }
            }
        });

        savReturnVehicleButton = (CardView) findViewById(R.id.save_return_vehicle_button_container);
        saveReturnVehicleRelativeLayout = (RelativeLayout) findViewById(R.id.save_return_vehicle_button);
        saveReturnVehicleRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editModeEnabled) {
                    String[] strings = {
                            vinEditText.getText().toString(),
                            yearEditText.getText().toString(),
                            makeEditText.getText().toString(),
                            modelEditText.getText().toString(),
                            trimEditText.getText().toString(),
                            styleEditText.getText().toString(),
                            engineEditText.getText().toString(),
                            descriptionEditText.getText().toString(),
                            pricePaidEditText.getText().toString(),
                            priceSoldEditText.getText().toString()};
                    return_main = true;
                    new EditInfoTask().execute(strings);
                }
            }
        });


    }

    /**
     *
     */
    @Override
    public void onBackPressed() {
        if (editModeEnabled) {
            deactivateEditMode();
        } else {
            super.onBackPressed();
        }
    }

    public void deactivateEditMode() {
        editModeEnabled = false;
        toggleEditTextEditable();
        saveVehicleButton.setVisibility(View.GONE);
        savReturnVehicleButton.setVisibility(View.GONE);
        toolbar.getMenu().clear();
        vinEditText.setText(vehicle.getVin());
        yearEditText.setText(vehicle.getYear());
        makeEditText.setText(vehicle.getMake());
        modelEditText.setText(vehicle.getModel());
        trimEditText.setText(vehicle.getTrim());
        styleEditText.setText(vehicle.getStyle());
        engineEditText.setText(vehicle.getEngine());
        descriptionEditText.setText(vehicle.getDescription());
        pricePaidEditText.setText(df.format(vehicle.getPricePaid()));
        if (vehicle.hasSoldPriceBeenSetBefore()) {
            priceSoldEditText.setText(df.format(vehicle.getPriceSold()));
        } else {
            priceSoldEditText.setText("");
        }
        toolbarTitle = vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel();
        toolbar.setTitle(toolbarTitle);
        toolbar.inflateMenu(R.menu.vehicle_detail_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void activateEditMode() {
        editModeEnabled = true;
        toggleEditTextEditable();
        saveVehicleButton.setVisibility(View.VISIBLE);
        savReturnVehicleButton.setVisibility(View.VISIBLE);
        toolbar.getMenu().clear();
        toolbar.setTitle("Edit: " + toolbarTitle);
        toolbar.inflateMenu(R.menu.vehicle_detail_edit_mode_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void toggleEditTextEditable() {
        if (editModeEnabled) {
            vinEditText.setEnabled(true);
            yearEditText.setEnabled(true);
            makeEditText.setEnabled(true);
            modelEditText.setEnabled(true);
            trimEditText.setEnabled(true);
            styleEditText.setEnabled(true);
            engineEditText.setEnabled(true);
            descriptionEditText.setEnabled(true);
            pricePaidEditText.setEnabled(true);
            priceSoldEditText.setEnabled(true);
        } else {
            vinEditText.setEnabled(false);
            yearEditText.setEnabled(false);
            makeEditText.setEnabled(false);
            modelEditText.setEnabled(false);
            trimEditText.setEnabled(false);
            styleEditText.setEnabled(false);
            engineEditText.setEnabled(false);
            descriptionEditText.setEnabled(false);
            pricePaidEditText.setEnabled(false);
            priceSoldEditText.setEnabled(false);
        }
    }

    private class EditInfoTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            frameLayout = (FrameLayout) findViewById(R.id.progress_bar_layout);
            progressBar.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.VISIBLE);
            frameLayout.setClickable(true);
        }

        @Override
        protected Void doInBackground(final String... params) {
            final String vin = params[0];
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(Vehicle.class)
                            .equalTo("Vin", vin)
                            .findFirst()
                            .setYear(params[1]);

                    realm.where(Vehicle.class)
                            .equalTo("Vin", vin)
                            .findFirst()
                            .setMake(params[2]);

                    realm.where(Vehicle.class)
                            .equalTo("Vin", vin)
                            .findFirst()
                            .setModel(params[3]);

                    realm.where(Vehicle.class)
                            .equalTo("Vin", vin)
                            .findFirst()
                            .setTrim(params[4]);

                    realm.where(Vehicle.class)
                            .equalTo("Vin", vin)
                            .findFirst()
                            .setStyle(params[5]);

                    realm.where(Vehicle.class)
                            .equalTo("Vin", vin)
                            .findFirst()
                            .setEngine(params[6]);

                    realm.where(Vehicle.class)
                            .equalTo("Vin", vin)
                            .findFirst()
                            .setDescription(params[7]);

                    if (!params[8].equals("")) {
                        realm.where(Vehicle.class)
                                .equalTo("Vin", vin)
                                .findFirst()
                                .setPricePaid(Double.parseDouble(params[8].toString()));
                    } else {
                        realm.where(Vehicle.class)
                                .equalTo("Vin", vin)
                                .findFirst()
                                .setPricePaid(0);
                    }

                    if (!params[9].equals("")) {
                        realm.where(Vehicle.class)
                                .equalTo("Vin", vin)
                                .findFirst()
                                .setPriceSold(Double.parseDouble(params[9].toString()));
                    } else {
                        realm.where(Vehicle.class)
                                .equalTo("Vin", vin)
                                .findFirst()
                                .setSoldPriceBeenSetBefore(false);
                    }

                    realm.where(Vehicle.class)
                            .equalTo("Vin", vin)
                            .findFirst()
                            .setVin(vin);
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);
            frameLayout.setVisibility(View.GONE);
            frameLayout.setClickable(false);

            if (return_main) {
                finish();
            } else {
                deactivateEditMode();
            }

            toast(toolbarTitle + " updated");

            return_main = false;

            realm = Realm.getDefaultInstance();
        }
    }

    private void toast(String string) {
        Toast toast = Toast.makeText(this, Html.fromHtml("<b>" + string + " <font color=\"green\"><big>&#x2713;</big></font></b>"), Toast.LENGTH_LONG);
//        ViewGroup group = (ViewGroup) toast.getView();
//        TextView messageTextView = (TextView) group.getChildAt(0);
//        messageTextView.(16);
        toast.show();
    }

}
