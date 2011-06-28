package cens.ucla.edu.budburst.floracaching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapterFloracache;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.HelperDrawableManager;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperGpsHandler;
import cens.ucla.edu.budburst.helper.HelperListItem;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.mapview.CompassView;
import cens.ucla.edu.budburst.utils.PBBItems;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FloraCacheMedLevelContent extends Activity {

	private LocationManager mLocManager = null;
	private HelperGpsHandler gpsHandler;
	private boolean mIsBound;
	private double mLatitude 		= 0.0;
	private double mLongitude 		= 0.0;
	private double mTargetLatitude;
	private double mTargetLongitude;
	private int mImageID;
	
	private ListView mListView;
	private TextView mDistanceTxt;
	private TextView mDirectionTxt;
	private TextView mDegreeTxt;
	private LinearLayout mLayout;
	private PBBItems pbbItem;
	private float mDistance;
	private TextView speciesName;
	private TextView distance;
	private ImageView speciesImageView;
	
	private Button mMakeOB;
	
	private static SensorManager mySensorManager;
	private boolean mSersorRunning;
	private CompassView mCompassView;
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
		
		bindService(new Intent(FloraCacheMedLevelContent.this, HelperGpsHandler.class), mConnection,
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
				mMakeOB.setEnabled(true);
				
				mLatitude = extras.getDouble("latitude");
				mLongitude = extras.getDouble("longitude");

				float dist[] = new float[1];
				Location.distanceBetween(mLatitude, mLongitude, mTargetLatitude, mTargetLongitude, dist);
				
				mDistance = dist[0];
				mDistanceTxt.setText(String.format("%-20s","+ Distance to plant: ") + String.format("%5dft", (int)(mDistance * 3)));
				mDirectionTxt.setText(String.format("%-20s","+ Direction to plant: ") + getDirectionStr(bearingTo(mLatitude, mLongitude, mTargetLatitude,  mTargetLongitude))
						+ " (" + bearingTo(mLatitude, mLongitude, mTargetLatitude,  mTargetLongitude) + "\u00B0" + ")");
				
				
			}
			// if Gps signal is bad
			else {
				//Toast.makeText(FloraCacheMedLevelContent.this, getString(R.string.Low_GPS_Signal), Toast.LENGTH_SHORT).show();
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
	    
	    Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		
		mTargetLatitude = pbbItem.getLatitude();
		mTargetLongitude = pbbItem.getLongitude();
		
		setTitleBar();
		
		speciesName = (TextView) findViewById(R.id.species_name);
		distance = (TextView) findViewById(R.id.species_info);
		speciesImageView = (ImageView) findViewById(R.id.species_img);
		speciesImageView.setBackgroundResource(R.drawable.shapedrawable);
		
		mLayout = (LinearLayout) findViewById(R.id.text_field_layout);
		mDistanceTxt = (TextView) findViewById(R.id.textfield1);
		mDirectionTxt = (TextView) findViewById(R.id.textfield2);
		
		//HelperFunctionCalls helper = new HelperFunctionCalls();
		//helper.showSpeciesThumbNail(this, pbbItem.getCategory(), pbbItem.getSpeciesID(), pbbItem.getScienceName(), imageView);
		speciesName.setText(pbbItem.getCommonName());
		ProgressBar spinner = (ProgressBar) findViewById(R.id.spinner);
		HelperDrawableManager dm = new HelperDrawableManager(FloraCacheMedLevelContent.this, spinner, speciesImageView);
		dm.fetchDrawableOnThread(pbbItem.getImageURL());
		
	    setSensor();
	    checkGPS();
	    
	    speciesImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final RelativeLayout linear = (RelativeLayout) View.inflate(FloraCacheMedLevelContent.this, R.layout.image_popup, null);
				
				AlertDialog.Builder dialog = new AlertDialog.Builder(FloraCacheMedLevelContent.this);
				ImageView imageView = (ImageView) linear.findViewById(R.id.main_image);
				ProgressBar spinner = (ProgressBar) linear.findViewById(R.id.spinner);
				spinner.setVisibility(View.VISIBLE);
				
				String getPhotoImageURL = pbbItem.getImageURL();
				
				Log.i("K", "pbbItem.getImageURL() : " + pbbItem.getImageURL());
				
				HelperDrawableManager dm = new HelperDrawableManager(FloraCacheMedLevelContent.this, spinner, imageView);
				dm.fetchDrawableOnThread(getPhotoImageURL);
				
		        dialog.setView(linear);
		        dialog.show();
			}
		});
				
	    mMakeOB = (Button)findViewById(R.id.makeobservation);
	    mMakeOB.setEnabled(false);
	    mMakeOB.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(pbbItem.getCategory() == HelperValues.LOCAL_WHATSINVASIVE_LIST 
						|| pbbItem.getCategory() == HelperValues.LOCAL_POISONOUS_LIST
						|| pbbItem.getCategory() == HelperValues.LOCAL_THREATENED_ENDANGERED_LIST) {
					OneTimeDBHelper oDBH = new OneTimeDBHelper(FloraCacheMedLevelContent.this);
					mImageID = oDBH.getImageID(FloraCacheMedLevelContent.this, pbbItem.getScienceName(), pbbItem.getCategory());
				}
				
				if(mDistance < 15.0) {
					
					Intent intent = new Intent(FloraCacheMedLevelContent.this, FloracacheDetail.class);
					PBBItems pbbItems = new PBBItems();
					pbbItems.setCommonName(pbbItem.getCommonName());
					pbbItems.setScienceName(pbbItem.getScienceName());
					pbbItems.setSpeciesID(pbbItem.getSpeciesID());
					pbbItems.setProtocolID(pbbItem.getProtocolID());
					pbbItems.setCategory(pbbItem.getCategory());
					pbbItems.setFloracacheID(pbbItem.getFloracacheID());
					pbbItems.setLatitude(pbbItem.getLatitude());
					pbbItems.setLongitude(pbbItem.getLongitude());
					pbbItems.setIsFloracache(HelperValues.IS_FLORACACHE_YES);
					pbbItems.setSpeciesImageID(mImageID);
					
					intent.putExtra("pbbItem", pbbItems);
					intent.putExtra("image_id", mImageID);
					startActivity(intent);
				
				}
				else {
					Toast.makeText(FloraCacheMedLevelContent.this, 
							"Not close enough.", 
							Toast.LENGTH_SHORT).show();	
				}
				
			}
		});

	    // TODO Auto-generated method stub
	}
	
	private void setSensor() {
		mCompassView = (CompassView) findViewById(R.id.compassview);
		mCompassView.setVisibility(View.VISIBLE);
		
		mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> mySensors = mySensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        
        if(mySensors.size() > 0){
        	mySensorManager.registerListener(mySensorEventListener, mySensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        	mSersorRunning = true;
        	//Toast.makeText(this, "Start ORIENTATION Sensor", Toast.LENGTH_LONG).show();	
        	
        }
        else{
        	//Toast.makeText(this, "No ORIENTATION Sensor", Toast.LENGTH_LONG).show();
        	mSersorRunning = false;
        	finish();	
        }
	}
	
	private SensorEventListener mySensorEventListener = new SensorEventListener(){

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			bearingTo(mLatitude, mLongitude, mTargetLatitude, mTargetLongitude);
			
			mSensorValue = (float)event.values[0];
			
			//mDirTxt.setText(getDirectionStr(mSensorValue) + " " + mSensorValue + "\u00B0");
			mCompassView.updateDirection(mSensorValue);
		}
    };
    
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

	
	/**
	 * 
	 * Calculates the bearing of the two locations supplied and returns the angle.
	 * 
	 */
	private String bearingP1toP2(double latitude1, double longitude1, double latitude2, double longitude2)
    {
		float trueBearing = -(float) (Math.atan2(longitude2 - longitude1, latitude2 - latitude1) * 180 / Math.PI) + 90.0f;       
		
		Log.i("K", "Bearing : " + trueBearing);

        if(trueBearing < 0) {
        	return getDirectionStr((float)trueBearing + 360.0f);
        }
        
        return getDirectionStr((float)trueBearing);
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
        
		/*
		Location currentLoc = new Location("");
		currentLoc.setLatitude(latitude1);
		currentLoc.setLongitude(longitude1);
		
		Location targetLoc = new Location("");
		targetLoc.setLatitude(latitude2);
		targetLoc.setLongitude(longitude2);
		
		float bearing = currentLoc.bearingTo(targetLoc);
		
		if(bearing < 0) {
        	return (float)bearing + 360.0f;
        }
		
        return (float)bearing;
        */
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
		setContentView(R.layout.floracachemidleveldetail);
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
		
		if(mSersorRunning){
			mySensorManager.unregisterListener(mySensorEventListener);	
		}
		super.onDestroy();
	}
}
