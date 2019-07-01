package com.rtr.win_hello;

// added by me
import androidx.appcompat.widget.AppCompatTextView; // AppCompatTextView
import android.content.Context;                     // Context
import android.graphics.Color;                      // Color
import android.view.Gravity;                        // Gravity

public class MyView extends AppCompatTextView {

	public MyView(Context drawingContext) {
		super(drawingContext);
		setTextColor(Color.rgb(0, 255, 0));
		setTextSize(60);
		setGravity(Gravity.CENTER);
		setText("Hello World!!!");

	}
}

