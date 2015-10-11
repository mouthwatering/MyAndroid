package com.example.smartringdemo;

import com.example.bluetoothgatt.Constants;
import com.example.bluetoothgatt.GattUtils;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RenameActivity extends Activity{
	private Button save_name;
	private EditText rename;
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
		setContentView(R.layout.rename);
		save_name=(Button)findViewById(R.id.save_name);
		rename=(EditText)findViewById(R.id.new_name);
		
		Intent intent = getIntent();	
		save_name.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				 String str=rename.getText().toString();
				 Intent intent = new Intent();
	             intent.putExtra("new_name",str);
	              setResult(1001, intent);
	              //AddWriteCharacteristic(str);
	              finish();
			}
			
		});
	}
//	protected void AddWriteCharacteristic(String str) {
//		// TODO Auto-generated method stub
//		 if(HomePageActivity.mGattCallback!=null&&HomePageActivity.Isconnected==true&&HomePageActivity.mGattCallback.mPickedDeviceGatt!=null){
//			 BluetoothGattCharacteristic characteristic = null;	
//		        try {  
//		            characteristic = GattUtils.getCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, Constants.SERVICE_GAP_UUID,
//		                            Constants.DEVICE_NAME_UUID);
//		            
//		            characteristic.setValue(str);
//		            } catch (Throwable t) {
//		            
//		            }
//		        HomePageActivity.mRequestQueue.addWriteCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, characteristic);
//		        HomePageActivity.mRequestQueue.execute();  
//         }
//	}

}
