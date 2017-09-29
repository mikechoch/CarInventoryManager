package com.choch.michaeldicioccio.myapplication.Json;

import com.choch.michaeldicioccio.myapplication.Vehicle.Vehicle;
import com.choch.michaeldicioccio.myapplication.Vehicle.VehicleBuyer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * Created by michaeldicioccio on 9/9/17.
 */

public class VehicleDataJson {

    private String json_url;
    private String price_paid;
    private Date date_bought;

    public VehicleDataJson(String json_url, String price_paid, Date date_bought) {
        this.json_url = json_url;
        this.price_paid = price_paid;
        this.date_bought = date_bought;
    }

    public Vehicle decodeVinNumber() throws IOException, JSONException {
        return parseJson(getJsonFromUrl(json_url));
    }

    private JSONObject getJsonFromUrl(String json_url) throws IOException, JSONException {
        InputStream is = new URL(json_url).openStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) { sb.append((char) cp); }
        return new JSONObject(sb.toString());
    }

    private Vehicle parseJson(JSONObject jsonObject) throws JSONException {
        Vehicle vehicle = new Vehicle();

        if (jsonObject.getBoolean(JsonAttributes.SUCCESS.getString())) {

            JSONObject jsonVehicleAttributes = jsonObject.getJSONObject(JsonAttributes.ATTRIBUTES.getString());

            vehicle.setVin(jsonVehicleAttributes.getString(VehicleJsonAttributes.VIN.getString()));
            vehicle.setYear(jsonVehicleAttributes.getString(VehicleJsonAttributes.YEAR.getString()));
            vehicle.setMake(jsonVehicleAttributes.getString(VehicleJsonAttributes.MAKE.getString()));
            vehicle.setModel(jsonVehicleAttributes.getString(VehicleJsonAttributes.MODEL.getString()));
            vehicle.setTrim(jsonVehicleAttributes.getString(VehicleJsonAttributes.TRIM.getString()));
            vehicle.setLocationMade(jsonVehicleAttributes.getString(VehicleJsonAttributes.MADE_IN.getString()));
            vehicle.setStyle(jsonVehicleAttributes.getString(VehicleJsonAttributes.STYLE.getString()));
            vehicle.setEngine(jsonVehicleAttributes.getString(VehicleJsonAttributes.ENGINE.getString()));

            vehicle.setVehicleBuyer(new VehicleBuyer());

            vehicle.setPricePaid(Double.parseDouble(price_paid));
            vehicle.setBuyDate(date_bought);

            vehicle.setSold(false);

            return vehicle;
        }

        return null;
    }

}
