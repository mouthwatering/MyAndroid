package com.example.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	private final static String DB_NAME     ="SmartRingDB";
	private final static String TABLE_NAME  ="SettingTable";
	private final static int    DB_VERSION  =1;
	private final static String SETTING_ID  ="id";
	private final static String SETTING_NAME="setting_name";
	private final static String SETTING_VIB ="Setting_vibrate";
	private final static String SETTING_LED ="Setting_led";
	private final static String SETTING_ON  ="Setting_on";
	
	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
	public DBHelper(Context context){	
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE " + TABLE_NAME + " (" + SETTING_ID+ " INTEGER primary key autoincrement, " 
														 + SETTING_NAME + " text, "
														 + SETTING_VIB+" Integer,"
														 + SETTING_LED+" Integer,"
														 + SETTING_ON+" Boolean"
														 +");";
	    db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db); 
	}

	public long Insert(String name,int vibrate ,int led,boolean on){
		
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		cv.put(SETTING_NAME,name);
		cv.put(SETTING_VIB, vibrate);
		cv.put(SETTING_LED, led);
		cv.put(SETTING_ON,on);
		long row=db.insert(TABLE_NAME, null, cv);
		return led;	
	}
	public void delete(int id){
		SQLiteDatabase db = this.getWritableDatabase();
		String where = SETTING_ID + " = ?";
		String[] whereValue ={ Integer.toString(id) };
		db.delete(TABLE_NAME, where, whereValue);
	}
	
	public void Update(String name,int vib,int led,boolean on){
		SQLiteDatabase db = this.getWritableDatabase();
		String where = SETTING_NAME + " = ?";
		String[] whereValue = { name };
			 
		ContentValues cv = new ContentValues();
		cv.put(SETTING_NAME, name);
		cv.put(SETTING_VIB, vib);
		cv.put(SETTING_LED,led);
		cv.put(SETTING_ON,on);
		db.update(TABLE_NAME, cv, where, whereValue); 

	}
}