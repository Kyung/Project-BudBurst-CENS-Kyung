package cens.ucla.edu.budburst.myplants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.Menu;
import android.view.MenuItem;
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
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.R.string;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperGpsHandler;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.onetime.OneTimePhenophase;
import cens.ucla.edu.budburst.onetime.OneTimeMainPage;
import cens.ucla.edu.budburst.utils.PBBItems;
import cens.ucla.edu.budburst.utils.QuickCapture;

public class PBBAddSite extends Activity{

	private boolean extra_gps_on = false;
	
	private Integer mSpeciesID;
	private Integer mPhenoID;
	private Integer mProtocolID;
	private Integer mPreviousActivity;
	private Integer mCategory;
	private Integer mFloracacheID;
	private Integer mIsFloracache;
	private Integer mIsFlickr;
	
	private Double mLatitude;
	private Double mLongitude;
	
	private String mCameraImageID;
	private String mNotes;
	private String mSelectedState = "";
	private String mCommonName = "Unknown/Other";
	private String mScienceName = "";
	private String mImageID = "";
	
	private long mEpoch;
	private float mAccuracy = 0;
	
	private EditText geolocationEdit;
	//private EditText lngEdit;
	private EditText sitename;
	private EditText comment;
	private EditText et1;
	private Button Human_Disturbance;
	private Button Shading;
	private Button Irrigation;
	private Button Habitat;
	private Button SwitchToMap;
	private Button UpdateGPS;
	private Dialog noteDialog = null;
	private boolean fromUpdateGPS = false;
	
	//private Location mCurrentLocation = null;
	private LocationManager mLocationManager = null;
	private HelperFunctionCalls mHelper;
	private HelperSharedPreference mPref;
	private Handler mHandler = new Handler();
	private Dialog mDialog;
	
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
	
	private LinearLayout ll;
	
	private PBBItems pbbItem;

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
		myTitleText.setText(" " + getString(R.string.AddSite_addSite));
		
		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		/*
		 *  If GPS is off, let's turn it on!
		 */
		if (!(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
		    	 || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){  
		         createLocationServiceDisabledAlert();  
		}

		mHelper = new HelperFunctionCalls();
		
		/*
		 * Initialize the layouts
		 */
		sitename = (EditText)this.findViewById(R.id.sitename);
		comment = (EditText)this.findViewById(R.id.comment);
		Human_Disturbance = (Button)findViewById(R.id.human_distance_text);
		Shading = (Button)findViewById(R.id.shading_text);
		Irrigation = (Button)findViewById(R.id.irrigation_text);
		Habitat = (Button)findViewById(R.id.habitat_text);
		SwitchToMap = (Button)findViewById(R.id.switch_to_mapview);
		UpdateGPS = (Button) findViewById(R.id.update_gps);
		
	}
	
	public void onResume(){
		super.onResume();
		// Get GPS - Latitude / Longitude / Accuracy
		mPref = new HelperSharedPreference(this);
		mLatitude = Double.parseDouble(mPref.getPreferenceString("latitude", "0.0"));
		mLongitude = Double.parseDouble(mPref.getPreferenceString("longitude", "0.0"));
		mAccuracy = Float.parseFloat(mPref.getPreferenceString("accuracy", "0"));
		
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		mCommonName = pbbItem.getCommonName();
		mScienceName = pbbItem.getScienceName();
		mCameraImageID = pbbItem.getCameraImageName();
		mProtocolID = pbbItem.getProtocolID();
		mSpeciesID = pbbItem.getSpeciesID();
		mPhenoID = pbbItem.getPhenophaseID();
		mIsFloracache = pbbItem.getIsFloracache();
		mFloracacheID = pbbItem.getFloracacheID();
		mIsFlickr = pbbItem.getIsFlickr();
		mCategory = pbbItem.getCategory();
		mNotes = pbbItem.getNote();
		
		Log.i("K", "mIsFloracache : " + mIsFloracache);
		Log.i("K", "mFloracacheID : " + mFloracacheID);
		Log.i("K", "mIsFlickr : " + mIsFlickr);
		
		Intent p_intent = getIntent();
		mPreviousActivity = p_intent.getExtras().getInt("from");

		if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE || 
				mPreviousActivity == HelperValues.FROM_USER_DEFINED_LISTS || 
				mPreviousActivity == HelperValues.FROM_LOCAL_PLANT_LISTS){
			// change the text when from quick capture.
			TextView addSiteTxt = (TextView) findViewById(R.id.add_site_text);
			addSiteTxt.setText(getString(R.string.Unknown_Plant_Text_2));
			mImageID = p_intent.getExtras().getString("image_id");
			sitename.setVisibility(View.GONE);
			gpsUpdate();
		}
		
