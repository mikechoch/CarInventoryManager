package com.choch.michaeldicioccio.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.choch.michaeldicioccio.myapplication.Activities.MainActivity;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by michaeldicioccio on 9/26/17.
 */

public class Sorting extends RealmObject {

    private int sort_index;
    private String sort_type;

    public Sorting() {
        this.sort_index = 1;
        this.sort_type = Sort.DATE.getSortType();
    }

    public int getSortIndex() {
        return sort_index;
    }

    public String getSortType() {
        return sort_type;
    }

    public void setSortIndex(int sort_index) {
        this.sort_index = sort_index;
    }

    public void setSortType(String sort_type) {
        this.sort_type = sort_type;
    }

    public AlertDialog createSortAlertDialog(final Context context) {
        AlertDialog.Builder sortAlertDialogBuilder = new AlertDialog.Builder(context);
        sortAlertDialogBuilder.setTitle("Sort By");
        sortAlertDialogBuilder.setSingleChoiceItems(MainActivity.vehicleSortTypes, sort_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                dialog.dismiss();
                Realm.init(context);
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        sort_index = which;
                        sort_type = MainActivity.vehicleSortTypes[which];
                    }
                });
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return sortAlertDialogBuilder.create();
    }
}
