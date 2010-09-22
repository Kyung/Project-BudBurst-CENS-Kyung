package cens.ucla.edu.budburst;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class firstActivity extends Activity{
	 
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//Retrieve username and password
		SharedPreferences pref = getSharedPreferences("userinfo",0);
		String synced = pref.getString("Synced", "false");
		
		//Check login
		if(	!(pref.getString("Username","").equals("")) && !(pref.getString("Password","").equals(""))){
			if(synced.equals("true")){
				Intent intent = new Intent(firstActivity.this, PlantList.class);
				startActivity(intent);
				finish();
			}
			else{
				Intent intent = new Intent(firstActivity.this, Sync.class);
				startActivity(intent);
				finish();
			}
		}
		else{
			Intent intent = new Intent(firstActivity.this, Login.class);
			firstActivity.this.startActivity(intent);
			finish();
		}
	}
}
