package com.example.smartringdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class LoveShow extends Activity{
	private static final String LOVE="LOVE";
	private static final String  TAG="love";
	private TextView tv01;
	protected void onCreate(Bundle SavedInstance){
		super.onCreate(SavedInstance);
		setContentView(R.layout.love_show);
		tv01=(TextView)findViewById(R.id.tv01);
		Intent intent=getIntent();
		Log.i(TAG,intent.getStringExtra(LOVE));
		tv01.setText(intent.getStringExtra(LOVE));
	}
}
