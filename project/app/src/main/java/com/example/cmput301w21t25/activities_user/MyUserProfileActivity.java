package com.example.cmput301w21t25.activities_user;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301w21t25.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyUserProfileActivity extends AppCompatActivity {
    //attributes
    private String Username;
    private String ContactInfo;
    private String userID;

    @Override
    protected void onCreate(Bundle passedData) {
        super.onCreate(passedData);
        setContentView(R.layout.activity_myprofile_view);
        userID = getIntent().getStringExtra("userID");
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
}