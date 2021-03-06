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
import cens.ucla.edu.budburst.database.SyncNetworkHelper;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;

public class PBBHelloscr extends Activity{
	
	private String mUsername;
	private String mPassword;
	private HelperSharedPreference mPref;
			
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helloscr);
		
		StaticDBHelper sDBHelper = new StaticDBHelper(PBBHelloscr.this);
		SQLiteDatabase sDB = sDBHelper.getReadableDatabase();
		
		//Retrieve username and password
		mPref = new HelperSharedPreference(this);
		
		mUsername = mPref.getPreferenceString("Username", "");
		mPassword = mPref.getPreferenceString("Password", "");

		if(mPref.getPreferenceBoolean("Preview")) {
			mUsername = "Preview";
		}
		
		//Display instruction message
		TextView textViewHello = (TextView)findViewById(R.id.hello_textview);
		textViewHello.setText(getString(R.string.hello) + mUsername + "!" + getString(R.string.instruction));
	
		//Sync button
		Button buttonSync = (Button)findViewById(R.id.sync);
		buttonSync.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				mPref.setPreferencesBoolean("Synced", true);
				
				// 'First' is the flag for executing only one-time in 'PlantList' activity...
				mPref.setPreferencesBoolean("First", true);
				
				Intent intent = new Intent(PBBHelloscr.this, PBBSync.class);
				intent.putExtra("sync_instantly", true);
				intent.putExtra("from", HelperValues.FROM_MAIN_PAGE);
				startActivity(intent);
				finish();
			}
		});
	}
}
