package cens.ucla.edu.budburst.helper;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;

public class MyLocOverlay extends MyLocationOverlay{

	private Context mContext;
	private MapView mMap;
	private LocationManager mLocManager;
	private Drawable drawable;
	private Paint accuracyPaint;
	private int width;
	private int height;
	private Point center;
	private Point left;
	
	public MyLocOverlay(Context context, MapView mapView) {
		super(context, mapView);
		
		mContext = context;
		mMap = mapView;
		// TODO Auto-generated constructor stub
	}
	
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
		
		double latitude = lastFix.getLatitude();
		double longitude = lastFix.getLongitude();
		float accuracy = lastFix.getAccuracy();
		
		float[] result = new float[1];
		
		Location.distanceBetween(latitude, longitude, latitude, longitude+1, result);
		float longitudeLineDistance = result[0];
		
		GeoPoint leftGeo = new GeoPoint((int)(latitude * 1e6), (int)(longitude - accuracy / longitudeLineDistance * 1e6));
		projection.toPixels(leftGeo, left);
		projection.toPixels(myLoc, center);
        int radius = center.x - left.x;

        accuracyPaint.setColor(0xff6666ff);
        accuracyPaint.setStyle(Style.STROKE);
        canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

        accuracyPaint.setColor(0x186666ff);
        accuracyPaint.setStyle(Style.FILL);
        canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

        drawable.setBounds(center.x - width / 2, center.y - height / 2, center.x + width / 2, center.y + height / 2);
        drawable.draw(canvas);
		
	}
	
	@Override
	protected boolean dispatchTap() {
		Toast.makeText(mContext, "This is my Location!", Toast.LENGTH_SHORT).show();
		return true;
	}
}
