package com.choch.michaeldicioccio.myapplication.Activities;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.choch.michaeldicioccio.myapplication.Default;
import com.choch.michaeldicioccio.myapplication.Json.VehicleDataJson;
import com.choch.michaeldicioccio.myapplication.Json.VinErrorCheck;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.SublimePickerFragment;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;


public class TypeVinActivity extends AppCompatActivity {

    /* Globals */
    private Realm realm;

    private Date date = new Date();

    private TextView clearDateBoughtTextView;
    private EditText vinEditText, priceEditText, dateBoughtEditText;
    private TextInputLayout vinTextInputLayout, priceTextInputLayout, dateBoughtTextInputLayout;
    private RelativeLayout keyboardAlphaNumericRelativeLayout, keyboardCurrencyRelativeLayout;
    CoordinatorLayout typeVinCoordinateLayout;

    private LayoutTransition layoutDisappearingTransition, layoutAppearingTransition;

    private CardView addVehicleButtonContainer;
    private RelativeLayout addVehicleButton;

    private ProgressBar progressBar;
    private FrameLayout frameLayout;

    private DecimalFormat df = new DecimalFormat(Default.DOUBLE_FORMAT.getObject().toString());
    private DateFormat dateFormat = new SimpleDateFormat(Default.DATE_FORMAT.getObject().toString(), Locale.US);


