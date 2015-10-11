package com.example.receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.os.Handler;

import java.util.ArrayList;

import com.example.smartringdemo.LoveSetting;

public class P2PManager {
	public static final int PORT_NUMER = 1234;
	private static final String TAG = "SmartRingManager";
	
	private static P2PManager instance;
	private static Handler mHandler;
	
	private Context mContext;
	private String mRemoteIPAddress;

    final ArrayList<onReceiveListener> mListenerList = new ArrayList<onReceiveListener>();
    private BroadcastReceiver mReceiver;

    public interface Callback {
		public void onSent(boolean result);
	}
	
	public interface onReceiveListener {
		public void onReceive(byte[] data, int length);
	}
	
	public P2PManager(Context context, String remoteIPAddress) {
		mContext = context;
		mRemoteIPAddress = remoteIPAddress;
		mHandler = new Handler();
	}
	
    public static P2PManager getInstance(Context context) {
    	 if (instance == null) {
            	SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
         	String address = sharedPreference.getString(LoveSetting.KEY_PREF_REMOTE_IP_ADDRESS, "");

         	instance = new P2PManager(context, address);

         	OnSharedPreferenceChangeListener prefListener = new OnSharedPreferenceChangeListener() {
                 @Override
                 public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                 	if (key.equals(LoveSetting.KEY_PREF_REMOTE_IP_ADDRESS)) {
                     	String address = sharedPreferences.getString(LoveSetting.KEY_PREF_REMOTE_IP_ADDRESS, "");
                     	instance.updateRemoteIPAddress(address);
                 	}
                 }
             };
         	
            	sharedPreference.registerOnSharedPreferenceChangeListener(prefListener);        	        	
         }

         return instance;
    }
    public void updateRemoteIPAddress(String remoteIPAddress) {
    	mRemoteIPAddress = remoteIPAddress;
    }
    
	
    public void send(byte[] data, Callback callback) {
    	new SenderThread(data, callback).start();
    }

    public void registerListener(onReceiveListener listener) {
    	synchronized (mListenerList) {
    		mListenerList.add(listener);
    	
        	if (mListenerList.size() == 1) {
        		mReceiver= new BroadcastReceiver() {
    		        @Override
    		        public void onReceive(Context context, Intent intent) {
    		        	String action = intent.getAction();
    		        	if (action.equals(P2PReceiverService.ACTION_DATA_RECEIVED)) {
    		        		byte[] data = intent.getByteArrayExtra(P2PReceiverService.EXTRA_RECEIVED_DATA);
    		        		int len = intent.getIntExtra(P2PReceiverService.EXTRA_RECEIVED_DATA_LEN, 0);
    	
    		        		
    		        		for (onReceiveListener listener : mListenerList) {
    		        			listener.onReceive(data, len);
    		        		}
    		        	}
    		        }
    		    };
    	
    		    IntentFilter intentFilter = new IntentFilter();
    	        intentFilter.addAction(P2PReceiverService.ACTION_DATA_RECEIVED);
    	        mContext.registerReceiver(mReceiver, intentFilter); 	

        		mContext.startService((new Intent(P2PReceiverService.ACTION_START_SERVER,
        				null, mContext, P2PReceiverService.class)));
        	}
    	}
   	}
    
	public void unregisterListener(onReceiveListener listener) {
    	synchronized (mListenerList) {
    		mListenerList.remove(listener);
    		
    		if (mListenerList.size() == 0) {
    			mContext.unregisterReceiver(mReceiver);
    			mReceiver = null;

    			mContext.startService((new Intent(P2PReceiverService.ACTION_STOP_SERVER,
    					null, mContext, P2PReceiverService.class)));
    		}
    	}
	}

	private class SenderThread extends Thread {
    	private byte[] mData;
    	private Callback mCallback;
    	
    	public SenderThread(byte[] data, Callback callback) {
    		super("SenderThread");
    		mData = data;
    		mCallback = callback;
    	}
    	
    	@Override
    	public void run() {
    		boolean result = true;
    		Socket socket = null;
    		
    		try {
    			socket = new Socket(mRemoteIPAddress, PORT_NUMER);
    			OutputStream output = socket.getOutputStream();

    			output.write(mData);
    		} catch (IOException e) {
    			result = false;
    			Log.d(TAG, "SenderThread e" + e);
    		} finally {
    			try {
    				if (socket != null) {
    					socket.close();
    				}
    			} catch (IOException e) {
    				
    			}
    		}

    		/* Make sure the callback is running in the same thread as the send() interface's caller */
    		final boolean tmpResult = result;
    		
    		if (mCallback != null) {
	    		mHandler.post(new Runnable () {
	    			public void run () {
	    				mCallback.onSent(tmpResult);
	    			}
	    		});
    		}
    	}
    }
}