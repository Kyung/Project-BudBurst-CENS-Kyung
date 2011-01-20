package cens.ucla.edu.budburst.mapview;

import java.util.List;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.onetime.HelloItemizedOverlay;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

public class SpeciesDetail_inMapView extends MapActivity {

	private String latitude;
	private String longitude;
	private HelloItemizedOverlay itemizedOverlay = null;
	private List<Overlay> mapOverlays = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.wpinfo);
	    
	    Intent intent = getIntent();
	    
		String cname = intent.getExtras().getString("common_name");
		latitude = intent.getExtras().getString("latitude");
		longitude = intent.getExtras().getString("longitude");
		String dt_taken = intent.getExtras().getString("dt_taken");
		String pheno = intent.getExtras().getString("pheno");
		
		TextView snameTxt = (TextView) findViewById(R.id.science_name);
		TextView cnameTxt = (TextView) findViewById(R.id.common_name);
		TextView dttakenTxt = (TextView) findViewById(R.id.dt_taken);
		TextView geoTxt = (TextView) findViewById(R.id.geodata);
		MapView myMap = (MapView) findViewById(R.id.simpleGM_map);
		
		cnameTxt.setText(" " + cname + " ");
		dttakenTxt.setText(" " + dt_taken + " ");
		
		// need to multiply 1000000 to get the proper data
		GeoPoint p = new GeoPoint((int)(Double.parseDouble(latitude) * 1000000), (int)(Double.parseDouble(longitude) * 1000000));
		//myMap.setBuiltInZoomControls(true);
		myMap.setClickable(false);
		MapController mc = myMap.getController();
		mc.animateTo(p);
		mc.setZoom(14);
		
		mapOverlays = myMap.getOverlays();
		Drawable marker = getResources().getDrawable(R.drawable.marker);
		itemizedOverlay = new HelloItemizedOverlay(marker, this);
		
		OverlayItem overlayitem = new OverlayItem(p, "spot", "Species found!");
		itemizedOverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedOverlay);
		
		myMap.setSatellite(false);
		myMap.setBackgroundResource(R.drawable.shapedrawable);
		
		//commentTxt.setText(comment);
	
	    // TODO Auto-generated method stub
	}
	
    // or when user press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
	    	// if there's a overlay, should call disableCompass() explicitly!!!!
			finish();
			return true;
		}
		return false;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
