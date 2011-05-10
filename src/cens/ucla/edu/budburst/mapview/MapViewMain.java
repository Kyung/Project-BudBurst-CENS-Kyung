package cens.ucla.edu.budburst.mapview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.R.string;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperGpsHandler;
import cens.ucla.edu.budburst.helper.HelperJSONParser;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperSettings;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.lists.ListGroupItem;
import cens.ucla.edu.budburst.lists.ListUserDefinedSpeciesDownload;
import cens.ucla.edu.budburst.myplants.PBBPlantList;
import cens.ucla.edu.budburst.onetime.OneTimePhenophase;
import cens.ucla.edu.budburst.utils.QuickCapture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
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

public class MapViewMain extends MapActivity{ //implements LocationListener {
	
	private HelperGpsHandler gpsHandler;
	private boolean mIsBound;
	private boolean mFirstGps;
	private Handler handler = new Handler();
	
	private OneTimeDBHelper otDBH = null;
	private String bestProvider;
	
	// Map related variables
	private LocationManager mLocManager = null;
	private MapView mMapView = null;
	private MyLocationOverlay mMyOverLay = null;
	private MapController mMapController = null;
	
	// Dialog
	private ProgressDialog mDialog;
	
	// other variables
	private String signalLevelString = null;
	private String url = null;	
	private double mLatitude 		= 0.0;
	private double mLongitude 		= 0.0;
	private boolean mHandlerDone	= false;
	private int mType = 100;
	
	private static final int GET_GPS_SIGNAL = 10;
	private static final int GET_MY_OBSERVED_LISTS = 11;
	private boolean viewFlag = false;
	
	private ArrayList<HelperPlantItem> mPlantList;
	private HelperPlantItem pItem;
	
	private Drawable mMarker;
	
	private String[] mPlantCategory;
	private boolean[] mSelect;
	private ArrayList<ListGroupItem> mArr;
	
	// timer variables
	private Timer timer;
	
