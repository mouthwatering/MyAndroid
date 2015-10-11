package com.example.receiver;

import com.example.bluetoothgatt.Constants;
import com.example.bluetoothgatt.GattUtils;
import com.example.smartringdemo.HomePageActivity;
import com.example.smartringdemo.SettingActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver{
	
	public static final String TAG = "SMS";

    //android.provider.Telephony.Sms.Intents
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";	  

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(SMS_RECEIVED_ACTION))
	       {           
	              Log.i(TAG, "new MSM");	 
	              if(SettingActivity.SMS_on==true&&HomePageActivity.mGattCallback!=null&&HomePageActivity.Isconnected==true&&HomePageActivity.mGattCallback.mPickedDeviceGatt!=null){
	            	  AddSMSCharacteristic();    
	              }        
           }
	}
	private void AddSMSCharacteristic() {
		// TODO Auto-generated method stub
		BluetoothGattCharacteristic characteristic = null;
		byte b[]={(byte) 0b01000000,(byte)0b00000000,(byte)0b00000000,(byte)0b00000000};
        try {  
            characteristic = GattUtils.getCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, Constants.UUID_SMART_RING_SERVICE,
                            Constants.UUID_SMART_RING_ALARM);
            characteristic.setValue(b);
            } catch (Throwable t) {
            Log.i(TAG, "invalid method for sms");           
            }
        HomePageActivity.mRequestQueue.addWriteCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, characteristic);
        HomePageActivity.mRequestQueue.execute();
	}
}