    /**
     * creates the actions for each item in the actionbar/toolbar menu
     * @param item - current item selected
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.type_vin_activity_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Vehicle");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        layoutDisappearingTransition = new LayoutTransition();
        layoutAppearingTransition = new LayoutTransition();
        realm = Realm.getDefaultInstance();

        Realm.init(this);

        typeVinCoordinateLayout = (CoordinatorLayout) findViewById(R.id.type_vin_coordinate_layout);
        layoutDisappearingTransition.setDuration(LayoutTransition.DISAPPEARING, 100);
        layoutAppearingTransition.setDuration(LayoutTransition.APPEARING, 100);

        setupAddVehicleButton();
        setupKeyboardConnectionToEditText();
        setupKeyboardAlphaNumericKeyListeners();
        setupKeyboardCurrencyKeyListeners();

        String intentExtra = getIntent().getStringExtra("ScannedVin");
        if (intentExtra != null && !intentExtra.isEmpty()) {
            vinEditText.setText(getIntent().getStringExtra("ScannedVin"));
            priceEditText.requestFocus();
        }
    }

    /**
     * handles on back pressed
     * back press closes returns to previous activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * checks whether network connection is available or not
     * @return - boolean signifying network connection or not
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private boolean passPreValidationChecks(String vin) {
        if (vin == null || vin.isEmpty()) {
            return false;
        }
        if (!isNetworkAvailable()) {
            Snackbar.make(typeVinCoordinateLayout, "No connection", Snackbar.LENGTH_SHORT).setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addVehicleButton.performClick();
                }
            }).show();
            return false;
        }
        if (!new VinErrorCheck(vin).isVinLengthValid(vin)) {
            vinTextInputLayout.setError("Enter a valid vin");
            vinEditText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return false;
        }
        if (vinExistsInRealm(vin)) {
            vinTextInputLayout.setError("Enter a valid vin");
            vinEditText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return false;
        }
        return true;
    }

    private boolean vinExistsInRealm(String vin) {
        return realm.where(Vehicle.class).equalTo("vin", vin).findFirst() != null;
    }


    /**
     *
     */
    private void setupAddVehicleButton() {
        addVehicleButtonContainer = (CardView) findViewById(R.id.add_vehicle_button_container);
        addVehicleButton = (RelativeLayout) findViewById(R.id.add_vehicle_button);
        addVehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vin = vinEditText.getText().toString();
                if (passPreValidationChecks(vin)) {
                    vin = new VinErrorCheck(vin).getVin();
                    String[] vinParams = {vin, priceEditText.getText().toString()};
                    new VinCheckTask().execute(vinParams);
                }
            }
        });
    }

    /**
     * setup the connection between the custom keyboards and the edit texts
     */
    private void setupKeyboardConnectionToEditText() {
        keyboardAlphaNumericRelativeLayout = (RelativeLayout) findViewById(R.id.alpha_numeric_keyboard_relative_layout);
        keyboardCurrencyRelativeLayout = (RelativeLayout) findViewById(R.id.currency_keyboard_relative_layout);

        vinTextInputLayout = (TextInputLayout) findViewById(R.id.type_vin_text_input_layout);
        vinEditText = (EditText) typeVinCoordinateLayout.findViewById(R.id.type_vin_edit_text);
        vinEditText.setTextColor(getResources().getColor(R.color.colorIconNotActivated));
        vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
        vinEditText.setShowSoftInputOnFocus(false);
        vinEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (vinEditText.getText().length() == 17) {
                        vinTextInputLayout.setErrorEnabled(false);
                        vinEditText.setTextColor(getResources().getColor(R.color.colorValidVin));
                        vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_17));

                    } else if (vinEditText.getText().length() == 0) {
                        vinTextInputLayout.setErrorEnabled(false);
                        vinEditText.setTextColor(getResources().getColor(R.color.colorIconNotActivated));
                        vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_17_focus));

                    } else {
                        vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_17_focus));
                        vinTextInputLayout.setError((17 - vinEditText.getText().length()) + " characters remaining");
                        vinEditText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }

                    keyboardAlphaNumericRelativeLayout.setVisibility(View.VISIBLE);
                    keyboardCurrencyRelativeLayout.setVisibility(View.VISIBLE);
                    keyboardAlphaNumericRelativeLayout.setClickable(true);
                    keyboardAlphaNumericRelativeLayout.bringToFront();
                    keyboardCurrencyRelativeLayout.setClickable(false);
                } else {
                    vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
                }
            }
        });

        vinEditText.addTextChangedListener(new TextWatcher() {
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
                    vinEditText.setText(s);
                    vinEditText.setSelection(vinEditText.getText().length());
                }

                if (vinEditText.getText().length() == 17 && priceEditText.getText().length() > 0 && dateBoughtEditText.getText().length() > 0) {
                    addVehicleButtonContainer.setVisibility(View.VISIBLE);
                } else {
                    addVehicleButtonContainer.setVisibility(View.GONE);
                }

                vinTextInputLayout.setErrorEnabled(false);
                if (vinEditText.getText().length() == 17) {
                    vinEditText.setTextColor(getResources().getColor(R.color.colorValidVin));
                    vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_17));

                } else if (vinEditText.getText().length() == 0) {
                    vinEditText.setTextColor(getResources().getColor(R.color.colorIconNotActivated));
                    vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_17_focus));

                } else {
                    vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_17_focus));
                    vinTextInputLayout.setError((17 - vinEditText.getText().length()) + " characters remaining");
                    vinEditText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }
        });

        priceTextInputLayout = (TextInputLayout) findViewById(R.id.type_price_text_input_layout);
        priceEditText = (EditText) typeVinCoordinateLayout.findViewById(R.id.type_price_edit_text);
        priceEditText.setTextColor(getResources().getColor(R.color.colorIconNotActivated));
        priceEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
        priceEditText.setShowSoftInputOnFocus(false);
        priceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    keyboardCurrencyRelativeLayout.setVisibility(View.VISIBLE);
                    keyboardAlphaNumericRelativeLayout.setVisibility(View.VISIBLE);

                    priceEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_17_focus));

                    keyboardCurrencyRelativeLayout.setClickable(true);
                    keyboardCurrencyRelativeLayout.bringToFront();
                    keyboardAlphaNumericRelativeLayout.setClickable(false);
                } else {
                    priceEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
                }
            }
        });
        priceEditText.addTextChangedListener(new TextWatcher() {
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

                if (vinEditText.getText().length() == 17 && priceEditText.getText().length() > 0 && dateBoughtEditText.getText().length() > 0) {
                    addVehicleButtonContainer.setVisibility(View.VISIBLE);
                } else {
                    addVehicleButtonContainer.setVisibility(View.GONE);
                }
            }
        });

        dateBoughtTextInputLayout = (TextInputLayout) findViewById(R.id.type_date_bought_text_input_layout);
        dateBoughtEditText = (EditText) typeVinCoordinateLayout.findViewById(R.id.type_date_bought_edit_text);
        dateBoughtEditText.setTextColor(getResources().getColor(R.color.colorIconNotActivated));
        dateBoughtEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
        dateBoughtEditText.setCursorVisible(false);
        dateBoughtEditText.setShowSoftInputOnFocus(false);
        dateBoughtEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    keyboardAlphaNumericRelativeLayout.setVisibility(View.GONE);
                    keyboardCurrencyRelativeLayout.setVisibility(View.GONE);

                    dateBoughtEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_17_focus));

                    if (dateBoughtEditText.getText().toString().equals("")) {
                        date = new Date();
                    }

                    createCalendarAlertDialog().show(getFragmentManager(), "SUBLIME_PICKER");
                } else {
                    dateBoughtEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
                }
            }
        });
        dateBoughtEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCalendarAlertDialog().show(getFragmentManager(), "SUBLIME_PICKER");
            }
        });
        dateBoughtEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (vinEditText.getText().length() == 17 && priceEditText.getText().length() > 0 && dateBoughtEditText.getText().length() > 0) {
                    addVehicleButtonContainer.setVisibility(View.VISIBLE);
                } else {
                    addVehicleButtonContainer.setVisibility(View.GONE);
                }
            }
        });

        clearDateBoughtTextView = (TextView) findViewById(R.id.type_clear_date_bought_text_view);
        clearDateBoughtTextView.bringToFront();
        clearDateBoughtTextView.setVisibility(View.GONE);
        clearDateBoughtTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateBoughtEditText.setText("");
                clearDateBoughtTextView.setVisibility(View.GONE);
            }
        });
    }

    /**
     *
     */
    private void setupKeyboardAlphaNumericKeyListeners() {
        final RelativeLayout key_1 = (RelativeLayout) findViewById(R.id.key_1);
        key_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView key1TextView = (TextView) findViewById(R.id.key_1_text);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), key1TextView.getText());
            }
        });

        final RelativeLayout key_2 = (RelativeLayout) findViewById(R.id.key_2);
        key_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView key2TextView = (TextView) findViewById(R.id.key_2_text);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), key2TextView.getText());
            }
        });

        final RelativeLayout key_3 = (RelativeLayout) findViewById(R.id.key_3);
        key_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView key3TextView = (TextView) findViewById(R.id.key_3_text);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), key3TextView.getText());
            }
        });

        final RelativeLayout key_4 = (RelativeLayout) findViewById(R.id.key_4);
        key_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView key4TextView = (TextView) findViewById(R.id.key_4_text);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), key4TextView.getText());
            }
        });

        final RelativeLayout key_5 = (RelativeLayout) findViewById(R.id.key_5);
        key_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView key5TextView = (TextView) findViewById(R.id.key_5_text);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), key5TextView.getText());
            }
        });

        final RelativeLayout key_6 = (RelativeLayout) findViewById(R.id.key_6);
        key_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView key6TextView = (TextView) findViewById(R.id.key_6_text);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), key6TextView.getText());
            }
        });

        final RelativeLayout key_7 = (RelativeLayout) findViewById(R.id.key_7);
        key_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView key7TextView = (TextView) findViewById(R.id.key_7_text);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), key7TextView.getText());
            }
        });

        final RelativeLayout key_8 = (RelativeLayout) findViewById(R.id.key_8);
        key_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView key8TextView = (TextView) findViewById(R.id.key_8_text);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), key8TextView.getText());
            }
        });

        final RelativeLayout key_9 = (RelativeLayout) findViewById(R.id.key_9);
        key_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView key9TextView = (TextView) findViewById(R.id.key_9_text);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), key9TextView.getText());
            }
        });


        final RelativeLayout key_0 = (RelativeLayout) findViewById(R.id.key_0);
        key_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView key0TextView = (TextView) findViewById(R.id.key_0_text);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), key0TextView.getText());
            }
        });

        final RelativeLayout key_A = (RelativeLayout) findViewById(R.id.key_A);
        key_A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "A");
            }
        });

        final RelativeLayout key_B = (RelativeLayout) findViewById(R.id.key_B);
        key_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "B");
            }
        });

        final RelativeLayout key_C = (RelativeLayout) findViewById(R.id.key_C);
        key_C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "C");
            }
        });

        final RelativeLayout key_D = (RelativeLayout) findViewById(R.id.key_D);
        key_D.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "D");
            }
        });

        final RelativeLayout key_E = (RelativeLayout) findViewById(R.id.key_E);
        key_E.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "E");
            }
        });

        final RelativeLayout key_F = (RelativeLayout) findViewById(R.id.key_F);
        key_F.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "F");
            }
        });

        final RelativeLayout key_G = (RelativeLayout) findViewById(R.id.key_G);
        key_G.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "G");
            }
        });

        final RelativeLayout key_H = (RelativeLayout) findViewById(R.id.key_H);
        key_H.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "H");
            }
        });

        final RelativeLayout key_I = (RelativeLayout) findViewById(R.id.key_I);
        key_I.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "I");
            }
        });

        final RelativeLayout key_J = (RelativeLayout) findViewById(R.id.key_J);
        key_J.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "J");
            }
        });

        final RelativeLayout key_K = (RelativeLayout) findViewById(R.id.key_K);
        key_K.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "K");
            }
        });

        final RelativeLayout key_L = (RelativeLayout) findViewById(R.id.key_L);
        key_L.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "L");
            }
        });

        final RelativeLayout key_M = (RelativeLayout) findViewById(R.id.key_M);
        key_M.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "M");
            }
        });

        final RelativeLayout key_N = (RelativeLayout) findViewById(R.id.key_N);
        key_N.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "N");
            }
        });

        final RelativeLayout key_O = (RelativeLayout) findViewById(R.id.key_O);
        key_O.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "O");
            }
        });

        final RelativeLayout key_P = (RelativeLayout) findViewById(R.id.key_P);
        key_P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "P");
            }
        });

        final RelativeLayout key_Q = (RelativeLayout) findViewById(R.id.key_Q);
        key_Q.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "Q");
            }
        });

        final RelativeLayout key_R = (RelativeLayout) findViewById(R.id.key_R);
        key_R.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "R");
            }
        });

        final RelativeLayout key_S = (RelativeLayout) findViewById(R.id.key_S);
        key_S.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "S");
            }
        });

        final RelativeLayout key_T = (RelativeLayout) findViewById(R.id.key_T);
        key_T.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "T");
            }
        });

        final RelativeLayout key_U = (RelativeLayout) findViewById(R.id.key_U);
        key_U.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "U");
            }
        });

        final RelativeLayout key_V = (RelativeLayout) findViewById(R.id.key_V);
        key_V.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "V");
            }
        });

        final RelativeLayout key_W = (RelativeLayout) findViewById(R.id.key_W);
        key_W.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "W");
            }
        });

        final RelativeLayout key_X = (RelativeLayout) findViewById(R.id.key_X);
        key_X.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "X");
            }
        });

        final RelativeLayout key_Y = (RelativeLayout) findViewById(R.id.key_Y);
        key_Y.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "Y");
            }
        });

        final RelativeLayout key_Z = (RelativeLayout) findViewById(R.id.key_Z);
        key_Z.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.getText().replace(vinEditText.getSelectionStart(), vinEditText.getSelectionEnd(), "Z");
            }
        });

        final RelativeLayout key_backspace = (RelativeLayout) findViewById(R.id.key_backspace);
        key_backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                int start = vinEditText.getSelectionStart();
                int end = vinEditText.getSelectionEnd();
                if (start > 0 && start == end) {
                    vinEditText.getText().replace(start-1, end, "");
                } else {
                    vinEditText.getText().replace(start, end, "");
                }
            }
        });

        key_backspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity.vibrator.vibrate(10);
                vinEditText.setText("");
                return true;
            }
        });
    }

    private void setupKeyboardCurrencyKeyListeners() {
        final RelativeLayout currency_key_1 = (RelativeLayout) findViewById(R.id.currency_key_1);
        currency_key_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKey1TextView = (TextView) findViewById(R.id.currency_key_1_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKey1TextView.getText());
            }
        });

        final RelativeLayout currency_key_2 = (RelativeLayout) findViewById(R.id.currency_key_2);
        currency_key_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKey2TextView = (TextView) findViewById(R.id.currency_key_2_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKey2TextView.getText());
            }
        });

        final RelativeLayout currency_key_3 = (RelativeLayout) findViewById(R.id.currency_key_3);
        currency_key_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKey3TextView = (TextView) findViewById(R.id.currency_key_3_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKey3TextView.getText());
            }
        });

        final RelativeLayout currency_key_4 = (RelativeLayout) findViewById(R.id.currency_key_4);
        currency_key_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKey4TextView = (TextView) findViewById(R.id.currency_key_4_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKey4TextView.getText());
            }
        });

        final RelativeLayout currency_key_5 = (RelativeLayout) findViewById(R.id.currency_key_5);
        currency_key_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKey5TextView = (TextView) findViewById(R.id.currency_key_5_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKey5TextView.getText());
            }
        });

        final RelativeLayout currency_key_6 = (RelativeLayout) findViewById(R.id.currency_key_6);
        currency_key_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKey6TextView = (TextView) findViewById(R.id.currency_key_6_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKey6TextView.getText());
            }
        });

        final RelativeLayout currency_key_7 = (RelativeLayout) findViewById(R.id.currency_key_7);
        currency_key_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKey7TextView = (TextView) findViewById(R.id.currency_key_7_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKey7TextView.getText());
            }
        });

        final RelativeLayout currency_key_8 = (RelativeLayout) findViewById(R.id.currency_key_8);
        currency_key_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKey8TextView = (TextView) findViewById(R.id.currency_key_8_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKey8TextView.getText());
            }
        });

        final RelativeLayout currency_key_9 = (RelativeLayout) findViewById(R.id.currency_key_9);
        currency_key_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKey9TextView = (TextView) findViewById(R.id.currency_key_9_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKey9TextView.getText());
            }
        });


        final RelativeLayout currency_key_0 = (RelativeLayout) findViewById(R.id.currency_key_0);
        currency_key_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKey0TextView = (TextView) findViewById(R.id.currency_key_0_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKey0TextView.getText());
            }
        });

        final RelativeLayout currency_key_dot = (RelativeLayout) findViewById(R.id.currency_key_dot);
        currency_key_dot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                TextView currencyKeyDotTextView = (TextView) findViewById(R.id.currency_key_dot_text);
                priceEditText.getText().replace(priceEditText.getSelectionStart(), priceEditText.getSelectionEnd(), currencyKeyDotTextView.getText());
            }
        });

        final RelativeLayout currency_key_backspace = (RelativeLayout) findViewById(R.id.currency_key_backspace);
        currency_key_backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.vibrator.vibrate(10);
                int start = priceEditText.getSelectionStart();
                int end = priceEditText.getSelectionEnd();
                if (start > 0 && start == end) {
                    priceEditText.getText().replace(start-1, end, "");
                } else {
                    priceEditText.getText().replace(start, end, "");
                }
            }
        });

        currency_key_backspace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity.vibrator.vibrate(10);
                priceEditText.setText("");
                return true;
            }
        });
    }

    /**
     * create the Sublime Picker Fragment for choosing dates
     * @return - SublimePickerFragment when shown will display a AlertDialog filled with a calendar
     */
    public SublimePickerFragment createCalendarAlertDialog() {
        final SublimePickerFragment sublimePickerFragment = new SublimePickerFragment();
        SublimeOptions sublimeOptions = new SublimeOptions();
        sublimeOptions.setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER);
        sublimeOptions.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        sublimeOptions.setDateParams(Calendar.getInstance());
        sublimeOptions.setCanPickDateRange(false);
        sublimeOptions.setAnimateLayoutChanges(true);
