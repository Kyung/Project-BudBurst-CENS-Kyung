package cens.ucla.edu.budburst.helper;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.PBBSplash;
import cens.ucla.edu.budburst.firstActivity;
import cens.ucla.edu.budburst.lists.ListLocalDownload;
import cens.ucla.edu.budburst.lists.ListUserDefinedCategory;
import cens.ucla.edu.budburst.lists.ListUserDefinedSpeciesDownload;
import cens.ucla.edu.budburst.lists.ListItems;
import cens.ucla.edu.budburst.mapview.MapViewMain;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class HelperBackgroundService extends Service{

	private NotificationManager mNotiManager;
	private LocationManager mLocManager	= null;
	private Double mLatitude = 0.0;
	private Double mLongitude = 0.0;
	private float mAccuracy = 0;
	private HelperSharedPreference mPref;
	private Context mContext;
	private Timer mTimer;
	private boolean mGpsEnabled = false;
	private boolean mNetworkEnabled = false;
	private int SIMPLE_NOTFICATION_ID = 1234567890;
	private Handler mHandler = new Handler();
	private Handler mTimerHandler = new Handler();
	
	private long mStartTime;
	
	private boolean mQuit = false;
	private boolean mStopDownload = false;
	/*
	 * Network Listener
	 */
	private LocationListener mNetworkListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
			
			if(mQuit) {
				mLocManager.removeUpdates(this);
				//stopSelf();
			}
			if(loc != null) {
				/*
				 * Stop Timer and start downloading data
				 */
				mTimer.cancel();
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
	private LocationListener mGpsListener = new LocationListener() {
		
		@Override
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
			if(mQuit) {
				mLocManager.removeUpdates(this);
				//stopSelf();
			}
			if(loc != null) {
				/*
				 * Stop Timer and start downloading data
				 */
				mTimer.cancel();
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
	
	private Runnable mUpdateTimeTask = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			//final long start = mStartTime;
			
			
			final long start = mStartTime;
		       
			long millis = SystemClock.uptimeMillis() - start;
		    int seconds = (int) (millis / 1000);
		       
		    int minutes = seconds / 60;
		       
		    seconds     = seconds % 60;

		    Log.i("K", "" + minutes + ":0" + seconds);

		    new checkVersion().execute();
		    
		    mTimerHandler.postAtTime(this, start + (((minutes * 60) + seconds + 1) * 1000));
		}
		
	};
	
	private class checkVersion extends AsyncTask <Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... Void) {
			// TODO Auto-generated method stub
			String getResponse = getRequest("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/checkUpdates.php?version=" 
		    		+ mPref.getPreferenceString("version", "1.5.0"));			
			
			if(getResponse.toString().equals("NEEDUPDATES")) {
				mPref.setPreferencesBoolean("needUpdate", true);
				Log.i("K", "Needs updates");
			}
			else {
				mPref.setPreferencesBoolean("needUpdate", false);
			}
			return null;
		}
	}
	
	private String getRequest(String getUrl) {
		
		String getResponse = "";
		
		Log.i("K", "getUrl : " + getUrl);
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet getHttp = new HttpGet(getUrl);
			HttpResponse responseGet = client.execute(getHttp);
			HttpEntity resEntityGet = responseGet.getEntity();
			
			if(resEntityGet != null) {
				getResponse = EntityUtils.toString(resEntityGet);
			}
		}
		catch(Exception e) {
			Log.i("K", "Exception in HttpRequest");
		}
		
		Log.i("K", "getResponse: " + getResponse);
		
		return getResponse;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i("K", "********************Start Lists as Background Service***********************");
		
		mContext = this;
		
		mNotiManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		mQuit = false;
		
		mPref = new HelperSharedPreference(mContext);		
		mPref.setPreferencesString("latitude", "0.0");
		mPref.setPreferencesString("longitude", "0.0");
		mPref.setPreferencesString("accuracy", "0");
		
		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    
	    try {
	    	mGpsEnabled = mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    }
	    catch(Exception ex) {}
	    
	    try {
	    	mNetworkEnabled = mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	    }	    
	    catch(Exception ex) {}
	    
	    int minTimeBetweenUpdatesms = 3*1000;
		int minDistanceBetweenUpdatesMeters = 5;
	    
	    if(mGpsEnabled) {
	    	Log.i("K", "GPS enabled");
	    	mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeBetweenUpdatesms, minDistanceBetweenUpdatesMeters, mGpsListener);
	    }
	    if(mNetworkEnabled) {
	    	Log.i("K", "Network enabled");
	    	mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeBetweenUpdatesms, minDistanceBetweenUpdatesMeters, mNetworkListener);
	    }

	    mTimer = new Timer();
		mTimer.schedule(new GetLastLocation(), 30 * 1000);
		
		
		// reference : http://developer.android.com/resources/articles/timed-ui-updates.html
		if(mStartTime == 0L) {
			mStartTime = System.currentTimeMillis();
			mTimerHandler.removeCallbacks(mUpdateTimeTask);
			mTimerHandler.postDelayed(mUpdateTimeTask, 5 * 1000);
		}
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
							Log.i("K", "gpsListener : " + mGpsListener);
							Log.i("K", "networkListener : " + mNetworkListener);
							
							mLocManager.removeUpdates(mGpsListener);
							mLocManager.removeUpdates(mNetworkListener);
							
							Location networkLoc = null;
							Location gpsLoc = null;
							
							if(mGpsEnabled) {
								gpsLoc = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							}
							if(mNetworkEnabled) {
								networkLoc = mLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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
		mNotiManager.notify(SIMPLE_NOTFICATION_ID, notification);
	}
	
	@Override
	public void onDestroy() {
		Log.i("K", "Destory background service");
		if(mLocManager != null) {
			mLocManager.removeUpdates(mGpsListener);
			mLocManager.removeUpdates(mNetworkListener);
			mLocManager = null;
		}
		
		mTimerHandler.removeCallbacks(mUpdateTimeTask);
		
		super.onDestroy();
		mQuit = true;
		mTimer.cancel();
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

		mLatitude = loc.getLatitude();
		mLongitude = loc.getLongitude();
		mAccuracy = loc.getAccuracy();
		
		//ListItems item = new ListItems(loc.getLatitude(), loc.getLongitude());
		
		//ListUserDefinedCategory listGroup = new ListUserDefinedCategory(mContext);
		//listGroup.execute(item);
		
		/*
		 * When the app receives my location, it will send my location information to the server.
		 * When the server gets my location info, the server will be getting a bunch of species information related to my location.
		 */
		HelperAnnounceMyLocation announceLoc = new HelperAnnounceMyLocation(mLatitude,
				mLongitude);
		announceLoc.execute();
		
		Date date = new Date();
		Log.i("K", "Date : " + date.getTime() + "=> lat : " 
				+ mLatitude.toString() + " lng : " 
				+ mLongitude.toString() + " accuracy : " + loc.getAccuracy());
		
		mPref.setPreferencesString("latitude", mLatitude.toString());
		mPref.setPreferencesString("longitude", mLongitude.toString());
		mPref.setPreferencesString("accuracy", Float.toHexString(mAccuracy));
		
		mQuit = true;
	}
}
