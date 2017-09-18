package com.choch.michaeldicioccio.myapplication.Activities;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
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

import com.choch.michaeldicioccio.myapplication.Defaults;
import com.choch.michaeldicioccio.myapplication.Json.VehicleDataJson;
import com.choch.michaeldicioccio.myapplication.Json.VinErrorCheck;
import com.choch.michaeldicioccio.myapplication.MessageStrings;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import org.json.JSONException;

import java.io.IOException;
import java.text.DecimalFormat;

import io.realm.Realm;


public class TypeVinActivity extends AppCompatActivity {

    private Realm realm;

    private EditText vinEditText;
    private RelativeLayout keyboardAlphaNumericRelativeLayout;

    private EditText priceEditText;
    private RelativeLayout keyboardCurrencyRelativeLayout;

    private LayoutTransition layoutDisappearingTransition;
    private LayoutTransition layoutAppearingTransition;

    private CardView addCarButtonContainer;
    private RelativeLayout addCarButton;

    private ProgressBar progressBar;
    private FrameLayout frameLayout;

    private DecimalFormat df = new DecimalFormat(Defaults.DOUBLE_FORMAT.getObject().toString());


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
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.type_vin_activity_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Car");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        keyboardAlphaNumericRelativeLayout = (RelativeLayout) findViewById(R.id.alpha_numeric_keyboard_relative_layout);
        keyboardCurrencyRelativeLayout = (RelativeLayout) findViewById(R.id.currency_keyboard_relative_layout);

        layoutDisappearingTransition = new LayoutTransition();
        layoutDisappearingTransition.setDuration(LayoutTransition.DISAPPEARING, 100);

        layoutAppearingTransition = new LayoutTransition();
        layoutAppearingTransition.setDuration(LayoutTransition.APPEARING, 100);

        addCarButtonContainer = (CardView) findViewById(R.id.add_vehicle_button_container);
        addCarButton = (RelativeLayout) findViewById(R.id.add_vehicle_button);
        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vin = vinEditText.getText().toString();

                VinErrorCheck vinErrorCheck = new VinErrorCheck(vin);

                if (vinErrorCheck.verifyVinLength(vin)) {
                    vin = vinErrorCheck.getVin();

                    if (realm.where(Vehicle.class).equalTo("Vin", vin).findFirst() == null) {
                        String[] vinParams = {vin, priceEditText.getText().toString()};
                        new VinCheckTask().execute(vinParams);

                    } else {
                        toast(MessageStrings.VIN_ALREADY_SCANNED.getMessage(), false);
                        vinEditText.setTextColor(getResources().getColor(R.color.colorInvalidVin));
                        vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_invalid));
                    }
                } else {
                    toast(vinErrorCheck.getErrorMessage(), false);
                    vinEditText.setTextColor(getResources().getColor(R.color.colorInvalidVin));
                    vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_invalid));
                }
            }
        });

        setupKeyboardConnectionToEditText();
        setupKeyboardAlphaNumericKeyListeners();
        setupKeyboardCurrencyKeyListeners();

        if (getIntent().getStringExtra("ScannedVin") != null) {
            vinEditText.setText(getIntent().getStringExtra("ScannedVin"));
            priceEditText.requestFocus();
        }
    }

    /**
     *
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void setupKeyboardConnectionToEditText() {
        CoordinatorLayout typeVinCoordinateLayout = (CoordinatorLayout) findViewById(R.id.type_vin_coordinate_layout);

        priceEditText = (EditText) typeVinCoordinateLayout.findViewById(R.id.type_price_edit_text);
        priceEditText.setTextColor(getResources().getColor(R.color.colorIconNotActivated));
        priceEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
        priceEditText.setShowSoftInputOnFocus(false);
        priceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    keyboardCurrencyRelativeLayout.setClickable(true);
                    keyboardCurrencyRelativeLayout.bringToFront();
                    keyboardAlphaNumericRelativeLayout.setClickable(false);
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
                if (vinEditText.getText().length() == 17 && priceEditText.getText().length() > 0) {
                    addCarButtonContainer.setVisibility(View.VISIBLE);
                } else {
                    addCarButtonContainer.setVisibility(View.GONE);
                }
            }
        });


        vinEditText = (EditText) typeVinCoordinateLayout.findViewById(R.id.type_vin_edit_text);
        vinEditText.setTextColor(getResources().getColor(R.color.colorIconNotActivated));
        vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
        vinEditText.setShowSoftInputOnFocus(false);
        vinEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    keyboardAlphaNumericRelativeLayout.setClickable(true);
                    keyboardAlphaNumericRelativeLayout.bringToFront();
                    keyboardCurrencyRelativeLayout.setClickable(false);
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

                if (vinEditText.getText().length() == 17 && priceEditText.getText().length() > 0) {
                    addCarButtonContainer.setVisibility(View.VISIBLE);
                } else {
                    addCarButtonContainer.setVisibility(View.GONE);
                }

                if (vinEditText.getText().length() == 17) {
                    keyboardAlphaNumericRelativeLayout.setLayoutTransition(layoutAppearingTransition);

                    vinEditText.setTextColor(getResources().getColor(R.color.colorValidVin));
                    vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_17));

                } else {
                    keyboardAlphaNumericRelativeLayout.setLayoutTransition(layoutDisappearingTransition);

                    vinEditText.setTextColor(getResources().getColor(R.color.colorIconNotActivated));
                    vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_not_17));
                }
            }
        });
    }

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
                Vehicle vehicle = new VehicleDataJson("https://api.vinaudit.com/query.php?vin=" + vin + "&key=" + getResources().getString(R.string.vin_audit_api) + "&format=json", price_paid).decodeVinNumber();
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
                System.out.println(result.getPricePaid());
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insert(result);
                    }
                });

                toast(result.getYear() + " " + result.getMake() + " " + result.getModel() + " added", true);

                Intent mainActivityIntent = new Intent(TypeVinActivity.this, MainActivity.class);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivityIntent);

            } else {
                toast(MessageStrings.INVALID_VIN.getMessage(), false);
                vinEditText.setTextColor(getResources().getColor(R.color.colorInvalidVin));
                vinEditText.setBackground(getResources().getDrawable(R.drawable.type_vin_underline_invalid));
                progressBar.setVisibility(View.GONE);
                frameLayout.setVisibility(View.GONE);
                frameLayout.setClickable(false);
            }
        }
    }

    private void toast(String string, boolean success) {
        Toast toast;
        if (success) {
            toast = Toast.makeText(this, Html.fromHtml("<b>" + string + " <font color=\"green\"><big>&#x2713;</big></font></b>"), Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(this, Html.fromHtml("<b>" + string + " <font color=\"red\"><big>&#x2717;</big></red></b>"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 100);
        }
        toast.show();
    }

}
