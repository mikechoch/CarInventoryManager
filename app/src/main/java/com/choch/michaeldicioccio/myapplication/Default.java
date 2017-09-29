package com.choch.michaeldicioccio.myapplication;

/**
 * Created by michaeldicioccio on 9/13/17.
 */

public enum Default {

    DOUBLE_FORMAT("0.00"),
    DATE_FORMAT("MM/dd/yyyy"),;

    final private Object object;

    Default(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }
}
