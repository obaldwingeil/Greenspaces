package com.example.greenspaces;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class SearchFragment extends Fragment {

    private ToggleButton toggleButton_map;
    private SearchView searchView;
    private TextView textView_filter;
    private TextView textView_sort;
    private View view_divider;

    private View popupView;
    private PopupWindow popupWindow;

    private ImageView imageView_exitFilter;
    private TextView textView_clearFilters;
    private Chip chip_saved;
    private FlexboxLayout flexboxLayout_features;
    private FlexboxLayout flexboxLayout_sports;
    private RatingBar ratingBar_filter;
    private LinearLayout linearLayout_type;
    private TextView textView_applyFilters;

    private ArrayList<String> filterList;
    private ArrayList<String> typeList;
    private ArrayList<String> features;
    private ArrayList<String> sports;
    private float rating;
    private Boolean only_saved;

    private ArrayList<Location> locationArrayList;
    private ArrayList<String> location_ids;

    private String sortBy;
    private Double latitude;
    private Double longitude;

    private MapFragment currentFragment;

    private String searchQuery;

    int PERMISSION_ID = 44;

    FusedLocationProviderClient mFusedLocationClient;

    View view;
    Context context;

    private String api_root;
    private static AsyncHttpClient client = new AsyncHttpClient();

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String user_id;

    public SearchFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        context = view.getContext();

        api_root = getString(R.string.api_root);

        sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString("user_id", null);
        editor = sharedPreferences.edit();

        textView_filter = view.findViewById(R.id.textView_filter);
        textView_sort = view.findViewById(R.id.textView_sort);
        searchView = view.findViewById(R.id.searchView);
        toggleButton_map = view.findViewById(R.id.toggleButton_map);
        view_divider = view.findViewById(R.id.view_divider);

        searchQuery = "";

        searchView.setOnSearchClickListener(v -> {
            if(currentFragment != null){
                currentFragment.closePopup();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                applyFilters();
                searchView.clearFocus();
                if(currentFragment != null){
                    currentFragment.closePopup();
                }
                Log.d("search query", "query text listener");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(currentFragment != null){
                    currentFragment.closePopup();
                }
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchQuery = "";
                applyFilters();
                searchView.clearFocus();
                return false;
            }
        });

        textView_filter.setOnClickListener(v -> openFilterMenu());
        textView_sort.setOnClickListener(v -> openSortMenu());

        filterList = new ArrayList<>();
        typeList = new ArrayList<>();
        rating = 0;
        only_saved = false;

        locationArrayList = new ArrayList<>();
        location_ids = new ArrayList<>();

        sortBy = "Distance";
        latitude = 0.0;
        longitude = 0.0;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        getLastLocation();

        features = new ArrayList<>(Arrays.asList(
                getString(R.string.uap),
                getString(R.string.hiking_trail),
                getString(R.string.playground),
                getString(R.string.toilets),
                getString(R.string.showers),
                getString(R.string.picnic),
                getString(R.string.restaurant),
                getString(R.string.bike_path),
                getString(R.string.fishing),
                getString(R.string.sailing)
        ));
        sports = new ArrayList<>(Arrays.asList(
                getString(R.string.football),
                getString(R.string.soccer),
                getString(R.string.basketball),
                getString(R.string.softball),
                getString(R.string.baseball),
                getString(R.string.volleyball),
                getString(R.string.tennis)
        ));

        toggleButton_map.setChecked(true);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("location_ids", location_ids);
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(bundle);
        currentFragment = mapFragment;
        loadFragment(mapFragment, R.id.fragmentContainer_search);

        toggleButton_map.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                applyFilters();
                if(isChecked){
                    textView_sort.setVisibility(View.GONE);
                    view_divider.setVisibility(View.GONE);
                } else {
                    textView_sort.setVisibility(View.VISIBLE);
                    view_divider.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    public void loadFragment(Fragment fragment, int id){
        FragmentManager fragmentManager = getParentFragmentManager();
        // create a fragment transaction to begin the transaction and replace the fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //replacing the placeholder - fragmentContainterView with the fragment that is passed as parameter
        fragmentTransaction.replace(id, fragment);
        fragmentTransaction.commit();
    }

    public void openSortMenu(){
        PopupMenu sortMenu = new PopupMenu(context, textView_sort);
        sortMenu.getMenuInflater().inflate(R.menu.menu_sort, sortMenu.getMenu());

        for(int i = 0; i < sortMenu.getMenu().size(); i++){
            MenuItem item = sortMenu.getMenu().getItem(i);
            if(sortBy.contentEquals(item.getTitle())){
                item.setIcon(R.drawable.sort_selected);
            }
        }
        sortMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                sortBy = menuItem.getTitle().toString();
                if(sortBy.equals("Distance")){
                    getLastLocation();
                }
                applyFilters();
                return true;
            }
        });
        sortMenu.show();
    }

    public void openFilterMenu(){
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_filtermenu, null);

        imageView_exitFilter = popupView.findViewById(R.id.imageView_exitFilter);
        textView_clearFilters = popupView.findViewById(R.id.textView_clearFilters);
        chip_saved = popupView.findViewById(R.id.chip_saved);
        flexboxLayout_features = popupView.findViewById(R.id.flexBox_features);
        flexboxLayout_sports = popupView.findViewById(R.id.flexBox_sports);
        ratingBar_filter = popupView.findViewById(R.id.ratingBar_filter);
        linearLayout_type = popupView.findViewById(R.id.linearLayout_type);
        textView_applyFilters = popupView.findViewById(R.id.textView_applyFilters);

        textView_clearFilters.setOnClickListener(v -> {
            filterList = new ArrayList<>();
            typeList = new ArrayList<>();
            rating = 0;
            only_saved = false;
            chip_saved.setChecked(false);

            flexboxLayout_features.removeAllViews();
            flexboxLayout_sports.removeAllViews();
            loadFilters();
            applyFilters();
        });

        loadFilters();
        textView_applyFilters.setOnClickListener(v -> {
            applyFilters();
            popupWindow.dismiss();
        });

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAsDropDown(textView_filter);

        imageView_exitFilter.setOnClickListener(v -> popupWindow.dismiss());
    }

    public void loadFilters(){
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(8,8,8,8);
        lp.setFlexGrow(2);
        for(int i = 0; i < features.size(); i++){
            TextView tag = new TextView(context);
            tag.setLayoutParams(lp);
            tag.setPadding(50, 20, 50, 20);
            if(filterList.contains(features.get(i))){
                tag.setBackgroundColor(context.getColor(R.color.dark_green));
            } else {
                tag.setBackgroundColor(context.getColor(R.color.medium_green));
            }
            tag.setText(features.get(i));
            tag.setOnClickListener(v -> featureToggle(v, tag, true));
            tag.setTextColor(context.getColor(R.color.white));
            tag.setGravity(Gravity.CENTER);
            flexboxLayout_features.addView(tag);
        }

        for(int i = 0; i < sports.size(); i++){
            TextView tag = new TextView(context);
            tag.setLayoutParams(lp);
            tag.setPadding(50, 20, 50, 20);
            if(filterList.contains(sports.get(i))){
                tag.setBackgroundColor(context.getColor(R.color.dark_green));
            } else {
                tag.setBackgroundColor(context.getColor(R.color.light_grey));
            }
            tag.setText(sports.get(i));
            tag.setOnClickListener(v -> featureToggle(v, tag, false));
            tag.setTextColor(context.getColor(R.color.white));
            tag.setGravity(Gravity.CENTER);
            flexboxLayout_sports.addView(tag);
        }

        ratingBar_filter.setRating(rating);
        ratingBar_filter.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float newRating, boolean fromUser) {
                rating = newRating;
            }
        });

        for(int index = 0; index < ((ViewGroup) linearLayout_type).getChildCount(); index++) {
            CheckBox check = (CheckBox)((ViewGroup) linearLayout_type).getChildAt(index);
            if(typeList.contains(check.getText().toString())){
                check.setChecked(true);
            } else {
                check.setChecked(false);
            }
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked && !typeList.contains(buttonView.getText().toString())){
                        typeList.add(buttonView.getText().toString());
                    } else {
                        typeList.remove(buttonView.getText().toString());
                    }
                    Log.d("types", String.valueOf(typeList));
                }
            });
        }

        chip_saved.setChecked(only_saved);
        chip_saved.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                only_saved = isChecked;
            }
        });
    }

    public void featureToggle(View v, TextView tag, Boolean isFeature){
        String text = tag.getText().toString();
        if(filterList.contains(text)){
            filterList.remove(text);
            if (isFeature) {
                tag.setBackgroundColor(context.getColor(R.color.medium_green));
            } else {
                tag.setBackgroundColor(context.getColor(R.color.light_grey));
            }
        } else {
            filterList.add(text);
            v.setBackgroundColor(context.getColor(R.color.dark_green));
        }
        Log.d("filters", String.valueOf(filterList));
    }

    public void applyFilters(){
        String api = api_root + "api/locations/filtered";
        Log.d("api", api);
        JSONObject body = new JSONObject();
        Log.d("filterList", String.valueOf(filterList));
        Log.d("typeList", String.valueOf(typeList));
        ArrayList<Double> point = new ArrayList<>();
        point.add(latitude);
        point.add(longitude);
        try {
            body.put("filterList", filterList);
            body.put("typeList", typeList);
            body.put("minRating", String.valueOf(rating));
            body.put("sortBy", sortBy);
            body.put("point", point);
            Log.d("point", String.valueOf(point));
            body.put("searchQuery", searchQuery);
            body.put("userId", user_id);
            body.put("saved", only_saved);

            StringEntity entity = new StringEntity(body.toString());
            client.get(context, api, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.d("api success", new String(responseBody));
                    try {
                        JSONObject response = new JSONObject(new String(responseBody));
                        JSONArray locations = response.getJSONArray("locations");
                        String saved_locations = response.getString("saved");
                        for(int i = 0; i < locations.length(); i++){
                            JSONObject location = locations.getJSONObject(i);
                            location_ids.add(location.getString("location_id"));
                            Location locationObj = new Location(
                                    location.getString("location_id"),
                                    location.getString("name"),
                                    location.getString("address"),
                                    location.getString("type"),
                                    location.getString("description"),
                                    (float) location.getDouble("rating"),
                                    saved_locations.contains(location.getString("location_id"))
                            );
                            locationArrayList.add(locationObj);
                        }
                        getImages();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("api failure", new String(responseBody));
                    if(!toggleButton_map.isChecked()) {
                        loadFragment(new ListFragment(), R.id.fragmentContainer_search);
                    } else {
                        loadFragment(new MapFragment(), R.id.fragmentContainer_search);
                    }
                    locationArrayList = new ArrayList<>();
                    location_ids = new ArrayList<>();
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void getImages() {
        String api = api_root + "api/images/location_ids";
        Log.d("api", api);
        JSONObject body = new JSONObject();
        try {
            body.put("location_ids", location_ids);
            StringEntity entity = new StringEntity(body.toString());
            client.get(context, api, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.d("api_response", new String(responseBody));
                    try {
                        JSONObject response = new JSONObject(new String(responseBody));
                        JSONArray images = response.getJSONArray("images");
                        for(int i = 0; i < locationArrayList.size(); i++){
                            Location location = locationArrayList.get(i);
                            for(int j = 0; j < images.length(); j++){
                                JSONObject image = images.getJSONObject(j);
                                if(image.getString("location_id").equals(location.getLocation_id())){
                                    Image imageObj = new Image(
                                            image.getString("url"),
                                            image.getString("location_id"),
                                            image.getString("user_id"),
                                            image.getString("review_id")
                                    );
                                    location.addImage(imageObj);
                                }
                            }
                        }
                        if(!toggleButton_map.isChecked()){
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("locationArrayList", (Serializable) locationArrayList);
                            ListFragment listFragment = new ListFragment();
                            listFragment.setArguments(bundle);
                            loadFragment(listFragment, R.id.fragmentContainer_search);
                            currentFragment = null;
                            locationArrayList = new ArrayList<>();
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList("location_ids", location_ids);
                            MapFragment mapFragment = new MapFragment();
                            mapFragment.setArguments(bundle);
                            currentFragment = mapFragment;
                            loadFragment(mapFragment, R.id.fragmentContainer_search);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("api_failure", new String(responseBody));
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {
                        android.location.Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            editor.putFloat("user_latitude", latitude.floatValue());
                            editor.putFloat("user_longitude", longitude.floatValue());
                            editor.apply();
                        }
                    }
                });
            } else {
                Toast.makeText(context, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
            }
        } else {
            // if permissions aren't available, request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            android.location.Location mLastLocation = locationResult.getLastLocation();
            editor.clear();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            editor.putFloat("user_latitude", latitude.floatValue());
            editor.putFloat("user_longitude", longitude.floatValue());
            editor.apply();
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}

