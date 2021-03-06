package com.example.cmput301w21t25.activities_experiments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301w21t25.R;
import com.example.cmput301w21t25.activities_main.SubbedExperimentsActivity;
import com.example.cmput301w21t25.activities_forum.ForumActivity;
import com.example.cmput301w21t25.activities_trials.AddTrialActivity;
import com.example.cmput301w21t25.activities_user.MyUserProfileActivity;
import com.example.cmput301w21t25.activities_user.OtherUserProfileActivity;
import com.example.cmput301w21t25.experiments.Experiment;
import com.example.cmput301w21t25.managers.ExperimentManager;
import com.example.cmput301w21t25.managers.TrialManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

/**
 * This activity is used to view an experiment in the HomeSubbedActivity list
 */
public class ViewSubbedExperimentActivity extends AppCompatActivity {

    private String expID;
    private String ownerID;
    private String userID;
    private Bundle expBundle;
    private ExperimentManager experimentManager = new ExperimentManager();
    private TrialManager trialManager = new TrialManager();
    private Experiment exp;

    @Override
    protected void onCreate(Bundle passedData) {
        super.onCreate(passedData);
        setContentView(R.layout.activity_view_subbed_experiment);

        userID = getIntent().getStringExtra("USER_ID");
        expBundle = getIntent().getBundleExtra("EXP_BUNDLE");
        exp = (Experiment) expBundle.getSerializable("EXP_OBJ");
        expID = exp.getFb_id(); //ck


        TextView expName = findViewById(R.id.exp_name_text_view);
        TextView expDesc = findViewById(R.id.exp_description_text_view);
        TextView expType = findViewById(R.id.exp_type_text_view);
        TextView minTrials = findViewById(R.id.min_trials_text_view);
        TextView currTrials = findViewById(R.id.current_trials_text_view);
        TextView region = findViewById(R.id.region_text_view);
        experimentManager.FB_UpdateExperimentTextViews(expID,expName,expDesc,expType,minTrials,region);
        trialManager.FB_FetchPublishedTrialCount(exp,currTrials);

        final Button addTrialButton = findViewById(R.id.add_trial_button);
        final Button commentsButton = findViewById(R.id.comments_button);
        final Button dataButton = findViewById(R.id.view_data_button);
        final Button unsubscribeButton = findViewById(R.id.unsubscribe_button);
        final Button viewOwnerButton = findViewById(R.id.exp_owner_button);

        if (exp.getIsEnded()) {
            addTrialButton.setBackgroundColor(getResources().getColor(R.color.custom_Grey_translucent));
        }
        else {
            addTrialButton.setBackgroundColor(getResources().getColor(R.color.custom_Yellow_light));
        }

        //Make add trial button open add trials page
        addTrialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!exp.getIsEnded()) {
                    Intent newTrial = new Intent(ViewSubbedExperimentActivity.this, AddTrialActivity.class);

                    //This line makes sure that this activity is not saved in the history stack
                    newTrial.addFlags(newTrial.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);

                    newTrial.putExtra("USER_ID", userID);
                    newTrial.putExtra("TRIAL_PARENT", exp);
                    startActivity(newTrial);
                }
                else {
                    Toast.makeText(ViewSubbedExperimentActivity.this, "This experiment has been ended.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewOwnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FB_FetchOwnerProfile(expID);
            }
        });

        unsubscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsubscribe();
            }
        });

        commentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewComments = new Intent(ViewSubbedExperimentActivity.this, ForumActivity.class);
                viewComments.putExtra("USER_ID", userID);
                viewComments.putExtra("FORUM_EXPERIMENT", exp);
                startActivity(viewComments);
            }
        });

        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchScreens = new Intent(ViewSubbedExperimentActivity.this, ExperimentDataActivity.class);
                switchScreens.putExtra("USER_ID", userID);
                switchScreens.putExtra("EXP", exp);
                startActivity(switchScreens);
            }
        });
    }


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    /**
     * this method is used to fetch user profile associate with the provided id
     * @param id provided user ID of the user to be fetched from DB
     */
    public void FB_FetchOwnerProfile(String id){//the input param is the exp ID
        DocumentReference docRef = db.collection("Experiments").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ownerID = (String)document.getData().get("ownerID");
                        DocumentReference docRef = db.collection("UserProfile").document(ownerID);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Log.d("YA-DB:", "User document retrieved passing ID to MyUserProfileActivity");
                                        //EDEN:
                                        //For list testing I'm going to send it to homeOwned instead
                                        //Can return to userProfile activity later

                                        //check if current user = experiment owner
                                        if (ownerID.equals(userID)) {
                                            //switch to myprofile
                                            Intent intent = new Intent(ViewSubbedExperimentActivity.this, MyUserProfileActivity.class);
                                            intent.putExtra("USER_ID", userID);
                                            intent.putExtra("prevScreen", "Experiment");
                                            intent.putExtra("EXP_BUNDLE", expBundle);
                                            startActivity(intent);
                                        }
                                        else {
                                            //switch to otherprofile
                                            Intent intent = new Intent(ViewSubbedExperimentActivity.this, OtherUserProfileActivity.class);
                                            intent.putExtra("ownerID", ownerID);
                                            intent.putExtra("prevScreen", "Experiment");
                                            intent.putExtra("EXP_BUNDLE", expBundle);
                                            startActivity(intent);
                                        }

                                    }
                                } else {
                                    Log.d("YA-DB:", "User Profile Query Failed", task.getException());
                                    //this means it couldnt complete query
                                }
                            }
                        });
                    }
                } else {
                    Log.d("YA-DB: ", "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * This method will unsubscribe the user from the experiment
     */
    public void unsubscribe() {
        //This method will unsubscribe the user to the experiment
        DocumentReference docRef = db.collection("UserProfile").document(userID);
        docRef
                .update("subscriptions", FieldValue.arrayRemove(expID))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DK", "you unsubscribed");
                        if(!userID.equals(exp.getOwnerID())){
                            ArrayList<String> tempKeys = exp.getContributorUsersKeys();
                            tempKeys.remove(userID);
                            experimentManager.FB_UpdateContributorUserKeys(tempKeys,expID);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("DK", "failed to unsubscribe");
                    }
                });


        Intent intent = new Intent(ViewSubbedExperimentActivity.this, SubbedExperimentsActivity.class);
        intent.putExtra("USER_ID", userID);
        startActivity(intent);

    }
}
