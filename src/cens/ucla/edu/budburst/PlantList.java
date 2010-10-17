package cens.ucla.edu.budburst;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.Helloscr.UpdateThread;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.OneTimeMain;

public class PlantList extends ListActivity {
	
	final private String TAG = "PlantList"; 
	
	private String username;
	private String password;
	private SharedPreferences pref;
	private SyncDBHelper syncDBHelper;
	private StaticDBHelper staticDBHelper;
	private ListView MyList;
	private Button tempBtn;
	private ArrayList<PlantItem> user_species_list;
	private int first_site_id = 0;
	private int pos = 0;
	private static boolean first_site_flag = true;
	private Button buttonSharedplant = null;
	
	//MENU contants
	final private int MENU_ADD_PLANT = 1;
	final private int MENU_ADD_SITE = 2;
	final private int MENU_LOGOUT = 5;
	final private int MENU_SYNC = 6;
	final private int MENU_HELP = 7;
	
	ArrayList<PlantItem> arPlantItem;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plantlist);
		
		// read 'First' in sharedpreferences..
		// this pref will be read only one time at the beginning of the app.
		// since the database is not read properly, call 'PlantList.java' activity again and finish it.
		pref = getSharedPreferences("userinfo",0);
		String first = pref.getString("First","true");
		
		Log.i("K", " FIRST : " + first);
		
		if(first.equals("true")) {
			new AlertDialog.Builder(PlantList.this)
			.setTitle("Sync Done")
			.setIcon(R.drawable.pbbicon_small)
			.setMessage("Database updated.")
			.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(PlantList.this, PlantList.class);
					finish();
					
					SharedPreferences.Editor edit = pref.edit();	
					// set 'First' to false so that it won't be read next time.
					edit.putString("First", "false");
					edit.commit();

					startActivity(intent);
				}
			})
			.show();
		}
	}
	
	public void onResume(){
		super.onResume();

		//tempBtn.setV
		MyList = getListView();
		
		MyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				onListItemClick(arg1, arg2, arg3);
			}
		});
		
		MyList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				return onLongListItemClick(arg1, arg2, arg3);
			}
			
		});
		
		//Initiate ArrayList
		user_species_list = new ArrayList<PlantItem>();
		
		//Retrieve username and password
		pref = getSharedPreferences("userinfo",0);
		username = pref.getString("Username","");
		password = pref.getString("Password","");
		
		/*
		//My plant button
		Button buttonMyplant = (Button)findViewById(R.id.myplant);
		buttonMyplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {}
		});
		
		*/
		
		//Shared plant button
		buttonSharedplant = (Button)findViewById(R.id.sharedplant);
		buttonSharedplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(PlantList.this, OneTimeMain.class);
				startActivity(intent);
			}
		});
		//buttonMyplant.setSelected(true);
		
		arPlantItem = new ArrayList<PlantItem>();
		syncDBHelper = new SyncDBHelper(PlantList.this);
		SQLiteDatabase syncDB  = syncDBHelper.getReadableDatabase();
		
		staticDBHelper = new StaticDBHelper(PlantList.this);
        
	
		try {
        	staticDBHelper.createDataBase();
	 	} catch (IOException ioe) {
	 		Log.e("K", "CREATE DATABASE : " + ioe.toString());
	 		throw new Error("Unable to create database");
	 	}
 
	 	try {
	 		staticDBHelper.openDataBase();
	 	}catch(SQLException sqle){
	 		Log.e("K", "OPEN DATABASE : " + sqle.toString());
	 		throw sqle;
	 	}
	 	
	 	
		SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();

		ArrayList<String> user_station_name = new ArrayList<String>();
		ArrayList<Integer> user_station_id = new ArrayList<Integer>();
		Cursor cursor;

		try{

			//Check user site is empty.			
			cursor = syncDB.rawQuery("SELECT site_id FROM my_sites;", null);
			Log.d(TAG, String.valueOf(cursor.getCount()));
			if(cursor.getCount() == 0)
			{
				cursor.close();
//				Intent intent = new Intent(PlantList.this, AddSite.class);
//				startActivity(intent);
//				finish();
				
				TextView instruction = (TextView)findViewById(R.id.instruction);
				instruction.setVisibility(View.VISIBLE);
				MyList.setVisibility(View.GONE); 
				return;
			}else{
				cursor.close();
			}
			
			//Check user plant is empty.			
			cursor = syncDB.rawQuery("SELECT site_id FROM my_plants;", null);
			Log.d(TAG, String.valueOf(cursor.getCount()));
			if(cursor.getCount() == 0)
			{
				cursor.close();
//				Intent intent = new Intent(PlantList.this, AddSite.class);
//				startActivity(intent);
//				finish();
				
				TextView instruction = (TextView)findViewById(R.id.instruction);
				instruction.setVisibility(View.VISIBLE);
				MyList.setVisibility(View.GONE); 
				return;
			}else{
				cursor.close();
			}			
			
			
			//Retreive site name and site id from my_plant table to draw plant list.
			cursor = syncDB.rawQuery("SELECT site_name, site_id FROM my_plants GROUP BY site_name;",null);

			while(cursor.moveToNext()){
				user_station_name.add(cursor.getString(0));
				user_station_id.add(cursor.getInt(1));
			}
			cursor.close();
			
			arPlantItem.clear();
			for(int i=0; i<user_station_name.size(); i++){
				
				PlantItem pi;
				
				//Retrieves plants from each site.
				Cursor cursor2 = syncDB.rawQuery("SELECT species_id FROM my_plants " +
						"WHERE site_name = '" + user_station_name.get(i) + "';", null);
				int count = 0;
				while(cursor2.moveToNext()){
					String qry = "SELECT _id, species_name, common_name, protocol_id FROM species WHERE _id = " + cursor2.getInt(0) + ";";
					
					Cursor cursor3 = staticDB.rawQuery(qry, null);
					
					cursor3.moveToNext();
					int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor3.getString(0), null, null);

					// get the number of check-marked data
					String pheno_done = "SELECT _id FROM my_observation WHERE species_id = " 
						+ cursor3.getString(0) + " AND site_id = " + user_station_id.get(i) + " GROUP BY phenophase_id;";
					Cursor cursor4 = syncDB.rawQuery(pheno_done, null);
					
					// get total_number_of phenophases from species
					String total_pheno = "SELECT Phenophase_ID FROM Phenophase_Protocol_Icon WHERE Protocol_ID = " + cursor3.getInt(3) + ";";
					Cursor cursor5 = staticDB.rawQuery(total_pheno, null);
					
					if(count == 0) {
						pi = new PlantItem(resID, cursor3.getString(2), cursor3.getString(1)+" (" + user_station_name.get(i) + ")"
								, cursor3.getInt(0), user_station_id.get(i), cursor3.getInt(3), cursor4.getCount(), cursor5.getCount(), true, user_station_name.get(i));
					}
					else {
						pi = new PlantItem(resID, cursor3.getString(2), cursor3.getString(1)+" (" + user_station_name.get(i) + ")"
								, cursor3.getInt(0), user_station_id.get(i), cursor3.getInt(3), cursor4.getCount(), cursor5.getCount(), false, user_station_name.get(i));
					}
					//PlantItem structure = >int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int siteID)
					
					arPlantItem.add(pi);
					
					cursor3.close();
					cursor4.close();
					cursor5.close();
					
					count++;
				}
				cursor2.close();
			}
			//To synchronize user_species_list with actual listview contents order.
			MyListAdapter mylistapdater = new MyListAdapter(this, R.layout.plantlist_item, arPlantItem);
			MyList.setAdapter(mylistapdater);
		}catch(Exception e){
			Log.e(TAG, e.toString());
		}
		finally{
			staticDBHelper.close();
			syncDBHelper.close();
		}
		
	}

	protected void onListItemClick(View v, int position, long id){
		//Intent intent = new Intent(this, PlantInfo.class);
		
		Intent intent = new Intent(this, GetPhenophase_PBB.class);
		intent.putExtra("species_id", arPlantItem.get(position).SpeciesID);
		intent.putExtra("site_id", arPlantItem.get(position).siteID);
		intent.putExtra("protocol_id", arPlantItem.get(position).protocolID);
		intent.putExtra("cname", arPlantItem.get(position).CommonName);
		intent.putExtra("sname", arPlantItem.get(position).SpeciesName);
		startActivity(intent);
	}
	
	protected boolean onLongListItemClick(View v, int position, long id) {
		pos = position;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Delete Species")
		.setMessage("Delete " + arPlantItem.get(position).CommonName + " ? \nIt cannot be undone.")
		.setIcon(R.drawable.pbbicon_small)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try{
					SyncDBHelper syncDBHelper = new SyncDBHelper(PlantList.this);
					SQLiteDatabase syncDB = syncDBHelper.getWritableDatabase();
					
					syncDB.execSQL("DELETE FROM my_plants WHERE species_id=" + arPlantItem.get(pos).SpeciesID 
									+ " AND site_id=" + arPlantItem.get(pos).siteID + ";");
					syncDBHelper.close();
					
					Toast.makeText(PlantList.this, "Item delete.", Toast.LENGTH_SHORT).show();
					
					Intent intent = new Intent(PlantList.this, PlantList.class);
					finish();
					startActivity(intent);
				}
				catch(Exception e){
					Log.e(TAG,e.toString());
				}
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		}).show();
		
		return true;
	}
		
	/////////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		SubMenu addButton = menu.addSubMenu("Manage")
			.setIcon(android.R.drawable.ic_menu_manage);
		addButton.add(0,MENU_ADD_SITE,0,"Add Site");
		addButton.add(0,MENU_ADD_PLANT,0,"Add Plant");
		
		menu.add(0,MENU_SYNC,0,"Sync").setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0,MENU_LOGOUT,0,"Log out").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, MENU_HELP, 0, "Help").setIcon(android.R.drawable.ic_menu_help);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case MENU_ADD_PLANT:
				intent = new Intent(PlantList.this, AddPlant.class);
				startActivity(intent);
				finish();
				return true;
			case MENU_ADD_SITE:
				intent = new Intent (PlantList.this, AddSite.class);
				startActivity(intent);
				//finish();
				return true;
			case MENU_SYNC:
				intent = new Intent(PlantList.this, Sync.class);
				intent.putExtra("sync_instantly", true);
				startActivity(intent);
				finish();
				return true;
			case MENU_HELP:
				Toast.makeText(PlantList.this, "Coming soon", Toast.LENGTH_SHORT).show();
				return true;
			case MENU_LOGOUT:
				new AlertDialog.Builder(PlantList.this)
					.setTitle("Log out")
					.setIcon(R.drawable.pbbicon_small)
					.setMessage("You might lose your unsynced data if you log out. Do you want to log out?")
					.setPositiveButton("Yes",mClick)
					.setNegativeButton("no",mClick)
					.show();
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
	
	void deleteContents(String path) {
		File file = new File(path);
		if(file.isDirectory()) {
			String[] fileList = file.list();
			
			for(int i = 0 ; i < fileList.length ; i++) {
				Log.i("K", "FILE NAME : " + "/sdcard/pbudburst/tmp/" + fileList[i] + " IS DELETED.");
				new File("/sdcard/pbudburst/tmp/" + fileList[i]).delete();
			}
		}
	}
	
	//Dialog confirm message if user clicks logout button
	DialogInterface.OnClickListener mClick =
		new DialogInterface.OnClickListener(){
		public void onClick(DialogInterface dialog, int whichButton){
			if(whichButton == DialogInterface.BUTTON1){
				
				SharedPreferences.Editor edit = pref.edit();				
				edit.putString("Username","");
				edit.putString("Password","");
				edit.putString("synced", "false");
				edit.commit();
				
				//Drop user table in database
				SyncDBHelper dbhelper = new SyncDBHelper(PlantList.this);
				dbhelper.clearAllTable(PlantList.this);
				dbhelper.close();
				
				deleteContents("/sdcard/pbudburst/tmp/");
				
				Intent intent = new Intent(PlantList.this, Login.class);
				startActivity(intent);
				finish();
			}else{
			}
		}
	};
	//Menu option
	/////////////////////////////////////////////////////////////
	
    // or when user press back button
	// when you hold the button for 3 sec, the app will be exited
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			boolean flag = false;
			if(event.getRepeatCount() == 3) {
				Toast.makeText(PlantList.this, "Thank you.", Toast.LENGTH_SHORT).show();
				finish();
				return true;
			}
			else if(event.getRepeatCount() == 0 && flag == false){
				Toast.makeText(PlantList.this, "Hold the Back Button to exit.", Toast.LENGTH_SHORT).show();
				flag = true;
			}
		}
		
		return false;
	}
}

