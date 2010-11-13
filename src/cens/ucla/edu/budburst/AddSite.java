package cens.ucla.edu.budburst;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import cens.ucla.edu.budburst.helper.SyncDBHelper;

public class AddSite extends Activity{

	final String TAG = "AddSite.class"; 
	private EditText latitude;
	private EditText longitude;
	private EditText sitename;
	private EditText comment;
	private Location cur_location = null;
	private LocationManager lmanager = null;
	ArrayAdapter<CharSequence> adspin;
	private String selectedState;
	private static GpsListener gpsListener;
	private double lat = 0.0;
	private double lon = 0.0;

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

		//Cancel button handler
		Button cancelButton = (Button)this.findViewById(R.id.cancel);	
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});


		//Save button handler
		Button saveButton = (Button)this.findViewById(R.id.save);	
		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

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
					"''," +
					"'" + selectedState + "'," +
					"''," +
					"''," +
					"'" + comment.getText().toString() + "'," +
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
						
						sb.append(address.getLocality()).append(", ").append(address.getAdminArea()).append("\n");
						sb.append(address.getCountryName()).append(", ").append(address.getPostalCode());
						
						longitude.setText(sb.toString());
						
						// when get the data, stop GPS
						lmanager.removeUpdates(gpsListener);
						Toast.makeText(AddSite.this, getString(R.string.AddSite_gotGPS), Toast.LENGTH_SHORT).show();
					}
					else {
						longitude.setText(getString(R.string.AddSite_gettingGPS));
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