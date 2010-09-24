package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;

import cens.ucla.edu.budburst.Login;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class OneTimeMain extends Activity {

	ArrayList<Button> buttonBar = new ArrayList<Button>();
	private Button observationBtn;
	private Button recommendationBtn;
	private SharedPreferences pref;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.onetimemain);
	    
	    pref = getSharedPreferences("userinfo",0);
	    SharedPreferences.Editor edit = pref.edit();				
		edit.putString("visited","false");
		edit.commit();
	    
	    // TODO Auto-generated method stub
	}
	
	public void onResume() {
		super.onResume();
		
		//My plant button
		Button buttonMyplant = (Button)findViewById(R.id.myplant);
		buttonMyplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(OneTimeMain.this, PlantList.class);
				startActivity(intent);
			}
		});
		
		/*
		//Shared plant button
		Button buttonSharedplant = (Button)findViewById(R.id.sharedplant);
		buttonSharedplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
			}
		});
		
		buttonSharedplant.setSelected(true);
		*/
		
		buttonBar.add((Button) this.findViewById(R.id.observation_btn));
		buttonBar.get(0).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(OneTimeMain.this)
					.setTitle("Select Category")
					.setIcon(android.R.drawable.ic_menu_more)
					.setItems(R.array.category, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String[] category = getResources().getStringArray(R.array.category);
							
							if(category[which].equals("Others")) {
								Intent intent = new Intent(OneTimeMain.this, GetSpeciesInfo.class);
								intent.putExtra("cname", "Others");
							    intent.putExtra("sname", "Others");
							    intent.putExtra("protocol_id", 0);
								startActivity(intent);
							}
							else {
								Intent intent = new Intent(OneTimeMain.this, Observation.class);
								intent.putExtra("SelectedList", category[which]);
								startActivity(intent);
							}
						}
					})
					.setNegativeButton("Back", null)
					.show();
			}		
		});
		
		
		buttonBar.add((Button) this.findViewById(R.id.recommend_btn));
		buttonBar.get(1).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(OneTimeMain.this)
				.setTitle("Select Category")
				.setIcon(android.R.drawable.ic_menu_more)
				.setItems(R.array.recommend, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] category = getResources().getStringArray(R.array.recommend);
						
						Intent intent = new Intent(OneTimeMain.this, Recommendation.class);
						intent.putExtra("SelectedList", category[which]);
						startActivity(intent);
					}
				})
				.setNegativeButton("Back", null)
				.show();
	
			}		
		});
	}
	
	///////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0,"Queue").setIcon(android.R.drawable.ic_menu_sort_by_size);
		menu.add(0, 2, 0, "Help").setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 3, 0, "Log out").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case 1:
				intent = new Intent(OneTimeMain.this, Queue.class);
				startActivity(intent);
				return true;
			case 2:
				return true;
			case 3:
				new AlertDialog.Builder(OneTimeMain.this)
				.setTitle("Question")
				.setMessage("You might lose your unsynced data if you log out. Do you want to log out?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						pref = getSharedPreferences("userinfo",0);
						SharedPreferences.Editor edit = pref.edit();				
						edit.putString("Username","");
						edit.putString("Password","");
						edit.putString("synced", "false");
						edit.commit();
						
						//Drop user table in database
						SyncDBHelper dbhelper = new SyncDBHelper(OneTimeMain.this);
						OneTimeDBHelper onehelper = new OneTimeDBHelper(OneTimeMain.this);
						dbhelper.clearAllTable(OneTimeMain.this);
						onehelper.clearAllTable(OneTimeMain.this);
						dbhelper.close();
						onehelper.close();
						
						Intent intent = new Intent(OneTimeMain.this, Login.class);
						startActivity(intent);
						finish();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				})
				.show();
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
	
    // or when user press back button
	// when you hold the button for 3 sec, the app will be exited
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			boolean flag = false;
			if(event.getRepeatCount() == 3) {
				Toast.makeText(OneTimeMain.this, "Thank you.", Toast.LENGTH_SHORT).show();
				finish();
				return true;
			}
			else if(event.getRepeatCount() == 0 && flag == false){
				Toast.makeText(OneTimeMain.this, "Hold the Back Button to exit.", Toast.LENGTH_SHORT).show();
				flag = true;
			}
		}
		
		return false;
	}
}
