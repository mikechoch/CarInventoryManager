package com.choch.michaeldicioccio.myapplication.Json;

/**
 * Created by michaeldicioccio on 9/12/17.
 */

public enum JsonAttributes {

    ATTRIBUTES("attributes"),
    SUCCESS("success");

    final private String attribute;

    JsonAttributes(String attribute) {
        this.attribute = attribute;
    }

    public String getString() {
        return attribute;
    }
}
