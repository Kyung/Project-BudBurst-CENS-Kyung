package cens.ucla.edu.budburst.floracaching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FloraCacheMidLevel extends ListActivity {

	private LocationManager mLocManager = null;
	private HelperGpsHandler gpsHandler;
	private boolean mIsBound;
	private boolean mFirstGps;
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
	
	private static int MAX_NUM_SHOWN = 5;
	
	private MyListAdapterFloracache mListapdater;
	private ArrayList<HelperPlantItem> mListArr;
	private HelperListItem mItem;
	
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
				
				TextView infoTxt = (TextView)findViewById(R.id.gps_info);
				infoTxt.setVisibility(View.GONE);
				
				getFloraLists();
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
					}
				})
				.show();
			}
		}	
	};
	/*
	private void updateView(int index) {
		
		Log.i("K", "index : " + index);
		
		View v = mListView.getChildAt(index - mListView.getFirstVisiblePosition());
		
		float dist[] = new float[1];
		Location.distanceBetween(mLatitude, mLongitude, mListArr.get(index).getLatitude(), mListArr.get(index).getLongitude(), dist);
		mListArr.get(index).setDescription(String.format("%5.2fm away ", dist[0])
				+ " / "
				+ bearingP1toP2(mLatitude, mLongitude, mListArr.get(index).getLatitude(), mListArr.get(index).getLongitude()) + " ");
		
	
		TextView descTxt = (TextView) v.findViewById(R.id.list_name_detail);
		descTxt.setText(String.format("%5.2fm away ", dist[0])
				+ " / "
				+ bearingP1toP2(mLatitude, mLongitude, mListArr.get(index).getLatitude(), mListArr.get(index).getLongitude()) + " ");
		
	}
	*/
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
	    
	    Intent gIntent = getIntent();
	    mGroupID = gIntent.getExtras().getInt("group_id");	    
	    mListArr = new ArrayList<HelperPlantItem>();
	    
	    setTitleBar();
	   
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
			mPlantList = oDBH.getFloracacheLists(FloraCacheMidLevel.this, HelperValues.FLORACACHE_MID, mGroupID);
			
			mListArr = new ArrayList<HelperPlantItem>();
			
			for(int i = 0 ; i < mPlantList.size() ; i++) {
				
				float dist[] = new float[1];
				Location.distanceBetween(mLatitude, mLongitude, mPlantList.get(i).getLatitude(), mPlantList.get(i).getLongitude(), dist);
				
				HelperPlantItem pi = new HelperPlantItem();
				pi.setSpeciesID(mPlantList.get(i).getUserSpeciesID());
				pi.setCategory(mPlantList.get(i).getUserSpeciesCategoryID());
				pi.setSpeciesName(mPlantList.get(i).getScienceName());
				pi.setCommonName(mPlantList.get(i).getCommonName());
				pi.setProtocolID(mPlantList.get(i).getProtocolID());
				pi.setUserName(mPlantList.get(i).getUserName());
				pi.setFloracacheID(mPlantList.get(i).getFloracacheID());
				pi.setDistance(dist[0]);
				pi.setDescription(String.format("%5.2fm away ", dist[0])
						+ " / "
						+ bearingP1toP2(mLatitude, mLongitude, mPlantList.get(i).getLatitude(), mPlantList.get(i).getLongitude()) + " ");
				
				mListArr.add(pi);
			}
			
			// sorting
			Comparator<HelperPlantItem> compare = new Comparator<HelperPlantItem>() {

				@Override
				public int compare(HelperPlantItem obj1, HelperPlantItem obj2) {
					// TODO Auto-generated method stub
					return String.valueOf(obj1.getDistance()).compareToIgnoreCase(String.valueOf(obj2.getDistance()));
				}
			};
			
			Collections.sort(mListArr, compare);
			
			ArrayList<HelperPlantItem> newArr = new ArrayList<HelperPlantItem>();
		
			int count = 0;
			for(HelperPlantItem item: mListArr) {
				
				if(count++ < MAX_NUM_SHOWN) {
					try {
						newArr.add(item.clone());
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			mListapdater = new MyListAdapterFloracache(this, R.layout.floracache_item ,newArr);
			mListView = getListView();
			mListView.setAdapter(mListapdater);	
		}
	}
	
	
	private String bearingP1toP2(double latitude1, double longitude1, double latitude2, double longitude2)
    {
        // current loc
        double Cur_Lat_radian = latitude1 * (3.141592 / 180);
        double Cur_Lon_radian = longitude1 * (3.141592 / 180);


        // target loc
        double Dest_Lat_radian = latitude2 * (3.141592 / 180);
        double Dest_Lon_radian = longitude2 * (3.141592 / 180);

        // radian distance
        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_Lat_radian) * Math.sin(Dest_Lat_radian) + Math.cos(Cur_Lat_radian) * Math.cos(Dest_Lat_radian) * Math.cos(Cur_Lon_radian - Dest_Lon_radian));

        // caculate direction
        double radian_bearing = Math.acos((Math.sin(Dest_Lat_radian) - Math.sin(Cur_Lat_radian) * Math.cos(radian_distance)) / (Math.cos(Cur_Lat_radian) * Math.sin(radian_distance)));        
       

        double trueBearing = 0;
        if (Math.sin(Dest_Lon_radian - Cur_Lon_radian) < 0)
        {
        	trueBearing = radian_bearing * (180 / 3.141592);
            trueBearing = 360 - trueBearing;
        }
        else
        {
        	trueBearing = radian_bearing * (180 / 3.141592);
        }
        
        String direction = "";
        
        if(trueBearing == 0 || trueBearing == 360)
        	direction = "N";
        if(trueBearing > 0 && trueBearing < 90) 
        	direction ="NE";
        if(trueBearing == 90)
        	direction = "E";
        if(trueBearing > 90 && trueBearing < 180) 
        	direction ="SE";
        if(trueBearing == 180)
        	direction = "S";
        if(trueBearing > 180 && trueBearing < 270) 
        	direction ="SW";
        if(trueBearing == 270)
        	direction = "W";
        if(trueBearing > 270 && trueBearing < 360) 
        	direction ="NW";
        
        return direction;
    }
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		String getDesc = mListArr.get(position).getDescription();
		String []getDescSplit = getDesc.split("/");
		String dest = getDescSplit[0].replace("m away", "").trim();
		
		mIndex = position;
		
		if(mListArr.get(mIndex).getCategory() == HelperValues.LOCAL_WHATSINVASIVE_LIST 
				|| mListArr.get(mIndex).getCategory() == HelperValues.LOCAL_POISONOUS_LIST
				|| mListArr.get(mIndex).getCategory() == HelperValues.LOCAL_THREATENED_ENDANGERED_LIST) {
			OneTimeDBHelper oDBH = new OneTimeDBHelper(FloraCacheMidLevel.this);
			mImageID = oDBH.getImageID(FloraCacheMidLevel.this, mListArr.get(mIndex).getSpeciesName(), mListArr.get(mIndex).getCategory());
		}
		
		if(Double.parseDouble(dest) < 6.0) {
			
			Intent intent = new Intent(FloraCacheMidLevel.this, FloracacheDetail.class);
			PBBItems pbbItem = new PBBItems();
			pbbItem.setCommonName(mListArr.get(mIndex).getCommonName());
			pbbItem.setScienceName(mListArr.get(mIndex).getSpeciesName());
			pbbItem.setSpeciesID(mListArr.get(mIndex).getSpeciesID());
			pbbItem.setProtocolID(mListArr.get(mIndex).getProtocolID());
			pbbItem.setCategory(mListArr.get(mIndex).getCategory());
			pbbItem.setFloracacheID(mListArr.get(mIndex).getFloracacheID());
			pbbItem.setIsFloracache(HelperValues.IS_FLORACACHE_YES);
			pbbItem.setSpeciesImageID(mImageID);
			
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("image_id", mImageID);
			startActivity(intent);
			
			
		/*	
			new AlertDialog.Builder(FloraCacheMidLevel.this)
			.setTitle(getString(R.string.PlantInfo_makeObs))
			.setMessage(getString(R.string.Floracache_Easy_Success))
			.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					// Move to QuickCapture
					Intent intent = new Intent(FloraCacheMidLevel.this, QuickCapture.class);
					PBBItems pbbItem = new PBBItems();
					pbbItem.setCommonName(mListArr.get(mIndex).getCommonName());
					pbbItem.setScienceName(mListArr.get(mIndex).getSpeciesName());
					pbbItem.setSpeciesID(mListArr.get(mIndex).getSpeciesID());
					pbbItem.setProtocolID(mListArr.get(mIndex).getProtocolID());
					pbbItem.setCategory(mListArr.get(mIndex).getCategory());
					pbbItem.setFloracacheID(HelperValues.IS_FLORACACHE_YES); // set floracacheID to easy value
					
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("image_id", mImageID);
					intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
					
					startActivity(intent);

				}
			})
			.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					// Move to Getphenophase without a photo.
					Intent intent = new Intent(FloraCacheMidLevel.this, OneTimePhenophase.class);
					PBBItems pbbItem = new PBBItems();
					pbbItem.setCommonName(mListArr.get(mIndex).getCommonName());
					pbbItem.setScienceName(mListArr.get(mIndex).getSpeciesName());
					pbbItem.setSpeciesID(mListArr.get(mIndex).getSpeciesID());
					pbbItem.setProtocolID(mListArr.get(mIndex).getProtocolID());
					pbbItem.setCategory(mListArr.get(mIndex).getCategory());
					pbbItem.setFloracacheID(HelperValues.IS_FLORACACHE_YES); // set floracacheID to easy value
					pbbItem.setLocalImageName("");
					
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("image_id", mImageID);
					intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
					
					startActivity(intent);
				}
			})
			.setNegativeButton(getString(R.string.Button_Cancel), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			})
			.show();
			*/
		}
		else {
			Toast.makeText(FloraCacheMidLevel.this, "Not close enough. Dist: " + String.format("%5.2f", Double.parseDouble(dest)) + "m", Toast.LENGTH_SHORT).show();	
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
		setContentView(R.layout.floracachemidlevel);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.Floracache_Game));
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
