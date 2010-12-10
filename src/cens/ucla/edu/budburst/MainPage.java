package cens.ucla.edu.budburst;

import java.io.File;
import java.io.IOException;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.OneTimeMain;
import cens.ucla.edu.budburst.onetime.QuickCapture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainPage extends Activity {
	
	private Button myPlantBtn = null;
	private Button oneTimeBtn = null;
	private Button myResultBtn = null;
	private Button mapBtn = null;
	private Button newsBtn = null;
	private Button weeklyBtn = null;
	private SharedPreferences pref;
	private StaticDBHelper staticDBHelper = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mainpage);
	    
	    pref = getSharedPreferences("userinfo",0);
	    SharedPreferences.Editor edit = pref.edit();				
		edit.putString("visited","false");
		edit.commit();
		
		/*
		Intent intent = getIntent();
		boolean move_to_plantlist = intent.getExtras().getBoolean("move_to_plantlist");
		if(move_to_plantlist) {
			Intent to_plantlist = new Intent(MainPage.this, PlantList.class);
			finish();
			startActivity(to_plantlist);
		}
		*/
		
		staticDBHelper = new StaticDBHelper(MainPage.this);
		OneTimeDBHelper onetime = new OneTimeDBHelper(MainPage.this);

		try {
        	staticDBHelper.createDataBase();
	 	} catch (IOException ioe) {
	 		Log.e("K", "CREATE DATABASE : " + ioe.toString());
	 		throw new Error("Unable to create database");
	 	}
 
	 	try {
	 		staticDBHelper.openDataBase();
	 	}catch(SQLException sqle){
	 		Log.e("K", "OPEN DATABASE : " + sqle.toString());
	 		throw sqle;
	 	}
	 	
	 	staticDBHelper.close();
	    
	    myPlantBtn = (Button)findViewById(R.id.my_plant);
	    
	    myPlantBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainPage.this, PlantList.class);
				startActivity(intent);
			}
	    });
	    
	    oneTimeBtn = (Button)findViewById(R.id.onetimeobservation);
	    oneTimeBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainPage.this, QuickCapture.class);
				startActivity(intent);
				
			}
	    	
	    });
	    
	    myResultBtn = (Button) findViewById(R.id.myresults);
	    myResultBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
			}
	    	
	    });
	    
	    mapBtn = (Button)findViewById(R.id.map);
	    mapBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainPage.this, PBB_map.class);
				startActivity(intent);
			}
	    	
	    });
	    
	    newsBtn = (Button)findViewById(R.id.news);
	    newsBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
			}
	    	
	    });
	    
	    weeklyBtn = (Button)findViewById(R.id.weekly);
	    weeklyBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
			}
	    	
	    });
	    
	    // TODO Auto-generated method stub
	}
	
	
	
    // or when user press back button
	// when you hold the button for 3 sec, the app will be exited
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			boolean flag = false;
			if(event.getRepeatCount() == 3) {
				Toast.makeText(MainPage.this, getString(R.string.Alert_thanks), Toast.LENGTH_SHORT).show();
				finish();
				
				return true;
			}
			else if(event.getRepeatCount() == 0 && flag == false){
				Toast.makeText(MainPage.this, getString(R.string.Alert_holdBackExit), Toast.LENGTH_SHORT).show();
				flag = true;
			}
		}
		
		return false;
	}
	
		///////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, getString(R.string.Menu_help)).setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 2, 0, getString(R.string.Menu_sync)).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, 3, 0, getString(R.string.Menu_logout)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){

		switch(item.getItemId()){
			case 1:
				return true;
			case 3:
				new AlertDialog.Builder(MainPage.this)
				.setTitle(getString(R.string.Menu_logout))
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
						edit.putBoolean("Update", false);
						edit.commit();
						
						//Drop user table in database
						SyncDBHelper dbhelper = new SyncDBHelper(MainPage.this);
						OneTimeDBHelper onehelper = new OneTimeDBHelper(MainPage.this);
						dbhelper.clearAllTable(MainPage.this);
						onehelper.clearAllTable(MainPage.this);
						dbhelper.close();
						onehelper.close();
						
						deleteContents("/sdcard/pbudburst/tmp/");
						deleteContents("/sdcard/pbudburst/wi_list/");
						deleteContents("/sdcard/pbudburst/");
						
						Intent intent = new Intent(MainPage.this, Login.class);
						startActivity(intent);
						finish();
					}
				})
				.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				})
				.setIcon(R.drawable.pbbicon_small)
				.show();
				return true;
			case 2:
				Intent intent = new Intent(MainPage.this, Sync.class);
				intent.putExtra("sync_instantly", true);
				startActivity(intent);
				finish();
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
	
	void deleteContents(String path) {
		File file = new File(path);
		if(file.isDirectory()) {
			String[] fileList = file.list();
			
			for(int i = 0 ; i < fileList.length ; i++) {
				Log.i("K", "FILE NAME : " + "/sdcard/pbudburst/tmp/" + fileList[i] + " IS DELETED.");
				new File(path + fileList[i]).delete();
			}
		}
	}
}

