package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import cens.ucla.edu.budburst.GetPhenophase_PBB;
import cens.ucla.edu.budburst.Login;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OneTimeMain extends ListActivity {

	ArrayList<Button> buttonBar = new ArrayList<Button>();
	private MyListAdapter mylistapdater;
	private SharedPreferences pref;
	private String imagePath = "";
	private double latitude = 0.0;
	private double longitude = 0.0;
	private String dt_taken = null;
	final private int PLANT_LIST = 99;
	private int SELECT_PLANT_NAME = 100;
	private int WILD_FLOWERS = 0;
	private int GRASSES = 1;
	private int DECIDUOUS_TREES = 2;
	private int EVERGREEN_TREES = 3;
	private int CONIFERS = 4;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	       
	    setContentView(R.layout.onetimemain);
	    
	    pref = getSharedPreferences("userinfo",0);
	    SharedPreferences.Editor edit = pref.edit();				
		edit.putString("visited","false");
		edit.commit();
		
		Intent p_intent = getIntent();
		
		// if previous activity is "PlantList.java"
		if(p_intent.getExtras().getInt("FROM") == PLANT_LIST) {
			latitude = 0.0;
			longitude = 0.0;
			imagePath = "none";
			dt_taken = "";
		}
		// else
		else {
			imagePath = p_intent.getExtras().getString("imagePath");
			latitude = p_intent.getExtras().getDouble("latitude");
			longitude = p_intent.getExtras().getDouble("longitude");
			dt_taken = p_intent.getExtras().getString("dt_taken");
			
		}
		
		Log.i("K", "Image Path : " + imagePath + " , lat : " + latitude + " lon : " + longitude);
		
	    // TODO Auto-generated method stub
	}
	

	public void onResume() {
		super.onResume();
		
		//My plant button
		/*
		Button buttonMyplant = (Button)findViewById(R.id.myplant);
		buttonMyplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(OneTimeMain.this, PlantList.class);
				startActivity(intent);
			}
		});
		*/
		ArrayList<oneTime> onetime_title = new ArrayList<oneTime>();
		oneTime otime;
		
		otime = new oneTime("Select Plant Name", "Unknown / Edit", "", "");
		onetime_title.add(otime);
		
		//otime = new oneTime("none", "Community Plants", "", "");
		//onetime_title.add(otime);
		
		otime = new oneTime("Recommendation Plants Lists", "Project Budburst", "pbbicon", "Project Budburst");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Local Invasives", "invasive_plant", "Help locate invasive plants");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Local Blooming", "whatsblooming", "Local plants in flower now");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Local Native", "whatsnative", "Native and cultural plants");
		onetime_title.add(otime);

		// What's popular is currently not available
		//otime = new oneTime("none", "What's Popular", "");
		//onetime_title.add(otime);

		mylistapdater = new MyListAdapter(OneTimeMain.this, R.layout.onetime_list ,onetime_title);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);

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
			
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);
			
			img.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/"+arSrc.get(position).image_url, null, null));
			//img.setBackgroundResource(R.drawable.shapedrawable);
			
			
			TextView header_view = (TextView) convertView.findViewById(R.id.list_header);
			TextView title_view = (TextView) convertView.findViewById(R.id.list_name);
			TextView title_desc = (TextView) convertView.findViewById(R.id.list_name_detail);
			
			
			if(!arSrc.get(position).header.equals("none")) {
				header_view.setText(" " + arSrc.get(position).header);
				header_view.setVisibility(View.VISIBLE);
			}
			else {
				header_view.setVisibility(View.GONE);
			}
			
			Log.i("K", "TITLE : " + arSrc.get(position).title);
			
			title_view.setText(arSrc.get(position).title);
			title_desc.setText(arSrc.get(position).description + " ");
	
			return convertView;
		}
	}
	
	class oneTime{	
		oneTime(String aHeader, String aTitle, String aImage_url, String aDescription){
			header = aHeader;
			title = aTitle;
			image_url = aImage_url;
			description = aDescription;
		}
		
		String header;
		String title;
		String image_url;
		String description;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		if(position == 0) {
				
				new AlertDialog.Builder(OneTimeMain.this)
				.setTitle(getString(R.string.OneTime_category))
				.setIcon(android.R.drawable.ic_menu_more)
				.setItems(R.array.category, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] category = getResources().getStringArray(R.array.category);
						StaticDBHelper staticDBHelper = new StaticDBHelper(OneTimeMain.this);
						SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase(); 
						
						Cursor cursor = null;

						if(category[which].equals(getString(R.string.Wild_Flowers))) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + WILD_FLOWERS + " LIMIT 1;",null);
						}
						else if(category[which].equals(getString(R.string.Grass))) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + GRASSES + " LIMIT 1;",null);
						}
						else if(category[which].equals(getString(R.string.Deciduous_Trees_and_Shrubs))) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + DECIDUOUS_TREES + " LIMIT 1;",null);
						}
						else if(category[which].equals(getString(R.string.Evergreen_Trees_and_Shrubs))) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + EVERGREEN_TREES + " LIMIT 1;",null);
						}
						else if(category[which].equals(getString(R.string.Conifer))) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + CONIFERS + " LIMIT 1;",null);
						}
						else {
						}
						
						while(cursor.moveToNext()){
							Integer protocol_id = cursor.getInt(3);
										
							Intent intent = new Intent(OneTimeMain.this, GetPhenophase_PBB.class);
							intent.putExtra("cname", "");
							intent.putExtra("sname", "");
							intent.putExtra("site_id", 0);
							intent.putExtra("protocol_id", protocol_id);
							intent.putExtra("species_id", "");
							intent.putExtra("imagePath", imagePath);
							intent.putExtra("FROM", SELECT_PLANT_NAME);
							intent.putExtra("imagePath", imagePath);
							intent.putExtra("latitude", latitude);
							intent.putExtra("longitude", longitude);
							
							startActivity(intent);
						}
						
						cursor.close();
						staticDB.close();
						staticDBHelper.close();
						
					}
				})
				.setNegativeButton("Back", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();
		}
		else {
			
			Intent intent = null;
	
			switch(position) {
			case 1:
				intent = new Intent(OneTimeMain.this, Flora_Observer.class);
				intent.putExtra("imagePath", imagePath);
				intent.putExtra("latitude", latitude);
				intent.putExtra("longitude", longitude);
				intent.putExtra("dt_taken", dt_taken);
				startActivity(intent);
				break;
			case 2:
		    	intent = new Intent(OneTimeMain.this, Whatsinvasive.class);
				startActivity(intent);
				break;
			case 3:
				intent = new Intent(OneTimeMain.this, Whatsinvasive.class);
				startActivity(intent);
				break;
			case 4:
				Toast.makeText(OneTimeMain.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
				break;
			}
			
		}
	}

	///////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0,"Help").setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 2, 0, "Update").setIcon(android.R.drawable.ic_menu_help);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case 1:
				return true;
			case 2:
				return true;
		}
		return false;
	}
}











