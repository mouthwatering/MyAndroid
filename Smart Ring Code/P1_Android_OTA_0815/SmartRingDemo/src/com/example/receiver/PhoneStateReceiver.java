package com.example.receiver;


import com.example.bluetoothgatt.Constants;
import com.example.bluetoothgatt.GattUtils;
import com.example.smartringdemo.HomePageActivity;
import com.example.smartringdemo.SettingActivity;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateReceiver extends BroadcastReceiver {

	private static final String TAG="IncomingCall";
	private static boolean incomingFlag=false;
	private static String incomingNumber=null;
		   
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//if call out
		if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){                        
            incomingFlag = false;
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);        
            Log.i(TAG, "call OUT:"+phoneNumber);                        
		}else{                        
            TelephonyManager tm = 
                (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);                                
            switch (tm.getCallState()) {
	            case TelephonyManager.CALL_STATE_RINGING:
	                    incomingFlag = true;//it is an incoming call
	                    incomingNumber = intent.getStringExtra("incoming_number");
	                    Log.i(TAG, "RINGING :"+ incomingNumber);
	                    Log.i(TAG,"call_on:"+SettingActivity.Call_on);
	                    if(SettingActivity.Call_on==true){
	                    	if(HomePageActivity.mGattCallback!=null){
	                    		HomePageActivity.IsCall=true;
	                    		AddRingCharacteristicToGatt();
	                    	}
	                    }
	                    break;
	            case TelephonyManager.CALL_STATE_OFFHOOK:  
	            	 if(incomingFlag){
	                     Log.i(TAG, "incoming ACCEPT :"+ incomingNumber);}
	            case TelephonyManager.CALL_STATE_IDLE:                                
	                    if(incomingFlag){
	                            Log.i(TAG, "incoming IDLE");                                
	                    }
	                    ChangeRingCharateristic();  
	                    break;
            } 
	
		}}

	
	/**
	 * Change the incoming call characteristic to end led flash
	 */
	public  void ChangeRingCharateristic() {
		 // TODO Auto-generated method stub	  
		     if(HomePageActivity.Isconnected==true&&HomePageActivity.mGattCallback.mPickedDeviceGatt!=null){
		    	 BluetoothGattCharacteristic characteristic_accept = null;
	             try {
	            	 byte b[]={(byte) 0b00000000,(byte)0b00000000,(byte)0b00000000,(byte)0b00000000};
	                 characteristic_accept = GattUtils
	                         .getCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, Constants.UUID_SMART_RING_SERVICE,
	                                 Constants.UUID_SMART_RING_ALARM);
	                 characteristic_accept.setValue(b);
	        
	             } catch (Throwable t) {
	                 Log.i(TAG, "invalid method for accept");	                
	             }
	             HomePageActivity.mRequestQueue.addWriteCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, characteristic_accept);
	             HomePageActivity.mRequestQueue.execute();
		     }    
	}
	
	/**
	 * Add the incoming call characteristic
	 */
	private void AddRingCharacteristicToGatt() {
		// TODO Auto-generated method stub
		if(HomePageActivity.mGattCallback.mPickedDeviceGatt!=null){
			if(HomePageActivity.Isconnected==true)
			{
				BluetoothGattCharacteristic characteristic = null;
		        try {
		        	byte b[]={(byte) 0b10000000,(byte)0b00000000,(byte)0b00000000,(byte)0b00000000};
		              characteristic = GattUtils
		                      .getCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, Constants.UUID_SMART_RING_SERVICE,
		                              Constants.UUID_SMART_RING_ALARM);
		              characteristic.setValue(b);	  		             
		              Log.i(TAG, "call added");
		        } catch (Throwable t) {
		              Log.i(TAG, "invalid method for call");             
		          }
		          HomePageActivity.mRequestQueue.addWriteCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, characteristic);
		          HomePageActivity.mRequestQueue.execute();
		          Log.i(TAG, "call executed");
			}
	  }
	}
	
}

