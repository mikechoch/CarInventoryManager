package com.choch.michaeldicioccio.myapplication.Json;

import com.choch.michaeldicioccio.myapplication.MessageStrings;

import me.dm7.barcodescanner.zbar.BarcodeFormat;


public class VinErrorCheck {

    /* Attributes */
    private String vin;
    private String errorMessage;

    /* Constructor */
    public VinErrorCheck(String vin) {
        System.out.println(vin);
        this.vin = vin;
    }

    /* Vin Verification */
    public boolean verifyValidVin(String barcodeFormat) {
        System.out.println(barcodeFormat);
        if (verifyBarcodeFormat(barcodeFormat) && isVinLengthValid(vin)) {
            return true;
        }

        return false;
    }

    /**
     * verifies the barcode format scanned with valid vin number barcode formats CODE: 39, 128, 93
     * @param barcodeFormat - String representing the barcode format scanned
     * @return - boolean representing a valid barcode
     */
    public boolean verifyBarcodeFormat(String barcodeFormat) {
        if (barcodeFormat.toLowerCase().equals(BarcodeFormat.CODE39.getName().toLowerCase())
                || barcodeFormat.toLowerCase().equals(BarcodeFormat.CODE128.getName().toLowerCase())
                || barcodeFormat.toLowerCase().equals(BarcodeFormat.CODE93.getName().toLowerCase())) {
            return true;
        }
        this.errorMessage = MessageStrings.INVALID_VIN_BARCODE_FORMAT.getMessage();

        return false;
    }


    /**
     * Checks on the 18 length vin and removes the first char since some have an "I"
     * Checks on the 17 length vin
     */
    public boolean isVinLengthValid(String vin) {
        int vin_n = vin.length();
        if (vin_n == 18 && vin.substring(0, 1).equals("I")) {
            this.vin = vin.substring(1);
            return true;
        } else if (vin_n == 17) {
            return true;
        }
        this.errorMessage = MessageStrings.INVALID_VIN_LENGTH.getMessage();
        return false;
    }

    /* Getters */
    public String getErrorMessage() {
        return errorMessage;
    }

    public String getVin() {
        return vin;
    }

}
