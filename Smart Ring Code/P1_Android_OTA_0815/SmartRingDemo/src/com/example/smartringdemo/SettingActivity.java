package com.example.smartringdemo;

import com.example.bluetoothgatt.Constants;
import com.example.bluetoothgatt.GattUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

public class SettingActivity extends Activity{
	
	private static final String TAG="SettingActivity";
	private ImageButton save,back;
	private static TextView Setting;
	private static CheckBox Vibrate;
	private static RadioGroup ModeRadio;
	private static String SettingType;
	public  static Switch switch01;
	public  static boolean Call_on = true;
	public  static boolean SMS_on  = true;
	public  static boolean Alarm_on= true;
	private static final String CALL  = "Incoming Call Setting";
	private static final String SMS   = "Incoming SMS Setting";
	private static final String CLOCK= "Incoming Alarm Setting";
	byte b[]={(byte) 0b00000000,(byte)0b00000000,(byte)0b00000000,(byte)0b00000000,
			(byte) 0b00000000,(byte)0b00000000,(byte)0b00000000,(byte)0b00000000,
			(byte) 0b00000000,(byte)0b00000000,(byte)0b00000000,(byte)0b00000000};
	
	public int type;
	private  Cursor localCursor =null;
	private SQLiteDatabase localSQLiteDatabase ;

	protected void onCreate(Bundle SavedInstance){
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    super.onCreate(SavedInstance);
		setContentView(R.layout.setting);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.setting_title);
		save=(ImageButton)findViewById(R.id.btn_save);
		back=(ImageButton)findViewById(R.id.btn_back);
		Setting=(TextView)findViewById(R.id.setting_type);
		Vibrate=(CheckBox)findViewById(R.id.Isvibrate);
		switch01=(Switch)findViewById(R.id.switch1);
		ModeRadio=(RadioGroup)findViewById(R.id.radioGroup);
		
		Intent intent=getIntent();	
		type=intent.getIntExtra("TypeItem", 0);
		switch(type){
			case 0:
				SettingType=CALL;
				break;
			case 1:
				SettingType=SMS;
				break;
			case 2:
				SettingType=CLOCK;
				break;		
	     }
		Log.i(TAG, "selected :"+type);
		localSQLiteDatabase = HomePageActivity.dbhelper.getWritableDatabase();  
		
		InitView(type);
		InitByte();
		switch01.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(type==0){
					Call_on=arg1;
				}
				else if(type==1){
					SMS_on=arg1;
				}
				else if(type==2){
					Alarm_on=arg1;
				}			
			}});
		
		save.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Save();		
			}	
		});
		back.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Back();
			}
		});	
	}
	  
    private void InitByte() {
		// TODO Auto-generated method stub
    	
        for(int i=1;i<4;i++){
	        localCursor= localSQLiteDatabase.rawQuery("select * from SettingTable where id="+i, null); 
	       if(localCursor!=null) {
	    	   localCursor.moveToFirst();}   
	       if(localCursor.getCount()>0){	    	
	    		   b[i-1]=(byte)((localCursor.getInt(2)<<5)+(localCursor.getInt(3)-1));	    	      	  
	       }
        }
	}

	protected void Back() {
		// TODO Auto-generated method stub
    	 new AlertDialog.Builder(this).setTitle("Are you sure to save")
    	 .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Save();
			}    		 
    	 }).setNegativeButton("no", new DialogInterface.OnClickListener() {       
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
            	finish();
            }  
        }).show();  
	}

	protected void Save() {
		// TODO Auto-generated method stub
    	SettingSave(type,SettingType);
		Toast.makeText(getApplicationContext(), "saved", 1000).show();
		finish();	
	}
	

	@Override
    protected void onDestroy() {
		super.onDestroy();
		if(localCursor!=null){
			localCursor.close();
			}
	}
	 void InitView(int type) {
		// TODO Auto-generated method stub
		int id=type+1;	       
        try{
	       localCursor=localSQLiteDatabase.rawQuery("select * from SettingTable where id="+id, null); 
	       if(localCursor!=null) {
	    	   Log.i(TAG,"cursor not null");
	    	   localCursor.moveToFirst();}   
	       if(localCursor.getCount()>0){    	   
	    	    Log.i(TAG,"count>0");
		        Setting.setText(localCursor.getString(localCursor.getColumnIndex("setting_name")));	       
		        switch01.setChecked(localCursor.getString(4).equals("1"));
		        switch(localCursor.getInt(3)){
		        case 1:
		        	 ModeRadio.check(R.id.radioMode1);
		        	break;
		        case 2:
		        	ModeRadio.check(R.id.radioMode2);
		        	break;
		        case 3:
		        	ModeRadio.check(R.id.radioMode3);
		        	break;
		        case 4:
		        	ModeRadio.check(R.id.radioMode4);
		        	break;
		        }
		        Vibrate.setChecked(localCursor.getInt(2)==1);
	       }
        }catch(Exception e){
        	e.printStackTrace();
        }
	}

	/**
	 * mode setting.now 2 mode for vibration, 4 for led
	 * @param type ,SettingType
	 */
	protected void SettingSave(int type,String SettingType) {
		// TODO Auto-generated method stub
		BluetoothGattCharacteristic characteristic=null;
		int vib=0,mode=1;		
		// no vibrate
		if(Vibrate.isChecked()==false){
			b[type]=0x00;		
			vib=0;
		}
		else{
			b[type]=0x20;
			vib=1;
		}
		
		switch(ModeRadio.getCheckedRadioButtonId()){
			case R.id.radioMode1:				
				b[type]=(byte) (b[type]+0x00);
				mode=1;
				break;
			case R.id.radioMode2:				
				b[type]=(byte)(b[type]+0x01);
				mode=2;
				break;
			case R.id.radioMode3:
				b[type]=(byte)(b[type]+0x02);
				mode=3;
				break;
			case R.id.radioMode4:
				b[type]=(byte)(b[type]+0x03);
				mode=4;
				break;
			default:
				b[type]=(byte) (b[type]+0x00);
				mode=2;
				break;
		}
		Log.i(TAG, "type:"+type+" b[type] :"+b[type]);
		HomePageActivity.dbhelper.Update(SettingType, vib, mode,switch01.isChecked());
		//GattDB
		try {	 
            characteristic = GattUtils
                    .getCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, Constants.UUID_SMART_RING_SERVICE,
                            Constants.UUID_SMART_RING_ALARM_CONFIG);
            Log.i(TAG, "characteristic:"+characteristic);
            characteristic.setValue(b);
        } catch (Throwable t) {
            Log.i(TAG, "invalid setting ");         
        }
        HomePageActivity.mRequestQueue.addWriteCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, characteristic);
        HomePageActivity.mRequestQueue.execute();
	}

	
}