	//
	private boolean firstResume = true;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch(msg.what) {
			case GET_GPS_SIGNAL:
				Log.i("K", "get GET_GPS_SIGNAL");
				showSpeciesOnMap(true);
				break;
			case GET_MY_OBSERVED_LISTS:
				Log.i("K", "get GET_MY_OBSERVED_LISTS");
				mDialog.dismiss();
			}
		}
	};
	
	
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
		
		bindService(new Intent(MapViewMain.this, HelperGpsHandler.class), mConnection,
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("K", "PBBMapMain - onCreate");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pbb_map);
		
		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Set MapView
		mMapView = (MapView)findViewById(R.id.map);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(false);
		// Set mapController
		mMapController = mMapView.getController();
		mMapController.setZoom(12);
		
		// Add mylocation overlay
		mMyOverLay = new MyLocationOverlay(MapViewMain.this, mMapView);
		mMyOverLay.enableMyLocation();
		mMyOverLay.enableCompass();
		
		
		// remove view of accuracy bar
		TextView titleBar = (TextView)findViewById(R.id.myloc_accuracy);
		titleBar.setVisibility(View.GONE);
		
		Intent pIntent = getIntent();
		mType = pIntent.getExtras().getInt("type", 100);
		
		// initialize plantList
		mPlantList = new ArrayList<HelperPlantItem>();
		
		// initialize marker
		mMarker = getResources().getDrawable(R.drawable.marker);
		mMarker.setBounds(0, 0, mMarker.getIntrinsicWidth(), mMarker.getIntrinsicHeight());
		
		IntentFilter inFilter = new IntentFilter(HelperGpsHandler.GPSHANDLERFILTER);
		registerReceiver(gpsReceiver, inFilter);
		Log.i("K", "Receiver Register");
		
		checkGpsIsOn();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if(mMapView != null) {
			mMapView.invalidate();
			mMapView.postInvalidate();
		}
	}

	public void checkGpsIsOn() {
		// check if GPS is turned on...
		if (mLocManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			
			Location lastLoc = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(lastLoc != null) {
				mLatitude = lastLoc.getLatitude();
				mLongitude = lastLoc.getLongitude();
				mMapController.setCenter(getPoint(mLatitude, mLongitude));
			}
			
			doBindService();
			showSpeciesOnMap(false);
			
		}
		else {
		   	
		 new AlertDialog.Builder(MapViewMain.this)
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
		   				finish();
		   			}
		   		})
		   		.show();
		}	
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
        	if(resultCode == RESULT_OK) {
        		Log.i("K", "onActivityResult");
            	doBindService();
            	showSpeciesOnMap(false);
        	}
        }
    }
	
	public void showSpeciesOnMap(boolean hasHandler) {
		
		// TODO Auto-generated method stub
		otDBH = new OneTimeDBHelper(MapViewMain.this);
		
		GeoPoint gPoint = new GeoPoint((int)(mLatitude * 1000000), (int)(mLongitude * 1000000));
		
		mMapView.setBuiltInZoomControls(true);
		mMapView.invalidate();
		
		if(getMyListsFromDB()) {
			Log.i("K", "Get species lists in the database");
			
			//MapView mapView, Drawable marker, ArrayList<PlantItem> plantList
			mMapView.getOverlays().add(new SpeciesMapOverlay(mMapView, mMarker, mPlantList));
			mMapView.getOverlays().add(mMyOverLay);
			
			mMapController.setCenter(gPoint);
			
			if(hasHandler) {
				mHandlerDone = true;
				mHandler.sendEmptyMessage(GET_MY_OBSERVED_LISTS);
				mMapController.setZoom(12);
			}
		}
		else {
			Log.i("K", "No species lists in the database.");
		}

	}
	
	public boolean getMyListsFromDB() {
		SyncDBHelper sDBH = new SyncDBHelper(this);
		OneTimeDBHelper oDBH = new OneTimeDBHelper(this);
		
		// add myPlantList from Monitored Plants
		// temporary stop
		//mPlantList = sDBH.getAllMyListInformation(this);
		
		// add myPlantList from Shared Plants
		mPlantList.addAll(oDBH.getAllMyListInformation(this));
		
		Log.i("K", "the number of mylist (size) : " + mPlantList.size());
		
		sDBH.close();
		oDBH.close();
		
		if(mPlantList.size() > 0) {
			return true;
		}
		return false;
	}
	
	
	public void getOtherUsersListsFromServer(int category) {
		SpeciesOthersFromServer getSpecies = new SpeciesOthersFromServer(MapViewMain.this, mMapView, mMyOverLay, category);
		getSpecies.execute(getString(R.string.get_onetimeob_others) + 
				"?latitude=" + mLatitude + "&longitude=" + mLongitude);
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, getString(R.string.PBBMapMenu_myLocation)).setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, 2, 0, getString(R.string.PBBMapMenu_changeView)).setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(0, 3, 0, getString(R.string.PBBMapMenu_refresh)).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, 4, 0, getString(R.string.otherCategoryMap)).setIcon(android.R.drawable.ic_menu_sort_by_size);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case 1:
				GeoPoint current_point = null;
				if(mLatitude == 0.0) {
					Toast.makeText(MapViewMain.this, getString(R.string.Alert_gettingGPS), Toast.LENGTH_SHORT).show();
				}
				else {
					current_point = new GeoPoint((int)(mLatitude * 1000000), (int)(mLongitude * 1000000));

					mMapController = mMapView.getController();
					mMapController.animateTo(current_point);
					mMapController.setZoom(17);
				}
				return true;
			case 2:
				if(!viewFlag) {
					mMapView.setSatellite(true);
					viewFlag = true;
				}
				else {
					mMapView.setSatellite(false);
					viewFlag = false;
				}
				
				return true;				
			case 3:
				getNewGPS();
				return true;
			case 4:
				showCategory();
				return true;
		}
		return false;
	}
	
	private void showCategory() {
		mArr = new ArrayList<ListGroupItem>(); 
		
		ListGroupItem gItem = new ListGroupItem();
		gItem.setCategoryID(1);
		gItem.setCategoryName("Local Budburst");
		mArr.add(gItem);
		
		gItem = new ListGroupItem();
		gItem.setCategoryID(2);
		gItem.setCategoryName("Local Invasive");
		mArr.add(gItem);
		
		gItem = new ListGroupItem();
		gItem.setCategoryID(3);
		gItem.setCategoryName("Local Poisonous");
		mArr.add(gItem);
		
		gItem = new ListGroupItem();
		gItem.setCategoryID(4);
		gItem.setCategoryName("Local Endangered");
		mArr.add(gItem);
		
		OneTimeDBHelper oDBH = new OneTimeDBHelper(MapViewMain.this);
		mArr.addAll(oDBH.getListGroupItem(MapViewMain.this));
		
		int arrLength = mArr.size();
		
		Log.i("K", "length : " + arrLength);
		
		mPlantCategory = new String[arrLength];
		mSelect = new boolean[arrLength];

		for(int i = 0 ; i < arrLength ; i++) {
			mPlantCategory[i] = mArr.get(i).getCategoryName();
			mSelect[i] = false;
		}
		
		new AlertDialog.Builder(MapViewMain.this)
   		.setTitle(getString(R.string.cateogory_text))
   		.setSingleChoiceItems(mPlantCategory, -1, new DialogInterface.OnClickListener() {
	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mSelect[which] = true;
			}
   		})
   		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				for(int i = 0 ; i < mSelect.length ; i++) {
					if(mSelect[i]) {
						getOtherUsersListsFromServer(mArr.get(i).getCategoryID());
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_back), null)
   		.show();
	}
	
	private void getNewGPS() {
		mDialog = new ProgressDialog(this);
		mDialog.setMessage(getString(R.string.Map_Getting_GPS_Signal));
		mDialog.setCancelable(true);
		mDialog.show();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Looper.prepare();
				// set update the location data in 1secs or 5meters
				unbindService(mConnection);
				bindService(new Intent(MapViewMain.this, HelperGpsHandler.class), mConnection,
						Context.BIND_AUTO_CREATE);
				
				Looper.loop();
			}
			
		}).start();
	}
	
	@Override
	public void onDestroy() {
		// when user finish this activity, turn off the gps
		// if there's a overlay, should call disableCompass() explicitly
		doUnbindService();
		if(gpsReceiver != null) {
			unregisterReceiver(gpsReceiver);
		}
		
		mMyOverLay.disableCompass();
		mMyOverLay.disableMyLocation();
	
		super.onDestroy();
	}

	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle extras = intent.getExtras();
			
			if(extras.getBoolean("signal")) {
				mLatitude = extras.getDouble("latitude");
				mLongitude = extras.getDouble("longitude");
				
				// convert points into GeoPoint
			    GeoPoint gPoint = getPoint(mLatitude, mLongitude);

			    // center the map
			    if(mFirstGps) {
			    	mMapController.setCenter(gPoint);
			    	mFirstGps = false;
			    }
			    
			    if(mDialog != null) {
			    	mDialog.dismiss();
			    }
			}
			// if Gps signal is bad
			else {
				new AlertDialog.Builder(MapViewMain.this)
				.setTitle("Weak Gps Signal")
				.setMessage("Cannot get Gps Signal, Make sure you are in the good connectivity area")
				.setPositiveButton(getString(R.string.Button_back), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();
			}
		}	
	};
}