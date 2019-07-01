package com.rtr.win_viewobject;

// default imports
import androidx.appcompat.app.AppCompatActivity;    // AppCompatActivity
import android.os.Bundle;                           // Bundle

// added by me
import android.view.Window;               // Window
import android.view.WindowManager;        // WindowManager
import android.view.View;                 // View
import android.content.pm.ActivityInfo;   // ActivityInfo
import android.graphics.Color;            // Color

import androidx.appcompat.widget.AppCompatTextView;       // AppCompatTextView
import android.view.Gravity;                              // Gravity


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);

        // logs
        System.out.println("RTR: inside onCreate ");

        // get rid of title bar and naviagation bar
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | 
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | 
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // make fullscreen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // forced landscape orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // set background color
        this.getWindow().getDecorView().setBackgroundColor(Color.BLACK);

        // view
        AppCompatTextView myView = new AppCompatTextView(this);
        myView.setTextColor(Color.rgb(0, 255, 0));
        myView.setTextSize(60);
        myView.setGravity(Gravity.CENTER);
        myView.setText("Hello World!!!");

        // set this view as our view
        setContentView(myView);
    }

    @Override
    protected void onPause() {
    	super.onPause();
    }

    @Override
    protected void onResume() {
    	super.onResume();
    }
}
