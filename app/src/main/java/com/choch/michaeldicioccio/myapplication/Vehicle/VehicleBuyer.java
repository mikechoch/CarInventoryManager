package com.choch.michaeldicioccio.myapplication.Vehicle;

import io.realm.RealmObject;

/**
 * Created by michaeldicioccio on 9/24/17.
 */

public class VehicleBuyer extends RealmObject {

    /* Attributes */
    String name;
    String phone_number;
    String email;

    /* Constructor */
    public VehicleBuyer () {

    }

    /* Getters */
    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    public String getEmail() {
        return email;
    }

    /* Setters */
    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
