package com.example.memorableplacesapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.content.SharedPreferences;

import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.memorableplacesapp.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;
    double lat,lon;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                startListening();

        }
    }
    public void startListening(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    public void cameraOnShift(Location location,String title){
        if(location!=null) {
            LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.addMarker(new MarkerOptions().position(sydney).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent intent = getIntent();
        if(intent.getIntExtra("position",0)==0) {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    cameraOnShift(location, "Your Location");
                }
            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                mMap.clear();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                cameraOnShift(lastKnownLocation, "Your Location");
            }
        }
        else{
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locationList.get(intent.getIntExtra("position",0)).latitude);
            placeLocation.setLongitude(MainActivity.locationList.get(intent.getIntExtra("position",0)).longitude);
            cameraOnShift(placeLocation,MainActivity.addressList.get(intent.getIntExtra("position",0)));
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                String placeAddress=addressOp(latLng);
                mMap.addMarker(new MarkerOptions().position(latLng).title(placeAddress));
                MainActivity.addressList.add(placeAddress);
                MainActivity.locationList.add(latLng);
                MainActivity.myAdapter.notifyDataSetChanged();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.memorableplacesapp",Context.MODE_PRIVATE);

                try {
                    ArrayList<String> latitudes = new ArrayList<String>();
                    ArrayList<String> longitudes = new ArrayList<String>();
                    for(LatLng coord : MainActivity.locationList){
                        latitudes.add(Double.toString(coord.latitude));
                        longitudes.add(Double.toString(coord.longitude));
                    }

                    sharedPreferences.edit().putString("lats",ObjectSerializer.serialize(latitudes)).apply();
                    sharedPreferences.edit().putString("lons",ObjectSerializer.serialize(longitudes)).apply();

                    sharedPreferences.edit().putString("addressList",ObjectSerializer.serialize(MainActivity.addressList)).apply();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Location Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
    String addressOp(LatLng latLng){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String address = "No Address";
        try {
            List<Address> listAddress = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(listAddress !=null && listAddress.size()>0) {
                address = "";
                if (listAddress.get(0).getThoroughfare() != null) {
                    address += listAddress.get(0).getThoroughfare() +" ";
                }
                if (listAddress.get(0).getLocality() != null) {
                    address += listAddress.get(0).getLocality()+ " ";
                }
                if (listAddress.get(0).getPostalCode() != null) {
                    address += listAddress.get(0).getPostalCode()+ " ";
                }
                if (listAddress.get(0).getAdminArea() != null) {
                    address += listAddress.get(0).getAdminArea();
                }

            }

        } //try ends
        catch(Exception e){
            e.printStackTrace();
        }
        if(address.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
            address+=sdf.format(new Date());
        }
        return address;
    }

}