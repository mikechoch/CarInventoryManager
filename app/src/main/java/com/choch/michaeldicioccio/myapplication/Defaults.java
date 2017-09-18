package com.choch.michaeldicioccio.myapplication;

/**
 * Created by michaeldicioccio on 9/13/17.
 */

public enum Defaults {

    DOUBLE_FORMAT("0.00");


    final private Object object;

    Defaults(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }
}
