package com.example.zomato;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class searchByName extends AppCompatActivity {
    ProgressBar progressBar;
    EditText searchByNameEditText;
    Button searchButton;
    String restaurantName;
    Integer id;
    String TAG = getClass().getName();
    String ZOMATO_URL = "https://developers.zomato.com/api/v2.1/restaurant?";
    ListView restaurantsListView;
    ArrayAdapter<String> restaurantDetailsAdapter;
    ArrayList<String> restaurantDetails = new ArrayList<>();
    Activity activity=getParent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_name);
        searchByNameEditText = findViewById(R.id.searchByNameEditText);
        searchButton = findViewById(R.id.searchbutton);
        searchButton.setOnClickListener(v -> {
            restaurantName = searchByNameEditText.getText().toString().toLowerCase();
            Log.d(TAG, "Restaurant NAME IS " + restaurantName);
            progressBar.setVisibility(View.VISIBLE);
            id = JsonDecoder.mapNamestoId.get(restaurantName);
            makeApiRequestWithRestaurantId(id);
        });
        restaurantsListView = findViewById(R.id.restaurantDetailsId);
        progressBar = findViewById(R.id.progressBarSearchByName);
    }

    //On search button's click,since the json decoder has already decoded id's for restaurants,user searched restaurantname
    //is searched with ID.
    public void makeApiRequestWithRestaurantId(Integer id) {
        progressBar.setVisibility(View.VISIBLE);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        client.addHeader("user-key", MainActivity.API_KEY);
        requestParams.put("res_id", id);
        Log.d(TAG, "Id of the requested restaurant " + id);
        client.get(ZOMATO_URL, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "HTTP REQUEST FOR SEARCHING BY RESTAURANT NAME SUCCEEDED");
                restaurantDetails = new JsonDecoder().getRestaurantDetails(response);
                Log.d(TAG, response.toString());
                dosomeListing(restaurantDetails);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "HTTP REQUEST  FOR SEARCHING BY RESTAURANT NAME FAILED");
                restaurantDetails.clear();
                restaurantDetails.add("NO SUCH RESTAURANT EXISTS.");
                restaurantDetails.add("MAKE SURE YOU ENTER ONE FROM THE PREVIOUS LIST.");
                restaurantDetails.add("WITH EXACT SPACES NEEDED.");
                Log.d(TAG, errorResponse.toString());
                dosomeListing(restaurantDetails);
            }

        });
    }

    //To send list of restaurant details to the listView to display. Here, getView method is overridden to change the textcolor in ListView.
    public void dosomeListing(ArrayList<String> restaurantDetails) {
        if (restaurantDetails.isEmpty())
            Log.d(TAG, "Restaurant details is empty");
        restaurantDetailsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, restaurantDetails) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        };
        restaurantsListView.setAdapter(restaurantDetailsAdapter);
        progressBar.setVisibility(View.INVISIBLE);
    }
}