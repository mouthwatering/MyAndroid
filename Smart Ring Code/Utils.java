package com.broadcom.smartring.utils;

import android.net.*;
import android.content.*;
import android.media.*;
import com.android.internal.telephony.*;
import android.util.*;
import java.lang.reflect.*;
import android.telephony.*;
import java.util.*;
import com.amap.api.location.*;
import android.location.*;
import android.app.*;
import android.support.v4.app.*;
import android.os.*;

public final class Utils
{
    private static final int NOTIFICATION_ID = 0;
    public static final String TAG = "SmartRingUtils";
    static AMapLocationListener mLocationListener;
    private static LocationManagerProxy mLocationManagerProxy;
    static Notification mNotification;
    static NotificationManager mNotificationManager;
    
    static {
        Utils.mLocationListener = null;
    }
    
    public static void cancelDisconnectedNotification(final Context context) {
        ((NotificationManager)context.getSystemService("notification")).cancel(0);
    }
    
    public static void dial(final Context context, final String s) {
        final Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + s));
        intent.addFlags(268435456);
        context.startActivity(intent);
    }
    
    public static String getLocation(final Runnable runnable) {
        return null;
    }
    
    public static void playRingTone(final Context context, final String s, final long n) {
        final Ringtone ringtone = RingtoneManager.getRingtone(context, Uri.parse(s));
        ringtone.play();
        while (true) {
            try {
                Thread.sleep(n);
                ringtone.stop();
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
                continue;
            }
            break;
        }
    }
    
    public static void rejectCall(final Context context) {
        try {
            final TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService("phone");
            final Method declaredMethod = Class.forName(telephonyManager.getClass().getName()).getDeclaredMethod("getITelephony", (Class<?>[])new Class[0]);
            declaredMethod.setAccessible(true);
            ((ITelephony)declaredMethod.invoke(telephonyManager, new Object[0])).endCall();
        }
        catch (Exception ex) {
            Log.d("SmartRingUtils", ex.getMessage());
        }
    }
    
    public static void sendSms(final Context context, final String s, final String s2) {
        final SmsManager default1 = SmsManager.getDefault();
        default1.sendMultipartTextMessage(s, (String)null, default1.divideMessage(s2), (ArrayList)null, (ArrayList)null);
    }
    
    public static void sendSosSmsWithLocation(final Context context, final String s, final String s2) {
        (Utils.mLocationManagerProxy = LocationManagerProxy.getInstance(context)).setGpsEnable(false);
        Utils.mLocationManagerProxy.requestLocationData("lbs", 60000L, 15.0f, Utils.mLocationListener);
        Utils.mLocationListener = new AMapLocationListener() {
            private String getContentWithLocation(final AMapLocation aMapLocation) {
                String string = null;
                if (aMapLocation != null) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(aMapLocation.getAddress()).append("\n");
                    string = sb.toString();
                }
                return String.valueOf(s2) + " " + string;
            }
            
            public void onLocationChanged(final Location location) {
            }
            
            @Override
            public void onLocationChanged(final AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0) {
                    Utils.sendSms(context, s, this.getContentWithLocation(aMapLocation));
                }
                Utils.mLocationManagerProxy.removeUpdates(Utils.mLocationListener);
                Utils.mLocationManagerProxy.destroy();
            }
            
            public void onProviderDisabled(final String s) {
            }
            
            public void onProviderEnabled(final String s) {
                this.getContentWithLocation(null);
            }
            
            public void onStatusChanged(final String s, final int n, final Bundle bundle) {
            }
        };
    }
    
    public static void setSystemAlarm(final Context context, final Intent intent, final long n) {
        ((AlarmManager)context.getSystemService("alarm")).set(1, n + System.currentTimeMillis(), PendingIntent.getService(context, 0, intent, 1073741824));
    }
    
    public static void showDisconnectedNotification(final Context context) {
        Utils.mNotificationManager = (NotificationManager)context.getSystemService("notification");
        Utils.mNotification = new NotificationCompat.Builder(context).setSmallIcon(2130837633).setTicker(context.getString(2131427439)).setContentInfo(context.getString(2131427341)).setContentTitle(context.getString(2131427439)).setContentText(context.getString(2131427440)).setAutoCancel(true).setDefaults(-1).build();
        Utils.mNotificationManager.notify(0, Utils.mNotification);
    }
    
    public static void showImmediateAlertNotification(final Context context) {
        Utils.mNotificationManager = (NotificationManager)context.getSystemService("notification");
        Utils.mNotification = new NotificationCompat.Builder(context).setSmallIcon(2130837633).setTicker(context.getString(2131427441)).setContentInfo(context.getString(2131427341)).setContentTitle(context.getString(2131427441)).setContentText(context.getString(2131427442)).setAutoCancel(true).setDefaults(-1).build();
        Utils.mNotificationManager.notify(1, Utils.mNotification);
    }
    
    public static void vibrate(final Context context, final long n) {
        ((Vibrator)context.getSystemService("vibrator")).vibrate(n);
    }
}
