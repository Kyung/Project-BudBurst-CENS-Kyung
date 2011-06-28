package cens.ucla.edu.budburst.mapview;

import com.google.android.maps.MapView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class PopupPanel {
	private View mPopup;
	private boolean mIsVisible = false;
	private LayoutInflater mInflater;
	private Context mContext;
	private MapView mMap;

	public PopupPanel(Context context, MapView map, int layout) {
		
		mContext = context;
		mMap = map;
		
		ViewGroup parent = (ViewGroup)map.getParent();
		mInflater = LayoutInflater.from(context);
		mPopup = mInflater.inflate(layout, parent, false);

	
		mPopup.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hide();
			}
		});
	}

	public View getView() {
		return(mPopup);
	}

	public void show() {
		RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(
			RelativeLayout.LayoutParams.FILL_PARENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT
		);
	
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp.setMargins(20, 40, 20, 0);
		
		
		hide();
		
		((ViewGroup)mMap.getParent()).addView(mPopup, lp);
		mIsVisible=true;
	}
	
	public void hide() {
		if (mIsVisible) {
			mIsVisible=false;
			((ViewGroup)mPopup.getParent()).removeView(mPopup);
		}
	}
}
