package cens.ucla.edu.budburst.mapview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.R.string;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.MyLocOverlay;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PBB_map extends MapActivity {
	private SharedPreferences pref;
	private OneTimeDBHelper otDBH = null;
	private static GpsListener gpsListener;
	private LocationManager lm = null;
	private MapView map=null;
	private MyLocOverlay me=null;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		  // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.pbb_map);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.PBBMap_title));
		
		startSignalLevelListener();
		
		// show toast for more information
		Toast.makeText(PBB_map.this, getString(R.string.PBB_MAP_toast), Toast.LENGTH_LONG).show();
		
		gpsListener = new GpsListener();
		
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		// set update the location data in 3secs or 30meters
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 30, gpsListener);
		       
		// check if GPS is turned on...
		if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
		   	
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
		
		// set criteria
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		String provider = lm.getBestProvider(criteria, true);    
		    
		Location loca = lm.getLastKnownLocation(provider);
		if(loca.getLatitude() != 0.0) {
			latitude = loca.getLatitude();
		    longitude = loca.getLongitude();
		}
		
		pref = getSharedPreferences("userinfo",0);
		    
		// TODO Auto-generated method stub
		otDBH = new OneTimeDBHelper(PBB_map.this);
		map=(MapView)findViewById(R.id.map);
		
		Log.i("K", "LAT : " + latitude + " LON : " + longitude);
		
		mc = map.getController();
		me = new MyLocOverlay(PBB_map.this, map);
		
		url = "http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/PBB_social_list.php?lat=" + latitude + "&lon=" + longitude;
	
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
		
		menu.add(0, 1, 0, getString(R.string.PBBMapMenu_myLocation)).setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, 2, 0, getString(R.string.PBBMapMenu_changeView)).setIcon(android.R.drawable.ic_menu_revert);
		menu.add(0, 3, 0, getString(R.string.PBBMapMenu_refresh)).setIcon(android.R.drawable.ic_menu_rotate);
			
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
				
				OneTimeDBHelper onehelper = new OneTimeDBHelper(PBB_map.this);
				onehelper.clearFlickr(PBB_map.this);
				
				db.close();
				onehelper.close();
	
				new DoAsyncTask().execute(url);

				return true;
		}
		return false;
	}

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
	
	// getting information of markers from the server
	class DoAsyncTask extends AsyncTask<String, Integer, Void> {
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
						
						OneTimeDBHelper onehelper = new OneTimeDBHelper(PBB_map.this);
						onehelper.clearPopularLists(PBB_map.this);

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
		    
		    /*
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
			*/

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
			
			if(lm.getLastKnownLocation("gps") == null) {
				current_point = new GeoPoint((int)(0.0), (int)(0.0));
				mc.animateTo(current_point);
				mc.setZoom(1);
			}
			else {
				current_point = new GeoPoint((int)(lm.getLastKnownLocation("gps").getLatitude() * 1000000), (int)(lm.getLastKnownLocation("gps").getLongitude() * 1000000));
				mc.animateTo(current_point);
				mc.setZoom(9);
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
	
	// overlay class
	private class SitesOverlay extends ItemizedOverlay<CustomItem> {
		private List<CustomItem> item=new ArrayList<CustomItem>();
		private	PopupPanel panel=new PopupPanel(R.layout.popup);
	
		public SitesOverlay(Drawable marker) {
			super(null);
			
		    // read data from the table
			SQLiteDatabase db;
			db = otDBH.getReadableDatabase();
			
			String query = null;
			query = "SELECT common_name, science_name, phenophase, dt_taken, lat, lon, distance FROM pbbFlickrLists";
			
			Cursor cursor = db.rawQuery(query, null);
			
		    while(cursor.moveToNext()) {
		    	
		    	String real_info_title = cursor.getString(0) + ";;" + 
		    							cursor.getString(1) + ";;" +
		    							cursor.getString(2) + ";;" +
		    							cursor.getString(3) + ";;" +
		    							cursor.getString(4) + ";;" +
		    							cursor.getString(5) + ";;" +
		    							cursor.getString(6);
		    						
		    	
		    	Log.i("K", "db info : " + real_info_title);
		    	
		    	
		    	item.add(new CustomItem(getPoint(Double.parseDouble(cursor.getString(4)),Double.parseDouble(cursor.getString(5))),
		    			cursor.getString(0), cursor.getString(1) + ";;" + cursor.getString(2) + ";;" + cursor.getString(3), getMarker(R.drawable.full_marker), marker));
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
			image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/plant", null, null));
			
			((TextView)view.findViewById(R.id.title))
			.setText("" + item.getTitle() + "\n" + split_temp[0]);
			((TextView)view.findViewById(R.id.geodata))
			.setText(getString(R.string.PBBMap_phenophase) + split_temp[1]);
			((TextView)view.findViewById(R.id.dt_taken))
			.setText(getString(R.string.PBBMap_date) + split_temp[2]);
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
					//Toast.makeText(PBB_map.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(PBB_map.this, SpeciesDetail_inMapView.class);
					
					/*
					 * we need common_name, latitude, longitude, dt_taken, pheno
					 *
					 * 
					 * intent.putExtra("common_name", value);
					intent.putExtra("latitude", value);
					intent.putExtra("longitude", value);
					intent.putExtra("dt_taken", value);
					intent.putExtra("pheno", value);
					 */
					
					
					
					
					
					//startActivity(intent);
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
				String strLoc = String.format(getString(R.string.PBBMap_currentLocation) + "%10.5f, %10.5f", latitude, longitude);
				
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
