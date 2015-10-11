package com.example.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;



public class P2PReceiverService extends Service {
	public static final String ACTION_START_SERVER = "com.example.receiver.START_SERVER";
	public static final String ACTION_STOP_SERVER = "com.example.receiver.STOP_SERVER";
	
	public static final String ACTION_DATA_RECEIVED = "com.example.receiver.DATA_RECEIVED";
	
	public static final String EXTRA_RECEIVED_DATA = "receivedData";
	public static final String EXTRA_RECEIVED_DATA_LEN = "receivedDataLen";
	
	private static final String TAG = "P2PReceiverService";

	private static final int MAX_DATA_LEN = 1024;
	private ServiceHandler mServiceHandler;
	private ServerThread mServerThread;

	@Override
	public void onCreate() {
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        
        mServiceHandler = new ServiceHandler(thread.getLooper());
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Message msg = mServiceHandler.obtainMessage();
		
		msg.arg1 = startId;
		msg.obj = intent;
		
		mServiceHandler.sendMessage(msg);
        
		return Service.START_NOT_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    
	@Override
    public void onDestroy() {
        super.onDestroy();
        
        if (mServerThread != null) {
        	mServerThread.shutdown();
        	mServerThread = null;
        }
    }

    private void broadcastDataReceivedIntent(byte[] data, int length) {
    	Intent intent = new Intent(ACTION_DATA_RECEIVED);
    	intent.putExtra(EXTRA_RECEIVED_DATA, data);
    	intent.putExtra(EXTRA_RECEIVED_DATA_LEN, length);

    	sendBroadcast(intent);
    }
    
	private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
        	Intent intent = (Intent)msg.obj;
        	String action = intent.getAction();
        	
        	if (ACTION_START_SERVER.equals(action)) {
        		if (mServerThread == null) {
            		mServerThread = new ServerThread();
            		mServerThread.start();        			
        		}
        	} else if (ACTION_STOP_SERVER.equals(action)) {
        		mServerThread.shutdown();
            	mServerThread = null;
        	}
        }
	}
	
    private class ServerThread extends Thread {
    	private boolean mIsRunning = true;
       	private ServerSocket mServerSocket;
    	
        @Override
        public void run() {
           	try {
       			boolean isRunning;

       			mServerSocket = new ServerSocket(P2PManager.PORT_NUMER);
       			
       			synchronized (this) {
           			isRunning = mIsRunning;
           		}

       			while (isRunning) {
                	try {
                		Socket clientSocket = mServerSocket.accept();
                		
                		Log.d(TAG, "Accept : " + clientSocket);

                		if (clientSocket != null) {
                		    new ReceiverThread(clientSocket).start();
                		}
                    } catch (IOException e) {
                        Log.e(TAG, "ServerThread clientSocket e : " + e);
                    }

                	synchronized (this) {
               			isRunning = mIsRunning;
               		}
       			}
           	} catch (IOException e) {
                Log.e(TAG, "ServerThread serverSocket e : " + e);
            }
        }
        
        public void shutdown() {
        	synchronized (this) {
        		mIsRunning = false;

        		if (mServerSocket != null) {
        			try {
        				mServerSocket.close();
        			} catch (IOException e) {
        			}
        		}
        	}
        }
    }

    private class ReceiverThread extends Thread {
    	private Socket mSocket;
    	
    	public ReceiverThread(Socket socket) {
    		super("ReceiverThread");
    		mSocket = socket;
    	}
    	
    	@Override
    	public void run() {
            InputStream input = null;

            try {
            	input = mSocket.getInputStream();

            	byte[] data = new byte[MAX_DATA_LEN];
            	
            	int len = input.read(data);
            	broadcastDataReceivedIntent(data, len);
            } catch (IOException e) {           	
            } finally {
            	try {
            		mSocket.close();
            	} catch (IOException e) {
            	}

            	try {
            		if (input != null) {
            			input.close();
            		}
            	} catch (IOException e) {
            	}
            }
    	}
    }
}

