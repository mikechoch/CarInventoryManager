package com.choch.michaeldicioccio.myapplication;

/**
 * Created by michaeldicioccio on 9/12/17.
 */

public enum MessageStrings {

    INVALID_VIN_LENGTH("Vin numbers are 17 chars long"),
    INVALID_VIN_BARCODE_FORMAT("Invalid barcode type"),
    VIN_ALREADY_SCANNED("Vin number already stored"),
    INVALID_VIN("Vin number invalid");

    final private String message;

    MessageStrings(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
