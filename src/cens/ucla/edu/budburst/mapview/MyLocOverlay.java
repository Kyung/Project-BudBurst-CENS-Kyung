package cens.ucla.edu.budburst.mapview;

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
	private MapView mMapView;
	private MapController mMapController = null;
	private LocationManager mLocManager;
	private Drawable mDrawable = null;
	private Paint mAccuracyPaint;
	private int mWidth;
	private int mHeight;
	private Point mCenter;
	private Point mLeft;
	private double mLatitude;
	private double mLongitude;
	private int previousZoomLevel = 9;
	private int mRadius;
	private float previousRadius = 1;
	private boolean first = true;
	
	public MyLocOverlay(Context context, MapView mapView) {
		super(context, mapView);
		
		mContext = context;
		mMapView = mapView;
		mMapController = mMapView.getController();
		// TODO Auto-generated constructor stub	
	}

	@Override
	protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLoc, long when) {
		if(mDrawable == null) {
			mAccuracyPaint = new Paint();
			mAccuracyPaint.setAntiAlias(true);
			mAccuracyPaint.setStrokeWidth(2.0f);
			
			int resID = mContext.getResources().getIdentifier("cens.ucla.edu.budburst:drawable/mylocation", null, null);
			mDrawable = mContext.getResources().getDrawable(resID);
			
			mWidth = mDrawable.getIntrinsicWidth();
			mHeight = mDrawable.getIntrinsicHeight();
			mCenter = new Point();
			mLeft = new Point();
		}
		
		Projection projection = mapView.getProjection();
		
		mLatitude = lastFix.getLatitude();
		mLongitude = lastFix.getLongitude();
		float accuracy = lastFix.getAccuracy();
		
		float[] result = new float[1];
		Location.distanceBetween(mLatitude, mLongitude, mLatitude, mLongitude+1, result);
		float longitudeLineDistance = result[0];
		
		GeoPoint leftGeo = new GeoPoint((int)(mLatitude * 1E6), 
				(int)(mLongitude - accuracy / longitudeLineDistance * 1E6));
		projection.toPixels(leftGeo, mLeft);
		projection.toPixels(myLoc, mCenter);
		
		mRadius = mCenter.x - mLeft.x;
		//Toast.makeText(mContext, "" + zoomLevel, Toast.LENGTH_SHORT).show();
		//double levelToMeters = (mapView.getMaxZoomLevel() - mapView.getZoomLevel()) * 38.31482042;
		
		//Log.i("K", "levelToMeters : " + levelToMeters);

		mAccuracyPaint.setColor(0xff6666ff);
		mAccuracyPaint.setStyle(Style.STROKE);
        canvas.drawCircle(mCenter.x, mCenter.y, 6, mAccuracyPaint);

        mAccuracyPaint.setColor(0x186666ff);
        mAccuracyPaint.setStyle(Style.FILL);
        canvas.drawCircle(mCenter.x, mCenter.y, 6, mAccuracyPaint);

        mDrawable.setBounds(mCenter.x - mWidth / 2, mCenter.y - mHeight / 2, mCenter.x + mWidth / 2, mCenter.y + mHeight / 2);
        mDrawable.draw(canvas);
        
        Paint backPaint = new Paint();
		backPaint.setARGB(50, 50, 50, 50);
		backPaint.setAntiAlias(true);
		
	}
	
	/* Draw a circle */
	private void drawCircle(Canvas c, int color, GeoPoint center, 
							float radius, int alpha, boolean border) {
		
		Point scCoord = mMapView.getProjection().toPixels(center, null);
		float r = mMapView.getProjection().metersToEquatorPixels(radius);
		 
		Paint p = new Paint();
        p.setStyle(Style.FILL);
		p.setColor(color);
		p.setAlpha(alpha);
		p.setAntiAlias(true);
		c.drawCircle(scCoord.x, scCoord.y, r, p);
		
		if(border) { //Draw border
			p.setStyle(Style.STROKE);
			p.setColor(color);
			p.setAlpha(100);
			p.setStrokeWidth(1.2F);
			p.setAntiAlias(true);
			c.drawCircle(scCoord.x, scCoord.y,r, p);
		}
	}

	@Override
	protected boolean dispatchTap() {
		GeoPoint current_point = new GeoPoint((int)(mLatitude * 1000000), (int)(mLongitude * 1000000));
		//mc.setCenter(current_point);
		//Toast.makeText(mContext, "Current radius : " + radius + "m" , Toast.LENGTH_SHORT).show();
		return true;
	}
}
