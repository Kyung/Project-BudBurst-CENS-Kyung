package cens.ucla.edu.budburst.helper;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;

public class MyLocOverlay extends MyLocationOverlay{

	private Context mContext;
	private MapView mMap;
	private MapController mc = null;
	private LocationManager mLocManager;
	private Drawable drawable = null;
	private Paint accuracyPaint;
	private int width;
	private int height;
	private Point center;
	private Point left;
	private double latitude;
	private double longitude;
	private int previousZoomLevel = 9;
	private float radius = 1;
	private float previousRadius = 1;
	private boolean first = true;
	
	public MyLocOverlay(Context context, MapView mapView) {
		super(context, mapView);
		
		mContext = context;
		mMap = mapView;
		mc = mMap.getController();
		// TODO Auto-generated constructor stub	
	}

	@Override
	protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLoc, long when) {
		if(drawable == null) {
			accuracyPaint = new Paint();
			accuracyPaint.setAntiAlias(true);
			accuracyPaint.setStrokeWidth(2.0f);
			
			int resID = mContext.getResources().getIdentifier("cens.ucla.edu.budburst:drawable/mylocation", null, null);
			drawable = mContext.getResources().getDrawable(resID);
			
			width = drawable.getIntrinsicWidth();
			height = drawable.getIntrinsicHeight();
			center = new Point();
			left = new Point();
		}
		
		Projection projection = mapView.getProjection();
		
		latitude = lastFix.getLatitude();
		longitude = lastFix.getLongitude();
		float accuracy = lastFix.getAccuracy();
		
		float[] result = new float[1];
		Location.distanceBetween(latitude, longitude, latitude, longitude+1, result);
		float longitudeLineDistance = result[0];
		
		GeoPoint leftGeo = new GeoPoint((int)(latitude * 1e6), (int)(longitude - accuracy / longitudeLineDistance * 1e6));
		projection.toPixels(leftGeo, left);
		projection.toPixels(myLoc, center);
		
		/*
		float[] result = new float[1];
		
		Location.distanceBetween(latitude, longitude, latitude, longitude+1, result);
		float longitudeLineDistance = result[0];
		
		GeoPoint leftGeo = new GeoPoint((int)(latitude * 1e6), (int)(longitude - accuracy / longitudeLineDistance * 1e6));
		projection.toPixels(leftGeo, left);
		projection.toPixels(myLoc, center);
        //int radius = center.x - left.x;
		
		// we set radius to 1 when when the zoom level is 9.
		int zoomLevel = mMap.getZoomLevel();
		if(zoomLevel != previousZoomLevel) {
			previousZoomLevel = zoomLevel;
		}
		
		if(zoomLevel > 9) {
			radius = (float)(Math.pow(2, (zoomLevel - 9)) * previousRadius); 
		}
		//Toast.makeText(mContext, "" + zoomLevel, Toast.LENGTH_SHORT).show();
		//double levelToMeters = (20-zoomLevel) * 38.31482042;
		 */

        accuracyPaint.setColor(0xff6666ff);
        accuracyPaint.setStyle(Style.STROKE);
        canvas.drawCircle(center.x, center.y, accuracy, accuracyPaint);

        accuracyPaint.setColor(0x186666ff);
        accuracyPaint.setStyle(Style.FILL);
        canvas.drawCircle(center.x, center.y, accuracy, accuracyPaint);

        drawable.setBounds(center.x - width / 2, center.y - height / 2, center.x + width / 2, center.y + height / 2);
        drawable.draw(canvas);
		
	}
	
	@Override
	protected boolean dispatchTap() {
		GeoPoint current_point = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));
		//mc.setCenter(current_point);
		
		Toast.makeText(mContext, "Hello", Toast.LENGTH_SHORT).show();
		//Toast.makeText(mContext, "Current radius : " + radius + "m" , Toast.LENGTH_SHORT).show();
		return true;
	}
}
