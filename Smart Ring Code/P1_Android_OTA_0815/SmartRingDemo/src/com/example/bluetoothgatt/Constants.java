package com.example.bluetoothgatt;

import java.util.UUID;

public class Constants {
	/**
     * UUID of smart ring Service
     */
    public static final UUID UUID_SMART_RING_SERVICE= UUID
            .fromString("d0348a5f-a983-7283-ab45-fb865ff05b25");  
    /**
     * UUID of alarm setting
     */
    public static final UUID UUID_SMART_RING_ALARM_CONFIG= UUID
    		.fromString("D5CCA00C-7A73-4a12-AE32-9F8026DD8097");
    /**
     * UUID of alarm type
     */
    public static final UUID UUID_SMART_RING_ALARM=UUID
    		.fromString("452BC88E-4C7C-4447-ABE6-F225AB35CA46");
    /**
     * UUID of gesture
     */
    public static final UUID UUID_SMART_RING_GESTURE=UUID
    		.fromString("DC5B79F9-6AB0-45cf-92FE-BA0463EF5DE0");
    /**
     * UUID of descriptor
     */
    public static final UUID CLIENT_CONFIG_DESCRIPTOR_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");
    
    /**
     * UUID of battery service
     */
    public static final UUID BATTERY_SERVICE_UUID = UUID
            .fromString("0000180F-0000-1000-8000-00805f9b34fb");

    /**
     * UUID of battery level characteristic
     */
    public static final UUID BATTERY_LEVEL_UUID = UUID
            .fromString("00002a19-0000-1000-8000-00805f9b34fb");

    /**
     * UUID of device information service
     */
    public static final UUID DEVICE_INFO_SERVICE_UUID = UUID
            .fromString("0000180A-0000-1000-8000-00805f9b34fb");

    /**
     * UUID of manufacturer name characteristic
     */
    public static final UUID MANUFACTURER_NAME_UUID = UUID
            .fromString("00002A29-0000-1000-8000-00805f9b34fb");
    /**
     * UUID of model number characteristic
     */
    public static final UUID MODEL_NUMBER_UUID = UUID
            .fromString("00002A24-0000-1000-8000-00805f9b34fb");

    /**
     * UUID of system id characteristic
     */
    public static final UUID SYSTEM_ID_UUID = UUID
            .fromString("00002A23-0000-1000-8000-00805f9b34fb");

}
