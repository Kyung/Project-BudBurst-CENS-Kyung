package cens.ucla.edu.budburst;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.SyncNetworkHelper;

public class Sync extends Activity{
	
	private String TAG = new String("SYNC");
	
	private String username;
	private String password;
	private SharedPreferences pref;
	
	//MENU constants
	final private int MENU_SYNC = 1;
	final private int MENU_ADD_PLANT = 2;
	final private int MENU_ADD_SITE = 3;
	final private int MENU_LOGOUT = 4;
	
	//Sync constants
	final private int SYNC_START = 0;
	final private int GET_SPECIES_ID = 1;
	final private int UPLOAD_ADDED_SITE = 2;
	final private int UPLOAD_ADDED_PLANT = 3;
	final private int UPLOAD_OBSERVATION = 4;
	final private int DOWNLOAD_USER_STATION = 5;
	final private int DOWNLOAD_USER_PLANTS = 6;
	final private int DOWNLOAD_OBSERVATION = 7;
	final private int DOWNLOAD_OBSERVATION_IMG = 8;
	final private int SYNC_COMPLETE = 9;
	
	final private int UPLOAD_QUICK_CAPTURE_PLANT = 10;
	final private int UPLOAD_QUICK_CAPTURE_OBSERVATION = 11;
	final private int DOWNLOAD_QUICK_CAPTURE_PLANT = 12;
	final private int DOWNLOAD_QUICK_CAPTURE_OBSERVATION = 13;
	
	//Boolean constant with integer
	final private int FINISH_INT = 1;
	final private int NOT_FINISH_INT = 0;
	
	public static int SERVER_ERROR = -99;
	public static int NETWORK_ERROR = 0;
	
