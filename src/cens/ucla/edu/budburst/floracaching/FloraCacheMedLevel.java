package cens.ucla.edu.budburst.floracaching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.android.maps.GeoPoint;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapterFloracache;
import cens.ucla.edu.budburst.adapter.MyListAdapterMainPage;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.HelperGpsHandler;
import cens.ucla.edu.budburst.helper.HelperListItem;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.mapview.CompassView;
import cens.ucla.edu.budburst.onetime.OneTimePhenophase;
import cens.ucla.edu.budburst.utils.PBBItems;
import cens.ucla.edu.budburst.utils.QuickCapture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FloraCacheMedLevel extends ListActivity {

	private LocationManager mLocManager = null;
	private HelperGpsHandler gpsHandler;
	private boolean mIsBound;
	private double mLatitude 		= 0.0;
	private double mLongitude 		= 0.0;
	private int mNumSpecies;
	private int mGroupID;
	private int mIndex;
	private int mImageID;
	
	private ListView mListView;
	
	private EditText mLatTxt;
	private EditText mLonTxt;
	private EditText mInfoTxt;
	private TextView mDirTxt;
	
	private Button mRefreshListBtn;
	//private Button mRefreshBtn2;
	
	private static int MAX_NUM_SHOWN = 20;
	
	private MyListAdapterFloracache mListapdater;
	private ArrayList<FloracacheItem> mPlantList = new ArrayList<FloracacheItem>();
	private ArrayList<HelperPlantItem> mListArr;
	private HelperListItem mItem;
	
	private boolean mFirstTime = true;
	
	private static SensorManager mySensorManager;
	private boolean sersorrunning;
	private CompassView myCompassView;
	
	private float mSensorValue = 0;
	
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
		
		bindService(new Intent(FloraCacheMedLevel.this, HelperGpsHandler.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}
	
	private void doUnbindService() {
		
		Log.i("K", "UnBindService");
		
		if(mIsBound) {
			if(mConnection != null) {
				
			}

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
				// show the compass!
				
				mRefreshListBtn.setEnabled(true);
				mLatitude = extras.getDouble("latitude");
				mLongitude = extras.getDouble("longitude");

				TextView infoTxt = (TextView)findViewById(R.id.gps_info);
				infoTxt.setVisibility(View.GONE);
				
				getFloraLists();
				doUnbindService();

			}
			// if Gps signal is bad
			else {
				Toast.makeText(FloraCacheMedLevel.this, getString(R.string.Low_GPS_Signal), Toast.LENGTH_SHORT).show();
			}
		}	
	};

	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Intent gIntent = getIntent();
	    mGroupID = gIntent.getExtras().getInt("group_id");	    
	    mListArr = new ArrayList<HelperPlantItem>();
	    
	    setTitleBar();
	    checkGPS();
	    
	    mRefreshListBtn = (Button)findViewById(R.id.refresh_lists);
	    mRefreshListBtn.setEnabled(false);
	    mRefreshListBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mLatitude != 0.0 && mLongitude != 0.0) {
					getFloraLists();
				}
				else {
					Toast.makeText(FloraCacheMedLevel.this, getString(R.string.Not_Finish_GPS), Toast.LENGTH_SHORT).show();
				}
				// TODO Auto-generated method stub
				
			}
		});
	    
	    // TODO Auto-generated method stub
	}
    
    private String getDirectionStr(float trueBearing) {
		String direction = "";
        
		if((trueBearing > 337.5 && trueBearing <= 360) || (trueBearing >= 0 && trueBearing < 22.5))
        	direction = "N";
        if(trueBearing >= 22.5 && trueBearing < 67.5) 
        	direction ="NE";
        if(trueBearing >= 67.5 && trueBearing < 112.5) 
        	direction = "E";
        if(trueBearing >= 112.5 && trueBearing < 157.5) 
        	direction = "SE";
        if(trueBearing >= 157.5 && trueBearing < 202.5) 
        	direction = "S";
        if(trueBearing >= 202.5 && trueBearing < 247.5) 
        	direction = "SW";
        if(trueBearing >= 247.5 && trueBearing < 292.5) 
        	direction = "W";
        if(trueBearing >= 292.5 && trueBearing < 337.5) 
        	direction = "NW";
        
        Log.i("K", "Direction : " + direction);
        
        return direction;
    }

	private void getFloraLists() {
		HelperSharedPreference hPref = new HelperSharedPreference(this);
		if(!hPref.getPreferenceBoolean("floracache")) {
			Toast.makeText(FloraCacheMedLevel.this, "To download the list, Go 'Settings' page", Toast.LENGTH_SHORT).show();
		}
		else {
			OneTimeDBHelper oDBH = new OneTimeDBHelper(this);
			mPlantList = oDBH.getFloracacheLists(FloraCacheMedLevel.this, HelperValues.FLORACACHE_MID, mGroupID, mLatitude, mLongitude);
			
			for(int i = 0 ; i < mPlantList.size() ; i++) {
				mPlantList.get(i).setFloracacheNotes(String.format("%4.0fft / ", mPlantList.get(i).getDistance() * 3.2808399) 
						+ "Direction : "  
						+ getDirectionStr(bearingTo(mLatitude, mLongitude, mPlantList.get(i).getLatitude(), mPlantList.get(i).getLongitude())));
			}
			
			mListapdater = new MyListAdapterFloracache(this, R.layout.floracache_item ,mPlantList);
			mListView = getListView();
			mListView.setAdapter(mListapdater);	
		}
	}
	
	private int bearingTo(double latitude1, double longitude1, double latitude2, double longitude2) {
		
		float azimuth = mSensorValue;

		Location currentLoc = new Location("");
		currentLoc.setLatitude(latitude1);
		currentLoc.setLongitude(longitude1);
		
		Location targetLoc = new Location("");
		targetLoc.setLatitude(latitude2);
		targetLoc.setLongitude(longitude2);
		
		azimuth = azimuth * 360 / (2 * (float) Math.PI);
		
		GeomagneticField geoField = new GeomagneticField(Double.valueOf(currentLoc.getLatitude()).floatValue(),
														Double.valueOf(currentLoc.getLongitude()).floatValue(),
														Double.valueOf(currentLoc.getAltitude()).floatValue(),
														System.currentTimeMillis());
		
		azimuth += geoField.getDeclination();
		float bearing = currentLoc.bearingTo(targetLoc);
		
		float direction = azimuth - bearing;
		
		if(bearing < 0) {
        	return (int)(bearing + 360.0f);
        }
        
        return (int)bearing;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		float dist = mPlantList.get(position).getDistance();
		
		mIndex = position;
		
		if(mPlantList.get(mIndex).getUserSpeciesCategoryID() == HelperValues.LOCAL_WHATSINVASIVE_LIST 
				|| mPlantList.get(mIndex).getUserSpeciesCategoryID() == HelperValues.LOCAL_POISONOUS_LIST
				|| mPlantList.get(mIndex).getUserSpeciesCategoryID() == HelperValues.LOCAL_THREATENED_ENDANGERED_LIST) {
			OneTimeDBHelper oDBH = new OneTimeDBHelper(FloraCacheMedLevel.this);
			mImageID = oDBH.getImageID(FloraCacheMedLevel.this, mPlantList.get(mIndex).getScienceName(), mPlantList.get(mIndex).getUserSpeciesCategoryID());
		}
		
		Intent intent = new Intent(FloraCacheMedLevel.this, FloraCacheMedLevelContent.class);
		PBBItems pbbItem = new PBBItems();
		pbbItem.setCommonName(mPlantList.get(mIndex).getCommonName());
		pbbItem.setScienceName(mPlantList.get(mIndex).getScienceName());
		pbbItem.setSpeciesID(mPlantList.get(mIndex).getUserSpeciesID());
		pbbItem.setProtocolID(mPlantList.get(mIndex).getProtocolID());
		pbbItem.setCategory(mPlantList.get(mIndex).getUserSpeciesCategoryID());
		pbbItem.setFloracacheID(mPlantList.get(mIndex).getFloracacheID());
		pbbItem.setLatitude(mPlantList.get(mIndex).getLatitude());
		pbbItem.setLongitude(mPlantList.get(mIndex).getLongitude());
		pbbItem.setIsFloracache(HelperValues.IS_FLORACACHE_YES);
		pbbItem.setSpeciesImageID(mImageID);
		pbbItem.setImageURL(getString(R.string.get_floracache_species_image) + mPlantList.get(mIndex).getImageID());
		
		intent.putExtra("pbbItem", pbbItem);
		intent.putExtra("image_id", mImageID);
		startActivity(intent);
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
		myTitleText.setText(" " + getString(R.string.Floracache_Game) + " Medium level");
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
