package cens.ucla.edu.budburst.helper;

import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.PBBSplash;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class HelperShowAll extends Activity implements OnClickListener{

	private Button next;
	private Button previous;
	private Button done;
	private ViewFlipper vf;
	private int page_num = 0;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	    
	    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.first_help);
	    
	    // TODO Auto-generated method stub
	    vf = (ViewFlipper) findViewById(R.id.viewflipper);
	    next = (Button) findViewById(R.id.next);
	    previous = (Button) findViewById(R.id.previous);
	    done = (Button) findViewById(R.id.done);
	    
	    slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
	    
	    gestureDetector = new GestureDetector(new MyGestureDetector(this, vf));
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
        	return true;
        }
	    else
	    	return false;
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
    	super.dispatchTouchEvent(event);
    	return gestureDetector.onTouchEvent(event);
    }
	
	public void onResume() {
		super.onResume();
		
		next.setOnClickListener(this);
	    previous.setOnClickListener(this);
	    done.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == next) {
			page_num++;
        	vf.setInAnimation(slideLeftIn);
        	vf.setOutAnimation(slideLeftOut);
        	vf.showNext();
		}
		if(v == previous) {
			page_num--;
        	vf.setInAnimation(slideRightIn);
        	vf.setOutAnimation(slideRightOut);
        	vf.showPrevious();
		}
		if(v == done) {
			new AlertDialog.Builder(HelperShowAll.this)
			.setTitle(getString(R.string.Move_to_Settings_title))
			.setIcon(R.drawable.pbb_icon_small)
			.setMessage(getString(R.string.Move_to_Settings))
			.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					// move to settings page
					Intent intent = new Intent(HelperShowAll.this, HelperSettings.class);
					intent.putExtra("from", HelperValues.FROM_MAIN_PAGE);
					startActivity(intent);
					
					finish();
				}
			})
			.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					// move to main page
					Intent intent = new Intent(HelperShowAll.this, PBBMainPage.class);
					startActivity(intent);
					finish();
				}
			})
			.show();
			
			
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			boolean flag = false;
			if(event.getRepeatCount() == 3) {
				/*
				 * Stop the service if it is still working
				 */
				Intent service = new Intent(HelperShowAll.this, HelperBackgroundService.class);
			    stopService(service);
			    
				finish();
				return true;
			}
			else if(event.getRepeatCount() == 0 && flag == false){
				Toast.makeText(HelperShowAll.this, getString(R.string.Alert_holdBackExit), Toast.LENGTH_SHORT).show();
				flag = true;
			}
		}
		
		return false;
	}
}
