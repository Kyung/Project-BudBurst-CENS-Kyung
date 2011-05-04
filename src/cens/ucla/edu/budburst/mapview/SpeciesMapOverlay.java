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
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.myplants.GetPhenophaseObserver;
import cens.ucla.edu.budburst.myplants.GetPhenophaseShared;
import cens.ucla.edu.budburst.utils.PBBItems;

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
			GeoPoint geoPoint = getPoint(mPlantList.get(i).getLatitude(), 
					mPlantList.get(i).getLongitude());
		
			int speciesID = mPlantList.get(i).getSpeciesID();

			
			mSItem.add(new SpeciesOverlayItem(geoPoint, speciesID, 
					mPlantList.get(i).getCommonName(),
					mPlantList.get(i).getCredit(),
					mPlantList.get(i).getDate(), 
					mPlantList.get(i).getImageName(), 
					getMarker(R.drawable.full_marker), 
					mMarker, 
					mPlantList.get(i).getCategory(),
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
		
		if(mPlantList.get(index).getWhichList() == HelperValues.MY_PLANT_LIST) {
			if(mPlantList.get(index).getWhere() == HelperValues.FROM_PLANT_LIST) {
				Intent intent = new Intent(mContext, GetPhenophaseObserver.class);
				
				PBBItems pbbItem = new PBBItems();
				pbbItem.setSpeciesID(mPlantList.get(index).getSpeciesID());
				pbbItem.setProtocolID(mPlantList.get(index).getProtocolID());
				pbbItem.setSiteID(mPlantList.get(index).getPlantID());
				pbbItem.setCommonName(mPlantList.get(index).getCommonName());
				pbbItem.setScienceName(mPlantList.get(index).getSpeciesName());
				pbbItem.setIsFlicker(HelperValues.IS_FLICKR_YES);

				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
				
				mContext.startActivity(intent);
			}
			else {
				Intent intent = new Intent(mContext, GetPhenophaseShared.class);
				intent.putExtra("id", mPlantList.get(index).getPlantID());
				//intent.putExtra("id", );
				mContext.startActivity(intent);
			}
		}
		// If the marker is from other users' list
		else if(mPlantList.get(index).getWhichList() == HelperValues.OTHERS_PLANT_LIST){
			// move to species Info.
			Intent intent = new Intent(mContext, SpeciesDetailMap.class);
			
			PBBItems pbbItem = new PBBItems();
			pbbItem.setSpeciesID(mPlantList.get(index).getSpeciesID());
			pbbItem.setProtocolID(mPlantList.get(index).getProtocolID());
			pbbItem.setSiteID(mPlantList.get(index).getPlantID());
			pbbItem.setCommonName(mPlantList.get(index).getCommonName());
			pbbItem.setScienceName(mPlantList.get(index).getSpeciesName());
			pbbItem.setDate(mPlantList.get(index).getDate());
			pbbItem.setNote(mPlantList.get(index).getNote());
			pbbItem.setCategory(mPlantList.get(index).getCategory());
			pbbItem.setPhenophaseID(mPlantList.get(index).getPhenoID());
			pbbItem.setLatitude(mPlantList.get(index).getLatitude());
			pbbItem.setLongitude(mPlantList.get(index).getLongitude());
			pbbItem.setIsFlicker(HelperValues.IS_FLICKR_YES);
			
			intent.putExtra("pbbItem", pbbItem);
			
			intent.putExtra("username", mPlantList.get(index).getUserName());
			intent.putExtra("imageID", mPlantList.get(index).getImageName());
		
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





