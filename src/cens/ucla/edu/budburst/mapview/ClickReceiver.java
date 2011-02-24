package cens.ucla.edu.budburst.mapview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.location.Location;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class ClickReceiver extends Overlay{
	private Context context;
	private GeoPoint myLoc;
	private GeoPoint fingerLoc = null;
	private double Radius = 6371000;
	
	public ClickReceiver(Context _context, GeoPoint myLocation) {
		context = _context;
		myLoc = myLocation;
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {

		fingerLoc = p;
		double distance = CalculationByDistance(myLoc, p);
		
		Toast.makeText(context, (int) distance + " meters away", Toast.LENGTH_SHORT).show();
	
		return true;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if(fingerLoc != null) {
			
			Projection projection = mapView.getProjection();
			Point pt = new Point();
			projection.toPixels(myLoc, pt);
			
			Point pt2 = new Point();
			projection.toPixels(fingerLoc, pt2);
			
			Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			linePaint.setStyle(Style.STROKE);
			linePaint.setStrokeWidth(2);
			linePaint.setColor(0xff6666ff);
			
			canvas.drawLine((float)pt.x, (float)pt.y, (float)pt2.x, (float)pt2.y, linePaint);
			
			super.draw(canvas, mapView, shadow);
		}
	}
	
	public double CalculationByDistance(GeoPoint gp1, GeoPoint gp2) {
		
		 double lat1 = gp1.getLatitudeE6()/1E6;		 
	     double lat2 = gp2.getLatitudeE6()/1E6;	 
	     double lon1 = gp1.getLongitudeE6()/1E6;
	     double lon2 = gp2.getLongitudeE6()/1E6;
	 	 double dLat = Math.toRadians(lat2-lat1);	 
	     double dLon = Math.toRadians(lon2-lon1);
	 	 double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	 
	     Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	 
	     Math.sin(dLon/2) * Math.sin(dLon/2);
	 
	     double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	 
	     return Radius * c;
	}
}
