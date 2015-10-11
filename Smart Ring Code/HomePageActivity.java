package com.broadcom.smartring.activities;

import com.broadcom.smartring.views.*;
import com.broadcom.smartring.services.*;
import com.broadcom.wicedsmart.*;
import android.view.*;
import android.annotation.*;
import java.util.*;
import com.broadcom.smartring.utils.*;
import android.preference.*;
import android.content.*;
import android.util.*;
import android.os.*;
import com.broadcom.smartring.*;
import android.app.*;
import android.widget.*;

public class HomePageActivity extends ListActivity implements View$OnClickListener
{
    private static final int BATTERY_LEVEL_ZERO = 0;
    private static final int DIALOG_ID_OTA_UPGRAGE = 1;
    private static final int DIALOG_ID_OTA_UPGRAGE_PROGRESS = 2;
    public static final String EXTRAS_DEVICE_ADDRESS = "extra_device_address";
    public static final String EXTRAS_DEVICE_NAME = "extra_device_name";
    private static final int ID_CALL_CONFIG = 0;
    private static final int ID_CLOCK_CONFIG = 2;
    private static final int ID_FIND_ME = 5;
    private static final int ID_LOVE_CONFIG = 3;
    private static final int ID_MASTER_CONFIG = 6;
    private static final int ID_PEDOMETER_START = 7;
    private static final int ID_SMS_CONFIG = 1;
    private static final int ID_SOS_CONFIG = 4;
    private static final int ID_UV_CHECKER = 8;
    private static final int REQUEST_CODE_RENAME = 1000;
    private static final int RINGTONE_DURATION = 1000;
    private static final String TAG = "HomePageActivity";
    private static final int VIBRATE_DURATION = 1000;
    private Button mButtonConnect;
    private Button mButtonDeviceInfo;
    private ImageButton mButtonDeviceList;
    private Button mButtonDeviceName;
    private Button mButtonFindRing;
    private Button mButtonUpgrade;
    private String mDeviceAddress;
    private String mDeviceName;
    private ImageView mImageViewConnState;
    private boolean mIsConnected;
    private SimpleAdapter mListItemAdapter;
    private ArrayList<HashMap<String, Object>> mListItems;
    private int mOtaMaxProgress;
    private AlertDialog mOtaUpgradeDialog;
    private OtaUpgrader mOtaUpgrader;
    private EditText mPatchPathEditor;
    private ProgressBar mProgressbarBattery;
    private SlideMenu mSlideMenu;
    private SmartRingService.Listener mSmartRingListener;
    private SmartRingService mSmartRingService;
    private TextView mTextDeviceAddress;
    private ProgressDialog mUpgradeProgressDialog;
    
    public HomePageActivity() {
        this.mIsConnected = false;
        this.mSmartRingListener = new SmartRingService.Listener() {
            @Override
            public void onCharacteristicChanged(final int n, final Object o) {
                switch (n) {
                    default: {}
                    case 2: {
                        HomePageActivity.this.updateBatteryLevelUI((int)o);
                    }
                }
            }
            
            @Override
            public void onConnectionStateChange(final int n, final int n2) {
                boolean b = true;
                final boolean b2 = n2 == 2 && b;
                if (n == 0) {
                    b = false;
                }
                HomePageActivity.this.processConnectionStateChanged(b2, b);
            }
        };
    }
    
    static /* synthetic */ void access$10(final HomePageActivity homePageActivity, final OtaUpgrader mOtaUpgrader) {
        homePageActivity.mOtaUpgrader = mOtaUpgrader;
    }
    
    static /* synthetic */ void access$12(final HomePageActivity homePageActivity, final int mOtaMaxProgress) {
        homePageActivity.mOtaMaxProgress = mOtaMaxProgress;
    }
    
    static /* synthetic */ void access$4(final HomePageActivity homePageActivity, final boolean mIsConnected) {
        homePageActivity.mIsConnected = mIsConnected;
    }
    
    static /* synthetic */ void access$9(final HomePageActivity homePageActivity, final ProgressDialog mUpgradeProgressDialog) {
        homePageActivity.mUpgradeProgressDialog = mUpgradeProgressDialog;
    }
    
