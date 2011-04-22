package cens.ucla.edu.budburst.floracaching;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.FloracacheItem;
import cens.ucla.edu.budburst.helper.HelperGpsHandler;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FloraCacheMidLevel extends Activity {

	private LocationManager mLocManager = null;
	private HelperGpsHandler gpsHandler;
	private boolean mIsBound;
	private boolean mFirstGps;
	private double mLatitude 		= 0.0;
	private double mLongitude 		= 0.0;
	private int mNumSpecies;
	
	private EditText mLatTxt;
	private EditText mLonTxt;
	private EditText mInfoTxt;
	
	private ArrayList<FloracacheItem> mPlantList = new ArrayList<FloracacheItem>();
	
	
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			gpsHandler = ((HelperGpsHandler.GpsBinder) binder).getService();
			//Toast.makeText(PBBMapMain.this, "Connected", Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			gpsHandler = null;
		}
	};
	
	private void doBindService() {
		
		Log.i("K", "BindService");
		
		bindService(new Intent(FloraCacheMidLevel.this, HelperGpsHandler.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
		//Toast.makeText(PBBMapMain.this, "bindService", Toast.LENGTH_SHORT).show();
	
	}
	
	private void doUnbindService() {
		
		Log.i("K", "UnBindService");
		
		if(mIsBound) {
			if(mConnection != null) {
				
			}
			
			//Toast.makeText(PBBMapMain.this, "UnbindService", Toast.LENGTH_SHORT).show();
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle extras = intent.getExtras();
			
			if(extras.getBoolean("signal")) {
				mLatitude = extras.getDouble("latitude");
				mLongitude = extras.getDouble("longitude");
				
			    mLatTxt.setText("Lat: " + mLatitude);
			    mLonTxt.setText("Lon: " + mLongitude);
			    
			    mInfoTxt.setText("- # Floracache plants : " + mNumSpecies + "\n" +
			    		"-  Closest species : " + getClosestSpecies() + "meters away");

			}
			// if Gps signal is bad
			else {
				new AlertDialog.Builder(FloraCacheMidLevel.this)
				.setTitle("Weak Gps Signal")
				.setMessage("Cannot get Gps Signal, Make sure you are in the good connectivity area")
				.setPositiveButton(getString(R.string.Button_back), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				})
				.show();
			}
		}	
	};
	
	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}
	
	private float getClosestSpecies() {
		
		float minDist = Float.MAX_VALUE;
		
		for(int i = 0 ; i < mPlantList.size() ; i++) {
			float distResult[] = new float[1];
			Location.distanceBetween(mLatitude, mLongitude, mPlantList.get(i).getLatitude(), mPlantList.get(i).getLongitude(), distResult);
			
			if(distResult[0] < minDist) {
				minDist = distResult[0];
			}
		}
		
		return minDist;
	}
	
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setTitleBar();
	    getFloraLists();
	    initializeLayout();
	    getMyListFromServer();
	    checkGPS();
	    // TODO Auto-generated method stub
	}
	
	private void getFloraLists() {
		HelperSharedPreference hPref = new HelperSharedPreference(this);
		if(!hPref.getPreferenceBoolean("floracache")) {
			Toast.makeText(FloraCacheMidLevel.this, "To download the list, Go 'Settings' page", Toast.LENGTH_SHORT).show();
		}
		else {
			OneTimeDBHelper oDBH = new OneTimeDBHelper(this);
			mPlantList = oDBH.getFloracacheLists(FloraCacheMidLevel.this, HelperValues.FLORACACHE_MID);
		}
	}
	
	private void initializeLayout() {
		mLatTxt = (EditText)findViewById(R.id.latitude);
		mLonTxt = (EditText)findViewById(R.id.longitude);
		mInfoTxt = (EditText)findViewById(R.id.num_species_info);
	}
	
	private void checkGPS() {
		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		Location lastLoc = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(lastLoc != null) {
			mLatitude = lastLoc.getLatitude();
			mLongitude = lastLoc.getLongitude();			
		}
		
		IntentFilter inFilter = new IntentFilter(HelperGpsHandler.GPSHANDLERFILTER);
		registerReceiver(gpsReceiver, inFilter);
		
		doBindService();
	}
	
	
	public void setTitleBar() {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.floracachemidlevel);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.Observation_Summary));
	}
	
	private void getMyListFromServer() {
		HelperSharedPreference hPref = new HelperSharedPreference(this);
		if(!hPref.getPreferenceBoolean("floracache")) {
			Toast.makeText(FloraCacheMidLevel.this, "To download the list, Go 'Settings' page", Toast.LENGTH_SHORT).show();
		}
		else {
			OneTimeDBHelper oDBH = new OneTimeDBHelper(this);
			mPlantList = oDBH.getFloracacheLists(FloraCacheMidLevel.this, HelperValues.FLORACACHE_MID);
			mNumSpecies = mPlantList.size();
		}
	}
	
	@Override
	public void onDestroy() {
		// when user finish this activity, turn off the gps
		// if there's a overlay, should call disableCompass() explicitly
		doUnbindService();
		if(gpsReceiver != null) {
			unregisterReceiver(gpsReceiver);
		}
		super.onDestroy();
	}
}
