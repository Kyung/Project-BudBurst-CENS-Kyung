package cens.ucla.edu.budburst;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import cens.ucla.edu.budburst.helper.BackgroundService;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.MyNearestPlants;
import cens.ucla.edu.budburst.onetime.OneTimeMain;
import cens.ucla.edu.budburst.onetime.QuickCapture;

public class AddSite extends Activity{

	final String TAG = "AddSite.class";
	private Integer species_id;
	private Integer pheno_id;
	private Integer protocol_id;
	private Integer previous_activity;
	
	private Double latitude = 0.0;
	private Double longitude = 0.0;
	private float accuracy = 0;
	
	private String camera_image_id;
	private String notes;
	private String selectedState = "";
	private String common_name = "Unknown/Other";
	
	private String species_name;
	
	private EditText geolocationEdit;
	//private EditText lngEdit;
	private EditText sitename;
	private EditText comment;
	private EditText et1;
	private Button Human_Disturbance;
	private Button Shading;
	private Button Irrigation;
	private Button Habitat;
	private Button UpdateGPS;
	private Dialog noteDialog = null;
	private boolean fromUpdateGPS = false;
	
	private Location cur_location = null;
	private LocationManager lmanager = null;
	ArrayAdapter<CharSequence> adspin;
	
	private static GpsListener gpsListener;
	private FunctionsHelper helper;
	
	private SharedPreferences pref;
	private Handler mHandler = new Handler();
	private Thread thread;
	
	//protected String[] list_hdistance = {getString(R.string.AddSite_Urban_Highly_Modified), getString(R.string.AddSite_Suburban), getString(R.string.AddSite_Rural), getString(R.string.AddSite_Wildland_or_natural_area)};
	//protected String[] list_shading = {getString(R.string.AddSite_Open), getString(R.string.AddSite_Partially_Shaded), getString(R.string.AddSite_Shaded)};
	//protected String[] list_irrigation = {getString(R.string.AddSite_Irrigated_Regualarly), getString(R.string.AddSite_Not_Irrigated)};
	//protected String[] list_habitat = {getString(R.string.AddSite_Field_or_grassland), getString(R.string.AddSite_Vegetable_or_flower_garden), getString(R.string.AddSite_Lawn), getString(R.string.AddSite_Pavement_over_50_of_area), getString(R.string.AddSite_Desert_or_dunes), getString(R.string.AddSite_Shrub_thicket), getString(R.string.AddSite_Forest_or_woodland), getString(R.string.AddSite_Edge_of_forest_or_woodland), getString(R.string.AddSite_Stream_lake_pond_edge)};
	
