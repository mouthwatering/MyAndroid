package com.example.receiver;

import com.example.bluetoothgatt.Constants;
import com.example.bluetoothgatt.GattUtils;
import com.example.smartringdemo.DeviceScanActivity;
import com.example.smartringdemo.HomePageActivity;
import com.example.smartringdemo.SettingActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context; 
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver{

	public static final String TAG = "Alarm";

    //android.provider.Telephony.Sms.Intents
    public static final String ALARM_RECEIVED_ACTION = "com.android.deskclock.ALARM_ALERT";	  

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(ALARM_RECEIVED_ACTION))
	       {           
	              Log.i(TAG, "new ALARM");	  
	              if(SettingActivity.Alarm_on==true){
	            	  AddAlarmCharacteristic();
	            	  }
	       }
	}

	private void AddAlarmCharacteristic() {
		// TODO Auto-generated method stub
		BluetoothGattCharacteristic characteristic = null;

		if(HomePageActivity.mGattCallback!=null&&HomePageActivity.Isconnected==true&&HomePageActivity.mGattCallback.mPickedDeviceGatt!=null){
			byte b[]={(byte) 0b00100000,(byte)0b00000000,(byte)0b00000000,(byte)0b00000000};
	        try {          
	            characteristic = GattUtils
	                    .getCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, Constants.UUID_SMART_RING_SERVICE,
	                            Constants.UUID_SMART_RING_ALARM);
	            
	            	characteristic.setValue(b);
	            
	        } catch (Throwable t) {
	            Log.i(TAG, "invalid method for alarm");           
	        }
	        HomePageActivity.mRequestQueue.addWriteCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, characteristic);
	        HomePageActivity.mRequestQueue.execute();
		}
 
	}


}