//        sublimeOptions.setDateRange(filterStartDate.getTime(), filterEndDate.getTime()); //TODO: useful in future

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(date);
        SelectedDate selectedDate = new SelectedDate(startCalendar);
        sublimeOptions.setDateParams(selectedDate);

        Bundle bundle = new Bundle();
        bundle.putParcelable("SUBLIME_OPTIONS", sublimeOptions);
        sublimePickerFragment.setArguments(bundle);
        sublimePickerFragment.setStyle(R.style.SPStyle, 0);
        final SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
            @Override
            public void onCancelled() {/* do nothing */}
            @Override
            public void onDateTimeRecurrenceSet(SelectedDate selectedDate, int hourOfDay, int minute, SublimeRecurrencePicker.RecurrenceOption recurrenceOption, String recurrenceRule) {
                sublimePickerFragment.dismiss();
                date = selectedDate.getFirstDate().getTime();
                dateBoughtEditText.setText(dateFormat.format(date));
                clearDateBoughtTextView.setVisibility(View.VISIBLE);
            }
        };

        sublimePickerFragment.setCallback(mFragmentCallback);
        return sublimePickerFragment;
    }

    /**
     * ASyncTask for handling Vin number verification
     * Vin number invalid will notify user with an error
     * Vin number valid and exists will notify user that the vehicle already exists
     * Vin number valid and does not exist will take user to TypeVinActivity and fill in edit text
     */
    private class VinCheckTask extends AsyncTask<String, Void, Vehicle> {

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
        protected Vehicle doInBackground(String... params) {
            String vin = params[0];
            String price_paid = df.format(Double.parseDouble(params[1]));

            try {
                Vehicle vehicle = new VehicleDataJson("https://api.vinaudit.com/query.php?vin=" + vin + "&key=" + getResources().getString(R.string.vin_audit_api) + "&format=json", price_paid, date).decodeVinNumber();
                return vehicle;
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Vehicle result) {
            super.onPostExecute(result);
            if (result != null) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insert(result);
                    }
                });

                validationToast(result.getYear() + " " + result.getMake() + " " + result.getModel() + " added", true);

                Intent mainActivityIntent = new Intent(TypeVinActivity.this, MainActivity.class);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivityIntent);

            } else {
                vinTextInputLayout.setError("Enter a valid vin");
                vinEditText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                progressBar.setVisibility(View.GONE);
                frameLayout.setVisibility(View.GONE);
                frameLayout.setClickable(false);
            }
        }
    }

    /**
     * shortcut for toasting message to user
     * @param toast_string - String to toast
     * @param success - valid or not, decides whether to print and X or Check in the toast
     */
    private void validationToast(String toast_string, boolean success) {
        Toast toast;
        if (success) {
            toast = Toast.makeText(this, Html.fromHtml("<b>" + toast_string + " <font color=\"green\"><big>&#x2713;</big></font></b>"), Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(this, Html.fromHtml("<b>" + toast_string + " <font color=\"red\"><big>&#x2717;</big></red></b>"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 100);
        }
        toast.show();
    }

    /**
     * shortcut for toasting message to user
     * @param toast_string - String to toast
     */
    private void toast(String toast_string) {
        Toast.makeText(this, toast_string, Toast.LENGTH_LONG).show();
    }

}
