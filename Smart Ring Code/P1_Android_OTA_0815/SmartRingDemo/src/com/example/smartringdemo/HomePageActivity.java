package com.example.smartringdemo;


import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.example.DB.DBHelper;
import com.example.bluetoothgatt.Constants;
import com.example.bluetoothgatt.GattUtils;
import com.example.bluetoothgatt.GattUtils.RequestQueue;
import com.example.receiver.P2PManager;




public class HomePageActivity extends ListActivity  {
	
	private ArrayList<HashMap<String, Object>> listItems;     
    private SimpleAdapter listItemAdapter;                  
    
    private TextView DeviceNameTV;
    private ImageButton DeviceList;
    private com.example.Views.SlideMenu slideMenu;
    private Button slide_btn01,slide_btn02,slide_btn03,slide_btn04,slide_btn05;
    private ProgressBar battery_progressbar;
    private TextView tv_device_name,tv_device_address,tv_state,tv_battery;
    private ImageView Conn_state;
    
    static  int  count_battery = 0;  
    private int battery_Max = 0;  
    
	private static final String TAG="HomePage";	
	
	public 	  static final String EXTRAS_DEVICE_NAME    = "DEVICE_NAME";
    public 	  static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public    static final String EXTRAS_ISVIBRATE		="ISVIBRATE";
    public    static final String EXTRAS_MODE			="MODE";
	public    static final String LOVE 				    = "LOVE";
	private   static final String CALL				    ="Incoming Call Setting";
	private   static final String SMS					="Incoming SMS Setting";
	private   static final String CLOCK				    ="Incoming Alarm Setting";
	protected static final String MODEL_ID			    = "model_id";
	protected static final String MANUFACTURE_NAME      = "manufacture_name";
	protected static final String SYSTEM_ID 		    = "system_id";
	
	private static final int DIALOG_ID_OTA_UPGRAGE 			= 1;
	private static final int DIALOG_ID_OTA_UPGRAGE_PROGRESS = 2;
	
	
    public    static GattCallback mGattCallback;
    public    static RequestQueue mRequestQueue=null;
    private String mDeviceName;
    private String mDeviceAddress ;
    private String device_info;
    private String manufacture_name;
    private String system_id;
    private String model_id;
       
    public  static boolean IsCall           =false;
    public  static boolean Isconnected      =false;   
    private static int     notificationState=1;    
    public  static boolean SMSunread        =false;    
    public boolean SMS_ON=true,Call_ON=true,Alarm_ON=true;
    public static  DBHelper dbhelper;
    private Cursor localCursor=null;
    private  SQLiteDatabase localSQLiteDatabase;
    
    Handler handler = new Handler(); 
    private com.example.receiver.P2PManager LoveManager;	
	private com.example.receiver.P2PManager.onReceiveListener LoveListener;
	
	private int mMaxProgress;
	private AlertDialog mOtaUpgradeDialog;
    private ProgressDialog mUpgradeProgressDialog;
	private EditText mPatchPathEditor;
	private OtaUpgrader mOtaUpgrader;
	
	
	
    /**
     * Callback invoked by Android framework and a LE connection state
     * change occurs
     */
    public class GattCallback extends BluetoothGattCallback{
    	
        public BluetoothGatt mPickedDeviceGatt=null; 
       
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange(): address=="
                    + gatt.getDevice().getAddress() + ", status = " + status + ", state="
                    + newState);
            boolean isConnected = (newState == BluetoothAdapter.STATE_CONNECTED);
           
            boolean isOk = (status == 0);
            if (isOk) {
            if (isConnected && isOk) {
                // Discover services, and return connection state = connected
                // after services discovered
                isOk = gatt.discoverServices();
                Log.i(TAG,"discover service");
                Conn_state.setImageResource(R.drawable.conn);
                    return;
                }      
            }

