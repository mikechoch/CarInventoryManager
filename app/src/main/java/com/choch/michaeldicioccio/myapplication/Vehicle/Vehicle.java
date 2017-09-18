package com.choch.michaeldicioccio.myapplication.Vehicle;

import io.realm.RealmObject;

/**
 * Created by michaeldicioccio on 9/5/17.
 */

public class Vehicle extends RealmObject {

    /* Attributes */
    String Vin;
    String year;
    String make;
    String model;
    String trim;
    String made_in;
    String style;
    String engine;
    String description;

    double price_paid;
    double price_sold;

    boolean sold_price_set;
    boolean sold;

    public Vehicle () {
        this.sold_price_set = false;
        this.sold = false;
    }

    /* Getters */
    public String getVin() {
        return Vin;
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

    public String getTrim() {
        return trim;
    }

    public String getLocationMade() {
        return made_in;
    }

    public String getStyle() {
        return style;
    }

    public String getEngine() {
        return engine;
    }

    public String getDescription() {
        return description;
    }

    public double getPricePaid() {
        return price_paid;
    }

    public double getPriceSold() {
        return price_sold;
    }

    public boolean hasSoldPriceBeenSetBefore() {
        return sold_price_set;
    }

    public boolean isSold() {
        return sold;
    }

    /* Setters */
    public void setVin(String vin) {
        this.Vin = vin;
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

    public void setTrim(String trim) {
        this.trim = trim;
    }

    public void setLocationMade(String location) {
        this.made_in = location;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPricePaid(double price) {
        this.price_paid = price;
    }

    public void setPriceSold(double price) {
        this.price_sold = price;
        this.sold_price_set = true;
    }

    public void setSoldPriceBeenSetBefore(boolean sold) {
        this.sold_price_set = sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

}
