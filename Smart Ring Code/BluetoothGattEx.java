package com.broadcom.wicedsmart;

import android.content.*;
import android.util.*;
import android.bluetooth.*;
import java.util.*;
import android.os.*;

public class BluetoothGattEx
{
    private static final boolean DEBUG = true;
    private static final int EVENT_QUIT = 255;
    private static final int EVENT_SEND_REQUEST = 1;
    private static final int EVENT_SEND_RESPOND = 2;
    private static final int EVENT_TIMEOUT = 3;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_DISCONNECTED = 0;
    private static final String TAG = "BluetoothGattEx";
    private static final long TIME_HANDLE_REQUEST_TIMEOUT = 15000L;
    public static final UUID UUID_CLIENT_CONFIG_DESCRIPTOR;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private Callback mCallback;
    private Handler mCallerHandler;
    private Object mConnectLock;
    private volatile int mConnectionState;
    private Context mContext;
    private String mDefaultDeviceAddress;
    private final BluetoothGattCallback mGattCallback;
    private boolean mIsAutoConnect;
    private RequestHandler mRequestHandler;
    
    static {
        UUID_CLIENT_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
    }
    
    public BluetoothGattEx(final Context mContext, final Handler mCallerHandler) {
        this.mIsAutoConnect = false;
        this.mConnectionState = 0;
        this.mConnectLock = new Object();
        this.mGattCallback = new BluetoothGattCallback() {
            public void onCharacteristicChanged(final BluetoothGatt bluetoothGatt, final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                Log.d("BluetoothGattEx", "onCharacteristicChanged");
                BluetoothGattEx.this.handleCharacteristicChanged(bluetoothGattCharacteristic);
            }
            
            public void onCharacteristicRead(final BluetoothGatt bluetoothGatt, final BluetoothGattCharacteristic bluetoothGattCharacteristic, final int n) {
                Log.d("BluetoothGattEx", "onCharacteristicRead, status = " + n);
                BluetoothGattEx.this.sendRespond(1, n, bluetoothGattCharacteristic);
            }
            
            public void onCharacteristicWrite(final BluetoothGatt bluetoothGatt, final BluetoothGattCharacteristic bluetoothGattCharacteristic, final int n) {
                Log.d("BluetoothGattEx", "onCharacteristicWrite, status = " + n);
                BluetoothGattEx.this.sendRespond(2, n, bluetoothGattCharacteristic);
            }
            
            public void onConnectionStateChange(final BluetoothGatt bluetoothGatt, final int n, final int n2) {
                Log.d("BluetoothGattEx", "onConnectionStateChange, status = " + n + ", newState = " + n2);
                if (n2 == 2) {
                    BluetoothGattEx.this.mBluetoothGatt.discoverServices();
                }
                else if (n2 == 0) {
                    BluetoothGattEx.this.handleConnectionStateChange(n, 0);
                }
            }
            
            public void onDescriptorRead(final BluetoothGatt bluetoothGatt, final BluetoothGattDescriptor bluetoothGattDescriptor, final int n) {
                Log.d("BluetoothGattEx", "onDescriptorRead, status = " + n);
                BluetoothGattEx.this.sendRespond(3, n, bluetoothGattDescriptor);
            }
            
            public void onDescriptorWrite(final BluetoothGatt bluetoothGatt, final BluetoothGattDescriptor bluetoothGattDescriptor, final int n) {
                Log.d("BluetoothGattEx", "onDescriptorWrite, status = " + n);
                BluetoothGattEx.this.sendRespond(4, n, bluetoothGattDescriptor);
            }
            
            public void onServicesDiscovered(final BluetoothGatt bluetoothGatt, final int n) {
                Log.d("BluetoothGattEx", "onServicesDiscovered, status = " + n);
                BluetoothGattEx.this.handleConnectionStateChange(n, 2);
            }
        };
        this.mContext = mContext;
        this.mCallerHandler = mCallerHandler;
        final HandlerThread handlerThread = new HandlerThread("BluetoothGattEx");
        handlerThread.start();
        this.mRequestHandler = new RequestHandler(handlerThread.getLooper());
    }
    
    private void closeGatt() {
        Log.d("BluetoothGattEx", "closeGatt");
        if (this.mBluetoothGatt != null) {
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
        }
    }
    
