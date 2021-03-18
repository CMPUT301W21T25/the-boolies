package com.example.cmput301w21t25.managers;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ExperimentManager {
    /**
     * please look over this before continuing work
     * Database stuff starting here ill add proper comments later in a bit of a rush atm
     * methods to get and set to database use as u please attributes in this section are listed below
     * @author:Yalmaz Abdullah
     * todo: update comments, add security and exceptions, complete incomplete methods,CHECK IF THIS STUFF WORKS RIGHT
     * */
    //ATTRIBUTES
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String fb_expID = "";
    private UserManager userManager= new UserManager();

    //CREATE EXPERIMENT
    /**
     * This method creates an Experiment Document in the database that can later be recompiled into one of the Experiment Class's children
     * @param userID the ID of the user who is creating the experiment
     * @param name the name of the experiment
     * @param ownerName the name of the owner of the experiment
     * @param description a brief description of the experiment
     * @param region the geolocation of the region
     * @param tags the tags associated with the experiment
     * @param geoEnabled a boolean that tells weather or not geolocation is enabled or not
     * @param published a boolean that tells weather or not the experiment is published or not
     * @param type the type of experiment(ie: count, nonNegCount, binomial etc)
     * @param date the date and time of creation of the experiment
     */
    public void FB_CreateExperiment(String userID, String name, String ownerName, String description, Location region, ArrayList<String> tags, Boolean geoEnabled, Boolean published, String type, Date date){
        // Create a new experiment Hash Map this is the datatype stored in firebase for documents
        Map<String,Object> experimentDoc  = new HashMap<>();
        experimentDoc.put("name",name);
        experimentDoc.put("owner",ownerName);
        experimentDoc.put("description",description);
        experimentDoc.put("region",region);
        experimentDoc.put("type", type);
        experimentDoc.put("tags",tags);
        experimentDoc.put("date",date);
        experimentDoc.put("geoEnabled",geoEnabled);
        experimentDoc.put("published",published);
        experimentDoc.put("trialKeys", Arrays.asList());//cause an experiment should start empty

        //experiment.put("comment", ); ill add this later

        // Add a new Experiment with a generated ID
        db.collection("Experiments")
                .add(experimentDoc)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        db.collection("UserProfile").document(userID).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if(document.exists()){
                                                ArrayList<String> currentOwned = (ArrayList<String>)document.getData().get("ownedExperiments");
                                                currentOwned.add(documentReference.getId());
                                                userManager.FB_UpdateOwnedExperiments(currentOwned,userID);
                                                userManager.FB_UpdateSubscriptions(currentOwned,userID);
                                            }
                                        }
                                    }
                                });

                        }
                    })
                .addOnFailureListener(new OnFailureListener() {
                    //security stuff to make debuging easier
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding experiment", e);
                    }
                });
    }

    //UPDATE EXPERIMENT
    /**
     * This method updates the description of the experiment
     * @param description new description
     * @param id id of the experiment you want to update
     */
    public void FB_UpdateDescription(String description,String id){
        DocumentReference docRef = db.collection("Experiments").document(id);
        docRef
                .update("description", description)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
    /**
     * This method updates the tags of an experiment
     * @param tags the new list of tags
     * @param id the id of the experiment you want to update
     */
    public void FB_UpdateTags(ArrayList tags,String id){
        DocumentReference docRef = db.collection("Experiments").document(id);
        docRef
                .update("tags", tags)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    /**
     * This method updates the geoEnabled boolean
     * @param geoEnabled this is the new geoEnabled boolean
     * @param id this is id of the experiment you want to update
     */
    public void FB_UpdateGeoEnabled(Boolean geoEnabled,String id){
        DocumentReference docRef = db.collection("Experiments").document(id);
        docRef
                .update("geoEnabled", geoEnabled)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    /**
     * This method updates the published boolean of the experiment
     * @param published this is the new published boolean
     * @param id this is the id of the experiment you want to update
     */
    public void FB_UpdatePublished(Boolean published,String id){
        DocumentReference docRef = db.collection("Experiments").document(id);
        docRef
                .update("published", published)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    /**
     * This method updates the list of trial keys
     * @param conductedTrials this is the new list of conducted trials
     * @param id this is the id of experiment you want to update
     */
    public void FB_UpdateConductedTrials(ArrayList<String> conductedTrials,String id){
        DocumentReference docRef = db.collection("Experiments").document(id);
        docRef
                .update("trialKeys", conductedTrials)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
    /**
     * End of database stuff -YA
     * */
}
