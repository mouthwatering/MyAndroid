package com.example.smartringdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class InfoActivity extends Activity {
	private TextView tv02, tv03, tv04;
	private static final String TAG = "iNFO_show";
	
	protected static final String MODEL_ID = "model_id";
	protected static final String MANUFACTURE_NAME = "manufacture_name";
	protected static final String SYSTEM_ID = "system_id";

	@Override
	protected void onCreate(Bundle savedInstance) {

		super.onCreate(savedInstance);
		setContentView(R.layout.info);

		tv02 = (TextView) findViewById(R.id.tv02);
		tv03 = (TextView) findViewById(R.id.tv03);
		tv04 = (TextView) findViewById(R.id.tv04);
		Intent intent=getIntent();
	   
	    tv02.setText("Manufacture Name:"+intent.getStringExtra(MANUFACTURE_NAME));
	    tv03.setText("Model Number:"+intent.getStringExtra( MODEL_ID ));
	    tv04.setText("System Id:"+intent.getStringExtra(SYSTEM_ID));
	}

	

	

}
