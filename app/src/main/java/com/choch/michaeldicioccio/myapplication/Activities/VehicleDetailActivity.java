package com.choch.michaeldicioccio.myapplication.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.choch.michaeldicioccio.myapplication.Defaults;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.RecyclerViewClickListener;
import com.choch.michaeldicioccio.myapplication.Vehicle.CustomExpensesRecyclerViewAdapter;
import com.choch.michaeldicioccio.myapplication.Vehicle.Expense;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;


public class VehicleDetailActivity extends AppCompatActivity {

    private Realm realm;
    private boolean editModeEnabled = false;
    private String toolbarTitle;
    private DecimalFormat df = new DecimalFormat(Defaults.DOUBLE_FORMAT.getObject().toString());

    private ArrayList<Expense> expensesArrayList;
    private RecyclerView swipeExpensesRecyclerView, noSwipeExpensesRecyclerView;
    private CustomExpensesRecyclerViewAdapter
            customSwipeExpensesRecyclerViewAdapter, customNoSwipeExpensesRecyclerViewAdapter;

    private Vehicle vehicle;

    private Toolbar toolbar;
    private TextView noExpensesTextView;
    private EditText
            vinEditText, yearEditText, makeEditText, modelEditText, trimEditText, styleEditText,
            engineEditText, dateBoughtEditText, pricePaidEditText, dateSoldEditText, priceSoldEditText, descriptionEditText;
    private CardView addExpenseButtonCardView, deleteAllExpensesButtonCardView, saveVehicleButtonCardView;
    private RelativeLayout addExpenseButtonRelativeLayout, deleteAllExpensesButtonRelativeLayout, saveVehicleButtonRelativeLayout;
    private LinearLayout dummyLinearLayout;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;


