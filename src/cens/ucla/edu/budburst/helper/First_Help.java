package cens.ucla.edu.budburst.helper;

import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.Splash;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class First_Help extends Activity implements OnClickListener{

	private Button next;
	private Button previous;
	private Button done;
	private ViewFlipper vf;
	private int page_num = 0;
	
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
	    
	    previous.setEnabled(false);
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
			vf.showNext();
			if(page_num == 0) {
				previous.setEnabled(false);
				next.setEnabled(true);
			}
			if(page_num == 1) {
				previous.setEnabled(true);
				next.setEnabled(true);
			}
			if(page_num == 2) {
				previous.setEnabled(true);
				next.setEnabled(true);
			}
			if(page_num == 3) {
				previous.setEnabled(true);
				next.setEnabled(false);
			}
			
			//vf.setAnimation(AnimationHelper.inFromRightAnimation());
			//vf.setAnimation(AnimationHelper.outToLeftAnimation());
			
		}
		if(v == previous) {
			page_num--;
			vf.showPrevious();
			if(page_num == 0) {
				previous.setEnabled(false);
				next.setEnabled(true);
			}
			if(page_num == 1) {
				previous.setEnabled(true);
				next.setEnabled(true);
			}
			if(page_num == 2) {
				previous.setEnabled(true);
				next.setEnabled(true);
			}
			if(page_num == 3) {
				previous.setEnabled(true);
				next.setEnabled(false);
			}
			//vf.setAnimation(AnimationHelper.inFromLeftAnimation());
			//vf.setAnimation(AnimationHelper.outToRightAnimation());

		}
		if(v == done) {
			Intent intent = new Intent(First_Help.this, MainPage.class);
			startActivity(intent);
			finish();
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
				Intent service = new Intent(First_Help.this, BackgroundService.class);
			    stopService(service);
			    
				finish();
				return true;
			}
			else if(event.getRepeatCount() == 0 && flag == false){
				Toast.makeText(First_Help.this, getString(R.string.Alert_holdBackExit), Toast.LENGTH_SHORT).show();
				flag = true;
			}
		}
		
		return false;
	}
}
