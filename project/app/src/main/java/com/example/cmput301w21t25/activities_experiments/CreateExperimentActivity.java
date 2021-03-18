package com.example.cmput301w21t25.activities_experiments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301w21t25.activities_main.HomeOwnedActivity;
import com.example.cmput301w21t25.managers.ExperimentManager;
import com.example.cmput301w21t25.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class CreateExperimentActivity extends AppCompatActivity {
    //(String name, String ownerID, String description, Location region, ArrayList<String> tags, Boolean geoEnabled, Boolean published, String type, Date date)
    EditText experimentName;
    EditText experimentDescription;
    EditText experimentTags;

    ArrayList<String> experimentKeywords;
    String type;

    Date experimentDate;
    Location experimentLocation;

    CheckBox published;
    CheckBox geolocationEnabled;

    String experimentOwner;

    ExperimentManager experimentManager;

    Button createExperiment;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void onCreate(Bundle passedData){
        super.onCreate(passedData);
        //Change layout
        setContentView(R.layout.activity_create_experiment);
        String userID;
        userID = getIntent().getStringExtra("USER_ID");
        //this can be called on click when
        //User ID for testing (has owned experiment): fdNzWupOTDKvwkrVHMADau



        experimentManager = new ExperimentManager();

        //This is temp I don't know what to do for location
        Location testLocal = new Location("edm");

        experimentName = findViewById(R.id.editTextExpName);


        published = findViewById(R.id.checkBoxPublish);
        geolocationEnabled = findViewById(R.id.checkBoxGeolocation);

        experimentDescription = findViewById(R.id.editTextEnterDescription);

        experimentTags = findViewById(R.id.editTextKeywords);



        createExperiment = findViewById(R.id.buttonCreateExperiment);
        createExperiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("YA-DB","testing call");
                /*
                ArrayList<String> testList = new ArrayList<>();
                testList.add("this");
                testList.add("is");
                testList.add("not a");
                testList.add("test");
                 */
                //experimentManager.FB_CreateExperiment("NEWTestName","fdNzWupOTDKvwkrVHMADau", "this is a test",testLocal,testList,false,false,"abstract",new Date());

                String description = experimentDescription.getText().toString();
                String name = experimentName.getText().toString();

                experimentKeywords = new ArrayList<String>();
                String keywords = experimentTags.getText().toString();
                experimentKeywords = parseKeywords(keywords);


                Log.d("description", description);
                Log.d("name", name);
                Log.d("keywords", experimentKeywords.toString());
                //Get the user's name from their profile
                DocumentReference docRef = db.collection("UserProfile").document(userID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String experimentOwner = (String) document.getData().get("name");
                                experimentManager.FB_CreateExperiment(userID, name, experimentOwner, description, testLocal, experimentKeywords, geolocationEnabled.isChecked(), published.isChecked(), type, new Date());
                                //
                            }
                        }
                    }
                });
                Intent switchScreen = new Intent(CreateExperimentActivity.this, HomeOwnedActivity.class);
                switchScreen.putExtra("USER_ID", userID);
                startActivity(switchScreen);
            }
        });

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButtonCount:
                if (checked)
                    type = "count";
                break;
            case R.id.radioButtonNonNegInt:
                if (checked)
                    type = "nonnegative count";
                break;
            case R.id.radioButtonMeasurement:
                if (checked)
                    type = "measurement";
                break;
            case R.id.radioButtonBinomial:
                if (checked)
                    type = "binomial";
                break;
        }
    }

    public ArrayList<String> parseKeywords(String keywords) {

        ArrayList<String> keywordList = new ArrayList<String>();
        StringTokenizer splitKeywords = new StringTokenizer(keywords, ",");

        while (splitKeywords.hasMoreTokens()) {
            String keyword = (String) splitKeywords.nextToken();

            if (keyword.trim().length() > 0) {
                keywordList.add(keyword.trim().toLowerCase());
            }
        }

        return keywordList;
    }

}