package cens.ucla.edu.budburst;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;

public class Splash extends Activity {

	final private int FROM_MAIN_PAGE = 103;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		SharedPreferences pref = getSharedPreferences("userinfo",0);
		String synced = pref.getString("Synced", "false");
		
		//Check login
		if(	!(pref.getString("Username","").equals("")) && !(pref.getString("Password","").equals(""))){
			if(synced.equals("true")){
				Intent intent = new Intent(Splash.this, MainPage.class);
				startActivity(intent);
				finish();
			}
			else{
				Intent intent = new Intent(Splash.this, Sync.class);
				intent.putExtra("from", 0);
				startActivity(intent);
				finish();
			}
		}
		else{
			Intent intent = new Intent(Splash.this, Login.class);
			Splash.this.startActivity(intent);
			finish();
		}
	    
	}
	
    // or when user press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			finish();
			return true;
		}
		return false;
	}
}
