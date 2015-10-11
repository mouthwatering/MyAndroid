package com.broadcom.smartring.services;

import android.app.*;
import com.broadcom.wicedsmart.*;
import android.bluetooth.*;
import android.util.*;
import com.broadcom.smartring.utils.*;
import android.telephony.*;
import android.content.*;
import android.net.*;
import com.broadcom.smartring.activities.*;
import android.widget.*;
import java.util.*;
import com.broadcom.smartring.data.*;
import com.broadcom.smartring.*;
import android.os.*;

public class SmartRingService extends Service
{
    private static final int ALARM_COFIG_SIZE = 12;
    public static final int ALARM_CONFIG_TYPE_MASTER = 11;
    public static final int ALARM_TYPE_ALARM_CLOCK = 2;
    public static final int ALARM_TYPE_CALL = 0;
    public static final int ALARM_TYPE_LOVE_TRANSMIT = 3;
    public static final int ALARM_TYPE_SMS = 1;
    public static final int ALARM_TYPE_TOTAL = 4;
    public static final int ALERT_LEVEL_HIGH = 2;
    public static final int ALERT_LEVEL_LOW = 0;
    public static final int ALERT_LEVEL_MEDIUM = 1;
    public static final int CHARACTERISTIC_BATTERY_LEVEL = 2;
    public static final int CHARACTERISTIC_GESTURE = 3;
    public static final int CHARACTERISTIC_PEDOMETER_STATUS = 1;
    public static final int CHARACTERISTISC_UV_INDEX = 4;
    private static final String CLOCK_ALARM_ACTION = "com.android.deskclock.ALARM_ALERT";
    private static final boolean DEBUG = true;
    private static final byte[] DEFAULT_ALARM_VALUE;
    public static final int DEVICE_NAME_SIZE = 8;
    private static final int EVENT_CHARACTERISTIC_CHANGED = 3;
    private static final int EVENT_CONNECTION_STATE_CHANGED = 2;
    private static final int EVENT_QUIT = 255;
    private static final int EVENT_START_CMD = 1;
    private static final long FIND_ME_RINGTONE_DURATION = 3000L;
    private static final long FIND_ME_VIBRATE_DURATION = 2000L;
    public static final int GESTURE_FIND_MOBILE = 52;
    public static final int GESTURE_LOVE_TRANSMIT = 50;
    public static final int GESTURE_REJECT_CALL = 49;
    public static final int GESTURE_SOS = 51;
    private static final String RESTART_ACTION = "com.example.smartringdemo.RESTART";
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_DISCONNECTED = 0;
    private static final String TAG = "SmartRingService";
    private static final long TIME_ACTIVATE_SERVICE_ALARM = 3000000L;
    private static final long TIME_AUTO_CONNECT_AFTER_BOOT = 2000L;
    public static final UUID UUID_ALERT_LEVEL;
    public static final UUID UUID_BATTERY_LEVEL;
    public static final UUID UUID_BATTERY_SERVICE;
    public static final UUID UUID_DEVICE_INFO_SERVICE;
    public static final UUID UUID_IMMEDIATE_ALERT_SERVICE;
    public static final UUID UUID_LINK_LOSS_SERVICE;
    public static final UUID UUID_MANUFACTURER_NAME;
    public static final UUID UUID_MODEL_NUMBER;
    public static final UUID UUID_SMART_RING_ALARM_CONFIG;
    public static final UUID UUID_SMART_RING_ALARM_CONTROL;
    public static final UUID UUID_SMART_RING_DEVICE_NAME;
    public static final UUID UUID_SMART_RING_GESTURE;
    public static final UUID UUID_SMART_RING_PEDOMETER_CONFIG;
    public static final UUID UUID_SMART_RING_PEDOMETER_STATUS;
    public static final UUID UUID_SMART_RING_SERVICE;
    public static final UUID UUID_SMART_RING_UV_INDEX;
    public static final UUID UUID_SYSTEM_ID;
    public static final UUID UUID_TX_POWER_LEVEL;
    public static final UUID UUID_TX_POWER_SERVICE;
    private final IBinder mBinder;
    private Configs mConfigs;
    private BluetoothGattEx.Callback mGattCallback;
    private BluetoothGattEx mGattEx;
    private boolean mIsInCall;
    private ArrayList<ListenerInfo> mListenerInfoList;
    private P2PManager.onReceiveListener mLoveTransmitListener;
    private P2PManager mLoveTransmitManager;
    private SharedPreferences$OnSharedPreferenceChangeListener mPreferenceListener;
    private ServiceHandler mServiceHandler;
    
