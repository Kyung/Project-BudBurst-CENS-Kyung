package cens.ucla.edu.budburst.mapview;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class SpeciesOverlayItem extends OverlayItem {
	private Drawable marker=null;
	private boolean isHeart=false;
	private Drawable heart=null;
	private int mSpeciesID = 0;
	private String mImageUrl;
	private int mCategory = 1;
	private boolean mIsFloracache = false;
	private String mCredit;
	
	public SpeciesOverlayItem(GeoPoint pt, int SpeciesID, String name, String credit,
			String snippet, String imageUrl, Drawable marker, 
			Drawable heart, int category, boolean isFloracache) {
		super(pt, name, snippet);
		mSpeciesID = SpeciesID;
		this.marker=marker;
		this.heart=heart;
		mImageUrl = imageUrl;
		mCategory = category;
		mIsFloracache = isFloracache;
		mCredit = credit;
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
	
	public String getImageUrl() {
		return mImageUrl;
	}
	
	public String getCredit() {
		return mCredit;
	}
	
	public int getSpeciesID() {
		return mSpeciesID;
	}
	
	public int getCategory() {
		return mCategory;
	}
	
	public boolean getIsFloracache() {
		return mIsFloracache;
	}
}	
