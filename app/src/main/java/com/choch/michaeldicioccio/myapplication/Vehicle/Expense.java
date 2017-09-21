package com.choch.michaeldicioccio.myapplication.Vehicle;

import io.realm.RealmObject;


public class Expense extends RealmObject {

    /* Attributes */
    String title;
    String description;

    double price;

    /* Constructor */
    public Expense() {

    }

    /* Getters */
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    /* Setters */
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