	int mValue;
	TextView mText;
	ProgressDialog mProgress;
	boolean mQuit = false;
	doSyncThread mSyncThread;
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helloscr);
		pref = getSharedPreferences("userinfo",0);
		username = pref.getString("Username","");
		password = pref.getString("Password","");
		
		//Display instruction message
		TextView textViewHello = (TextView)findViewById(R.id.hello_textview);
		textViewHello.setText(getString(R.string.hello) + " " + username + "!" + getString(R.string.instruction));
		
		//Sync button
		Button buttonSync = (Button)findViewById(R.id.sync);
		buttonSync.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				doSync();
			}
			}
		);
	
		//If the previous activity is not plant list, then start sync instantly.  
		Intent parent_intent = getIntent();
		if(parent_intent.getExtras() != null &&
				parent_intent.getExtras().getBoolean("sync_instantly",false)){
			doSync();
		}
	}
	
	void doSync(){
		mQuit = false;
		
		SharedPreferences.Editor edit = pref.edit();				
		edit.putString("Synced","true");
		edit.commit();
		
		mSyncThread = new doSyncThread(mainMsgHandler, Sync.this);
		mSyncThread.setDaemon(true);
		mSyncThread.start();
	
		//Show progress
		showDialog(0);
		mProgress.setProgress(0);
	}
	
	
	/////////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);	
		
		menu.add(0,MENU_SYNC,0,getString(R.string.Menu_sync)).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0,MENU_LOGOUT,0,getString(R.string.Menu_logout)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case MENU_SYNC:
				doSync();
				return true;
			case MENU_LOGOUT:
				new AlertDialog.Builder(Sync.this)
					.setTitle(getString(R.string.Alert_titleQuestion))
					.setMessage(getString(R.string.Alert_logout))
					.setPositiveButton(getString(R.string.Button_yes),mClick)
					.setNegativeButton(getString(R.string.Button_no),mClick)
					.show();
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
	

	//Progress dialog generate
	protected Dialog onCreateDialog(int id){
		switch(id){
		case 0:
			mProgress = new ProgressDialog(this);
			mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgress.setTitle(getString(R.string.Alert_synchronizing));
			mProgress.setMessage(getString(R.string.Alert_pleaseWait));
			mProgress.setCancelable(false);
			mProgress.setButton(getString(R.string.Button_cancel), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mQuit = true;
					doSyncThread.stopThread();
				}
			});
			return mProgress;
		}
		return null;
	}
	
	//Main Message Handler
	//Handle all UI
	Handler mainMsgHandler = new Handler(){

		public void handleMessage(Message msg){
			
			int mProgressVal = msg.arg1;
			int finish = msg.arg2;
			
			Message msgToThread = Message.obtain();
			
			if(mQuit == true){
				dismissDialog(0);
				return;
			}
			
			if(msg.what == SERVER_ERROR){
				String error_message = (String)msg.obj;
				if(error_message.equals(getString(R.string.Alert_wrongUserPass)))
				{
					SharedPreferences.Editor edit = pref.edit();				
					edit.putString("Username","");
					edit.putString("Password","");
					edit.commit();
					
					Toast.makeText(Sync.this, getString(R.string.Alert_messageServer) 
							+ error_message, Toast.LENGTH_LONG).show();
					
					Intent intent = new Intent(Sync.this,Login.class);
					startActivity(intent);
					finish();
					return;
				}
				mProgress.dismiss();
				//dismissDialog(0);
				//removeDialog(0);
				Toast.makeText(Sync.this, getString(R.string.Alert_messageServer) 
						+ error_message, Toast.LENGTH_LONG).show();
			}else if(msg.what < NETWORK_ERROR){
				mProgress.dismiss();
				//removeDialog(0);
				Toast.makeText(Sync.this, getString(R.string.Alert_errorDownload), Toast.LENGTH_SHORT).show();
			}
		
			//Each step would be reached whenever doSyncThread transfer data with server
			switch(msg.what){			
			case SYNC_START:
				mProgress.setProgress(0);
				mProgress.setMessage(getString(R.string.Alert_uploadingSites));
				
				//Start Next Step
				msgToThread.what = UPLOAD_ADDED_SITE;
				msgToThread.arg1 = mProgressVal;
				break;
			case GET_SPECIES_ID:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage("");
				
				msgToThread.what = UPLOAD_ADDED_SITE;
				msgToThread.arg1 = mProgressVal;
				break;
			case UPLOAD_ADDED_SITE:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_uploadingSites));
				
				//Start Next Step
				msgToThread.what = UPLOAD_ADDED_PLANT;
				msgToThread.arg1 = mProgressVal;
				break;
			case UPLOAD_ADDED_PLANT:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_uploadingPlants));
				
				//Trigger Next Step
				msgToThread.what = UPLOAD_QUICK_CAPTURE_PLANT;
				msgToThread.arg1 = mProgressVal;
				break;
			
			case UPLOAD_QUICK_CAPTURE_PLANT:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_uploadingOneTimeOB));
				
				msgToThread.what = UPLOAD_QUICK_CAPTURE_OBSERVATION;
				msgToThread.arg1 = mProgressVal;
				break;	
			// need to modify 	
			case UPLOAD_QUICK_CAPTURE_OBSERVATION:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_uploadingObs));
				
				//Trigger Next Step
				msgToThread.what = UPLOAD_OBSERVATION;
				msgToThread.arg1 = mProgressVal;
				break;
			case UPLOAD_OBSERVATION:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_uploadingObs_Observations));
				
				//Trigger Next Step
				//Check if this is done, then go next stage
				//Otherwise stay this stage.
				if(finish == NOT_FINISH_INT){
					msgToThread.what = UPLOAD_OBSERVATION;
					msgToThread.arg1 = mProgressVal;
				}else{
					msgToThread.what = DOWNLOAD_USER_STATION;
					msgToThread.arg1 = mProgressVal; 
				}
				break;
	
			case DOWNLOAD_USER_STATION:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_downloadingSites));
				
				//Trigger Next Step
				msgToThread.what = DOWNLOAD_USER_PLANTS;
				msgToThread.arg1 = mProgressVal;
				
				break;
			case DOWNLOAD_USER_PLANTS:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_downloadingPlants));
				
				//Trigger Next Step
				msgToThread.what = DOWNLOAD_OBSERVATION;
				msgToThread.arg1 = mProgressVal;
				break;
			case DOWNLOAD_OBSERVATION:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_downloadingObs));
				
				//Trigger Next Step
				msgToThread.what = DOWNLOAD_QUICK_CAPTURE_PLANT;
				msgToThread.arg1 = mProgressVal;
				break;
		
			case DOWNLOAD_QUICK_CAPTURE_PLANT:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_downloadingOneTimeOB));
				
				msgToThread.what = DOWNLOAD_QUICK_CAPTURE_OBSERVATION;
				msgToThread.arg1 = mProgressVal;
				break;
				
			case DOWNLOAD_QUICK_CAPTURE_OBSERVATION:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_downloadingOneTimeOB));
				
				msgToThread.what = DOWNLOAD_OBSERVATION_IMG;
				msgToThread.arg1 = mProgressVal;				

				break;
		
			case DOWNLOAD_OBSERVATION_IMG:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage(getString(R.string.Alert_downloadingImages));
				
				if(finish == 0){
					msgToThread.what = DOWNLOAD_OBSERVATION_IMG;
					msgToThread.arg1 = mProgressVal;
				}else{
					msgToThread.what = SYNC_COMPLETE;
					msgToThread.arg1 = mProgressVal; 
				}
				break;
			case SYNC_COMPLETE:
				mProgress.setProgress(mProgressVal);
				dismissDialog(0);
				removeDialog(0);
				
				//Move to plant list screen
				Intent intent = new Intent(Sync.this, MainPage.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				
				Toast.makeText(Sync.this, getString(R.string.Alert_synchronized), Toast.LENGTH_SHORT).show();
				
				break;
			}
			
			//Send message
			if(msg.what != SYNC_COMPLETE)
				mSyncThread.threadMsgHandler.sendMessage(msgToThread);
		}
	};
	
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
				SyncDBHelper dbhelper = new SyncDBHelper(Sync.this);
				dbhelper.clearAllTable(Sync.this);
				dbhelper.close(); 
				
				//Delete all files in /sdcard
				File sdcard = new File("/sdcard/pbudburst/");
				String[] files = sdcard.list();
				
				for(int i=0; i<files.length; i++){
					File file = new File("/sdcard/pbudburst/" + files[i]);
					file.delete();
				}
				
				Intent intent = new Intent(Sync.this, Login.class);
				startActivity(intent);
				finish();
			}else{
			}
		}
	};
	//Menu option
	/////////////////////////////////////////////////////////////
	
	
}