		if(mIsFloracache != HelperValues.IS_FLORACACHE_NO ||
				mIsFlickr == HelperValues.IS_FLICKR_YES) {
			
			mLatitude = pbbItem.getLatitude();
			mLongitude = pbbItem.getLongitude();
			
			SwitchToMap.setVisibility(View.GONE);
			UpdateGPS.setVisibility(View.GONE);
		}
		
		String strLoc = String.format("%10.6f / %10.6f \u00b1 %4.1fm", mLatitude, mLongitude, mAccuracy);
		geolocationEdit.setText(strLoc);
		
		buttonListener();
		saveButtonListener();
	}
	
	
	private void buttonListener() {
		Human_Disturbance.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				gpsUpdate();
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(PBBAddSite.this);
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
				AlertDialog.Builder builder = new AlertDialog.Builder(PBBAddSite.this);
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
				AlertDialog.Builder builder = new AlertDialog.Builder(PBBAddSite.this);
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
				AlertDialog.Builder builder = new AlertDialog.Builder(PBBAddSite.this);
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
		
		SwitchToMap.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(PBBAddSite.this, PBBChangeMyPosition.class));
			}
		});
		
		UpdateGPS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				extra_gps_on = true;
				
				Intent service = new Intent(PBBAddSite.this, HelperBackgroundService.class);
			    stopService(service);
				
				// TODO Auto-generated method stub
				if(!fromUpdateGPS) {
					UpdateGPS.setText("Finish GPS");
					Toast.makeText(PBBAddSite.this, "GPS on", Toast.LENGTH_SHORT).show();
					fromUpdateGPS = true;
					// start Service
					Intent startService = new Intent(PBBAddSite.this, HelperGpsHandler.class);
					startService(startService);
					
					
					IntentFilter inFilter = new IntentFilter(HelperGpsHandler.GPSHANDLERFILTER);
					registerReceiver(gpsReceiver, inFilter);
				}
				else {
					UpdateGPS.setText("Update GPS");
					Toast.makeText(PBBAddSite.this, "GPS off", Toast.LENGTH_SHORT).show();
					fromUpdateGPS = false;
	
					Intent startService = new Intent(PBBAddSite.this, HelperGpsHandler.class);
					stopService(startService);
				}
				
			}
		});
	}
	
	private void saveButtonListener() {
		//Save button with Site Information
		Button saveBtnWithSiteInfo = (Button)this.findViewById(R.id.save);	
		saveBtnWithSiteInfo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// if there is no lat/lon received...
				if(mLatitude == 0.0 || mLongitude == 0.0) {
					//Check site name is empty
					if(mPreviousActivity == HelperValues.FROM_PLANT_LIST && sitename.getText().toString().equals("")){
						Toast.makeText(PBBAddSite.this, getString(R.string.AddSite_checkSiteName), Toast.LENGTH_SHORT).show();
						return;
					}
					if(mPreviousActivity == HelperValues.FROM_PLANT_LIST && checkSiteNameDuplicated()) {
						Toast.makeText(PBBAddSite.this, getString(R.string.AddSite_alreadyExists), Toast.LENGTH_SHORT).show();
						return;
					}
					
					// ask users if they want to save the site info without lat/lon
					new AlertDialog.Builder(PBBAddSite.this)
					.setTitle(getString(R.string.Alert_addSite))
					.setMessage(getString(R.string.Alert_saveWithoutGeoData))
					.setPositiveButton(getString(R.string.Button_OK),new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							insertIntoSite(sitename.getText().toString());
							insertPlantAndObservation();
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
						String userSiteName = sitename.getText().toString();

						//Check site name is empty
						if(mPreviousActivity == HelperValues.FROM_PLANT_LIST && userSiteName.equals("")){
							Toast.makeText(PBBAddSite.this,getString(R.string.AddSite_checkSiteName), Toast.LENGTH_SHORT).show();

							return;
						}
						
						//Check if site name is duplicated
						if(checkSiteNameDuplicated()) {
							Toast.makeText(PBBAddSite.this,getString(R.string.AddSite_alreadyExists), Toast.LENGTH_SHORT).show();
							return;
						}

						//Insert user typed site name into database
						insertIntoSite(userSiteName);
						//Insert into onetime tables
						insertPlantAndObservation();
						
					}catch(Exception e){
					}
				}
			}
		});

	}
	
	private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle extras = intent.getExtras();
			mLatitude = extras.getDouble("latitude");
			mLongitude = extras.getDouble("longitude");
			mAccuracy = extras.getFloat("accuracy");
			
			String strLoc = String.format("%10.6f / %10.6f \u00b1 %4.1fm", mLatitude, mLongitude, mAccuracy);
			geolocationEdit.setText("" + strLoc);

		}
		
	};
	
	public void addCustomName() {
		mDialog = new Dialog(PBBAddSite.this);
		
		mDialog.setContentView(R.layout.species_name_custom_dialog);
		mDialog.setTitle(getString(R.string.GetPhenophase_PBB_message));
		mDialog.setCancelable(true);
		mDialog.show();
		
		et1 = (EditText)mDialog.findViewById(R.id.custom_common_name);
		Button doneBtn = (Button)mDialog.findViewById(R.id.custom_done);
		
		doneBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCommonName= et1.getText().toString();
				if(mCommonName.equals("")) {
					mCommonName = "Unknown/Other";
				}
				
				String epochStr = Long.toString(mEpoch);
				int epochInt = Integer.parseInt(epochStr);
				
				mHelper.insertNewSharedPlantToDB(PBBAddSite.this, HelperValues.UNKNOWN_SPECIES, epochInt, 
						mProtocolID, mCommonName, "", mCategory, 
						mImageID, mIsFloracache, mFloracacheID);
				int getID = mHelper.getID(PBBAddSite.this);
				mHelper.insertNewObservation(PBBAddSite.this, getID, mPhenoID, mLatitude, mLongitude, mAccuracy, mCameraImageID, mNotes);
				
				Intent intent = new Intent(PBBAddSite.this, PBBPlantList.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				
				/*
				 *  Add vibration when done
				 */
				Toast.makeText(PBBAddSite.this, getString(R.string.QuickCapture_Added), Toast.LENGTH_SHORT).show();
				Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(500);
				
				mDialog.cancel();
				
				finish();
			}
		});
	}
	
	public void gpsUpdate() {
		
		if(!extra_gps_on) {
			mLatitude = Double.parseDouble(mPref.getPreferenceString("latitude", "0.0"));
			mLongitude = Double.parseDouble(mPref.getPreferenceString("longitude", "0.0"));
			mAccuracy = Float.parseFloat(mPref.getPreferenceString("accuracy", "0"));
		}
		
		String strLoc = String.format("%10.6f / %10.6f \u00b1 %4.1fm", mLatitude, mLongitude, mAccuracy);
		geolocationEdit.setText(strLoc);
	}
	
	public boolean checkSiteNameDuplicated() {
		
		SyncDBHelper syncDBHelper = new SyncDBHelper(PBBAddSite.this);
		SQLiteDatabase syncWDB = syncDBHelper.getWritableDatabase();
		
		/*
		 * Check if site name is duplicated
		 */
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
	
	public void insertIntoSite(String userSiteName) {
		SyncDBHelper syncDBHelper = new SyncDBHelper(PBBAddSite.this);
		SQLiteDatabase syncWDB = syncDBHelper.getWritableDatabase();
		
		mEpoch = System.currentTimeMillis()/1000;
		
		int official = HelperValues.OFFICIAL;
		int synced = SyncDBHelper.SYNCED_NO;
		
		if(mPreviousActivity == HelperValues.FROM_PLANT_LIST) {
			official = HelperValues.OFFICIAL;
		}
		else {
			userSiteName = Long.toString(mEpoch);
			official = HelperValues.UNOFFICIAL;
		}
		
		String query = "INSERT INTO my_sites VALUES(" +
		"null, " + 
		"'" + mEpoch + "'," + 
		"'" + userSiteName + "'," +
		"'" + mLatitude + "'," + 
		"'" + mLongitude + "'," +
		"'" + mAccuracy + "'," +
		"'" + mSelectedState + "'," +
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, HelperGpsHandler.class));
	}
	
	public void insertPlantAndObservation() {

		Intent service = new Intent(PBBAddSite.this, HelperBackgroundService.class);
	    stopService(service);
		
		SyncDBHelper syncDBHeler = new SyncDBHelper(PBBAddSite.this);
		SQLiteDatabase syncDB = syncDBHeler.getReadableDatabase();
		
		Cursor c = syncDB.rawQuery("SELECT site_id, site_name FROM my_sites WHERE latitude='" 
				+ mLatitude + "' ORDER BY site_id DESC LIMIT 1;", null);
		while(c.moveToNext()) {
			
			/*
			 *  If the activity is from SHARED_PLANTS
			 */
			if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE || 
				mPreviousActivity == HelperValues.FROM_USER_DEFINED_LISTS || 
				mPreviousActivity == HelperValues.FROM_LOCAL_PLANT_LISTS) {

				if(mCommonName.equals("Unknown/Other")) {
					addCustomName();
				}
				else {
					HelperFunctionCalls helper = new HelperFunctionCalls();
					
					/*
					 *  Get speciesID if the activity is from LOCAL_BUDBURST_LISTS
					 * 
					 */
					if(mPreviousActivity == HelperValues.FROM_LOCAL_PLANT_LISTS && 
							(mCategory == HelperValues.LOCAL_BUDBURST_LIST || 
							mCategory == HelperValues.LOCAL_WHATSINVASIVE_LIST || 
							mCategory == HelperValues.LOCAL_POISONOUS_LIST || 
							mCategory == HelperValues.LOCAL_THREATENED_ENDANGERED_LIST)) {
						StaticDBHelper staticDB = new StaticDBHelper(PBBAddSite.this);
						SQLiteDatabase sDBH = staticDB.getReadableDatabase();
						
						Cursor cursorGetSpeciesID = sDBH.rawQuery("SELECT _id FROM species WHERE species_name=\"" 
								+ mScienceName + "\";", null);
						
						int getSpeciesID = 999;
						mSpeciesID = getSpeciesID;
						
						while(cursorGetSpeciesID.moveToNext()) {
							getSpeciesID = cursorGetSpeciesID.getInt(0);
						}
						
						mSpeciesID = getSpeciesID;
						
						cursorGetSpeciesID.close();
						sDBH.close();
					}
					
					if(helper.insertNewSharedPlantToDB(PBBAddSite.this, 
							mSpeciesID, Integer.parseInt(c.getString(0)), 
							mProtocolID, mCommonName, mScienceName, 
							mCategory, mImageID, mIsFloracache, mFloracacheID)) {
						int getID = helper.getID(PBBAddSite.this);

						helper.insertNewObservation(PBBAddSite.this, getID, mPhenoID, mLatitude, mLongitude, mAccuracy, mCameraImageID, mNotes);
						
						Intent intent = new Intent(PBBAddSite.this, PBBPlantList.class);
						Toast.makeText(PBBAddSite.this, getString(R.string.QuickCapture_Added), Toast.LENGTH_SHORT).show();
						
						/*
						 * Clear all stacked activities.
						 */
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						
						Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
						vibrator.vibrate(500);
						
						finish();
					}
					else {
						Log.i("K", "Database insertion failed...");
					}
				}
			}
			/*
			 *  If other previous activities
			 */
			else {
				if(mHelper.insertNewMyPlantToDB(PBBAddSite.this, mSpeciesID, mCommonName, Integer.parseInt(c.getString(0)), c.getString(1), mProtocolID, mCategory)){
					Intent intent = new Intent(PBBAddSite.this, PBBPlantList.class);
					Toast.makeText(PBBAddSite.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
					
					/*
					 * Clear all stacked activities.
					 */
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					
					Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
					vibrator.vibrate(500);
					
					finish();
				}else{
					Toast.makeText(PBBAddSite.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
				}
			}
		}
		
		c.close();
		syncDB.close();
		
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
}