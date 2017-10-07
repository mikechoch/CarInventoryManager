package com.choch.michaeldicioccio.myapplication.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
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

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.choch.michaeldicioccio.myapplication.Default;
import com.choch.michaeldicioccio.myapplication.R;
import com.choch.michaeldicioccio.myapplication.RecyclerViewClickListener;
import com.choch.michaeldicioccio.myapplication.SublimePickerFragment;
import com.choch.michaeldicioccio.myapplication.Vehicle.CustomExpensesRecyclerViewAdapter;
import com.choch.michaeldicioccio.myapplication.Vehicle.Expense;
import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;


public class VehicleDetailActivity extends AppCompatActivity {

    /* Globals */
    private Realm realm;
    private boolean editModeEnabled = false;
    private String toolbarTitle;
    private Date bought_date, sold_date;

    private ArrayList<Expense> expensesArrayList;
    private RecyclerView swipeExpensesRecyclerView, noSwipeExpensesRecyclerView;
    private CustomExpensesRecyclerViewAdapter
            customSwipeExpensesRecyclerViewAdapter, customNoSwipeExpensesRecyclerViewAdapter;

    private Vehicle vehicle;

    private Toolbar toolbar;
    private TextInputLayout dateSoldTextInputLayout, priceSoldTextInputLayout;
    private TextView noExpensesTextView, clearDateBoughtTextView, clearDateSoldTextView;
    private EditText
            vinEditText, yearEditText, makeEditText, modelEditText, trimEditText, styleEditText,
            engineEditText, buyerNameEdittext, buyerPhoneEditText, buyerEmailEditText,
            dateBoughtEditText, pricePaidEditText, dateSoldEditText, priceSoldEditText,
            descriptionEditText;
    private CardView addExpenseButtonCardView, deleteAllExpensesButtonCardView,
            saveVehicleButtonCardView, vehicleBuyerInfoCardView;
    private RelativeLayout addExpenseButtonRelativeLayout, deleteAllExpensesButtonRelativeLayout, saveVehicleButtonRelativeLayout;
    private LinearLayout dummyLinearLayout;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;

    private DecimalFormat df = new DecimalFormat(Default.DOUBLE_FORMAT.getObject().toString());
    private DateFormat dateFormat = new SimpleDateFormat(Default.DATE_FORMAT.getObject().toString(), Locale.US);


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
                returnToMainFragment();
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

        setupEditTexts();
        setupAllButtons();
        setupAllRecyclerViews();

