package com.example.zomato;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final String TAG = getClass().getName();
    private Button getLocationButton;
    private Button getRestaurantsButton;
    private ListView listView;
    Button searchByNameButton;
    private LocationManager locationManager;
    private LocationListener locationListener;
    final long MIN_TIME = 5000;
    final float MIN_DIST = 1000;
    final int REQUEST_CODE = 123;
    private String LOCATION_PROVIDER;
    private String latitude;
    private String longitude;
    ProgressBar progressBar;
    ArrayAdapter<String> restaurantsListAdapter;
    static final String API_KEY = "1b3c8b37ea96785391fa55c288ac385c";
    static String ZOMATO_URL = "https://developers.zomato.com/api/v2.1/geocode?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLocationButton = findViewById(R.id.getLocation);
        getLocationButton.setOnClickListener(v -> {
            Log.d(TAG, "Get location button pressed");
            getLocation();
        });
        getRestaurantsButton = findViewById(R.id.getRestaurants);
        getRestaurantsButton.setOnClickListener(v -> {
            Log.d(TAG, "Get restaurants button pressed");
            getRestaurantsInCurrentLocation();
        });
        listView = findViewById(R.id.restaurantsListView);
        searchByNameButton = findViewById(R.id.searchByName);
        searchByNameButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, searchByName.class);
            startActivity(intent);
        });
        progressBar = findViewById(R.id.progressBarID);
    }

    //When getLocations button is pressed.
    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            Log.d(TAG, "Requesting permissions:");
            return;
        }
        Log.d(TAG, "Location Permissions granted");
        Toast.makeText(getApplicationContext(), "Location permission granted", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.VISIBLE);
        locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
    }

    //When "get restaurants here" button is pressed.
    public void getRestaurantsInCurrentLocation() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Get restaurants in current Location is called");
        RequestParams requestParams = new RequestParams();
        requestParams.put("lat", latitude);
        requestParams.put("lon", longitude);
        doSomeNetworking(requestParams);
    }

    //Making request to Zomato API for fetching restaurants in USER's location.
    public void doSomeNetworking(RequestParams requestParams) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("user-key", API_KEY);
        client.get(ZOMATO_URL, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "HTTP REQUEST SUCCEEDED");
                ArrayList<String> restaurantsList = new JsonDecoder().getRestaurantsList(response);
                doSomeListing(restaurantsList);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "HTTP REQUEST FAILED");
                Toast.makeText(getApplicationContext(), "Http request failed", Toast.LENGTH_SHORT).show();
                Log.d(TAG, errorResponse.toString());
            }
        });
    }

    //Sending list of restaurants to display in listView.
    public void doSomeListing(ArrayList<String> restaurantsList) {
        if (!restaurantsList.isEmpty()) {
            progressBar.setVisibility(View.INVISIBLE);
            restaurantsListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, restaurantsList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view.findViewById(android.R.id.text1);
                    textView.setTextColor(Color.WHITE);
                    return view;
                }
            };
            listView.setAdapter(restaurantsListAdapter);
        } else {
            //If there are no restaurants in location then it uses default latitude and longitude.
            Log.d(TAG, "Restaurants List is empty");
            Toast.makeText(getApplicationContext(), "NO RESTAURANTS FOUND, SO USING DEFAULT LOCATION", Toast.LENGTH_SHORT).show();
            latitude = "10.95";
            longitude = "79.38";
            getRestaurantsInCurrentLocation();
        }

    }

    //Once the user Allows or Rejects the permission.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions granted");
                getLocation();
            } else {
                Toast.makeText(getApplicationContext(), "PLEASE ALLOW LOCATION", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "User denied permission to location");
            }
        }
    }

    //Location listener implementation in onResume.
    @Override
    protected void onResume() {
        super.onResume();
        //Code to get Latitude and Longitude.
        LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                Log.d(TAG, "Current latitude :" + latitude);
                Log.d(TAG, "Current longitude :" + longitude);
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "RECEIVED LOCATION DETAILS", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.d(TAG, "Location provider disabled.");
                Toast.makeText(getApplicationContext(), "PLEASE TURN ON LOCATION", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.d(TAG, "Location provider enabled.");
                getLocation();
            }
        };
        Log.e(TAG, "OnResume() method called now:");
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressBar.setVisibility(View.INVISIBLE);
    }
}