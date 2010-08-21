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
	final private int SYNC_START = 1;
	final private int UPLOAD_ADDED_SITE = 2;
	final private int UPLOAD_ADDED_PLANT = 3;
	final private int UPLOAD_OBSERVATION = 4;
	final private int DOWNLOAD_USER_STATION = 5;
	final private int DOWNLOAD_USER_PLANTS = 6;
	final private int DOWNLOAD_OBSERVATION = 7;
	final private int DOWNLOAD_OBSERVATION_IMG = 8;
	final private int SYNC_COMPLETE = 9;
	
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
		textViewHello.setText("Hello " + username + ",\n" + getString(R.string.instruction));
		
		//Sync button
		Button buttonSync = (Button)findViewById(R.id.sync);
		buttonSync.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				doSync();
			}
			}
		);
		
		//My plant button
		Button buttonMyplant = (Button)findViewById(R.id.myplant);
		buttonMyplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {

			}
		}
		);

		//Shared plant button
		Button buttonSharedplant = (Button)findViewById(R.id.sharedplant);
		buttonSharedplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				Toast.makeText(Sync.this,"Coming soon..!",Toast.LENGTH_SHORT).show();
			}
			}
		);
		
		buttonMyplant.setSelected(true);

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
		
//		SubMenu addButton = menu.addSubMenu("Add")
//			.setIcon(android.R.drawable.ic_menu_add);
//		addButton.add(0,MENU_ADD_PLANT,0,"Add Plant");
//		addButton.add(0,MENU_ADD_SITE,0,"Add Site");		
		
		menu.add(0,MENU_SYNC,0,"Sync").setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0,MENU_LOGOUT,0,"Log out").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
//			case MENU_ADD_PLANT:
//				intent = new Intent(Sync.this, AddPlant.class);
//				startActivity(intent);
//				finish();
//				return true;
//			case MENU_ADD_SITE:
//				intent = new Intent (Intent.ACTION_VIEW);
//				intent.setData(Uri.parse(getString(R.string.add_site_URL)));
//				startActivity(intent);
//				finish();
//				return true;
			case MENU_SYNC:
