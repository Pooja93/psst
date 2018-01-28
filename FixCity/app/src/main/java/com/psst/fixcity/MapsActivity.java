package com.psst.fixcity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;

    private CameraPosition mCameraPosition;
    private Location mLastKnownLocation;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(33.7756, 84.3963);
    private static final int DEFAULT_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private static int mode = -1;

    private EditText title_field, desc_field;

    //SLIDING
    private SlidingUpPanelLayout slidingLayout;
    String slidingState = "collapsed";
    float prev_sliding_val = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_maps);

        title_field = (EditText)findViewById(R.id.title_field);
        desc_field = (EditText)findViewById(R.id.desc_field);



        //SLIDING
        slidingLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingLayout.setPanelSlideListener(onSlideListener());
        slidingLayout.setAnchorPoint(0.75f);



        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        Log.d(TAG, "Asking for Permissions.");
        // Prompt the user for permission.
        getLocationPermission();

        Log.d(TAG, "Updating UI Location.");
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        Log.d(TAG, "Getting Device Loc");
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        //mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("Marker in Gatech"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation));
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Log.d(TAG, "Getting last location from fused provider.");
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                Log.d(TAG, "Done.");
                //*
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            Log.d(TAG, "New Location: "+mLastKnownLocation.getLongitude()+", "+mLastKnownLocation.getLatitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
                //*/
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void onReportClicked(View v){
        dismissKeyboard(v);
        updateSelection(0);
    }

    public void onReportListClicked(View v){
        dismissKeyboard(v);
        updateSelection(1);
    }

    public void onAccountClicked(View v){
        dismissKeyboard(v);
        updateSelection(2);
    }


    public void dismissKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    private void updateSelection(int sel){

        if(slidingState.equals("collapsed"))
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

        ImageView imgView = (ImageView)findViewById(R.id.selected_1);
        LinearLayout panel = (LinearLayout)findViewById(R.id.report_panel);
        imgView.setVisibility(View.INVISIBLE);
        panel.setVisibility(View.GONE);
        if(sel == 0){
            imgView.setVisibility(View.VISIBLE);
            panel.setVisibility(View.VISIBLE);
        }

        imgView = (ImageView)findViewById(R.id.selected_2);
        panel = (LinearLayout)findViewById(R.id.report_list_panel);
        imgView.setVisibility(View.INVISIBLE);
        panel.setVisibility(View.GONE);
        if(sel == 1){
            imgView.setVisibility(View.VISIBLE);
            panel.setVisibility(View.VISIBLE);
        }

        imgView = (ImageView)findViewById(R.id.selected_3);
        panel = (LinearLayout)findViewById(R.id.account_panel);
        imgView.setVisibility(View.INVISIBLE);
        panel.setVisibility(View.GONE);
        if(sel == 2){
            imgView.setVisibility(View.VISIBLE);
            panel.setVisibility(View.VISIBLE);
        }

        mode = sel;
    }


    private SlidingUpPanelLayout.PanelSlideListener onSlideListener() {

        return new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v){
                if(prev_sliding_val == -1) prev_sliding_val = v;
                else{
                    if(v<prev_sliding_val){
                        //dismissKeyboard(view);
                    }
                    prev_sliding_val = v;
                }
                if(mode == -1) updateSelection(0);
                slidingState = "slide";
            }

            @Override
            public void onPanelCollapsed(View view) {
                dismissKeyboard(view);
                updateSelection(-1);
                slidingState = "collapsed";
            }

            @Override
            public void onPanelExpanded(View view) {
                if(mode == -1) updateSelection(0);
                slidingState = "expanded";
            }

            @Override
            public void onPanelAnchored(View view) {
                if(mode == -1) updateSelection(0);
                slidingState = "anchored";
            }

            @Override
            public void onPanelHidden(View view) {

            }

        };
    }

}
