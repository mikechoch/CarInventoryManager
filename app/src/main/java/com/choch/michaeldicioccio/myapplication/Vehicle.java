package com.choch.michaeldicioccio.myapplication;

import io.realm.RealmObject;

/**
 * Created by michaeldicioccio on 9/5/17.
 */

public class Vehicle extends RealmObject {

    /* Attributes */
    String VIN;
    String year;
    String make;
    String model;
    boolean sold;

    /* Getters */
    public String getVIN() {
        return VIN;
    }

    public String getYear() {
        return year;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public boolean isSold() {
        return sold;
    }

    /* Setters */
    public void setVIN(String VIN) {
        this.VIN = VIN;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

}
