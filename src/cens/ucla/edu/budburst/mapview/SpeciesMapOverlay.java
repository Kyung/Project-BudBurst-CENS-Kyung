package cens.ucla.edu.budburst.mapview;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.GetPhenophaseShared;
import cens.ucla.edu.budburst.GetPhenophaseObserver;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

// overlay class
public class SpeciesMapOverlay extends BalloonItemizedOverlay<SpeciesOverlayItem> {
	private Context mContext;
	private ArrayList<HelperPlantItem> mPlantList = new ArrayList<HelperPlantItem>();
	private ArrayList<SpeciesOverlayItem> mSItem = new ArrayList<SpeciesOverlayItem>();
	private	PopupPanel mPanel;
	private Drawable mMarker;
	private MapView mMap = null;
	private SpeciesOverlayItem speciesItem;
	
	public SpeciesMapOverlay(MapView mapView, Drawable marker, ArrayList<HelperPlantItem> plantList) {
		super(boundCenter(marker), mapView);
		
		mContext = mapView.getContext();
		mPlantList = plantList;

		// read data from the table
		for(int i = 0 ; i < mPlantList.size() ; i++) {
			GeoPoint geoPoint = getPoint(mPlantList.get(i).Latitude, 
					mPlantList.get(i).Longitude);
		
			int speciesID = mPlantList.get(i).SpeciesID;
			
			Log.i("K", "speciesID: " + mPlantList.get(i).SpeciesID +
					", Category : " + mPlantList.get(i).Category + 
					", ImageName : " + mPlantList.get(i).ImageName +
					", Lat: " + mPlantList.get(i).Latitude +
					", Lng: " + mPlantList.get(i).Longitude);
			
			mSItem.add(new SpeciesOverlayItem(geoPoint, speciesID, 
					mPlantList.get(i).CommonName,
					mPlantList.get(i).Date, 
					mPlantList.get(i).ImageName, 
					getMarker(R.drawable.full_marker), 
					mMarker, 
					mPlantList.get(i).Category,
					false));
		}
	
		populate();
	}
	
	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0),
												(int)(lon*1000000.0)));
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
		
	@Override
	protected boolean onBalloonTap(int index) {
		// Observed species are from PlantList and Shared Plant
		// need to separate.
		
		if(mPlantList.get(index).WhichList == HelperValues.MY_PLANT_LIST) {
			if(mPlantList.get(index).Where == HelperValues.FROM_PLANT_LIST) {
				Intent intent = new Intent(mContext, GetPhenophaseObserver.class);

				Log.i("K", "specise_id : " + mPlantList.get(index).SpeciesID
						+ " protocol_id : " + mPlantList.get(index).ProtocolID
						+ " site_id : " + mPlantList.get(index).PlantID
						+ " cname : " + mPlantList.get(index).CommonName
						+ " sname : " + mPlantList.get(index).SpeciesName);
				
				
				intent.putExtra("species_id", mPlantList.get(index).SpeciesID);
				intent.putExtra("protocol_id", mPlantList.get(index).ProtocolID);
				// actually this name should be called as siteID but use PlantID field as this.
				intent.putExtra("site_id", mPlantList.get(index).PlantID); 
				intent.putExtra("cname", mPlantList.get(index).CommonName);
				intent.putExtra("sname", mPlantList.get(index).SpeciesName);
				intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
				
				mContext.startActivity(intent);
			}
			else {
				Intent intent = new Intent(mContext, GetPhenophaseShared.class);
				Log.i("K", "PlantID: " + mPlantList.get(index).PlantID);
				intent.putExtra("id", mPlantList.get(index).PlantID);
				mContext.startActivity(intent);
			}
		}
		// If the marker is from other users' list
		else if(mPlantList.get(index).WhichList == HelperValues.OTHERS_PLANT_LIST){
			// move to species Info.
			
			Log.i("K", "Species ID : " + mPlantList.get(index).SpeciesID);
			
			Intent intent = new Intent(mContext, SpeciesDetailMap.class);
			intent.putExtra("cname", mPlantList.get(index).CommonName);
			intent.putExtra("sname", mPlantList.get(index).SpeciesName);
			intent.putExtra("dt_taken", mPlantList.get(index).Date);
			intent.putExtra("notes", mPlantList.get(index).Note);
			intent.putExtra("species_id", mPlantList.get(index).SpeciesID);
			intent.putExtra("category", mPlantList.get(index).Category);
			intent.putExtra("username", mPlantList.get(index).UserName);
			intent.putExtra("phenophase_id", mPlantList.get(index).PhenoID);
			intent.putExtra("protocol_id", mPlantList.get(index).ProtocolID);
			intent.putExtra("latitude", mPlantList.get(index).Latitude);
			intent.putExtra("longitude", mPlantList.get(index).Longitude);
			intent.putExtra("imageID", mPlantList.get(index).ImageName);
		
			mContext.startActivity(intent);
		
		}
		else {
			
		}
		
		hideBalloon();
		
		return true;
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





