package com.choch.michaeldicioccio.myapplication.Vehicle;

import io.realm.RealmObject;


public class Expense extends RealmObject {

    /* Attributes */
    private String title;
    private String description;

    private double price;

    /* Constructor */
    public Expense() {

    }

    public Expense(Expense expense) {
        this.title = expense.getTitle();
        this.price = expense.getPrice();
        this.description = expense.getDescription();
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

    /* Overrides */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Expense && title.equals(((Expense) obj).getTitle());

    }
}
