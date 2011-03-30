package cens.ucla.edu.budburst.mapview;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

class SpeciesOverlayItem extends OverlayItem {
	Drawable marker=null;
	boolean isHeart=false;
	Drawable heart=null;
	
	SpeciesOverlayItem(GeoPoint pt, String name, String snippet,
						 Drawable marker, Drawable heart) {
		super(pt, name, snippet);

		this.marker=marker;
		this.heart=heart;
		
	}
	
	@Override
	public Drawable getMarker(int stateBitset) {
		Drawable result=(isHeart ? heart : marker);
		
		setState(result, stateBitset);
	
		return(result);
	}
	
	public void toggleHeart() {
		isHeart=!isHeart;
	}
}	
