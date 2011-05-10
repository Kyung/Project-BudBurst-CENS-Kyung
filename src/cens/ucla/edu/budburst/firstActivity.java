package cens.ucla.edu.budburst;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.util.ByteArrayBuffer;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper2;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperRefreshPlantLists;
import cens.ucla.edu.budburst.helper.HelperSettings;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.lists.ListUserDefinedSpeciesDownload;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class firstActivity extends Activity{
	
	private String mGetCurrentVersion;
	private String mGetOldVersion;
	private SharedPreferences mPref;
	private LocationManager mLocManager;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.splash);
	    // TODO Auto-generated method stub	
	}
	
	public void onResume() {
		super.onResume();
		
		getCurrentVersion();
		moveToMainPage();
		
		Intent service = new Intent(firstActivity.this, HelperBackgroundService.class);
	    startService(service);
	}
	
	public void getCurrentVersion() {
		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		ComponentName comp = new ComponentName(firstActivity.this, "");
		
		try {
			PackageInfo pInfo = this.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			mGetCurrentVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void moveToMainPage() {
		mPref = getSharedPreferences("userinfo", 0);
		mGetOldVersion = mPref.getString("version", mGetCurrentVersion);
		
	 	SharedPreferences.Editor edit = mPref.edit();				
		edit.putString("version", mGetCurrentVersion);
		edit.commit();
		
		if(!mGetCurrentVersion.equals(mGetOldVersion)) {
		 	downloadPlantLists();
		}
		else {
			if(!mLocManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
		    	new AlertDialog.Builder(this)
		    	.setTitle("Turn on GPS")
		    	.setMessage(getString(R.string.Update_Database_Table))
		    	.setPositiveButton("Redirect", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
			        	startActivity(myIntent);
					}
				})
				.show();
		    }
		    else {
	
				new Handler().postDelayed(new Runnable(){
			    	public void run() {
			    		//checkUpdate();
			    		Intent intent = new Intent(firstActivity.this, PBBSplash.class);
						finish();
						startActivity(intent);
			    	}
			    }, 2000);

		    }		
		}
	}
	
	private void downloadPlantLists() {
		
		HelperFunctionCalls helper = new HelperFunctionCalls();
		helper.changedSharedPreferenceTree(firstActivity.this);
		
		HelperSharedPreference hPref = new HelperSharedPreference(firstActivity.this);
		hPref.setPreferencesBoolean("firstDownloadTreeList", false);

		HelperRefreshPlantLists refreshList = new HelperRefreshPlantLists(firstActivity.this);
		refreshList.execute();
		
	 	Intent intent = new Intent(firstActivity.this, firstActivity.class);
		startActivity(intent);
		finish();
	}
		
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	     if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	 //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
	    	 return true;
	     }
	     return super.onKeyDown(keyCode, event);    
	}
	
	public void checkUpdate() {
		
		try {
			URL urls = new URL(getString(R.string.check_version_number) + "?version=" + mGetCurrentVersion);
			Object getContent = urls.getContent();
			
			Log.i("K", "getContent : " + getContent);
			HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
			conn.connect();
			
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
            
            /* Read bytes to the Buffer until
             * there is nothing more to read(-1). */
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current = 0;
            while((current = bis.read()) != -1){
                    baf.append((byte)current);
            }

            /* Convert the Bytes read to a String. */
            String myString = new String(baf.toByteArray());
            
            if(myString.equals("NEEDUPDATES")) {
            	new AlertDialog.Builder(this)
    	    	.setTitle("Budburst needs updates")
    	    	.setMessage("Please updates budburst. App might crash if you use the old version.")
    	    	.setPositiveButton("Updates", new DialogInterface.OnClickListener() {
    				
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					// TODO Auto-generated method stub
    					Intent intent = new Intent(Intent.ACTION_VIEW);
    					intent.setData(Uri.parse("market://details?id=cens.ucla.edu.budburst"));
    					startActivity(intent);
    				}
    			})
    			.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						moveToMainPage();
					}
				})
    			.show();
            }
            else {
            	moveToMainPage();
            }
		}
		catch(Exception e) {}
	}
}
