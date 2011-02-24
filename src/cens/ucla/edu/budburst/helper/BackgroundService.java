package cens.ucla.edu.budburst.helper;

import java.util.Date;

import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.lists.DownloadListByService;
import cens.ucla.edu.budburst.lists.GetUserPlantLists;
import cens.ucla.edu.budburst.lists.Items;
import cens.ucla.edu.budburst.mapview.PBB_map;
import android.R;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service{

	private NotificationManager notificationMgr;
	private static GpsListener gpsListener;
	private LocationManager lm	= null;
	private Double latitude = 0.0;
	private Double longitude = 0.0;
	private float accuracy = 0;
	private Intent background_intent;
	private SharedPreferences pref;
	private Criteria criteria;
	private String provider;
	private boolean mQuit;
	private Context mContext;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i("K", "********************Start Lists as Background Service***********************");
		
		mContext = this;
		
		mQuit = false;
		pref = getSharedPreferences("userinfo", 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putBoolean("new", false);
		edit.putBoolean("highly", false);
		edit.putString("latitude","0.0");
		edit.putString("longitude","0.0");
		edit.putString("accuracy", "0");
		edit.commit();
		
		gpsListener = new GpsListener();
		
	    lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    
	    /*
	     * Set criteria
	     */
	    Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    criteria.setAltitudeRequired(false);
	    criteria.setBearingRequired(false);
	    criteria.setCostAllowed(true);
	    criteria.setPowerRequirement(Criteria.POWER_LOW);
	    
	    String provider = lm.getBestProvider(criteria, true);
	    
		int minTimeBetweenUpdatesms = 1000;
		int minDistanceBetweenUpdatesMeters = 0;
	    //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeBetweenUpdatesms, minDistanceBetweenUpdatesMeters, gpsListener);
		lm.requestLocationUpdates(provider, minTimeBetweenUpdatesms, minDistanceBetweenUpdatesMeters, gpsListener);		
	}
	
	@Override
	public void onDestroy() {
		if(lm != null) {
			lm.removeUpdates(gpsListener);
			lm = null;
		}
		
		super.onDestroy();
		mQuit = true;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i("K", "onStart");
		mQuit = false;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	class GpsListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
			if(mQuit) {
				lm.removeUpdates(gpsListener);
				stopSelf();
			}
			if(loc != null) {
				
				/*
				 * Getting local species lists from the server
				 * This is called only once when the app firstly receives GPS data.
				 * 
				 */
				
				if(!pref.getBoolean("listDownloaded", false)) {
					/*
					 * Get USDA Plant Lists
					 * 
					 */
					Items item = new Items(mContext, loc.getLatitude(), loc.getLongitude(), Values.BUDBURST_LIST);
					new DownloadListByService().execute(item);
					
					item = new Items(mContext, loc.getLatitude(), loc.getLongitude(), Values.WHATSINVASIVE_LIST);
					new DownloadListByService().execute(item);
					
					/*
					 * Get User Plant Tree Lists - UCLA
					 * 
					 */
					new GetUserPlantLists().execute(mContext);
					
					/*
					 * The list download process should be done only once.
					 * So, we are using preference value to guarantee this process happens only once.
					 * 
					 */
					SharedPreferences.Editor edit = pref.edit();
					edit.putBoolean("listDownloaded", true);
					edit.commit();
				}
				
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				accuracy = loc.getAccuracy();
				
				Date date = new Date();
				
				Log.i("K", "Date : " + date.getTime() + "=> lat : " + latitude.toString() + " lng : " + longitude.toString() + " accuracy : " + loc.getAccuracy());
				pref = getSharedPreferences("userinfo", 0);
				SharedPreferences.Editor edit = pref.edit();
				
				/*
				 * 
				 *  If the accuracy is less than 20 meters,
				 *  turn off the GPS.
				 *  
				 */
				
				if(loc.getAccuracy() <= 20) {
					edit.putBoolean("new", true);
					edit.putBoolean("highly", true);
					edit.putString("latitude",latitude.toString());
					edit.putString("longitude",longitude.toString());
					edit.putString("accuracy", Float.toHexString(accuracy));
					edit.commit();
					//sendBroadcast(background_intent);
					lm.removeUpdates(gpsListener);
					Log.i("K","GPS turned off");
					stopSelf();
				}
				else {
					edit.putBoolean("new", true);
					edit.putBoolean("highly", false);
					edit.putString("latitude",latitude.toString());
					edit.putString("longitude",longitude.toString());
					edit.putString("accuracy", Float.toHexString(accuracy));
					edit.commit();
				}
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
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
			switch(status) {
			case LocationProvider.OUT_OF_SERVICE:
				Log.v("K", "Status Changed: Out of Service");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.v("K", "Status Changed: Temporarily Unavailable");
				break;
			case LocationProvider.AVAILABLE:
				Log.v("K", "Status Changed: Available");
				break;
			}
		}	
	}
}
