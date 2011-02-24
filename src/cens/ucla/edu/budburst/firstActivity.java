package cens.ucla.edu.budburst;

import java.io.IOException;

import cens.ucla.edu.budburst.helper.BackgroundService;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class firstActivity extends Activity{
	 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

	    setContentView(R.layout.splash);
	    // TODO Auto-generated method stub
	    
	}
	
	public void onResume() {
		super.onResume();
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		/*
		 * Checking if GPS is turned on.
		 * otherwise, move to the settings page 
		 * 
		 */
		
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
	    	
	    	/*
	    	 * 
	    	 * Start two services
	    	 *  - 1. Tree Lists service
	    	 *  - 2. Local Lists from USDA
	    	 * 
	    	 */
	    	
		    Intent service = new Intent(firstActivity.this, BackgroundService.class);
		    startService(service);
		    		    
		    StaticDBHelper staticDBHelper = new StaticDBHelper(firstActivity.this);
			SyncDBHelper syncDBHelper = new SyncDBHelper(firstActivity.this);
			OneTimeDBHelper otDBHelper = new OneTimeDBHelper(firstActivity.this);
			
			try {
	        	staticDBHelper.createDataBase();
		 	} catch (IOException e) {
		 		Log.e("K", "CREATE DATABASE : " + e.toString());
		 		throw new Error("Unable to create database");
		 	}
	 
		 	try {
		 		staticDBHelper.openDataBase();
		 	}catch(SQLException sqle){
		 		Log.e("K", "OPEN DATABASE : " + sqle.toString());
		 		throw sqle;
		 	}
		 	
		 	staticDBHelper.close();
		 	syncDBHelper.close();
		 	otDBHelper.close();
		    
		    

		    new Handler().postDelayed(new Runnable(){
		    	public void run() {
		    		Intent intent = new Intent(firstActivity.this, Splash.class);
					finish();
					startActivity(intent);
		    	}
		    }, 2000);
	    }		
	}
}
