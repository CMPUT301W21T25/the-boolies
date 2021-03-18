package com.example.cmput301w21t25.activities_trials;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301w21t25.experiments.BinomialExperiment;
import com.example.cmput301w21t25.experiments.CountExperiment;
import com.example.cmput301w21t25.experiments.Experiment;
import com.example.cmput301w21t25.experiments.MeasurementExperiment;
import com.example.cmput301w21t25.experiments.NonNegCountExperiment;
import com.example.cmput301w21t25.trials.BinomialTrial;
import com.example.cmput301w21t25.trials.CountTrial;
import com.example.cmput301w21t25.trials.MeasurementTrial;
import com.example.cmput301w21t25.trials.NonNegCountTrial;
import com.example.cmput301w21t25.trials.Trial;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AddTrialActivity extends AppCompatActivity {

    ListView browseList;
    ArrayAdapter<Experiment> experimentArrayAdapter;

    @Override
    protected void onCreate(Bundle passedData) {
        super.onCreate(passedData);
        String userID;
        userID = getIntent().getStringExtra("USER_ID");
        Experiment exp = (Experiment) getIntent().getSerializableExtra("EXP");
        String expID = exp.getFb_id();
        FB_FetchTrialKeys(expID,userID,exp);
        //finish();
    }


    /********************************************
     *            DB Functions HERE             *
     ********************************************
     *******************************************/
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<String> trialKeys = new ArrayList<String>();
    ArrayList<Trial> trialList = new ArrayList<Trial>();
    public void FB_FetchTrialKeys(String expID,String userID,Experiment parent) {
        trialKeys.clear();
        DocumentReference docRef = db.collection("Experiments").document(expID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()&&(Boolean)document.getData().get("published")==true) {//combine into one really big conditional?
                        trialKeys = (ArrayList<String>) document.getData().get("trialKeys");
                        Log.d("YA-DB: ", "DocumentSnapshot data: " + trialKeys);
                        FB_FetchTrials(parent);
                        //FB_FetchNotSubscribed(subscriptionKeys);
                    }
                    else if(document.exists()&&(Boolean)document.getData().get("published")==false&&(String)document.getData().get("ownerID")==userID){
                        trialKeys = (ArrayList<String>) document.getData().get("trialKeys");
                    }
                } else {
                    Log.d("YA-DB: ", "get failed with ", task.getException());
                }
            }
        });
    }
    public void FB_FetchTrials(Experiment parent) {
        trialList.clear();
        String type = parent.getType();
        if(!trialKeys.isEmpty()){
            for (String key : trialKeys) {
                DocumentReference docRef = db.collection("TrialDocs").document(key);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("YA-DB: ", "testing");
                                switch (type) {
                                    case "binomial":
                                        //ArrayList<Experiment>test = new ArrayList<Experiment>();
                                        BinomialTrial binTrial = document.toObject(BinomialTrial.class);
                                        trialList.add(binTrial);
                                        break;
                                    case "count":
                                        CountTrial countTrial = document.toObject(CountTrial.class);
                                        trialList.add(countTrial);
                                        Log.d("YA-DB: ", String.valueOf(trialList));
                                        break;
                                    case "nonnegative count":
                                        NonNegCountTrial nnCountTrial = document.toObject(NonNegCountTrial.class);
                                        trialList.add(nnCountTrial);
                                        break;
                                    case "measurement":
                                        MeasurementTrial mesTrial = document.toObject(MeasurementTrial.class);
                                        trialList.add(mesTrial);
                                        break;
                                    default:
                                        Log.d("YA-DB: ", "this experiment was not assigned the correct class when it was uploaded so i dont know what class to make");
                                }
                            }
                        } else {
                            Log.d("YA-DB: ", "search failed ");
                        }
                    }
                });
            }
        }
    }

}