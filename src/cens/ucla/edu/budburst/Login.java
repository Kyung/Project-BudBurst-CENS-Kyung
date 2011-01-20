package cens.ucla.edu.budburst;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Kyung
 *
 */
public class Login extends Activity{
	private static final String TAG = "LoginActivity";
	private int FROM_MAIN_PAGE = 103;
	private TextView textUsername;
	private TextView textPassword;
	private SharedPreferences pref;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
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
				String Username = textUsername.getText().toString();
				String Password = textPassword.getText().toString();
				
				if(Username.equals("") || Password.equals("")){
					Toast.makeText(Login.this, getString(R.string.Alert_wrongUserPass),Toast.LENGTH_SHORT).show();	
				}else{

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
	
	@Override
	public void onResume(){
		super.onResume();
	}
}