    private boolean connectAsync(final String mBluetoothDeviceAddress) {
        Log.d("BluetoothGattEx", "connectAsync");
        if (mBluetoothDeviceAddress == null) {
            Log.w("BluetoothGattEx", "connectAsync, Unspecified address.");
            return false;
        }
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager)this.mContext.getSystemService("bluetooth");
            if (this.mBluetoothManager == null) {
                Log.e("BluetoothGattEx", "connectAsync, Unable to initialize BluetoothManager.");
                return false;
            }
        }
        if (this.mBluetoothAdapter == null) {
            this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
            if (this.mBluetoothAdapter == null) {
                Log.e("BluetoothGattEx", "connectAsync, Unable to obtain a BluetoothAdapter.");
                return false;
            }
        }
        final BluetoothDevice remoteDevice = this.mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
        if (remoteDevice == null) {
            Log.w("BluetoothGattEx", "connectAsync, Device not found.  Unable to connect.");
            return false;
        }
        if (this.mBluetoothGatt != null) {
            Log.d("BluetoothGattEx", "connectAsync, close previous Gatt.");
            this.mBluetoothGatt.close();
        }
        this.mBluetoothGatt = remoteDevice.connectGatt(this.mContext, false, this.mGattCallback);
        if (this.mBluetoothGatt == null) {
            Log.w("BluetoothGattEx", "connectAsync, Gatt Connect fail.");
            return false;
        }
        this.mBluetoothDeviceAddress = mBluetoothDeviceAddress;
        return true;
    }
    
    private boolean disconnectAsync() {
        Log.d("BluetoothGattEx", "disconnectAsync");
        if (this.mBluetoothAdapter == null || this.mBluetoothGatt == null) {
            Log.d("BluetoothGattEx", "disconnectAsync, not initialized");
            return true;
        }
        this.mBluetoothGatt.disconnect();
        return true;
    }
    
    private BluetoothGattCharacteristic getCharacteristic(final UUID uuid, final UUID uuid2) {
        final BluetoothGattService service = this.mBluetoothGatt.getService(uuid);
        Log.d("BluetoothGattEx", "getCharacteristic, service = " + service);
        if (service == null) {
            return null;
        }
        return service.getCharacteristic(uuid2);
    }
    
    private void handleCharacteristicChanged(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (this.mCallback != null) {
            if (this.mCallerHandler == null) {
                this.mCallback.onCharacteristicChanged(bluetoothGattCharacteristic);
                return;
            }
            this.mCallerHandler.post((Runnable)new Runnable() {
                @Override
                public void run() {
                    BluetoothGattEx.this.mCallback.onCharacteristicChanged(bluetoothGattCharacteristic);
                }
            });
        }
    }
    
    private void handleConnectionStateChange(final int n, final int n2) {
        Log.d("BluetoothGattEx", "handleConnectionStateChange, status = " + n + ", newState = " + n2);
        switch (n2) {
            case 0: {
                this.mConnectionState = 0;
                this.sendRespond(6, n, null);
                break;
            }
            case 2: {
                if (n == 0) {
                    this.mConnectionState = 2;
                }
                else {
                    this.mConnectionState = 0;
                }
                this.sendRespond(5, n, null);
                break;
            }
        }
        if (this.mCallback != null) {
            if (this.mCallerHandler == null) {
                this.mCallback.onConnectionStateChange(n, n2);
                return;
            }
            this.mCallerHandler.post((Runnable)new Runnable() {
                @Override
                public void run() {
                    BluetoothGattEx.this.mCallback.onConnectionStateChange(n, n2);
                }
            });
        }
    }
    
    private BluetoothGattCharacteristic readCharacteristicSync(final UUID uuid, final UUID uuid2) {
        Log.d("BluetoothGattEx", "readCharacteristicSync, srvUuid = " + uuid + ", charUuid = " + uuid2);
        if (!this.waitConnection()) {
            return null;
        }
        final BluetoothGattCharacteristic characteristic = this.getCharacteristic(uuid, uuid2);
        Log.d("BluetoothGattEx", "readCharacteristicSync, characteristic = " + characteristic);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = null;
        if (characteristic != null) {
            final GattRespond sendSyncRequest = this.sendSyncRequest(1, characteristic);
            bluetoothGattCharacteristic = null;
            if (sendSyncRequest != null) {
                final int mStatus = sendSyncRequest.mStatus;
                bluetoothGattCharacteristic = null;
                if (mStatus == 0) {
                    bluetoothGattCharacteristic = (BluetoothGattCharacteristic)sendSyncRequest.mData;
                }
            }
        }
        Log.d("BluetoothGattEx", "readCharacteristicSync, rspChar = " + bluetoothGattCharacteristic);
        return bluetoothGattCharacteristic;
    }
    
    private GattRequest sendRequest(final int n, final Object o) {
        Log.d("BluetoothGattEx", "sendRequest, type = " + n + ", data = " + o);
        final GattRequest gattRequest = new GattRequest(n, o);
        this.mRequestHandler.sendMessage(this.mRequestHandler.obtainMessage(1, (Object)gattRequest));
        return gattRequest;
    }
    
    private void sendRespond(final int arg1, final int arg2, final Object obj) {
        Log.d("BluetoothGattEx", "sendRespond, requestType = " + arg1 + ", status = " + arg2);
        final Message obtainMessage = this.mRequestHandler.obtainMessage(2);
        obtainMessage.arg1 = arg1;
        obtainMessage.arg2 = arg2;
        obtainMessage.obj = obj;
        this.mRequestHandler.sendMessage(obtainMessage);
    }
    
    private GattRespond sendSyncRequest(final int n, final Object o) {
        Log.d("BluetoothGattEx", "sendSyncRequest, type = " + n + ", data = " + o);
        final GattRequest sendRequest = this.sendRequest(n, o);
        synchronized (sendRequest) {
            Log.d("BluetoothGattEx", "sendSyncRequest, waiting");
            while (sendRequest.mResult == null) {
                Log.d("BluetoothGattEx", "sendSyncRequest, mResult is null");
                try {
                    sendRequest.wait();
                }
                catch (InterruptedException ex) {}
            }
            // monitorexit(sendRequest)
            return sendRequest.mResult;
        }
    }
    
    private boolean waitConnection() {
        while (true) {
            Log.d("BluetoothGattEx", "waitConnection, mConnectionState = " + this.mConnectionState);
            boolean connect = true;
            synchronized (this.mConnectLock) {
                Log.d("BluetoothGattEx", "waitConnection, checking mConnectionState = " + this.mConnectionState);
                if (this.mConnectionState == 0 && this.mIsAutoConnect) {
                    String s;
                    if (this.mBluetoothDeviceAddress != null) {
                        s = this.mBluetoothDeviceAddress;
                    }
                    else {
                        s = this.mDefaultDeviceAddress;
                    }
                    if (s == null) {
                        return false;
                    }
                    connect = this.connect(s);
                }
                return connect;
            }
            connect = false;
            return connect;
        }
    }
    
    public void close() {
        Log.d("BluetoothGattEx", "close()");
        this.mCallback = null;
        this.mRequestHandler.sendEmptyMessage(255);
    }
    
    public boolean connect(final String s) {
        boolean b = true;
        Log.d("BluetoothGattEx", "connect, address = " + s);
        synchronized (this.mConnectLock) {
            Log.d("BluetoothGattEx", "connect, sending request");
            this.mConnectionState = 1;
            final GattRespond sendSyncRequest = this.sendSyncRequest(5, s);
            if (sendSyncRequest == null || sendSyncRequest.mStatus != 0) {
                b = false;
            }
            // monitorexit(this.mConnectLock)
            Log.d("BluetoothGattEx", "connect, ret = " + b);
            return b;
        }
    }
    
    public void disconnect() {
        Log.d("BluetoothGattEx", "disconnect");
        synchronized (this.mConnectLock) {
            Log.d("BluetoothGattEx", "disconnect, sending request");
            this.sendRequest(6, null);
        }
    }
    
    public int getConnectionState() {
        return this.mConnectionState;
    }
    
    public String getDeviceAddress() {
        return this.mBluetoothDeviceAddress;
    }
    
    public boolean hasService(final UUID uuid) {
        final BluetoothGattService service = this.mBluetoothGatt.getService(uuid);
        Log.d("BluetoothGattEx", "hasService, service = " + service);
        return service != null;
    }
    
    public byte[] readCharacteristic(final UUID uuid, final UUID uuid2) {
        final BluetoothGattCharacteristic characteristicSync = this.readCharacteristicSync(uuid, uuid2);
        byte[] value = null;
        if (characteristicSync != null) {
            value = characteristicSync.getValue();
        }
        Log.d("BluetoothGattEx", "readCharacteristic, value[] = " + Arrays.toString(value));
        return value;
    }
    
    public int readCharacteristicInt(final UUID uuid, final UUID uuid2, final int n) {
        final BluetoothGattCharacteristic characteristicSync = this.readCharacteristicSync(uuid, uuid2);
        int intValue = 0;
        if (characteristicSync != null) {
            intValue = characteristicSync.getIntValue(n, 0);
        }
        Log.d("BluetoothGattEx", "readCharacteristicInt, value = " + intValue);
        return intValue;
    }
    
    public String readCharacteristicString(final UUID uuid, final UUID uuid2) {
        final BluetoothGattCharacteristic characteristicSync = this.readCharacteristicSync(uuid, uuid2);
        String stringValue = null;
        if (characteristicSync != null) {
            stringValue = characteristicSync.getStringValue(0);
        }
        Log.d("BluetoothGattEx", "readCharacteristicString, value = " + stringValue);
        return stringValue;
    }
    
    public void setAutoConnect(final boolean mIsAutoConnect, final String mDefaultDeviceAddress) {
        Log.d("BluetoothGattEx", "setAutoConnect, isAuto = " + mIsAutoConnect + ", defaultAddress = " + mDefaultDeviceAddress);
        this.mIsAutoConnect = mIsAutoConnect;
        this.mDefaultDeviceAddress = mDefaultDeviceAddress;
    }
    
    public void setCallback(final Callback mCallback) {
        this.mCallback = mCallback;
    }
    
    public boolean setCharacteristicNotification(final UUID uuid, final UUID uuid2, final boolean b) {
        Log.d("BluetoothGattEx", "setCharacteristicNotification");
        final BluetoothGattCharacteristic characteristic = this.getCharacteristic(uuid, uuid2);
        boolean setCharacteristicNotification = false;
        if (characteristic != null) {
            byte[] value;
            if (b) {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            }
            else {
                value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            }
            final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BluetoothGattEx.UUID_CLIENT_CONFIG_DESCRIPTOR);
            Log.d("BluetoothGattEx", "setCharacteristicNotification, descriptor = " + descriptor);
            setCharacteristicNotification = false;
            if (descriptor != null) {
                descriptor.setValue(value);
                final GattRespond sendSyncRequest = this.sendSyncRequest(4, descriptor);
                setCharacteristicNotification = false;
                if (sendSyncRequest != null) {
                    final int mStatus = sendSyncRequest.mStatus;
                    setCharacteristicNotification = false;
                    if (mStatus == 0) {
                        setCharacteristicNotification = this.mBluetoothGatt.setCharacteristicNotification(characteristic, b);
                    }
                }
            }
        }
        Log.d("BluetoothGattEx", "setCharacteristicNotification, ret = " + setCharacteristicNotification);
        return setCharacteristicNotification;
    }
    
    public boolean writeCharacteristic(final UUID uuid, final UUID uuid2, final int n) {
        Log.d("BluetoothGattEx", "writeCharacteristic, srvUuid = " + uuid + ", charUuid = " + uuid2 + ", value = " + n);
        if (!this.waitConnection()) {
            return false;
        }
        final BluetoothGattCharacteristic characteristic = this.getCharacteristic(uuid, uuid2);
        boolean b = false;
        if (characteristic != null) {
            characteristic.setValue(n, 17, 0);
            final GattRespond sendSyncRequest = this.sendSyncRequest(2, characteristic);
            b = false;
            if (sendSyncRequest != null) {
                final int mStatus = sendSyncRequest.mStatus;
                b = false;
                if (mStatus == 0) {
                    b = true;
                }
            }
        }
        Log.d("BluetoothGattEx", "writeCharacteristic, ret = " + b);
        return b;
    }
    
    public boolean writeCharacteristic(final UUID uuid, final UUID uuid2, final byte[] value) {
        Log.d("BluetoothGattEx", "writeCharacteristic, srvUuid = " + uuid + ", charUuid = " + uuid2 + ", value[] = " + Arrays.toString(value));
        if (!this.waitConnection()) {
            return false;
        }
        final BluetoothGattCharacteristic characteristic = this.getCharacteristic(uuid, uuid2);
        boolean b = false;
        if (characteristic != null) {
            characteristic.setValue(value);
            final GattRespond sendSyncRequest = this.sendSyncRequest(2, characteristic);
            b = false;
            if (sendSyncRequest != null) {
                final int mStatus = sendSyncRequest.mStatus;
                b = false;
                if (mStatus == 0) {
                    b = true;
                }
            }
        }
        Log.d("BluetoothGattEx", "writeCharacteristic, ret = " + b);
        return b;
    }
    
    public interface Callback
    {
        void onCharacteristicChanged(BluetoothGattCharacteristic p0);
        
        void onConnectionStateChange(int p0, int p1);
    }
    
    private static final class GattRequest
    {
        public static final int TYPE_CONNECT = 5;
        public static final int TYPE_DISCONNECT = 6;
        public static final int TYPE_READ_CHAR = 1;
        public static final int TYPE_READ_DESC = 3;
        public static final int TYPE_WRITE_CHAR = 2;
        public static final int TYPE_WRITE_DESC = 4;
        public final Object mData;
        public GattRespond mResult;
        public final int mType;
        
        public GattRequest(final int mType, final Object mData) {
            this.mResult = null;
            this.mType = mType;
            this.mData = mData;
        }
        
        @Override
        public String toString() {
            String s = "[ mType = " + this.mType;
            switch (this.mType) {
                case 1:
                case 2: {
                    s = String.valueOf(s) + ", UUID = " + ((BluetoothGattCharacteristic)this.mData).getUuid();
                    break;
                }
                case 3:
                case 4: {
                    s = String.valueOf(s) + ", UUID = " + ((BluetoothGattDescriptor)this.mData).getUuid();
                    break;
                }
                case 5: {
                    s = String.valueOf(s) + ", Device Address = " + (String)this.mData;
                    break;
                }
            }
            return String.valueOf(s) + " ]";
        }
    }
    
    private static final class GattRespond
    {
        public Object mData;
        public int mStatus;
        
        public GattRespond(final int mStatus, final Object mData) {
            this.mStatus = mStatus;
            this.mData = mData;
        }
        
        @Override
        public String toString() {
            return "[ mStatus = " + this.mStatus + " ]";
        }
    }
    
    private class RequestHandler extends Handler
    {
        private GattRequest mCurrRequest;
        private final ArrayList<GattRequest> mRequestQueue;
        
        public RequestHandler(final Looper looper) {
            super(looper);
            this.mRequestQueue = new ArrayList<GattRequest>();
            this.mCurrRequest = null;
        }
        
        private boolean execGattRequest(final GattRequest gattRequest) {
            boolean b = false;
            switch (gattRequest.mType) {
                default: {
                    b = false;
                    break;
                }
                case 1: {
                    b = BluetoothGattEx.this.mBluetoothGatt.readCharacteristic((BluetoothGattCharacteristic)gattRequest.mData);
                    break;
                }
                case 2: {
                    b = BluetoothGattEx.this.mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic)gattRequest.mData);
                    break;
                }
                case 3: {
                    b = BluetoothGattEx.this.mBluetoothGatt.readDescriptor((BluetoothGattDescriptor)gattRequest.mData);
                    break;
                }
                case 4: {
                    b = BluetoothGattEx.this.mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor)gattRequest.mData);
                    break;
                }
                case 5: {
                    b = BluetoothGattEx.this.connectAsync((String)gattRequest.mData);
                    break;
                }
                case 6: {
                    b = BluetoothGattEx.this.disconnectAsync();
                    break;
                }
            }
            Log.d("BluetoothGattEx", "handleGattRequest ret = " + b);
            return b;
        }
        
        private void handleRequest(final GattRequest gattRequest) {
            Log.d("BluetoothGattEx", "handleRequest, request = " + gattRequest);
            this.mRequestQueue.add(gattRequest);
            if (this.mCurrRequest == null) {
                this.nextGattRequest();
            }
        }
        
        private void handleRespond(final int n, final int n2, final Object o) {
            Log.d("BluetoothGattEx", "handleRespond, mCurrRequest = " + this.mCurrRequest + "requestType = " + n + ", status = " + n2);
            if (this.mCurrRequest != null) {
                final GattRequest mCurrRequest = this.mCurrRequest;
                final int mType = mCurrRequest.mType;
                int n3 = 0;
                Label_0313: {
                    if (mType == n) {
                        n3 = 0;
                        switch (n) {
                            case 1:
                            case 2: {
                                break Label_0313;
                            }
                            case 3:
                            case 4: {
                                break Label_0313;
                            }
                            case 5:
                            case 6: {
                                break Label_0313;
                            }
                        }
                    }
                Block_6_Outer:
                    while (true) {
                        if (n3 == 0) {
                            return;
                        }
                        this.removeMessages(3);
                        this.mCurrRequest = null;
                        mCurrRequest.mResult = new GattRespond(n2, o);
                        Log.d("BluetoothGattEx", "handleRespond, notify");
                        synchronized (mCurrRequest) {
                            mCurrRequest.notify();
                            // monitorexit(mCurrRequest)
                            this.nextGattRequest();
                            return;
                            // iftrue(Label_0116:, uuid != uuid2)
                            // iftrue(Label_0116:, uuid3 != uuid4)
                            Block_5: {
                                while (true) {
                                    n3 = 1;
                                    continue Block_6_Outer;
                                    final BluetoothGattCharacteristic bluetoothGattCharacteristic = (BluetoothGattCharacteristic)o;
                                    final BluetoothGattCharacteristic bluetoothGattCharacteristic2 = (BluetoothGattCharacteristic)mCurrRequest.mData;
                                    Log.d("BluetoothGattEx", "handleRespond, rspChar UUID = " + bluetoothGattCharacteristic.getUuid());
                                    final UUID uuid = bluetoothGattCharacteristic.getUuid();
                                    final UUID uuid2 = bluetoothGattCharacteristic2.getUuid();
                                    n3 = 0;
                                    break Block_5;
                                    final BluetoothGattDescriptor bluetoothGattDescriptor = (BluetoothGattDescriptor)o;
                                    final BluetoothGattDescriptor bluetoothGattDescriptor2 = (BluetoothGattDescriptor)mCurrRequest.mData;
                                    Log.d("BluetoothGattEx", "handleRespond, rspDesc UUID = " + bluetoothGattDescriptor.getUuid());
                                    final UUID uuid3 = bluetoothGattDescriptor.getUuid();
                                    final UUID uuid4 = bluetoothGattDescriptor2.getUuid();
                                    n3 = 0;
                                    continue;
                                }
                                n3 = 1;
                                continue Block_6_Outer;
                            }
                            n3 = 1;
                            continue Block_6_Outer;
                        }
                        break;
                    }
                }
            }
        }
        
        private void handleTimeout() {
            Log.d("BluetoothGattEx", "handleTimeout");
            synchronized (this.mCurrRequest) {
                this.mCurrRequest.mResult = new GattRespond(257, null);
                this.mCurrRequest.notify();
                // monitorexit(this.mCurrRequest)
                this.mCurrRequest = null;
                this.nextGattRequest();
            }
        }
        
        private void nextGattRequest() {
            Log.d("BluetoothGattEx", "nextGattRequest");
            while (true) {
                Log.d("BluetoothGattEx", "nextGattRequest RequestQueue size = " + this.mRequestQueue.size());
                if (this.mRequestQueue.size() <= 0) {
                    break;
                }
                final GattRequest mCurrRequest = this.mRequestQueue.remove(0);
                Log.d("BluetoothGattEx", "nextGattRequest request = " + mCurrRequest);
                if (this.execGattRequest(mCurrRequest)) {
                    this.mCurrRequest = mCurrRequest;
                    this.sendEmptyMessageDelayed(3, 15000L);
                    break;
                }
                mCurrRequest.mResult = new GattRespond(257, null);
                synchronized (mCurrRequest) {
                    mCurrRequest.notifyAll();
                }
            }
        }
        
        public void handleMessage(final Message message) {
            switch (message.what) {
                default: {}
                case 1: {
                    this.handleRequest((GattRequest)message.obj);
                }
                case 2: {
                    this.handleRespond(message.arg1, message.arg2, message.obj);
                }
                case 3: {
                    this.handleTimeout();
                }
                case 255: {
                    BluetoothGattEx.this.closeGatt();
                    this.getLooper().quit();
                }
            }
        }
    }
}
