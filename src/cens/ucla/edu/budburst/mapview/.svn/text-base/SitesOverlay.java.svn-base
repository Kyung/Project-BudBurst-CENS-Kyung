package cens.ucla.edu.budburst.mapview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class SitesOverlay extends ItemizedOverlay<OverlayItem> {

	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private Drawable marker = null;
	private Context mContext;
	private Double mLatitude = 0.0;
	private Double mLongitude = 0.0;
	
	public SitesOverlay(Context context, Drawable marker) {
		super(marker);
		this.marker = marker;
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return (items.get(i));
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return (items.size());
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
		boundCenter(marker);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		
		final int action = event.getAction();
		
		if(action == MotionEvent.ACTION_UP) {
			items.clear();
			//Convert the touched point to geoPoint
			Projection proj = mapView.getProjection();
			GeoPoint loc = proj.fromPixels((int)event.getX(), (int)event.getY());
			
			mLatitude = loc.getLatitudeE6() / 1E6;
			mLongitude = loc.getLongitudeE6() / 1E6;
			
			items.add(new OverlayItem(loc, "", ""));
		}
		populate();
		
		return true;
	}
	
	public double getLatitude() {
		return mLatitude;
	}
	
	public double getLongitude() {
		return mLongitude;
	}

}
