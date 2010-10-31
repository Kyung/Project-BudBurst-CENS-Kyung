package cens.ucla.edu.budburst.onetime;

/* Copyright (c) 2008-2010 -- CommonsWare, LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import android.app.Activity;
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
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class MyNearestPlants extends MapActivity {
	
	private SharedPreferences pref;
	private OneTimeDBHelper otDBH = null;
	private static GpsListener gpsListener;
	private LocationManager lm = null;
	private MapView map=null;
	private MyLocationOverlay me=null;
	private MapController mc = null;
	private String signalLevelString = null;
	private String url = null;	
	private double latitude 		= 0.0;
	private double longitude 		= 0.0;
	private static final int EXCELLENT_LEVEL = 75;
	private static final int GOOD_LEVEL = 50;
	private static final int MODERATE_LEVEL = 25;
	private static final int WEAK_LEVEL = 0;
	private boolean viewFlag = false;
	protected CharSequence[] items = {"Invasive Plants", "Blooming Flowers"};
	protected boolean[] selections = new boolean[items.length];
	
	
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
				Toast.makeText(MyNearestPlants.this, "Your Signal Strength is quite low. You may not see the map well.", Toast.LENGTH_SHORT).show();
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		  // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.mynearestplants);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.flora_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  What's Blooming");
		
		startSignalLevelListener();
		
		gpsListener = new GpsListener();
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		// set update the location data in 3secs or 30meters
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 30, gpsListener);
		       
		// check if GPS is turned on...
		if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
		   	
		}
		else {
		   	
		 new AlertDialog.Builder(MyNearestPlants.this)
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
				
			//geo.setText(strLocs);
		}
		    
		// TODO Auto-generated method stub
		otDBH = new OneTimeDBHelper(MyNearestPlants.this);
		    
		pref = getSharedPreferences("userinfo",0);

		map=(MapView)findViewById(R.id.map);
	
		//map.getController().setCenter(getPoint(40.76793169992044, -73.98180484771729));
		//map.getController().setZoom(14);
		//map.setBuiltInZoomControls(true);
	
		//Drawable marker=getResources().getDrawable(R.drawable.marker);
	
		//marker.setBounds(0, 0, marker.getIntrinsicWidth(),marker.getIntrinsicHeight());
	
		//map.getOverlays().add(new SitesOverlay(marker));
	
		//me=new MyLocationOverlay(this, map);
		//map.getOverlays().add(me);
		
		Log.i("K", "LAT : " + latitude + " LON : " + longitude);
		
		mc = map.getController();
		me = new MyLocationOverlay(MyNearestPlants.this, map);
		
		url = "http://whatsinvasive.com/~kshan/WB_TEST/flickr_response.php?lat=" + latitude + "&lon=" + longitude;
	
		new DoAsyncTask().execute(url);
	}

	@Override
	public void onResume() {
		super.onResume();
		me.enableCompass();

	}		

	@Override
	public void onPause() {
		super.onPause();
		me.disableCompass();
	}		

	///////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, "My Location").setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, 2, 0, "Change View").setIcon(android.R.drawable.ic_menu_revert);
		menu.add(0, 3, 0, "Refresh").setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, 4, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case 1:
				GeoPoint current_point = null;
				if(latitude == 0.0) {
					Toast.makeText(MyNearestPlants.this, "Getting your location. Please wait...", Toast.LENGTH_SHORT).show();
				}
				else {
					current_point = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));

					mc = map.getController();
					mc.animateTo(current_point);
					mc.setZoom(16);
				}
				return true;
			case 2:
				if(!viewFlag) {
					map.setSatellite(true);
					viewFlag = true;
				}
				else {
					map.setSatellite(false);
					viewFlag = false;
				}
				
				return true;				
			case 3:
				SharedPreferences.Editor edit = pref.edit();				
				edit.putBoolean("Update", false);
				edit.commit();
				
				// delete all components in PopularLists
				SQLiteDatabase db;
				db = otDBH.getWritableDatabase();
				
				OneTimeDBHelper onehelper = new OneTimeDBHelper(MyNearestPlants.this);
				onehelper.clearFlickr(MyNearestPlants.this);
				
				db.close();
				onehelper.close();
				
				for(int i = 0 ; i < selections.length ; i++) {
					selections[i] = true;
				}
				
				new DoAsyncTask().execute(url);
				
				return true;
			
			case 4:
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MyNearestPlants.this);
				builder.setTitle("Pick your preference");
				builder.setCancelable(true);
				builder.setMultiChoiceItems(items, selections, new DialogInterface.OnMultiChoiceClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						// TODO Auto-generated method stub
						Log.i("K", "SELECTION  : " + selections[which]);
						Log.i("K", "isChecked : " + isChecked);
						
						if(isChecked) {
							selections[which] = true;
							Log.i("K", "SELECTION  : " + selections[which]);
							final AlertDialog alert = (AlertDialog)dialog;
							final ListView list = alert.getListView();
							list.setItemChecked(which, true);
						}
						else {
							selections[which] = false;
							final AlertDialog alert = (AlertDialog)dialog;
							final ListView list = alert.getListView();
							list.setItemChecked(which, false);
						}
					}
				});
				builder.setIcon(R.drawable.pbbicon_small);
				builder.setPositiveButton("Choose", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						// open database and set it writable
						SQLiteDatabase db;
						db = otDBH.getWritableDatabase();
						
						for(int i = 0 ; i < selections.length ; i++) {
							if(!selections[i]) {
								db.execSQL("DELETE FROM flickrLists WHERE category =" + i + "");
								Log.i("K", "DB DELETED category = " + i);
							}
						}
						
						db.close();
						
						map = null;
						map = (MapView)findViewById(R.id.map);
						
						Drawable marker = getResources().getDrawable(R.drawable.marker);
						marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
						
						map.getOverlays().add(new SitesOverlay(marker));
						me.enableMyLocation();
						map.getOverlays().add(me);
						
						//SharedPreferences.Editor edit = pref.edit();				
						//edit.putBoolean("Update", false);
						//edit.commit();
						
						// delete all components in PopularLists
						//SQLiteDatabase db;
						//db = otDBH.getWritableDatabase();
						
						//OneTimeDBHelper onehelper = new OneTimeDBHelper(MyNearestPlants.this);
						//onehelper.clearFlickr(MyNearestPlants.this);
						
						//db.close();
						//onehelper.close();
						
						//new DoAsyncTask().execute(url);

					}
				});
				builder.setNegativeButton("Back", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				});
				builder.show();

				
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////

	@Override
	protected boolean isRouteDisplayed() {
		return(false);
	}

	@Override
	public void onDestroy() {
		Log.i("K", "IN onDestory()");
	
		// when user finish this activity, turn off the gps
		lm.removeUpdates(gpsListener);
		// if there's a overlay, should call disableCompass() explicitly!!!!
		me.disableCompass();
		me.disableMyLocation();
	
		// terminate telephony
		stopListening();
	
		super.onDestroy();
	}
	
	class DoAsyncTask extends AsyncTask<String, Integer, Void> {
		ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(MyNearestPlants.this, "Loading...", "Loading map components...", true);
		}
		@Override
		protected Void doInBackground(String... get_url) {
			
			boolean flag = pref.getBoolean("Update", false);

			
			// flag == false means "let's update!"
			if(!flag) {

				SharedPreferences.Editor edit = pref.edit();				
				edit.putBoolean("Update", true);
				edit.commit();
				
				int length = get_url.length;
				
				Log.i("K", "URL LENGTH : " + length);
				
				HttpClient httpClient = new DefaultHttpClient();
				String url = new String(get_url[0]);
				Log.i("K", "URL : " + url);
				int category = 0;
				if(selections[0] && !selections[1]) {
					category = 0;
				}
				if(selections[0] && !selections[1]) {
					category = 1;
				}
				if(selections[0] && selections[1]) {
					category = 2;
				}

				HttpPost httpPost = new HttpPost(url + "&category=" + category);
				
				try {
					HttpResponse response = httpClient.execute(httpPost);
					
					if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						JSONHelper jHelper = new JSONHelper();
						
						String line = null;
						String[] str = null;
						line = br.readLine();
						
						Log.i("K", "Line : " + line);
						
						String getAreaByJSON = jHelper.getFlickrTags(line);
						
						
						Log.i("K", "JSON : " + getAreaByJSON);
						
						
						str = getAreaByJSON.split("\n\n");

						// open database and set it writable
						SQLiteDatabase db;
						db = otDBH.getWritableDatabase();
						
						OneTimeDBHelper onehelper = new OneTimeDBHelper(MyNearestPlants.this);
						onehelper.clearPopularLists(MyNearestPlants.this);

						for(int i = 0 ; i < str.length ; i++) {
							String[] split = str[i].split(";;");
							// distance value
							float[] distance = new float[2];
							
							Location.distanceBetween(latitude, longitude, Double.parseDouble(split[5]), Double.parseDouble(split[6]), distance);
							// the measurement is "meter"
							Log.i("K", "DISTANCE : " + distance[0]);
							
							distance[0] *= 0.00062137;
							
							int dot = String.valueOf(distance[0]).indexOf(".");
							String dotString = String.valueOf(distance[0]).substring(0, dot+3);
							
							Log.i("K", "DOT_STRING : " + dotString);
							
							split[3] = split[3].replace("'", "");
							split[7] = split[7].replace("'", "");
							split[8] = split[8].replace("'", "");
							
							//insert into table
							// category : 0 -> What's invasive
							// category : 1 -> What's blooming
							// id, secret, farm, title, dt_taken, lat, lon, owner, server, distance, category
							db.execSQL("INSERT INTO flickrLists VALUES("
									+ "'" + split[0] + "',"
									+ "'" + split[1] + "',"
									+ "'" + split[2] + "',"
									+ "'" + split[3] + "',"
									+ "'" + split[4] + "',"
									+ "'" + split[5] + "',"
									+ "'" + split[6] + "',"
									+ "'" + split[7] + "',"
									+ "'" + split[8] + "',"
									+ "" + Double.parseDouble(dotString) + "," 
									+ "" + split[9] + ");");
							
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

			map = null;
			map=(MapView)findViewById(R.id.map);			
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
						Toast.makeText(MyNearestPlants.this, "Getting your location. Please wait...", Toast.LENGTH_SHORT).show();
					}
					else {
						Intent intent = new Intent(MyNearestPlants.this, Whatspopular.class);
						intent.putExtra("lat", latitude);
						intent.putExtra("lng", longitude);
						startActivity(intent);
					}
				}
		    });
			

		    //List<Overlay> mapOverlays = myMap.getOverlays();
			Drawable marker = getResources().getDrawable(R.drawable.marker);
			marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
			
			
			map.getOverlays().add(new SitesOverlay(marker));
			me.enableMyLocation();
			map.getOverlays().add(me);
			
			// get current location
			GeoPoint current_point = null;
			
			map.setBuiltInZoomControls(true);
			mc = map.getController();
			
			
			if(lm.getLastKnownLocation("gps") == null) {
				current_point = new GeoPoint((int)(0.0), (int)(0.0));
				mc.animateTo(current_point);
				mc.setZoom(1);
			}
			else {
				current_point = new GeoPoint((int)(lm.getLastKnownLocation("gps").getLatitude() * 1000000), (int)(lm.getLastKnownLocation("gps").getLongitude() * 1000000));
				mc.animateTo(current_point);
				mc.setZoom(12);
			}
			
			// end get current location
			
			map.setSatellite(false);
			map.invalidate();

			dialog.dismiss();
		}
	}
	
	// or when user press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			Log.i("K", "IN BACK BUTTON");
			lm.removeUpdates(gpsListener);
			// if there's a overlay, should call disableCompass() explicitly!!!!
			me.disableMyLocation();
			me.disableCompass();
			// terminate telephony
			stopListening();
		
			finish();
			return true;
		}
		return false;
	}
 
	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0),
												(int)(lon*1000000.0)));
	}
	
	private class SitesOverlay extends ItemizedOverlay<CustomItem> {
		private List<CustomItem> item=new ArrayList<CustomItem>();
		private	PopupPanel panel=new PopupPanel(R.layout.popup);
	
		public SitesOverlay(Drawable marker) {
			super(null);
			
		    // read data from the table
			SQLiteDatabase db;
			db = otDBH.getReadableDatabase();
			
			String query = null;
			query = "SELECT id, secret, farm, title, dt_taken, lat, lon, owner, server, category FROM flickrLists";
			
			Cursor cursor = db.rawQuery(query, null);
			
		    while(cursor.moveToNext()) {
		    	
		    	String real_info_title = cursor.getString(0) + ";;" + 
		    							cursor.getString(1) + ";;" +
		    							cursor.getString(2) + ";;" +
		    							cursor.getString(3) + ";;" +
		    							cursor.getString(4) + ";;" +
		    							cursor.getString(5) + ";;" +
		    							cursor.getString(6) + ";;" +
		    							cursor.getString(7) + ";;" + 
		    							cursor.getString(8) + ";;" + 
		    							cursor.getInt(9);
		    	
		    	Log.i("K", "db info : " + real_info_title);
		    	
		    	if(cursor.getInt(9) == 0) {
		    		item.add(new CustomItem(getPoint(Double.parseDouble(cursor.getString(5)),Double.parseDouble(cursor.getString(6))),
			    			cursor.getString(3), cursor.getString(0) + ";;" + cursor.getString(7) + ";;" + cursor.getString(8), getMarker(R.drawable.flower_full_marker), marker));
		    	}
		    	else {
		    		item.add(new CustomItem(getPoint(Double.parseDouble(cursor.getString(5)),Double.parseDouble(cursor.getString(6))),
			    			cursor.getString(3), cursor.getString(0) + ";;" + cursor.getString(7) + ";;" + cursor.getString(8), getMarker(R.drawable.full_marker), marker));
		    	}
    	
		    }
		    
			db.close();
			otDBH.close();
		    cursor.close();
		    
		    
			populate();
		}
		
		private Drawable getMarker(int resource) {
			Drawable marker=getResources().getDrawable(resource);
			marker.setBounds(-marker.getIntrinsicWidth() / 2, marker.getIntrinsicHeight(), marker.getIntrinsicWidth() / 2, 0);
			boundCenter(marker);
			return(marker);
		}
	
		@Override
		protected CustomItem createItem(int i) {
			return(item.get(i));
		}
	
		@Override
		public void draw(Canvas canvas, MapView mapView,
										boolean shadow) {
			super.draw(canvas, mapView, false);
		}
		
		/*
		private Drawable LoadImageFromWebOperation(String url) {
			try {
				InputStream is = (InputStream) new URL(url).getContent();
				Drawable d = Drawable.createFromStream(is, "image.jpg");
				return d;
			}
			catch(Exception e) {
				return null;
			}
		}
		*/
		
		private Bitmap LoadImageFromWebOperation(String url) {
			try {
				URL imageURL = new URL(url);
				HttpURLConnection conn = (HttpURLConnection)imageURL.openConnection();
				conn.setDoInput(true);
				conn.connect();
				
				InputStream is = conn.getInputStream();
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				
				return bitmap; 
				
			}
			catch(Exception e) {
				return null;
			}
		}
		
		@Override
		protected boolean onTap(int i) {
			OverlayItem item=getItem(i);
			GeoPoint geo=item.getPoint();
			Point pt = map.getProjection().toPixels(geo, null);
			map.getController().setCenter(geo);
			View view=panel.getView();
		
			String temp = item.getSnippet();
			String[] split_temp = temp.split(";;");
			
			ImageView image = (ImageView)view.findViewById(R.id.imageView);
			
			SQLiteDatabase db;
			db = otDBH.getReadableDatabase();
			
			String query = null;
			query = "SELECT id, secret, farm, title, dt_taken, lat, lon, owner, server FROM flickrLists WHERE id=" + split_temp[0];
			
			Cursor cursor = db.rawQuery(query, null);
			String dt_taken = "";

			while(cursor.moveToNext()) {
				String url = "http://farm" + cursor.getString(2) + ".static.flickr.com/" + cursor.getString(8) + "/" + cursor.getString(0) + "_" + cursor.getString(1) + "_s.jpg";
				
				Log.i("K", "URL : " + url);
				
				Bitmap drawable = LoadImageFromWebOperation(url);
				image.setImageBitmap(drawable);
				
				dt_taken = cursor.getString(4);
			}
		    
			cursor.close();
			db.close();
			
			
			((TextView)view.findViewById(R.id.title))
			.setText("Title : " + item.getTitle() + "\nCredit : " + split_temp[1]);
			((TextView)view.findViewById(R.id.geodata))
			.setText("Lat : " + String.valueOf(geo.getLatitudeE6()/1000000.0) + "\nLon : " + String.valueOf(geo.getLongitudeE6()/1000000.0));
			((TextView)view.findViewById(R.id.dt_taken))
			.setText("Date : " + dt_taken);
			Button okayBtn = (Button)view.findViewById(R.id.okayBtn);
			okayBtn.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					panel.hide();
					map.invalidate();
				}
			});
			
			Button moreBtn = (Button)view.findViewById(R.id.detailBtn);
			moreBtn.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
				}
			});
		
			panel.show();
		
			return(true);
		}

	
		@Override
		public int size() {
			return(item.size());
		}
		
		void toggleHeart() {
			CustomItem focus=getFocus();
			
			if (focus!=null) {
				focus.toggleHeart();
			}
			
			map.invalidate();
		}

	}

	class PopupPanel {
		View popup;
		boolean isVisible=false;
		LayoutInflater inflater = getLayoutInflater();
	
		PopupPanel(int layout) {
			ViewGroup parent=(ViewGroup)map.getParent();

			popup=inflater.inflate(layout, parent, false);

		
			popup.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					hide();
				}
			});
		}
	
		View getView() {
			return(popup);
		}
	
		void show() {
			RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
			);
		
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp.setMargins(0, 30, 0, 0);
			
			
			hide();
			
			((ViewGroup)map.getParent()).addView(popup, lp);
			isVisible=true;
		}
		
		void hide() {
			if (isVisible) {
				isVisible=false;
				((ViewGroup)popup.getParent()).removeView(popup);
			}
		}
	}
	
	private class GpsListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
			if(loc != null) {
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				String strLoc = String.format(" Current Location : %10.5f, %10.5f", latitude, longitude);
				
				//geo.setText(strLoc);
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
	
	class CustomItem extends OverlayItem {
		Drawable marker=null;
		boolean isHeart=false;
		Drawable heart=null;
		
		CustomItem(GeoPoint pt, String name, String snippet,
							 Drawable marker, Drawable heart) {
			super(pt, name, snippet);

			this.marker=marker;
			this.heart=heart;
			
		}
		
		@Override
		public Drawable getMarker(int stateBitset) {
			Drawable result=(isHeart ? heart : marker);
			
			setState(result, stateBitset);
		
			return(result);
		}
		
		void toggleHeart() {
			isHeart=!isHeart;
		}
	}
}