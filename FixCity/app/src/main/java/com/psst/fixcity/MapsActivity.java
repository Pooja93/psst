package com.psst.fixcity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.formats.NativeAd;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.width;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String user_id = "theopanag";
    private static int reputation_total = 183, votes_total = 12, posts_total = 5;


    private GoogleMap mMap;

    private NetworkInterface networkInterface;

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

    //MARKER STUFF
    private Marker mDropPin;
    private boolean mDropPinMode = false;


    //IMAGES
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static Bitmap mImage = null;

    private EditText title_field, desc_field;

    //SLIDING
    private SlidingUpPanelLayout slidingLayout;
    String slidingState = "collapsed";
    float prev_sliding_val = -1;

    private ArrayList<Report> reports = new ArrayList<>();
    private ArrayList<Marker> reportMarkers = new ArrayList<>();

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

        networkInterface = new NetworkInterface(this);

        //SLIDING
        slidingLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingLayout.setPanelSlideListener(onSlideListener());
        slidingLayout.setAnchorPoint(0.75f);

        networkInterface.get();

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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "Clicked: "+latLng.latitude+", "+latLng.longitude);

                if(!slidingState.equals("collapsed"))
                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                if(!mDropPinMode){ return;}

                mDropPin.setPosition(latLng);

                TextView pos = (TextView)findViewById(R.id.location_text);
                pos.setText(latLng.latitude+", "+latLng.longitude);
                pos.setVisibility(View.VISIBLE);
            }
        });
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


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap resized = cropSquare(imageBitmap);

            Bitmap rotated = rotate90(resized);
            ImageView imageView = (ImageView)findViewById(R.id.display_pic);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(rotated);

            mImage = Bitmap.createBitmap(rotated);
        }
    }

    private Bitmap rotate90(Bitmap bmp){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    private Bitmap cropSquare(Bitmap bmp){
        int w = bmp.getWidth(), h = bmp.getHeight();
        Log.d(TAG, "Size: (w,h) = "+w+","+ h);
        Bitmap resized;
        if(w>h){
            resized = Bitmap.createBitmap(bmp, (w-h)/2, 0, h, h);
        }else{
            resized = Bitmap.createBitmap(bmp, 0, (h-w)/2, w, w);
        }
        Log.d(TAG, "New Size: (w,h) = "+resized.getWidth()+","+ resized.getHeight());
        return resized;
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
        if(reports == null || reports.size() == 0) return;
        ArrayList<Report> sorted_reports = sort(reports);

        LinearLayout layout = (LinearLayout)findViewById(R.id.report_list_panel);
        layout.removeAllViews();

        LinearLayout.LayoutParams lpw = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams lpm = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams lpm2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        LinearLayout.LayoutParams lpmm = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 2);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(lpm);

        LinearLayout layout1 = new LinearLayout(this);
        layout1.setLayoutParams(lpm);
        layout1.setOrientation(LinearLayout.VERTICAL);


        for(int i = 0 ; i < sorted_reports.size() ; i++){
            String title = sorted_reports.get(i).title;
            String desc = sorted_reports.get(i).desc;
            String user_id = sorted_reports.get(i).user_id;
            int votes = sorted_reports.get(i).votes;
            LatLng pos = sorted_reports.get(i).pos;
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(pos));
            reportMarkers.add(marker);

            LinearLayout entry = new LinearLayout(this);
            entry.setLayoutParams(lpm);
            entry.setOrientation(LinearLayout.HORIZONTAL);

            int padding_px = dp_to_pixels(10);
            entry.setPadding(padding_px,padding_px,padding_px,padding_px);

            ImageView thumbnail = new ImageView(this);
            thumbnail.setLayoutParams(lpmm);
            thumbnail.setImageDrawable(getResources().getDrawable(R.drawable.android_sign));
            entry.addView(thumbnail);

            LinearLayout text_layout = new LinearLayout(this);
            text_layout.setLayoutParams(lpm);
            text_layout.setOrientation(LinearLayout.VERTICAL);
            padding_px = dp_to_pixels(5);
            text_layout.setPadding(padding_px,padding_px,padding_px,padding_px);

            TextView title_txt = new TextView(this);
            title_txt.setLayoutParams(lpm);
            title_txt.setText(title);
            title_txt.setTypeface(Typeface.DEFAULT_BOLD);
            title_txt.setTextColor(Color.rgb(5,5,5));
            text_layout.addView(title_txt);

            TextView desc_txt = new TextView(this);
            desc_txt.setLayoutParams(lpm);
            desc_txt.setText(desc);
            //desc_txt.setTextColor(Color.rgb(5,5,5));
            text_layout.addView(desc_txt);

            entry.addView(text_layout);

            TextView votes_view = new TextView(this);
            votes_view.setLayoutParams(lpm2);
            votes_view.setText(votes+"");
            votes_view.setTextSize(17);
            int maxV = sorted_reports.get(0).votes;
            int minV = sorted_reports.get(sorted_reports.size()-1).votes;
            float g_factor = 180*(float)(maxV - votes)/(maxV-minV);
            float r_factor = 240*(float)(votes-minV)/(maxV-minV);
            votes_view.setTextColor(Color.rgb((int)r_factor,(int)g_factor,20));
            votes_view.setTypeface(Typeface.DEFAULT_BOLD);
            votes_view.setGravity(Gravity.CENTER);
            votes_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView vote_clicked = (TextView)view;
                    TextView votes_total_txt = (TextView)findViewById(R.id.votes_total);
                    if(vote_clicked.getTextSize() > 46){
                        vote_clicked.setTextSize(17);
                        String prev_votes = (String)vote_clicked.getText();
                        vote_clicked.setText(Integer.parseInt(prev_votes)-1+"");
                        votes_total--;
                        votes_total_txt.setText("Votes ("+votes_total+")");
                        return;}
                    vote_clicked.setTextSize(19);
                    String prev_votes = (String)vote_clicked.getText();
                    vote_clicked.setText(Integer.parseInt(prev_votes)+1+"");
                    votes_total++;
                    votes_total_txt.setText("Votes ("+votes_total+")");
                }
            });


            entry.addView(votes_view);

            layout1.addView(entry);
        }
        scrollView.addView(layout1);
        layout.addView(scrollView);
        //layout.addView(layout1);
    }

    private void removeMarkers(){
        if(reportMarkers != null) return;

        for(int i = 0 ; i < reportMarkers.size() ; i++)
            reportMarkers.get(i).remove();
        reportMarkers = new ArrayList<>();
    }

    public void onAccountClicked(View v){
        dismissKeyboard(v);
        updateSelection(2);
    }

    public void onCameraClicked(View v){
        dispatchTakePictureIntent();
    }

    public void onLocationClicked(View v){
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        if(mDropPinMode || mLastKnownLocation == null) return;
        mDropPinMode = true;
        LatLng pos = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        mDropPin = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .draggable(true));
        //mMap.getProjection().fromScreenLocation(new PointF(dropPinView.getLeft() + (dropPinView.getWidth() / 2), dropPinView.getBottom()));
    }


    public void dismissKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void onSendClicked(View v){
        String title = String.valueOf(title_field.getText());
        String description = String.valueOf(desc_field.getText());
        if(mDropPin == null) return;
        Report report = new Report(title, description, user_id, mDropPin.getPosition(), mImage);
        networkInterface.send(report);
        cleanForm();
        reports.add(report);
        Marker newMarker = mMap.addMarker(new MarkerOptions()
                .position(mDropPin.getPosition()));
        reportMarkers.add(newMarker);

    }

    private void cleanForm(){
        title_field.setText("");
        desc_field.setText("");

        int reputation_plus = 5;
        if(mImage != null) reputation_plus = 10;
        reputation_total += reputation_plus;
        TextView rep_txt = (TextView)findViewById(R.id.reputation_total);
        rep_txt.setText("Reputation ("+reputation_total+")");

        TextView posts_txt = (TextView)findViewById(R.id.posts_total);
        posts_txt.setText("Posts ("+posts_total+")");



        TextView pos = (TextView)findViewById(R.id.location_text);
        pos.setText("+"+reputation_plus+" reputation.");
        pos.setTextColor(Color.rgb(0,150,0));

        //pos.setVisibility(View.GONE);

        ImageView imageView = (ImageView)findViewById(R.id.display_pic);
        imageView.setVisibility(View.INVISIBLE);

        mDropPin.remove();
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

    public void fillReports(JSONArray jsonArray) throws Exception{
        reports.clear();
        removeMarkers();
        for(int i = 0 ; i < jsonArray.length() ; i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Report report_i = new Report();
            report_i.title = jsonObject.getString("title");
            report_i.desc = jsonObject.getString("description");
            report_i.pos = new LatLng(jsonObject.getDouble("lattitude"),
                    jsonObject.getDouble("longitude"));
            report_i.user_id = jsonObject.getString("userId");
            report_i.votes = jsonObject.getInt("votes");
            reports.add(report_i);
        }
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

    private ArrayList<Report> sort(ArrayList<Report> reports) {
        ArrayList<Report> sorted_reports = new ArrayList<>();
        ArrayList<Integer> used = new ArrayList<>();

        for (int k = 0; k < reports.size(); k++) {
            int max_votes = -1;
            int max_ind = -1;
            for (int i = 0; i < reports.size(); i++) {
                boolean found_ind = false;
                for (int j = 0; j < used.size() && !found_ind; j++) {
                    found_ind = used.get(j) == i;
                }
                if (found_ind) continue;
                if (reports.get(i).votes > max_votes) {
                    max_votes = reports.get(i).votes;
                    max_ind = i;
                }
            }
            sorted_reports.add(reports.get(max_ind));
            used.add(max_ind);
        }

        return sorted_reports;
    }

    //HELPER FUNCTIONS
    private int dp_to_pixels(float dp){
        return (int) ((dp)*getResources().getDisplayMetrics().density +0.5f);
    }
}