    /**
     * inflates the menu for the current activity
     * @param menu - current menu inflated in the actionbar/toolbar
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            activateEditMode();
            return true;

        } else if (id == R.id.action_save) {
            new EditInfoTask().execute(getEditTextStrings());
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

        if (getIntent().getStringExtra("vin") != null) {
            vehicle = realm.copyFromRealm(realm.where(Vehicle.class).equalTo("vin", getIntent().getStringExtra("vin")).findFirst());
            toolbarTitle = vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel();
            expensesArrayList = new ArrayList<>(vehicle.getExpenses());
            getSupportActionBar().setTitle(toolbarTitle);
        }

        vinEditText = (EditText) findViewById(R.id.vin_edit_text);
        yearEditText = (EditText) findViewById(R.id.year_edit_text);
        makeEditText = (EditText) findViewById(R.id.make_edit_text);
        modelEditText = (EditText) findViewById(R.id.model_edit_text);
        trimEditText = (EditText) findViewById(R.id.trim_edit_text);
        styleEditText = (EditText) findViewById(R.id.style_edit_text);
        engineEditText = (EditText) findViewById(R.id.engine_edit_text);
        dateBoughtEditText = (EditText) findViewById(R.id.date_bought_edit_text);
        pricePaidEditText = (EditText) findViewById(R.id.price_paid_edit_text);
        dateSoldEditText = (EditText) findViewById(R.id.date_sold_edit_text);
        priceSoldEditText = (EditText) findViewById(R.id.price_sold_edit_text);
        descriptionEditText = (EditText) findViewById(R.id.description_edit_text);

        vinEditText.setEnabled(false);
        yearEditText.setEnabled(false);
        makeEditText.setEnabled(false);
        modelEditText.setEnabled(false);
        trimEditText.setEnabled(false);
        styleEditText.setEnabled(false);
        engineEditText.setEnabled(false);
        dateBoughtEditText.setEnabled(false);
        pricePaidEditText.setEnabled(false);
        dateSoldEditText.setEnabled(false);
        priceSoldEditText.setEnabled(false);
        descriptionEditText.setEnabled(false);

        vinEditText.setText(vehicle.getVin());
        yearEditText.setText(vehicle.getYear());
        makeEditText.setText(vehicle.getMake());
        modelEditText.setText(vehicle.getModel());
        trimEditText.setText(vehicle.getTrim());
        styleEditText.setText(vehicle.getStyle());
        engineEditText.setText(vehicle.getEngine());
        descriptionEditText.setText(vehicle.getDescription());

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date date1 = calendar.getTime();
        dateBoughtEditText.setText(dateFormat.format(date));
        dateSoldEditText.setText(dateFormat.format(date1));

        if (vehicle.hasPaidPriceBeenSetBefore()) {
            pricePaidEditText.setText(df.format(vehicle.getPricePaid()));
        } else {
            pricePaidEditText.setText("");
        }

        if (vehicle.hasSoldPriceBeenSetBefore()) {
            priceSoldEditText.setText(df.format(vehicle.getPriceSold()));
        } else {
            priceSoldEditText.setText("");
        }

        addExpenseButtonCardView = (CardView) findViewById(R.id.add_expense_button_container);
        addExpenseButtonCardView.setVisibility(View.VISIBLE);
        addExpenseButtonRelativeLayout = (RelativeLayout) findViewById(R.id.add_expense_button);
        addExpenseButtonRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startExpenseDetailActivity(-1);
            }
        });

        deleteAllExpensesButtonCardView = (CardView) findViewById(R.id.delete_all_expenses_button_container);
        deleteAllExpensesButtonCardView.setVisibility(View.GONE);
        deleteAllExpensesButtonRelativeLayout = (RelativeLayout) findViewById(R.id.delete_all_expenses_button);
        deleteAllExpensesButtonRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VehicleDetailActivity.this);
                //set message
                builder.setMessage("All expenses will be deleted once you save, are you sure you want to delete all expenses?");

                //when click on DELETE
                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        expensesArrayList.clear();

                        customSwipeExpensesRecyclerViewAdapter.notifyDataSetChanged();
                        customNoSwipeExpensesRecyclerViewAdapter.notifyDataSetChanged();

                        deleteAllExpensesButtonCardView.setVisibility(View.GONE);
                        if (customSwipeExpensesRecyclerViewAdapter.getItemCount() > 0) {
                            noExpensesTextView.setVisibility(View.GONE);
                            if (editModeEnabled) {
                                swipeExpensesRecyclerView.setVisibility(View.VISIBLE);
                                noSwipeExpensesRecyclerView.setVisibility(View.GONE);
                            } else {
                                swipeExpensesRecyclerView.setVisibility(View.GONE);
                                noSwipeExpensesRecyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            noExpensesTextView.setVisibility(View.VISIBLE);
                            swipeExpensesRecyclerView.setVisibility(View.GONE);
                            noSwipeExpensesRecyclerView.setVisibility(View.GONE);
                        }

                        dialogInterface.dismiss();
                    }
                    //not removing items if cancel is done
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.cancel();
                    }
                }).setCancelable(false);

                final AlertDialog dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(
                                getResources().getColor(R.color.colorPrimary));

                        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(
                                Color.BLACK);
                    }
                });
                dialog.show();
            }
        });

        saveVehicleButtonCardView = (CardView) findViewById(R.id.save_vehicle_button_container);
        saveVehicleButtonRelativeLayout = (RelativeLayout) findViewById(R.id.save_vehicle_button);
        saveVehicleButtonRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EditInfoTask().execute(getEditTextStrings());
            }
        });

        swipeExpensesRecyclerView = (RecyclerView) findViewById(R.id.swipe_expenses_recycler_view);
        customSwipeExpensesRecyclerViewAdapter = new CustomExpensesRecyclerViewAdapter(
                this,
                expensesArrayList,
                new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
//                        startExpenseDetailActivity(position);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                });

        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(this);

        swipeExpensesRecyclerView.setLayoutManager(layoutManager1);
        swipeExpensesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(
                    RecyclerView recyclerView,
                    RecyclerView.ViewHolder viewHolder,
                    RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT | direction == ItemTouchHelper.RIGHT) {
//                    final Expense expense = expensesArrayList.get(position);
                    customSwipeExpensesRecyclerViewAdapter.removeAt(position);

                    if (customSwipeExpensesRecyclerViewAdapter.getItemCount() > 0) {
                        noExpensesTextView.setVisibility(View.GONE);
                        if (editModeEnabled) {
                            swipeExpensesRecyclerView.setVisibility(View.VISIBLE);
                            noSwipeExpensesRecyclerView.setVisibility(View.GONE);
                        } else {
                            swipeExpensesRecyclerView.setVisibility(View.GONE);
                            noSwipeExpensesRecyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        noExpensesTextView.setVisibility(View.VISIBLE);
                        swipeExpensesRecyclerView.setVisibility(View.GONE);
                        noSwipeExpensesRecyclerView.setVisibility(View.GONE);
                    }

//                    CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.vehicle_detail_coordinate_layout);
//                    Snackbar.make(coordinatorLayout, "Deleted " + expense.getTitle(), Snackbar.LENGTH_SHORT)
//                            .setAction("UNDO", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    customSwipeExpensesRecyclerViewAdapter.addAt(position, expense);
//
//                                    if (customSwipeExpensesRecyclerViewAdapter.getItemCount() > 0) {
//                                        noExpensesTextView.setVisibility(View.GONE);
//                                        if (editModeEnabled) {
//                                            swipeExpensesRecyclerView.setVisibility(View.VISIBLE);
//                                            noSwipeExpensesRecyclerView.setVisibility(View.GONE);
//                                        } else {
//                                            swipeExpensesRecyclerView.setVisibility(View.GONE);
//                                            noSwipeExpensesRecyclerView.setVisibility(View.VISIBLE);
//                                        }
//                                    } else {
//                                        noExpensesTextView.setVisibility(View.VISIBLE);
//                                        swipeExpensesRecyclerView.setVisibility(View.GONE);
//                                        noSwipeExpensesRecyclerView.setVisibility(View.GONE);
//                                    }
//                                }
//                            })
//                            .setActionTextColor(getResources().getColor(R.color.colorPrimary))
//                            .show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(swipeExpensesRecyclerView); //set swipe to recylcerview

        swipeExpensesRecyclerView.setAdapter(customSwipeExpensesRecyclerViewAdapter);

        noSwipeExpensesRecyclerView = (RecyclerView) findViewById(R.id.no_swipe_expenses_recycler_view);
        customNoSwipeExpensesRecyclerViewAdapter = new CustomExpensesRecyclerViewAdapter(
                this,
                expensesArrayList,
                new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        startExpenseDetailActivity(position);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                });

        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);

        noSwipeExpensesRecyclerView.setLayoutManager(layoutManager2);
        noSwipeExpensesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        noSwipeExpensesRecyclerView.setAdapter(customNoSwipeExpensesRecyclerViewAdapter);

        noExpensesTextView = (TextView) findViewById(R.id.no_expenses_text_view);
        if (customSwipeExpensesRecyclerViewAdapter.getItemCount() > 0) {
            noExpensesTextView.setVisibility(View.GONE);
            if (editModeEnabled) {
                swipeExpensesRecyclerView.setVisibility(View.VISIBLE);
                noSwipeExpensesRecyclerView.setVisibility(View.GONE);
            } else {
                swipeExpensesRecyclerView.setVisibility(View.GONE);
                noSwipeExpensesRecyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            noExpensesTextView.setVisibility(View.VISIBLE);
            swipeExpensesRecyclerView.setVisibility(View.GONE);
            noSwipeExpensesRecyclerView.setVisibility(View.GONE);
        }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK){
                vehicle = realm.copyFromRealm(realm.where(Vehicle.class).equalTo("vin", getIntent().getStringExtra("vin")).findFirst());

                expensesArrayList.clear();
                for (int i = 0; i < vehicle.getExpenseCount(); i++) {
                    expensesArrayList.add(vehicle.getExpenseAt(i));
                }

                customSwipeExpensesRecyclerViewAdapter.notifyDataSetChanged();
                customNoSwipeExpensesRecyclerViewAdapter.notifyDataSetChanged();

                if (customSwipeExpensesRecyclerViewAdapter.getItemCount() > 0) {
                    noExpensesTextView.setVisibility(View.GONE);
                    if (editModeEnabled) {
                        swipeExpensesRecyclerView.setVisibility(View.VISIBLE);
                        noSwipeExpensesRecyclerView.setVisibility(View.GONE);
                    } else {
                        swipeExpensesRecyclerView.setVisibility(View.GONE);
                        noSwipeExpensesRecyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    noExpensesTextView.setVisibility(View.VISIBLE);
                    swipeExpensesRecyclerView.setVisibility(View.GONE);
                    noSwipeExpensesRecyclerView.setVisibility(View.GONE);
                }
            }

            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public void deactivateEditMode() {
        editModeEnabled = false;
        
        descriptionEditText.setText(vehicle.getDescription());

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date date1 = calendar.getTime();
        dateBoughtEditText.setText(dateFormat.format(date));
        dateSoldEditText.setText(dateFormat.format(date1));

        if (vehicle.hasPaidPriceBeenSetBefore()) {
            pricePaidEditText.setText(df.format(vehicle.getPricePaid()));
        } else {
            pricePaidEditText.setText("");
        }

        if (vehicle.hasSoldPriceBeenSetBefore()) {
            priceSoldEditText.setText(df.format(vehicle.getPriceSold()));
        } else {
            priceSoldEditText.setText("");
        }

        expensesArrayList.clear();
        for (int i = 0; i < vehicle.getExpenseCount(); i++) {
            expensesArrayList.add(vehicle.getExpenseAt(i));
        }

        customSwipeExpensesRecyclerViewAdapter.notifyDataSetChanged();
        customNoSwipeExpensesRecyclerViewAdapter.notifyDataSetChanged();

        toggleEditTextEditable();
        toolbar.getMenu().clear();
        toolbarTitle = vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel();
        toolbar.setTitle(toolbarTitle);
        toolbar.inflateMenu(R.menu.vehicle_detail_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void activateEditMode() {
        editModeEnabled = true;
        toggleEditTextEditable();
        toolbar.getMenu().clear();
        toolbar.setTitle("Edit: " + toolbarTitle);
        toolbar.inflateMenu(R.menu.vehicle_detail_edit_mode_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void toggleEditTextEditable() {
        if (editModeEnabled) {
            dateBoughtEditText.setEnabled(true);
            pricePaidEditText.setEnabled(true);
            dateSoldEditText.setEnabled(true);
            priceSoldEditText.setEnabled(true);
            descriptionEditText.setEnabled(true);

            addExpenseButtonCardView.setVisibility(View.GONE);
            saveVehicleButtonCardView.setVisibility(View.VISIBLE);

            if (customSwipeExpensesRecyclerViewAdapter.getItemCount() > 0) {
                deleteAllExpensesButtonCardView.setVisibility(View.VISIBLE);

                noExpensesTextView.setVisibility(View.GONE);
                swipeExpensesRecyclerView.setVisibility(View.VISIBLE);
                noSwipeExpensesRecyclerView.setVisibility(View.GONE);
            } else {
                deleteAllExpensesButtonCardView.setVisibility(View.GONE);

                noExpensesTextView.setVisibility(View.VISIBLE);
                swipeExpensesRecyclerView.setVisibility(View.GONE);
                noSwipeExpensesRecyclerView.setVisibility(View.GONE);
            }
        } else {
            dateBoughtEditText.setEnabled(false);
            pricePaidEditText.setEnabled(false);
            dateSoldEditText.setEnabled(false);
            priceSoldEditText.setEnabled(false);
            descriptionEditText.setEnabled(false);

            addExpenseButtonCardView.setVisibility(View.VISIBLE);
            deleteAllExpensesButtonCardView.setVisibility(View.GONE);
            saveVehicleButtonCardView.setVisibility(View.GONE);

            if (customSwipeExpensesRecyclerViewAdapter.getItemCount() > 0) {
                noExpensesTextView.setVisibility(View.GONE);
                swipeExpensesRecyclerView.setVisibility(View.GONE);
                noSwipeExpensesRecyclerView.setVisibility(View.VISIBLE);
            } else {
                noExpensesTextView.setVisibility(View.VISIBLE);
                swipeExpensesRecyclerView.setVisibility(View.GONE);
                noSwipeExpensesRecyclerView.setVisibility(View.GONE);
            }

            dummyLinearLayout.requestFocus();
        }
    }

    private String[] getEditTextStrings() {
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

        return strings;
    }

    private void startExpenseDetailActivity(int position) {
        Intent expenseDetailActivity = new Intent(this, ExpenseDetailActivity.class);
        expenseDetailActivity.putExtra("vin", vehicle.getVin().toString());
        expenseDetailActivity.putExtra("ExpensePosition", position);
        startActivityForResult(expenseDetailActivity, 1);
    }

    private class EditInfoTask extends AsyncTask<String, Void, String> {

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
        protected String doInBackground(final String... params) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Realm backgroundRealm = Realm.getDefaultInstance();
            backgroundRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm executeRealm) {
                    String vin = params[0];
                    Vehicle vehicle = executeRealm.where(Vehicle.class).equalTo("vin", vin).findFirst();

                    vehicle.setVin(vin);
                    vehicle.setYear(params[1]);
                    vehicle.setMake(params[2]);
                    vehicle.setModel(params[3]);
                    vehicle.setTrim(params[4]);
                    vehicle.setStyle(params[5]);
                    vehicle.setEngine(params[6]);
                    vehicle.setDescription(params[7]);

                    if (!params[8].equals("")) {
                        vehicle.setPricePaid(Double.parseDouble(params[8].toString()));
                    } else {
                        vehicle.setPaidPriceBeenSetBefore(false);
                    }

                    if (!params[9].equals("")) {
                        vehicle.setPriceSold(Double.parseDouble(params[9].toString()));
                    } else {
                        vehicle.setSoldPriceBeenSetBefore(false);
                    }

                    vehicle.clearExpenses();
                    for (Expense expense : expensesArrayList) {
                        vehicle.addExpense(expense);
                    }
                }
            });

            backgroundRealm.close();

            return params[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);
            frameLayout.setVisibility(View.GONE);
            frameLayout.setClickable(false);

            vehicle = realm.copyFromRealm(realm.where(Vehicle.class).equalTo("vin", getIntent().getStringExtra("vin")).findFirst());
            deactivateEditMode();

            toast(toolbarTitle + " updated");
        }
    }

    private class DeleteExpensesTask extends AsyncTask<String, Void, String> {

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
        protected String doInBackground(final String... params) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Realm backgroundRealm = Realm.getDefaultInstance();
            backgroundRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm executeRealm) {
                    String vin = params[0];
                    Vehicle vehicle = executeRealm.where(Vehicle.class).equalTo("vin", vin).findFirst();
                    vehicle.clearExpenses();
                }
            });

            backgroundRealm.close();

            return params[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressBar.setVisibility(View.GONE);
            frameLayout.setVisibility(View.GONE);
            frameLayout.setClickable(false);

            vehicle = realm.copyFromRealm(realm.where(Vehicle.class).equalTo("vin", getIntent().getStringExtra("vin")).findFirst());
//            deactivateEditMode();

            toast("All expenses deleted");
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
