package cens.ucla.edu.budburst;

import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.BackgroundService;
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
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

	    setContentView(R.layout.splash);
	    // TODO Auto-generated method stub	    
	}
	
	public void onResume() {
		super.onResume();
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		ComponentName comp = new ComponentName(firstActivity.this, "");
		
		try {
			PackageInfo pInfo = this.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			getCurrentVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		try {
			URL urls = new URL("http://networkednaturalist.org/User_Plant_Lists_Images/200_thumb.jpg");
			HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
			conn.connect();
			
			Toast.makeText(firstActivity.this, " " + conn.getResponseCode(), Toast.LENGTH_SHORT).show();
		}
		catch(Exception e) {
			
		}
		*/
		
		
		
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
			    		Intent intent = new Intent(firstActivity.this, Splash.class);
						finish();
						startActivity(intent);
			    	}
			    }, 2000);
			 	
			 	/*SharedPreferences pref = getSharedPreferences("userinfo",0);
				if(pref.getBoolean("db_upgraded", true)) {
					new Handler().postDelayed(new Runnable(){
				    	public void run() {
				    		Intent intent = new Intent(firstActivity.this, Splash.class);
							finish();
							startActivity(intent);
				    	}
				    }, 2000);
				}*/
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
}
