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
import cens.ucla.edu.budburst.lists.ListGroupItem;
import cens.ucla.edu.budburst.lists.ListItems;
import cens.ucla.edu.budburst.lists.ListMain;
import cens.ucla.edu.budburst.lists.ListUserDefinedCategory;
import cens.ucla.edu.budburst.lists.ListUserDefinedSpeciesDownload;
import cens.ucla.edu.budburst.onetime.OneTimeMainPage;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
	private ArrayList<ListGroupItem> mArr;
	private boolean[] mSelect;
	private String[] mGroupName;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    mArr = new ArrayList<ListGroupItem>();
	    
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
						
						HelperFunctionCalls helper = new HelperFunctionCalls();
						helper.changeSharedPreference(HelperSettings.this);

						// getting contents again...
						Toast.makeText(HelperSettings.this, getString(R.string.Start_Downloading), Toast.LENGTH_SHORT).show();
						HelperRefreshPlantLists getLocalList = new HelperRefreshPlantLists(HelperSettings.this);
						getLocalList.execute();
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
	    
	    Preference downloadUserDefinedListPref = (Preference) findPreference("downloadDefinedLists");
	    downloadUserDefinedListPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				checkConnectivity();
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
						edit.putBoolean("localpoisonous", false);
						edit.putBoolean("localendangered", false);
						edit.putBoolean("listDownloaded", false);
						edit.putBoolean("firstDownloadTreeList", true);
						edit.putBoolean("floracache", false);
						edit.commit();

						//Drop user table in database
						SyncDBHelper sDBH = new SyncDBHelper(HelperSettings.this);
						OneTimeDBHelper oDBH = new OneTimeDBHelper(HelperSettings.this);
						SQLiteDatabase oDB = oDBH.getWritableDatabase();
						
						sDBH.clearAllTable(HelperSettings.this);
						oDBH.clearAllTable(HelperSettings.this);
						sDBH.close();
						oDBH.close();

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
	
	private void checkConnectivity() {
		new AlertDialog.Builder(HelperSettings.this)
		.setTitle(getString(R.string.DownLoad_Tree_Lists))
		.setMessage(getString(R.string.List_ask_connectivity))
		.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub				
				showUserDefinedLists();

			}
		})
		.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		})
		.show();

	}
	
	private void showUserDefinedLists() {

		// call user defined lists
		OneTimeDBHelper oDBH = new OneTimeDBHelper(HelperSettings.this);
		mArr = oDBH.getListGroupItem(HelperSettings.this);
		
		int groupCnt = mArr.size();
		mGroupName = new String[groupCnt];
		mSelect = new boolean[groupCnt];
		
		for(int i = 0 ; i < groupCnt ; i++) {
			mGroupName[i] = mArr.get(i).getCategoryName();
			mSelect[i] = false;
		}
		
		new AlertDialog.Builder(HelperSettings.this)
		.setTitle("Select User Defined Lists")
		.setSingleChoiceItems(mGroupName, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mSelect[which] = true;
			}
		})
		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				for(int i = 0 ; i < mSelect.length ; i++) {
					if(mSelect[i]) {
						ListUserDefinedSpeciesDownload userPlant = new ListUserDefinedSpeciesDownload(HelperSettings.this
								, mArr.get(i).getCategoryID());
						userPlant.execute();
					}
				}
			}
		})
		.setNeutralButton("Refresh Lists", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				ListUserDefinedCategory userCategory = new ListUserDefinedCategory(HelperSettings.this);
				
				HelperSharedPreference pref = new HelperSharedPreference(HelperSettings.this);
				
				double getLatitude = Double.parseDouble(pref.getPreferenceString("latitude", "0.0"));
				double getLongitude = Double.parseDouble(pref.getPreferenceString("longitude", "0.0"));
				
				ListItems lItem = new ListItems(getLatitude, getLongitude);
				userCategory.execute(lItem);
			}
		})
		.show();
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