        dummyLinearLayout = (LinearLayout) findViewById(R.id.dummy_layout);
        dummyLinearLayout.requestFocus();
    }

    /**
     * handles bottom nav bar click
     * edit mode is on will turn it off
     * edit mode is off will return to last shown main fragment
     */
    @Override
    public void onBackPressed() {
        if (editModeEnabled) {
            deactivateEditMode();
        } else {
            returnToMainFragment();
            super.onBackPressed();
        }
    }

    /**
     * handles the on activity result
     *
     * @param requestCode - code given for specific activity to handle specific result
     * @param resultCode - RESULT.OK or RESULT.CANCEL handling
     * @param data - the intent to grab any data passed from an activity to this one
     */
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

    /**
     * setup all edit texts, listeners for edit texts, etc
     */
    private void setupEditTexts() {
        vehicleBuyerInfoCardView = (CardView) findViewById(R.id.vehicle_buyer_info_card_view);

        dateSoldTextInputLayout = (TextInputLayout) findViewById(R.id.date_sold_text_input_layout);
        priceSoldTextInputLayout = (TextInputLayout) findViewById(R.id.price_sold_text_input_layout);

        if (vehicle.isSold()) {
            vehicleBuyerInfoCardView.setVisibility(View.VISIBLE);
            dateSoldTextInputLayout.setVisibility(View.VISIBLE);
            priceSoldTextInputLayout.setVisibility(View.VISIBLE);
        } else {
            vehicleBuyerInfoCardView.setVisibility(View.GONE);
            dateSoldTextInputLayout.setVisibility(View.GONE);
            priceSoldTextInputLayout.setVisibility(View.GONE);
        }

        vinEditText = (EditText) findViewById(R.id.vin_edit_text);
        yearEditText = (EditText) findViewById(R.id.year_edit_text);
        makeEditText = (EditText) findViewById(R.id.make_edit_text);
        modelEditText = (EditText) findViewById(R.id.model_edit_text);
        trimEditText = (EditText) findViewById(R.id.trim_edit_text);
        styleEditText = (EditText) findViewById(R.id.style_edit_text);
        engineEditText = (EditText) findViewById(R.id.engine_edit_text);
        buyerNameEdittext = (EditText) findViewById(R.id.buyer_name_edit_text);
        buyerPhoneEditText = (EditText) findViewById(R.id.buyer_phone_edit_text);
        buyerEmailEditText = (EditText) findViewById(R.id.buyer_email_edit_text);
        dateBoughtEditText = (EditText) findViewById(R.id.date_bought_edit_text);
        pricePaidEditText = (EditText) findViewById(R.id.price_paid_edit_text);
        dateSoldEditText = (EditText) findViewById(R.id.date_sold_edit_text);
        priceSoldEditText = (EditText) findViewById(R.id.price_sold_edit_text);
        descriptionEditText = (EditText) findViewById(R.id.description_edit_text);

        clearDateBoughtTextView = (TextView) findViewById(R.id.clear_date_bought_text_view);
        clearDateBoughtTextView.bringToFront();
        clearDateBoughtTextView.setVisibility(View.GONE);
        clearDateBoughtTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateBoughtEditText.setText("");
                clearDateBoughtTextView.setVisibility(View.GONE);
            }
        });


        clearDateSoldTextView = (TextView) findViewById(R.id.clear_date_sold_text_view);
        clearDateSoldTextView.bringToFront();
        clearDateSoldTextView.setVisibility(View.GONE);
        clearDateSoldTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateSoldEditText.setText("");
                clearDateSoldTextView.setVisibility(View.GONE);
            }
        });

        vinEditText.setEnabled(false);
        yearEditText.setEnabled(false);
        makeEditText.setEnabled(false);
        modelEditText.setEnabled(false);
        trimEditText.setEnabled(false);
        styleEditText.setEnabled(false);
        engineEditText.setEnabled(false);
        buyerNameEdittext.setEnabled(false);
        buyerPhoneEditText.setEnabled(false);
        buyerEmailEditText.setEnabled(false);
        dateBoughtEditText.setEnabled(false);
        pricePaidEditText.setEnabled(false);
        dateSoldEditText.setEnabled(false);
        priceSoldEditText.setEnabled(false);
        descriptionEditText.setEnabled(false);

        dateBoughtEditText.setCursorVisible(false);
        dateBoughtEditText.setShowSoftInputOnFocus(false);
        dateBoughtEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (dateBoughtEditText.getText().toString().equals("")) {
                        bought_date = new Date();
                    } else {
                        try {
                            bought_date = new SimpleDateFormat(Default.DATE_FORMAT.getObject().toString()).parse(dateBoughtEditText.getText().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    createCalendarAlertDialog(dateBoughtEditText, clearDateBoughtTextView, bought_date).show(getFragmentManager(), "SUBLIME_PICKER");
                }
            }
        });
        dateBoughtEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dateBoughtEditText.getText().toString().equals("")) {
                    bought_date = new Date();
                } else {
                    try {
                        bought_date = new SimpleDateFormat(Default.DATE_FORMAT.getObject().toString()).parse(dateBoughtEditText.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                createCalendarAlertDialog(dateBoughtEditText, clearDateBoughtTextView, bought_date).show(getFragmentManager(), "SUBLIME_PICKER");
            }
        });

        dateSoldEditText.setCursorVisible(false);
        dateSoldEditText.setShowSoftInputOnFocus(false);
        dateSoldEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (dateSoldEditText.getText().toString().equals("")) {
                        sold_date = new Date();
                    } else {
                        try {
                            sold_date = new SimpleDateFormat(Default.DATE_FORMAT.getObject().toString()).parse(dateSoldEditText.getText().toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    createCalendarAlertDialog(dateSoldEditText, clearDateSoldTextView, sold_date).show(getFragmentManager(), "SUBLIME_PICKER");
                }
            }
        });
        dateSoldEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dateSoldEditText.getText().toString().equals("")) {
                    sold_date = new Date();
                } else {
                    try {
                        sold_date = new SimpleDateFormat(Default.DATE_FORMAT.getObject().toString()).parse(dateSoldEditText.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                createCalendarAlertDialog(dateSoldEditText, clearDateSoldTextView, sold_date).show(getFragmentManager(), "SUBLIME_PICKER");
            }
        });

        vinEditText.setText(vehicle.getVin());
        yearEditText.setText(vehicle.getYear());
        makeEditText.setText(vehicle.getMake());
        modelEditText.setText(vehicle.getModel());
        trimEditText.setText(vehicle.getTrim());
        styleEditText.setText(vehicle.getStyle());
        engineEditText.setText(vehicle.getEngine());
        buyerNameEdittext.setText(removeExtraSpacesName(vehicle.getVehicleBuyer().getName()));
        buyerPhoneEditText.setText(fancyPhoneNumber(vehicle.getVehicleBuyer().getPhoneNumber()));
        buyerEmailEditText.setText(removeExtraSpacesEmail(vehicle.getVehicleBuyer().getEmail()));
        descriptionEditText.setText(vehicle.getDescription());

        if (vehicle.getBuyDate() != null) {
            dateBoughtEditText.setText(dateFormat.format(vehicle.getBuyDate()));
        } else {
            dateBoughtEditText.setText("");
        }

        if (vehicle.getSellDate() != null) {
            dateSoldEditText.setText(dateFormat.format(vehicle.getSellDate()));
        } else {
            dateSoldEditText.setText("");
        }

        if (vehicle.hasPaidPriceBeenSetBefore()) {
            pricePaidEditText.setText(("$" + df.format(vehicle.getPricePaid())));
        } else {
            pricePaidEditText.setText("");
        }

        if (vehicle.hasSoldPriceBeenSetBefore()) {
            priceSoldEditText.setText(("$" + df.format(vehicle.getPriceSold())));
        } else {
            priceSoldEditText.setText("");
        }
    }

    /**
     * setup all buttons, listeners for buttons, etc
     */
    private void setupAllButtons() {
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
    }

    /**
     * setup all recycler views
     * one is for add expenses and viewing them
     * other one is for swipe deleting and deleting all expenses at once
     */
    private void setupAllRecyclerViews() {
        swipeExpensesRecyclerView = (RecyclerView) findViewById(R.id.swipe_expenses_recycler_view);
        customSwipeExpensesRecyclerViewAdapter = new CustomExpensesRecyclerViewAdapter(
                expensesArrayList,
                new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {

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
                    final Expense expense = expensesArrayList.get(position);
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

                    CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.vehicle_detail_coordinate_layout);
                    Snackbar.make(coordinatorLayout, "Deleted " + expense.getTitle(), Snackbar.LENGTH_SHORT)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    customSwipeExpensesRecyclerViewAdapter.addAt(position, expense);

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
                            })
                            .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                            .show();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(swipeExpensesRecyclerView);

        swipeExpensesRecyclerView.setAdapter(customSwipeExpensesRecyclerViewAdapter);

        noSwipeExpensesRecyclerView = (RecyclerView) findViewById(R.id.no_swipe_expenses_recycler_view);
        customNoSwipeExpensesRecyclerViewAdapter = new CustomExpensesRecyclerViewAdapter(
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
    }

    /**
     * create the Sublime Picker Fragment for choosing dates
     * @param editText - specific edit text to add date to
     * @param clearButton - clear button to handle
     * @param date - date to start SublimePickerFragment on when opening
     * @return - SublimePickerFragment when shown will display a AlertDialog filled with a calendar
     */
    public SublimePickerFragment createCalendarAlertDialog(final EditText editText, final TextView clearButton, Date date) {
        final SublimePickerFragment sublimePickerFragment = new SublimePickerFragment();
        SublimeOptions sublimeOptions = new SublimeOptions();
        sublimeOptions.setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER);
        sublimeOptions.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        sublimeOptions.setCanPickDateRange(false);
        sublimeOptions.setAnimateLayoutChanges(true);
