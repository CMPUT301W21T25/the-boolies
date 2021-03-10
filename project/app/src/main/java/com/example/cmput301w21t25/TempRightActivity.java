package com.example.cmput301w21t25;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

public class TempRightActivity extends AppCompatActivity {

    float x1;
    float x2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_right);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                break;
        }

        if (x1 < x2) {
            Intent switchScreen = new Intent(TempRightActivity.this, MainActivity.class);
            startActivity(switchScreen);
        }
        return super.onTouchEvent(event);
    }
}