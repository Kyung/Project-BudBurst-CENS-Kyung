package cens.ucla.edu.budburst;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.MyNearestPlants;

public class AddSite extends Activity{

	final String TAG = "AddSite.class"; 
	private EditText latitude;
	private EditText longitude;
	private EditText sitename;
	private EditText comment;
	private Button Human_Disturbance;
	private Button Shading;
	private Button Irrigation;
	private Button Habitat;
	private Location cur_location = null;
	private LocationManager lmanager = null;
	ArrayAdapter<CharSequence> adspin;
	private String selectedState = "";
	private static GpsListener gpsListener;
	private double lat = 0.0;
	private double lon = 0.0;
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
		latitude = (EditText)this.findViewById(R.id.latitude);
		longitude = (EditText)this.findViewById(R.id.longitude);
		myTitleText.setText("  " + getString(R.string.AddSite_addSite));
		
		lmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		if (!(lmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)
		    	 && lmanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){  
		         createLocationServiceDisabledAlert();  
		}
		
		gpsListener = new GpsListener();
	    // set update the location data in 3secs or 30meters
		lmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
		
		
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
		
		sitename = (EditText)this.findViewById(R.id.sitename);
		comment = (EditText)this.findViewById(R.id.comment);
		Human_Disturbance = (Button)findViewById(R.id.human_distance_text);
		Shading = (Button)findViewById(R.id.shading_text);
		Irrigation = (Button)findViewById(R.id.irrigation_text);
		Habitat = (Button)findViewById(R.id.habitat_text);
	
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
		
		
		
		//Cancel button handler
		/*
		Button cancelButton = (Button)this.findViewById(R.id.cancel);	
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		*/

		//Save button handler
		Button saveButton = (Button)this.findViewById(R.id.save);	
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				
				if(selectedState.equals("")) {
					Toast.makeText(AddSite.this, "Still getting GPS data...", Toast.LENGTH_SHORT).show();
					
				}
				else {
					SyncDBHelper syncDBHelper = new SyncDBHelper(AddSite.this);
					SQLiteDatabase syncWDB = syncDBHelper.getWritableDatabase();

					try{
						String usertype_stname = sitename.getText().toString();

						//Check site name is empty
						if(usertype_stname.equals("")){
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
						
						query = "INSERT INTO my_sites VALUES(" +
						"null, " + 
						"'" + epoch + "'," + 
						"'" + usertype_stname + "'," +
						"'" + latitude.getText().toString() + "'," + 
						"'" + longitude.getText().toString() + "'," +
						"'" + selectedState + "'," +
						"'" + comment.getText().toString() + "'," +
						"'" + Human_Disturbance.getText().toString() + "'," +
						"'" + Shading.getText().toString() + "'," +
						"'" + Irrigation.getText().toString() + "'," +
						"'" + Habitat.getText().toString() + "'," +		
						SyncDBHelper.SYNCED_NO + ");";
						
						
						syncWDB.execSQL(query);
						cursor.close();

						new AlertDialog.Builder(AddSite.this)
						.setTitle(getString(R.string.AddSite_newAdded))
						.setIcon(R.drawable.pbbicon_small)
						.setMessage(getString(R.string.AddSite_note1))
						.setPositiveButton(getString(R.string.Button_OK), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(AddSite.this, PlantList.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								finish();
							}
						})
						.show();
						
					}catch(Exception e){
						Log.e(TAG, e.toString());
					}finally{
						syncDBHelper.close();					
					}
				}
			}
		});
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
				lat = loc.getLatitude();
				lon = loc.getLongitude();
				String strLoc = String.format("%10.6f / %10.6f", lat, lon);
				
				Geocoder gc = new Geocoder(AddSite.this, Locale.getDefault());
				try {
					List<Address> addr = gc.getFromLocation(lat, lon, 1);
					StringBuilder sb = new StringBuilder();
					
					if(addr.size() > 0) {
						Address address = addr.get(0);
						
						selectedState = address.getAdminArea();
						
						sb.append(address.getLocality()).append(", ").append(address.getAdminArea()).append("\n");
						sb.append(address.getCountryName()).append(", ").append(address.getPostalCode());
						
						longitude.setText(sb.toString());
						
						// when get the data, stop GPS
						lmanager.removeUpdates(gpsListener);
						Toast.makeText(AddSite.this, getString(R.string.AddSite_gotGPS), Toast.LENGTH_SHORT).show();
					}
					else {
						longitude.setText(getString(R.string.Alert_gettingGPS));
					}
		
				}
				catch(IOException e) {
					
				}
				
				latitude.setText("" + strLoc);
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