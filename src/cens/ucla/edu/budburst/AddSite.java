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
import android.os.Bundle;
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
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.MyNearestPlants;
import cens.ucla.edu.budburst.onetime.OneTimeMain;

public class AddSite extends Activity{

	final String TAG = "AddSite.class";
	private Integer UNOFFICIAL = 0;
	private Integer OFFICIAL = 1;
	private Integer FROM_PLANT_LIST = 100;
	private Integer FROM_QUICK_CAPTURE = 101;
	private Integer species_id;
	private Integer pheno_id;
	private Integer protocol_id;
	private Integer previous_activity;
	
	private Double latitude = 0.0;
	private Double longitude = 0.0;
	
	private String camera_image_id;
	private String notes;
	private String dt_taken;
	private String selectedState = "";
	
	private String species_name;
	
	private EditText latEdit;
	private EditText lngEdit;
	private EditText sitename;
	private EditText comment;
	private EditText et1;
	private Button Human_Disturbance;
	private Button Shading;
	private Button Irrigation;
	private Button Habitat;
	
	private Location cur_location = null;
	private LocationManager lmanager = null;
	ArrayAdapter<CharSequence> adspin;
	
	private static GpsListener gpsListener;
	private FunctionsHelper helper;
	
	//protected String[] list_hdistance = {getString(R.string.AddSite_Urban_Highly_Modified), getString(R.string.AddSite_Suburban), getString(R.string.AddSite_Rural), getString(R.string.AddSite_Wildland_or_natural_area)};
	//protected String[] list_shading = {getString(R.string.AddSite_Open), getString(R.string.AddSite_Partially_Shaded), getString(R.string.AddSite_Shaded)};
	//protected String[] list_irrigation = {getString(R.string.AddSite_Irrigated_Regualarly), getString(R.string.AddSite_Not_Irrigated)};
	//protected String[] list_habitat = {getString(R.string.AddSite_Field_or_grassland), getString(R.string.AddSite_Vegetable_or_flower_garden), getString(R.string.AddSite_Lawn), getString(R.string.AddSite_Pavement_over_50_of_area), getString(R.string.AddSite_Desert_or_dunes), getString(R.string.AddSite_Shrub_thicket), getString(R.string.AddSite_Forest_or_woodland), getString(R.string.AddSite_Edge_of_forest_or_woodland), getString(R.string.AddSite_Stream_lake_pond_edge)};
	
	protected String[] list_hdistance = {"Urban/Highly Modified", "Suburban", "Rural", "Wildland or natural area"};
	protected String[] list_shading = {"Open", "Partially Shaded", "Shaded"};
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
		latEdit = (EditText)this.findViewById(R.id.latitude);
		lngEdit = (EditText)this.findViewById(R.id.longitude);
		myTitleText.setText("  " + getString(R.string.AddSite_addSite));
		
		lmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		if (!(lmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)
		    	 && lmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){  
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
		
		
		Intent p_intent = getIntent();
		previous_activity = p_intent.getExtras().getInt("from");
		
		Log.i("K", "previous_activity : " + previous_activity);
		
		if(previous_activity == FROM_PLANT_LIST) {
			species_id = p_intent.getExtras().getInt("species_id");
			species_name = p_intent.getExtras().getString("species_name");
			protocol_id = p_intent.getExtras().getInt("protocol_id");
			pheno_id = p_intent.getExtras().getInt("pheno_id");
			camera_image_id = p_intent.getExtras().getString("camera_image_id");
			dt_taken = p_intent.getExtras().getString("dt_taken");
			notes = p_intent.getExtras().getString("notes");
			
			Log.i("K", "FROM_PLANT_LIST //// species_id : " + species_id + " , pheno_id : " + pheno_id + " , camera_image_id : " + camera_image_id + " ,dt_taken : " + dt_taken);
		}
		else if(previous_activity == FROM_QUICK_CAPTURE){
			species_id = p_intent.getExtras().getInt("species_id");
			species_name = p_intent.getExtras().getString("cname");
			protocol_id = p_intent.getExtras().getInt("protocol_id");
			pheno_id = p_intent.getExtras().getInt("pheno_id");
			camera_image_id = p_intent.getExtras().getString("camera_image_id");
			dt_taken = p_intent.getExtras().getString("dt_taken");
			//notes = p_intent.getExtras().getString("notes");
			ll.setVisibility(View.VISIBLE);
			
			TextView site_label = (TextView)findViewById(R.id.sitename_lable);
			site_label.setVisibility(View.GONE);
			sitename.setVisibility(View.GONE);
			
			Log.i("K", "FROM_QUICK_CAPTURE //// species_id : " + species_id + " , pheno_id : " + pheno_id + " , camera_image_id : " + camera_image_id + " ,dt_taken : " + dt_taken);
		}
		else {
			species_id = p_intent.getExtras().getInt("species_id");
			species_name = p_intent.getExtras().getString("species_name");
		}
		
		
		gpsListener = new GpsListener();
	    // set update the location data in 3secs or 30meters
		lmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
		
		
		Button doneBtn = (Button) findViewById(R.id.submit);
		doneBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

					helper.insertNewPlantToDB(AddSite.this, species_id, 0, 0, species_name, "");
					int getID = helper.getID(AddSite.this);
					helper.insertNewObservation(AddSite.this, getID, pheno_id, latitude, longitude, camera_image_id, dt_taken, "");
					
					//helper.insertNewPlantToDB(GetPhenophase.this, species_id, 0, protocol_id, cname, sname, latitude, longitude, dt_taken, "", camera_image_id);
					Toast.makeText(AddSite.this, getString(R.string.PlantInfo_successAdded), Toast.LENGTH_SHORT).show();
					
					Intent intent = new Intent(AddSite.this, PlantList.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					
					Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
					vibrator.vibrate(1000);
					
					finish();
			}
		});
		/*
		// start GPS
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
	    
		String bestprovider = lmanager.getBestProvider(criteria, true);

	    if(bestprovider == null)
	    	bestprovider = "gps";
	    */
	}

	public void onResume(){
		super.onResume();
		
		
	
		Human_Disturbance.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(AddSite.this);
				builder.setTitle("Pick one");
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
				AlertDialog.Builder builder = new AlertDialog.Builder(AddSite.this);
				builder.setTitle("Pick one");
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
				AlertDialog.Builder builder = new AlertDialog.Builder(AddSite.this);
				builder.setTitle("Pick one");
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
				AlertDialog.Builder builder = new AlertDialog.Builder(AddSite.this);
				builder.setTitle("Pick one");
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

		//Save button handler
		Button saveButton = (Button)this.findViewById(R.id.save);	
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				
				if(selectedState.equals("")) {
					Toast.makeText(AddSite.this, getString(R.string.Not_Finish_GPS), Toast.LENGTH_SHORT).show();
					
				}
				else {
				
					SyncDBHelper syncDBHelper = new SyncDBHelper(AddSite.this);
					SQLiteDatabase syncWDB = syncDBHelper.getWritableDatabase();

					try{
						String usertype_stname = sitename.getText().toString();

						//Check site name is empty
						if(previous_activity == FROM_PLANT_LIST && usertype_stname.equals("")){
							Toast.makeText(AddSite.this,getString(R.string.AddSite_checkSiteName), Toast.LENGTH_SHORT).show();
							InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(sitename.getWindowToken(), 0);
							return;
						}
						
						//Check if site name is duplicated
						String query = "SELECT site_id FROM my_sites WHERE site_name='" 
							+ usertype_stname + "';";
						Cursor cursor = syncWDB.rawQuery(query, null);
						if(cursor.getCount() != 0){
							Toast.makeText(AddSite.this,getString(R.string.AddSite_alreadyExists), Toast.LENGTH_SHORT).show();
							InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(sitename.getWindowToken(), 0);
							cursor.close();
							return;
						}

						//Insert user typed site name into database
						//Calendar now = Calendar.getInstance();
						
						long epoch = System.currentTimeMillis()/1000;
						
						int official = OFFICIAL;
						int synced = 9;
						
						if(usertype_stname.equals("")) {
							usertype_stname = "Unknown Site";
							official = UNOFFICIAL;
							synced = SyncDBHelper.SYNCED_YES;
						}
						else {
							official = OFFICIAL;
							synced = SyncDBHelper.SYNCED_NO;
						}
						
						query = "INSERT INTO my_sites VALUES(" +
						"null, " + 
						"'" + epoch + "'," + 
						"'" + usertype_stname + "'," +
						"'" + latEdit.getText().toString() + "'," + 
						"'" + lngEdit.getText().toString() + "'," +
						"'" + selectedState + "'," +
						"'" + comment.getText().toString() + "'," +
						"'" + Human_Disturbance.getText().toString() + "'," +
						"'" + Shading.getText().toString() + "'," +
						"'" + Irrigation.getText().toString() + "'," +
						"'" + Habitat.getText().toString() + "'," +
						official + "," + 
						synced + ");";
						
						
						syncWDB.execSQL(query);
						cursor.close();
						
						// species from quick_capture will not show the additional dialog for adding species name...
						if(previous_activity == FROM_PLANT_LIST && species_id == 999) {
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
									String common_name = et1.getText().toString();
									species_name = common_name;
									
									insertDB();
								}
							});
						}
						else{
							insertDB();
						}
						
					}catch(Exception e){
						Log.e(TAG, e.toString());
					}finally{
						syncDBHelper.close();					
					}
				}
			}
		});
	}
	
	public void insertDB() {
		SyncDBHelper syncDBHeler = new SyncDBHelper(AddSite.this);
		SQLiteDatabase syncDB = syncDBHeler.getReadableDatabase();
		
		Cursor c = syncDB.rawQuery("SELECT site_id, site_name FROM my_sites WHERE latitude='" 
				+ latEdit.getText().toString() + "';", null);
		while(c.moveToNext()) {
			Log.i("K", "SPECIES_ID : " + species_id + " SPECIES NAME : " + species_name +
					" SITE ID : " + c.getString(0) + " NAME : " + c.getString(1));
			
			Log.i("K", "previous_activity : " + previous_activity);
			
			// if the activity is from QUICK_CAPTURE
			if(previous_activity == FROM_QUICK_CAPTURE) {
				FunctionsHelper helper = new FunctionsHelper();
				helper.insertNewPlantToDB(AddSite.this, species_id, Integer.parseInt(c.getString(0)), 9, species_name, "");
				int getID = helper.getID(AddSite.this);

				helper.insertNewObservation(AddSite.this, getID, pheno_id, latitude, longitude, camera_image_id, dt_taken, "");
				
				Intent intent = new Intent(AddSite.this, PlantList.class);
				Toast.makeText(AddSite.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
				//clear all stacked activities.
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				
				// add vibration when done
				Toast.makeText(AddSite.this, getString(R.string.PlantInfo_successAdded), Toast.LENGTH_SHORT).show();
				Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(1000);
				
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
					vibrator.vibrate(1000);
					
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
		
		if(speciesid == 999) {
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
					"0,"+
					//"1,"+ // 1 means it's official, from add plant list
					"'" + speciesname + "'," +
					"1, " +
					SyncDBHelper.SYNCED_NO + ");"
					);
			
			if(speciesid == 999) {
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
			
			Log.i("K", "LOCATION CHANGED@@@ ");
			// TODO Auto-generated method stub
			if(loc != null) {
				Log.i("K", "LOCATION CHANGED!! ");
				// get current location value
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				String strLoc = String.format("%10.6f / %10.6f", latitude, longitude);
				
				Geocoder gc = new Geocoder(AddSite.this, Locale.getDefault());
				try {
					List<Address> addr = gc.getFromLocation(latitude, longitude, 1);
					StringBuilder sb = new StringBuilder();
					
					if(addr.size() > 0) {
						Address address = addr.get(0);
						
						selectedState = address.getAdminArea();
						
						sb.append(address.getLocality()).append(", ").append(address.getAdminArea()).append("\n");
						sb.append(address.getCountryName()).append(", ").append(address.getPostalCode());
						
						lngEdit.setText(sb.toString());
						
						// when get the data, stop GPS
						lmanager.removeUpdates(gpsListener);
						Toast.makeText(AddSite.this, getString(R.string.AddSite_gotGPS), Toast.LENGTH_SHORT).show();
					}
					else {
						lngEdit.setText(getString(R.string.Alert_gettingGPS));
					}
		
				}
				catch(IOException e) {
					
				}
				
				latEdit.setText("" + strLoc);
			}
		}
		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			Toast.makeText(AddSite.this, getString(R.string.AddSite_disabledGPS), Toast.LENGTH_SHORT).show();
			
		}
		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			Toast.makeText(AddSite.this, getString(R.string.AddSite_enabledGPS), Toast.LENGTH_SHORT).show();
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
			lmanager.removeUpdates(gpsListener);
			return true;
		}
		return false;
	}
}