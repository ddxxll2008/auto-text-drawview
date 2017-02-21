package com.zbys.autotext;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private FrameLayout content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
}
