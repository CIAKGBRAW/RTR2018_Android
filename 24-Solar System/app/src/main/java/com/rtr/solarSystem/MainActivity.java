package com.rtr.solarSystem;

// default imports
import androidx.appcompat.app.AppCompatActivity;    // AppCompatActivity
import android.os.Bundle;                           // Bundle

// added by me
import android.view.Window;               // Window
import android.view.WindowManager;        // WindowManager
import android.view.View;                 // View
import android.content.pm.ActivityInfo;   // ActivityInfo
import android.graphics.Color;            // Color

public class MainActivity extends AppCompatActivity {

	private GLESView glewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);

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

        // define our own view
        glewView = new GLESView(this);

        // set this view as our view
        setContentView(glewView);
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
