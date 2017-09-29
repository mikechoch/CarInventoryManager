package com.choch.michaeldicioccio.myapplication;


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
