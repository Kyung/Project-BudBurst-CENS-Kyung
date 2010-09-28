package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import cens.ucla.edu.budburst.Login;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.Whatsinvasive.MyListAdapter;
import cens.ucla.edu.budburst.onetime.Whatsinvasive.species;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OneTimeMain extends ListActivity {

	ArrayList<Button> buttonBar = new ArrayList<Button>();
	private Button observationBtn;
	private Button recommendationBtn;
	private MyListAdapter mylistapdater;
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
		
		ArrayList<oneTime> onetime_title = new ArrayList<oneTime>();
		oneTime otime;
		
		otime = new oneTime("Flora Observer", "One Time Observation", "");
		onetime_title.add(otime);
		
		otime = new oneTime("Recommendation", "What's Blooming", "");
		onetime_title.add(otime);
		otime = new oneTime("none", "What's Invasive", "");
		onetime_title.add(otime);
		otime = new oneTime("none", "What's Native", "");
		onetime_title.add(otime);
		otime = new oneTime("none", "What's Popular", "");
		onetime_title.add(otime);

		
		mylistapdater = new MyListAdapter(OneTimeMain.this, R.layout.onetime_list ,onetime_title);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);
		/*
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
		*/
		
		/*
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
		*/
	}
	
	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<oneTime> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<oneTime> aarSrc){
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}
		
		public int getCount(){
			return arSrc.size();
		}
		
		public String getItem(int position){
			return arSrc.get(position).title;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			/*
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);

			String imagePath = TEMP_PATH + arSrc.get(position).image_url + ".jpg";
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			
			try{
				FileOutputStream out = new FileOutputStream(imagePath);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
			}catch(Exception e){
				Log.e("K", e.toString());
			}
		
			img.setBackgroundResource(R.drawable.shapedrawable);
			img.setImageBitmap(bitmap);
			*/
			
			TextView header_view = (TextView) convertView.findViewById(R.id.list_header);
			TextView title_view = (TextView) convertView.findViewById(R.id.list_name);
			
			
			if(!arSrc.get(position).header.equals("none")) {
				header_view.setText(" " + arSrc.get(position).header);
				header_view.setVisibility(View.VISIBLE);
			}
			else {
				header_view.setVisibility(View.GONE);
			}
			
			Log.i("K", "TITLE : " + arSrc.get(position).title);
			
			title_view.setText(" " + arSrc.get(position).title);
	
			return convertView;
		}
	}
	
	class oneTime{	
		oneTime(String aHeader, String aTitle, String aImage_url){
			header = aHeader;
			title = aTitle;
			image_url = aImage_url;
		}
		
		String header;
		String title;
		String image_url;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		String[] category = getResources().getStringArray(R.array.recommend);
	
		if(position == 0) {
			Intent intent = new Intent(OneTimeMain.this, Flora_Observer.class);
			startActivity(intent);
		}
		else {
			Intent intent = new Intent(OneTimeMain.this, Recommendation.class);
			
			switch(position) {
			case 1:
				intent.putExtra("SelectedList", "What's Blooming");
				break;
			case 2:
				intent.putExtra("SelectedList", "What's Invasive");
				break;
			case 3:
				intent.putExtra("SelectedList", "What's Native");
				break;
			case 4:
				intent.putExtra("SelectedList", "What's Popular");
				break;
			}
			startActivity(intent);
		}

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
				.setTitle("Logout")
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
						
						deleteContents("/sdcard/pbudburst/tmp/");
						
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
	
	void deleteContents(String path) {
		File file = new File(path);
		if(file.isDirectory()) {
			String[] fileList = file.list();
			
			for(int i = 0 ; i < fileList.length ; i++) {
				File newFile = new File(fileList[i]);
				Log.i("K", "FILE NAME : " + "/sdcard/pbudburst/tmp/" + fileList[i] + " IS DELETED.");
				new File("/sdcard/pbudburst/tmp/" + fileList[i]).delete();
			}
		}
	}
}











