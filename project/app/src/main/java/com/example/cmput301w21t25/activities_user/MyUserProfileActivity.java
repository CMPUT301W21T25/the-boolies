package com.example.cmput301w21t25.activities_user;

import android.content.Intent;
import android.os.Bundle;
//import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301w21t25.R;
import com.example.cmput301w21t25.activities_experiments.ViewSearchedExperimentActivity;
import com.example.cmput301w21t25.activities_experiments.ViewSubbedExperimentActivity;
import com.example.cmput301w21t25.activities_forum.ForumActivity;
import com.example.cmput301w21t25.activities_main.CreatedExperimentsActivity;
import com.example.cmput301w21t25.activities_main.SubbedExperimentsActivity;
import com.example.cmput301w21t25.activities_main.SearchExperimentsActivity;
import com.example.cmput301w21t25.activities_qr.MenuQRActivity;
import com.example.cmput301w21t25.activities_trials.AddTrialActivity;
import com.example.cmput301w21t25.activities_trials.HideTrialActivity;
import com.example.cmput301w21t25.experiments.Experiment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.cmput301w21t25.managers.UserManager;

/**
 * This activity is used to view the profile of the user
 * @author Curtis Yalmaz
 */

public class MyUserProfileActivity extends AppCompatActivity {
    //attributes
    private String Username;
    private String ContactInfo;
    private String userID;
    private String prevScreen;
    private String codeType;
    private Bundle expBundle;
    private Experiment exp;

    @Override
    protected void onCreate(Bundle passedData) {
        super.onCreate(passedData);
        setContentView(R.layout.activity_myprofile_view);
        userID = getIntent().getStringExtra("USER_ID");
        prevScreen = getIntent().getStringExtra("prevScreen");
        //try to get bundle, (only from experiment view)
        expBundle = getIntent().getBundleExtra("EXP_BUNDLE");
        exp = (Experiment) getIntent().getSerializableExtra("TRIAL_PARENT");
        codeType = getIntent().getStringExtra("CODE_TYPE");

        Button updateButton = findViewById(R.id.updateButton);
        Button backButton = findViewById(R.id.button2);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        FB_FetchUserInfo(userID);
    }


    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * This method fetches user info from the database using the provided id
     * @param id the id of the user
     */
    public void FB_FetchUserInfo(String id){
        DocumentReference docRef = db.collection("UserProfile").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Username = (String)document.getData().get("name");
                        ContactInfo = (String)document.getData().get("email");
                        Log.d("YA-DB: ", "DocumentSnapshot data: " + Username);
                        //update EDITTEXT fields
                        EditText editName = findViewById(R.id.updateName);
                        EditText editEmail = findViewById(R.id.updateEmail);
                        editName.setText(Username);
                        editEmail.setText(ContactInfo);
                    }
                } else {
                    Log.d("YA-DB: ", "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Go back to previous screen
     */
    public void goBack() {
        Intent intent = null;
        switch (prevScreen) {
            case "Owned":
                intent = new Intent(MyUserProfileActivity.this, CreatedExperimentsActivity.class);
                Log.i("curtis", "going back to owned");
                break;
            case "Subbed":
                intent = new Intent(MyUserProfileActivity.this, SubbedExperimentsActivity.class);
                Log.i("curtis", "going back to subbed");
                break;
            case "Experiment":
                // go back to experiment view
                intent = new Intent(MyUserProfileActivity.this, ViewSearchedExperimentActivity.class);
                intent.putExtra("EXP_BUNDLE", expBundle);
                Log.i("curtis", "going back to viewing an experiment");
                break;
            case "Browse":
                intent = new Intent(MyUserProfileActivity.this, SearchExperimentsActivity.class);
                Log.i("curtis", "going back to browse page");
                break;
            case "AddTrial":
                intent = new Intent(MyUserProfileActivity.this, AddTrialActivity.class);
                //add experiment
                intent.putExtra("TRIAL_PARENT", exp);
                Log.i("curtis", "going back to add trial page");
                break;
            case "HideTrial":
                intent = new Intent(MyUserProfileActivity.this, HideTrialActivity.class);
                intent.putExtra("EXPERIMENT", exp);
                break;
            case "Forum":
                intent = new Intent(MyUserProfileActivity.this, ForumActivity.class);
                intent.putExtra("FORUM_EXPERIMENT", exp);
                break;
            case "QR":
                intent = new Intent(MyUserProfileActivity.this, MenuQRActivity.class);
                intent.putExtra("TRIAL_PARENT", exp);
                intent.putExtra("CODE_TYPE", codeType);
            case "SubbedExperiment":
                intent = new Intent(MyUserProfileActivity.this, ViewSubbedExperimentActivity.class);
                intent.putExtra("TRIAL_PARENT", exp);
                intent.putExtra("CODE_TYPE", codeType);
            default:
                intent = new Intent(MyUserProfileActivity.this, CreatedExperimentsActivity.class);
                Log.i("curtis", "going back to default owned");
        }

        intent.putExtra("USER_ID", userID);
        startActivity(intent);
    }

    /**
     * Update user information
     */
    public void update() {
        EditText newName = findViewById(R.id.updateName);
        EditText newEmail = findViewById(R.id.updateEmail);
        String name = newName.getText().toString();
        String email = newEmail.getText().toString();
        UserManager userManager = new UserManager();

        userManager.FB_isUnique(name, userID, email, this,  "update");

    }
}
