package cens.ucla.edu.budburst.artools;

public class ARObject {
	
	private ARLocation arLocation;
	private ARViewObject arView;
	private String arName;
	private String arDescription;	
	
	public ARLocation getArLocation() {
		return arLocation;
	}
	public void setArLocation(ARLocation arLocation) {
		this.arLocation = arLocation;
	}
	public String getArName() {
		return arName;
	}
	public void setArName(String arName) {
		this.arName = arName;
	}
	public String getArDescription() {
		return arDescription;
	}
	public void setArDescription(String arDescription) {
		this.arDescription = arDescription;
	}
	public ARViewObject getArViewObject() {
		return arView;
	}
	public void setArViewObject(ARViewObject arView) {
		this.arView = arView;		
	}
}
