package com.choch.michaeldicioccio.myapplication.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.choch.michaeldicioccio.myapplication.Default;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.Vehicle.Expense;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import java.text.DecimalFormat;

import io.realm.Realm;


public class ExpenseDetailActivity extends AppCompatActivity {

    /* Globals */
    private Realm realm;
    private Toolbar toolbar;

    private Vehicle vehicle;
    private Expense expense;

    private boolean editModeEnabled = false;
    private boolean newExpense;
    private String toolbarTitle;
    private int expense_position;

    private EditText expenseTitleEditText, expensePriceEditText, expenseDescriptionEditText;
    private TextInputLayout expenseTitleTextInputLayout, expensePriceTextInputLayout, expenseDescriptionTextInputLayout;
    private CardView saveVehicleButton;
    private RelativeLayout saveVehicleRelativeLayout;
    private LinearLayout dummyLinearLayout;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;

    private DecimalFormat df = new DecimalFormat(Default.DOUBLE_FORMAT.getObject().toString());


    /**
     * inflates the menu for the current activity
     * @param menu - current menu inflated in the actionbar/toolbar
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.expense_detail_menu, menu);
        if (getIntent().getIntExtra("ExpensePosition", -1) != -1) {
            deactivateEditMode();
        } else {
            activateEditMode();
        }
        return true;
    }

    /**
     * creates the actions for each item in the actionbar/toolbar menu
     * @param item - current item selected
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            activateEditMode();
            return true;

        } else if (id == R.id.action_save) {
            new EditExpenseTask().execute(getEditTextStrings());
            return true;

        } else if (id == android.R.id.home) {
            if (newExpense) {
                setResultForFinish(false);
            } else {
                if (editModeEnabled) {
                    deactivateEditMode();
                } else {
                    setResultForFinish(false);
                }
            }
            return true;

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
        setContentView(R.layout.expense_detail_activity);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupAllListeners();

        dummyLinearLayout = (LinearLayout) findViewById(R.id.dummy_layout);

        if (getIntent().getStringExtra("vin") != null) {
            vehicle = realm.where(Vehicle.class).equalTo("vin", getIntent().getStringExtra("vin")).findFirst();
            if (getIntent().getIntExtra("ExpensePosition", -1) != -1) {
                newExpense = false;
                expense_position = getIntent().getIntExtra("ExpensePosition", -1);
                expense = vehicle.getExpenses().get(expense_position);
                toolbarTitle = expense.getTitle();

                getSupportActionBar().setTitle(toolbarTitle);

                expenseTitleEditText.setText(expense.getTitle());
                expensePriceEditText.setText(("$" + df.format(expense.getPrice())));
                expenseDescriptionEditText.setText(expense.getDescription());

                expenseTitleEditText.setEnabled(false);
                expensePriceEditText.setEnabled(false);
                expenseDescriptionEditText.setEnabled(false);

                dummyLinearLayout.requestFocus();

            } else {
                newExpense = true;
                toolbarTitle = "New Expense";
                activateEditMode();
            }
        }

    }

    /**
     * handles bottom nav bar click
     * new expenses will return straight to detail view
     * pre-existing expenses will be handled as normal,
     * in edit mode back press will leave edit mode
     * out of edit mode back press will return to detail view
     */
    @Override
    public void onBackPressed() {
        if (newExpense) {
            super.onBackPressed();
            setResultForFinish(false);
        } else {
            if (editModeEnabled) {
                deactivateEditMode();
            } else {
                super.onBackPressed();
                setResultForFinish(false);
            }
        }
    }

