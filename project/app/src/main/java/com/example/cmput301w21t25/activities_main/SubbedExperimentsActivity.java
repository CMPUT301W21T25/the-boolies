package com.example.cmput301w21t25.activities_main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cmput301w21t25.R;
import com.example.cmput301w21t25.activities_experiments.ViewSubbedExperimentActivity;
import com.example.cmput301w21t25.activities_user.MyUserProfileActivity;
import com.example.cmput301w21t25.customAdapters.CustomListExperiment;
import com.example.cmput301w21t25.experiments.Experiment;
import com.example.cmput301w21t25.managers.ExperimentManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * this activity shows a list of all the experiments this user is subscribed to
 */
public class SubbedExperimentsActivity extends AppCompatActivity {

    private ListView subbedExperimentsList;
    private ArrayAdapter<Experiment> experimentAdapter;
    private ArrayList<Experiment> subbedExperiments;
    private FloatingActionButton browseButton;
    private ExperimentManager experimentManager = new ExperimentManager();

    private float x1;
    private float x2;
    private float y1;
    private float y2;

    String userID;

    @Override
    protected void onCreate(Bundle passedData) {
        super.onCreate(passedData);
        setContentView(R.layout.activity_subbed_experiments);

        /*setup the custom toolbar!
         */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userID = getIntent().getStringExtra("USER_ID");

        browseButton = findViewById(R.id.exp_search_button);

        subbedExperimentsList = findViewById(R.id.subbed_experiment_list_view);
        subbedExperiments = new ArrayList<Experiment>();
        experimentAdapter = new CustomListExperiment(this, subbedExperiments);
        subbedExperimentsList.setAdapter(experimentAdapter);

        /////////////////////////////////////////////
        experimentManager.FB_UpdateSubbedExperimentAdapter(userID,experimentAdapter,subbedExperiments);

        //Prevent listview from eating onTouchEvent
        subbedExperimentsList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                return false;
            }
        });

        subbedExperimentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("DK: ", "Position clicked = " + position);
                Experiment experiment = (Experiment) subbedExperimentsList.getItemAtPosition(position);
                Intent viewExp = new Intent(SubbedExperimentsActivity.this, ViewSubbedExperimentActivity.class);

                Bundle expBundle = new Bundle();
                expBundle.putSerializable("EXP_OBJ", experiment);

                viewExp.putExtra("USER_ID", userID);
                viewExp.putExtra("EXP_BUNDLE", expBundle);

                startActivity(viewExp);
            }
        });

        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchScreen = new Intent(SubbedExperimentsActivity.this, SearchExperimentsActivity.class);
                switchScreen.putExtra("USER_ID", userID);
                startActivity(switchScreen);
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
                Intent home = new Intent(SubbedExperimentsActivity.this, CreatedExperimentsActivity.class);
                home.putExtra("USER_ID", userID);
                startActivity(home);
                return true;
            case R.id.settings_button:
                Intent user_settings = new Intent(SubbedExperimentsActivity.this, MyUserProfileActivity.class);
                user_settings.putExtra("USER_ID", userID);
                user_settings.putExtra("prevScreen", "Owned");
                startActivity(user_settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Screen switching
     * Base Code from: Sorting in Flow, "Slide Animation Between Activites - Android Studio Tutorial"
     * Accessed through Youtube; Published Dec. 22, 2017; Length 5:41
     * URL: https://www.youtube.com/watch?v=0s6x3Sn4eYo
     * Includes animation files slide_in_left, slide_in_right, slide_out_left, slide_out_right
     * Alterations made by Eden
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                float y = (y1 - y2);
                float x = (x1 - x2);

                //To deal with sensitivity so scrolling doesn't switch screens
                if (Math.abs(y) > Math.abs(x)) {
                    return false;
                }

                if (x1 < (x2)) {
                    Intent switchScreen = new Intent(SubbedExperimentsActivity.this, CreatedExperimentsActivity.class);
                    switchScreen.putExtra("USER_ID", userID);
                    startActivity(switchScreen);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
