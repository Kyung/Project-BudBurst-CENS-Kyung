package cens.ucla.edu.budburst.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

import cens.ucla.edu.budburst.PBBLogin;
import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.floracaching.FloraCacheEasyLevel;
import cens.ucla.edu.budburst.floracaching.FloraCacheOverlay;
import cens.ucla.edu.budburst.floracaching.FloracacheGetLists;
import cens.ucla.edu.budburst.lists.ListMain;
import cens.ucla.edu.budburst.lists.ListUserPlants;
import cens.ucla.edu.budburst.onetime.OneTimeMainPage;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class HelperSettings extends PreferenceActivity {

	private SharedPreferences pref;
	private String mUsername;
	private int mPreviousActivity;
	private int mOneTimeMainPreviousActivity;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    pref = getSharedPreferences("userinfo", 0);
	    Intent pIntent = getIntent();
	    mPreviousActivity = pIntent.getExtras().getInt("from");
	    
	    addPreferencesFromResource(R.xml.preferences);
	    // TODO Auto-generated method stub
	    
	    Preference downloadListPref = (Preference) findPreference("downloadLists");
	    downloadListPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				
				new AlertDialog.Builder(HelperSettings.this)
				.setTitle(getString(R.string.List_Download_Title))
				.setMessage(getString(R.string.List_ask_connectivity))
				.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						stopService(new Intent(HelperSettings.this, HelperBackgroundService.class));
						
						HelperFunctionCalls helper = new HelperFunctionCalls();
						helper.changeSharedPreference(HelperSettings.this);

						// getting contents again...
						Toast.makeText(HelperSettings.this, getString(R.string.Start_Downloading), Toast.LENGTH_SHORT).show();
						startService(new Intent(HelperSettings.this, HelperBackgroundService.class));					
					}
				})
				.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				})
				.show();
				return true;
			}
	    	
	    });
	    
	    Preference downloadTreeListPref = (Preference) findPreference("downloadTreeLists");
	    downloadTreeListPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(HelperSettings.this)
				.setTitle(getString(R.string.DownLoad_Tree_Lists))
				.setMessage(getString(R.string.List_ask_connectivity))
				.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						Toast.makeText(HelperSettings.this, getString(R.string.Start_Downloading_UCLA_Tree_Lists), Toast.LENGTH_SHORT).show();
						
						HelperFunctionCalls helper = new HelperFunctionCalls();
						helper.changedSharedPreferenceTree(HelperSettings.this);
						HelperSharedPreference hPref = new HelperSharedPreference(HelperSettings.this);
						hPref.setPreferencesBoolean("firstDownloadTreeList", false);
						//SharedPreferences.Editor edit = pref.edit();
						//edit.putBoolean("firstDownloadTreeList", false);
						//edit.commit();

						ListUserPlants userPlant = new ListUserPlants(HelperSettings.this);
						userPlant.execute();
							
					}
				})
				.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();

				return true;
			}
	    });
	    
	    Preference downloadFloracachePref = (Preference) findPreference("downloadFloracache");
	    downloadFloracachePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				
				new AlertDialog.Builder(HelperSettings.this)
				.setTitle(getString(R.string.DownLoad_Tree_Lists))
				.setMessage(getString(R.string.List_ask_connectivity))
				.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						Toast.makeText(HelperSettings.this, getString(R.string.Start_Downloading_Floracache_Lists), Toast.LENGTH_SHORT).show();
						
						FloracacheGetLists fLists = new FloracacheGetLists(HelperSettings.this);
						fLists.execute(getString(R.string.get_floracaching_plant_lists));
						
						//FloraCacheGetGroups fGroups = new FloraCacheGetGroups(HelperSettings.this);
					}
				})
				.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();
				
				
				return true;
			}
	    	
	    });
	    
	    mUsername = pref.getString("Username", "");
	    if(mUsername.equals("test10")){
	    	mUsername = "Preview";
	    }
	    
	    Preference logoutPref = (Preference) findPreference("userlogout");
	    if(mPreviousActivity != HelperValues.FROM_MAIN_PAGE) {
	    	logoutPref.setEnabled(false);
	    	logoutPref.setSummary("Only logout from the main page");
	    }
	    
	    
	    logoutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(HelperSettings.this)
				.setTitle(getString(R.string.Menu_logout) + " - " + mUsername)
				.setMessage(getString(R.string.Alert_logout))
				.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						pref = getSharedPreferences("userinfo",0);
						SharedPreferences.Editor edit = pref.edit();				
						edit.putString("Username","");
						edit.putString("Password","");
						edit.putString("synced", "false");
						edit.putBoolean("getTreeLists", false);
						edit.putBoolean("Update", false);
						edit.putBoolean("localbudburst", false);
						edit.putBoolean("localwhatsinvasive", false);
						edit.putBoolean("localnative", false);
						edit.putBoolean("localpoisonous", false);
						edit.putBoolean("listDownloaded", false);
						edit.putBoolean("firstDownloadTreeList", true);
						edit.putBoolean("floracache", false);
						edit.commit();

						//Drop user table in database
						SyncDBHelper dbhelper = new SyncDBHelper(HelperSettings.this);
						OneTimeDBHelper onehelper = new OneTimeDBHelper(HelperSettings.this);
						dbhelper.clearAllTable(HelperSettings.this);
						onehelper.clearAllTable(HelperSettings.this);
						dbhelper.close();
						onehelper.close();

						HelperFunctionCalls helper = new HelperFunctionCalls();
						helper.deleteContents(HelperValues.BASE_PATH);

						Intent intent = new Intent(HelperSettings.this, PBBLogin.class);
				        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra("from", HelperValues.FROM_SETTINGS);
						startActivity(intent);
						
						stopService(new Intent(HelperSettings.this, HelperGpsHandler.class));
						
						finish();
					}
				})
				.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();
				return true;
			}
	    });
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			switch(mPreviousActivity) {
			case HelperValues.FROM_MAIN_PAGE:
				startActivity(new Intent(this, PBBMainPage.class));
				break;
			case HelperValues.FROM_PLANT_LIST:
				finish();
				break;
			case HelperValues.FROM_ONE_TIME_MAIN:
				finish();
				break;
			}
			return true;
		}
		return false;
	}	
}
