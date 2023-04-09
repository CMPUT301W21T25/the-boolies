package com.example.cmput301w21t25.managers;

import android.location.Location;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cmput301w21t25.FirestoreExperimentCallback;
import com.example.cmput301w21t25.FirestoreStringCallback;
import com.example.cmput301w21t25.experiments.Experiment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    //
    // ATTRIBUTES
    //
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String fb_expID = "";
    private UserManager userManager= new UserManager();
    //
    // CREATE EXPERIMENT
    //
    /**
     * This method creates an Experiment Document in the database that can later be recompiled into one of the Experiment Class's children
     * @param ownerID the ID of the user who is creating the experiment
     * @param experimentName the name of the experiment
     * @param ownerName the name of the owner of the experiment
     * @param description a brief description of the experiment
     * @param region the geolocation of the region
     * @param tags the tags associated with the experiment
     * @param subscribe a boolean that tells whether the owner subscribed to the experiment or not
     * @param geoEnabled a boolean that tells whether or not geolocation is enabled or not
     * @param published a boolean that tells whether or not the experiment is published or not
     * @param type the type of experiment(ie: count, nonNegCount, binomial etc)
     * @param date the date and time of creation of the experiment
     * @param minTrials the minimum number of trials required to end an experiment
     */
    public void FB_CreateExperiment(String ownerID, String experimentName, String ownerName,
                                    String description, String region, ArrayList<String> tags,
                                    Boolean subscribe, Boolean geoEnabled, Boolean published,
                                    String type, Date date, int minTrials){
        // Create a new experiment Hash Map this is the datatype stored in firebase for documents
        Map<String,Object> experimentDoc  = new HashMap<>();
        experimentDoc.put("ownerID", ownerID);
        experimentDoc.put("name",experimentName);
        experimentDoc.put("minNumTrials", minTrials);
        experimentDoc.put("owner",ownerName);
        experimentDoc.put("description",description);
        experimentDoc.put("region",region);
        experimentDoc.put("type", type);
        experimentDoc.put("tags",tags);
        experimentDoc.put("date",date);
        experimentDoc.put("geoEnabled",geoEnabled);
        experimentDoc.put("published",published);
        experimentDoc.put("isEnded",false);
        experimentDoc.put("trialKeys", Arrays.asList());//cause an experiment should start empty
        experimentDoc.put("commentKeys", Arrays.asList());//cause an experiment should start empty
        ArrayList<String> contributors = new ArrayList<String>();
        contributors.add(ownerID);
        experimentDoc.put("contributorUsersKeys",contributors);//cause an experiment should start empty



        // Add a new Experiment with a generated ID
        db.collection("Experiments")
                .add(experimentDoc)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        db.collection("UserProfile").document(ownerID).get()

                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if(document.exists()){
                                                ArrayList<String> currentOwned = (ArrayList<String>)document.getData().get("ownedExperiments");
                                                currentOwned.add(documentReference.getId());

                                                userManager.FB_UpdateOwnedExperiments(currentOwned,ownerID);
                                                if (subscribe) {
                                                    ArrayList<String> currentSubbed = (ArrayList<String>) document.getData().get("subscriptions");
                                                    currentSubbed.add(documentReference.getId());
                                                    userManager.FB_UpdateSubscriptions(currentOwned, ownerID);
                                                }

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
    //////////////////////////////////
    // UPDATE EXPERIMENT
    //
    //////////////////////////////////
    /**
     * this method updates the name of owner stored in the database by getting the user name o
     * @param ownerID id of the user whose name is to be updated
     */
    public void FB_UpdateName(String ownerID,String name) {
        //needs an owner id to update their experiments
        //grabs their current name, changes them on experiments
        db.collection("Experiments")
                .whereEqualTo("ownerID", ownerID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                document.getReference().update("owner", name);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
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
     * This method updates the published boolean of the experiment
     * @param ended this is the new isEnded boolean
     * @param id this is the id of the experiment you want to update
     */
    public void FB_UpdateEnded(Boolean ended,String id){
        DocumentReference docRef = db.collection("Experiments").document(id);
        docRef
                .update("isEnded", ended)
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
    public void FB_UpdateTrialKeys(ArrayList<String> conductedTrials,String id){
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
     * This method updates the list of Comment keys
     * @param comments this is the new list of comments
     * @param id this is the id of experiment you want to update
     */
    public void FB_UpdateCommentKeys(ArrayList<String> comments,String id){
        DocumentReference docRef = db.collection("Experiments").document(id);
        docRef
                .update("commentKeys", comments)
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
     *
     * @param contributors
     * @param id
     */
    public void FB_UpdateContributorUserKeys(ArrayList<String> contributors,String id){
        DocumentReference docRef = db.collection("Experiments").document(id);
        docRef
                .update("contributorUsersKeys", contributors)
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
     *
     * @param hidden
     * @param id
     */
    public void FB_UpdateHiddenUserKeys(ArrayList<String> hidden,String id){
        DocumentReference docRef = db.collection("Experiments").document(id);
        docRef
                .update("hiddenUsersKeys", hidden)
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
    //////////////////////////////////
    // UPDATE OBSERVERS
    //
    //////////////////////////////////
    /**
     *This method is used to fetch all published experiments that this user owns
     * @param userID
     * @param experimentAdapter
     * @param experiments
     */
    public void FB_UpdateOwnedExperimentAdapter(String userID, ArrayAdapter<Experiment> experimentAdapter, ArrayList<Experiment> experiments){
        userManager.FB_FetchOwnedExperimentKeys(userID, new FirestoreStringCallback() {
            @Override
            public void onCallback(ArrayList<String> list) {
                if(list.size()>0){
                    Log.d("YA-DB: ", "calling the fetch" );
                    db.collection("Experiments")
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                                    experiments.clear();
                                    experimentAdapter.notifyDataSetChanged();
                                    for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
                                    {
                                        if(list.contains(doc.getId())){
                                            Experiment experiment = doc.toObject(Experiment.class);
                                            experiment.setFb_id(doc.getId());
                                            experiments.add(experiment);
                                            experimentAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                }
                else{
                    experiments.clear();
                    experimentAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    /**
     *This method is used to fetch the list of experiments this user is subscribed to
     * @param userID
     * @param experimentAdapter
     * @param experiments
     */
    public void FB_UpdateSubbedExperimentAdapter(String userID, ArrayAdapter<Experiment> experimentAdapter, ArrayList<Experiment> experiments){
        userManager.FB_FetchSubbedExperimentKeys(userID, new FirestoreStringCallback() {
            @Override
            public void onCallback(ArrayList<String> list) {
                if(list.size()>0){
                    Log.d("YA-DB: ", "calling the fetch" );
                    db.collection("Experiments").whereEqualTo("published", true)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                                    experiments.clear();
                                    experimentAdapter.notifyDataSetChanged();
                                    for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
                                    {
                                        if (list.contains(doc.getId())) {
                                            Experiment experiment = doc.toObject(Experiment.class);
                                            experiment.setFb_id(doc.getId());
                                            experiments.add(experiment);
                                            experimentAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                }
                else{
                    experiments.clear();
                    experimentAdapter.notifyDataSetChanged();
                }
            }
        });
    }
    /**
     * This method is used to fetch list of unsubscribed experiments that are published
     * @param userID
     * @param experimentAdapter
     * @param experiments
     * @param fsCallBack
     */
    public void FB_UpdateBrowseExperimentAdapter(String userID, ArrayAdapter<Experiment> experimentAdapter, ArrayList<Experiment> experiments, FirestoreExperimentCallback fsCallBack){
        userManager.FB_FetchSubbedExperimentKeys(userID, new FirestoreStringCallback() {
            @Override
            public void onCallback(ArrayList<String> list) {
                if(list.size()<=0){
                    //setting dummy value so list isnt empty when using contains
                    list.add("dummy");
                }
                db.collection("Experiments").whereEqualTo("published", true)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        experiments.clear();
                        experimentAdapter.notifyDataSetChanged();
                        for(QueryDocumentSnapshot doc: task.getResult())
                        {
                            if (!list.contains(doc.getId())) {
                                Experiment experiment = doc.toObject(Experiment.class);
                                experiment.setFb_id(doc.getId());
                                experiments.add(experiment);
                                experimentAdapter.notifyDataSetChanged();
                            }
                            fsCallBack.onCallback(experiments);
                            }
                        }
                    });
                }
        });
    }
    /**
     * This method is used to fetch basic information about experiments and update the textviews
     * @param expID
     * @param expName
     * @param expDesc
     * @param expType
     * @param minTrials
     * @param region
     */
    public void FB_UpdateExperimentTextViews(String expID, TextView expName,TextView expDesc,TextView expType,TextView minTrials, TextView region){
        db.collection("Experiments").whereEqualTo(FieldPath.documentId(),expID)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                        for(QueryDocumentSnapshot doc: queryDocumentSnapshots)
                        {
                            Experiment experiment = doc.toObject(Experiment.class);
                            experiment.setFb_id(doc.getId());
                            expName.setText(experiment.getName());
                            expDesc.setText(experiment.getDescription());
                            expType.setText(experiment.getType());
                            minTrials.setText("Minimum Trials: " + String.valueOf(experiment.getMinNumTrials()));
                            region.setText("Region: " + experiment.getRegion());
                        }
                    }
                });
    }

    //////////////////////////////////
    //FETCH KEYS FOR OTHERS
    //////////////////////////////////
    /**
     * This method is used to fetch the list of keys of Trials this user has made
     * @param id
     * @param fsCallback
     */
    public void FB_FetchTrialKeys(String id, FirestoreStringCallback fsCallback){//the fsCallback is an object that functions similarly to a wait function
        db.collection("Experiments").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {
                if(doc != null && doc.exists()){
                    ArrayList<String> key = (ArrayList<String>) doc.get("trialKeys");
                    if(key==null){
                        key= new ArrayList<String>();
                        key.add("");
                    }
                    fsCallback.onCallback(key);
                }
            }
        });
    }
    /**
     * This method is used to fetch the list of keys of the Comments this user has made
     * @param id
     * @param fsCallback
     */
    public void FB_FetchCommentKeys(String id, FirestoreStringCallback fsCallback){//the fsCallback is an object that functions similarly to a wait function
        db.collection("Experiments").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {
                if(doc != null && doc.exists()){
                    ArrayList<String> key = (ArrayList<String>) doc.get("commentKeys");
                    if(key==null){
                        key= new ArrayList<String>();
                        key.add("");
                    }
                    fsCallback.onCallback(key);
                }
            }
        });
    }
    public void FB_FetchHiddenKeys(String id, FirestoreStringCallback fsCallback){//the fsCallback is an object that functions similarly to a wait function
        db.collection("Experiments").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {
                if(doc != null && doc.exists()){
                    ArrayList<String> key = (ArrayList<String>) doc.get("hiddenUsersKeys");
                    if(key==null){
                        key= new ArrayList<String>();
                        key.add("");
                    }
                    fsCallback.onCallback(key);
                }
            }
        });
    }
    public void FB_FetchContribKeys(String id, FirestoreStringCallback fsCallback){//the fsCallback is an object that functions similarly to a wait function
        db.collection("Experiments").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {
                if(doc != null && doc.exists()){
                    ArrayList<String> key = (ArrayList<String>) doc.get("contributorUsersKeys");
                    if(key==null){
                        key= new ArrayList<String>();
                        key.add("");
                    }
                    fsCallback.onCallback(key);
                }
            }
        });
    }
    /**
     * End of database stuff -YA
     * */
}
