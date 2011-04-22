package cens.ucla.edu.budburst.artools;

import android.graphics.Bitmap;

public class ARViewObject {

	private ARPosition position;
	private Bitmap icon;
	private boolean visible;
	
	public ARViewObject() {
		
		position = new ARPosition();
		initialize();
		visible = false;
		
	}
	public void initialize(){
		
	}
	
	public ARPosition getPosition() {
		return position;
	}
	public void setPosition(ARPosition position) {
		this.position = position;
	}
	public Bitmap getIcon() {
		return icon;
	}
	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}	
}
