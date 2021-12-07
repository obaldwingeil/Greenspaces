package com.example.greenspaces;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonElement;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


public class MapFragment extends Fragment {

    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mp;
    private ValueAnimator markerAnimator;

    View view;
    Context context;

    private ArrayList<String> location_ids;

    private String api_root;
    private static AsyncHttpClient client = new AsyncHttpClient();

    private SharedPreferences sharedPreferences;
    private String user_id;

    private ImageView imageView_saved;
    private Boolean saved;
    private RatingBar ratingBar;

    private ImageView imageView_collapse;
    private LinearLayout linearLayout_locationItem;
    private Boolean open;

    private Boolean markerSelected = false;

    private PopupWindow popupWindow;
    private View popupView;

    FusedLocationProviderClient mFusedLocationClient;

    Double latitude;
    Double longitude;

    int PERMISSION_ID = 44;

    public MapFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token));
        view = inflater.inflate(R.layout.fragment_map, container, false);
        context = view.getContext();
        api_root = getString(R.string.api_root);

        sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString("user_id", null);

        latitude = 0.0;
        longitude = 0.0;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (getArguments() != null && getArguments().getStringArrayList("location_ids") != null) {
            location_ids = getArguments().getStringArrayList("location_ids");
            getArguments().clear();
        } else {
            location_ids = null;
            showNoResultsWindow();
        }

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mp = mapboxMap;

                mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/obaldwingeil/ckvb9vhch8pg615s210bol8g0"), new Style.OnStyleLoaded() {
                    // styles: https://docs.mapbox.com/api/maps/styles/#mapbox-styles
                    // custom: mapbox://styles/obaldwingeil/ckvb9vhch8pg615s210bol8g0
                    // outdoors: mapbox://styles/mapbox/outdoors-v11
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        UiSettings uiSettings = mapboxMap.getUiSettings();
                        SymbolLayer layer = (SymbolLayer) style.getLayer("greenspaces");
                        assert layer != null;
                        if(location_ids != null && location_ids.size() != 0){
                            Log.d("map filter", "did something");
                            layer.setFilter(Expression.in(Expression.get("location_id"), Expression.literal(String.valueOf(location_ids))));
                        } else {
                            layer.setFilter(Expression.neq(Expression.literal(""), ""));
                        }

                        Expression iconDecider = Expression.switchCase(
                                Expression.eq(Expression.get("type"), Expression.literal("golf course")), Expression.literal("golf-selected"),
                                Expression.eq(Expression.get("type"), Expression.literal("pool")), Expression.literal("pool-selected"),
                                Expression.eq(Expression.get("type"), Expression.literal("open water facility")), Expression.literal("water-selected"),
                                Expression.eq(Expression.get("type"), Expression.literal("beach")), Expression.literal("beach-selected"),
                                Expression.eq(Expression.get("type"), Expression.literal("campground")), Expression.literal("campsite-selected"),
                                Expression.eq(Expression.get("type"), Expression.literal("dog park")), Expression.literal("dog-park-selected"),
                                Expression.eq(Expression.get("type"), Expression.literal("garden")), Expression.literal("garden-selected"),
                                Expression.eq(Expression.get("type"), Expression.literal("national park")), Expression.literal("napark-selected"),
                                Expression.literal("park-selected")
                        );

                        style.addSource(new GeoJsonSource("selected-marker"));
                        style.addLayer(new SymbolLayer("selected-marker-layer", "selected-marker")
                                .withProperties(
                                        PropertyFactory.iconHaloWidth(5f),
                                        PropertyFactory.iconHaloColor("#FFFFFF"),
                                        PropertyFactory.iconHaloBlur(2f),
                                        PropertyFactory.iconImage(iconDecider),
                                        PropertyFactory.iconAllowOverlap(true))
                        );

                        mp.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                            @Override
                            public boolean onMapClick(@NonNull LatLng point) {
                                final SymbolLayer selectedMarkerSymbolLayer = (SymbolLayer) style.getLayer("selected-marker-layer");selectedMarkerSymbolLayer.setSourceLayer("selected-marker");
                                final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);

                                List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "greenspaces");
                                List<Feature> selectedFeature = mapboxMap.queryRenderedFeatures(
                                        pixel, "selected-marker-layer");

                                if (selectedFeature.size() > 0 && markerSelected) {
                                    return false;
                                }

                                if (features.isEmpty()) {
                                    if (markerSelected) {
                                        deselectMarker(selectedMarkerSymbolLayer);
                                    }
                                    return false;
                                }

                                GeoJsonSource source = style.getSourceAs("selected-marker");
                                if (source != null) {
                                    Feature newFeature = Feature.fromGeometry(features.get(0).geometry(), features.get(0).properties());
                                    source.setGeoJson(FeatureCollection.fromFeatures(
                                            new Feature[]{newFeature}));
                                }

                                if (markerSelected) {
                                    deselectMarker(selectedMarkerSymbolLayer);
                                }
                                if (features.size() > 0) {
                                    pulseIcon(selectedMarkerSymbolLayer);
                                    Feature feature = features.get(0);

                                    // Ensure the feature has properties defined
                                    if (feature.properties() != null) {
                                        Point point1 = (Point) feature.geometry();
                                        LatLng latlng = new LatLng(point1.coordinates().get(1), point1.coordinates().get(0));
                                        CameraPosition position = new CameraPosition.Builder()
                                                .target(latlng)
                                                .zoom(12)
                                                .build();
                                        mp.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);
                                        showPopupWindow(feature);
                                    }
                                }
                                return true;
                            }
                        });
                    }
                });
            }
        });

        return view;
    }

    public void closePopup(){
        if(popupWindow != null){
            popupWindow.dismiss();
        }
    }

    private void pulseIcon(final SymbolLayer iconLayer) {
        mp.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                markerAnimator = new ValueAnimator();
                markerAnimator.setFloatValues(1f, 2f);
                markerAnimator.setDuration(300);
                // markerAnimator.setRepeatCount(ValueAnimator.INFINITE);
                // markerAnimator.setRepeatMode(ValueAnimator.REVERSE);
                markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        iconLayer.setProperties(
                                PropertyFactory.iconSize((float) animator.getAnimatedValue())
                        );
                    }
                });
                markerAnimator.start();
            }
        });
    }

    private void selectMarker(final SymbolLayer iconLayer) {
        markerAnimator = new ValueAnimator();
        markerAnimator.setObjectValues(1f, 2f);
        markerAnimator.setDuration(300);
        markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                iconLayer.setProperties(
                        PropertyFactory.iconSize((float) animator.getAnimatedValue())
                );
            }
        });
        markerAnimator.start();
        markerSelected = true;
    }

    private void deselectMarker(final SymbolLayer iconLayer) {
        markerAnimator.setObjectValues(2f, 1f);
        markerAnimator.setDuration(300);
        markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                iconLayer.setProperties(
                        PropertyFactory.iconSize((float) animator.getAnimatedValue())
                );
            }
        });
        markerAnimator.start();
        markerSelected = false;
    }

    public void showNoResultsWindow(){
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_noresults, null);
        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 300);
    }

    public void showPopupWindow(Feature feature){
        if(popupWindow != null){
            popupWindow.dismiss();
        }
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_map, null);

        imageView_collapse = popupView.findViewById(R.id.imageView_collapse);
        Picasso.get().load("file:///android_asset/collapse.png").into(imageView_collapse);
        linearLayout_locationItem = popupView.findViewById(R.id.linearLayout_locationItem);
        open = true;
        imageView_collapse.setOnClickListener(v -> collapse());

        TextView textView_name = popupView.findViewById(R.id.textView_listName);
        TextView textView_type = popupView.findViewById(R.id.textView_listType);
        ratingBar = popupView.findViewById(R.id.ratingBar_list);
        imageView_saved = popupView.findViewById(R.id.imageView_listSaved);
        saved = false;

        Intent intent = new Intent(context, LocationActivity.class);

        for (Map.Entry<String, JsonElement> entry : feature.properties().entrySet()) {
            // Log all the properties
            Log.d("d", String.format("%s = %s", entry.getKey(), entry.getValue()));
            switch (entry.getKey()) {
                case "location_id":
                    intent.putExtra("location_id", entry.getValue().getAsString());
                    getImage(popupView, entry.getValue().getAsString());
                    break;
                case "name":
                    textView_name.setText(entry.getValue().getAsString());
                    break;
                case "type":
                    textView_type.setText(entry.getValue().getAsString());
                    break;
            }
        }

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        popupWindow = new PopupWindow(popupView, width, height, false);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 200);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                context.startActivity(intent);
                return false;
            }
        });
    }

    public void collapse(){
        if(open){
            linearLayout_locationItem.setVisibility(View.GONE);
            open = false;
            Picasso.get().load("file:///android_asset/expand.png").into(imageView_collapse);
        } else {
            linearLayout_locationItem.setVisibility(View.VISIBLE);
            open = true;
            Picasso.get().load("file:///android_asset/collapse.png").into(imageView_collapse);
        }
    }

    public void getImage(View popupView, String locationID) {
        ImageView imageView = popupView.findViewById(R.id.imageView_list);

        String api = api_root + "api/images/" + locationID + "/" + user_id;
        client.get(api, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("api", new String(responseBody));
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    JSONObject image = response.getJSONArray("images").getJSONObject(0);
                    Picasso.get().load(image.getString("url")).into(imageView);

                    saved = response.getBoolean("saved");
                    if(saved){
                        Picasso.get().load("file:///android_asset/saved.png").into(imageView_saved);
                    } else {
                        Picasso.get().load("file:///android_asset/not_saved.png").into(imageView_saved);
                    }
                    imageView_saved.setOnClickListener(v -> addLocation(locationID));

                    ratingBar.setRating((float) response.getDouble("rating"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void addLocation(String locationID){
        if(user_id != null){
            saved = !saved;
            if(saved){
                Picasso.get().load("file:///android_asset/saved.png").into(imageView_saved);
            }
            else{
                Picasso.get().load("file:///android_asset/not_saved.png").into(imageView_saved);
            }

            String api = api_root + "api/user/add/" +user_id;
            JSONObject body = new JSONObject();
            try {
                body.put("locationId", locationID);
                body.put("saved", saved);
                StringEntity entity = new StringEntity(body.toString());
                client.post(context, api, entity, "application/json", new AsyncHttpResponseHandler() {
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

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            LocationComponentOptions locationComponentOptions =
                    LocationComponentOptions.builder(context)
                            .pulseEnabled(true)
                            .build();

            LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions
                    .builder(context, loadedMapStyle)
                    .locationComponentOptions(locationComponentOptions)
                    .build();

            // Get an instance of the component
            LocationComponent locationComponent = mp.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            getLastLocation();

        } else {
            permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> list) {
                    Toast.makeText(context, "R.string.user_location_permission_explanation", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPermissionResult(boolean b) {
                    if (b) {
                        mp.getStyle(new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                enableLocationComponent(style);
                            }
                        });
                    } else {
                        Toast.makeText(context, "R.string.user_location_permission_not_granted", Toast.LENGTH_LONG).show();
                    }

                }
            });
            permissionsManager.requestLocationPermissions(requireActivity());
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {
                        android.location.Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            LatLng latLng = new LatLng(latitude, longitude);
                            mp.setCameraPosition(new CameraPosition.Builder()
                                    .target(latLng)
                                    .zoom(10)
                                    .build());
                        }
                    }
                });
            } else {
                Toast.makeText(context, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            android.location.Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
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

    // method to check
    // if location is enabled
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
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if(popupView != null){
            popupView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if(popupView != null){
            popupView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        if(popupView != null){
            popupView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mapView.onDestroy();
        if(popupWindow != null){
            popupWindow.dismiss();
        }
    }
}
