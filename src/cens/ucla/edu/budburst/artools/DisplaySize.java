package cens.ucla.edu.budburst.artools;

import android.app.Activity;

public class DisplaySize {

	private int width;
	private int height;
	Activity activity;
	
	public DisplaySize(Activity activity) {
		this.activity = activity;
	}

	public int getWidth() {
		return activity.getWindowManager().getDefaultDisplay().getWidth();
	}

	public int getHeight() {
		return activity.getWindowManager().getDefaultDisplay().getHeight();
	}	
}
