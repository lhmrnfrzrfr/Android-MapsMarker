package com.ilham.mymapsmarker;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    //not primary
//    final double min = 94.0;
//    final double max = 97.9;
//    Random random = new Random();
    //primary
    private TextView tvLat, tvLong, tvAddress, tvNotif;
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String TAG = "MainActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLat = findViewById(R.id.tv3);
        tvLong = findViewById(R.id.tv4);
        tvAddress = findViewById(R.id.tv5);
        tvNotif = findViewById(R.id.tv6);
        getLocationPermission();
    }

    private void initMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission : Getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Toast.makeText(MainActivity.this, "Location found!", Toast.LENGTH_SHORT).show();
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                            LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(myLocation);
                            Double latitude = myLocation.latitude;
                            Double longitude = myLocation.longitude;
                            tvLat.setText(String.valueOf(latitude));
                            tvLong.setText(String.valueOf(longitude));
                            tvAddress.setText(getCompleteAddress(latitude, longitude));
                            gMap.clear();
                            gMap.addMarker(markerOptions);

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            gMap.setMyLocationEnabled(true);
            //gMap.getUiSettings().setMyLocationButtonEnabled(false);
            gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    //markerOptions.title(getCompleteAddress(latLng.latitude, latLng.longitude));
                    Toast.makeText(MainActivity.this, "Get latitude : " + latLng.latitude + ", longitude : " + latLng.longitude, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MainActivity.this, getCompleteAddress(latLng.latitude, latLng.longitude), Toast.LENGTH_SHORT).show();
                    Double latitude = latLng.latitude;
                    Double longitude = latLng.longitude;
                    tvLat.setText(String.valueOf(latitude));
                    tvLong.setText(String.valueOf(longitude));
                    tvAddress.setText(getCompleteAddress(latitude, longitude));
//                    double randomValue = min + (max - min) *random.nextDouble();
//                    tvNotif.setText(randomValue+"%");
                    gMap.clear();
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    gMap.addMarker(markerOptions);
                }
            });

        }
    }

    private void placeMarker(){}

    private String getCompleteAddress(double Latitude, double Longitude){

        String address = "";
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

        try{
            List<Address> addressList = geocoder.getFromLocation(Latitude, Longitude, 11);
                    if(address!=null){
                        Address returnAddress = addressList.get(0);
                        StringBuilder stringBuilderAddress = new StringBuilder("");
                        for(int i=0; i<=returnAddress.getMaxAddressLineIndex();i++){
                            stringBuilderAddress.append(returnAddress.getAddressLine(i)).append("\n");
                        }

                        address = stringBuilderAddress.toString();
                    }else{
                        Toast.makeText(this, "Address not found!", Toast.LENGTH_SHORT).show();
                    }
        }catch(Exception e){
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();

        }
        return address;
    }
}
