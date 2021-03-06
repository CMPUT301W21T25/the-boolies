package com.example.cmput301w21t25.activities_trials;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cmput301w21t25.R;
import com.example.cmput301w21t25.experiments.Experiment;
import com.example.cmput301w21t25.location.Maps;
import com.example.cmput301w21t25.managers.TrialManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.GeoPoint;

/**
 * @author Eden
 * this activity is used to conduct experiments for measurement type experiments On completion, sends
 * trial to database as doc, returns to add trial list view.
 */
public class ConductMeasurementTrialActivity extends AppCompatActivity {

    Toolbar trialHeader;
    Button submitTrialButton;
    EditText measurementDisplay;
    TextView description;

    private Experiment trialParent;
    private String userID;
    private String measurementString;
    private Float measurement;

    private TrialManager trialManager;

    //Location
    private FusedLocationProviderClient locationClient;
    private Location location;
    private Maps maps;
    //End location

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle passedData) {
        super.onCreate(passedData);
        setContentView(R.layout.activity_conduct_measurement_trial);

        //Grab user ID
        userID = getIntent().getStringExtra("USER_ID");
        trialParent = (Experiment) getIntent().getSerializableExtra("TRIAL_PARENT");
        //Initialize TrialManager
        trialManager = new TrialManager();

        //Need to pass experiment ID to access title, description, etc. to pass to toolbar

        trialHeader = findViewById(R.id.measurementExperimentInfo);
        submitTrialButton = findViewById(R.id.submit_trial_measurement_button);
        measurementDisplay = findViewById(R.id.measurementEntry);
        description = findViewById(R.id.measureExpDescription);

        //Display Experiment info on conduct Trial page
        trialHeader.setTitle(trialParent.getName());
        trialHeader.setSubtitle(trialParent.getOwner());
        description.setText(trialParent.getDescription());

        Toast toast = Toast.makeText(getApplicationContext(), "The measurement are required", Toast.LENGTH_LONG);
        //On click, confirm trial, return to trial list view
        submitTrialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(measurementDisplay.getText().length()>=1){
                    if (!trialParent.isGeoEnabled() || getLocation() != null) { //check if we dont need a location or if we have one
                        measurementString = measurementDisplay.getText().toString();
                        measurement = Float.parseFloat(measurementString);
                        //Create the doc form of the trial in the database to call later

                        //Since get returns null if we dont have a location, create a null geopoint instead
                        GeoPoint geoPoint = null;
                        if (trialParent.isGeoEnabled()) {
                            geoPoint = new GeoPoint(getLocation().getLatitude(), getLocation().getLongitude());
                        }
                        trialManager.FB_CreateMeasurementTrial(userID, trialParent.getFb_id(), trialParent.getName(), trialParent.getOwner(), false, measurement, trialParent, geoPoint);
                        //Intent return to list view and add to trial list
                        Intent switchScreen = new Intent(ConductMeasurementTrialActivity.this, AddTrialActivity.class);
                        switchScreen.putExtra("USER_ID", userID);
                        switchScreen.putExtra("TRIAL_PARENT", trialParent);
                        startActivity(switchScreen);
                    }
                    else {
                        //call toast that says you need a location
                        Toast.makeText(ConductMeasurementTrialActivity.this, "This experiment requires a location!", Toast.LENGTH_SHORT).show();
                        }
                }else{
                    toast.show();
                }
            }
        });

        //                                  LOCATION (curtis)
        //---------------------------------------------------------------------------------

        //Location;
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        TrialLocationCheck(); // Check if Experiment requires location
        Button setButton = (Button) findViewById(R.id.button3);
        setButton.setVisibility(View.GONE);
        maps = new Maps();
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //exit map fragment
                setButton.setVisibility(View.GONE);
                setLocation(maps.getTrialLocation());
                getSupportFragmentManager().beginTransaction().remove(maps).commit();
            }
        });
    }

    private void TrialLocationCheck() {
        Button setLocButton = (Button) findViewById(R.id.getLocButton);
        if (trialParent.isGeoEnabled()) {
            //create button to go to map fragment
            setLocButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getUserLocation();
                }
            });
        }
        else {
            setLocButton.setVisibility(View.GONE);
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

        locationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            setLocation(location);
                            maps.setTrialLocation(location);
                            Bundle args = new Bundle();
                            args.putParcelable("TrialLocation", getLocation());
                            args.putString("MODE", "Trial");
                            Fragment mFragment = maps;
                            mFragment.setArguments(args);
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.frame, mFragment).commit();
                        }
                    }
                });
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
