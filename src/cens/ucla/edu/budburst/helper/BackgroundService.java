package cens.ucla.edu.budburst.helper;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.Splash;
import cens.ucla.edu.budburst.firstActivity;
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
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service{

	private NotificationManager notificationMgr;
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
	
	private Timer timer;
	private boolean gpsEnabled = false;
	private boolean networkEnabled = false;
	private int SIMPLE_NOTFICATION_ID = 1234567890;

	private Handler mHandler = new Handler();
	
	private boolean stopListDownload = false;
	/*
	 * Network Listener
	 */
	private LocationListener networkListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
			
			if(mQuit) {
				lm.removeUpdates(this);
				stopSelf();
			}
			if(loc != null) {
				/*
				 * Stop Timer and start downloading data
				 */
				timer.cancel();
				downloadingDataFromServer(loc);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	};
	
	/*
	 * GPS Listener
	 */
	private LocationListener gpsListener = new LocationListener() {
		
		@Override
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
			if(mQuit) {
				lm.removeUpdates(this);
				stopSelf();
			}
			if(loc != null) {
				/*
				 * Stop Timer and start downloading data
				 */
				timer.cancel();
				downloadingDataFromServer(loc);
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
		}	
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i("K", "********************Start Lists as Background Service***********************");
		
		mContext = this;
		
		notificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		mQuit = false;
		pref = getSharedPreferences("userinfo", 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putBoolean("new", false);
		edit.putBoolean("highly", false);
		edit.putString("latitude","0.0");
		edit.putString("longitude","0.0");
		edit.putString("accuracy", "0");
		edit.commit();
		
	    lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    
	    try {
	    	gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    }
	    catch(Exception ex) {}
	    
	    try {
	    	networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	    }	    
	    catch(Exception ex) {}
	    
	    /*
	     * Set criteria
	     */
	    /*
	    Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    criteria.setAltitudeRequired(false);
	    criteria.setBearingRequired(false);
	    criteria.setCostAllowed(true);
	    criteria.setPowerRequirement(Criteria.POWER_LOW);
	    
	    String provider = lm.getBestProvider(criteria, true);
	    */
	    
	    int minTimeBetweenUpdatesms = 1000;
		int minDistanceBetweenUpdatesMeters = 0;
	    
	    if(gpsEnabled) {
	    	Log.i("K", "GPS enabled");
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeBetweenUpdatesms, minDistanceBetweenUpdatesMeters, gpsListener);
	    }
	    if(networkEnabled) {
	    	Log.i("K", "Network enabled");
	    	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeBetweenUpdatesms, minDistanceBetweenUpdatesMeters, networkListener);
	    }
		
	    //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeBetweenUpdatesms, minDistanceBetweenUpdatesMeters, gpsListener);

		timer = new Timer();
		timer.schedule(new GetLastLocation(), 30000);
	}
	
	class GetLastLocation extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			mHandler.post(
					new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Log.i("K", "Time elapse.");
							Log.i("K", "gpsListener : " + gpsListener);
							Log.i("K", "networkListener : " + networkListener);
							
							lm.removeUpdates(gpsListener);
							lm.removeUpdates(networkListener);
							
							Location networkLoc = null;
							Location gpsLoc = null;
							
							if(gpsEnabled) {
								gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							}
							if(networkEnabled) {
								networkLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
							}
							
							if(gpsLoc != null && networkLoc != null) {
								if(gpsLoc.getTime() > networkLoc.getTime()) {
									downloadingDataFromServer(gpsLoc);
								}
								else {
									downloadingDataFromServer(networkLoc);
								}
							}
							
							if(gpsLoc != null) {
								downloadingDataFromServer(gpsLoc);
							}
							if(networkLoc != null) {
								downloadingDataFromServer(networkLoc);
							}
						}
						
					});
		}
	}

	private void displayNotificationMessage(String message) {
		Notification notification = new Notification(R.drawable.stat_sys_download_done, message, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, null, PendingIntent.FLAG_CANCEL_CURRENT);
		
		notification.setLatestEventInfo(this, "Project Budburst", message, contentIntent);
		
		notificationMgr.notify(SIMPLE_NOTFICATION_ID, notification);
	}
	
	@Override
	public void onDestroy() {
		Log.i("K", "Destory background service");
		if(lm != null) {
			lm.removeUpdates(gpsListener);
			lm.removeUpdates(networkListener);
			lm = null;
		}
		
		super.onDestroy();
		mQuit = true;
		timer.cancel();
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
	
	
	public void downloadingDataFromServer(Location loc) {
		
		/*
		 * Getting local species lists from the server
		 * This is called only once when the app firstly receives GPS data.
		 * 
		 */
		
		/*
		 * If all boolean values are true, (meaning all lists are downloaded..)
		 * show the notification message and stop the service.
		 */
		if((pref.getBoolean("localbudburst", false))
				&&(pref.getBoolean("localwhatsinvasive", false))) {
				//&&(pref.getBoolean("localnative", false))
				//&&(pref.getBoolean("localpoisonous", false))
				//&&(pref.getBoolean("getTreeLists", false))) {
			
					mQuit = true;
				
					displayNotificationMessage("Successfully download local plant lists");
					
					/*
					 * The list download process should be done only once.
					 * So, we are using preference value to guarantee this process happens only once.
					 * 
					 */

					SharedPreferences.Editor edit = pref.edit();
					edit.putBoolean("listDownloaded", true);
					edit.commit();
										
					stopSelf();
		}
		
		if(!stopListDownload) {
			/*
			 * Get USDA Plant Lists
			 * 
			 */
			Items item = new Items(mContext, loc.getLatitude(), loc.getLongitude(), Values.BUDBURST_LIST);
			new DownloadListByService().execute(item);
			
			item = new Items(mContext, loc.getLatitude(), loc.getLongitude(), Values.WHATSINVASIVE_LIST);
			new DownloadListByService().execute(item);
			
			
			
			
			stopListDownload = true;
		}
		
		
		
		latitude = loc.getLatitude();
		longitude = loc.getLongitude();
		accuracy = loc.getAccuracy();
		
		Date date = new Date();
		
		Log.i("K", "Date : " + date.getTime() + "=> lat : " + latitude.toString() + " lng : " + longitude.toString() + " accuracy : " + loc.getAccuracy());
		pref = getSharedPreferences("userinfo", 0);
		SharedPreferences.Editor edit = pref.edit();
		
		/*
		 *  If the accuracy is less than 20 meters,
		 *  turn off the GPS.
		 */
		
		if(loc.getAccuracy() <= 20) {
			edit.putBoolean("new", true);
			edit.putBoolean("highly", true);
			edit.putString("latitude",latitude.toString());
			edit.putString("longitude",longitude.toString());
			edit.putString("accuracy", Float.toHexString(accuracy));
			edit.commit();
			
			//lm.removeUpdates(gpsListener);
			//lm.removeUpdates(networkListener);
			
			Log.i("K","Receive GPS within 20 meters. Turned off GPS.");
			
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
