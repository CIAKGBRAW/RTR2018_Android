package com.rtr.win_hello;

// default imports
import androidx.appcompat.app.AppCompatActivity;    // AppCompatActivity
import android.os.Bundle;                           // Bundle

// added by me
import android.view.Window;               // Window
import android.view.WindowManager;        // WindowManager
import android.content.pm.ActivityInfo;   // ActivityInfo
import android.graphics.Color;            // Color

public class MainActivity extends AppCompatActivity {

	private MyView myView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);

        // get rid of title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        // make fullscreen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // forced landscape orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        this.getWindow().getDecorView().setBackgroundColor(Color.BLACK);

        // define our own view
        myView = new MyView(this);

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
