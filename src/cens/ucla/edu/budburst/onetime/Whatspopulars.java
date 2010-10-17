package cens.ucla.edu.budburst.onetime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.onetime.Whatsinvasive.DoAsyncTask;
import cens.ucla.edu.budburst.onetime.Whatsinvasive.species;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Whatspopulars extends MapActivity {

	private static GpsListener gpsListener;
	private LocationManager lm = null;
	private MapView myMap = null; 
	private SharedPreferences pref;
	private OneTimeDBHelper otDBH = null;
	private MyLocationOverlay myLocOverlay = null;
	private MapController mc = null;
	private String url = null;
	private TextView geo = null;
	private TextView signal = null;
	private double latitude 		= 0.0;
	private double longitude 		= 0.0;
	private GeoPoint last_point = null;
	private String signalLevelString = null;
	
	private static final int EXCELLENT_LEVEL = 75;
	private static final int GOOD_LEVEL = 50;
	private static final int MODERATE_LEVEL = 25;
	private static final int WEAK_LEVEL = 0;
	
	
	private void startSignalLevelListener() {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    	int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTH; 
 
    	tm.listen(phoneStateListener, events);
    }
	
	private void stopListening(){
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
	}
	
	private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
		@Override
		public void onSignalStrengthChanged(int asu)
		{
			Log.i("K", "onSignalStrengthChanged " + asu);
			
			int level = (int) ((((float)asu)/31.0) * 100);
			//setSignalLevel(info_ids[INFO_SIGNAL_LEVEL_INDEX],info_ids[INFO_SIGNAL_LEVEL_INFO_INDEX],asu);
			
			signalLevelString = "Weak";
			
			if(level > EXCELLENT_LEVEL)		signalLevelString = "Excellent";
			else if(level > GOOD_LEVEL)		signalLevelString = "Good";
			else if(level > MODERATE_LEVEL)	signalLevelString = "Moderate";
			else if(level > WEAK_LEVEL)		signalLevelString = "Weak";
			
			signal.setText(" Signal Strength : " + signalLevelString);
			
			if(signalLevelString.equals("Weak")) {
				Toast.makeText(Whatspopulars.this, "Your Signal Strength is quite low. You may not see the map well.", Toast.LENGTH_SHORT).show();
			}
			
			super.onSignalStrengthChanged(asu);
		}
	};
	
	/*
	private void setSignalLevel(int id,int infoid,int level){
		int progress = (int) ((((float)level)/31.0) * 100);
		String signalLevelString = getSignalLevelString(progress);
		
		Log.i("signalLevel ","" + signalLevelString);
	}
	*/
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.whatspopulars);
	    setTitle("What's popular map mode");
	    
	    startSignalLevelListener();
	    
		geo = (TextView) findViewById(R.id.geodata);
		signal = (TextView) findViewById(R.id.distance);
	    
		gpsListener = new GpsListener();
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		// set update the location data in 3secs or 30meters
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 30, gpsListener);
		       
		// check if GPS is turned on...
		if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
		   	
		}
		else {
		   	
		 new AlertDialog.Builder(Whatspopulars.this)
		   		.setTitle("Turn On GPS")
		   		.setMessage("To get your current location, you need to turn on GPS. Would you like to go to the setting page?")
		   		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		   			public void onClick(DialogInterface dialog, int whichButton) {
		   				Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
		   				startActivityForResult(intent, 1);
		   			}
		   		})
		   		.setNegativeButton("No", new DialogInterface.OnClickListener() {
		   			public void onClick(DialogInterface dialog, int whichButton) {
		    				
		   			}
		   		})
		   		.show();
		    }
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		    
		String provider = lm.getBestProvider(criteria, true);
		    
		Location loca = lm.getLastKnownLocation(provider);
		if(loca.getLatitude() != 0.0) {
			latitude = loca.getLatitude();
		    longitude = loca.getLongitude();
		    String strLocs = String.format(" Current Location : %7.3f, %7.3f", latitude, longitude);
				
			geo.setText(strLocs);
		}
		    
		// TODO Auto-generated method stub
		otDBH = new OneTimeDBHelper(Whatspopulars.this);
		    
		pref = getSharedPreferences("userinfo",0);
		String username = pref.getString("Username","");
		String password = pref.getString("Password","");
		    
		url = "http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/comment.php?username=" + username + "&password=" + password;
		    
		new DoAsyncTask().execute(url);
	}
	
	public void onResume() {
		super.onResume();

		myMap = (MapView) findViewById(R.id.simpleGM_map);
	}
	
	public void onRestart() {
		super.onRestart();
		Log.i("K", " I AM IN onRestart!");
		
		pref = getSharedPreferences("userinfo",0);
		String lat = pref.getString("lat", "");
		String lng = pref.getString("lng", "");
		
		Log.i("K", " LAT : " + lat + " LNG : " + lng);
		
		GeoPoint latest_point;
		
		if(lat == "") {
			latest_point = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));	
		}
		else {
			latest_point = new GeoPoint((int)(Double.parseDouble(lat) * 1000000), (int)(Double.parseDouble(lng) * 1000000));
		}

		mc = myMap.getController();
		mc.animateTo(latest_point);
		mc.setZoom(13);
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == 0) {
			if(requestCode == 1) {
				SharedPreferences.Editor edit = pref.edit();				
				edit.putBoolean("Update", false);
				edit.commit();
				
				new DoAsyncTask().execute(url);
			}
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	class DoAsyncTask extends AsyncTask<String, Integer, Void> {
		ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(Whatspopulars.this, "Loading...", "Loading map components...", true);
		}
		@Override
		protected Void doInBackground(String... get_url) {
			
			boolean flag = pref.getBoolean("Update", false);
			
			if(!flag) {
				SharedPreferences.Editor edit = pref.edit();				
				edit.putBoolean("Update", true);
				edit.commit();
				
				HttpClient httpClient = new DefaultHttpClient();
				String url = new String(get_url[0]);
				Log.i("K", "URL : " + url);
				HttpPost httpPost = new HttpPost(url);
				
				try {
					HttpResponse response = httpClient.execute(httpPost);
					
					if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						String line = "{'comment':";
						String[] str = null;
						
						line += br.readLine();
						line += "}";
						
						Log.i("K", "Line : " + line);
						
						JSONHelper jHelper = new JSONHelper();
						String getAreaByJSON = jHelper.getCommentTags(line);
						str = getAreaByJSON.split("\n\n\n\n");

						// open database and set it writable
						SQLiteDatabase db;
						db = otDBH.getWritableDatabase();
						
						OneTimeDBHelper onehelper = new OneTimeDBHelper(Whatspopulars.this);
						onehelper.clearPopularLists(Whatspopulars.this);

						for(int i = 0 ; i < str.length ; i++) {
							String[] split = str[i].split(";;");

							/*
							String image_name = null;
							
							try {
								SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
								String randomNum = new Integer(prng.nextInt()).toString();
								MessageDigest sha = MessageDigest.getInstance("SHA-1");
								byte[] result = sha.digest(randomNum.getBytes());
								image_name = hexEncode(result);
							} catch (NoSuchAlgorithmException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							*/
							
							//Log.i("K", "COMMENTS : " + split[7]);
							
							//insert into table
							// pheno, cname, sname, latitude, longitude, dt_taken, comment_count, comment
							db.execSQL("INSERT INTO popularLists VALUES("
									+ "'" + split[0] + "',"
									+ "'" + split[1] + "',"
									+ "'" + split[2] + "',"
									+ "'" + split[3] + "',"
									+ "'" + split[4] + "',"
									+ "'" + split[5] + "',"
									+ "'" + split[6] + "',"
									+ "'no comments');"); // temporary no comments
							
							Log.i("K", "inserted into the table....");
							
						}
						
						db.close();
						otDBH.close();
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// TODO Auto-generated method stub
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			Log.i("K", "I am in onPostExecute!!!!");
			//mylistapdater = new MyListAdapter(Whatspopular.this, R.layout.plantlist_item ,arSpeciesList);
			//ListView MyList = getListView();
			//MyList.setAdapter(mylistapdater);

			
		    // show some text on the map
		    int x = 10;
		    int y = 10;
		  
		    TextView et1 = new TextView(getApplicationContext());
		    et1.setText("Touch the marker to see the details.");
		    et1.setTextColor(Color.BLACK);
		    
		    MapView.LayoutParams screenLP;
		    screenLP = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
		    									MapView.LayoutParams.WRAP_CONTENT,
		    									x, y,
		    									MapView.LayoutParams.TOP_LEFT);
		    myMap.addView(et1, screenLP);    
		    // end show...
		    
		    
		    Button mapBtn = new Button(getApplicationContext());
		    mapBtn.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.ic_menu_myplaces));
		    
		    screenLP = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
		    									MapView.LayoutParams.WRAP_CONTENT,
		    									8,30,
		    									MapView.LayoutParams.TOP_LEFT);
		    myMap.addView(mapBtn, screenLP);
		    
			
		    mapBtn.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(latitude == 0.0) {
						Toast.makeText(Whatspopulars.this, "Getting your location. Please wait...", Toast.LENGTH_SHORT).show();
					}
					else {
						Intent intent = new Intent(Whatspopulars.this, Whatspopular.class);
						intent.putExtra("lat", latitude);
						intent.putExtra("lng", longitude);
						startActivity(intent);
					}
				}
		    });
			
		    // read data from the table
			SQLiteDatabase db;
			db = otDBH.getReadableDatabase();
			
			String query = null;
			query = "SELECT pheno, cname, sname, lat, lng, dt_taken, c_count, comments FROM popularLists";
		
		    List<Overlay> mapOverlays = myMap.getOverlays();
			Drawable marker = getResources().getDrawable(R.drawable.marker);
			HelloItemizedOverlay itemizedOverlay = new HelloItemizedOverlay(marker, Whatspopulars.this, myMap);
			
			Cursor cursor = db.rawQuery(query, null);
			
		    while(cursor.moveToNext()) {
		    	
		    	String real_info_title = cursor.getString(0) + ";;" + 
		    							cursor.getString(1) + ";;" +
		    							cursor.getString(2) + ";;" +
		    							cursor.getString(3) + ";;" +
		    							cursor.getString(4) + ";;" +
		    							cursor.getString(5) + ";;" +
		    							cursor.getString(6) + ";;" +
		    							cursor.getString(7);
		    	
		    	Log.i("K", "db info : " + real_info_title);
		    	
		    	last_point = new GeoPoint((int)(Double.parseDouble(cursor.getString(3)) * 1000000), (int)(Double.parseDouble(cursor.getString(4)) * 1000000));
		    	OverlayItem overlayitem = new OverlayItem(last_point, cursor.getString(1), real_info_title);
		    	itemizedOverlay.addOverlay(overlayitem);
		    }
		    
			db.close();
			otDBH.close();
		    cursor.close();
		    
		    // add my current location -> this obviously requires location updates
		    // so, need to explicitly disableMyLocation() -> no more battery drains!!!!
		    myLocOverlay = new MyLocationOverlay(Whatspopulars.this, myMap);
			myLocOverlay.enableMyLocation();
			myMap.getOverlays().add(myLocOverlay);
			
			
			// need to multiply 1000000 to get the proper data
		    mapOverlays.add(itemizedOverlay);
			// end read and add to the map
			
			// get current location
			GeoPoint current_point = null;
			
			myMap.setBuiltInZoomControls(true);
			mc = myMap.getController();
			
			if(lm.getLastKnownLocation("gps") == null) {
				current_point = new GeoPoint((int)(0.0), (int)(0.0));
				mc.animateTo(current_point);
				mc.setZoom(1);
			}
			else {
				current_point = new GeoPoint((int)(lm.getLastKnownLocation("gps").getLatitude() * 1000000), (int)(lm.getLastKnownLocation("gps").getLongitude() * 1000000));
				mc.animateTo(current_point);
				mc.setZoom(10);
			}
			
			// end get current location
			
			myMap.setSatellite(false);
			myMap.invalidate();
			
			//Log.i("K", "" + arSpeciesList);

			dialog.dismiss();
		}
	}
	
	
		///////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0,"My Location").setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, 2, 0, "Update").setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, 3, 0, "Help").setIcon(android.R.drawable.ic_menu_help);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case 1:
				GeoPoint current_point = null;
				if(latitude == 0.0) {
					Toast.makeText(Whatspopulars.this, "Getting your location. Please wait...", Toast.LENGTH_SHORT).show();
				}
				else {
					current_point = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));

					mc = myMap.getController();
					mc.animateTo(current_point);
					mc.setZoom(15);
				}
				return true;
			case 2:
				SharedPreferences.Editor edit = pref.edit();				
				edit.putBoolean("Update", false);
				edit.commit();
				
				new DoAsyncTask().execute(url);
				
				return true;
			
			case 3:
				Toast.makeText(Whatspopulars.this, "Coming soon", Toast.LENGTH_SHORT).show();
				
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
	

	
    public void onDestroy() {
    	Log.i("K", "IN onDestory()");
    	
    	// when user finish this activity, turn off the gps
    	lm.removeUpdates(gpsListener);
    	// if there's a overlay, should call disableCompass() explicitly!!!!
    	myLocOverlay.disableCompass();
    	myLocOverlay.disableMyLocation();
    	
    	// terminate telephony
    	stopListening();
    	
        super.onDestroy();
    }

    // or when user press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			Log.i("K", "IN BACK BUTTON");
			lm.removeUpdates(gpsListener);
	    	// if there's a overlay, should call disableCompass() explicitly!!!!
			myLocOverlay.disableMyLocation();
			myLocOverlay.disableCompass();
			
			// terminate telephony
	    	stopListening();
			
			finish();
			return true;
		}
		return false;
	}
	
	
	private class GpsListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
			if(loc != null) {
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				String strLoc = String.format(" Current Location : %10.5f, %10.5f", latitude, longitude);
				
				geo.setText(strLoc);
			}
		}
		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
		}
	}
}
