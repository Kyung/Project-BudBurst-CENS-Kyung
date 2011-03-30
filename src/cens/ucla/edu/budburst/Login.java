package cens.ucla.edu.budburst;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.BackgroundService;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.onetime.Whatsinvasive;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Kyung
 *
 */
public class Login extends Activity{
	private static final String TAG = "LoginActivity";
	private ProgressDialog dialog1 = null;
	private TextView textUsername;
	private TextView textPassword;
	private String Username = "";
	private String Password = "";
	private SharedPreferences pref;
	private boolean loginValid = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		/*
    	 * 
    	 * Start two services
    	 *  - 1. Tree Lists service
    	 *  - 2. Local Lists from USDA
    	 * 
    	 */
    	
	    Intent service = new Intent(Login.this, BackgroundService.class);
	    startService(service);
		
		
		OneTimeDBHelper onetime = new OneTimeDBHelper(Login.this);
		
		textUsername = (TextView)findViewById(R.id.username_text);
		textPassword = (TextView)findViewById(R.id.password_text);
		
		//Define Preferences to store username and password
		pref = getSharedPreferences("userinfo", 0);
		
		if(	!(pref.getString("Username","").equals("")) && !(pref.getString("Password","").equals(""))){
			Intent intent = new Intent(Login.this, Sync.class);
			intent.putExtra("from", 0);
			Login.this.startActivity(intent);
			finish();
		}

		//Login button
		Button buttonLogin = (Button)findViewById(R.id.login_button);
		buttonLogin.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Username = textUsername.getText().toString().trim();
				Password = textPassword.getText().toString().trim();
				
				if(Username.equals("") || Password.equals("")){
					Toast.makeText(Login.this, getString(R.string.Alert_wrongUserPass),Toast.LENGTH_SHORT).show();	
				}else{
					new AsyncLogin().execute(getString(R.string.authentication) + "?username=" + Username + "&password=" + Password);
				}
			}
		}		
		);
		
		//Test login button
		Button buttonTestLogin = (Button)findViewById(R.id.test_login_button);
		buttonTestLogin.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){

				//Set id/pwd as test/test
				SharedPreferences.Editor edit = pref.edit();				
				edit.putString("Username","test10");
				edit.putString("Password","test10");
				edit.putBoolean("Preview", true);
				edit.commit();
				
				show_dialog();
			}
		}
		);
		
		//Sign up button
		Button buttonSignUp = (Button)findViewById(R.id.signup_button);
		buttonSignUp.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){

				//Make intent web browser for sign up
				Intent intent = new Intent (Intent.ACTION_VIEW);
				intent.setData(Uri.parse(getString(R.string.signupURL)));
				startActivity(intent);
			}
		}
		);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        stopService(new Intent(Login.this, BackgroundService.class));
	        finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	public void show_dialog() {
		new AlertDialog.Builder(Login.this)
		.setTitle(getString(R.string.Preview_Mode))
		.setMessage(getString(R.string.Preview_Mode_Description))
		.setPositiveButton(getString(R.string.Preview_Mode_Done), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Login.this, Sync.class);
				intent.putExtra("from", 0);
				startActivity(intent);
				finish();
			}
		})
		.show();
	}	
	
	class AsyncLogin extends AsyncTask<String, Integer, Void> {
		
		protected void onPreExecute() {
			dialog1 = ProgressDialog.show(Login.this, "Loading...", "Logging in", true);
		}
		@Override
		protected Void doInBackground(String... url) {
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url[0]);
			String result = "";
			
			try {
				HttpResponse response = httpClient.execute(httpPost);
				
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					
					StringBuilder result_str = new StringBuilder();
					for(;;){
						String line = br.readLine();
						if (line == null) 
							break;
						result_str.append(line+'\n');
					}
					result = result_str.toString();
					JSONObject jsonobj = new JSONObject(result);
					
					if(jsonobj.getBoolean("success") == false){
						loginValid = false;
					}
					else {
						loginValid = true;
						SharedPreferences.Editor edit = pref.edit();				
						edit.putString("Username",Username.trim());
						edit.putString("Password",Password.trim());
						edit.commit();
						
						Intent intent = new Intent(Login.this, Sync.class);
						intent.putExtra("from", 0);
						Login.this.startActivity(intent);
						finish();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// TODO Auto-generated method stub
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			if(!loginValid) {
				Toast.makeText(Login.this, getString(R.string.InValid_ID_PW), Toast.LENGTH_SHORT).show();
			}
			dialog1.dismiss();
		}
	}
}
