package cens.ucla.edu.budburst.mapview;

import cens.ucla.edu.budburst.Help;
import cens.ucla.edu.budburst.Login;
import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.Sync;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.MyLocOverlay;
import cens.ucla.edu.budburst.helper.Values;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MyLocation extends MapActivity {

	private SharedPreferences pref;
	private static GpsListener gpsListener;
	private LocationManager locManager = null;
	private MapView mMapView = null;
	private MapController mapCon = null;
	private MyLocOverlay mOver = null;
	private SitesOverlay sOverlay = null;
	private double latitude = 0.0;
	private double longitude = 0.0;
	private float accuracy = 0;
	private TextView mylocInfo;
	private boolean first_myLoc = true;
	private boolean satelliteView = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.pbb_map);
	    
	    mMapView = (MapView)findViewById(R.id.map);
	    mMapView.setBuiltInZoomControls(true);
	    
	    mylocInfo = (TextView) findViewById(R.id.myloc_accuracy);
	    
	    mapCon = mMapView.getController();
	    mapCon.setZoom(19);
	    
	   
	    /*
	     * Add Mylocation Overlay
	     */
	    mOver = new MyLocOverlay(MyLocation.this, mMapView);
	    mOver.enableMyLocation();
	    mMapView.getOverlays().add(mOver);
	    
	    /*
	     * Add ItemizedOverlay Overlay
	     */
	    Drawable marker = getResources().getDrawable(R.drawable.marker);
	    marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
	    sOverlay = new SitesOverlay(MyLocation.this, marker);	    
	    mMapView.getOverlays().add(sOverlay);
	    
	   	
	    mMapView.setSatellite(false);
	    mMapView.invalidate();
	    
	    pref = getSharedPreferences("userinfo",0);
	    latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
	    longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
	    
	    GeoPoint geoP = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));
	    mapCon.animateTo(geoP);
	   
	    gpsListener = new GpsListener();
	    locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
	     
	    // TODO Auto-generated method stub
	}
	
	private class GpsListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {
			if(loc != null) {
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				accuracy = loc.getAccuracy();
				
				GeoPoint geoPoint = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));
				
				mylocInfo.setText("Accuracy : " + accuracy + "\u00b1m");
				
				if(first_myLoc) {
					mapCon.animateTo(geoPoint);
					first_myLoc = false;
				}
				
				mOver.onLocationChanged(loc);
				
			}
			// TODO Auto-generated method stub
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

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		locManager.removeUpdates(gpsListener);
		mOver.disableMyLocation();
	}
	
	// or when user press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			
			new AlertDialog.Builder(MyLocation.this)
	   		.setTitle(getString(R.string.Message_Save_GPS))
	   		.setPositiveButton(getString(R.string.Button_GPS), new DialogInterface.OnClickListener() {
	   			public void onClick(DialogInterface dialog, int whichButton) {
	   				pref = getSharedPreferences("userinfo", 0);
	   				SharedPreferences.Editor edit = pref.edit();
	   				edit.putBoolean("new", true);
	   				edit.putBoolean("highly", true);
	   				edit.putString("latitude", Double.toString(latitude));
	   				edit.putString("longitude", Double.toString(longitude));
	   				edit.putString("accuracy", Float.toHexString(accuracy));
	   				edit.commit();
	   				
	   				finish();
	   			}
	   		})
	   		.setNegativeButton(getString(R.string.Button_Cancel), new DialogInterface.OnClickListener() {
	   			public void onClick(DialogInterface dialog, int whichButton) {
	   				locManager.removeUpdates(gpsListener);
	   				mOver.disableMyLocation();		
	   				finish();
	   			}
	   		})
	   		.setNeutralButton(getString(R.string.Button_Marker), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					pref = getSharedPreferences("userinfo", 0);
	   				SharedPreferences.Editor edit = pref.edit();
	   				edit.putBoolean("new", true);
	   				edit.putBoolean("highly", true);
	   				edit.putString("latitude", Double.toString(sOverlay.getLatitude()));
	   				edit.putString("longitude", Double.toString(sOverlay.getLongitude()));
	   				edit.putString("accuracy", Float.toHexString(accuracy));
	   				edit.commit();
	   				
	   				finish();

				}
			})
	   		.show();			
		}
		return false;
	}
	
	
		/*
	 * Menu option(non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, getString(R.string.Menu_help)).setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 2, 0, getString(R.string.Menu_Satellite)).setIcon(android.R.drawable.ic_menu_mapmode);
			
		return true;
	}
	
	/*
	 * Menu option selection handling(non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){

		switch(item.getItemId()){
			case 1:
				Toast.makeText(MyLocation.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				return true;
			case 2:
				if(!satelliteView) {
					mMapView.setSatellite(true);
					satelliteView = true;
				}
				else {
					mMapView.setSatellite(false);
					satelliteView = false;
				}
				return true;
		}
		return false;
	}

}
