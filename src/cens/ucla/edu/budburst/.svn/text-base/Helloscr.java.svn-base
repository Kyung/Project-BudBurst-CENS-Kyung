package cens.ucla.edu.budburst;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

public class Helloscr extends Activity{

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
	
	ProgressDialog mProgress;
	boolean mQuit;
	UpdateThread mThread;
	private boolean cancelBtnClickedFlag = false;
			
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helloscr);
		
		//Call SyncNetworkHelper
		sync = new SyncNetworkHelper();
		
		//Retrieve username and password
		pref = getSharedPreferences("userinfo",0);
		username = pref.getString("Username","");
		password = pref.getString("Password","");
		
		//Display instruction message
		TextView textViewHello = (TextView)findViewById(R.id.hello_textview);
		textViewHello.setText("Hello " + username + ",\n" + getString(R.string.instruction));
		
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
				Toast.makeText(Helloscr.this,"Coming soon..!",Toast.LENGTH_SHORT).show();
			}
			}
		);
		
		//Sync button
		Button buttonSync = (Button)findViewById(R.id.sync);
		buttonSync.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				SharedPreferences.Editor edit = pref.edit();				
				edit.putString("Synced","true");
				edit.commit();
				
				//Show progress
				showDialog(0);
				mProgress.setProgress(0);
				
				//Call a thread to transfer data
				mQuit = false;
				mThread = new UpdateThread();
				mThread.start();
				
			}
			}
		);
		
		buttonMyplant.setSelected(true);
		syncDBHelper = new SyncDBHelper(Helloscr.this);
		
		Intent parent_intent = getIntent();
		if(parent_intent.getExtras() != null &&
				parent_intent.getExtras().getBoolean("sync_instantly",false)){
			//Display pregress dialog
			showDialog(0);
			mProgress.setProgress(0);
			
			//Run thread
			mQuit = false;
			mThread = new UpdateThread();
			mThread.start();
		}
	}
	
	/////////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		SubMenu addButton = menu.addSubMenu("Add")
			.setIcon(android.R.drawable.ic_menu_add);
		addButton.add(0,MENU_ADD_PLANT,0,"Add Plant");
		addButton.add(0,MENU_ADD_SITE,0,"Add Site");		
		
		menu.add(0,MENU_SYNC,0,"Sync").setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0,MENU_LOGOUT,0,"Log out").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case MENU_ADD_PLANT:
				intent = new Intent(Helloscr.this, AddPlant.class);
				startActivity(intent);
				finish();
				return true;
			case MENU_ADD_SITE:
				intent = new Intent (Intent.ACTION_VIEW);
				intent.setData(Uri.parse(getString(R.string.add_site_URL)));
				startActivity(intent);
				finish();
				return true;
			case MENU_SYNC:
				SharedPreferences.Editor edit = pref.edit();				
				edit.putString("Synced","true");
				edit.commit();
				
				//Display pregress dialog
				showDialog(0);
				mProgress.setProgress(0);
				
				//Run thread
				mQuit = false;
				mThread = new UpdateThread();
				mThread.start();
				
				return true;
	
			case MENU_LOGOUT:
				new AlertDialog.Builder(Helloscr.this)
					.setTitle("Question")
					.setMessage("You might lose your unsynced data if you log out. Do you want to log out?")
					.setPositiveButton("Yes",mClick)
					.setNegativeButton("no",mClick)
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
				SyncDBHelper dbhelper = new SyncDBHelper(Helloscr.this);
				dbhelper.clearAllTable(Helloscr.this);
				dbhelper.close(); 
				
				String[] filelist = fileList();
				File sdcard = new File("/sdcard/pbudburst/");
				String[] files = sdcard.list();
				
				for(int i=0; i<files.length; i++){
					File file = new File("/sdcard/pbudburst/" + files[i]);
					file.delete();
				}
				
				Intent intent = new Intent(Helloscr.this, Login.class);
				startActivity(intent);
				finish();
			}else{
			}
		}
	};
	//Menu option
	/////////////////////////////////////////////////////////////
	
	//Thread
	class UpdateThread extends Thread{

		boolean continueFlag=true;

		public void run(){
			String json_result;
			
		//1.Upload user newly added plant
//			if(continueFlag){
//				//json_result = sync.upload_new_plant(username, password, Helloscr.this);
//				if(json_result.equals("No new plant")){
//					Log.d(TAG,"No new plant");
//				}
//				else if(json_result != null){
//					Message msg = mHandler.obtainMessage();
//					msg.arg1 = 10;
//					msg.arg2 = UPLOAD_ADDED_PLANT;
//					msg.obj = json_result;
//					mHandler.sendMessage(msg);
//					try{Thread.sleep(80);}catch(Exception e){;}
//				}
//			}else{
//				Message msg = mHandler.obtainMessage();
//				msg.arg1 = -1;
//				msg.obj = new String("Error occurs while uploading new plant.");
//				mHandler.sendMessage(msg);
//				continueFlag = false;
//				return;
//			}
			
		//2.UPLOAD_OBSERVATION
//			if(continueFlag){
//				//json_result = sync.upload_new_obs(username, password, Helloscr.this);
//				if(json_result.equals("No new obs")){
//					Log.d(TAG,"No new obs");
//				}
//				else if(json_result != null){
//					Message msg = mHandler.obtainMessage();
//					msg.arg1 = 20;
//					msg.arg2 = UPLOAD_OBSERVATION;
//					msg.obj = json_result;
//					mHandler.sendMessage(msg);
//					try{Thread.sleep(80);}catch(Exception e){;}
//				}
//			}else{
//				Message msg = mHandler.obtainMessage();
//				msg.arg1 = -1;
//				msg.obj = new String("Error occurs while uploading new observation.");
//				mHandler.sendMessage(msg);
//				continueFlag = false;
//				return;
//			}
			
			
		//3.Download user species in user's station
			if(continueFlag && 
					(json_result = sync.download_json(
					getString(R.string.get_my_species_in_my_station_URL)
					+"&username="+ username	+"&password="+ password)) != null
					){
				
				//Send Message to handler to show progress
				Message msg = mHandler.obtainMessage();
				msg.arg1 = 30;
				msg.arg2 = DOWNLOAD_MY_SPECIES_IN_MY_STATION;
				msg.obj = json_result; 
				mHandler.sendMessage(msg);
				try{Thread.sleep(80);}catch(Exception e){;}

			}else{
				Message msg = mHandler.obtainMessage();
				msg.arg1 = -1;
				msg.obj = new String("Error occurs while downloading my speices.");
				mHandler.sendMessage(msg);
				continueFlag = false;
				return;
			}

		//4.Download user observation table
			if(continueFlag && 
					(json_result = sync.download_json(
					getString(R.string.get_observation_URL)
					+"&username="+ username	+"&password="+ password)) != null
					){
				
				//Send Message to handler to show progress
				Message msg = mHandler.obtainMessage();
				msg.arg1 = 50;
				msg.arg2 = DOWNLOAD_OBSERVATION;
				msg.obj = json_result;
				mHandler.sendMessage(msg);
				try{Thread.sleep(80);}catch(Exception e){;}
				
			}else{
				Message msg = mHandler.obtainMessage();
				msg.arg1 = -1;
				msg.obj = new String("Error occurs while downloading my observation.");
				mHandler.sendMessage(msg);
				try{Thread.sleep(80);}catch(Exception e){;}
				continueFlag = false;
				return;
			}

		//5.Download observation image files
//			if(continueFlag && sync.download_image(
//				getString(R.string.download_obs_image_URL), Helloscr.this)
//				){
//			
//				//Send Message to handler to show progress
//				Message msg = mHandler.obtainMessage();
//				msg.arg1 = 70;
//				msg.arg2 = DOWNLOAD_OBSERVATION_IMG;
//				msg.obj = new String("{\"success\":true}");
//				mHandler.sendMessage(msg);	
//				try{Thread.sleep(80);}catch(Exception e){;}
//			
//			}else{
//				Message msg = mHandler.obtainMessage();
//				msg.arg1 = -1;
//				msg.obj = new String("Error occurs while downloading image files.");
//
//				mHandler.sendMessage(msg);
//				try{Thread.sleep(80);}catch(Exception e){;}
//				continueFlag = false;
//				return;
//			}
			
		//Send 100% progress message
			if(continueFlag){
				Message msg = mHandler.obtainMessage();
				msg.arg1 = 100;
				msg.arg2 = NETWORK_COMPLETED;
				mHandler.sendMessage(msg);
				try{Thread.sleep(80);}catch(Exception e){;}
				return;
			}
		}
	}
	
	//Message handler for download thread
	Handler mHandler = new Handler(){
		public void handleMessage(Message msg){

			progressValue = msg.arg1;
			SQLiteDatabase db;

			if(progressValue == -1){//Download fails
				mQuit = true;
				try{
					dismissDialog(0);
				}catch(Exception e){
					Log.e(TAG, e.toString());
				}
				Toast.makeText(Helloscr.this,msg.obj + 
						" Please check your network connectivity and try again.",Toast.LENGTH_SHORT).show();
				progressValue = 0;
			}
			else if(progressValue < 100){
				mProgress.setProgress(progressValue);
				
				try{

					JSONObject jsonobj = new JSONObject((String)msg.obj);
					if(jsonobj.getBoolean("success")){
						//result = jsonresult.toString();
						db = syncDBHelper.getWritableDatabase();
						
						switch(msg.arg2){
						case UPLOAD_ADDED_SITE:
							break;
						case UPLOAD_ADDED_PLANT:
							mProgress.setMessage("Uploading newly added plants..");
							break;
						case UPLOAD_OBSERVATION:
							mProgress.setMessage("Uploading obseravtions..");

							break;
						case DOWNLOAD_MY_SPECIES_IN_MY_STATION:
							mProgress.setMessage("Downloading my plants..");
							try{
								jsonobj = new JSONObject((String)msg.obj);
								JSONArray jsonresult = new JSONArray(jsonobj.getString("results"));
								db.execSQL("DELETE FROM my_plants;");
								for(int i=0; i<jsonresult.length(); i++){
									db.execSQL("INSERT INTO my_plants " +
											"(species_id, site_id, site_name, protocol_id)" +
											"VALUES(" +
											jsonresult.getJSONObject(i).getString("spc_id") + "," +
											jsonresult.getJSONObject(i).getString("st_id") + "," +
											"'"+
											jsonresult.getJSONObject(i).getString("st_name") + "'," +
											jsonresult.getJSONObject(i).getString("pro_id")
											+ ");");
								}
								
								Log.d(TAG, "DOWNLOAD_MY_SPECIES_IN_MY_STATION: success to store into db");

							}catch(Exception e){
								Log.e(TAG, e.toString());
								Log.d(TAG, "DOWNLOAD_MY_SPECIES_IN_MY_STATION: failed to store into db");
							}
							break;
						case DOWNLOAD_OBSERVATION:
							mProgress.setMessage("Downloading my observation..");
							try{
								jsonobj = new JSONObject((String)msg.obj);
								JSONArray jsonresult = new JSONArray(jsonobj.getString("results"));
								db.execSQL("DELETE FROM my_observation;");
								for(int i=0; i<jsonresult.length(); i++){
									String query = "INSERT INTO my_observation " +
									"(species_id, site_id, phenophase_id, image_id, time, note)" +
									"VALUES(" +
									jsonresult.getJSONObject(i).getString("species_id") + "," +
									jsonresult.getJSONObject(i).getString("site_id") + "," +
									jsonresult.getJSONObject(i).getString("phenophase_id") + "," +
									"'"+
									jsonresult.getJSONObject(i).getString("image_id") + "'," +
									"'"+
									jsonresult.getJSONObject(i).getString("time") + "'," +
									"'"+
									jsonresult.getJSONObject(i).getString("note") + "');";
									db.execSQL(query);
								}
								
								Log.d(TAG, "DOWNLOAD_OBSERVATION: success to store into db");
							}catch(Exception e){
								Log.e(TAG, e.toString());
								Log.d(TAG, "DOWNLOAD_OBSERVATION: failed to store into db");
							}							
							break;
						case DOWNLOAD_OBSERVATION_IMG:
							mProgress.setMessage("Downloading observation image..");
							try{
								Log.d(TAG, "DOWNLOAD_OBS_IMAGE: success to store into db");
							}catch(Exception e){
								Log.e(TAG, e.toString());
								Log.d(TAG, "DOWNLOAD_OBSERVATION: failed to store into db");
							}		
							break;
						case NETWORK_COMPLETED:
							break;
						}
						db.close();
					}
					else{ //Server rejects client request
						error_message = jsonobj.getString("error_message");
						mQuit = true;
						try{
							dismissDialog(0);
						}catch(Exception e){
							Log.e(TAG, e.toString());
						}
						
						//Error message handling
						if(error_message.equals("Wrong username and password"))
						{
							SharedPreferences.Editor edit = pref.edit();				
							edit.putString("Username","");
							edit.putString("Password","");
							edit.commit();
						
			
							Intent intent = new Intent(Helloscr.this,Login.class);
							startActivity(intent);
							finish();
							return;
						}
						Toast.makeText(Helloscr.this, "Message from server:\n" + error_message, Toast.LENGTH_LONG).show();
					}

				}
				catch(Exception e){
					Log.e(TAG, e.toString());
				}
			}
			else{ //Download completed.
				mProgress.setMessage("Download completed.");
				mProgress.setProgress(100);
				mQuit = true;
				try{
					dismissDialog(0);
				}catch(Exception e){
					Log.e(TAG, e.toString());
				}
				if(cancelBtnClickedFlag == true){
					//Toast.makeText(Helloscr.this, "Sync canceled.", Toast.LENGTH_SHORT).show();
				}
				else{
					Intent intent = new Intent(Helloscr.this, PlantList.class);
					startActivity(intent);
					Toast.makeText(Helloscr.this, "Sync done.",Toast.LENGTH_SHORT).show();
					finish();
				}
				syncDBHelper.close();
			}
		}
	};
	
	//Dialog for download thread
	protected Dialog onCreateDialog(int id){
		switch(id){
		case 0:
			mProgress = new ProgressDialog(this);
			mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgress.setTitle("Syncing");
			mProgress.setMessage("Wait...");
			mProgress.setCancelable(false);
			mProgress.setButton("Cancel",new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int wchihButton){
					mQuit = true;
					cancelBtnClickedFlag = true;
					SharedPreferences.Editor edit = pref.edit();				
					edit.putString("Synced","false");
					edit.commit();
					//dismissDialog(0);
				}
			});
			return mProgress;
		}
		return null;
	}
}
