package cens.ucla.edu.budburst.floracaching;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;
import cens.ucla.edu.budburst.GetPhenophaseObserver;
import cens.ucla.edu.budburst.GetPhenophaseShared;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.FloracacheItem;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.lists.ListDetail;
import cens.ucla.edu.budburst.mapview.BalloonItemizedOverlay;
import cens.ucla.edu.budburst.mapview.PopupPanel;
import cens.ucla.edu.budburst.mapview.SpeciesDetailMap;
import cens.ucla.edu.budburst.mapview.SpeciesOverlayItem;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.QuickCapture;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class FloraCacheOverlay extends BalloonItemizedOverlay<SpeciesOverlayItem>{
	private Context mContext;
	private ArrayList<FloracacheItem> mPlantList = new ArrayList<FloracacheItem>();
	private ArrayList<SpeciesOverlayItem> mSItem = new ArrayList<SpeciesOverlayItem>();
	private	PopupPanel mPanel;
	private Drawable mMarker;
	private MapView mMap = null;
	private SpeciesOverlayItem speciesItem;
	private int mIndex;
	private int mImageID;
	
	public FloraCacheOverlay(MapView mapView, Drawable marker, ArrayList<FloracacheItem> plantList) {
		super(boundCenter(marker), mapView);
		
		mContext = mapView.getContext();
		mPlantList = plantList;

		// read data from the table
		for(int i = 0 ; i < mPlantList.size() ; i++) {
			GeoPoint geoPoint = getPoint(mPlantList.get(i).getLatitude(), 
					mPlantList.get(i).getLongitude());

			//SpeciesOverlayItem(GeoPoint pt, int SpeciesID, String name, String snippet, String imageUrl,
			// Drawable marker, Drawable heart, int category)
			mSItem.add(new SpeciesOverlayItem(geoPoint, 
					mPlantList.get(i).getUserSpeciesID(), 
					mPlantList.get(i).getFloracacheNotes(),
					"", 
					"",
					getMarker(R.drawable.full_marker), 
					mMarker, 
					mPlantList.get(i).getUserSpeciesCategoryID(),
					true));
		}
	
		populate();
	}
	
	private Drawable getMarker(int resource) {
		Drawable marker = mContext.getResources().getDrawable(resource);
		marker.setBounds(-marker.getIntrinsicWidth() / 2, marker.getIntrinsicHeight(), marker.getIntrinsicWidth() / 2, 0);
		boundCenter(marker);
		return(marker);
	}

	@Override
	protected SpeciesOverlayItem createItem(int i) {
		return(mSItem.get(i));
	}

	private Bitmap LoadImageFromWebOperation(String url) {
		try {
			URL imageURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)imageURL.openConnection();
			conn.setDoInput(true);
			conn.connect();
			
			InputStream is = conn.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			
			return bitmap; 
			
		}
		catch(Exception e) {
			return null;
		}
	}
	
	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}
		
	@Override
	protected boolean onBalloonTap(int index) {
		// Observed species are from PlantList and Shared Plant
		
		mIndex = index;
		
		
		// calculate distance from mylocation to the marker
		double latitude = mPlantList.get(index).getLatitude();
		double longitude = mPlantList.get(index).getLongitude();
		
		HelperSharedPreference hPref = new HelperSharedPreference(mContext);
		double curLat = Double.parseDouble(hPref.getPreferenceString("latitude", "0.0"));
		double curLon = Double.parseDouble(hPref.getPreferenceString("longitude", "0.0"));
		
		float[] distResult = new float[1];
		
		Location.distanceBetween(latitude, longitude, curLat, curLon, distResult);

		if(distResult[0] < 1000) {
			showDialog();
		}
		else {
			Toast.makeText(mContext, "distResult : " + distResult[0] + "m", Toast.LENGTH_SHORT).show();	
		}
			
		hideBalloon();
		
		return true;
	}
	
	private void showDialog() {
		
		if(mPlantList.get(mIndex).getUserSpeciesCategoryID() != HelperValues.LOCAL_BUDBURST_LIST) {
			OneTimeDBHelper oDBH = new OneTimeDBHelper(mContext);
			mImageID = oDBH.getImageID(mContext, mPlantList.get(mIndex).getScienceName(), mPlantList.get(mIndex).getUserSpeciesCategoryID());
		}
		
		Log.i("K", "mPlantList.get(mIndex).getUserSpeciesID() : " + mPlantList.get(mIndex).getUserSpeciesID());
		
		new AlertDialog.Builder(mContext)
		.setTitle("Make an observation")
		.setMessage("Great! You are very close to the species. Proceed to make an observation?")
		.setPositiveButton(mContext.getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				/*
				 * Move to QuickCapture
				 */
				Intent intent = new Intent(mContext, QuickCapture.class);
				
				intent.putExtra("cname", mPlantList.get(mIndex).getCommonName());
				intent.putExtra("sname", mPlantList.get(mIndex).getScienceName());
				intent.putExtra("species_id", mPlantList.get(mIndex).getUserSpeciesID());
				intent.putExtra("protocol_id", mPlantList.get(mIndex).getProtocolID());				
				intent.putExtra("category", mPlantList.get(mIndex).getUserSpeciesCategoryID());
				intent.putExtra("image_id", mImageID);
				intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
				
				mContext.startActivity(intent);

			}
		})
		.setNeutralButton(mContext.getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				/*
				 * Move to Getphenophase without a photo.
				 */

				Intent intent = new Intent(mContext, GetPhenophase.class);
				intent.putExtra("camera_image_id", "");
				intent.putExtra("cname", mPlantList.get(mIndex).getCommonName());
				intent.putExtra("sname", mPlantList.get(mIndex).getScienceName());
				intent.putExtra("protocol_id", mPlantList.get(mIndex).getProtocolID());
				intent.putExtra("species_id", mPlantList.get(mIndex).getUserSpeciesID());
				intent.putExtra("category", mPlantList.get(mIndex).getUserSpeciesCategoryID());
				intent.putExtra("image_id", mImageID);
				intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
				
				mContext.startActivity(intent);

			}
		})
		.setNegativeButton(mContext.getString(R.string.Button_Cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		})
		.show();
	}


	@Override
	public int size() {
		return(mSItem.size());
	}
	
	
	void toggleHeart() {
		SpeciesOverlayItem focus=getFocus();
		
		if (focus!=null) {
			focus.toggleHeart();
		}
		
		mMap.invalidate();
	}	
}
