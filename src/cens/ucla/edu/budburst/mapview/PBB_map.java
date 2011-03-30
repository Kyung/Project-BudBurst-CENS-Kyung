package cens.ucla.edu.budburst.mapview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.R.string;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.MyLocOverlay;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.QuickCapture;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PBB_map extends MapActivity {
	private SharedPreferences pref;
	private OneTimeDBHelper otDBH = null;
	private GpsListener gpsListener;
	
	// Map related variables
	private LocationManager mLocManager = null;
	private MapView mMapView = null;
	//private MyLocOverlay mMyOverLay = null;
	private MyLocationOverlay mMyOverLay = null;
	private MapController mMapController = null;
	
	// Dialog
	private ProgressDialog mDialog;
	
	// other variables
	private String signalLevelString = null;
	private String url = null;	
	private double latitude 		= 0.0;
	private double longitude 		= 0.0;
	private boolean mHandlerDone	= false;
	
	private static final int GET_GPS_SIGNAL = 10;
	private static final int GET_MY_OBSERVED_LISTS = 11;
	private static final int EXCELLENT_LEVEL = 75;
	private static final int GOOD_LEVEL = 50;
	private static final int MODERATE_LEVEL = 25;
	private static final int WEAK_LEVEL = 0;
	private boolean viewFlag = false;
	
	private ArrayList<PlantItem> plantList;
	private PlantItem pItem;
	
	// timer variables
	private Timer timer;
	
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
			
			//signal.setText(" Signal Strength : " + signalLevelString);
			
			if(signalLevelString.equals("Weak")) {
				//Toast.makeText(PBB_map.this, "Your Signal Strength is quite low. You may not see the map well.", Toast.LENGTH_SHORT).show();
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
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch(msg.what) {
			case GET_GPS_SIGNAL:
				Log.i("K", "get GET_GPS_SIGNAL");
				showSpeciesOnMap(true);
				break;
			case GET_MY_OBSERVED_LISTS:
				Log.i("K", "get GET_MY_OBSERVED_LISTS");
				mDialog.dismiss();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pbb_map);
		
		// startSignalLevelListener();
		
		// remove accuracy bar
		TextView titleBar = (TextView)findViewById(R.id.myloc_accuracy);
		titleBar.setVisibility(View.GONE);
		
		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Set MapView
		mMapView = (MapView)findViewById(R.id.map);
		
		// Set mapController
		mMapController = mMapView.getController();
		mMapController.setZoom(12);
		
		// Add mylocation overlay
		mMyOverLay = new MyLocationOverlay(PBB_map.this, mMapView);
		mMyOverLay.enableMyLocation();
		
		gpsListener = new GpsListener();
		
		// check if GPS is turned on...
		if (mLocManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			
			Location lastLoc = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			// if we can get the last known location, use that first.
			if(lastLoc.getLatitude() != 0.0) {
				latitude = lastLoc.getLatitude();
			    longitude = lastLoc.getLongitude();
			    
			    // convert points into GeoPoint
			    GeoPoint gPoint = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));
			    // center the map
			    mMapController.setCenter(gPoint);

			    // load my species from the database
			    showSpeciesOnMap(false);
			}
			// else try to get the new GPS
			else {
				getNewGPS();			
			}
		}
		else {
		   	
		 new AlertDialog.Builder(PBB_map.this)
		   		.setTitle("Turn On GPS")
		   		.setMessage(getString(R.string.Message_locationDisabledTurnOn))
		   		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		   			public void onClick(DialogInterface dialog, int whichButton) {
		   				Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
		   				startActivityForResult(intent, 1);
		   			}
		   		})
		   		.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
		   			public void onClick(DialogInterface dialog, int whichButton) {
		    				
		   			}
		   		})
		   		.show();
		}	
	}
	
	
	public void showSpeciesOnMap(boolean hasHandler) {
		       
		Log.i("K", "in showSpeciesOnMap() function");
		
		// set criteria
		//Criteria criteria = new Criteria();
		//criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		//criteria.setAltitudeRequired(false);
		//criteria.setBearingRequired(false);
		//criteria.setCostAllowed(false);
		//criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		//String provider = mLocManager.getBestProvider(criteria, true);    
		    
		//Location lastLoc = mLocManager.getLastKnownLocation(provider);
		//if(lastLoc.getLatitude() != 0.0) {
		//	latitude = lastLoc.getLatitude();
		//    longitude = lastLoc.getLongitude();
		//}
		
		//pref = getSharedPreferences("userinfo",0);
		// TODO Auto-generated method stub
		otDBH = new OneTimeDBHelper(PBB_map.this);
		
		GeoPoint gPoint = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));
		
		//ClickReceiver clickRecvr = new ClickReceiver(PBB_map.this, gPoint);
		//mMapView.getOverlays().add(clickRecvr);
		
		//mMapView.removeAllViews();
		mMapView.setBuiltInZoomControls(true);
		
		
		// add button in the map(top)
		/*
	    Button mapBtn = new Button(getApplicationContext());
	    mapBtn.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.ic_menu_gallery));
	    
	    MapView.LayoutParams screenLP;
	    screenLP = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
	    									MapView.LayoutParams.WRAP_CONTENT,
	    									235,0,
	    									MapView.LayoutParams.TOP_LEFT);
	    mMapView.addView(mapBtn, screenLP);
		*/
		
		/*
		 * Call species information from the local database
		 */
		if(getMyListsFromDB()) {
			Log.i("K", "Get species lists in the database");
			
			Drawable marker = getResources().getDrawable(R.drawable.marker);
			marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
			
			//MapView mapView, Drawable marker, ArrayList<PlantItem> plantList
			mMapView.getOverlays().add(new SpeciesMapOverlay(mMapView, marker, plantList));
			mMapView.getOverlays().add(mMyOverLay);
			
			mMapController.setCenter(gPoint);
			
			
			if(hasHandler) {
				mHandlerDone = true;
				mHandler.sendEmptyMessage(GET_MY_OBSERVED_LISTS);
				mMapController.setZoom(12);
			}
		}
		else {
			Log.i("K", "No species lists in the database.");
		}

	}
	
	public void showUsersSpeciesOnMap() {
		
		Log.i("K", "in showUsersSpeciesOnMap() function");
		
		GeoPoint gPoint = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));
		mMapView.setBuiltInZoomControls(true);
		
		mMapView.invalidate();
		
		Drawable marker = getResources().getDrawable(R.drawable.marker);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		
		//MapView mapView, Drawable marker, ArrayList<PlantItem> plantList
		mMapView.getOverlays().add(new SpeciesMapOverlay(mMapView, marker, plantList));
		mMapView.getOverlays().add(mMyOverLay);
		
		mMapController.setCenter(gPoint);
	}
	
	public boolean getMyListsFromDB() {
		SyncDBHelper sDBH = new SyncDBHelper(this);
		OneTimeDBHelper oDBH = new OneTimeDBHelper(this);
		
		// initialize plantList
		plantList = new ArrayList<PlantItem>();
		
		// add myPlantList from Monitored Plants
		plantList = sDBH.getAllMyListInformation(this);
		
		// add myPlantList from Shared Plants
		plantList.addAll(oDBH.getAllMyListInformation(this));
		
		Log.i("K", "the number of mylist (size) : " + plantList.size());
		
		sDBH.close();
		oDBH.close();
		
		if(plantList.size() > 0) {
			return true;
		}
		return false;
	}
	
	public void getOtherUsersListsFromServer(int category) {
		
		SpeciesOthersFromServer getSpecies = new SpeciesOthersFromServer(PBB_map.this, mMapView, mMyOverLay, category);
		getSpecies.execute(getString(R.string.get_onetimeob_others) + 
				"?latitude=" + latitude + "&longitude=" + longitude);
		
		/*
		while(true) {
			
			Log.i("K", "Waiting....");
			
			if(getSpecies.finishDownloading()) {
				// initialize plantList
				plantList = new ArrayList<PlantItem>();
				
				// get other users' shared plant observations
				plantList = getSpecies.getPlantList();
				
				Log.i("K", "the number of mylist (size) : " + plantList.size());
				
				if(plantList.size() > 0) {
					break;
				}
			}
		}
		return true;
		*/
	}

	@Override
	public void onResume() {
		super.onResume();
		mMyOverLay.enableCompass();
	}		

	@Override
	public void onPause() {
		super.onPause();
		mMyOverLay.disableCompass();
	}		
	
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, getString(R.string.PBBMapMenu_myLocation)).setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, 2, 0, getString(R.string.PBBMapMenu_changeView)).setIcon(android.R.drawable.ic_menu_revert);
		//menu.add(0, 3, 0, getString(R.string.PBBMapMenu_seeLists)).setIcon(android.R.drawable.ic_menu_recent_history);
		menu.add(0, 4, 0, getString(R.string.PBBMapMenu_refresh)).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, 5, 0, getString(R.string.otherCategoryMap)).setIcon(android.R.drawable.ic_menu_gallery);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case 1:
				GeoPoint current_point = null;
				if(latitude == 0.0) {
					Toast.makeText(PBB_map.this, getString(R.string.Alert_gettingGPS), Toast.LENGTH_SHORT).show();
				}
				else {
					current_point = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));

					mMapController = mMapView.getController();
					mMapController.animateTo(current_point);
					mMapController.setZoom(15);
				}
				return true;
			case 2:
				if(!viewFlag) {
					mMapView.setSatellite(true);
					viewFlag = true;
				}
				else {
					mMapView.setSatellite(false);
					viewFlag = false;
				}
				
				return true;				
			case 3:	
				Intent intent = new Intent(PBB_map.this, PlantList.class);
				startActivity(intent);
					
				return true;
			case 4:
				getNewGPS();
				return true;
			case 5:
				new AlertDialog.Builder(PBB_map.this)
		   		.setTitle("Category")
		   		.setNegativeButton("Back", null)
		   		.setItems(R.array.plantcategory, new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String[] category = getResources().getStringArray(R.array.plantcategory);

						if(category[which].equals("Shared Plants")) {
							getOtherUsersListsFromServer(0);
						}
						else if(category[which].equals("Trees")){
							getOtherUsersListsFromServer(1);
						}
						else {
							
						}
					}
		   		})
		   		.show();
				
				
				return true;
		}
		return false;
	}
	
	private void getNewGPS() {
		mDialog = new ProgressDialog(this);
		mDialog.setMessage(getString(R.string.Map_Getting_GPS_Signal));
		mDialog.setCancelable(true);
		mDialog.show();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Looper.prepare();
				// set update the location data in 1secs or 5meters
				mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
				
				Looper.loop();
			}
			
		}).start();

	}
	
	@Override
	public void onDestroy() {
		// when user finish this activity, turn off the gps
		mLocManager.removeUpdates(gpsListener);
		
		// if there's a overlay, should call disableCompass() explicitly
		mMyOverLay.disableCompass();
		mMyOverLay.disableMyLocation();
	
		// terminate telephony
		stopListening();
	
		super.onDestroy();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	// or when user press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			mLocManager.removeUpdates(gpsListener);
			
			// if there's a overlay, should call disableCompass() explicitly.
			mMyOverLay.disableMyLocation();
			mMyOverLay.disableCompass();
			
			// terminate telephony
			stopListening();
		
			finish();
			return true;
		}
		return false;
	}
 
	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}
	
	class GpsListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
			if(loc != null) {
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				
				if(!mHandlerDone) {
					mHandler.sendEmptyMessage(GET_GPS_SIGNAL);
				}
				//timer.cancel();
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

	
	
	
	
	
	
	
	
	
	
	
	
	 /*
	// getting information of markers from the server
	public class DownloadSpeciesList extends AsyncTask<String, Integer, Void> {
		ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(PBB_map.this, getString(R.string.Alert_loading), getString(R.string.PBBMap_loadingComponents), true);
		}
		@Override
		protected Void doInBackground(String... get_url) {
			
			boolean flag = pref.getBoolean("Update", false);

			
			// flag == false means "let's update!"
			if(!flag) {

				SharedPreferences.Editor edit = pref.edit();				
				edit.putBoolean("Update", true);
				edit.commit();
				
				HttpClient httpClient = new DefaultHttpClient();
				String url = new String(get_url[0]);

				HttpPost httpPost = new HttpPost(url);
				
				try {
					HttpResponse response = httpClient.execute(httpPost);
					
					if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						JSONHelper jHelper = new JSONHelper();
						
						String line = null;
						String[] str = null;
						line = br.readLine();
						
						Log.i("K", "Line : " + line);
						
						String getAreaByJSON = jHelper.getFlickrPBBTags(line);
						
						
						Log.i("K", "JSON : " + getAreaByJSON);
						
						
						str = getAreaByJSON.split("\n\n");

						// open database and set it writable
						SQLiteDatabase db;
						db = otDBH.getWritableDatabase();
						
						for(int i = 0 ; i < str.length ; i++) {
							String[] split = str[i].split(";;");

							
							split[0] = split[0].replace("'", "");
							split[1] = split[1].replace("'", "");
							split[2] = split[2].replace("'", "");
							
							//insert into table
							//common_name, science_name, phenophase, dt_taken, lat, lon, distance
							db.execSQL("INSERT INTO pbbFlickrLists VALUES("
									+ "'" + split[0] + "',"
									+ "'" + split[1] + "',"
									+ "'" + split[2] + "',"
									+ "'" + split[3] + "',"
									+ "'" + split[4] + "',"
									+ "'" + split[5] + "',"
									+ "" + split[6] + ");");
							
							Log.i("K", "inserted into the pbbFlickrLists table....");
			
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

			map = null;
			map=(MapView)findViewById(R.id.map);			
		    // show some text on the map
		    int x = 10;
		    int y = 10;

		    TextView et1 = new TextView(getApplicationContext());
		    et1.setText(getString(R.string.PBBMap_touchMarker));
		    et1.setTextColor(Color.BLACK);
		    
		    MapView.LayoutParams screenLP;
		    screenLP = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
		    									MapView.LayoutParams.WRAP_CONTENT,
		    									x, y,
		    									MapView.LayoutParams.TOP_LEFT);
		    map.addView(et1, screenLP);    
		    // end show...
		    
		   
		    Button mapBtn = new Button(getApplicationContext());
		    mapBtn.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.ic_menu_gallery));
		    
		    screenLP = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
		    									MapView.LayoutParams.WRAP_CONTENT,
		    									235,0,
		    									MapView.LayoutParams.TOP_LEFT);
		    map.addView(mapBtn, screenLP);
		    
			
		    mapBtn.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(latitude == 0.0) {
						Toast.makeText(PBB_map.this, "Getting your location. Please wait...", Toast.LENGTH_SHORT).show();
					}
					else {
						Intent intent = new Intent(PBB_map.this, Whatspopular.class);
						intent.putExtra("lat", latitude);
						intent.putExtra("lng", longitude);
						startActivity(intent);
					}
				}
		    });
			

		    //List<Overlay> mapOverlays = myMap.getOverlays();
			Drawable marker = getResources().getDrawable(R.drawable.marker);
			marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
			
			me.enableMyLocation();
			map.getOverlays().add(new SitesOverlay(marker));
			map.getOverlays().add(me);
			
			// get current location
			GeoPoint current_point = null;
			
			map.setBuiltInZoomControls(true);
			mc = map.getController();
			
			if(mLocManager.getLastKnownLocation("gps") == null) {
				current_point = new GeoPoint((int)(0.0), (int)(0.0));
				mc.animateTo(current_point);
				mc.setZoom(1);
			}
			else {
				current_point = new GeoPoint((int)(mLocManager.getLastKnownLocation("gps").getLatitude() * 1000000), (int)(mLocManager.getLastKnownLocation("gps").getLongitude() * 1000000));
				mc.animateTo(current_point);
				mc.setZoom(9);
			}
			
			// end get current location
			
			map.setSatellite(false);
			map.invalidate();

			dialog.dismiss();
		}
	}
	*/