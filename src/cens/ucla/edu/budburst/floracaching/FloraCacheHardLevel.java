package cens.ucla.edu.budburst.floracaching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cens.ucla.edu.budburst.PBBHelpPage;
import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.PBBSync;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapterFloracache;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.HelperGpsHandler;
import cens.ucla.edu.budburst.helper.HelperListItem;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperSettings;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.onetime.OneTimePhenophase;
import cens.ucla.edu.budburst.utils.PBBItems;
import cens.ucla.edu.budburst.utils.QuickCapture;

import com.google.android.maps.GeoPoint;

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
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FloraCacheHardLevel extends ListActivity {
	
	private LocationManager mLocManager = null;
	private HelperGpsHandler gpsHandler;
	private boolean mIsBound;
	private boolean mFirstTime = true;
	private double mLatitude 		= 0.0;
	private double mLongitude 		= 0.0;
	private int mNumSpecies;
	private int mGroupID;
	private int mIndex;
	private int mImageID;
	
	private static int MAX_NUM_SHOWN = 20;
	
	private EditText mLatTxt;
	private EditText mLonTxt;
	private EditText mInfoTxt;
	
	private MyListAdapterFloracache mListapdater;
	private ArrayList<HelperPlantItem> mListArr;
	private HelperListItem mItem;
	
	private Button mRefreshListBtn;
	
	private ArrayList<FloracacheItem> mPlantList = new ArrayList<FloracacheItem>();
	
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			gpsHandler = ((HelperGpsHandler.GpsBinder) binder).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			gpsHandler = null;
		}
	};
	
	private void doBindService() {
		
		Log.i("K", "BindService");
		
		bindService(new Intent(FloraCacheHardLevel.this, HelperGpsHandler.class), mConnection,
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
				mLatitude = extras.getDouble("latitude");
				mLongitude = extras.getDouble("longitude");
				
				mRefreshListBtn.setEnabled(true);
				
				TextView infoTxt = (TextView)findViewById(R.id.gps_info);
				infoTxt.setVisibility(View.GONE);
				
				Log.i("K", "mLatitude : " + mLatitude + " mLongitude : " + mLongitude);
				
				if(mFirstTime) {
					mFirstTime = false;
					getFloraLists();
				}
				else {
					//updateInfo();
				}
				
			}
			// if Gps signal is bad
			else {
				//Toast.makeText(FloraCacheHardLevel.this, getString(R.string.Low_GPS_Signal), Toast.LENGTH_SHORT).show();
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
				// TODO Auto-generated method stub
				updateFloraLists();
			}
		});
	    
	    // TODO Auto-generated method stub
	}
	
	private void getFloraLists() {
		HelperSharedPreference hPref = new HelperSharedPreference(this);
		if(!hPref.getPreferenceBoolean("floracache")) {
			Toast.makeText(FloraCacheHardLevel.this, "To download the list, Go 'Settings' page", Toast.LENGTH_SHORT).show();
		}
		else {
			OneTimeDBHelper oDBH = new OneTimeDBHelper(this);
			mPlantList = oDBH.getFloracacheLists(FloraCacheHardLevel.this, HelperValues.FLORACACHE_HARD, mGroupID, mLatitude, mLongitude);
			
			mListapdater = new MyListAdapterFloracache(this, R.layout.floracache_item ,mPlantList);
			ListView MyList = getListView();
			MyList.setAdapter(mListapdater);	
		}
	}
	
	private void updateFloraLists() {

		OneTimeDBHelper oDBH = new OneTimeDBHelper(this);
		mPlantList = oDBH.getFloracacheLists(FloraCacheHardLevel.this, HelperValues.FLORACACHE_HARD, mGroupID, mLatitude, mLongitude);
		
		mListapdater = new MyListAdapterFloracache(this, R.layout.floracache_item ,mPlantList);
		ListView MyList = getListView();
		MyList.setAdapter(mListapdater);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		mIndex = position;
		
		updateFloraLists();
		
		if(mPlantList.get(mIndex).getUserSpeciesCategoryID() == HelperValues.LOCAL_WHATSINVASIVE_LIST 
				|| mPlantList.get(mIndex).getUserSpeciesCategoryID() == HelperValues.LOCAL_POISONOUS_LIST
				|| mPlantList.get(mIndex).getUserSpeciesCategoryID() == HelperValues.LOCAL_THREATENED_ENDANGERED_LIST) {
			OneTimeDBHelper oDBH = new OneTimeDBHelper(FloraCacheHardLevel.this);
			mImageID = oDBH.getImageID(FloraCacheHardLevel.this, mPlantList.get(mIndex).getScienceName(), mListArr.get(mIndex).getCategory());
		}
		
		if(mPlantList.get(mIndex).getDistance() < 15.0) {
			
			if(mPlantList.get(mIndex).getUserSpeciesCategoryID() != HelperValues.LOCAL_BUDBURST_LIST) {
				OneTimeDBHelper oDBH = new OneTimeDBHelper(FloraCacheHardLevel.this);
				mImageID = oDBH.getImageID(FloraCacheHardLevel.this, mPlantList.get(mIndex).getScienceName(), mPlantList.get(mIndex).getUserSpeciesCategoryID());
			}
			
			Intent intent = new Intent(FloraCacheHardLevel.this, FloracacheDetail.class);
			PBBItems pbbItem = new PBBItems();
			pbbItem.setCommonName(mPlantList.get(mIndex).getCommonName());
			pbbItem.setScienceName(mPlantList.get(mIndex).getScienceName());
			pbbItem.setSpeciesID(mPlantList.get(mIndex).getUserSpeciesID());
			pbbItem.setProtocolID(mPlantList.get(mIndex).getProtocolID());
			pbbItem.setCategory(mPlantList.get(mIndex).getUserSpeciesCategoryID());
			pbbItem.setIsFloracache(HelperValues.IS_FLORACACHE_YES); // set floracacheID to easy value
			pbbItem.setFloracacheID(mPlantList.get(mIndex).getFloracacheID());
			pbbItem.setLatitude(mPlantList.get(mIndex).getLatitude());
			pbbItem.setLongitude(mPlantList.get(mIndex).getLongitude());
			
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("image_id", mImageID);
			intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
			
			startActivity(intent);
		}
		else {
			Toast.makeText(FloraCacheHardLevel.this, "Not close enough. Dist: " + String.format("%5.2f", mPlantList.get(mIndex).getDistance() * 3.2808399) + "ft", Toast.LENGTH_SHORT).show();	
		}
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
		setContentView(R.layout.floracachehardlevel);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.Floracache_Game) + " Hard Level");
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
	
	
	/*
	 * Menu option(non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, getString(R.string.Floracache_Hard_Refresh)).setIcon(android.R.drawable.ic_menu_rotate);
			
		return true;
	}
	
	/*
	 * Menu option selection handling(non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){

		Intent intent;
		switch(item.getItemId()){
			case 1:
				Toast.makeText(FloraCacheHardLevel.this, getString(R.string.Map_Getting_GPS_Signal), Toast.LENGTH_SHORT).show();
				doBindService();
				return true;					
		}
		return false;
	}
}