//				SharedPreferences.Editor edit = pref.edit();				
//				edit.putString("Synced","true");
//				edit.commit();
				doSync();
				return true;
			case MENU_LOGOUT:
				new AlertDialog.Builder(Sync.this)
					.setTitle("Question")
					.setMessage("You might lose your unsynced data if you log out. Do you want to log out?")
					.setPositiveButton("Yes",mClick)
					.setNegativeButton("no",mClick)
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
			mProgress.setTitle("Syncing...");
			mProgress.setMessage("Wait...");
			mProgress.setCancelable(false);
			mProgress.setButton("Cancel", new DialogInterface.OnClickListener() {
				
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
				if(error_message.equals("Wrong username and password"))
				{
					SharedPreferences.Editor edit = pref.edit();				
					edit.putString("Username","");
					edit.putString("Password","");
					edit.commit();
					
					Toast.makeText(Sync.this, "Message from server:\n" 
							+ error_message, Toast.LENGTH_LONG).show();
					
					Intent intent = new Intent(Sync.this,Login.class);
					startActivity(intent);
					finish();
					return;
				}
				dismissDialog(0);
				removeDialog(0);
				Toast.makeText(Sync.this, "Message from server:\n" 
						+ error_message, Toast.LENGTH_LONG).show();
			}else if(msg.what < NETWORK_ERROR){
				dismissDialog(0);
				removeDialog(0);
				Toast.makeText(Sync.this, "Network error occurs. Please " +
						"check your network status and try again.", Toast.LENGTH_SHORT).show();
			}
		
			//Each step would be reached whenever doSyncThread transfer data with server
			switch(msg.what){
			case SYNC_START:
				mProgress.setProgress(0);
				mProgress.setMessage("Uploading new sites");
				
				//Start Next Step
				msgToThread.what = UPLOAD_ADDED_SITE;
				msgToThread.arg1 = mProgressVal;
				break;
			case UPLOAD_ADDED_SITE:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage("Uploading new sites");
				
				//Start Next Step
				msgToThread.what = UPLOAD_ADDED_PLANT;
				msgToThread.arg1 = mProgressVal;
				break;
			case UPLOAD_ADDED_PLANT:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage("Uploading new plants");
				
				//Trigger Next Step
				msgToThread.what = UPLOAD_OBSERVATION;
				msgToThread.arg1 = mProgressVal;
				break;
			case UPLOAD_OBSERVATION:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage("Uploading your observation");
				
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
				mProgress.setMessage("Downloading your site");
				
				//Trigger Next Step
				msgToThread.what = DOWNLOAD_USER_PLANTS;
				msgToThread.arg1 = mProgressVal;
				
				break;
			case DOWNLOAD_USER_PLANTS:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage("Downloading your plant");
				
				//Trigger Next Step
				msgToThread.what = DOWNLOAD_OBSERVATION;
				msgToThread.arg1 = mProgressVal;
				break;
			case DOWNLOAD_OBSERVATION:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage("Downloading observation");
				
				//Trigger Next Step
				msgToThread.what = DOWNLOAD_OBSERVATION_IMG;
				msgToThread.arg1 = mProgressVal;
				break;
			case DOWNLOAD_OBSERVATION_IMG:
				mProgress.setProgress(mProgressVal);
				mProgress.setMessage("Downloading image files");
				
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
				Intent intent = new Intent(Sync.this, PlantList.class);
				startActivity(intent);
				finish();
				
				Toast.makeText(Sync.this, "Sync done.", Toast.LENGTH_SHORT).show();
				
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
	final private int SYNC_START = 1;
	final private int UPLOAD_ADDED_SITE = 2;
	final private int UPLOAD_ADDED_PLANT = 3;
	final private int UPLOAD_OBSERVATION = 4;
	final private int DOWNLOAD_USER_STATION = 5;
	final private int DOWNLOAD_USER_PLANTS = 6;
	final private int DOWNLOAD_OBSERVATION = 7;
	final private int DOWNLOAD_OBSERVATION_IMG = 8;
	final private int SYNC_COMPLETE = 9;
	
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
	    	SQLiteDatabase syncRDB = syncDBHelper.getReadableDatabase();
	    	SQLiteDatabase syncWDB = syncDBHelper.getWritableDatabase();
	    	Cursor cursor;
	    	String query;
	    	JSONObject jsonobj;
	    	JSONArray jsonresult;
			
			switch(msg.what){
			case UPLOAD_ADDED_SITE:
				msgToMain.what = UPLOAD_ADDED_SITE;
				msgToMain.arg1 = mProgressVal + 5;
				
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
				cursor.close();
				break;
			case UPLOAD_ADDED_PLANT:
				msgToMain.what = UPLOAD_ADDED_PLANT;
				msgToMain.arg1 = mProgressVal + 5;
				
		    	//Open database cursor
		    	query =	"SELECT species_id, site_id " +
		    			"FROM my_plants " +
		    			"WHERE synced=" + SyncDBHelper.SYNCED_NO + ";";
				cursor = syncRDB.rawQuery(query, null);
			    
				Log.d(TAG, String.valueOf(cursor.getCount()));
				//Check if returned data is empty, then break switch statement
				if(cursor.getCount() == 0){
			        cursor.close();
					break;
				}
				
				while(cursor.moveToNext()){
					String a = cursor.getString(0);
					String b = cursor.getString(1);
					serverResponse = 
					SyncNetworkHelper.upload_new_plant(username, password, context, 
							a, b);

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
				cursor.close();
				break;
			case UPLOAD_OBSERVATION:
				msgToMain.what = UPLOAD_OBSERVATION;
		    	msgToMain.arg1 = mProgressVal;

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
				cursor.close();
				break;
			case DOWNLOAD_USER_STATION:
				msgToMain.what = DOWNLOAD_USER_STATION;
		    	msgToMain.arg1 = mProgressVal + 5;
		    	
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
								"(site_id, site_name, latitude, longitude, city" +
								", state, zipcode, country, comments, synced)" +
								"VALUES(" +
								"'" + jsonresult.getJSONObject(i).getString("_id") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("name") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("latitude") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("longitude") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("city") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("state") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("postal") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("country") + "'," +
								"'" + jsonresult.getJSONObject(i).getString("comments") + "'," +
								SyncDBHelper.SYNCED_YES + ");");
					}
					Log.d(TAG, "DOWNLOAD_MY_SPECIES_IN_MY_STATION: success to store into db");
				}catch(Exception e){
					Log.e(TAG, e.toString());
					Log.d(TAG, "DOWNLOAD_MY_SPECIES_IN_MY_STATION: failed to store into db");
				}
				break;
			case DOWNLOAD_USER_PLANTS:
				msgToMain.what = DOWNLOAD_USER_PLANTS;
		    	msgToMain.arg1 = mProgressVal + 5;
				
				serverResponse = SyncNetworkHelper
				.download_json(context.getString(R.string.get_my_species_in_my_station_URL)
						+"&username="+ username	+"&password="+ password);
				if(serverResponse == null){
					msgToMain.what = DOWNLOAD_USER_PLANTS * -1;
					mLoop.quit();
				}
				
				try{
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
						syncWDB.execSQL("INSERT INTO my_plants " +
								"(species_id, site_id, site_name, protocol_id, synced)" +
								"VALUES(" +
								jsonresult.getJSONObject(i).getString("spc_id") + "," +
								jsonresult.getJSONObject(i).getString("st_id") + "," +
								"'"+
								jsonresult.getJSONObject(i).getString("st_name") + "'," +
								jsonresult.getJSONObject(i).getString("pro_id") + "," +
								SyncDBHelper.SYNCED_YES + ");");
					}
					Log.d(TAG, "DOWNLOAD_USER_PLANTS: success to store into db");

				}catch(Exception e){
					Log.e(TAG, e.toString());
					Log.d(TAG, "DOWNLOAD_USER_PLANTS: failed to store into db");
				}
				break;
			case DOWNLOAD_OBSERVATION:
				msgToMain.what = DOWNLOAD_OBSERVATION;
		    	msgToMain.arg1 = mProgressVal + 5;
				
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
						"(species_id, site_id, phenophase_id, image_id, time, note,synced)" +
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
				break;
			case DOWNLOAD_OBSERVATION_IMG:
				msgToMain.what = DOWNLOAD_OBSERVATION_IMG;
		    	msgToMain.arg1 = mProgressVal;
		    	
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
					
					cursor.close();
				}
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