//        sublimeOptions.setDateRange(filterStartDate.getTime(), filterEndDate.getTime()); // useful in future

        if (date == null) {
            date = new Date();
        }

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
                editText.setText(dateFormat.format(selectedDate.getFirstDate().getTime()));
                clearButton.setVisibility(View.VISIBLE);
            }
        };

        sublimePickerFragment.setCallback(mFragmentCallback);
        return sublimePickerFragment;
    }

    /**
     * removes all extra space from a name, before, middle, and after
     * @param name - String name to remove space from
     * @return - String name with spaces removed
     */
    public String removeExtraSpacesName(String name) {
        StringBuilder spaceless_sb = new StringBuilder();
        if (name != null) {
            if (name.length() > 0) {
                boolean first_alpha = false;
                for (char letter : name.toCharArray()) {
                    if (first_alpha && letter == ' ') {
                        spaceless_sb.append(" ");
                        first_alpha = false;
                    } else if (!(letter == ' ')) {
                        first_alpha = true;
                        spaceless_sb.append(letter);
                    }
                }

                if (spaceless_sb.charAt(spaceless_sb.length() - 1) == ' ') {
                    return spaceless_sb.substring(0, spaceless_sb.length() - 1);
                }
                return spaceless_sb.toString();
            }
        }
        return null;
    }

    /**
     * fancies up a phone number with parenthesis for area code and dash for three to four digit
     * @param phone_number - String phone number to fancy
     * @return - fancy String phone number
     */
    public String fancyPhoneNumber(String phone_number) {
        StringBuilder phone_number_sb = new StringBuilder();
        String area_code;
        String three_digit;
        String four_digit;
        if (phone_number != null) {
            switch (phone_number.length()) {
                case 7:
                    three_digit = phone_number.substring(0, 3);
                    four_digit = phone_number.substring(3);
                    phone_number_sb.append(three_digit + "-" + four_digit);
                    return phone_number_sb.toString();
                case 10:
                    area_code = phone_number.substring(0, 3);
                    three_digit = phone_number.substring(3, 6);
                    four_digit = phone_number.substring(6);
                    phone_number_sb.append("(" + area_code + ") " + three_digit + "-" + four_digit);
                    return phone_number_sb.toString();
            }
        }

        return phone_number;
    }

    /**
     * removes all spaces from an email
     * @param email - String email to remove spaces from
     * @return - String email with spaces removed
     */
    public String removeExtraSpacesEmail(String email) {
        StringBuilder email_sb = new StringBuilder();
        if (email != null) {
            if (email.length() > 0) {
                for (char letter : email.toCharArray()) {
                    if (letter != ' ') {
                        email_sb.append(letter);
                    }
                }
                return email_sb.toString();
            }
        }
        return null;
    }

    /**
     * leave edit mode and change UI elements to show edit mode is off
     */
    public void deactivateEditMode() {
        editModeEnabled = false;

        buyerNameEdittext.setText(removeExtraSpacesName(vehicle.getVehicleBuyer().getName()));
        buyerPhoneEditText.setText(fancyPhoneNumber(vehicle.getVehicleBuyer().getPhoneNumber()));
        buyerEmailEditText.setText(removeExtraSpacesEmail(vehicle.getVehicleBuyer().getEmail()));

        if (vehicle.getBuyDate() != null) {
            dateBoughtEditText.setText(dateFormat.format(vehicle.getBuyDate()));
        } else {
            dateBoughtEditText.setText("");
        }

        if (vehicle.getSellDate() != null) {
            dateSoldEditText.setText(dateFormat.format(vehicle.getSellDate()));
        } else {
            dateSoldEditText.setText("");
        }

        if (vehicle.hasPaidPriceBeenSetBefore()) {
            pricePaidEditText.setText(("$" + df.format(vehicle.getPricePaid())));
        } else {
            pricePaidEditText.setText("");
        }

        if (vehicle.hasSoldPriceBeenSetBefore()) {
            priceSoldEditText.setText(("$" + df.format(vehicle.getPriceSold())));
        } else {
            priceSoldEditText.setText("");
        }

        expensesArrayList.clear();
        for (int i = 0; i < vehicle.getExpenseCount(); i++) {
            expensesArrayList.add(vehicle.getExpenseAt(i));
        }

        descriptionEditText.setText(vehicle.getDescription());

        customSwipeExpensesRecyclerViewAdapter.notifyDataSetChanged();
        customNoSwipeExpensesRecyclerViewAdapter.notifyDataSetChanged();

        toggleEditTextEditable();
        toolbar.getMenu().clear();
        toolbarTitle = vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel();
        toolbar.setTitle(toolbarTitle);
        toolbar.inflateMenu(R.menu.vehicle_detail_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * enter edit mode and change UI elements to show edit mode is on
     */
    private void activateEditMode() {
        editModeEnabled = true;

        buyerPhoneEditText.setText(vehicle.getVehicleBuyer().getPhoneNumber());

        if (vehicle.hasPaidPriceBeenSetBefore()) {
            pricePaidEditText.setText(String.valueOf(vehicle.getPricePaid()));
        } else {
            pricePaidEditText.setText("");
        }

        if (vehicle.hasSoldPriceBeenSetBefore()) {
            priceSoldEditText.setText(String.valueOf(vehicle.getPriceSold()));
        } else {
            priceSoldEditText.setText("");
        }

        toggleEditTextEditable();
        toolbar.getMenu().clear();
        toolbar.setTitle("Edit: " + toolbarTitle);
        toolbar.inflateMenu(R.menu.vehicle_detail_edit_mode_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /**
     * edit text toggle for leaving and entering edit mode
     */
    private void toggleEditTextEditable() {
        if (editModeEnabled) {
            if (vehicle.isSold()) {
                vehicleBuyerInfoCardView.setVisibility(View.VISIBLE);
                dateSoldTextInputLayout.setVisibility(View.VISIBLE);
                priceSoldTextInputLayout.setVisibility(View.VISIBLE);
                buyerNameEdittext.setEnabled(true);
                buyerPhoneEditText.setEnabled(true);
                buyerEmailEditText.setEnabled(true);
                dateSoldEditText.setEnabled(true);
                priceSoldEditText.setEnabled(true);
            } else {
                vehicleBuyerInfoCardView.setVisibility(View.GONE);
                dateSoldTextInputLayout.setVisibility(View.GONE);
                priceSoldTextInputLayout.setVisibility(View.GONE);
                buyerNameEdittext.setEnabled(false);
                buyerPhoneEditText.setEnabled(false);
                buyerEmailEditText.setEnabled(false);
                dateSoldEditText.setEnabled(false);
                priceSoldEditText.setEnabled(false);
            }

            dateBoughtEditText.setEnabled(true);
            pricePaidEditText.setEnabled(true);
            descriptionEditText.setEnabled(true);

            if (dateBoughtEditText.getText().toString().equals("")) {
                clearDateBoughtTextView.setVisibility(View.GONE);
            } else {
                clearDateBoughtTextView.setVisibility(View.VISIBLE);
            }

            if (dateSoldEditText.getText().toString().equals("")) {
                clearDateSoldTextView.setVisibility(View.GONE);
            } else {
                clearDateSoldTextView.setVisibility(View.VISIBLE);
            }

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
            buyerNameEdittext.setEnabled(false);
            buyerPhoneEditText.setEnabled(false);
            buyerEmailEditText.setEnabled(false);
            dateBoughtEditText.setEnabled(false);
            pricePaidEditText.setEnabled(false);
            dateSoldEditText.setEnabled(false);
            priceSoldEditText.setEnabled(false);
            descriptionEditText.setEnabled(false);

            clearDateBoughtTextView.setVisibility(View.GONE);
            clearDateSoldTextView.setVisibility(View.GONE);

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

    /**
     * before storing edit text info to Realm, grab all of the edit text Strings
     * @return - return edit texts as a String Array
     */
    private String[] getEditTextStrings() {
        String[] strings = {
                vinEditText.getText().toString(),
                yearEditText.getText().toString(),
                makeEditText.getText().toString(),
                modelEditText.getText().toString(),
                trimEditText.getText().toString(),
                styleEditText.getText().toString(),
                engineEditText.getText().toString(),
                buyerNameEdittext.getText().toString(),
                buyerPhoneEditText.getText().toString(),
                buyerEmailEditText.getText().toString(),
                descriptionEditText.getText().toString(),
                dateBoughtEditText.getText().toString(),
                pricePaidEditText.getText().toString(),
                dateSoldEditText.getText().toString(),
                priceSoldEditText.getText().toString()};

        return strings;
    }

    /**
     * starts the ExpenseDetailActivity based and the data shown is based on the expense clicked
     * @param position
     */
    private void startExpenseDetailActivity(int position) {
        Intent expenseDetailActivity = new Intent(this, ExpenseDetailActivity.class);
        expenseDetailActivity.putExtra("vin", vehicle.getVin().toString());
        expenseDetailActivity.putExtra("ExpensePosition", position);
        startActivityForResult(expenseDetailActivity, 1);
    }

    /**
     * returns to main fragment last shown with RESULT.OK
     */
    private void returnToMainFragment() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    /**
     * ASyncTask for handling saving all VehicleDetail information into Realm
     */
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
                    vehicle.getVehicleBuyer().setName(params[7]);
                    vehicle.getVehicleBuyer().setPhoneNumber(params[8]);
                    vehicle.getVehicleBuyer().setEmail(params[9]);
                    vehicle.setDescription(params[10]);

                    if (!params[11].equals("")) {
                        try {
                            vehicle.setBuyDate(new SimpleDateFormat(Default.DATE_FORMAT.getObject().toString()).parse(params[11]));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        vehicle.setBuyDate(null);
                    }

                    if (!params[12].equals("")) {
                        vehicle.setPricePaid(Double.parseDouble(params[12]));
                    } else {
                        vehicle.setPaidPriceBeenSetBefore(false);
                    }

                    if (!params[13].equals("")) {
                        try {
                            vehicle.setSellDate(new SimpleDateFormat(Default.DATE_FORMAT.getObject().toString()).parse(params[13]));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        vehicle.setSellDate(null);
                    }

                    if (!params[14].equals("")) {
                        vehicle.setPriceSold(Double.parseDouble(params[14]));
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

            validationToast(toolbarTitle + " updated");
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

    /**
     * shortcut for toasting message to user
     * @param toast_string - String to toast
     */
    private void toast(String toast_string) {
        Toast.makeText(this, toast_string, Toast.LENGTH_LONG).show();
    }

}
