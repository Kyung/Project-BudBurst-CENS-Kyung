package cens.ucla.edu.budburst.lists;

import android.content.Context;

public class Items {
	public Items(double latitude, double longitude, int category) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.category = category;
	}
	
	public Items(Context context, double latitude, double longitude, int category) {
		this.context = context;
		this.latitude = latitude;
		this.longitude = longitude;
		this.category = category;
	}
	
	public Context context;
	public double latitude;
	public double longitude;
	public int category;
}
