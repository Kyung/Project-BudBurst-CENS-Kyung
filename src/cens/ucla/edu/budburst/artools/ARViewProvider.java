package cens.ucla.edu.budburst.artools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ARViewProvider {
	
	int displayWidth;
	int displayHeight;
	ARObject arObject;
	//ARViewObject arView;
	ARPosition arPosition;
	Bitmap icon; 
	boolean visible;
	final int limitedAngle = 90;
	
	public ARViewProvider(Activity activity, ARObject arObject) {
		
		this.displayWidth = new DisplaySize(activity).getWidth();
		this.displayHeight = new DisplaySize(activity).getHeight();
		this.arObject = arObject;
		this.arPosition = new ARPosition();
		
		this.icon 	= arObject.getArViewObject().getIcon();//BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon);
		//arView.setIcon(icon);
		
		visible = false;
		
		coordinate();
	}
	public void coordinate() {		
		
		double diffrenceAngle = Math.toRadians(arObject.getArLocation().angle - ARManager.deviceDegree);
		double maxAngle = Math.toRadians(limitedAngle) / 2;
		
		// where to show the images
		double x = displayWidth/2 + ( Math.sin(diffrenceAngle) * displayWidth / (2 * Math.sin( Math.toDegrees(maxAngle) )) );
		arPosition.setX( (int) x );
		arPosition.setY(displayHeight / 2);
		arObject.getArViewObject().setPosition(arPosition);
		
		// 시야각 180도 이상넘어가면 보이지 않음
		// set visible area
		if( Math.toDegrees( (Math.PI / 2.0) ) < Math.abs(arObject.getArLocation().angle - ARManager.deviceDegree) ) 
			visible = false;
		else 
			visible = true;
		
		arObject.getArViewObject().setVisible(this.visible);
	}
	
	public ARObject getARViewObject() {		
		return arObject; 
	}
}
