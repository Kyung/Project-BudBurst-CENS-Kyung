package cens.ucla.edu.budburst.artools;

import android.location.Location;

public class ARLocationProvider {
		
	ARLocation coorArLocation;		
	
	public ARLocationProvider(Location cLocation, ARObject ARObject) {		
		coorArLocation = new ARLocation();
		ARLocation arLocation = ARObject.getArLocation();
		
		double curLatitude = cLocation.getLatitude();
		double curLongitude = cLocation.getLongitude();
		
		coorArLocation.latitude 	= arLocation.latitude;
		coorArLocation.longitude 	= arLocation.longitude;
		coorArLocation.distance 	= new Distance().distance(curLatitude, curLongitude, coorArLocation.latitude, coorArLocation.longitude);
		coorArLocation.angle		= new Angle().angle(curLatitude, curLongitude, coorArLocation.latitude, coorArLocation.longitude);
	}

	public ARLocation getARLoction() {
		return coorArLocation;
	}
	
}