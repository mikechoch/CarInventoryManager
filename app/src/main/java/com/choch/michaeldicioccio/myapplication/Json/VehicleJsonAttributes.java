package com.choch.michaeldicioccio.myapplication.Json;


public enum VehicleJsonAttributes {

    VIN("VIN"),
    YEAR("Year"),
    MAKE("Make"),
    MODEL("Model"),
    TRIM("Trim"),
    MADE_IN("Made In"),
    STYLE("Style"),
    ENGINE("Engine");

    final private String attribute;

    VehicleJsonAttributes(String attribute) {
        this.attribute = attribute;
    }

    public String getString() {
        return attribute;
    }
}
