package com.choch.michaeldicioccio.myapplication;


public enum MessageStrings {

    INVALID_VIN_LENGTH("Invalid barcode"),
    INVALID_VIN_BARCODE_FORMAT("Invalid barcode"),
    VIN_ALREADY_SCANNED("Vehicle already exists"),
    INVALID_VIN("Vehicle vin number invalid");

    final private String message;

    MessageStrings(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