//Adapters:MyListAdapter and SeparatedAdapter
class MyListAdapter extends BaseAdapter{
	Context maincon;
	LayoutInflater Inflater;
	ArrayList<PlantItem> arSrc;
	int layout;
	int previous_site = 0;
	
	public MyListAdapter(Context context, int alayout, ArrayList<PlantItem> aarSrc){
		maincon = context;
		Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		arSrc = aarSrc;
		layout = alayout;
	}
	
	public int getCount(){
		return arSrc.size();
	}
	
	public String getItem(int position){
		return arSrc.get(position).CommonName;
	}
	
	public long getItemId(int position){
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null)
			convertView = Inflater.inflate(layout, parent, false);
		
		TextView site_header = (TextView)convertView.findViewById(R.id.list_header);
		
		Log.i("K", "POSITION : " + position + " TOPITEM : " + arSrc.get(position).TopItem);
		
		if(arSrc.get(position).TopItem) {
			site_header.setVisibility(View.VISIBLE);
			site_header.setText("  " + arSrc.get(position).Site);
			
		}
		else {
			site_header.setVisibility(View.GONE);
		}
		
		ImageView img = (ImageView)convertView.findViewById(R.id.icon);
		img.setImageResource(arSrc.get(position).Picture);
		
		TextView textname = (TextView)convertView.findViewById(R.id.commonname);
		textname.setText(arSrc.get(position).CommonName);
		
		TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
		String [] splits = arSrc.get(position).SpeciesName.split(" ");
		textdesc.setText(splits[0] + " " + splits[1]);
		
		
		TextView pheno_stat = (TextView)convertView.findViewById(R.id.pheno_stat);
		if(arSrc.get(position).total_pheno != 0) {
			pheno_stat.setText(arSrc.get(position).current_pheno + " / " + arSrc.get(position).total_pheno);
		}
		else {
			pheno_stat.setVisibility(View.GONE);
		}

		return convertView;
	}

}
	
class PlantItem{
	PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID){
		Picture = aPicture;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		SpeciesID = aSpeciesID;
	}

	PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int aSiteID){
		Picture = aPicture;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		SpeciesID = aSpeciesID;
		siteID = aSiteID;
	}
	
	PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int aSiteID, int aProtocolID, int aPheno_done, int aTotal_pheno, boolean aTopItem, String aSiteName){
		Picture = aPicture;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		Site = aSiteName;
		SpeciesID = aSpeciesID;
		siteID = aSiteID;
		protocolID = aProtocolID;
		current_pheno = aPheno_done;
		total_pheno = aTotal_pheno;
		TopItem = aTopItem;
	}
	
	int Picture;
	String CommonName;
	String SpeciesName;
	int SpeciesID;
	int siteID;
	int protocolID;
	int current_pheno;
	int total_pheno;
	String Site;
	boolean TopItem;
}