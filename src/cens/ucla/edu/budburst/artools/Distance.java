package cens.ucla.edu.budburst.artools;

public class Distance {
    
	private double pi = 3.1415926535;    

    public double distance(double lat1,double lon1, double lat2, double lon2){

                  double theta, dist;
                  theta = lon1-lon2;
                  dist  = Math.sin(deg2rad(lat1))*Math.sin(deg2rad(lat2))+
                  Math.cos(deg2rad(lat1))*Math.cos(deg2rad(lat2))*Math.cos(deg2rad(theta));
                  dist = Math.acos(dist);
                  dist = rad2deg(dist);
                  dist = dist*60*1.1515;

                  //km
                  //dist = dist * 1.609344;                  
                  return dist;
    }
    private double deg2rad(double deg){
                  return (deg*pi/180);
    }
    private double rad2deg(double rad){
                  return (rad*180/pi);
    }
}
