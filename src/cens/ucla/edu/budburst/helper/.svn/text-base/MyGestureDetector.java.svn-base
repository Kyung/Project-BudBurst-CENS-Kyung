package cens.ucla.edu.budburst.helper;

import cens.ucla.edu.budburst.R;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

public class MyGestureDetector extends SimpleOnGestureListener {
	
	private ViewFlipper viewFlipper;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	private Context context;
	
	public MyGestureDetector(Context context, ViewFlipper vf) {
		this.context = context;
		viewFlipper = vf;
		
		slideLeftIn = AnimationUtils.loadAnimation(context, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(context, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(context, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(context, R.anim.slide_right_out);
	}
		
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            // right to left swipe
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            	Log.i("K", "right to left");
            	viewFlipper.setInAnimation(slideLeftIn);
            	viewFlipper.setOutAnimation(slideLeftOut);
            	viewFlipper.showNext();
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            	Log.i("K", "left to right");
            	viewFlipper.setInAnimation(slideRightIn);
            	viewFlipper.setOutAnimation(slideRightOut);
            	viewFlipper.showPrevious();
            }
        } catch (Exception e) {
            // nothing
        }
        return false;
    }
}