    static {
        UUID_SMART_RING_SERVICE = UUID.fromString("D0348A5F-A983-7283-AB45-FB865FF05B25");
        UUID_SMART_RING_ALARM_CONFIG = UUID.fromString("D5CCA00C-7A73-4A12-AE32-9F8026DD8097");
        UUID_SMART_RING_ALARM_CONTROL = UUID.fromString("452BC88E-4C7C-4447-ABE6-F225AB35CA46");
        UUID_SMART_RING_GESTURE = UUID.fromString("DC5B79F9-6AB0-45CF-92FE-BA0463EF5DE0");
        UUID_SMART_RING_PEDOMETER_CONFIG = UUID.fromString("751A6857-C9BB-402A-A53C-E9A694FA6B5D");
        UUID_SMART_RING_PEDOMETER_STATUS = UUID.fromString("7A18ECB8-E35F-44BA-8FD6-2F24045FC9B2");
        UUID_SMART_RING_DEVICE_NAME = UUID.fromString("2ACD6A1D-5B4B-469C-B52D-47FE693C7FD7");
        UUID_BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
        UUID_BATTERY_LEVEL = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");
        UUID_DEVICE_INFO_SERVICE = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
        UUID_MANUFACTURER_NAME = UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB");
        UUID_MODEL_NUMBER = UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB");
        UUID_SYSTEM_ID = UUID.fromString("00002A23-0000-1000-8000-00805F9B34FB");
        UUID_LINK_LOSS_SERVICE = UUID.fromString("00001803-0000-1000-8000-00805F9B34FB");
        UUID_IMMEDIATE_ALERT_SERVICE = UUID.fromString("00001802-0000-1000-8000-00805F9B34FB");
        UUID_ALERT_LEVEL = UUID.fromString("00002A06-0000-1000-8000-00805F9B34FB");
        UUID_TX_POWER_SERVICE = UUID.fromString("00001804-0000-1000-8000-00805F9B34FB");
        UUID_TX_POWER_LEVEL = UUID.fromString("00002A07-0000-1000-8000-00805F9B34FB");
        UUID_SMART_RING_UV_INDEX = UUID.fromString("70BB3A62-F697-4254-99C3-6B0D559C6D45");
        DEFAULT_ALARM_VALUE = new byte[4];
    }
    
