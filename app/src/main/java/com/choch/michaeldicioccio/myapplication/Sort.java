package com.choch.michaeldicioccio.myapplication;

/**
 * Created by michaeldicioccio on 9/26/17.
 */

public enum Sort {

    MAKE("Vehicle Make"),
    MODEL("Vehicle Model"),
    DATE("Transaction Date");

    final private String sort_type;

    Sort(String sort_type) {
        this.sort_type = sort_type;
    }

    public String getSortType() {
        return sort_type;
    }
}