class doSyncThread extends Thread{
	Handler mMsgHandler;
	static Looper mLoop;
	String serverResponse;
	Context context;
	
	//Sync constants
	final private int SYNC_START = 0;
	final private int GET_SPECIES_ID = 1;
	final private int UPLOAD_ADDED_SITE = 2;
	final private int UPLOAD_ADDED_PLANT = 3;
	final private int UPLOAD_OBSERVATION = 4;
	final private int DOWNLOAD_USER_STATION = 5;
	final private int DOWNLOAD_USER_PLANTS = 6;
	final private int DOWNLOAD_OBSERVATION = 7;
	final private int DOWNLOAD_OBSERVATION_IMG = 8;
	final private int SYNC_COMPLETE = 9;
	
	final private int UPLOAD_QUICK_CAPTURE_PLANT = 10;
	final private int UPLOAD_QUICK_CAPTURE_OBSERVATION = 11;
	final private int DOWNLOAD_QUICK_CAPTURE_PLANT = 12;
	final private int DOWNLOAD_QUICK_CAPTURE_OBSERVATION = 13;
	
	private String TAG = new String("SYNC");
	
	private String username;
	private String password;
	private SharedPreferences pref;
	
	//Boolean constant with integer
	final private int FINISH_INT = 1;
	final private int NOT_FINISH_INT = 0;
	
	
	doSyncThread(Handler handler, Context ctx){
		context = ctx;
		mMsgHandler = handler;
	}
	
	public void run(){
		Looper.prepare();
		pref = context.getSharedPreferences("userinfo",0);
		username = pref.getString("Username","");
		password = pref.getString("Password","");
		
		Message msgToMain = Message.obtain();
		msgToMain.what = SYNC_START;
		mMsgHandler.sendMessage(msgToMain);
		
		mLoop = Looper.myLooper();
		Looper.loop();
	}
	
	public static void stopThread(){
		mLoop.quit();
	}
	
