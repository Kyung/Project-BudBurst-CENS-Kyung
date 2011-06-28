package cens.ucla.edu.budburst.mapview;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cens.ucla.edu.budburst.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


/**
 * Declare a separate class which extends MapView class
 * 
 * @author kyunghan
 *
 */
public class MapCustomView extends MapView{

	// set the long press value
	private boolean mLongPress;
	
	// Time in ms before the OnLongpressListener is triggered.
	private static final int LONGPRESS_THRESHOLD = 700;
	private Timer mLongPressTimer = new Timer();
    private MapCustomView.OnLongpressListener mLongpressListener;
    private GeoPoint mLastMapCenter;
    
	public MapCustomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public MapCustomView(Context context, String apiKey) {
		super(context, apiKey);
	}
	
	public MapCustomView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	// Define the interface we will interact with from our Map
	public interface OnLongpressListener {
        public void onLongpress(MapView view, GeoPoint longpressLocation);
    }
    
    public void setOnLongpressListener(MapCustomView.OnLongpressListener listener) {
    	mLongpressListener = listener;
    }
    
    
    // This method is called every time user touches the map,
    // drags a finger on the map, or removes finger from the map.
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		handleLongPress(event);
		return super.onTouchEvent(event);
	}
	
	/**
     * This method takes MotionEvents and decides whether or not
     * a longpress has been detected. This is the meat of the
     * OnLongpressListener.
     *
     * The Timer class executes a TimerTask after a given time,
     * and we start the timer when a finger touches the screen.
     *
     * We then listen for map movements or the finger being
     * removed from the screen. If any of these events occur
     * before the TimerTask is executed, it gets cancelled. Else
     * the listener is fired.
     *
     * @param event
     */
	private void handleLongPress(final MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			mLongPressTimer = new Timer();
			mLongPressTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					GeoPoint longPressLoc = getProjection().fromPixels((int)event.getX(), (int)event.getY());
					
					/**
					* Fire the listener. We pass the map location
					* of the longpress as well, in case it is needed
					* by the caller.
					* 
					*/
					mLongpressListener.onLongpress(MapCustomView.this, longPressLoc);
					
				}

			}, LONGPRESS_THRESHOLD);
			
			mLastMapCenter = getMapCenter();
		}
		
		if(event.getAction() == MotionEvent.ACTION_MOVE) {
			if (!getMapCenter().equals(mLastMapCenter)) {
				// User is panning the map, this is no longpress
				mLongPressTimer.cancel();
			}
			
			mLastMapCenter = getMapCenter();
		}
		
		if(event.getAction() == MotionEvent.ACTION_UP) {
			mLongPressTimer.cancel();
		}
	}	
}
