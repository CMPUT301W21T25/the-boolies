package com.example.cmput301w21t25.activities_experiments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cmput301w21t25.R;
import com.example.cmput301w21t25.activities_forum.ForumActivity;
import com.example.cmput301w21t25.activities_main.CreatedExperimentsActivity;
import com.example.cmput301w21t25.activities_main.SubbedExperimentsActivity;
import com.example.cmput301w21t25.activities_user.MyUserProfileActivity;
import com.example.cmput301w21t25.activities_user.OtherUserProfileActivity;
import com.example.cmput301w21t25.experiments.Experiment;
import com.example.cmput301w21t25.managers.ExperimentManager;
import com.example.cmput301w21t25.managers.TrialManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * This activity is used to browse other experiments from a list
 */
public class ViewSearchedExperimentActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_view_searched_experiment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userID = getIntent().getStringExtra("USER_ID");
        exp = unpackExperiment();
        expID = exp.getFb_id(); //ck

        TextView expName = findViewById(R.id.exp_name_text_view);
        TextView expDesc = findViewById(R.id.exp_description_text_view);
        TextView expType = findViewById(R.id.exp_type_text_view);
        TextView minTrials = findViewById(R.id.min_trials_text_view);
        TextView currTrials = findViewById(R.id.current_trials_text_view);
        TextView region = findViewById(R.id.region_text_view);
        TextView geoLoc = findViewById(R.id.geoLoc_text_view);

        Chip owner = findViewById(R.id.owner_chip);
        owner.setText(exp.getOwner());

        final Button commentsButton = findViewById(R.id.comments_button);
        final Button dataButton = findViewById(R.id.view_data_button);
        final Button subscribeButton = findViewById(R.id.subscribe_button);
        final Button ownerViewButton = findViewById(R.id.exp_owner_button);

        if (exp.isGeoEnabled()) {
            geoLoc.setText("WARNING: Trials require a location");
        }
        else {
            geoLoc.setText("No location required!");
        }


        experimentManager.FB_UpdateExperimentTextViews(expID,expName,expDesc,expType,minTrials,region);
        trialManager.FB_FetchPublishedTrialCount(exp,currTrials);

        //DK

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
            }
        });

        commentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewComments = new Intent(ViewSearchedExperimentActivity.this, ForumActivity.class);
                viewComments.putExtra("USER_ID", userID);
                viewComments.putExtra("FORUM_EXPERIMENT", exp);
                startActivity(viewComments);
            }
        });

        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchScreens = new Intent(ViewSearchedExperimentActivity.this, ExperimentDataActivity.class);
                switchScreens.putExtra("USER_ID", userID);
                switchScreens.putExtra("EXP", exp);
                startActivity(switchScreens);
            }
        });

        owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FB_FetchOwnerProfile(expID);
            }
        });
    }

    /**
     * This event is menu setup!
     * @param menu this is the menu being integrated
     * @return true to indicate there is a menu (return false to turn off)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    /**
     * This event is for menu item setup
     * @param item these are items that will be added to the menu
     * @return @return true to indicate there is this item (return false to turn off)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.home_button:
                Intent home = new Intent(ViewSearchedExperimentActivity.this, CreatedExperimentsActivity.class);

                home.putExtra("USER_ID", userID);
                startActivity(home);
                return true;
            case R.id.settings_button:
                Intent user_settings = new Intent(ViewSearchedExperimentActivity.this, MyUserProfileActivity.class);
                user_settings.putExtra("USER_ID", userID);
                user_settings.putExtra("prevScreen", "Owned");
                startActivity(user_settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method is to unpack and extract the experiment from the Intent Bundle
     * @return experiment to be viewed
     */
    private Experiment unpackExperiment() {

        expBundle = getIntent().getBundleExtra("EXP_BUNDLE");
        Experiment exp = (Experiment) expBundle.getSerializable("EXP_OBJ");
        return exp;
    }




    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //not used will be deleted after confirmed that its safe to do so -YA
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
                                            Intent intent = new Intent(ViewSearchedExperimentActivity.this, MyUserProfileActivity.class);
                                            intent.putExtra("USER_ID", userID);
                                            intent.putExtra("prevScreen", "Experiment");
                                            intent.putExtra("EXP_BUNDLE", expBundle);
                                            startActivity(intent);
                                        }
                                        else {
                                            //switch to otherprofile
                                            Intent intent = new Intent(ViewSearchedExperimentActivity.this, OtherUserProfileActivity.class);
                                            intent.putExtra("ownerID", ownerID);
                                            intent.putExtra("USER_ID", userID);
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
     * This method will subscribe the user to the experiment
     */
    public void subscribe() {
        DocumentReference docRef = db.collection("UserProfile").document(userID);
        docRef
                .update("subscriptions", FieldValue.arrayUnion(expID))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("curtis", "you subscribed");
                        if(!userID.equals(exp.getOwnerID())){
                            ArrayList<String> tempKeys = exp.getContributorUsersKeys();
                            tempKeys.add(userID);
                            experimentManager.FB_UpdateContributorUserKeys(tempKeys,expID);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("curtis", "failed to subscribe");
                    }
                });

        // change this so that it takes you back to search activity without saving viewed experiment in history
        Intent intent = new Intent(ViewSearchedExperimentActivity.this, SubbedExperimentsActivity.class);
        intent.putExtra("USER_ID", userID);
        startActivity(intent);
    }

}