	//Thread Message Handler
	public Handler threadMsgHandler = new Handler(){
		public void handleMessage(Message msg){
			//Receive msg from main class and process uploading first,
			//then process downloading later
			//msg.what => current sync step
			//msg.arg1 => progress value
			//msg.arg2 => flag showing if the work has been done. 
			//(The work means those work that takes long time like uploading downloading img file)

			//Send success or fail message to main class
			//so that main class can keep syncing or stop it

			//At the same time, main class display sync progress to dialog box.
			
			int mProgressVal = msg.arg1;
			Message msgToMain = Message.obtain();
			
			//Open database
	    	SyncDBHelper syncDBHelper = new SyncDBHelper(context);
	    	
	    	OneTimeDBHelper otDBHelper = new OneTimeDBHelper(context);
	    	
	    	
	    	Cursor cursor;
	    	String query;
	    	JSONObject jsonobj;
	    	JSONArray jsonresult;
			
			switch(msg.what){
			
			case SYNC_START:
				msgToMain.what = SYNC_START;
				msgToMain.arg1 = mProgressVal;
				break;
			case GET_SPECIES_ID:

				msgToMain.what = GET_SPECIES_ID;
				msgToMain.arg1 = mProgressVal + 1;
				serverResponse = SyncNetworkHelper.get_species_id(username, password);

				if(serverResponse == null){
					mLoop.quit();
					break;
				}
				
				//Server response check
				try{
					jsonobj = new JSONObject(serverResponse);
					if(jsonobj.getBoolean("success") == false){
						msgToMain.what = Sync.SERVER_ERROR;
						msgToMain.obj = jsonobj.getString("error_message");
						mLoop.quit();
						break;
					}else{
						//Retrieve new site id 
						String max_species_id = jsonobj.getString("results");
							
						SharedPreferences.Editor edit = pref.edit();				
						edit.putInt("other_species_id", Integer.parseInt(max_species_id));
						edit.commit();
						
						Log.i("K", " GET MAX SPECIES ID : " + max_species_id);
					}
				}catch(Exception e){
		            e.printStackTrace();
					Log.e(TAG, e.toString());
					Log.d(TAG, "UPLOAD_ADDED_SITE: failed");
				}
				
				break;
			case UPLOAD_ADDED_SITE:
				msgToMain.what = UPLOAD_ADDED_SITE;
				msgToMain.arg1 = mProgressVal + 3;
				
				SQLiteDatabase syncRDB = syncDBHelper.getReadableDatabase();
		    	SQLiteDatabase syncWDB = syncDBHelper.getWritableDatabase();
				
				query = "SELECT site_id, site_name, latitude, longitude, state, comments " +
						"FROM my_sites WHERE synced=" + SyncDBHelper.SYNCED_NO;
				cursor = syncRDB.rawQuery(query, null);
				
				if(cursor.getCount() == 0){
					cursor.close();
					break;
				}
				
				while(cursor.moveToNext()){
					String site_id =  cursor.getString(0);
					String site_name = cursor.getString(1);
					String latitude = cursor.getString(2);
					String longitude = cursor.getString(3);
					String state = cursor.getString(4);
					String comments = cursor.getString(5);
					
					serverResponse = SyncNetworkHelper.upload_new_site(username, password, 
							site_id, site_name, latitude, longitude, state, comments);
					
					if(serverResponse == null){
						msgToMain.what = UPLOAD_ADDED_SITE* -1;
						mLoop.quit();
						break;
					}
					
					//Server response check
					try{
						jsonobj = new JSONObject(serverResponse);
						if(jsonobj.getBoolean("success") == false){
							msgToMain.what = Sync.SERVER_ERROR;
							msgToMain.obj = jsonobj.getString("error_message");
							mLoop.quit();
							break;
						}else{
							//Retrieve new site id 
							String site_id_from_server = jsonobj.getString("results");
							
							//Update my_plants table with new site id 
							query = "UPDATE my_plants " +
							"SET site_id='" + site_id_from_server + "' " +
							"WHERE site_id='" + site_id+"';";
							syncWDB.execSQL(query);
							
							//Update my_observation table with new site id
							query = "UPDATE my_observation " +
							"SET site_id='" + site_id_from_server + "' " +
							"WHERE site_id='" + site_id+"';";
							syncWDB.execSQL(query);
						}
					}catch(Exception e){
			            e.printStackTrace();
						Log.e(TAG, e.toString());
						Log.d(TAG, "UPLOAD_ADDED_SITE: failed");
					}
					
				}
				syncRDB.close();
				syncWDB.close();
				cursor.close();
				break;
			case UPLOAD_ADDED_PLANT:
				msgToMain.what = UPLOAD_ADDED_PLANT;
				msgToMain.arg1 = mProgressVal + 2;
				
				syncRDB = syncDBHelper.getReadableDatabase();

				
		    	//Open database cursor
		    	query =	"SELECT species_id, site_id, active, common_name " +
		    			"FROM my_plants " +
		    			"WHERE synced=" + SyncDBHelper.SYNCED_NO + ";";
				cursor = syncRDB.rawQuery(query, null);
			    
				Log.d(TAG, String.valueOf(cursor.getCount()));
				//Check if returned data is empty, then break switch statement
				if(cursor.getCount() == 0){
			        cursor.close();
					break;
				}
				else {
					query = "DELETE FROM my_plants;";
					syncRDB.execSQL(query);
				}
				
				while(cursor.moveToNext()){
					String a = cursor.getString(0);
					String b = cursor.getString(1);
					Integer c = cursor.getInt(2);
					String d = cursor.getString(3);
					
					Log.i("K","species_id : " + a + " site_id : " + b + " active : " + c + " common_name : " + d);

					serverResponse = 
					SyncNetworkHelper.upload_new_plant(username, password, context, 
							a, b, c, d);

//					serverResponse = 
//						SyncNetworkHelper.upload_new_plant(username, password, context, 
//								cursor.getString(0), cursor.getString(1));
					if(serverResponse == null){
						//Error occurs. Quit thread. Notify this to main class.
						msgToMain.what = UPLOAD_ADDED_PLANT * -1;
						mLoop.quit();
						break;
					}
					
					//Server response check
					try{
						jsonobj = new JSONObject(serverResponse);
						if(jsonobj.getBoolean("success") == false){
							msgToMain.what = Sync.SERVER_ERROR;
							msgToMain.obj = jsonobj.getString("error_message");
							mLoop.quit();
							break;
						}
					}catch(Exception e){
			            e.printStackTrace();
						Log.e(TAG, e.toString());
						Log.d(TAG, "UPLOAD_ADDED_PLANT: failed");
					}
				}
				syncRDB.close();
				cursor.close();
				break;
				
			case UPLOAD_QUICK_CAPTURE_PLANT:
				msgToMain.what = UPLOAD_QUICK_CAPTURE_PLANT;
				msgToMain.arg1 = mProgressVal + 2;
		
				SQLiteDatabase otRDB = otDBHelper.getReadableDatabase();
		    	//SQLiteDatabase otWDB = otDBHelper.getWritableDatabase();
				
				query = "SELECT plant_id, species_id, site_id, protocol_id, cname, sname FROM onetimeob " +
						"WHERE synced=" + SyncDBHelper.SYNCED_NO + ";";
				cursor = otRDB.rawQuery(query, null);
				
				if(cursor.getCount() == 0) {
					otRDB.close();
					cursor.close();
					break;
				}
				else {
					while(cursor.moveToNext()) {
						int plant_id = cursor.getInt(0);
						Log.i("K", "plant_id : " + plant_id);
						int species_id = cursor.getInt(1);
						int site_id = cursor.getInt(2);
						int protocol_id = cursor.getInt(3);
						String cname = cursor.getString(4);
						String sname = cursor.getString(5);
						
						serverResponse = 
							SyncNetworkHelper.upload_onetime_ob(username, password, plant_id, species_id, site_id, protocol_id, cname, sname);
						
						if(!serverResponse.equals("UPLOADED_OK")) {
							msgToMain.what = Sync.SERVER_ERROR;
							msgToMain.obj = "Upload Error - One time Observation";
							mLoop.quit();
							break;
						}
					}
				}
				
				otRDB.close();
				cursor.close();
				break;
				
			case UPLOAD_OBSERVATION:
				msgToMain.what = UPLOAD_OBSERVATION;
		    	msgToMain.arg1 = mProgressVal;

		    	syncRDB = syncDBHelper.getReadableDatabase();
		    	syncWDB = syncDBHelper.getWritableDatabase();
		   
		    	//Open database cursor
		    	query = "SELECT species_id, site_id, phenophase_id, time, note, image_id, _id " +
		    			"FROM my_observation " +
		    			"WHERE synced=" + SyncDBHelper.SYNCED_NO + ";";
		    	
		    	
				cursor = syncRDB.rawQuery(query, null);
				
				//Check if returned data is empty, then break switch statement
				if(cursor.getCount() == 0){
					msgToMain.arg1 = 30;
			        msgToMain.arg2 = FINISH_INT;
					cursor.close();
					break;
				}else{
			        msgToMain.arg2 = NOT_FINISH_INT;
					cursor.moveToNext();
					
					Log.i("K", "OOO : " + cursor.getString(0));
					
					
					serverResponse = SyncNetworkHelper.upload_new_obs(username, password, context,
							cursor.getString(0), cursor.getString(1), cursor.getString(2),
							cursor.getString(3), cursor.getString(4), cursor.getString(5));
					if(serverResponse == null){
						//Error occurs. Quit thread. Notify this to main class.
						msgToMain.what = UPLOAD_OBSERVATION * -1;
						mLoop.quit();
					}
					
					//Server response check
					try{
						jsonobj = new JSONObject(serverResponse);
						if(jsonobj.getBoolean("success") == false){
							msgToMain.what = Sync.SERVER_ERROR;
							msgToMain.obj = jsonobj.getString("error_message");
							mLoop.quit();
							break;
						}
					}catch(Exception e){
						Log.e(TAG, e.toString());
						Log.d(TAG, "UPLOAD_OBSERVATION: failed");
					}
					
					//Set progress
					if(mProgressVal < 30) //Up to 40
						msgToMain.arg1 = mProgressVal + 1;
					
					query = "UPDATE my_observation " +
							"SET synced=" + SyncDBHelper.SYNCED_YES + " " +
							"WHERE _id=" + cursor.getString(6)+";";
					syncWDB.execSQL(query);
				}
				syncWDB.close();
				cursor.close();
				break;
				
			case UPLOAD_QUICK_CAPTURE_OBSERVATION:
				
				msgToMain.what = UPLOAD_QUICK_CAPTURE_OBSERVATION;
		    	msgToMain.arg1 = mProgressVal;

		    	otRDB = otDBHelper.getReadableDatabase();
		    	
		    	//Open database cursor
		    	query = "SELECT plant_id, phenophase_id, lat, lng, image_id, dt_taken, notes " +
		    			"FROM onetimeob_observation " +
		    			"WHERE synced=" + SyncDBHelper.SYNCED_NO + ";";
		    	
				cursor = otRDB.rawQuery(query, null);
				
				//Check if returned data is empty, then break switch statement
				if(cursor.getCount() == 0){
					msgToMain.arg1 = 30;
			        msgToMain.arg2 = FINISH_INT;
			        otRDB.close();
					cursor.close();
					break;
				}else{
			        msgToMain.arg2 = NOT_FINISH_INT;

					while(cursor.moveToNext()) {
						
						
						Log.i("K", cursor.getInt(0) + " , " + 
								cursor.getInt(1) + " , " + 
								cursor.getDouble(2) + " , " +
								cursor.getDouble(3) + " , " +
								cursor.getString(4) + " , " +
								cursor.getString(5) + " , " +
								cursor.getString(6));
						
						
						serverResponse = SyncNetworkHelper.upload_quick_observations(username, password, context,
								cursor.getInt(0),
								cursor.getInt(1),
								cursor.getDouble(2),
								cursor.getDouble(3),
								cursor.getString(4),
								cursor.getString(5),
								cursor.getString(6));
						
						if(!serverResponse.equals("UPLOADED_OK")) {
							msgToMain.what = Sync.SERVER_ERROR;
							msgToMain.obj = "Upload Error - One time Observation";
							mLoop.quit();
							
							System.exit(0);
							break;
						}
					}
				}
				
				otRDB.close();
				cursor.close();
				break;
				
				

			case DOWNLOAD_USER_STATION:
				msgToMain.what = DOWNLOAD_USER_STATION;
		    	msgToMain.arg1 = mProgressVal + 5;
		    	
		    	syncRDB = syncDBHelper.getReadableDatabase();
		    	syncWDB = syncDBHelper.getWritableDatabase();
		    	
				serverResponse = SyncNetworkHelper.download_json(
						context.getString(R.string.get_my_sites_URL)
						+"&username="+ username	+"&password="+ password);
				if(serverResponse == null){
					msgToMain.what = DOWNLOAD_USER_STATION * -1;
					mLoop.quit();
				}
				
				//Server response check
				try{
					jsonobj = new JSONObject(serverResponse);
					if(jsonobj.getBoolean("success") == false){
						msgToMain.what = Sync.SERVER_ERROR;
						msgToMain.obj = jsonobj.getString("error_message");
						mLoop.quit();
						break;
					}
				}catch(Exception e){
					Log.e(TAG, e.toString());
					Log.d(TAG, "DOWNLOAD_USER_STATION: failed");
				}
				
				//when serverResponse 'success' is true
				try{
					jsonobj = new JSONObject(serverResponse);
					
					if(jsonobj.getString("results").equals("USER SITE LIST IS EMPTY")){
						//User site list is empty. Later plant list activity prompts user
						// to add new site.
						break;
					}
					
					
					jsonresult = new JSONArray(jsonobj.getString("results"));
					syncWDB.execSQL("DELETE FROM my_sites;");
					for(int i=0; i<jsonresult.length(); i++){
						syncWDB.execSQL("INSERT INTO my_sites " +
								"(site_id, site_name, latitude, longitude, state, comments, synced, official)" +
								"VALUES(" +
								"'" + jsonresult.getJSONObject(i).getString("_id") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("name") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("latitude") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("longitude") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("state") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("comments") + "'," +
								SyncDBHelper.SYNCED_YES + "," +
								1 + ");");
					}
					Log.d(TAG, "DOWNLOAD_MY_SPECIES_IN_MY_STATION: success to store into db");
				}catch(Exception e){
					Log.e(TAG, e.toString());
					Log.d(TAG, "DOWNLOAD_MY_SPECIES_IN_MY_STATION: failed to store into db");
				}
				
				syncRDB.close();
				syncWDB.close();
				
				break;
			case DOWNLOAD_USER_PLANTS:
				msgToMain.what = DOWNLOAD_USER_PLANTS;
		    	msgToMain.arg1 = mProgressVal + 5;
				
		    	syncRDB = syncDBHelper.getReadableDatabase();
		    	syncWDB = syncDBHelper.getWritableDatabase();
		    	
				serverResponse = SyncNetworkHelper
				.download_json(context.getString(R.string.get_my_species_in_my_station_URL)
						+"&username="+ username	+"&password="+ password);
				if(serverResponse == null){
					msgToMain.what = DOWNLOAD_USER_PLANTS * -1;
					mLoop.quit();
				}
				
				try{
					
					Log.i("K", "SERVER RESPONSE : " + serverResponse);
					
					jsonobj = new JSONObject(serverResponse);
					
					//Server response check
					if(jsonobj.getBoolean("success") == false){
						msgToMain.what = Sync.SERVER_ERROR;
						msgToMain.obj = jsonobj.getString("error_message");
						mLoop.quit();
						break;
					}
					
					if(jsonobj.getString("results").equals("USER PLANT LIST IS EMPTY")){
						//User plant list is empty. Later PlantList activity prompts user
						// to add new plant.
						break;
					}
		
					jsonresult = new JSONArray(jsonobj.getString("results"));
					syncWDB.execSQL("DELETE FROM my_plants;");
					for(int i=0; i<jsonresult.length(); i++){
						
						int species_id = Integer.parseInt(jsonresult.getJSONObject(i).getString("spc_id"));
						
						Log.i("K", "INSERT INTO my_plants " +
								"(species_id, site_id, site_name, protocol_id, common_name, active, synced)" +
								"VALUES(" +
								species_id + "," +
								jsonresult.getJSONObject(i).getString("st_id") + "," +
								"'"+
								jsonresult.getJSONObject(i).getString("st_name") + "'," +
								jsonresult.getJSONObject(i).getString("pro_id") + "," +
								"'"+
								jsonresult.getJSONObject(i).getString("c_name") + "'," +
								"1," +
								SyncDBHelper.SYNCED_YES + ");");
						
						
						
						//if(species_id > 76) {
						//	species_id = 77;
						//}
						
						syncWDB.execSQL("INSERT INTO my_plants " +
								"(species_id, site_id, site_name, protocol_id, common_name, active, synced)" +
								"VALUES(" +
								species_id + "," +
								jsonresult.getJSONObject(i).getString("st_id") + "," +
								"'"+
								jsonresult.getJSONObject(i).getString("st_name") + "'," +
								jsonresult.getJSONObject(i).getString("pro_id") + "," +
								"'"+
								jsonresult.getJSONObject(i).getString("c_name") + "'," +
								"1," +
								SyncDBHelper.SYNCED_YES + ");");
					}
					Log.d(TAG, "DOWNLOAD_USER_PLANTS: success to store into db");

				}catch(Exception e){
					Log.e(TAG, e.toString());
					Log.d(TAG, "DOWNLOAD_USER_PLANTS: failed to store into db");
				}
				
				syncRDB.close();
				syncWDB.close();
				
				break;
			case DOWNLOAD_OBSERVATION:
				msgToMain.what = DOWNLOAD_OBSERVATION;
		    	msgToMain.arg1 = mProgressVal + 5;
				
		    	syncRDB = syncDBHelper.getReadableDatabase();
		    	syncWDB = syncDBHelper.getWritableDatabase();
		    	
				serverResponse = SyncNetworkHelper
				.download_json(context.getString(R.string.get_observation_URL)
						+"&username="+ username	+"&password="+ password);
				if(serverResponse == null){
					msgToMain.what = DOWNLOAD_OBSERVATION * -1;
					mLoop.quit();
				}
				
				//Parse response and insert it to DB
				try{
					jsonobj = new JSONObject(serverResponse);
					
					if(jsonobj.getString("results").equals("OBS LIST IS EMPTY")){
						//User plant list is empty. Later PlantList activity prompts user
						// to add new plant.
						break;
					}
					
					//Server response check
					if(jsonobj.getBoolean("success") == false){
						msgToMain.what = Sync.SERVER_ERROR;
						msgToMain.obj = jsonobj.getString("error_message");
						mLoop.quit();
						break;
					}
					
					jsonresult = new JSONArray(jsonobj.getString("results"));
					syncWDB.execSQL("DELETE FROM my_observation;");
					for(int i=0; i<jsonresult.length(); i++){
						query = "INSERT INTO my_observation " +
						"(species_id, site_id, phenophase_id, image_id, time, note, synced)" +
						"VALUES(" +
						jsonresult.getJSONObject(i).getString("species_id") + "," +
						jsonresult.getJSONObject(i).getString("site_id") + "," +
						jsonresult.getJSONObject(i).getString("phenophase_id") + "," +
						"'"+
						jsonresult.getJSONObject(i).getString("image_id") + "'," +
						"'"+
						jsonresult.getJSONObject(i).getString("time") + "'," +
						"'"+
						jsonresult.getJSONObject(i).getString("note") + "'," + 
						SyncDBHelper.IMG_FILE_NEED_TO_BE_DOWNLOADED + ");";
						
						syncWDB.execSQL(query);
					}
					
					Log.d(TAG, "DOWNLOAD_OBSERVATION: success to store into db");
				}catch(Exception e){
					Log.e(TAG, e.toString());
					Log.d(TAG, "DOWNLOAD_OBSERVATION: failed to store into db");
				}		
				
				syncRDB.close();
				syncWDB.close();
				
				break;
				
			
				
			case DOWNLOAD_QUICK_CAPTURE_PLANT:
				msgToMain.what = DOWNLOAD_QUICK_CAPTURE_PLANT;
		    	msgToMain.arg1 = mProgressVal + 5;
		    	
		    	Log.i("K", "Start DOWNLOAD_QUICK_CAPTURE_PLANT");
		    	
		    	SQLiteDatabase onetime = otDBHelper.getWritableDatabase();
		    	
		    	Log.i("K", "URL : " + context.getString(R.string.get_onetime_plant_URL)
						+"?username="+ username	+"&password="+ password);
		    	
				serverResponse = SyncNetworkHelper
				.download_json(context.getString(R.string.get_onetime_plant_URL)
						+"?username="+ username	+"&password="+ password);
				if(serverResponse == null){
					msgToMain.what = DOWNLOAD_QUICK_CAPTURE_PLANT * -1;
					mLoop.quit();
					System.exit(0);
				}
				
				try{
					
					Log.i("K", "SERVER RESPONSE : " + serverResponse);
					
					jsonobj = new JSONObject(serverResponse);
					
					//Server response check
					if(jsonobj.getBoolean("success") == false){
						msgToMain.what = Sync.SERVER_ERROR;
						msgToMain.obj = jsonobj.getString("error_message");
						mLoop.quit();
						break;
					}
					
					if(jsonobj.getString("results").equals("ONETIME PLANT LIST IS EMPTY")){
						//User plant list is empty. Later PlantList activity prompts user
						// to add new plant.
						onetime.close();
						break;
					}
		
					jsonresult = new JSONArray(jsonobj.getString("results"));
					// delete first
					onetime.execSQL("DELETE FROM onetimeob;");
					for(int i=0; i<jsonresult.length(); i++){
						
						onetime.execSQL("INSERT INTO onetimeob " +
								"(plant_id, species_id, site_id, protocol_id, cname, sname, synced)" +
								"VALUES(" +
								jsonresult.getJSONObject(i).getString("Plant_ID") + "," +
								jsonresult.getJSONObject(i).getString("Species_ID") + "," +
								jsonresult.getJSONObject(i).getString("Site_ID") + "," +
								jsonresult.getJSONObject(i).getString("Protocol_ID") + ",'" +
								jsonresult.getJSONObject(i).getString("Common_Name") + "','" +
								jsonresult.getJSONObject(i).getString("Science_Name") + "'," +
								SyncDBHelper.SYNCED_YES + ");");
					}
					onetime.close();
					Log.d(TAG, "DOWNLOAD_ONETIME_PLANTS: success to store into db");

				}catch(Exception e){
					Log.e(TAG, e.toString());
					Log.d(TAG, "DOWNLOAD_ONETIME_PLANTS: failed to store into db");
				}
				
				break;
				
				
			case DOWNLOAD_QUICK_CAPTURE_OBSERVATION:
				
				Log.i("K", "Start DOWNLOAD_QUICK_CAPTURE_OBSERVATION");
				
				msgToMain.what = DOWNLOAD_QUICK_CAPTURE_OBSERVATION;
		    	msgToMain.arg1 = mProgressVal + 5;
		    	
		    	onetime = otDBHelper.getWritableDatabase();
		    	
		    	Log.i("K", "URL : " + context.getString(R.string.get_onetime_observation_URL)
						+"?username="+ username	+"&password="+ password);
		    	
				serverResponse = SyncNetworkHelper
				.download_json(context.getString(R.string.get_onetime_observation_URL)
						+"?username="+ username	+"&password="+ password);
				if(serverResponse == null){
					msgToMain.what = DOWNLOAD_QUICK_CAPTURE_PLANT * -1;
					mLoop.quit();
					System.exit(0);
				}
				
				try{
					
					Log.i("K", "SERVER RESPONSE : " + serverResponse);
					
					jsonobj = new JSONObject(serverResponse);
					
					//Server response check
					if(jsonobj.getBoolean("success") == false){
						msgToMain.what = Sync.SERVER_ERROR;
						msgToMain.obj = jsonobj.getString("error_message");
						mLoop.quit();
						break;
					}
					
					if(jsonobj.getString("results").equals("ONETIME OBSERVATION LIST IS EMPTY")){
						//User plant list is empty. Later PlantList activity prompts user
						// to add new plant.
						onetime.close();
						break;
					}
		
					jsonresult = new JSONArray(jsonobj.getString("results"));
					// empty items in the onetimeob_observation table
					onetime.execSQL("DELETE FROM onetimeob_observation;");
					for(int i=0; i<jsonresult.length(); i++){
						
						String has_image_flag = jsonresult.getJSONObject(i).getString("Has_Image");
						int image_flag = Integer.parseInt(has_image_flag);
						
						String image_name = "";
						if(image_flag == 1) {
							image_name = "quick_" + jsonresult.getJSONObject(i).getString("Image_ID");
						}
						
						
						onetime.execSQL("INSERT INTO onetimeob_observation " +
								"(plant_id, phenophase_id, lat, lng, image_id, dt_taken, notes, synced)" +
								"VALUES(" +
								jsonresult.getJSONObject(i).getString("Plant_ID") + "," +
								jsonresult.getJSONObject(i).getString("Phenophase_ID") + "," +
								jsonresult.getJSONObject(i).getString("Latitude") + "," +
								jsonresult.getJSONObject(i).getString("Longitude") + ",'" +
								image_name + "','" +
								jsonresult.getJSONObject(i).getString("Dates") + "','" +
								jsonresult.getJSONObject(i).getString("Notes") + "'," +
								SyncDBHelper.SYNCED_YES + ");");
	
						
						if(image_flag == 1) {
							SyncNetworkHelper.download_image_for_onetime(context.getString(R.string.download_onetime_image_URL), Integer.parseInt(jsonresult.getJSONObject(i).getString("Image_ID")));
						}
					}
					onetime.close();
					Log.d(TAG, "DOWNLOAD_ONETIME_OBSERVATIONS: success to store into db");

				}catch(Exception e){
					Log.e(TAG, e.toString());
					Log.d(TAG, "DOWNLOAD_ONETIME_OBSERVATIONS: failed to store into db");
				}
				
				break;
				
							
			case DOWNLOAD_OBSERVATION_IMG:
				msgToMain.what = DOWNLOAD_OBSERVATION_IMG;
		    	msgToMain.arg1 = mProgressVal;
		    	
		    	syncRDB = syncDBHelper.getReadableDatabase();
		    	syncWDB = syncDBHelper.getWritableDatabase();
		    	
				cursor = syncRDB.rawQuery("SELECT image_id, _id FROM my_observation" +
						" WHERE synced=" + SyncDBHelper.IMG_FILE_NEED_TO_BE_DOWNLOADED +
						" AND image_id>0;",null);
				
				//Check if returned data is empty, then break switch statement
				if(cursor.getCount() == 0){
					msgToMain.arg1 = 99;//Set 
			        msgToMain.arg2 = FINISH_INT;
					cursor.close();
					break;
				}else{
			        msgToMain.arg2 = NOT_FINISH_INT;
					
			        //Move cursor to get values
			        cursor.moveToNext();
					int image_id = cursor.getInt(0);
					if(image_id == 0){
						//Update my_observation to prevent duplicated downloading
						query = "UPDATE my_observation " +
						"SET synced=" + SyncDBHelper.SYNCED_YES + " " +
						"WHERE _id=" + cursor.getString(1)+";";
						syncWDB.execSQL(query);
						
						syncWDB.close();
						cursor.close();
						break;
					}
						
					
					//Check progress values if it's less than 99
					if(mProgressVal < 99)
						msgToMain.arg1 = mProgressVal + 1;
					serverResponse = SyncNetworkHelper
					.download_image(context.getString(R.string.download_obs_image_URL), image_id)
					.toString();
					
					
					//Check if serverResponse is false 
					if(serverResponse.equals("false")){
						msgToMain.what = DOWNLOAD_OBSERVATION_IMG * -1;
						mLoop.quit();
					}
					
					//Update my_observation to prevent duplicated downloading
					query = "UPDATE my_observation " +
					"SET synced="+ SyncDBHelper.SYNCED_YES + " " +
					"WHERE _id=" + cursor.getString(1)+";";
					syncWDB.execSQL(query);
					syncWDB.close();
					cursor.close();
				}
				syncRDB.close();
				syncWDB.close();
				break;
			
			case SYNC_COMPLETE:
				msgToMain.what = SYNC_COMPLETE;
		    	msgToMain.arg1 = 100;
				break;
			}
			
			//TODO: parse serverResponse json object file

			//Send a msg to main to handle upload and download result.
			mMsgHandler.sendMessage(msgToMain);
			syncDBHelper.close();
		}
	};

}
