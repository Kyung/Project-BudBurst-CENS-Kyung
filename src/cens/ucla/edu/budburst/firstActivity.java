package cens.ucla.edu.budburst;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class firstActivity extends Activity{
	 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

	    setContentView(R.layout.splash);
	    // TODO Auto-generated method stub
	    
	    try {
			Thread.sleep(2500);
			Intent intent = new Intent(firstActivity.this, Splash.class);
			finish();
			startActivity(intent);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}
}
