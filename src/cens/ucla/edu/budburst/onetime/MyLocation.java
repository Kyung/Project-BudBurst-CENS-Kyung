package cens.ucla.edu.budburst.onetime;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.MyLocOverlay;
import cens.ucla.edu.budburst.mapview.PBB_map;

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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class MyLocation extends MapActivity {

	private SharedPreferences pref;
	private static GpsListener gpsListener;
	private LocationManager locManager = null;
	private MapView map = null;
	private MapController mapCon = null;
	private MyLocOverlay mOver = null;
	private double latitude = 0.0;
	private double longitude = 0.0;
	private float accuracy = 0;
	private TextView mylocInfo;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.pbb_map);
	    
	    map = (MapView)findViewById(R.id.map);
	    map.setBuiltInZoomControls(true);
	    
	    mylocInfo = (TextView) findViewById(R.id.myloc_accuracy);
	    
	    mapCon = map.getController();
	    mapCon.setZoom(19);
	    
	    mOver = new MyLocOverlay(MyLocation.this, map);
	    mOver.enableMyLocation();
	    map.getOverlays().add(mOver);
	   	
	    map.setSatellite(false);
	    map.invalidate();
	    
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
				
				mylocInfo.setText("Accuracy : " + accuracy + "\u00b1m");
				mapCon.animateTo(new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000)));
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
	   		.setTitle("Save GPS")
	   		.setMessage(getString(R.string.Message_Save_GPS))
	   		.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
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
	   		.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
	   			public void onClick(DialogInterface dialog, int whichButton) {
	   				locManager.removeUpdates(gpsListener);
	   				mOver.disableMyLocation();		
	   				finish();
	   			}
	   		})
	   		.show();			
		}
		return false;
	}
}