	protected String[] list_hdistance = {"Urban/Highly Modified", "Suburban", "Rural", "Wildland or natural area"};
	protected String[] list_shading = {"No Shade", "Partially Shaded", "Shaded"};
	protected String[] list_irrigation = {"Irrigated Regualarly", "Not Irrigated"};
	protected String[] list_habitat = {"Field or grassland", "Vegetable or flower garden", "Lawn", 
			"Pavement over 50% of area", "Desert or dunes(little vegetation)", "Shrub thicket", "Forest or woodland",
			"Edge of forest or woodland", "Stream/lake/pond edge"};

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.addsite);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		geolocationEdit = (EditText)this.findViewById(R.id.geo_location);
		//lngEdit = (EditText)this.findViewById(R.id.longitude);
		myTitleText.setText("  " + getString(R.string.AddSite_addSite));
		
		lmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		if (!(lmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)
		    	 || lmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){  
		         createLocationServiceDisabledAlert();  
		}
		
		LinearLayout ll = (LinearLayout)findViewById(R.id.header_item);
		ll.setVisibility(View.GONE);
		
		helper = new FunctionsHelper();
		
		sitename = (EditText)this.findViewById(R.id.sitename);
		comment = (EditText)this.findViewById(R.id.comment);
		Human_Disturbance = (Button)findViewById(R.id.human_distance_text);
		Shading = (Button)findViewById(R.id.shading_text);
		Irrigation = (Button)findViewById(R.id.irrigation_text);
		Habitat = (Button)findViewById(R.id.habitat_text);
		UpdateGPS = (Button)findViewById(R.id.updates_gps);
		
		
		Intent p_intent = getIntent();
		previous_activity = p_intent.getExtras().getInt("from");
		
		Log.i("K", "previous_activity : " + previous_activity);
		
		if(previous_activity == Values.FROM_PLANT_LIST) {
			species_id = p_intent.getExtras().getInt("species_id");
			species_name = p_intent.getExtras().getString("species_name");
			protocol_id = p_intent.getExtras().getInt("protocol_id");
			pheno_id = p_intent.getExtras().getInt("pheno_id");
			camera_image_id = p_intent.getExtras().getString("camera_image_id");
			notes = p_intent.getExtras().getString("notes");

			// turn on the GPS
			// we don't need to turn it on in Quick Capture mode, because we receive the data from background service...
			gpsListener = new GpsListener();
		    // set update the location data in 1secs or 10meters
			int minTimeBetweenUpdatesms = 1000 * 1;
			int minDistanceBetweenUpdatesMeters = 10;
			lmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeBetweenUpdatesms, minDistanceBetweenUpdatesMeters, gpsListener);
		}
		else if(previous_activity == Values.FROM_QUICK_CAPTURE || previous_activity == Values.FROM_ONETIME_DIRECT){
			species_id = p_intent.getExtras().getInt("species_id");
			species_name = p_intent.getExtras().getString("cname");
			protocol_id = p_intent.getExtras().getInt("protocol_id");
			pheno_id = p_intent.getExtras().getInt("pheno_id");
			camera_image_id = p_intent.getExtras().getString("camera_image_id");
			notes = p_intent.getExtras().getString("notes");
			ll.setVisibility(View.VISIBLE);
			
			Log.i("K", "QUICK_CAPTURE //// species_id : " + species_id + " , pheno_id : " + pheno_id + " , camera_image_id : " + camera_image_id + " protocol id : " + protocol_id);
			
			sitename.setVisibility(View.GONE);
			
			gpsUpdate();
			// check if the service receives the GPS data
		}
		else {
			species_id = p_intent.getExtras().getInt("species_id");
			species_name = p_intent.getExtras().getString("species_name");
		}
		
		Button noteBtn = (Button) findViewById(R.id.notes);
		
		noteBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				gpsUpdate();
				noteDialog = new Dialog(AddSite.this);
				
				noteDialog.setContentView(R.layout.add_note_custom_dialog);
				noteDialog.setCancelable(true);
				noteDialog.show();
				
				et1 = (EditText)noteDialog.findViewById(R.id.custom_notes);
				if(notes != "") {
					et1.setText(notes);
				}
				Button doneBtn = (Button)noteDialog.findViewById(R.id.custom_done);
				
				doneBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(!notes.equals("")) {
							Toast.makeText(AddSite.this, "Updated Notes", Toast.LENGTH_SHORT).show();
						}
						else {
							Toast.makeText(AddSite.this, "Added Notes", Toast.LENGTH_SHORT).show();
						}
						notes = et1.getText().toString();
						noteDialog.dismiss();
					}
				});
			}
		});

		// when user press the done button...
		Button saveBtnWithoutSiteInfo = (Button) findViewById(R.id.submit);
		saveBtnWithoutSiteInfo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gpsUpdate();
				// TODO Auto-generated method stub
				
				pref = getSharedPreferences("userinfo", 0);
					
				boolean highly = pref.getBoolean("highly", false);
				
				if(latitude == 0.0 || longitude == 0.0 || accuracy == 0) {
					new AlertDialog.Builder(AddSite.this)
					.setTitle(getString(R.string.Done_Quick_Capture))
					.setMessage("Save without GeoLocation?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if((previous_activity == Values.FROM_ONETIME_DIRECT) 
									|| (previous_activity == Values.FROM_QUICK_CAPTURE && species_name.equals("Unknown/Other"))) {
								addCustomName();
							}
							else {
								insertToOneTimeWithoutSite();
							}
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					})
					.show();
				}
				else {
					if(highly == false){
						new AlertDialog.Builder(AddSite.this)
						.setTitle(getString(R.string.Done_Quick_Capture))
						.setMessage("Save with GPS info - \n" + String.format("%6.3f / %6.3f \u00b1 %3.1fm", latitude, longitude, accuracy) + "?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if((previous_activity == Values.FROM_ONETIME_DIRECT) 
										|| (previous_activity == Values.FROM_QUICK_CAPTURE && species_name.equals("Unknown/Other"))) {
									addCustomName();
								}
								else {
									insertToOneTimeWithoutSite();
								}
							}
						})
						.setNegativeButton("No", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
							}
						})
						.show();
					}
					else{
						if((previous_activity == Values.FROM_ONETIME_DIRECT) 
								|| (previous_activity == Values.FROM_QUICK_CAPTURE && species_name.equals("Unknown/Other"))) {
							addCustomName();
						}
						else {
							insertToOneTimeWithoutSite();
						}
					}
				}
			}
		});
	}
	
	public void onResume(){
		super.onResume();
				
		Human_Disturbance.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				gpsUpdate();
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(AddSite.this);
				builder.setTitle(getString(R.string.AddSite_HumanDisturbance));
				builder.setItems(list_hdistance, new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Human_Disturbance.setText(list_hdistance[which]);
					}
					
				});
				builder.setCancelable(true);
				builder.show();
				
			}
		});
		
		Shading.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				gpsUpdate();
				AlertDialog.Builder builder = new AlertDialog.Builder(AddSite.this);
				builder.setTitle(getString(R.string.AddSite_Shading));
				builder.setItems(list_shading, new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Shading.setText(list_shading[which]);
					}
					
				});
				builder.setCancelable(true);
				builder.show();
			}
		});
		
		Irrigation.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				gpsUpdate();
				AlertDialog.Builder builder = new AlertDialog.Builder(AddSite.this);
				builder.setTitle(getString(R.string.AddSite_Irrigation));
				builder.setItems(list_irrigation, new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Irrigation.setText(list_irrigation[which]);
					}
					
				});
				builder.setCancelable(true);
				builder.show();
			}
		});
		
		Habitat.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				gpsUpdate();
				AlertDialog.Builder builder = new AlertDialog.Builder(AddSite.this);
				builder.setTitle(getString(R.string.AddSite_Habitat));
				builder.setItems(list_habitat, new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Habitat.setText(list_habitat[which]);
					}
					
				});
				builder.setCancelable(true);
				builder.show();
			}
		});
		
		// updateGPS button
		UpdateGPS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent service = new Intent(AddSite.this, BackgroundService.class);
			    stopService(service);
				
				// TODO Auto-generated method stub
				if(!fromUpdateGPS) {
					UpdateGPS.setText("Finish GPS");
					Toast.makeText(AddSite.this, "GPS on", Toast.LENGTH_SHORT).show();
					fromUpdateGPS = true;
					gpsListener = new GpsListener();
				    // set update the location data in 1secs or 1meter
					int minTimeBetweenUpdatesms = 1000 * 1;
					int minDistanceBetweenUpdatesMeters = 1;
					lmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeBetweenUpdatesms, minDistanceBetweenUpdatesMeters, gpsListener);
				}
				else {
					UpdateGPS.setText("Update GPS");
					Toast.makeText(AddSite.this, "GPS off", Toast.LENGTH_SHORT).show();
					fromUpdateGPS = false;
					lmanager.removeUpdates(gpsListener);
				}
				
			}
		});
		

		//Save button with Site Information
		Button saveBtnWithSiteInfo = (Button)this.findViewById(R.id.save);	
		saveBtnWithSiteInfo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// if there is no lat/lon received...
				if(latitude == 0.0 || longitude == 0.0) {
					//Check site name is empty
					if(previous_activity == Values.FROM_PLANT_LIST && sitename.getText().toString().equals("")){
						Toast.makeText(AddSite.this,getString(R.string.AddSite_checkSiteName), Toast.LENGTH_SHORT).show();
						return;
					}
					if(previous_activity == Values.FROM_PLANT_LIST && checkSiteNameDuplicated()) {
						Toast.makeText(AddSite.this,getString(R.string.AddSite_alreadyExists), Toast.LENGTH_SHORT).show();
						return;
					}
					
					// ask users if they want to save the site info without lat/lon
					new AlertDialog.Builder(AddSite.this)
					.setTitle(getString(R.string.Alert_addSite))
					.setMessage(getString(R.string.Alert_saveWithoutGeoData))
					.setPositiveButton(getString(R.string.Button_OK),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							insertIntoSite(sitename.getText().toString());
							insertToOneTimeTable();
							Intent intent = new Intent(AddSite.this, PlantList.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							finish();
							startActivity(intent);
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							// nothing happens...
						}
					})
					.show();
				}
				else {

					try{
						String usertype_stname = sitename.getText().toString();

						//Check site name is empty
						if(previous_activity == Values.FROM_PLANT_LIST && usertype_stname.equals("")){
							Toast.makeText(AddSite.this,getString(R.string.AddSite_checkSiteName), Toast.LENGTH_SHORT).show();

							return;
						}
						
						//Check if site name is duplicated
						if(checkSiteNameDuplicated()) {
							Toast.makeText(AddSite.this,getString(R.string.AddSite_alreadyExists), Toast.LENGTH_SHORT).show();
							return;
						}

						//Insert user typed site name into database
						insertIntoSite(usertype_stname);
						//Insert into onetime tables
						insertToOneTimeTable();
						
					}catch(Exception e){
						Log.e(TAG, e.toString());
					}
				}
			}
		});
	}
	
	public void addCustomName() {
		Dialog dialog = new Dialog(AddSite.this);
		
		dialog.setContentView(R.layout.species_name_custom_dialog);
		dialog.setTitle(getString(R.string.GetPhenophase_PBB_message));
		dialog.setCancelable(true);
		dialog.show();
		
		et1 = (EditText)dialog.findViewById(R.id.custom_common_name);
		Button doneBtn = (Button)dialog.findViewById(R.id.custom_done);
		
		doneBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				common_name= et1.getText().toString();
				if(common_name.equals("")) {
					common_name = "Unknown/Other";
				}
				
				helper.insertNewPlantToDB(AddSite.this, Values.UNKNOWN_SPECIES, 0, 9, common_name, "");
				int getID = helper.getID(AddSite.this);
				helper.insertNewObservation(AddSite.this, getID, pheno_id, latitude, longitude, accuracy, camera_image_id, notes);
				
				Intent intent = new Intent(AddSite.this, PlantList.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				
				// add vibration when done
				Toast.makeText(AddSite.this, getString(R.string.QuickCapture_Added), Toast.LENGTH_SHORT).show();
				Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(500);
				finish();
			}
		});
	}
	
	public void gpsUpdate() {
		if(previous_activity == Values.FROM_QUICK_CAPTURE || previous_activity == Values.FROM_ONETIME_DIRECT) {
			pref = getSharedPreferences("userinfo", 0);
			if(pref.getBoolean("new", false)) {
				latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
				longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
				accuracy = Float.parseFloat(pref.getString("accuracy", "0"));
			}
			String strLoc = String.format("%8.4f / %8.4f \u00b1 %4.1fm", latitude, longitude, accuracy);
			
			geolocationEdit.setText(strLoc);
		}
	}
	
	public boolean checkSiteNameDuplicated() {
		
		SyncDBHelper syncDBHelper = new SyncDBHelper(AddSite.this);
		SQLiteDatabase syncWDB = syncDBHelper.getWritableDatabase();
		
		//Check if site name is duplicated
		String query = "SELECT site_id FROM my_sites WHERE site_name='" 
			+ sitename.getText().toString() + "';";
		Cursor cursor = syncWDB.rawQuery(query, null);
		if(cursor.getCount() != 0){

			cursor.close();
			return true;
		}
		cursor.close();
		syncWDB.close();
		syncDBHelper.close();			
		return false;
	}
	
	public void insertIntoSite(String usertype_stname) {
		SyncDBHelper syncDBHelper = new SyncDBHelper(AddSite.this);
		SQLiteDatabase syncWDB = syncDBHelper.getWritableDatabase();
		
		long epoch = System.currentTimeMillis()/1000;
		
		int official = Values.OFFICIAL;
		int synced = SyncDBHelper.SYNCED_NO;
		
		if(usertype_stname.equals("")) {
			usertype_stname = "Quick Capture Site";
			official = Values.UNOFFICIAL;
			synced = SyncDBHelper.SYNCED_NO;
		}
		else {
			official = Values.OFFICIAL;
			synced = SyncDBHelper.SYNCED_NO;
		}
		
		String query = "INSERT INTO my_sites VALUES(" +
		"null, " + 
		"'" + epoch + "'," + 
		"'" + usertype_stname + "'," +
		"'" + latitude + "'," + 
		"'" + longitude + "'," +
		"'" + accuracy + "'," +
		"'" + selectedState + "'," +
		"'" + comment.getText().toString() + "'," +
		"'" + Human_Disturbance.getText().toString() + "'," +
		"'" + Shading.getText().toString() + "'," +
		"'" + Irrigation.getText().toString() + "'," +
		"'" + Habitat.getText().toString() + "'," +
		official + "," + 
		synced + ");";
		
		Log.i("K", "QUERY : " + query);
		
		
		syncWDB.execSQL(query);
		syncWDB.close();
		
	}
	
	public void insertToOneTimeWithoutSite() {
		FunctionsHelper helper = new FunctionsHelper();
		helper.insertNewPlantToDB(AddSite.this, species_id, 0, 9, species_name, "");
		int getID = helper.getID(AddSite.this);

		helper.insertNewObservation(AddSite.this, getID, pheno_id, latitude, longitude, accuracy, camera_image_id, notes);
		
		Intent intent = new Intent(AddSite.this, PlantList.class);
		//Toast.makeText(AddSite.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
		//clear all stacked activities.
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		
		// add vibration when done
		Toast.makeText(AddSite.this, getString(R.string.QuickCapture_Added), Toast.LENGTH_SHORT).show();
		Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(500);
		
		finish();

	}
	
	public void insertToOneTimeTable() {
		
		// turn off the gps...
		if(gpsListener != null) {
			lmanager.removeUpdates(gpsListener);
		}
		
		SyncDBHelper syncDBHeler = new SyncDBHelper(AddSite.this);
		SQLiteDatabase syncDB = syncDBHeler.getReadableDatabase();
		
		Cursor c = syncDB.rawQuery("SELECT site_id, site_name FROM my_sites WHERE latitude='" 
				+ latitude + "';", null);
		while(c.moveToNext()) {
			Log.i("K", "SPECIES_ID : " + species_id + " SPECIES NAME : " + species_name +
					" SITE ID : " + c.getString(0) + " NAME : " + c.getString(1));
			
			Log.i("K", "previous_activity : " + previous_activity);
			
			// if the activity is from QUICK_CAPTURE or FROM_ONETIME_DIRECT
			if(previous_activity == Values.FROM_QUICK_CAPTURE || previous_activity == Values.FROM_ONETIME_DIRECT) {
				FunctionsHelper helper = new FunctionsHelper();
				helper.insertNewPlantToDB(AddSite.this, species_id, Integer.parseInt(c.getString(0)), 9, species_name, "");
				int getID = helper.getID(AddSite.this);

				helper.insertNewObservation(AddSite.this, getID, pheno_id, latitude, longitude, accuracy, camera_image_id, notes);
				
				Intent intent = new Intent(AddSite.this, PlantList.class);
				Toast.makeText(AddSite.this, getString(R.string.QuickCapture_Added), Toast.LENGTH_SHORT).show();
				//clear all stacked activities.
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				
				// add vibration when done
				//Toast.makeText(AddSite.this, getString(R.string.PlantInfo_successAdded), Toast.LENGTH_SHORT).show();
				Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(500);
				
				finish();
			}
			// if other previous activities
			else {
				if(insertNewPlantToDB(species_id, species_name, Integer.parseInt(c.getString(0)), c.getString(1))){
					Intent intent = new Intent(AddSite.this, PlantList.class);
					Toast.makeText(AddSite.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
					
					//clear all stacked activities.
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					
					Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
					vibrator.vibrate(500);
					
					finish();
				}else{
					Toast.makeText(AddSite.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
				}
			}
		}
		
		c.close();
		syncDB.close();
		
	}
	
	public boolean insertNewPlantToDB(int speciesid, String speciesname, int siteid, String sitename){

		int s_id = speciesid;
		SharedPreferences pref = getSharedPreferences("userinfo",0);
		
		if(speciesid == Values.UNKNOWN_SPECIES) {
			s_id = pref.getInt("other_species_id", 0);
			s_id++;
		}
		
		try{
			SyncDBHelper syncDBHelper = new SyncDBHelper(this);
			SQLiteDatabase syncDB = syncDBHelper.getWritableDatabase();
			
			syncDB.execSQL("INSERT INTO my_plants VALUES(" +
					"null," +
					speciesid + "," +
					siteid + "," +
					"'" + sitename + "',"+
					protocol_id + ","+
					"'" + speciesname + "'," +
					"1, " +
					SyncDBHelper.SYNCED_NO + ");"
					);
			
			if(speciesid == Values.UNKNOWN_SPECIES) {
				SharedPreferences.Editor edit = pref.edit();				
				edit.putInt("other_species_id", s_id);
				edit.commit();
			}
			
			syncDBHelper.close();
			return true;
		}
		catch(Exception e){
			Log.e(TAG,e.toString());
			return false;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(gpsListener != null) {
			lmanager.removeUpdates(gpsListener);
		}
		
		stopService(new Intent(AddSite.this, BackgroundService.class));
	}

	private void createLocationServiceDisabledAlert(){  
		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
		builder.setMessage(getString(R.string.Message_locationDisabled))  
		     .setCancelable(false)  
		     .setPositiveButton(getString(R.string.Button_enable),  
		          new DialogInterface.OnClickListener(){  
		          public void onClick(DialogInterface dialog, int id){  
		               showLocationOptions();  
		          }  
		     });  
		     builder.setNegativeButton(getString(R.string.Button_cancel),  
		          new DialogInterface.OnClickListener(){  
		          public void onClick(DialogInterface dialog, int id){  
		               dialog.cancel();  
		          }  
		     });  
		AlertDialog alert = builder.create();  
		alert.show();  
		}  

	private void showLocationOptions(){  
	        Intent gpsOptionsIntent = new Intent(  
	                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
	        startActivity(gpsOptionsIntent);  
	}  
	
	private class GpsListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {

			// TODO Auto-generated method stub
			if(loc != null) {
				// get current location value
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				accuracy = loc.getAccuracy();
				//getReverseGeo(latitude, longitude);
				
				String strLoc = String.format("%8.4f / %8.4f \u00b1 %4.1fm", latitude, longitude, accuracy);
				geolocationEdit.setText("" + strLoc);
				
				if(fromUpdateGPS) {
					if(accuracy < 1) {
						lmanager.removeUpdates(gpsListener);
					}
				}
				else {
					// when get the data, stop GPS
					lmanager.removeUpdates(gpsListener);
				}
			} 
		}
		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			//Toast.makeText(AddSite.this, getString(R.string.AddSite_disabledGPS), Toast.LENGTH_SHORT).show();
			
		}
		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			//Toast.makeText(AddSite.this, getString(R.string.AddSite_enabledGPS), Toast.LENGTH_SHORT).show();
		}
		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}	
	}
	
	
    // or when user press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			if(gpsListener != null) {
				lmanager.removeUpdates(gpsListener);
			}
			return true;
		}
		return false;
	}
}