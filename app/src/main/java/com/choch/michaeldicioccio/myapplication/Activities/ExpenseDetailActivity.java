package com.choch.michaeldicioccio.myapplication.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.choch.michaeldicioccio.myapplication.Defaults;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.Vehicle.Expense;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import java.text.DecimalFormat;

import io.realm.Realm;


public class ExpenseDetailActivity extends AppCompatActivity {

    private Realm realm;
    private Toolbar toolbar;

    private Vehicle vehicle;
    private Expense expense;

    private boolean editModeEnabled = false;
    private String toolbarTitle;

    private EditText expenseTitleEditText, expensePriceEditText, expenseDescriptionEditText;
    private CardView saveVehicleButton;
    private RelativeLayout saveVehicleRelativeLayout;
    private LinearLayout dummyLinearLayout;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;

    private DecimalFormat df = new DecimalFormat(Defaults.DOUBLE_FORMAT.getObject().toString());


    /**
     * inflates the menu for the current activity
     * @param menu - current menu inflated in the actionbar/toolbar
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.expense_detail_menu, menu);
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
        setContentView(R.layout.expense_detail_activity);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getStringExtra("Vin") != null) {
            vehicle = realm.where(Vehicle.class).equalTo("Vin", getIntent().getStringExtra("Vin")).findFirst();
            if (getIntent().getIntExtra("ExpensePosition", -1) != -1) {
                expense = vehicle.getExpenses().get(getIntent().getIntExtra("ExpensePosition", -1));
            }
            toolbarTitle = expense.getTitle();
            getSupportActionBar().setTitle(toolbarTitle);
        }

        expenseTitleEditText = (EditText) findViewById(R.id.expense_title_edit_text);
        expensePriceEditText = (EditText) findViewById(R.id.expense_price_edit_text);
        expenseDescriptionEditText = (EditText) findViewById(R.id.expense_description_edit_text);

        expenseTitleEditText.setText(expense.getTitle());
        expensePriceEditText.setText(df.format(expense.getPrice()));
        expenseDescriptionEditText.setText(expense.getDescription());

        expenseTitleEditText.setEnabled(false);
        expensePriceEditText.setEnabled(false);
        expenseDescriptionEditText.setEnabled(false);

        saveVehicleButton = (CardView) findViewById(R.id.save_expense_button_container);
        saveVehicleRelativeLayout = (RelativeLayout) findViewById(R.id.save_expense_button);
        saveVehicleRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editModeEnabled) {
                    new EditExpenseTask().execute(getEditTextStrings());
                }
            }
        });

        dummyLinearLayout = (LinearLayout) findViewById(R.id.dummy_layout);
        dummyLinearLayout.requestFocus();
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
        toolbar.getMenu().clear();

        expenseTitleEditText.setText(expense.getTitle());
        expensePriceEditText.setText(df.format(expense.getPrice()));
        expenseDescriptionEditText.setText(expense.getDescription());

        toolbarTitle = expense.getTitle();
        toolbar.setTitle(toolbarTitle);
        toolbar.inflateMenu(R.menu.expense_detail_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void activateEditMode() {
        editModeEnabled = true;
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
        protected Void doInBackground(String... params) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Realm realmBackground = Realm.getDefaultInstance();

            Vehicle vehicle = realmBackground.where(Vehicle.class).equalTo("Vin", getIntent().getStringExtra("Vin")).findFirst();
            Expense expense = vehicle.getExpenseAt(getIntent().getIntExtra("ExpensePosition", -1));

            realmBackground.beginTransaction();
            if (expense == null) {
                expense.setTitle(params[0]);
                expense.setPrice(Double.parseDouble(params[1]));
                expense.setDescription(params[2]);
                vehicle.getExpenses().add(expense);

            } else {
                expense.setTitle(params[0]);
                expense.setPrice(Double.parseDouble(params[1]));
                expense.setDescription(params[2]);
            }
            realmBackground.commitTransaction();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            frameLayout.setVisibility(View.GONE);
            frameLayout.setClickable(false);

            deactivateEditMode();

            toast(toolbarTitle + " updated");
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