    /**
     * sets up all UI element listeners
     */
    public void setupAllListeners() {
        expenseTitleTextInputLayout = (TextInputLayout) findViewById(R.id.expense_title_text_input_layout);
        expensePriceTextInputLayout = (TextInputLayout) findViewById(R.id.expense_price_text_input_layout);
        expenseDescriptionTextInputLayout = (TextInputLayout) findViewById(R.id.expense_description_text_input_layout);

        expenseTitleEditText = (EditText) findViewById(R.id.expense_title_edit_text);
        expenseTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                expenseTitleEditText.setHintTextColor(getResources().getColor(R.color.colorPrimary));
                expenseTitleTextInputLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        expensePriceEditText = (EditText) findViewById(R.id.expense_price_edit_text);
        expensePriceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                expensePriceEditText.setHintTextColor(getResources().getColor(R.color.colorPrimary));
                expensePriceTextInputLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        expenseDescriptionEditText = (EditText) findViewById(R.id.expense_description_edit_text);

        saveVehicleButton = (CardView) findViewById(R.id.save_expense_button_container);
        saveVehicleRelativeLayout = (RelativeLayout) findViewById(R.id.save_expense_button);
        saveVehicleRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validEditTexts()) {
                    new EditExpenseTask().execute(getEditTextStrings());
                }
            }
        });
    }

    /**
     *
     */
    public void deactivateEditMode() {
        editModeEnabled = false;

        expenseTitleEditText.setText(expense.getTitle());
        expensePriceEditText.setText(("$" + df.format(expense.getPrice())));
        expenseDescriptionEditText.setText(expense.getDescription());

        toggleEditTextEditable();
        toolbar.getMenu().clear();
        toolbarTitle = expense.getTitle();
        toolbar.setTitle(toolbarTitle);
        toolbar.inflateMenu(R.menu.expense_detail_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void activateEditMode() {
        editModeEnabled = true;

        expensePriceEditText.setText(String.valueOf(expense.getPrice()));

        toggleEditTextEditable();
        toolbar.getMenu().clear();
        toolbar.setTitle("Edit: " + toolbarTitle);
        toolbar.inflateMenu(R.menu.expense_detail_edit_mode_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void toggleEditTextEditable() {
        if (editModeEnabled) {
            expenseTitleEditText.setEnabled(true);
            expensePriceEditText.setEnabled(true);
            expenseDescriptionEditText.setEnabled(true);
            saveVehicleButton.setVisibility(View.VISIBLE);

        } else {
            expenseTitleEditText.setEnabled(false);
            expensePriceEditText.setEnabled(false);
            expenseDescriptionEditText.setEnabled(false);
            saveVehicleButton.setVisibility(View.GONE);

            dummyLinearLayout.requestFocus();
        }
    }

    private String[] getEditTextStrings() {
        String[] strings = {
                expenseTitleEditText.getText().toString(),
                expensePriceEditText.getText().toString(),
                expenseDescriptionEditText.getText().toString()};

        return strings;
    }

    private boolean validEditTexts() {
        boolean passed = true;

        boolean[] validEditTexts = {expenseTitleEditText.getText().toString().equals(""),
                expensePriceEditText.getText().toString().equals(""),
                !(isNumeric(expensePriceEditText.getText().toString()))};

        for (int i = 0 ; i < validEditTexts.length; i++) {
            if (validEditTexts[i]) {
                switch(i) {
                    case 0:
                        expenseTitleTextInputLayout.setError("Enter a expense title");
                        passed = false;
                        break;
                    case 1:
                        expensePriceTextInputLayout.setError("Enter a valid price");
                        passed = false;
                        break;
                    case 2:
                        expensePriceTextInputLayout.setError("Enter a valid price");
                        passed = false;
                        break;
                }
            }
        }

        return passed;
    }

    private boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private void setResultForFinish(boolean resultForFinish) {
        Intent returnIntent = new Intent();
        if (resultForFinish) {
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            setResult(Activity.RESULT_CANCELED, returnIntent);
        }
        finish();
    }

    private class EditExpenseTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            frameLayout = (FrameLayout) findViewById(R.id.progress_bar_layout);
            progressBar.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.VISIBLE);
            frameLayout.setClickable(true);
        }

        @Override
        protected Void doInBackground(final String... params) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Realm backgroundRealm = Realm.getDefaultInstance();
            backgroundRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm executeRealm) {
                    Vehicle vehicle = executeRealm.where(Vehicle.class).equalTo("vin", getIntent().getStringExtra("vin")).findFirst();
                    Expense temp_expense = null;

                    if (!newExpense) {
                        temp_expense = vehicle.getExpenseAt(getIntent().getIntExtra("ExpensePosition", -1));
                    } else {
                        temp_expense = executeRealm.createObject(Expense.class);
                    }

                    temp_expense.setTitle(params[0]);
                    temp_expense.setPrice(Double.parseDouble(params[1]));
                    temp_expense.setDescription(params[2]);

                    if (getIntent().getIntExtra("ExpensePosition", -1) == -1) {
                        vehicle.addExpense(temp_expense);
                    } else {
                        vehicle.getExpenses().add(getIntent().getIntExtra("ExpensePosition", -1), temp_expense);
                    }
                }
            });

            backgroundRealm.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);
            frameLayout.setVisibility(View.GONE);
            frameLayout.setClickable(false);

            vehicle = realm.where(Vehicle.class).equalTo("vin", getIntent().getStringExtra("vin")).findFirst();
            if (getIntent().getIntExtra("ExpensePosition", -1) == -1) {
                expense = vehicle.getExpenseAt(vehicle.getExpenseCount() - 1);
            } else {
                expense = vehicle.getExpenseAt(expense_position);
            }
            deactivateEditMode();

            validationToast(toolbarTitle + " updated");

            setResultForFinish(true);
        }
    }

    /**
     * shortcut for toasting message to user
     * @param toast_string - String to toast
     */
    private void validationToast(String toast_string) {
        Toast toast = Toast.makeText(this, Html.fromHtml("<b>" + toast_string + " <font color=\"green\"><big>&#x2713;</big></font></b>"), Toast.LENGTH_LONG);
//        ViewGroup group = (ViewGroup) validationToast.getView();
//        TextView messageTextView = (TextView) group.getChildAt(0);
//        messageTextView.(16);
        toast.show();
    }

}
