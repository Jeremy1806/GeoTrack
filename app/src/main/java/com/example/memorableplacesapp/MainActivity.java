package com.example.memorableplacesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView myList;
    static ArrayList<LatLng> locationList = new ArrayList<LatLng>();
    static ArrayList<String>  addressList = new ArrayList<String>();
    static ArrayAdapter<String> myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplacesapp",Context.MODE_PRIVATE);
        ArrayList<String> latitudes = new ArrayList<String>();
        ArrayList<String> longitudes = new ArrayList<String>();
        addressList.clear();
        locationList.clear();
        latitudes.clear();
        longitudes.clear();
        try{
            addressList = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("addressList",ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lats",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lons",ObjectSerializer.serialize(new ArrayList<String>())));

        }
        catch (Exception e){
            e.printStackTrace();
        }
        if(addressList.size()>0 && latitudes.size()>0 && longitudes.size()>0){
            if(addressList.size()==latitudes.size() && addressList.size()==longitudes.size()){
                for(int i=0;i<latitudes.size();i++){
                    locationList.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
        }
        else{
            locationList.add(new LatLng(0,0));
            addressList.add("Add a new Place :");
        }
        myList = findViewById(R.id.myList);

        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,addressList);
        myList.setAdapter(myAdapter);

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }

}