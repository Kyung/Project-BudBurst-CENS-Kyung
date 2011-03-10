package cens.ucla.edu.budburst;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cens.ucla.edu.budburst.helper.BackgroundService;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.lists.ListMain;
import cens.ucla.edu.budburst.lists.UserDefinedTreeLists;
import cens.ucla.edu.budburst.mapview.PBB_map;
import cens.ucla.edu.budburst.onetime.OneTimeMain;
import cens.ucla.edu.budburst.onetime.QuickCapture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainPage extends Activity {
	
	private ImageButton myPlantBtn = null;
	private Button oneTimeBtn = null;
	private Button myResultBtn = null;
	private Button mapBtn = null;
	private Button newsBtn = null;
	private Button weeklyBtn = null;
	private SharedPreferences pref;
	private String username;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mainpage);
	    
	    pref = getSharedPreferences("userinfo",0);
	    username = pref.getString("Username", "");
	    if(username.equals("test10")){
	    	username = "Preview";
	    }
	    SharedPreferences.Editor edit = pref.edit();	
	    edit.putBoolean("new", false);
		edit.putString("visited","false");
		edit.commit();
		
		LinearLayout ll = (LinearLayout) findViewById(R.id.header);
		ll.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainPage.this, firstActivity.class);
				finish();
				startActivity(intent);
			}
		});
		
		/*
		Intent intent = getIntent();
		boolean move_to_plantlist = intent.getExtras().getBoolean("move_to_plantlist");
		if(move_to_plantlist) {
			Intent to_plantlist = new Intent(MainPage.this, PlantList.class);
			finish();
			startActivity(to_plantlist);
		}
		*/
		
		int synced = checkSync();
 
	    myPlantBtn = (ImageButton)findViewById(R.id.my_plant);
	    
	    if(synced == SyncDBHelper.SYNCED_NO) {
	    	LinearLayout sync_layout = (LinearLayout)findViewById(R.id.my_plant_sync);
	    	LinearLayout unsync_layout = (LinearLayout)findViewById(R.id.my_plant_unsync);
	    	
	    	sync_layout.setVisibility(View.GONE);
	    	unsync_layout.setVisibility(View.VISIBLE);
	    	
	    	ImageButton unsync_Btn = (ImageButton) findViewById(R.id.unsync_my_plant);
	    	unsync_Btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainPage.this, PlantList.class);
					startActivity(intent);
				}
			});
	    }
	    
	    myPlantBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainPage.this, PlantList.class);
				startActivity(intent);
			}
	    });
	    
	    /*
	    oneTimeBtn = (Button)findViewById(R.id.onetimeobservation);
	    oneTimeBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainPage.this, QuickCapture.class);
				intent.putExtra("from", Values.FROM_MAIN_PAGE);
				startActivity(intent);
				
			}
	    	
	    });
	    */
	    
	    myResultBtn = (Button) findViewById(R.id.myresults);
	    myResultBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				startActivity(new Intent(MainPage.this, ListMain.class));
			}
	    });
	    
	    mapBtn = (Button)findViewById(R.id.map);
	    mapBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				//Intent intent = new Intent(MainPage.this, PBB_map.class);
				//startActivity(intent);
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
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			boolean flag = false;
			if(event.getRepeatCount() == 3) {

				/*
				 * Stop the service if it is still working
				 */
				Intent service = new Intent(MainPage.this, BackgroundService.class);
			    stopService(service);
			    
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
	
	public int checkSync() {
		SyncDBHelper syncDB = new SyncDBHelper(MainPage.this);
		OneTimeDBHelper onetime = new OneTimeDBHelper(MainPage.this);
		
		SQLiteDatabase ot = onetime.getReadableDatabase();
		SQLiteDatabase sync = syncDB.getReadableDatabase();
		
		int synced = SyncDBHelper.SYNCED_YES;
		
		Cursor syncCheck = ot.rawQuery("SELECT synced FROM oneTimeObservation", null);
		while(syncCheck.moveToNext()) {
			if(syncCheck.getInt(0) == SyncDBHelper.SYNCED_NO) {
				synced = SyncDBHelper.SYNCED_NO;
			}
		}
		syncCheck.close();
		
		syncCheck = ot.rawQuery("SELECT synced FROM oneTimePlant", null);
		while(syncCheck.moveToNext()) {
			if(syncCheck.getInt(0) == SyncDBHelper.SYNCED_NO) {
				synced = SyncDBHelper.SYNCED_NO;
			}
		}
		syncCheck.close();
	
		// check if there is any unsynced data from my_observation and onetimeob tables.
		syncCheck = sync.rawQuery("SELECT synced FROM my_observation", null);
		while(syncCheck.moveToNext()) {
			if(syncCheck.getInt(0) == SyncDBHelper.SYNCED_NO) {
				synced = SyncDBHelper.SYNCED_NO;
			}
		}
		syncCheck.close();
		
		sync.close();
		ot.close();
		
		return synced;
	}
	
	/*
	 * Menu option(non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, getString(R.string.Menu_help)).setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 2, 0, getString(R.string.Menu_sync)).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, 3, 0, getString(R.string.Menu_about)).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(0, 4, 0, getString(R.string.Menu_logout)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			
		return true;
	}
	
	/*
	 * Menu option selection handling(non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){

		Intent intent;
		switch(item.getItemId()){
			case 1:
				intent = new Intent(MainPage.this, Help.class);
				intent.putExtra("from", Values.FROM_MAIN_PAGE);
				startActivity(intent);
				return true;
			case 2:
				intent = new Intent(MainPage.this, Sync.class);
				intent.putExtra("sync_instantly", true);
				intent.putExtra("from", Values.FROM_MAIN_PAGE);
				startActivity(intent);
				finish();
				return true;				
			case 3:
				intent = new Intent(MainPage.this, Help.class);
				intent.putExtra("from", Values.FROM_ABOUT);
				startActivity(intent);
				return true;
			case 4:
				new AlertDialog.Builder(MainPage.this)
				.setTitle(getString(R.string.Menu_logout) + " - " + username)
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
						edit.commit();
						
						//Drop user table in database
						SyncDBHelper dbhelper = new SyncDBHelper(MainPage.this);
						OneTimeDBHelper onehelper = new OneTimeDBHelper(MainPage.this);
						dbhelper.clearAllTable(MainPage.this);
						onehelper.clearAllTable(MainPage.this);
						onehelper.clearLocalListAll(MainPage.this);
						dbhelper.close();
						onehelper.close();
						
						deleteContents(Values.TEMP_PATH);
						deleteContents(Values.WI_PATH);
						//deleteContents(Values.TREE_PATH);
						deleteContents(Values.BASE_PATH);
						
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
				.show();
				return true;
		}
		return false;
	}
	
	void deleteContents(String path) {
		File file = new File(path);
		if(file.isDirectory()) {
			String[] fileList = file.list();
			
			for(int i = 0 ; i < fileList.length ; i++) {
				Log.i("K", "FILE NAME : " + path + fileList[i] + " IS DELETED.");
				new File(path + fileList[i]).delete();
			}
		}
	}
}

