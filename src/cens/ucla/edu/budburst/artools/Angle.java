package cens.ucla.edu.budburst.artools;

public class Angle {

	private double pi = Math.PI;

	public double angle(double lat1, double lon1, double lat2, double lon2){
		
		double angle = 0;
		double longitudinalDifference = lon2 - lon1;
		double latitudinalDifference = lat2 - lat1;
		double possibleAzimuth = (pi * .5f) - Math.atan(latitudinalDifference / longitudinalDifference);
		
		if (longitudinalDifference > 0) 
			angle = Math.toDegrees(possibleAzimuth);
		else if (longitudinalDifference < 0) 
			angle = Math.toDegrees(possibleAzimuth + pi);
		else if (latitudinalDifference < 0) 
			angle = Math.toDegrees(pi);		
		
		return (Math.round(angle)*100) /100;
	}
}
