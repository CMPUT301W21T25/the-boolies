package com.example.cmput301w21t25.activities_user;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301w21t25.R;
import com.example.cmput301w21t25.activities_experiments.ViewExperimentActivity;
import com.example.cmput301w21t25.activities_main.HomeOwnedActivity;
import com.example.cmput301w21t25.activities_main.HomeSubbedActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OtherUserProfileActivity extends AppCompatActivity {
    //attributes
    private String Username;
    private String ContactInfo;
    private String userID;
    private String prevScreen;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle passedData) {
        super.onCreate(passedData);
        setContentView(R.layout.activity_otherprofile_view);
        userID = getIntent().getStringExtra("userID");
        prevScreen = getIntent().getStringExtra("prevScreen");
        bundle = getIntent().getBundleExtra("bundle");

        FB_FetchUserInfo(userID);
    }


    /********************************************
     * DB Functions HERE!!!!!!!!!!!!!!!!!!!!!!!!!
     ********************************************
     *******************************************/
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //so far this is the only information that comprises the user profile th
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
                        //update VIEWTEXT fields
                        TextView viewName = findViewById(R.id.viewName);
                        viewName.setText(Username);
                        TextView viewEmail = findViewById(R.id.viewEmail);
                        viewEmail.setText(ContactInfo);



                    }
                } else {
                    Log.d("YA-DB: ", "get failed with ", task.getException());
                }
            }
        });
    }


    public void backButton(View view) {
        Intent intent = null;
        switch (prevScreen) {
            case "Experiment":
                intent = new Intent(OtherUserProfileActivity.this, ViewExperimentActivity.class);
                break;

        }

        intent.putExtra("USER_ID", userID);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }
}





