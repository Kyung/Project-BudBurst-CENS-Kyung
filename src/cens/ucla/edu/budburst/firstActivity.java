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
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
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
	
	private String getCurrentVersion;
	private String getOldVersion;
	private SharedPreferences pref;
	private LocationManager lm;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

	    setContentView(R.layout.splash);
	    // TODO Auto-generated method stub	    
	}
	
	public void onResume() {
		super.onResume();
		getCurrentVersion();
		moveToMainPage();
	}
	
	public void getCurrentVersion() {
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		ComponentName comp = new ComponentName(firstActivity.this, "");
		
		try {
			PackageInfo pInfo = this.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			getCurrentVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void moveToMainPage() {
		pref = getSharedPreferences("userinfo", 0);
		getOldVersion = pref.getString("version", getCurrentVersion);
		
		//Toast.makeText(firstActivity.this, "getOldVersion : " + getOldVersion + " getCurrentVersion : " + getCurrentVersion , Toast.LENGTH_SHORT).show();
		
	 	SharedPreferences.Editor edit = pref.edit();				
		edit.putString("version", getCurrentVersion);
		edit.commit();
		
		if(!getCurrentVersion.equals(getOldVersion)) {
	    	/*
		     * Upgrade Database...
		     */
		    StaticDBHelper staticDBHelper = new StaticDBHelper(firstActivity.this);
			SyncDBHelper syncDBHelper = new SyncDBHelper(firstActivity.this);
			OneTimeDBHelper otDBHelper = new OneTimeDBHelper(firstActivity.this);
			
			SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
			
			staticDBHelper.onUpgrade(staticDB, 1, 2);
			
			staticDB.close();
		 	staticDBHelper.close();
		 	syncDBHelper.close();
		 	otDBHelper.close();
			
		 	Log.i("K", "application database has been upgraded");
		 	
		 	Intent intent = new Intent(firstActivity.this, firstActivity.class);
			startActivity(intent);
			finish();
		 	
		 	/*
			new AlertDialog.Builder(this)
	    	.setTitle("Upgrade Database")
	    	.setMessage("Database Upgrade Complete.")
	    	.setPositiveButton("Next", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			})
			.show();
			*/
		}
		else {
			if(!lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
		    	new AlertDialog.Builder(this)
		    	.setTitle("Turn on GPS")
		    	.setMessage("Please turn on GPS to use this system. Press Redirect button to move to the settings page.")
		    	.setPositiveButton("Redirect", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent myIntent = new Intent( Settings.ACTION_SECURITY_SETTINGS );
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
			URL urls = new URL(getString(R.string.check_version_number) + "?version=" + getCurrentVersion);
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
		catch(Exception e) {
			
		}
	}
}
