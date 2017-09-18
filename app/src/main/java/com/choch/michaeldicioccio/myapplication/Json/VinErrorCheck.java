package com.choch.michaeldicioccio.myapplication.Json;

import com.choch.michaeldicioccio.myapplication.MessageStrings;

import me.dm7.barcodescanner.zbar.BarcodeFormat;


public class VinErrorCheck {

    private String vin;
    private String errorMessage;

    public VinErrorCheck(String vin) {
        System.out.println(vin);
        this.vin = vin;
    }

    public boolean verifyValidVin(String barcodeFormat) {
        System.out.println(barcodeFormat);
        if (verifyBarcodeFormat(barcodeFormat) && verifyVinLength(vin)) {
            return true;
        }

        return false;
    }

    public boolean verifyBarcodeFormat(String barcodeFormat) {
        if (barcodeFormat.toLowerCase().equals(BarcodeFormat.CODE39.getName().toLowerCase())
                || barcodeFormat.toLowerCase().equals(BarcodeFormat.CODE128.getName().toLowerCase())
                || barcodeFormat.toLowerCase().equals(BarcodeFormat.CODE93.getName().toLowerCase())) {
            return true;
        }
        this.errorMessage = MessageStrings.INVALID_VIN_BARCODE_FORMAT.getMessage();

        return false;
    }

    public boolean verifyVinLength(String vin) {
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getVin() {
        return vin;
    }

}
