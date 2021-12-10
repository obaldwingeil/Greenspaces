package com.example.greenspaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class LocationActivity extends AppCompatActivity {

    private ScrollView scrollView_location;
    private TextView textView_name;
    private ImageView imageView_locationSaved;
    private TextView textView_type;
    private RatingBar ratingBar_location;
    private RecyclerView recyclerView_images;
    private Button button_directions;
    private Button button_link;
    private TextView textView_description;
    private TextView textView_details;
    private TextView textView_seasonal;
    private TextView textView_hours;
    private FlexboxLayout flexboxLayout_tags;
    private TableLayout tableLayout_cost;
    private TextView textView_weather;
    private TextView textView_contact;
    private TextView textView_phone;
    private TextView textView_email;
    private Button button_reviews;
    private Button button_photos;

    protected String locationID;
    private String url;
    protected String locationName;

    private String api_root;
    private static AsyncHttpClient client = new AsyncHttpClient();

    protected Double rating;
    private ArrayList<Image> images;
    private ArrayList<String> activityList;
    private ArrayList<String> featureList;

    private SharedPreferences sharedPreferences;
    private boolean saved;
    private String user_id;

    private Double latitude;
    private Double longitude;
    private Float user_lat;
    private Float user_long;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        scrollView_location = findViewById(R.id.scrollView_location);
        textView_name = findViewById(R.id.textView_locationName);
        imageView_locationSaved = findViewById(R.id.imageView_locationSaved);
        textView_type = findViewById(R.id.textView_type);
        ratingBar_location = findViewById(R.id.ratingBar_location);
        recyclerView_images = findViewById(R.id.recyclerView_images);
        button_directions = findViewById(R.id.button_directions);
        button_link = findViewById(R.id.button_link);
        textView_description = findViewById(R.id.textView_description);
        textView_details = findViewById(R.id.textView_details);
        textView_seasonal = findViewById(R.id.textView_seasonal);
        textView_hours = findViewById(R.id.textView_hours);
        flexboxLayout_tags = findViewById(R.id.flexBox_location);
        tableLayout_cost = findViewById(R.id.tableLayout_cost);
        textView_weather = findViewById(R.id.textView_weather);
        textView_contact = findViewById(R.id.textView_contact);
        textView_phone = findViewById(R.id.textView_phone);
        textView_email = findViewById(R.id.textView_email);
        button_reviews = findViewById(R.id.button_userReviews);
        button_photos = findViewById(R.id.button_userPhotos);

        images = new ArrayList<>();
        activityList = new ArrayList<>();
        featureList = new ArrayList<>();

        sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString("user_id", null);
        user_lat = sharedPreferences.getFloat("user_latitude", 0.9f);
        user_long = sharedPreferences.getFloat("user_longitude", 0.9f);

        saved = false;

        api_root = getString(R.string.api_root);

        Intent intent = getIntent();
        locationID = intent.getStringExtra("location_id");
        getLocation();

        button_reviews.setOnClickListener(v -> {
            loadFragment(new ReviewsFragment(), R.id.fragContainer_location);
        });

        button_photos.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("id", locationID);
            bundle.putString("parent", "location");
            PhotosFragment photosFragment = new PhotosFragment();
            photosFragment.setArguments(bundle);
            loadFragment(photosFragment, R.id.fragContainer_location);
        });

        button_directions.setOnClickListener(v -> getDirections());
        button_link.setOnClickListener(v -> openWebsite());

        imageView_locationSaved.setOnClickListener(v -> addLocation());
        checkSavedLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        images = new ArrayList<>();
        getLocation();
    }

    public void getLocation(){
        String api = api_root + "api/locations/" + locationID;
        Log.d("api", api);
        client.get(api, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("api", new String(responseBody));
                getImages();
                button_reviews.callOnClick();
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    JSONObject location = response.getJSONArray("location").getJSONObject(0);
                    locationName = location.getString("name");
                    textView_name.setText(locationName);
                    textView_type.setText(location.getString("type"));

                    if(location.isNull("rating")){
                        ratingBar_location.setRating(0);
                        rating = 0.0;
                    } else {
                        ratingBar_location.setRating((float)location.getDouble("rating"));
                        rating = location.getDouble("rating");
                    }
                    url = location.getString("link");
                    if(!location.isNull("description")){
                        textView_description.setText(location.getString("description"));
                        textView_description.setVisibility(View.VISIBLE);
                    }
                    if(!location.isNull("details")){
                        String text = location.getString("details").replace(", ", "\n");
                        textView_details.setText(text.substring(0, text.length()-1));
                        textView_details.setVisibility(View.VISIBLE);
                    }
                    if(!location.isNull("seasonal")){
                        textView_seasonal.setText(location.getString("seasonal"));
                        textView_seasonal.setVisibility(View.VISIBLE);
                    }
                    if(!location.isNull("hours")){
                        String hours = location.getString("hours").toLowerCase();
                        if(hours.contains("monday:")){
                            Map<String, String> hours_map = new HashMap<>();
                            String[] pairs = hours.split(", ");
                            for (String pair : pairs) {
                                String[] keyValue = pair.split(": ");
                                hours_map.put(keyValue[0], keyValue[1]);
                            }
                            textView_hours.setTypeface(Typeface.MONOSPACE);
                            hours = String.format("%-20s%s%n", "Sunday: ", hours_map.get("sunday"))
                                    + String.format("%-20s%s%n", "Monday:", hours_map.get("monday"))
                                    + String.format("%-20s%s%n", "Tuesday:", hours_map.get("tuesday"))
                                    + String.format("%-20s%s%n", "Wednesday:", hours_map.get("wednesday"))
                                    + String.format("%-20s%s%n", "Thursday:", hours_map.get("thursday"))
                                    + String.format("%-20s%s%n", "Friday:", hours_map.get("friday"))
                                    + String.format("%-20s%s", "Saturday:", hours_map.get("saturday"));
                        }
                        textView_hours.setText(hours);
                        textView_hours.setVisibility(View.VISIBLE);
                    }

                    if(!location.isNull("activities")){
                        String activity_str = location.getString("activities");
                        ArrayList<String> activities = new ArrayList<String>(Arrays.asList(activity_str.split(", ")));
                        if(activityList.size() == 0){
                            TextView label = new TextView(LocationActivity.this);
                            label.setText(getString(R.string.activities));
                            label.setTextSize(18);
                            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(8,8,8,8);
                            lp.setFlexGrow(2);
                            label.setLayoutParams(lp);
                            flexboxLayout_tags.addView(label);
                            for(int i = 0; i < activities.size(); i++){
                                activityList.add(activities.get(i));
                                TextView tag = new TextView(LocationActivity.this);
                                tag.setLayoutParams(lp);
                                tag.setPadding(50, 20, 50, 20);
                                tag.setBackgroundColor(getColor(R.color.medium_green));
                                tag.setText(activities.get(i));
                                tag.setTextSize(16);
                                tag.setTextColor(getColor(R.color.white));
                                tag.setGravity(Gravity.CENTER);
                                flexboxLayout_tags.addView(tag);
                            }
                        }
                    }

                    if(!location.isNull("features")){
                        String feature_str = location.getString("features");
                        ArrayList<String> features = new ArrayList<String>(Arrays.asList(feature_str.split(", ")));
                        if(featureList.size() == 0){
                            TextView label = new TextView(LocationActivity.this);
                            label.setText(getString(R.string.features));
                            label.setTextSize(18);
                            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(8,8,8,8);
                            lp.setFlexGrow(2);
                            label.setLayoutParams(lp);
                            flexboxLayout_tags.addView(label);
                            for(int i = 0; i < features.size(); i++){
                                featureList.add(features.get(i));
                                TextView tag = new TextView(LocationActivity.this);
                                tag.setLayoutParams(lp);
                                tag.setPadding(50, 20, 50, 20);
                                tag.setBackgroundColor(getColor(R.color.light_green));
                                tag.setText(features.get(i));
                                tag.setTextSize(16);
                                tag.setTextColor(getColor(R.color.white));
                                tag.setGravity(Gravity.CENTER);
                                flexboxLayout_tags.addView(tag);
                            }
                        }
                    }

                    if(!location.isNull("cost") && tableLayout_cost.getChildCount() == 0){
                        Log.d("cost", location.getString("cost"));
                        JSONArray cost_array = new JSONArray(location.getString("cost"));
                        JSONArray costs = new JSONArray();
                        if(location.getString("type").equals("national park")){
                            costs = cost_array.getJSONArray(0);
                        } else {
                            costs = cost_array;
                        }
                        JSONObject sample = costs.getJSONObject(0);
                        TableRow title = new TableRow(LocationActivity.this);
                        Iterator<String> iter = sample.keys();
                        while(iter.hasNext()){
                            TextView label = new TextView(LocationActivity.this);
                            label.setText(iter.next());
                            label.setPadding(16, 6, 16, 6);
                            label.setTextSize(18);
                            label.setBackgroundColor(getColor(R.color.faded_dark_green));
                            title.addView(label);
                        }
                        tableLayout_cost.addView(title);
                        for(int i = 0; i < costs.length(); i++){
                            TableRow row = new TableRow(LocationActivity.this);
                            JSONObject cost = costs.getJSONObject(i);
                            Iterator<String> column_iter = cost.keys();
                            while(column_iter.hasNext()){
                                TextView column = new TextView(LocationActivity.this);
                                String key = column_iter.next();
                                String text = cost.getString(key);
                                if(key.equals("cost") && !text.contains("$")){
                                    text = "$" + text;
                                }
                                column.setText(text);
                                column.setPadding(8, 2, 8, 2);
                                column.setTextSize(16);
                                column.setMaxWidth(900);
                                row.addView(column);
                            }
                            tableLayout_cost.setStretchAllColumns(true);
                            tableLayout_cost.addView(row);
                            View view = new View(LocationActivity.this);
                            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4);
                            view.setBackgroundColor(getColor(R.color.faded_dark_green));
                            view.setLayoutParams(params);
                            tableLayout_cost.addView(view);
                        }
                    }

                    if(!location.isNull("weather")){
                        textView_weather.setText("Weather: " + location.getString("weather"));
                        textView_weather.setVisibility(View.VISIBLE);
                    }

                    if(!location.isNull("phone")){
                        textView_contact.setVisibility(View.VISIBLE);
                        String raw_phone = location.getString("phone");
                        if(raw_phone.contains("(")) {
                            textView_phone.setText("Phone: " + raw_phone);
                        } else {
                            String number = raw_phone.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3");
                            textView_phone.setText("Phone: " + number);
                        }
                        textView_phone.setVisibility(View.VISIBLE);
                    }
                    if(!location.isNull("email")){
                        textView_contact.setVisibility(View.VISIBLE);
                        textView_email.setText("Email: " + location.getString("email"));
                        textView_email.setVisibility(View.VISIBLE);
                    }

                    latitude = location.getDouble("lat");
                    longitude = location.getDouble("long");

                    button_reviews.callOnClick();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("api", new String(responseBody));
            }
        });
    }

    public void getImages(){
        String api = api_root + "api/images/" + locationID;
        client.get(api, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("images api", new String(responseBody));
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    JSONArray res = response.getJSONArray("images");
                    for(int i = 0; i < res.length(); i++){
                        JSONObject image_obj = res.getJSONObject(i);
                        Image image = new Image(
                                image_obj.getString("url"),
                                String.valueOf(image_obj.getInt("location_id")),
                                image_obj.getString("user_id"),
                                image_obj.getString("review_id")
                        );
                        images.add(image);
                    }
                    ImageAdapter adapter = new ImageAdapter(images);
                    recyclerView_images.setAdapter(adapter);
                    LinearLayoutManager layoutManager =  new LinearLayoutManager( LocationActivity.this, LinearLayoutManager.HORIZONTAL, false);
                    recyclerView_images.setLayoutManager(layoutManager);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void loadFragment(Fragment fragment, int id){
        FragmentManager fragmentManager = getSupportFragmentManager();
        // create a fragment transaction to begin the transaction and replace the fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //replacing the placeholder - fragmentContainterView with the fragment that is passed as parameter
        fragmentTransaction.replace(id, fragment);
        fragmentTransaction.commit();
    }

    public void checkSavedLocation(){
        String api = api_root + "api/user/" + user_id;
        client.get(api, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("user api", new String(responseBody));
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    JSONObject user = response.getJSONArray("user").getJSONObject(0);
                    if(!user.isNull("locations")){
                        String locations = user.getString("locations");
                        if(locations.contains(locationID)){
                            Picasso.get().load("file:///android_asset/saved.png").into(imageView_locationSaved);
                        } else {
                            Picasso.get().load("file:///android_asset/not_saved.png").into(imageView_locationSaved);
                        }
                    } else {
                        Picasso.get().load("file:///android_asset/not_saved.png").into(imageView_locationSaved);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void openWebsite(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
        else{
            Log.e("intent", "cannot handle intent");
        }
    }

    public void getDirections(){
        Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + user_lat + "," + user_long + "&destination=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Log.e("map intent", "cannot handle intent");
        }
    }

    public void addLocation(){
        if(user_id != null){
            saved = !saved;
            if(saved){
                Picasso.get().load("file:///android_asset/saved.png").into(imageView_locationSaved);
            }
            else{
                Picasso.get().load("file:///android_asset/not_saved.png").into(imageView_locationSaved);
            }

            String api = api_root + "api/user/add/" +user_id;
            JSONObject body = new JSONObject();
            try {
                body.put("locationId", locationID);
                body.put("saved", saved);
                StringEntity entity = new StringEntity(body.toString());
                client.post(this, api, entity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d("location_added", new String(responseBody));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