    public SmartRingService() {
        this.mBinder = (IBinder)new LocalBinder();
        this.mListenerInfoList = new ArrayList<ListenerInfo>();
        this.mIsInCall = false;
        this.mPreferenceListener = (SharedPreferences$OnSharedPreferenceChangeListener)new SharedPreferences$OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String s) {
                if (s.equals("pref_key_remote_ip_address") && SmartRingService.this.mLoveTransmitManager != null) {
                    SmartRingService.this.mLoveTransmitManager.updateRemoteIPAddress(sharedPreferences.getString("pref_key_remote_ip_address", ""));
                }
            }
        };
        this.mGattCallback = new BluetoothGattEx.Callback() {
            @Override
            public void onCharacteristicChanged(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                Log.d("SmartRingService", "onConnectionStateChange, UUID = " + bluetoothGattCharacteristic.getUuid());
                SmartRingService.this.mServiceHandler.sendMessage(SmartRingService.this.mServiceHandler.obtainMessage(3, (Object)bluetoothGattCharacteristic));
            }
            
            @Override
            public void onConnectionStateChange(final int n, final int n2) {
                Log.d("SmartRingService", "onConnectionStateChange, status = " + n + ", newState = " + n2);
                SmartRingService.this.mServiceHandler.sendMessage(SmartRingService.this.mServiceHandler.obtainMessage(2, n, n2));
            }
        };
    }
    
    private boolean connectDefault() {
        return this.connect(this.mConfigs.getConnectedDeviceAddress());
    }
    
    private void findMobile() {
        if (this.mConfigs.isImmediateAlertVibrate()) {
            Utils.vibrate((Context)this, 2000L);
        }
        if (this.mConfigs.isImmediateAlertNotify()) {
            Utils.showImmediateAlertNotification((Context)this);
        }
        Utils.playRingTone((Context)this, this.mConfigs.getAlertRingTone(), 3000L);
    }
    
    private void handleBootCompleted() {
        this.mServiceHandler.postDelayed((Runnable)new Runnable() {
            @Override
            public void run() {
                SmartRingService.this.connectDefault();
            }
        }, 2000L);
    }
    
    private void handleCharacteristicChanged(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        final UUID uuid = bluetoothGattCharacteristic.getUuid();
        Log.d("SmartRingService", "handleCharacteristicChanged, uuid = " + uuid);
        Label_0086: {
            if (!SmartRingService.UUID_SMART_RING_PEDOMETER_STATUS.equals(uuid)) {
                break Label_0086;
            }
            Object o = PedometerStatus.fromBytes(bluetoothGattCharacteristic.getValue());
            int n = 1;
        Block_6_Outer:
            while (true) {
                final int n2 = n;
                synchronized (this.mListenerInfoList) {
                    for (final ListenerInfo listenerInfo : this.mListenerInfoList) {
                        listenerInfo.mHandler.post((Runnable)new Runnable() {
                            @Override
                            public void run() {
                                listenerInfo.mListener.onCharacteristicChanged(n2, o);
                            }
                        });
                    }
                    return;
                    // iftrue(Label_0158:, !SmartRingService.UUID_SMART_RING_GESTURE.equals((Object)uuid))
                    while (true) {
                        final int intValue = bluetoothGattCharacteristic.getIntValue(17, 0);
                        this.handleGesture(intValue);
                        o = intValue;
                        n = 3;
                        continue Block_6_Outer;
                        Label_0117: {
                            continue;
                        }
                    }
                    Label_0158: {
                        final boolean equals = SmartRingService.UUID_SMART_RING_UV_INDEX.equals(uuid);
                    }
                    o = null;
                    n = 0;
                    // iftrue(Label_0050:, !equals)
                    o = bluetoothGattCharacteristic.getIntValue(33, 0);
                    n = 4;
                    continue Block_6_Outer;
                    // iftrue(Label_0117:, !SmartRingService.UUID_BATTERY_LEVEL.equals((Object)uuid))
                    o = bluetoothGattCharacteristic.getIntValue(17, 0);
                    n = 2;
                }
            }
        }
    }
    
    private void handleClockAlarm() {
        if (this.mConfigs.isAlarmOn(2)) {
            this.setAlarm(2);
        }
    }
    
    private void handleConnected() {
        this.mConfigs.setConnectedDeviceAddress(this.mGattEx.getDeviceAddress());
        this.enableNotification(true);
    }
    
    private void handleConnectionStateChange(final int n, final int n2) {
        Log.d("SmartRingService", "handleConnectionStateChange, status = " + n + ", newState = " + n2);
        int hasService = 0;
    Label_0107_Outer:
        while (true) {
            while (true) {
                switch (n2) {
                    case 0: {
                        Label_0101: {
                            break Label_0101;
                            synchronized (this.mListenerInfoList) {
                                for (final ListenerInfo listenerInfo : this.mListenerInfoList) {
                                    listenerInfo.mHandler.post((Runnable)new Runnable() {
                                        @Override
                                        public void run() {
                                            listenerInfo.mListener.onConnectionStateChange(n, n2);
                                        }
                                    });
                                }
                                return;
                                while (true) {
                                    hasService = (this.mGattEx.hasService(SmartRingService.UUID_SMART_RING_SERVICE) ? 1 : 0);
                                    this.handleConnected();
                                    break;
                                    hasService = 1;
                                    break;
                                    hasService = 0;
                                    continue Label_0107_Outer;
                                }
                            }
                            // iftrue(Label_0064:, hasService == 0)
                            // iftrue(Label_0064:, n != 0)
                        }
                        break;
                    }
                    case 2: {
                        continue;
                    }
                }
                break;
            }
            Label_0064: {
                if (hasService != 0) {
                    continue;
                }
            }
            break;
        }
    }
    
    private void handleGesture(final int n) {
        switch (n) {
            default: {}
            case 52: {
                this.findMobile();
            }
            case 49: {
                Utils.rejectCall((Context)this);
                this.resetAlarm();
            }
            case 50: {
                this.transmitLove();
            }
            case 51: {
                this.handleSos();
            }
        }
    }
    
    private void handlePhoneStateChange() {
        final int callState = ((TelephonyManager)this.getSystemService("phone")).getCallState();
        Log.d("SmartRingService", "handlePhoneStateChange, callState = " + callState);
        switch (callState) {
            case 1: {
                this.mIsInCall = true;
                final boolean alarmOn = this.mConfigs.isAlarmOn(0);
                Log.d("SmartRingService", "handlePhoneStateChange, isAlarmCall = " + alarmOn);
                if (alarmOn) {
                    this.setAlarm(0);
                    return;
                }
                break;
            }
            case 0: {
                this.mIsInCall = false;
                this.resetAlarm();
            }
        }
    }
    
    private void handleSmsReceived() {
        if (this.mConfigs.isAlarmOn(1) && !this.mIsInCall) {
            this.setAlarm(1);
        }
    }
    
    private void handleSos() {
        final String sosNumber = this.mConfigs.getSosNumber();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String sosContent = SmartRingService.this.mConfigs.getSosContent();
                if (SmartRingService.this.mConfigs.isSendSosWithLocation()) {
                    Utils.sendSosSmsWithLocation((Context)SmartRingService.this, sosNumber, sosContent);
                    return;
                }
                Utils.sendSms((Context)SmartRingService.this, sosNumber, sosContent);
            }
        }).start();
        Utils.dial((Context)this, sosNumber);
    }
    
    private void handleUserPresent() {
        if (this.mConfigs.isAlarmOn(1)) {
            this.resetAlarm();
        }
    }
    
    private void initLoveTransmit() {
        this.mLoveTransmitManager = new P2PManager((Context)this, this.mConfigs.getRemoteIpAddress());
        this.mLoveTransmitListener = new P2PManager.onReceiveListener() {
            @Override
            public void onReceive(final byte[] array, final int n) {
                if (SmartRingService.this.mConfigs.isAlarmOn(2)) {
                    SmartRingService.this.setAlarm(3);
                }
                SmartRingService.this.showLoveMessage(array);
            }
        };
        this.mLoveTransmitManager.registerListener(this.mLoveTransmitListener);
    }
    
    private void setActivateServiceAlarm() {
        Utils.setSystemAlarm((Context)this, new Intent("com.example.smartringdemo.RESTART", (Uri)null, (Context)this, (Class)SmartRingService.class), 3000000L);
    }
    
    private void showLoveMessage(final byte[] array) {
        final String s = new String(array);
        final Intent intent = new Intent((Context)this, (Class)LoveShowActivity.class);
        intent.putExtra("text", s);
        intent.addFlags(268435456);
        this.getApplication().startActivity(intent);
    }
    
    private static char toHexChar(final int n) {
        if (n < 10) {
            return (char)(n + 48);
        }
        return (char)(-10 + (n + 65));
    }
    
    private void transmitLove() {
        this.transmitText(this.getString(2131427425));
    }
    
    private void transmitText(final String s) {
        this.mLoveTransmitManager.send(s.getBytes(), (P2PManager.Callback)new P2PManager.Callback() {
            @Override
            public void onSent(final boolean b) {
                Log.i("SmartRingService", "onSent result = " + b);
                if (!b) {
                    Toast.makeText(SmartRingService.this.getApplicationContext(), (CharSequence)"Love send error!", 0).show();
                }
            }
        });
    }
    
    public boolean connect(final String s) {
        final boolean connect = this.mGattEx.connect(s);
        boolean hasService = false;
        if (connect) {
            hasService = this.mGattEx.hasService(SmartRingService.UUID_SMART_RING_SERVICE);
            if (!hasService) {
                this.mGattEx.disconnect();
            }
        }
        return hasService;
    }
    
    public void disconnect() {
        this.mGattEx.disconnect();
    }
    
    public void enableNotification(final boolean b) {
        this.mGattEx.setCharacteristicNotification(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_GESTURE, b);
        this.mGattEx.setCharacteristicNotification(SmartRingService.UUID_BATTERY_SERVICE, SmartRingService.UUID_BATTERY_LEVEL, b);
    }
    
    public void enableUvNotification(final boolean b) {
        this.mGattEx.setCharacteristicNotification(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_UV_INDEX, b);
    }
    
    public void findRing() {
        Log.d("SmartRingService", "findRing");
        this.setImmediateAlertLevel(this.mConfigs.getImmediateAlertLevel());
    }
    
    public AlarmConfigItem getAlarmConfig(final int n) {
        Log.d("SmartRingService", "getAlarmConfig, type = " + n);
        AlarmConfigItem alarmConfigItem = null;
        if (n < 4) {
            final byte[] characteristic = this.mGattEx.readCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONFIG);
            alarmConfigItem = null;
            if (characteristic != null) {
                final int length = characteristic.length;
                alarmConfigItem = null;
                if (length == 12) {
                    alarmConfigItem = new AlarmConfigItem(characteristic[n]);
                }
            }
        }
        Log.d("SmartRingService", "getAlarmConfig, config = " + alarmConfigItem);
        return alarmConfigItem;
    }
    
    public AlarmConfigItem[] getAllAlarmConfig() {
        Log.d("SmartRingService", "getAllAlarmConfig");
        final byte[] characteristic = this.mGattEx.readCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONFIG);
        AlarmConfigItem[] array = null;
        if (characteristic != null) {
            final int length = characteristic.length;
            array = null;
            if (length == 12) {
                array = new AlarmConfigItem[4];
                for (int i = 0; i < 4; ++i) {
                    array[i] = new AlarmConfigItem(characteristic[i]);
                }
            }
        }
        Log.d("SmartRingService", "getAllAlarmConfig, configArray = " + Arrays.toString(array));
        return array;
    }
    
    public int getBatteryLevel() {
        Log.d("SmartRingService", "getBatteryLevel");
        return this.mGattEx.readCharacteristicInt(SmartRingService.UUID_BATTERY_SERVICE, SmartRingService.UUID_BATTERY_LEVEL, 17);
    }
    
    public int getConnectionState() {
        return this.mGattEx.getConnectionState();
    }
    
    public String getDeviceAddress() {
        return this.mGattEx.getDeviceAddress();
    }
    
    public int getImmediateAlertLevel() {
        Log.d("SmartRingService", "getImmdiateAlertLevel");
        return this.mGattEx.readCharacteristicInt(SmartRingService.UUID_IMMEDIATE_ALERT_SERVICE, SmartRingService.UUID_ALERT_LEVEL, 17);
    }
    
    public int getLinkLossAlertLevel() {
        Log.d("SmartRingService", "getLinkLossAlertLevel");
        return this.mGattEx.readCharacteristicInt(SmartRingService.UUID_LINK_LOSS_SERVICE, SmartRingService.UUID_ALERT_LEVEL, 17);
    }
    
    public String getManufacturerName() {
        Log.d("SmartRingService", "getManufacturerName");
        return this.mGattEx.readCharacteristicString(SmartRingService.UUID_DEVICE_INFO_SERVICE, SmartRingService.UUID_MANUFACTURER_NAME);
    }
    
    public MasterConfig getMasterConfig() {
        Log.d("SmartRingService", "getMasterConfig");
        final byte[] characteristic = this.mGattEx.readCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONFIG);
        Object o = null;
        if (characteristic != null) {
            o = new MasterConfig(characteristic[11]);
        }
        Log.d("SmartRingService", "getMasterConfig, config = " + o);
        return (MasterConfig)o;
    }
    
    public String getModelID() {
        Log.d("SmartRingService", "getModelID");
        return this.mGattEx.readCharacteristicString(SmartRingService.UUID_DEVICE_INFO_SERVICE, SmartRingService.UUID_MODEL_NUMBER);
    }
    
    public PedometerConfig getPedometerConfig() {
        Log.d("SmartRingService", "getPedometerConfig");
        final byte[] characteristic = this.mGattEx.readCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_PEDOMETER_CONFIG);
        Object fromBytes = null;
        if (characteristic != null) {
            fromBytes = PedometerConfig.fromBytes(characteristic);
        }
        Log.d("SmartRingService", "getPedometerConfig, config = " + fromBytes);
        return (PedometerConfig)fromBytes;
    }
    
    public PedometerStatus getPedometerStatus() {
        Log.d("SmartRingService", "getPedometerStatus");
        final byte[] characteristic = this.mGattEx.readCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_PEDOMETER_STATUS);
        Object fromBytes = null;
        if (characteristic != null) {
            fromBytes = PedometerStatus.fromBytes(characteristic);
        }
        Log.d("SmartRingService", "getPedometerStatus, status = " + fromBytes);
        return (PedometerStatus)fromBytes;
    }
    
    public String getSystemID() {
        Log.d("SmartRingService", "getSystemID");
        final byte[] characteristic = this.mGattEx.readCharacteristic(SmartRingService.UUID_DEVICE_INFO_SERVICE, SmartRingService.UUID_SYSTEM_ID);
        final StringBuilder sb = new StringBuilder();
        if (characteristic != null) {
            for (int i = 0; i < characteristic.length; ++i) {
                sb.append(toHexChar((0xF0 & characteristic[i]) >> 4));
                sb.append(toHexChar(0xF & characteristic[i]));
            }
        }
        return sb.toString();
    }
    
    public int getTxPowerLevel() {
        Log.d("SmartRingService", "getTxPowerLevel");
        return this.mGattEx.readCharacteristicInt(SmartRingService.UUID_TX_POWER_SERVICE, SmartRingService.UUID_TX_POWER_LEVEL, 17);
    }
    
    public IBinder onBind(final Intent intent) {
        return this.mBinder;
    }
    
    public void onCreate() {
        (this.mConfigs = SmartRingApp.getApplication().getConfigs()).registerOnSharedPreferenceChangeListener(this.mPreferenceListener);
        final HandlerThread handlerThread = new HandlerThread("SmartRingService");
        handlerThread.start();
        this.mServiceHandler = new ServiceHandler(handlerThread.getLooper());
        (this.mGattEx = new BluetoothGattEx((Context)this, this.mServiceHandler)).setAutoConnect(true, this.mConfigs.getConnectedDeviceAddress());
        this.mGattEx.setCallback(this.mGattCallback);
        this.initLoveTransmit();
        this.setActivateServiceAlarm();
    }
    
    public void onDestroy() {
        this.mGattEx.close();
        this.mServiceHandler.sendEmptyMessage(255);
        this.mConfigs.unregisterOnSharedPreferenceChangeListener(this.mPreferenceListener);
        this.mLoveTransmitManager.unregisterListener(this.mLoveTransmitListener);
    }
    
    public int onStartCommand(final Intent obj, final int n, final int arg1) {
        Log.d("SmartRingService", "onStartCommand, intent = " + obj);
        if (obj != null) {
            final Message obtainMessage = this.mServiceHandler.obtainMessage(1);
            obtainMessage.arg1 = arg1;
            obtainMessage.obj = obj;
            this.mServiceHandler.sendMessage(obtainMessage);
        }
        return 2;
    }
    
    public boolean onUnbind(final Intent intent) {
        return super.onUnbind(intent);
    }
    
    public void registerListener(final Listener listener) {
        final ListenerInfo listenerInfo = new ListenerInfo(new Handler(), listener);
        synchronized (this.mListenerInfoList) {
            this.mListenerInfoList.add(listenerInfo);
        }
    }
    
    public boolean resetAlarm() {
        Log.d("SmartRingService", "resetAlarm");
        return this.mGattEx.writeCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONTROL, SmartRingService.DEFAULT_ALARM_VALUE);
    }
    
    public boolean setAlarm(final int n) {
        Log.d("SmartRingService", "setAlarm, type = " + n);
        boolean writeCharacteristic = false;
        if (n < 4) {
            final byte[] copy = Arrays.copyOf(SmartRingService.DEFAULT_ALARM_VALUE, SmartRingService.DEFAULT_ALARM_VALUE.length);
            copy[0] = (byte)(0xFF & 1 << 7 - n);
            writeCharacteristic = this.mGattEx.writeCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONTROL, copy);
        }
        Log.d("SmartRingService", "setAlarm, ret = " + writeCharacteristic);
        return writeCharacteristic;
    }
    
    public boolean setAlarmConfig(final int n, final AlarmConfigItem alarmConfigItem) {
        Log.d("SmartRingService", "setAlarmConfig, type = " + n + ", config = " + alarmConfigItem);
        boolean writeCharacteristic = false;
        if (n < 4) {
            final byte[] characteristic = this.mGattEx.readCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONFIG);
            writeCharacteristic = false;
            if (characteristic != null) {
                final int length = characteristic.length;
                writeCharacteristic = false;
                if (length == 12) {
                    characteristic[n] = alarmConfigItem.toByte();
                    writeCharacteristic = this.mGattEx.writeCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONFIG, characteristic);
                }
            }
        }
        Log.d("SmartRingService", "setAlarmConfig, ret = " + writeCharacteristic);
        return writeCharacteristic;
    }
    
    public boolean setAllAlarmConfig(final AlarmConfigItem[] array) {
        Log.d("SmartRingService", "setAllAlarmConfig, configArray = " + Arrays.toString(array));
        final int length = array.length;
        boolean writeCharacteristic = false;
        if (length == 4) {
            final byte[] characteristic = this.mGattEx.readCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONFIG);
            writeCharacteristic = false;
            if (characteristic != null) {
                final int length2 = characteristic.length;
                writeCharacteristic = false;
                if (length2 == 12) {
                    for (int i = 0; i < 4; ++i) {
                        characteristic[i] = array[i].toByte();
                    }
                    writeCharacteristic = this.mGattEx.writeCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONFIG, characteristic);
                }
            }
        }
        return writeCharacteristic;
    }
    
    public boolean setDeviceName(final String s) {
        Log.d("SmartRingService", "setDeviceName");
        final byte[] bytes = s.getBytes();
        final byte[] array = new byte[8];
        int length = bytes.length;
        if (length > 8) {
            length = 8;
        }
        System.arraycopy(bytes, 0, array, 0, length);
        return this.mGattEx.writeCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_DEVICE_NAME, bytes);
    }
    
    public boolean setImmediateAlertLevel(final int n) {
        Log.d("SmartRingService", "setImmdiateAlertLevel" + n);
        return this.mGattEx.writeCharacteristic(SmartRingService.UUID_IMMEDIATE_ALERT_SERVICE, SmartRingService.UUID_ALERT_LEVEL, n);
    }
    
    public boolean setLinkLossAlertLevel(final int n) {
        Log.d("SmartRingService", "setLinkLossAlertLevel");
        return this.mGattEx.writeCharacteristic(SmartRingService.UUID_LINK_LOSS_SERVICE, SmartRingService.UUID_ALERT_LEVEL, n);
    }
    
    public boolean setMasterConfig(final MasterConfig masterConfig) {
        Log.d("SmartRingService", "setMasterConfig, config = " + masterConfig);
        final byte[] characteristic = this.mGattEx.readCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONFIG);
        boolean writeCharacteristic = false;
        if (characteristic != null) {
            final int length = characteristic.length;
            writeCharacteristic = false;
            if (length == 12) {
                characteristic[11] = masterConfig.toByte();
                writeCharacteristic = this.mGattEx.writeCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_ALARM_CONFIG, characteristic);
            }
        }
        Log.d("SmartRingService", "setMasterConfig, ret = " + writeCharacteristic);
        return writeCharacteristic;
    }
    
    public boolean setPedometerConfig(final PedometerConfig pedometerConfig) {
        Log.d("SmartRingService", "setPedometerConfig");
        return this.mGattEx.writeCharacteristic(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_PEDOMETER_CONFIG, pedometerConfig.toBytes());
    }
    
    public boolean setPedometerNotification(final boolean b) {
        return this.mGattEx.setCharacteristicNotification(SmartRingService.UUID_SMART_RING_SERVICE, SmartRingService.UUID_SMART_RING_PEDOMETER_STATUS, b);
    }
    
    public void unregisterListener(final Listener listener) {
        synchronized (this.mListenerInfoList) {
            for (final ListenerInfo listenerInfo : this.mListenerInfoList) {
                if (listenerInfo.mListener == listener) {
                    this.mListenerInfoList.remove(listenerInfo);
                }
            }
        }
    }
    
    public interface Listener
    {
        void onCharacteristicChanged(int p0, Object p1);
        
        void onConnectionStateChange(int p0, int p1);
    }
    
    private static final class ListenerInfo
    {
        public Handler mHandler;
        public Listener mListener;
        
        public ListenerInfo(final Handler mHandler, final Listener mListener) {
            this.mHandler = mHandler;
            this.mListener = mListener;
        }
    }
    
    public class LocalBinder extends Binder
    {
        public SmartRingService getService() {
            return SmartRingService.this;
        }
    }
    
    private class ServiceHandler extends Handler
    {
        public ServiceHandler(final Looper looper) {
            super(looper);
        }
        
        public void handleMessage(final Message message) {
            switch (message.what) {
                case 1: {
                    final String action = ((Intent)message.obj).getAction();
                    Log.d("SmartRingService", "handleMessage, action = " + action);
                    if (action == null) {
                        break;
                    }
                    if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                        SmartRingService.this.handleBootCompleted();
                        return;
                    }
                    if ("android.intent.action.PHONE_STATE".equals(action)) {
                        SmartRingService.this.handlePhoneStateChange();
                        return;
                    }
                    if ("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
                        SmartRingService.this.handleSmsReceived();
                        return;
                    }
                    if ("com.android.deskclock.ALARM_ALERT".equals(action)) {
                        SmartRingService.this.handleClockAlarm();
                        return;
                    }
                    if ("android.intent.action.USER_PRESENT".equals(action)) {
                        SmartRingService.this.handleUserPresent();
                        return;
                    }
                    if ("com.example.smartringdemo.RESTART".equals(action)) {
                        SmartRingService.this.setActivateServiceAlarm();
                        return;
                    }
                    break;
                }
                case 2: {
                    SmartRingService.this.handleConnectionStateChange(message.arg1, message.arg2);
                }
                case 3: {
                    SmartRingService.this.handleCharacteristicChanged((BluetoothGattCharacteristic)message.obj);
                }
                case 255: {
                    this.getLooper().quit();
                }
            }
        }
    }
}
