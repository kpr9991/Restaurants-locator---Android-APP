package com.example.zomato;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class JsonDecoder {
    ArrayList<String> restaurantsList = new ArrayList<>();
    ArrayList<String> restaurantDetails = new ArrayList<>();
    static HashMap<String, Integer> mapNamestoId = new HashMap<>();     //ID's of all restaurants cannot be stored and hence i mapped last recieved restaurant names list to their ID's.
    String TAG = getClass().getName();

    //To get list of all available restaurant names and map them to their ID's for next searchByrestaurant name feature.
    public ArrayList<String> getRestaurantsList(JSONObject jsonObject) {
        Log.d(TAG, "getRestaurantsList method Called");
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("nearby_restaurants");
            ;
            for (int i = 0; i < jsonArray.length(); i++) {
                String restaurantName = jsonArray.getJSONObject(i).getJSONObject("restaurant").getString("name");
                Integer restaurantId = jsonArray.getJSONObject(i).getJSONObject("restaurant").getInt("id");
                mapNamestoId.put(restaurantName.toLowerCase(), restaurantId);
                restaurantsList.add(restaurantName);
                Log.d(TAG, "Restaurant Name" + restaurantName);
                Log.d(TAG, "Restaurant ID " + restaurantId);
            }
        } catch (Exception e) {
            Log.d(TAG, "JSON Object decoding Failed");
        }
        return restaurantsList;
    }

    //To get details about the searched restaurant.
    public ArrayList<String> getRestaurantDetails(JSONObject jsonObject) {
        restaurantDetails.clear();
        try {
            restaurantDetails.add("Restaurant Name -> " + jsonObject.getString("name"));
            restaurantDetails.add("Cuisines Available -> " + jsonObject.getString("cuisines"));
            restaurantDetails.add("Operational timing -> " + jsonObject.getString("timings"));
            restaurantDetails.add("Locality -> " + jsonObject.getJSONObject("location").getString("locality_verbose"));
            restaurantDetails.add("Latitude of Location  -> " + jsonObject.getJSONObject("location").getString("latitude"));
            restaurantDetails.add("Longitude of Location -> " + jsonObject.getJSONObject("location").getString("longitude"));
        } catch (Exception e) {
            Log.d(TAG, "Decoding restaurant JSON object failed");
        }
        return restaurantDetails;
    }

}