    @SuppressLint({ "InflateParams" })
    private AlertDialog createOtaUpgradeDialog() {
        final AlertDialog$Builder setNegativeButton = new AlertDialog$Builder((Context)this, 3).setTitle(2131427359).setPositiveButton(2131427360, (DialogInterface$OnClickListener)new DialogInterface$OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int n) {
                if (HomePageActivity.this.mOtaUpgrader == null) {
                    HomePageActivity.access$10(HomePageActivity.this, new OtaNoSecureUpgrader((Context)HomePageActivity.this, HomePageActivity.this.mDeviceAddress, HomePageActivity.this.mPatchPathEditor.getText().toString(), new OtaUpgrader.Callback() {
                        @Override
                        public void onFinish(final int n) {
                            HomePageActivity.this.runOnUiThread((Runnable)new Runnable() {
                                private final /* synthetic */ int val$resId = HomePageActivity.this.getUpgradeStatusStr(n);
                                
                                @Override
                                public void run() {
                                    HomePageActivity.access$9(HomePageActivity.this, null);
                                    HomePageActivity.this.removeDialog(2);
                                    Toast.makeText((Context)HomePageActivity.this, this.val$resId, 0).show();
                                }
                            });
                            HomePageActivity.access$10(HomePageActivity.this, null);
                        }
                        
                        @Override
                        public void onProgress(final int n, final int n2) {
                            if (HomePageActivity.this.mUpgradeProgressDialog != null) {
                                HomePageActivity.this.runOnUiThread((Runnable)new Runnable() {
                                    @Override
                                    public void run() {
                                        HomePageActivity.this.mUpgradeProgressDialog.setProgress(n);
                                    }
                                });
                            }
                        }
                    }));
                    HomePageActivity.access$12(HomePageActivity.this, HomePageActivity.this.mOtaUpgrader.getPatchSize());
                    HomePageActivity.this.mOtaUpgrader.start();
                }
                HomePageActivity.this.showDialog(2);
            }
        }).setNegativeButton(2131427361, (DialogInterface$OnClickListener)new DialogInterface$OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int n) {
                Toast.makeText((Context)HomePageActivity.this, 2131427367, 0).show();
            }
        });
        final View inflate = this.getLayoutInflater().inflate(2130903074, (ViewGroup)null);
        setNegativeButton.setView(inflate);
        this.mPatchPathEditor = (EditText)inflate.findViewById(2131099740);
        return setNegativeButton.create();
    }
    
    private ProgressDialog createUpgradeProgressDialog() {
        final ProgressDialog progressDialog = new ProgressDialog((Context)this);
        progressDialog.setTitle(2131427362);
        progressDialog.setProgressStyle(1);
        progressDialog.setMax(this.mOtaMaxProgress);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.setButton(-2, (CharSequence)this.getString(2131427361), (DialogInterface$OnClickListener)new DialogInterface$OnClickListener() {
            public void onClick(final DialogInterface dialogInterface, final int n) {
                if (HomePageActivity.this.mOtaUpgrader != null) {
                    HomePageActivity.this.mOtaUpgrader.stop();
                }
            }
        });
        return progressDialog;
    }
    
    private int getUpgradeStatusStr(final int n) {
        switch (n) {
            default: {
                return 2131427366;
            }
            case 0: {
                return 2131427365;
            }
            case 133: {
                return 2131427367;
            }
        }
    }
    
    private void initBackgroundView() {
        this.mSlideMenu = (SlideMenu)this.findViewById(2131099711);
        this.mButtonConnect = (Button)this.findViewById(2131099757);
        this.mButtonDeviceInfo = (Button)this.findViewById(2131099758);
        this.mButtonUpgrade = (Button)this.findViewById(2131099759);
        this.mButtonDeviceList = (ImageButton)this.findViewById(2131099747);
        this.mImageViewConnState = (ImageView)this.findViewById(2131099749);
        this.mProgressbarBattery = (ProgressBar)this.findViewById(2131099750);
        this.mButtonDeviceName = (Button)this.findViewById(2131099712);
        this.mTextDeviceAddress = (TextView)this.findViewById(2131099713);
        this.mButtonFindRing = (Button)this.findViewById(2131099714);
        this.mProgressbarBattery.setMax(100);
        this.mButtonConnect.setOnClickListener((View$OnClickListener)this);
        this.mButtonDeviceInfo.setOnClickListener((View$OnClickListener)this);
        this.mButtonUpgrade.setOnClickListener((View$OnClickListener)this);
        this.mButtonDeviceList.setOnClickListener((View$OnClickListener)this);
        this.mButtonFindRing.setOnClickListener((View$OnClickListener)this);
        this.mButtonDeviceName.setOnClickListener((View$OnClickListener)this);
        this.mTextDeviceAddress.setText((CharSequence)this.mDeviceAddress);
        if (this.mDeviceName != null) {
            this.mButtonDeviceName.setText((CharSequence)this.mDeviceName);
            return;
        }
        this.mButtonDeviceName.setText(2131427348);
    }
    
    private void initDevice() {
        final Intent intent = this.getIntent();
        this.mDeviceName = intent.getStringExtra("extra_device_name");
        this.mDeviceAddress = intent.getStringExtra("extra_device_address");
    }
    
    private void initListView() {
        this.mListItems = new ArrayList<HashMap<String, Object>>();
        final HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
        hashMap.put("ItemTitle", (Integer)this.getString(2131427426));
        hashMap.put("ItemImage", 2130837611);
        this.mListItems.add((HashMap<String, Object>)hashMap);
        final HashMap<String, Integer> hashMap2 = new HashMap<String, Integer>();
        hashMap2.put("ItemTitle", (Integer)this.getString(2131427427));
        hashMap2.put("ItemImage", 2130837617);
        this.mListItems.add((HashMap<String, Object>)hashMap2);
        final HashMap<String, Integer> hashMap3 = new HashMap<String, Integer>();
        hashMap3.put("ItemTitle", (Integer)this.getString(2131427428));
        hashMap3.put("ItemImage", 2130837612);
        this.mListItems.add((HashMap<String, Object>)hashMap3);
        final HashMap<String, Integer> hashMap4 = new HashMap<String, Integer>();
        hashMap4.put("ItemTitle", (Integer)this.getString(2131427429));
        hashMap4.put("ItemImage", 2130837614);
        this.mListItems.add((HashMap<String, Object>)hashMap4);
        final HashMap<String, Integer> hashMap5 = new HashMap<String, Integer>();
        hashMap5.put("ItemTitle", (Integer)this.getString(2131427430));
        hashMap5.put("ItemImage", 2130837618);
        this.mListItems.add((HashMap<String, Object>)hashMap5);
        final HashMap<String, Integer> hashMap6 = new HashMap<String, Integer>();
        hashMap6.put("ItemTitle", (Integer)this.getString(2131427431));
        hashMap6.put("ItemImage", 2130837613);
        this.mListItems.add((HashMap<String, Object>)hashMap6);
        final HashMap<String, Integer> hashMap7 = new HashMap<String, Integer>();
        hashMap7.put("ItemTitle", (Integer)this.getString(2131427432));
        hashMap7.put("ItemImage", 2130837615);
        this.mListItems.add((HashMap<String, Object>)hashMap7);
        final HashMap<String, Integer> hashMap8 = new HashMap<String, Integer>();
        hashMap8.put("ItemTitle", (Integer)this.getString(2131427433));
        hashMap8.put("ItemImage", 2130837616);
        this.mListItems.add((HashMap<String, Object>)hashMap8);
        final HashMap<String, Integer> hashMap9 = new HashMap<String, Integer>();
        hashMap9.put("ItemTitle", (Integer)this.getString(2131427434));
        hashMap9.put("ItemImage", 2130837619);
        this.mListItems.add((HashMap<String, Object>)hashMap9);
        this.setListAdapter((ListAdapter)(this.mListItemAdapter = new SimpleAdapter((Context)this, (List)this.mListItems, 2130903076, new String[] { "ItemTitle", "ItemImage" }, new int[] { 2131099745, 2131099744 })));
    }
    
    private void initView() {
        this.getWindow().setFeatureInt(7, 2130903079);
        this.initBackgroundView();
        this.initListView();
    }
    
    private void processConnectionStateChanged(final boolean mIsConnected, final boolean b) {
        this.mIsConnected = mIsConnected;
        this.runOnUiThread((Runnable)new Runnable() {
            @Override
            public void run() {
                final Context applicationContext = HomePageActivity.this.getApplicationContext();
                if (HomePageActivity.this.mIsConnected) {
                    HomePageActivity.this.upgratedConnectedUi();
                    Utils.cancelDisconnectedNotification(applicationContext);
                    Utils.vibrate(applicationContext, 1000L);
                    return;
                }
                HomePageActivity.this.upgrateDisconnectedUi();
                final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                final boolean boolean1 = defaultSharedPreferences.getBoolean("pref_key_mobile_linkloss_vib", true);
                final boolean boolean2 = defaultSharedPreferences.getBoolean("pref_key_mobile_linkloss_ntf", true);
                final String string = defaultSharedPreferences.getString("pref_key_mobile_linkloss_tone", "");
                if (boolean1) {
                    Utils.vibrate(applicationContext, 1000L);
                }
                if (boolean2) {
                    Utils.showDisconnectedNotification(applicationContext);
                }
                Utils.playRingTone(applicationContext, string, 1000L);
                HomePageActivity.this.finish();
            }
        });
    }
    
    private void startDeviceAlarmConfigActivity(final int n) {
        final Intent intent = new Intent((Context)this, (Class)DeviceAlarmConfigActivity.class);
        intent.putExtra("alarm_type", n);
        this.startActivity(intent);
    }
    
    private void updateBatteryLevelUI(final int n) {
        Log.d("HomePageActivity", "updateBatteryLevelUI, batteryLevel = " + n);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HomePageActivity.this.mProgressbarBattery.setProgress(n);
                HomePageActivity.this.mProgressbarBattery.setProgress(n);
            }
        }).start();
    }
    
    protected void onActivityResult(final int n, final int n2, final Intent intent) {
        if (n == 1000 && n2 == -1) {
            this.mButtonDeviceName.setText((CharSequence)intent.getStringExtra("extra_new_name"));
        }
    }
    
    public void onBackPressed() {
        final Intent intent = new Intent("android.intent.action.MAIN");
        intent.setFlags(268435456);
        intent.addCategory("android.intent.category.HOME");
        this.startActivity(intent);
    }
    
    public void onClick(final View view) {
        this.mSlideMenu.closeMenu();
        switch (view.getId()) {
            default: {}
            case 2131099757: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (HomePageActivity.this.mIsConnected) {
                            HomePageActivity.this.mSmartRingService.disconnect();
                            HomePageActivity.access$4(HomePageActivity.this, false);
                        }
                    }
                }).start();
                this.finish();
            }
            case 2131099758: {
                this.startActivity(new Intent((Context)this, (Class)DeviceInfoActivity.class));
            }
            case 2131099759: {
                this.showDialog(1);
            }
            case 2131099747: {
                if (this.mSlideMenu.isMainScreenShowing()) {
                    this.mSlideMenu.openMenu();
                    return;
                }
                this.mSlideMenu.closeMenu();
            }
            case 2131099714: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HomePageActivity.this.mSmartRingService.findRing();
                    }
                }).start();
            }
            case 2131099712: {
                final Intent intent = new Intent((Context)this, (Class)RenameActivity.class);
                intent.putExtra("extra_name", this.mDeviceName);
                this.startActivityForResult(intent, 1000);
            }
        }
    }
    
    protected void onCreate(final Bundle bundle) {
        this.requestWindowFeature(7);
        super.onCreate(bundle);
        this.setContentView(2130903066);
        this.initDevice();
        this.initView();
        this.mSmartRingService = SmartRingApp.getApplication().getSmartRingService();
    }
    
    public Dialog onCreateDialog(final int n) {
        switch (n) {
            default: {
                return null;
            }
            case 1: {
                return (Dialog)(this.mOtaUpgradeDialog = this.createOtaUpgradeDialog());
            }
            case 2: {
                return (Dialog)(this.mUpgradeProgressDialog = this.createUpgradeProgressDialog());
            }
        }
    }
    
    protected void onListItemClick(final ListView listView, final View view, final int n, final long n2) {
        super.onListItemClick(listView, view, n, n2);
        switch ((int)n2) {
            default: {}
            case 0: {
                this.startDeviceAlarmConfigActivity(0);
            }
            case 1: {
                this.startDeviceAlarmConfigActivity(1);
            }
            case 2: {
                this.startDeviceAlarmConfigActivity(2);
            }
            case 3: {
                this.startActivity(new Intent((Context)this, (Class)LoveConfigActivity.class));
            }
            case 4: {
                this.startActivity(new Intent((Context)this, (Class)SOSConfigActivity.class));
            }
            case 5: {
                this.startActivity(new Intent((Context)this, (Class)FindMeActivity.class));
            }
            case 6: {
                this.startActivity(new Intent((Context)this, (Class)MasterConfigActivity.class));
            }
            case 7: {
                this.startActivity(new Intent((Context)this, (Class)PedometerStartActivity.class));
            }
            case 8: {
                this.startActivity(new Intent((Context)this, (Class)UVcheckerActivity.class));
            }
        }
    }
    
    protected void onPause() {
        super.onPause();
        if (this.mSmartRingService != null) {
            this.mSmartRingService.unregisterListener(this.mSmartRingListener);
        }
    }
    
    protected void onResume() {
        super.onResume();
        if (this.mSmartRingService != null) {
            this.mSmartRingService.registerListener(this.mSmartRingListener);
        }
    }
    
    protected void upgrateDisconnectedUi() {
        this.mProgressbarBattery.setProgress(0);
        this.mImageViewConnState.setImageResource(2130837622);
        this.mButtonConnect.setText(2131427350);
    }
    
    protected void upgratedConnectedUi() {
        this.mImageViewConnState.setImageResource(2130837621);
        this.mButtonConnect.setText(2131427377);
        this.mProgressbarBattery.setProgress(this.mSmartRingService.getBatteryLevel());
    }
}
