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
import android.net.Uri;
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

	private HelperSharedPreference mPref;
	private String mUsername;
	private int mPreviousActivity;
	private int mOneTimeMainPreviousActivity;
	private ArrayList<ListGroupItem> mArr;
	private boolean[] mSelect;
	private String[] mGroupName;
	static final int UNINSTALL_REQUEST = 0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    mArr = new ArrayList<ListGroupItem>();
	    
	    mPref = new HelperSharedPreference(this);
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
	    
	    mUsername = mPref.getPreferenceString("Username", "");
	    if(mUsername.equals("test10")){
	    	mUsername = "Preview";
	    }
	    
	    Preference updatePref = (Preference) findPreference("update");
	    if(mPref.getPreferenceBoolean("needUpdate")) {
	    	updatePref.setTitle("Need to update");
		    updatePref.setSummary("Please update the application");
	    }
	    else {
	    	updatePref.setTitle("No need to update");
		    updatePref.setSummary("The most recent version");
	    }
	    
	    updatePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				if(mPref.getPreferenceBoolean("needUpdate")) {
					new AlertDialog.Builder(HelperSettings.this)
					.setTitle("Upgrade the app")
					.setMessage("Move to the market, you may uninstall and reinstall the app.")
					.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse("market://details?id=cens.ucla.edu.budburst"));
							startActivity(intent);
						}
					})
					.setNegativeButton(getString(R.string.Button_back), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					})
					.show();
					//Uri packageURI = Uri.parse("package:cens.ucla.edu.budburst");
					//Intent unInstall = new Intent(Intent.ACTION_DELETE, packageURI);
					//startActivityForResult(unInstall, UNINSTALL_REQUEST);
				}
				else {
					Toast.makeText(HelperSettings.this, "No need to update", Toast.LENGTH_SHORT).show();
				}
				return false;
			}
	    });
	    
	    
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
						
						mPref.setPreferencesString("Username", "");
						mPref.setPreferencesString("Password", "");
						mPref.setPreferencesString("synced", "false");
						mPref.setPreferencesBoolean("getTreeLists", false);
						mPref.setPreferencesBoolean("Update", false);
						mPref.setPreferencesBoolean("localbudburst", false);
						mPref.setPreferencesBoolean("localwhatsinvasive", false);
						mPref.setPreferencesBoolean("localpoisonous", false);
						mPref.setPreferencesBoolean("localendangered", false);
						mPref.setPreferencesBoolean("listDownloaded", false);
						mPref.setPreferencesBoolean("floracache", false);
						mPref.setPreferencesBoolean("firstDownloadTreeList", true);
						mPref.setPreferencesBoolean("needUpdate", false);
						
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
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == UNINSTALL_REQUEST) {
            if (resultCode == RESULT_OK) {
            	Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=cens.ucla.edu.budburst"));
				startActivity(intent);
            }
        }
    }
	
	private void checkConnectivity() {
		new AlertDialog.Builder(HelperSettings.this)
		.setTitle(getString(R.string.DownLoad_Tree_Lists))
		.setMessage(getString(R.string.List_ask_connectivity))
		.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub				
				downloadGroupList();

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
	
	private void downloadGroupList() {

		ListUserDefinedCategory userCategory = new ListUserDefinedCategory(HelperSettings.this);
		
		HelperSharedPreference pref = new HelperSharedPreference(HelperSettings.this);
		
		double getLatitude = Double.parseDouble(pref.getPreferenceString("latitude", "0.0"));
		double getLongitude = Double.parseDouble(pref.getPreferenceString("longitude", "0.0"));
		
		ListItems lItem = new ListItems(getLatitude, getLongitude);
		userCategory.execute(lItem);	
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
