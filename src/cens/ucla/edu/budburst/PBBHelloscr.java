package cens.ucla.edu.budburst;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.database.SyncNetworkHelper;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
import cens.ucla.edu.budburst.helper.HelperValues;

public class PBBHelloscr extends Activity{

	final String TAG = new String("Helloscr"); 
	
	private String username;
	private String password;
	private SharedPreferences pref;
	private int progressValue;
	private SyncNetworkHelper sync;
	private String error_message;

	//MENU constants
	final private int MENU_SYNC = 1;
	final private int MENU_ADD_PLANT = 2;
	final private int MENU_ADD_SITE = 3;
	final private int MENU_LOGOUT = 4;
	
	//Sync constants
	final private int UPLOAD_ADDED_SITE = 1;
	final private int UPLOAD_ADDED_PLANT = 2;
	final private int UPLOAD_OBSERVATION = 3;
	final private int DOWNLOAD_MY_SPECIES_IN_MY_STATION = 4;
	final private int DOWNLOAD_OBSERVATION = 5;
	final private int DOWNLOAD_OBSERVATION_IMG = 6;
	final private int NETWORK_COMPLETED = 7;
	
	public SyncDBHelper syncDBHelper;
			
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helloscr);
		
		//Call SyncNetworkHelper
		sync = new SyncNetworkHelper();
	
		StaticDBHelper sDBHelper = new StaticDBHelper(PBBHelloscr.this);
		SQLiteDatabase sDB = sDBHelper.getReadableDatabase();
		
		//Retrieve username and password
		pref = getSharedPreferences("userinfo",0);
		username = pref.getString("Username","");
		password = pref.getString("Password","");

		if(pref.getBoolean("Preview", false)) {
			username = "Preview";
		}
		
		//Display instruction message
		TextView textViewHello = (TextView)findViewById(R.id.hello_textview);
		textViewHello.setText(getString(R.string.hello) + username + "!" + getString(R.string.instruction));
	
		//Sync button
		Button buttonSync = (Button)findViewById(R.id.sync);
		buttonSync.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				SharedPreferences.Editor edit = pref.edit();				
				edit.putString("Synced","true");
				
				// 'First' is the flag for executing only one-time in 'PlantList' activity...
				edit.putString("First", "true");
				edit.commit();
				
				Intent intent = new Intent(PBBHelloscr.this, PBBSync.class);
				intent.putExtra("sync_instantly", true);
				intent.putExtra("from", HelperValues.FROM_MAIN_PAGE);
				startActivity(intent);
				finish();
			}
		});
	}
	
	/////////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		menu.add(0,MENU_LOGOUT,0,"Log out").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			
		return true;
	}
	
	
	/*
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case MENU_LOGOUT:
				new AlertDialog.Builder(PBBHelloscr.this)
					.setTitle(getString(R.string.Alert_titleQuestion))
					.setMessage(getString(R.string.Alert_logout))
					.setPositiveButton(getString(R.string.Button_yes),mClick)
					.setNegativeButton(getString(R.string.Button_no),mClick)
					.show();
				return true;
		}
		return false;
	}
	

	
	//Dialog confirm message if user clicks logout button
	DialogInterface.OnClickListener mClick =
		new DialogInterface.OnClickListener(){
		public void onClick(DialogInterface dialog, int whichButton){
			if(whichButton == DialogInterface.BUTTON1){
				
				SharedPreferences.Editor edit = pref.edit();				
				edit.putString("Username","");
				edit.putString("Password","");
				edit.putString("synced", "false");
				edit.commit();
				
				//Drop user table in database
				SyncDBHelper dbhelper = new SyncDBHelper(PBBHelloscr.this);
				dbhelper.clearAllTable(PBBHelloscr.this);
				dbhelper.close(); 
				
				String[] filelist = fileList();
				File sdcard = new File("/sdcard/pbudburst/");
				String[] files = sdcard.list();
				
				for(int i=0; i<files.length; i++){
					File file = new File("/sdcard/pbudburst/" + files[i]);
					file.delete();
				}
				
				Intent intent = new Intent(PBBHelloscr.this, PBBLogin.class);
				startActivity(intent);
				finish();
			}else{
			}
		}
	};
	*/
}
