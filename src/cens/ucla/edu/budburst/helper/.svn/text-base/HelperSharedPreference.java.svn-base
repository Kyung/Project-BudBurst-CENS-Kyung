package cens.ucla.edu.budburst.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class HelperSharedPreference {
	
	private Context mContext;
	private SharedPreferences mPref;
	private SharedPreferences.Editor mEdit;
	
	public HelperSharedPreference(Context context) {
		mContext = context;
		mPref = mContext.getSharedPreferences("userinfo", 0);
		mEdit = mPref.edit();
	}
	
	public void setPreferencesString(String key, String value) {
		mEdit.putString(key, value);
		mEdit.commit();
	}
	
	public void setPreferencesInt(String key, int value) {
		mEdit.putInt(key, value);
		mEdit.commit();
	}	

	public void setPreferencesBoolean(String key, boolean value) {			
		mEdit.putBoolean(key, value);
		mEdit.commit();
	}

	public String getPreferenceString(String key, String defaultValue) {
		return mPref.getString(key, defaultValue);
	}
	
	public boolean getPreferenceBoolean(String key) {
		return mPref.getBoolean(key, false);
	}

}