            // If we got here, this is a disconnect with or without error
            // close gatt connection
            if (!isOk) {
                gatt.close();
                //tv_state.setText("disconnected");
                Conn_state.setImageResource(R.drawable.disconn);
                battery_progressbar.setProgress(0);
                slide_btn01.setText("reconnect");
                Log.i(TAG,"connection error");
            }
            if(isConnected==false){
            	 gatt.close();
               // tv_state.setText("disconnected");
                 Conn_state.setImageResource(R.drawable.disconn);
                 battery_progressbar.setProgress(0);
                 slide_btn01.setText("reconnect");
                 Log.i(TAG,"connection down");
            }
//            processConnectionStateChanged(false, !isOk);
        }

        /**
         * Callback invoked by Android framework when LE service discovery
         * completes
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != 0) {
                // Error occurred. close the connection and return a
                // disconnected status
                gatt.close();
                try {
//                    processConnectionStateChanged(false, true);  
                	Conn_state.setImageResource(R.drawable.disconn);
                } catch (Throwable t) {
                    Log.e(TAG, "error", t);
                }
            } else {
                try {
//                    processConnectionStateChanged(true, false);
                	Log.i(TAG,"really connected");
                	Conn_state.setImageResource(R.drawable.conn);
                 	InitRead();
                 	ChangeDiscriptor(true);
                } catch (Throwable t) {
                    Log.e(TAG, "error", t);
                }
            }
        }

        /**
         * Callback invoked by Android framework when a characteristic read
         * completes
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic, int status) {
            if (status == 0) {
                try {
                   processCharacteristicRead(characteristic);
                } catch (Throwable t) {
                    Log.e(TAG, "error", t);
                }
            }
            HomePageActivity.mRequestQueue.next();// Execute the next queued request, if any
        }

        private void processCharacteristicRead(
    			BluetoothGattCharacteristic characteristic) {
    		// TODO Auto-generated method stub
    		UUID uuid=characteristic.getUuid();		
    		if(Constants.UUID_SMART_RING_ALARM_CONFIG.equals(uuid)){
    			InitDBhelper(characteristic);
    		}else if(Constants.MANUFACTURER_NAME_UUID.equals(uuid)){ 			
    			manufacture_name=characteristic.getStringValue(0);			
    		}else if(Constants.MODEL_NUMBER_UUID.equals(uuid)){	
    			model_id=characteristic.getStringValue(0);
    			Log.i(TAG, "model_id:"+model_id);
    		}else if(Constants.SYSTEM_ID_UUID.equals(uuid)){
    			
    			byte[] systemIdBytes = characteristic.getValue();
                long systemIdLong = GattUtils.unsignedBytesToLong(systemIdBytes, 8, 0);
                long manuId = 0xFFFFFFFFFFL & systemIdLong; // 40bits
                long orgId = 0xFFFFFFL & (systemIdLong >> 40);
                String manuIdString = String.format("%010X", manuId);
                String orgIdString = String.format("%06X", orgId);
               	system_id=orgIdString + " " + manuIdString;
    			Log.i(TAG, "system_id:"+system_id);
    		}
    		else if(Constants.BATTERY_LEVEL_UUID.equals(uuid)){
    			int battery_level=characteristic.getIntValue(
    	                BluetoothGattCharacteristic.FORMAT_UINT8, 0);
    			battery_progressbar.setProgress(battery_level);  
    			Conn_state.setImageResource(R.drawable.conn);
    			Log.i(TAG,"battery_level"+ battery_level);
    		}  		
    	}

    	/**
         * Callback invoked by Android framework when a descriptor read
         * completes
         */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
            if (status == 0) {
                try {
                   processDescriptorRead(descriptor);
                } catch (Throwable t) {
                    Log.e(TAG, "error", t);
                }
            }
            HomePageActivity.mRequestQueue.next();// Execute the next queued request, if
            // any
        }

        private void processDescriptorRead(BluetoothGattDescriptor descriptor) {
    		// TODO Auto-generated method stub
    		
    	}

    	/**
         * Callback invoked by Android framework when a characteristic
         * notification occurs
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic) {
            try {
            	 Log.i(TAG,"interupted");
                processCharacteristicNotification(characteristic);       
            } catch (Throwable t) {
                Log.e(TAG, "error", t);
            }
        }

        private void processCharacteristicNotification(
    			BluetoothGattCharacteristic characteristic) {
    		// TODO Auto-generated method stub
        	UUID uuid=characteristic.getUuid();
        	if(uuid.equals(Constants.UUID_SMART_RING_GESTURE)){
	        	int accept = characteristic.getIntValue(
	                    BluetoothGattCharacteristic.FORMAT_UINT8, 0);           
	              if(accept==49){
	            	  if(IsCall==true){
		            	  Log.i(TAG, "**************reject Call**********************");
		            	  rejectCall(); 
	            	  }
	            	  else{
	            		  Log.i(TAG,"************love sms nofitifation*****************");
	                	  byte[] data=new String("I miss you").getBytes();;
	                	  LoveManager.send(data, new P2PManager.Callback() {
	          				public void onSent(boolean result) {
	          					Log.i(TAG, "onSent result = " + result);
	          					if(result==false)
	          						Toast.makeText(getApplicationContext(), "send error!", 1000).show();
	            	     }
	                	  });
	                	 }
	            	  }
        	}
        	else if(uuid.equals(Constants.BATTERY_LEVEL_UUID)){
        		ReadBatteryLevel();
        	}
             
    	}
	

    	/**
         * Callback invoked by Android framework when a descriptor write
         * completes
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
            if (status == 0) {
                try {
                   processDescriptorWrite(descriptor);
                } catch (Throwable t) {
                    Log.e(TAG, "error", t);
                }
            }
            HomePageActivity.mRequestQueue.next();// Execute the next queued request, if any
        }

        private void processDescriptorWrite(BluetoothGattDescriptor descriptor) {
    		// TODO Auto-generated method stub   		
    	}

    	/**
         * Callback invoked by Android framework when a characteristic write
         * completes
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == 0) {
                try {
                   processCharacteristicWrite(characteristic);
                } catch (Throwable t) {
                    Log.e(TAG, "error", t);
                }
            }
            HomePageActivity.mRequestQueue.next();// Execute the next queued request, if any
        }

    	private void processCharacteristicWrite(
    			BluetoothGattCharacteristic characteristic) {
    		// TODO Auto-generated method stub
    		UUID uuid=characteristic.getUuid();
    		if(uuid.equals(Constants.UUID_SMART_RING_ALARM_CONFIG)){
    			
    		}
    		else if(uuid.equals(Constants.BATTERY_LEVEL_UUID)){
    			Log.i(TAG, "battery read");   			
    			
    			
    		}
    	}

		private void ReadBatteryLevel() {
			// TODO Auto-generated method stub
			BluetoothGattCharacteristic characteristic=null;
	    	characteristic = GattUtils.getCharacteristic(mGattCallback.mPickedDeviceGatt, Constants.BATTERY_SERVICE_UUID,
	    		                         Constants.BATTERY_LEVEL_UUID);
	    	mRequestQueue.addReadCharacteristic(mGattCallback.mPickedDeviceGatt, characteristic);     	 
	    	mRequestQueue.execute();
	    	Log.i(TAG,"battery charateristic"+characteristic);
		}
            
}
	
	protected void onCreate(Bundle savedInstanceState) {
		
		Log.i(TAG, "homepage oncreate");
	
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);		
	    dbhelper=new DBHelper(getApplicationContext());
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.pg_title);
		DeviceNameTV	=(TextView)findViewById(R.id.Devicename);
	     DeviceList		=(ImageButton)findViewById(R.id.Devicelist);  
	     slideMenu		= (com.example.Views.SlideMenu) findViewById(R.id.slide_menu);
	     Conn_state		=(ImageView)findViewById(R.id.conn_state);
	     slide_btn01	=(Button)findViewById(R.id.slide_btn01);
	     slide_btn02	=(Button)findViewById(R.id.slide_btn02);
	     slide_btn03	=(Button)findViewById(R.id.slide_btn03);
	    // slide_btn04	=(Button)findViewById(R.id.slide_btn04);
	     slide_btn05	=(Button)findViewById(R.id.slide_btn05);     
	     battery_progressbar=(ProgressBar)findViewById(R.id.battery_progressbar);
	     battery_progressbar.setMax(100);  
	     tv_device_name=(TextView)findViewById(R.id.device_name);
	     tv_device_address=(TextView)findViewById(R.id.mac_address);
	  
	     slide_btn01.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub		
				if (mGattCallback.mPickedDeviceGatt != null) {
					mGattCallback.mPickedDeviceGatt.disconnect();
					mGattCallback.mPickedDeviceGatt.close();	           
		        }
				Isconnected=false;
				Conn_state.setImageResource(R.drawable.disconn);
				finish();
			}    	 
	     });
	     slide_btn02.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub				
				slideMenu.closeMenu();
				Intent intent=new Intent(HomePageActivity.this,InfoActivity.class);
				intent.putExtra(MANUFACTURE_NAME, manufacture_name);
				intent.putExtra(MODEL_ID,model_id);
				intent.putExtra(SYSTEM_ID,system_id);
				startActivity(intent);
			}	    	 
	     });
	     slide_btn03.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					slideMenu.closeMenu();
//					if (mGattCallback.mPickedDeviceGatt != null) {
//                        mGattCallback.mPickedDeviceGatt.disconnect();
//                        mGattCallback.mPickedDeviceGatt.close();
//
//mGattCallback.mPickedDeviceGatt =null;
//}
//Isconnected=false;

	                showDialog(DIALOG_ID_OTA_UPGRAGE);
	                
				}	    	 
		     });
	      slide_btn05.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					slideMenu.closeMenu();
					Intent intent=new Intent(HomePageActivity.this,RenameActivity.class);
					intent.putExtra("device_name", mDeviceName);
					startActivityForResult(intent,1000);
				}
		    	 
		     });
	     
	     final Intent intent = getIntent();
	     mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
	     mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
	     
	     tv_device_address.setText(mDeviceAddress);
	     DeviceList.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (slideMenu.isMainScreenShowing()) {
					slideMenu.openMenu();
				} else {
					slideMenu.closeMenu();
				}
			}});
				
		//get the device name and mac address
		
        if(mDeviceName!=null){
        	//DeviceNameTV.setText(mDeviceName);
        	tv_device_name.setText(mDeviceName);
        	}else{
        		//DeviceNameTV.setText("Unkown Device");
            	tv_device_name.setText("Unkown Device");
        }
       
        Log.i(TAG,mDeviceName+"   "+mDeviceAddress);
	
		mGattCallback=new GattCallback();
	    mRequestQueue = com.example.bluetoothgatt.GattUtils.createRequestQueue();
	    Log.i(TAG, "request queue created");		
		DeviceScanActivity.device = DeviceScanActivity.mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
		Log.i(TAG,"device gotten");
		Isconnected=connect();	
		
		//tv_state.setText(Isconnected==true?"connected":"disconnected");	
	   
		 initListView();  
	     this.setListAdapter(listItemAdapter);	    		
		RegisterLoveListener();		
		
	}
	

	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data)
	    {
	        super.onActivityResult(requestCode, resultCode, data);
	        if(requestCode == 1000 && resultCode == 1001)
	        {
	            String new_device_name = data.getStringExtra("new_name");
	            Log.i(TAG, new_device_name);
	            tv_device_name.setText(new_device_name);
	        }
	    }

	protected void closeDevice() {
		// TODO Auto-generated method stub
		if (mGattCallback.mPickedDeviceGatt != null) {
			mGattCallback. mPickedDeviceGatt.close();
			mGattCallback.mPickedDeviceGatt = null;
        }
	}

	
	private void RegisterLoveListener() {
		// TODO Auto-generated method stub
		LoveManager = P2PManager.getInstance(getApplication());		
		LoveListener = new P2PManager.onReceiveListener() {
			public void onReceive(byte[] data, int length) {
				Log.d(TAG, "onReceive data = " + data);
				AddLoveCharacteristic();				
				onDataReceived(data, length);
			}
		};		
		LoveManager.registerListener(LoveListener);
	}


	protected void onDataReceived(byte[] data, int length) {
		// TODO Auto-generated method stub
		Intent intent=new Intent(HomePageActivity.this,LoveShow.class);
		try {
			intent.putExtra(LOVE,new String(data,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		startActivity(intent);
	}

	protected void AddLoveCharacteristic() {
		// TODO Auto-generated method stub
		BluetoothGattCharacteristic characteristic = null;
		if(HomePageActivity.mGattCallback!=null&&HomePageActivity.Isconnected==true&&HomePageActivity.mGattCallback.mPickedDeviceGatt!=null){
			byte b[]={(byte) 0b00010000,(byte)0b00000000,(byte)0b00000000,(byte)0b00000000};
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


	public void ChangeDiscriptor(boolean  connectionstate) {
		// TODO Auto-generated method stub
		/**
		 * set descriptor to 01 ,enable notification
		 */	
			// TODO Auto-generated method stub
			Log.i(TAG, "start to notify on");
			if(Isconnected==true&&mGattCallback.mPickedDeviceGatt!=null){
			     
				BluetoothGattCharacteristic notifyCharacteristic = GattUtils.getCharacteristic(
			        		mGattCallback.mPickedDeviceGatt, Constants.UUID_SMART_RING_SERVICE,
			                Constants.UUID_SMART_RING_GESTURE);  
			    BluetoothGattDescriptor descriptor = GattUtils.getDescriptor(mGattCallback.mPickedDeviceGatt,
			                    Constants.UUID_SMART_RING_SERVICE, Constants.UUID_SMART_RING_GESTURE,
			                    Constants.CLIENT_CONFIG_DESCRIPTOR_UUID);  
			    if(connectionstate){
				     mGattCallback.mPickedDeviceGatt.setCharacteristicNotification(notifyCharacteristic, true);    
				     descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);     
				     }
			    mRequestQueue.addWriteDescriptor(mGattCallback.mPickedDeviceGatt, descriptor);
			    mRequestQueue.execute();
			    Log.i(TAG, "gesture  notify already on "+descriptor.getValue());
			  
			    notifyCharacteristic = GattUtils.getCharacteristic(
		        		mGattCallback.mPickedDeviceGatt, Constants.BATTERY_SERVICE_UUID,
		                Constants.BATTERY_LEVEL_UUID);  
		        descriptor = GattUtils.getDescriptor(mGattCallback.mPickedDeviceGatt,
		                    Constants.BATTERY_SERVICE_UUID, Constants.BATTERY_LEVEL_UUID,
		                    Constants.CLIENT_CONFIG_DESCRIPTOR_UUID);  
		        if(connectionstate){
		    	   mGattCallback.mPickedDeviceGatt.setCharacteristicNotification(notifyCharacteristic, true);    
		    	   descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);  
		    	   }   
		        mRequestQueue.addWriteDescriptor(mGattCallback.mPickedDeviceGatt, descriptor);
		        mRequestQueue.execute();
		        Log.i(TAG, "battery notify already on "+descriptor.getValue());
			}      	
	}


	/**
     * Init the DB to syn with the smart ring
     * @param characteristic
     */
	public void InitDBhelper(BluetoothGattCharacteristic characteristic) {
		// TODO Auto-generated method stub
		byte b[]= characteristic.getValue();
     	int id,i;
     	localSQLiteDatabase = dbhelper.getWritableDatabase();  
        for(i=0;i<3;i++){
            id=i+1;    	 
	        localCursor = localSQLiteDatabase.rawQuery("select * from SettingTable where id="+id, null);
	        int vib=(b[i]>>>5);    	       
	        int mode=(int)(b[i]&0b00011111)+1;	       
	        //exist
	          if(localCursor.getCount()>0){ 	    
	        	  switch(i){
			          case 0:
			        	  dbhelper.Update(CALL, vib, mode,true);
		        		  break;
		        	  case 1:
		        		  dbhelper.Update(SMS,vib, mode,true);
		        		  break;
		        	  case 2:
		        		  dbhelper.Update(CLOCK, vib, mode,true);
		        		  break;
	        	  }     		          	  
	          }
	          //does not exist,default setting
	          else{
	        	  switch(i){
	        	  case 0:
	        		  dbhelper.Insert(CALL, 0, 1, true);
	        		  break;
	        	  case 1:
	        		  dbhelper.Insert(SMS, 0, 1, true);
	        		  break;
	        	  case 2:
	        		  dbhelper.Insert(CLOCK, 0, 1, true);
	        		  break;       	  
	        	  }
    			}
        }
	}

	    @Override
	    protected void onResume() {
	        super.onResume();	        
	        Log.i(TAG,"homepage resumed");
	    }

	  
		@Override
	    protected void onPause() {
	        super.onPause();
	        Log.i(TAG, "homepage paused");
	    }

	    @Override
	    protected void onDestroy() {
	    	closeDevice();	    	
	        super.onDestroy();
	        Log.i(TAG, "homepage destroyed");
	        LoveManager.unregisterListener(LoveListener);   
	     
	    }
	    
		public void onBackPressed() { 
		    //back button to back to home		  
		    Intent i= new Intent(Intent.ACTION_MAIN); 
		    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		    i.addCategory(Intent.CATEGORY_HOME); 
		    startActivity(i);  
		}
	
	    private void initListView() {
		// TODO Auto-generated method stub
			listItems = new ArrayList<HashMap<String, Object>>();  
	        String showItem=null;       
	        
	         for(int i=0;i<4;i++)     
	         {     
	             HashMap<String, Object> map = new HashMap<String, Object>();  
	             switch(i){
	            	 case 0: showItem="Incoming Call";
	            	 		break;
	            	 case 1: showItem="Incoming SMS";
	            	 		break;
	            	 case 2: showItem="Alarm Clock";
	            	 		break;
	            	 case 3: showItem="Love SMS";
	            	        break;
	            	 
	             }
	             map.put("ItemTitle", showItem);    
	             switch(i){
		             case 0:
		            	 map.put("ItemImage", R.drawable.call);
		            	 break;
		             case 1:
		            	 map.put("ItemImage", R.drawable.sms);
		            	 break;
		             case 2:
		                  map.put("ItemImage",R.drawable.alarm);
		                  break;
		             case 3:
		            	 map.put("ItemImage", R.drawable.love);
		            	 break;
		             
	             }	
	             listItems.add(map);  
	             
	         }     
           
	         listItemAdapter = new SimpleAdapter(this,listItems,
		             R.layout.list_item,		               
		             new String[] {"ItemTitle", "ItemImage"},      		                
		             new int[] {R.id.ItemTitle, R.id.ItemImage} 
	         );     
	    }

		@Override
		protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
			super.onListItemClick(l, v, position, id);
           if(id==3){
        	   startActivity(new Intent(HomePageActivity.this,LoveSetting.class));
           }
           else if(id==4){
        	   
        	   startActivity(new Intent(HomePageActivity.this,SOSsetting.class));
           }
           else{
				Intent intent_setting;
				intent_setting=new Intent(HomePageActivity.this,SettingActivity.class);			
				intent_setting.putExtra("TypeItem", position);		
				startActivity(intent_setting); 
           }			
	    }
	    private void InitRead() {
			// TODO Auto-generated method stub	    	
	    		
	    	BluetoothGattCharacteristic characteristic=null;
	    	characteristic = GattUtils.getCharacteristic(mGattCallback.mPickedDeviceGatt, Constants.UUID_SMART_RING_SERVICE,
	    		                         Constants.UUID_SMART_RING_ALARM_CONFIG);
	    	mRequestQueue.addReadCharacteristic(mGattCallback.mPickedDeviceGatt, characteristic);     	 
	    	
	    	 // Get model number
	        characteristic = GattUtils.getCharacteristic(mGattCallback.mPickedDeviceGatt,
	                Constants.DEVICE_INFO_SERVICE_UUID, Constants.MODEL_NUMBER_UUID);
	        mRequestQueue.addReadCharacteristic(mGattCallback.mPickedDeviceGatt, characteristic);

	        // Get manufacturer name
	        characteristic = GattUtils.getCharacteristic(mGattCallback.mPickedDeviceGatt,
	                Constants.DEVICE_INFO_SERVICE_UUID, Constants.MANUFACTURER_NAME_UUID);
	        mRequestQueue.addReadCharacteristic(mGattCallback.mPickedDeviceGatt, characteristic);

	        // Get system Id
	        characteristic = GattUtils.getCharacteristic(mGattCallback.mPickedDeviceGatt,
	                Constants.DEVICE_INFO_SERVICE_UUID, Constants.SYSTEM_ID_UUID);
	        mRequestQueue.addReadCharacteristic(mGattCallback.mPickedDeviceGatt, characteristic);
	        
	        characteristic = GattUtils.getCharacteristic(mGattCallback.mPickedDeviceGatt,
	                Constants.BATTERY_SERVICE_UUID, Constants.BATTERY_LEVEL_UUID);
	        mRequestQueue.addReadCharacteristic(mGattCallback.mPickedDeviceGatt, characteristic);
	        mRequestQueue.execute();	    	
		}

		/**
		 * connect to the gatt
		 */
	  private boolean connect() {
	     // if cannot connect  
		 	if (DeviceScanActivity.device == null) {
	           showMessage("cannot connect to ");
	            Log.i(TAG, "no device found");
	            return false;
	        }        
		 	//auto connect
		 	//mGattCallback.mPickedDeviceGatt = DeviceScanActivity.device.connectGatt(this, true, mGattCallback);
		 	mGattCallback.mPickedDeviceGatt = DeviceScanActivity.device.connectGatt(this, false, mGattCallback);
	        if (mGattCallback.mPickedDeviceGatt == null) {
	            showMessage("cannot connect to gatt");
	            Log.i(TAG, "no device gatt"+mDeviceAddress+" "+mDeviceName);
	            return false;
	        }
	        else {
	        	Toast.makeText(getApplicationContext(), " connecting with "+mDeviceName+" ......", 1500).show();
	            Log.i(TAG, "device gatt  connect"+mDeviceAddress+" "+mDeviceName);
	            return true;
	        }
	       
	    }
	 
	 private void showMessage(String msg) {
	        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	    }
	 /**
	  * check unread sms,here unused
	  */
	 private boolean checkSMS() {
			// TODO Auto-generated method stub
			Uri uriSMS = Uri.parse("content://sms");  
	        Cursor c = getBaseContext().getContentResolver().query(uriSMS, null,  
	                        "read = 0", null, null);  
	        if (c.getCount() == 0) {  
	                return false;  
	                // no unread
	        } else  
	                return true;  
		}
	 
	 /**
	  * change to notification state
	  */
	 public static void WriteCallGestureDiscriptor() {
			// TODO Auto-generated method stub
		        // Set the enable/disable notification settings
		        BluetoothGattCharacteristic notifyCharacteristic = GattUtils.getCharacteristic(
		        		mGattCallback.mPickedDeviceGatt, Constants.UUID_SMART_RING_SERVICE,
		                Constants.UUID_SMART_RING_GESTURE);
		       
		        if (notificationState >= 0 && notificationState <= 2) {
		            BluetoothGattDescriptor descriptor = GattUtils.getDescriptor(mGattCallback.mPickedDeviceGatt,
		                    Constants.UUID_SMART_RING_SERVICE, Constants.UUID_SMART_RING_GESTURE,
		                    Constants.CLIENT_CONFIG_DESCRIPTOR_UUID);		            
		            if (notificationState > 0) {		            	            	
		            	mGattCallback.mPickedDeviceGatt.setCharacteristicNotification(notifyCharacteristic, true);		            	
		                if (notificationState == 1) {
		                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		                    Log.i(TAG,"set notification enabled");
		                } else if (notificationState == 2) {
		                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
		                }
		            } else {
		            	HomePageActivity.mGattCallback.mPickedDeviceGatt.setCharacteristicNotification(notifyCharacteristic, false);
		                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		            }
		            mRequestQueue.addWriteDescriptor(HomePageActivity.mGattCallback.mPickedDeviceGatt, descriptor);
		            mRequestQueue.execute();
		        }
		    
		}
	 
	 public void rejectCall(){
		 try{
	            TelephonyManager manager = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
	            Class c = Class.forName(manager.getClass().getName());
	            Method m = c.getDeclaredMethod("getITelephony");
	            m.setAccessible(true);
	            ITelephony telephony = (ITelephony)m.invoke(manager);
	            telephony.endCall();
	            Log.i(TAG, "rejected");
	        } catch(Exception e){
	            Log.d("",e.getMessage());
	        }	    	
	    	ChangeCallCharacteristic();
	    	IsCall=false;
	 }


	private void ChangeCallCharacteristic() {
		// TODO Auto-generated method stub
			   Log.i(TAG, "started to change ring characteristic");
			     if(HomePageActivity.Isconnected==true&&HomePageActivity.mGattCallback.mPickedDeviceGatt!=null){
			    	 Log.i(TAG, "accepted");
			    	 BluetoothGattCharacteristic characteristic_accept = null;
		             try {
		            	 byte b[]={(byte) 0b00000000,(byte)0b00000000,(byte)0b00000000,(byte)0b00000000};
		                 characteristic_accept = GattUtils
		                         .getCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, Constants.UUID_SMART_RING_SERVICE,
		                                 Constants.UUID_SMART_RING_ALARM);
		                 characteristic_accept.setValue(b);		        
		             } catch (Throwable t) {
		                 Log.i(TAG, "invalid method for reject");	                
		             }
		             HomePageActivity.mRequestQueue.addWriteCharacteristic(HomePageActivity.mGattCallback.mPickedDeviceGatt, characteristic_accept);
		             HomePageActivity.mRequestQueue.execute();
			     }    	
	}	
	@Override
    public Dialog onCreateDialog(int id) {
    	switch (id) {
	    	case DIALOG_ID_OTA_UPGRAGE:
	    	{	    		
	    		mOtaUpgradeDialog = createOtaUpgradeDialog();
	    		return mOtaUpgradeDialog;
	    	}

	    	case DIALOG_ID_OTA_UPGRAGE_PROGRESS:
	    	{
	    		mUpgradeProgressDialog = createUpgradeProgressDialog();
	    		return mUpgradeProgressDialog;
	    	}

	    	default:
    			return null;
    	}
    }
	private AlertDialog createOtaUpgradeDialog() {        
    	AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this, AlertDialog.THEME_HOLO_LIGHT)
	    	.setTitle(R.string.ota_upgrade_dialog_title)
	    	.setPositiveButton(R.string.upgrade, 
	    			new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int whichButton) {	    					
	    					if (mOtaUpgrader == null) {
	    						String patchPath = mPatchPathEditor.getText().toString();
	    						mOtaUpgrader = new OtaUpgrader(HomePageActivity.this, mDeviceAddress, patchPath,
	    												new OtaUpgrader.Callback() {
	    							public void onFinish(int status) {
	    								final int resId;
	    								
	    								switch (status) {
		    								case OtaUpgrader.STATUS_OK:
		    									resId = R.string.upgrade_success;
		    									break;
		    								case OtaUpgrader.STATUS_ABORT:
		    									resId = R.string.upgrade_cancelled;
		    									break;
		    								default:
		    									resId = R.string.upgrade_failed;
		    									break;
	    								}

	    								runOnUiThread(new Runnable() {
	    									public void run() {
                                                mUpgradeProgressDialog = null;
	    										removeDialog(DIALOG_ID_OTA_UPGRAGE_PROGRESS);

	    										Toast.makeText(HomePageActivity.this, resId, Toast.LENGTH_SHORT).show();	
	    										
	    									
	    									}
	    								});
	    								
	    	                    		mOtaUpgrader = null;
	    							}
	    							
	    							public void onProgress(int realSize, int precent) {
	    								final int progress = realSize;

	    								if (mUpgradeProgressDialog != null) {
	    									runOnUiThread(new Runnable() {
		    									public void run() {
		    	    								mUpgradeProgressDialog.setProgress(progress);
		    									}
	    									});
	    								}
	    							}
	    						});
	    						
	    						mMaxProgress = mOtaUpgrader.getPatchSize();
	    						
	    						mOtaUpgrader.start();
	    					}

	    					showDialog(DIALOG_ID_OTA_UPGRAGE_PROGRESS);
	    				}
                })
	    	.setNegativeButton(R.string.cancel,
	    			new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						Toast.makeText(HomePageActivity.this, R.string.upgrade_cancelled, Toast.LENGTH_SHORT).show();
    					}
            	});

	    View view = getLayoutInflater().inflate(R.layout.ota_upgrade_dialog, null);
	    builder.setView(view);
	
		mPatchPathEditor = (EditText)(view.findViewById(R.id.patch_path_editor));
		return builder.create();
    }

	private ProgressDialog createUpgradeProgressDialog() {
		ProgressDialog dialog = new ProgressDialog(HomePageActivity.this);
		
	    dialog.setTitle(R.string.ota_upgrade_progress_dialog_title);
	    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    dialog.setMax(mMaxProgress);
	    dialog.setProgress(0);
	    dialog.setCancelable(false);
	    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	if (mOtaUpgrader != null) {
                    		mOtaUpgrader.stop();
                    	}
                    }
                });

	    return dialog;
	}
}
