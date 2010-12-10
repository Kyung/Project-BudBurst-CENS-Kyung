package cens.ucla.edu.budburst;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

public class firstActivity extends Activity{
	 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

	    setContentView(R.layout.splash);
	    // TODO Auto-generated method stub

	    new Handler().postDelayed(new Runnable(){
	    	public void run() {
	    		Intent intent = new Intent(firstActivity.this, Splash.class);
				finish();
				startActivity(intent);
	    	}
	    }, 2500);

	}
}
