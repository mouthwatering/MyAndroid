package com.example.smartringdemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

public class LoveSetting extends  PreferenceActivity implements OnPreferenceChangeListener
{
	public final static String KEY_PREF_REMOTE_IP_ADDRESS = "pref_remote_ip_address";
	private EditTextPreference mRemoteIPPreference;
	
	@Override
	protected void onCreate(Bundle icicle) {	
		super.onCreate(icicle);
		
		addPreferencesFromResource(R.xml.preference);
	
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String address = sharedPreferences.getString(KEY_PREF_REMOTE_IP_ADDRESS, null);
	
		mRemoteIPPreference = (EditTextPreference) findPreference(KEY_PREF_REMOTE_IP_ADDRESS);
		mRemoteIPPreference.setSummary(address);
		mRemoteIPPreference.setOnPreferenceChangeListener(this);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mRemoteIPPreference) {
			mRemoteIPPreference.setSummary((String)newValue);
			return true;
		}

		return false;
	}
}
