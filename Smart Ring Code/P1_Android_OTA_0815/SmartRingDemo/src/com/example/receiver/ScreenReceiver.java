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
import android.widget.Toast;

public class ScreenReceiver extends BroadcastReceiver{
	private static final String TAG="screen on";

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub	
		if (intent != null  
                && Intent.ACTION_USER_PRESENT.equals(intent.getAction())){
			if(SettingActivity.SMS_on==true&&HomePageActivity.mGattCallback!=null&&HomePageActivity.Isconnected==true&&HomePageActivity.mGattCallback.mPickedDeviceGatt!=null){
          	 ChangeSMSCharacteristic();    
            }  
		}
	}

	private void ChangeSMSCharacteristic() {
		// TODO Auto-generated method stub
		BluetoothGattCharacteristic characteristic = null;
		byte b[]={(byte) 0b00000000,(byte)0b00000000,(byte)0b00000000,(byte)0b00000000};
        try {  
            characteristic = GattUtils
                    .getCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, Constants.UUID_SMART_RING_SERVICE,
                            Constants.UUID_SMART_RING_ALARM);
            characteristic.setValue(b);
            } catch (Throwable t) {
            Log.i(TAG, "invalid method for sms");           
            }
        HomePageActivity.mRequestQueue.addWriteCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, characteristic);
        HomePageActivity.mRequestQueue.execute();
	}

}
