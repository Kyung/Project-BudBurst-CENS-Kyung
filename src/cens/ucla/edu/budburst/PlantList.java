package cens.ucla.edu.budburst;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.Helloscr.UpdateThread;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.Flora_Observer;
import cens.ucla.edu.budburst.onetime.OneTimeMain;

public class PlantList extends ListActivity {
	
	final private String TAG = "PlantList"; 
	
	private String username;
	private String password;
	private SharedPreferences pref;
	private SyncDBHelper syncDBHelper;
	private StaticDBHelper staticDBHelper;
	private ListView MyList;
	private Dialog dialog = null;
	private EditText et1 = null;
	private Button tempBtn;
	private ArrayList<PlantItem> user_species_list;
	private int first_site_id = 0;
	private int pos = 0;
	private LinearLayout lout = null;
	private static boolean first_site_flag = true;
	private Button buttonSharedplant = null;
	private int dialog_species_id = 0;
	private int dialog_site_id = 0;
	
	//MENU contants
	final private int MENU_ADD_PLANT = 1;
	final private int MENU_SYNC = 6;
	final private int MENU_HELP = 7;
	final private int PLANT_LIST = 99;
	
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
			.setTitle(getString(R.string.Alert_synchronized))
			.setIcon(R.drawable.pbbicon_small)
			.setMessage(getString(R.string.Alert_dbUpdated))
			.setPositiveButton(getString(R.string.Button_OK),new DialogInterface.OnClickListener() {
				
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
		/*
		buttonSharedplant = (Button)findViewById(R.id.sharedplant);
		buttonSharedplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(PlantList.this, OneTimeMain.class);
				startActivity(intent);
			}
		});
		*/
		//buttonMyplant.setSelected(true);
		
		arPlantItem = new ArrayList<PlantItem>();
		syncDBHelper = new SyncDBHelper(PlantList.this);
		SQLiteDatabase syncDB  = syncDBHelper.getReadableDatabase();
		
		staticDBHelper = new StaticDBHelper(PlantList.this);
        
		
		Cursor cursorss = syncDB.rawQuery("SELECT _id, species_id, site_id FROM my_observation;", null);
		while(cursorss.moveToNext()) {
			Log.i("K", "MY OBSERVATION : " + cursorss.getInt(0) + " , " + cursorss.getInt(1) + " , " + cursorss.getInt(2));
		}
		cursorss.close();
		
		cursorss = syncDB.rawQuery("SELECT species_id, common_name, active, synced FROM my_plants;", null);
		while(cursorss.moveToNext()) {
			Log.i("K", "MY PLANTS : " + cursorss.getInt(0) + " , " + cursorss.getString(1) + " , active : " + cursorss.getInt(2)+ " , synced : " + cursorss.getInt(3));
		}
		cursorss.close();
		
		OneTimeDBHelper onetime = new OneTimeDBHelper(PlantList.this);
		SQLiteDatabase ot  = onetime.getReadableDatabase();

		cursorss = ot.rawQuery("SELECT _id, species_id, site_id, cname, sname, synced FROM onetimeob;", null);
		while(cursorss.moveToNext()) {
			Log.i("K", "ONETIME OB : " + cursorss.getInt(0) + " ,species_id " + cursorss.getInt(1) + " , site_id : " + cursorss.getString(2) + " , cname : " + cursorss.getString(3) + " , sname : " + cursorss.getString(4) + " , SYNCED : " + cursorss.getInt(5));
		}
		cursorss.close();
		
		cursorss = ot.rawQuery("SELECT plant_id, phenophase_id, lat, lng, image_id, dt_taken, notes, synced FROM onetimeob_observation;", null);
		while(cursorss.moveToNext()) {
			Log.i("K", "ONETIME OBSERVATION - plant_ID : " + cursorss.getInt(0) + " , Phenophase_id : " + cursorss.getInt(1) + " , lat : " + cursorss.getDouble(2) + " , lng : " + cursorss.getDouble(3) + ", image_id : " + cursorss.getString(4) + " , date_taken : " + cursorss.getString(5));
		}
		
		cursorss.close();
		ot.close();
		onetime.close();
		
		
		try {
        	staticDBHelper.createDataBase();
	 	} catch (IOException ioe) {
	 		Log.e("K", "CREATE DATABASE : " + ioe.toString());
	 		throw new Error(getString(R.string.Alert_dbError));
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
			boolean header = false;
			int count = 0;
			for(int i=0; i<user_station_name.size(); i++){
				
				PlantItem pi;
				
				//Retrieves plants from each site.
				Cursor cursor2 = syncDB.rawQuery("SELECT species_id, common_name, active FROM my_plants " +
						"WHERE site_name = '" + user_station_name.get(i) + "';", null);
				while(cursor2.moveToNext()){
					count++;
					// if active flag is 0, skip the operation below...
					// active = 0 means, the corresponding species got deleted
					if(cursor2.getInt(2) == 0) {
						continue;
					}
					else {
						// check if the species_id and common_name are from "UNKNOWN"
						int species_id = 0;
						if(cursor2.getInt(0) > 76) {
							species_id = 999;
						}
						else {
							species_id = cursor2.getInt(0);
						}
						
						// if common_name from the server is "null", change it to "Unknown Plant"
						String common_name = "";
						if(cursor2.getString(1).equals("null")) {
							common_name = "Unknown Plant";
						}
						else {
							common_name = cursor2.getString(1);
						}
						
						
						String qry = "SELECT _id, species_name, common_name, protocol_id FROM species WHERE _id = " + species_id + ";";
						
						Cursor cursor3 = staticDB.rawQuery(qry, null);
						
						cursor3.moveToNext();
						int resID = 0;
						if(Integer.parseInt(cursor3.getString(0)) > 76) {
							resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null);
						}
						else {
							resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor3.getString(0), null, null);
						}

						// get the number of check-marked data
						String pheno_done = "SELECT _id FROM my_observation WHERE species_id = " 
							+ cursor2.getInt(0) + " AND site_id = " + user_station_id.get(i) + " GROUP BY phenophase_id;";
						Cursor cursor4 = syncDB.rawQuery(pheno_done, null);
						
						// get total_number_of phenophases from species
						String total_pheno = "SELECT Phenophase_ID FROM Phenophase_Protocol_Icon WHERE Protocol_ID = " + cursor3.getInt(3) + ";";
						Cursor cursor5 = staticDB.rawQuery(total_pheno, null);
						
						if(!header) {
							pi = new PlantItem(resID, common_name, cursor3.getString(1)+" (" + user_station_name.get(i) + ")"
									, cursor2.getInt(0), user_station_id.get(i), cursor3.getInt(3), cursor4.getCount(), cursor5.getCount(), true, user_station_name.get(i), true);
							header = true;
						}
						else {
							pi = new PlantItem(resID, common_name, cursor3.getString(1)+" (" + user_station_name.get(i) + ")"
									, cursor2.getInt(0), user_station_id.get(i), cursor3.getInt(3), cursor4.getCount(), cursor5.getCount(), false, user_station_name.get(i), true);
						}
						//PlantItem structure = >int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int siteID)
						
						arPlantItem.add(pi);
						
						cursor3.close();
						cursor4.close();
						cursor5.close();
					}
				}
				cursor2.close();
				
				// show one_time_observation species
			}
			//To synchronize user_species_list with actual listview contents order.
			
			add_oneTimeObs();
			
			Log.i("K","@@@@@@@@@@@@@@@ : " + count);
			
			MyListAdapter mylistapdater = new MyListAdapter(this, R.layout.plantlist_item, arPlantItem);
			MyList.setAdapter(mylistapdater);
		}catch(Exception e){
			Log.e(TAG, e.toString());
		}
		finally{
			staticDBHelper.close();
			syncDBHelper.close();
			syncDB.close();
		}
		
	}

	private void add_oneTimeObs() {
		
		OneTimeDBHelper onetime = new OneTimeDBHelper(PlantList.this);
		SQLiteDatabase ot  = onetime.getReadableDatabase();
		SyncDBHelper syncHelper = new SyncDBHelper(PlantList.this);
		SQLiteDatabase sync = syncHelper.getReadableDatabase();
		
		Cursor cursor = ot.rawQuery("SELECT _id, species_id, site_id, protocol_id, cname, sname, synced FROM onetimeob", null);
		PlantItem pi;
		
		// header is called only once. (top)
		boolean header = false;
		
		int count = 0;
		while(cursor.moveToNext()) {
			count++;
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s" + cursor.getInt(1), null, null);
			
			
			
			int pheno_count = 0;
			int pheno_current = 0;
			
			// query the total number of onetime observations of each species - not decided yet
			
			/*
			 * 
			 * 
			 * 
			 * StaticDBHelper sDB = new StaticDBHelper(PlantList.this);
			SQLiteDatabase staticDB = sDB.getReadableDatabase();
			Cursor pheno_total = staticDB.rawQuery("SELECT COUNT(Protocol_ID) as cnt FROM PhenoPhase_Protocol_Icon WHERE Protocol_ID=" + cursor.getInt(3) + ";", null);
			while(pheno_total.moveToNext()) {
				pheno_count = pheno_total.getInt(0);
			}
			
			pheno_total.close();
			sDB.close();
			staticDB.close();
			*/
			
			Log.i("K", "PLANT ID : " + cursor.getInt(0));
			
			// query the number of onetime observations have been made by each species
			Cursor pheno_cur = ot.rawQuery("SELECT COUNT(plant_id) as cnt FROM onetimeob_observation WHERE plant_id=" + cursor.getInt(0) + ";", null);
			while(pheno_cur.moveToNext()) {
				pheno_current = pheno_cur.getInt(0);
			}
			
			pheno_cur.close();
			
			Log.i("K", "PHENO CURRENT : " + pheno_current);

			
			// if site_id == 0 meaning one-time observations are added to the database in "OneTimeMain"
			if(cursor.getInt(2) == 0) {
				if(!header) {
					pi = new PlantItem(resID, cursor.getString(4), cursor.getString(4), cursor.getInt(1),
							cursor.getInt(2), cursor.getInt(3), pheno_current, 12, true, "No Site", false);
					// make header true (not called any more)
					header = true;
				}
				else {
					pi = new PlantItem(resID, cursor.getString(4), cursor.getString(4), cursor.getInt(1),
							cursor.getInt(2), cursor.getInt(3), pheno_current, 12, false, "No Site", false);
				}
					
				arPlantItem.add(pi);
			}
			// means one-time observations are added to the database in "Getphenophase" or "Add Plant"
			else {

				Cursor cursor2 = sync.rawQuery("SELECT site_name FROM my_sites WHERE site_id=" + cursor.getInt(2) + ";", null);
				
				while(cursor2.moveToNext()) {
						
					Log.i("K", "resID : " + resID + " , cname : " + cursor.getString(4) + " , species_id : " + cursor.getInt(1));
					// resID, common_name, site_name, species_id, site_id, protocol_id, current_number_of_pheno, total_number_of_pheno, top, header_name, monitored
					if(!header) {
						pi = new PlantItem(resID, cursor.getString(4), "", cursor.getInt(1),
								cursor.getInt(2), cursor.getInt(3), pheno_current, 12, true, cursor2.getString(0), false);
						header = true;
					}
					else {
						pi = new PlantItem(resID, cursor.getString(4), "", cursor.getInt(1),
								cursor.getInt(2), cursor.getInt(3), pheno_current, 12, false, cursor2.getString(0), false);
					}
						
					arPlantItem.add(pi);
					cursor2.close();
				}
			}
		}
		
		Log.i("K", "########### : " + count);
		
		cursor.close();
		onetime.close();
		ot.close();
		
		sync.close();
		syncHelper.close();
	}

	protected void onListItemClick(View v, int position, long id){
		//Intent intent = new Intent(this, PlantInfo.class);
		
		Log.i("K", "POSITION : " + position);
		
		if(arPlantItem.get(position).SpeciesID == 999) {
			Toast.makeText(PlantList.this, getString(R.string.DoSyncFirst_For_UnknownPlant), Toast.LENGTH_SHORT).show();
		}
		else {
			Log.i("K", "POSITION -> SPECIES_ID : " + arPlantItem.get(position).SpeciesID);
				
			Intent intent = new Intent(this, GetPhenophase_PBB.class);
			intent.putExtra("species_id", arPlantItem.get(position).SpeciesID);
			intent.putExtra("site_id", arPlantItem.get(position).siteID);
			intent.putExtra("protocol_id", arPlantItem.get(position).protocolID);
			intent.putExtra("cname", arPlantItem.get(position).CommonName);
			intent.putExtra("sname", arPlantItem.get(position).SpeciesName);
			startActivity(intent);
		}
	}
	
	protected boolean onLongListItemClick(View v, int position, long id) {
		pos = position;
		
		Log.i("K","LONG POSITION SELECTED");
		
		new AlertDialog.Builder(PlantList.this)
		.setTitle("Select one")
		.setIcon(R.drawable.pbbicon_small)
		.setNegativeButton("Back", null)
		.setItems(R.array.plantlist, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String[] category = getResources().getStringArray(R.array.plantlist);
				// temporary deleting item not used
				if(category[which].equals("Delete Species")) {
					try{
						SyncDBHelper syncDBHelper = new SyncDBHelper(PlantList.this);
						SQLiteDatabase syncDB = syncDBHelper.getWritableDatabase();
						
						// 
						syncDB.execSQL("UPDATE my_plants SET active = 0 AND synced = " + SyncDBHelper.SYNCED_NO + " WHERE species_id=" + arPlantItem.get(pos).SpeciesID 
										+ " AND site_id=" + arPlantItem.get(pos).siteID + ";");
						syncDB.execSQL("UPDATE my_plants SET synced = " + SyncDBHelper.SYNCED_NO + " WHERE species_id=" + arPlantItem.get(pos).SpeciesID 
										+ " AND site_id=" + arPlantItem.get(pos).siteID + ";");
						syncDB.execSQL("DELETE FROM my_observation WHERE species_id=" + arPlantItem.get(pos).SpeciesID + " AND site_id=" + arPlantItem.get(pos).siteID);
						syncDBHelper.close();
						
						Toast.makeText(PlantList.this, "Item delete.", Toast.LENGTH_SHORT).show();
						
						Intent intent = new Intent(PlantList.this, PlantList.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						finish();
						startActivity(intent);
					}
					catch(Exception e){
						Log.e(TAG,e.toString());
					}
				}
				else{

					if(arPlantItem.get(pos).SpeciesID > 0 && arPlantItem.get(pos).SpeciesID < 77) {
						Toast.makeText(PlantList.this, getString(R.string.Cannot_Change), Toast.LENGTH_SHORT).show();
					}
					else {
						
						dialog(arPlantItem.get(pos).SpeciesID, arPlantItem.get(pos).siteID);	
					}					
				}
			}
		})
		.show();
		
		return true;
	}
	
	public void dialog(int species_id, int site_id) {
		
		dialog_species_id = species_id;
		dialog_site_id = site_id;
		
		dialog = new Dialog(PlantList.this);
		
		dialog.setContentView(R.layout.species_name_custom_dialog);
		dialog.setTitle(getString(R.string.GetPhenophase_PBB_message));
		dialog.setCancelable(true);
		dialog.show();
		
		et1 = (EditText)dialog.findViewById(R.id.custom_common_name);
		Button doneBtn = (Button)dialog.findViewById(R.id.custom_done);
		
		doneBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String common_name = et1.getText().toString();
				if(common_name.equals("")) {
				// nothing happens
				}
				else {
					
					SyncDBHelper syncDBHelper = new SyncDBHelper(PlantList.this);
					SQLiteDatabase syncDB  = syncDBHelper.getWritableDatabase();
					
					Log.i("K", "UPDATE my_plants SET common_name='" + common_name 
							+ "' WHERE species_id=" + dialog_species_id 
							+ ";");

					syncDB.execSQL("UPDATE my_plants SET common_name='" + common_name 
							+ "' WHERE species_id=" + dialog_species_id + " AND site_id =" + dialog_site_id 
							+ ";");
					
					syncDB.execSQL("UPDATE my_plants SET active=2 WHERE species_id=" + dialog_species_id + ";");
					syncDB.execSQL("UPDATE my_plants SET synced=9 WHERE species_id=" + dialog_species_id + ";");

					syncDBHelper.close();

					Toast.makeText(PlantList.this, getString(R.string.GetPhenophase_PBB_update_name), Toast.LENGTH_SHORT).show();
				}
				
				dialog.cancel();
				
				Intent intent = new Intent(PlantList.this, PlantList.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				finish();
				startActivity(intent);
			}
		});
	}
		
	/////////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		//SubMenu addButton = menu.addSubMenu(getString(R.string.Menu_addplants)).setIcon(android.R.drawable.ic_menu_manage);
		//addButton.add(0,MENU_ADD_SITE,0,getString(R.string.AddSite_addSite));
	//	addButton.add(0,MENU_ADD_MONITORED,0,getString(R.string.Menu_addMonitored));
		//addButton.add(0,MENU_ADD_QUICK,0,getString(R.string.Menu_addQuick));
		menu.add(0, MENU_ADD_PLANT, 0, "Add Plant").setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, MENU_SYNC, 0, "Sync").setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, MENU_HELP, 0, "Help").setIcon(android.R.drawable.ic_menu_help);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case MENU_ADD_PLANT:
				intent = new Intent(PlantList.this, OneTimeMain.class);
				intent.putExtra("FROM", PLANT_LIST);
				startActivity(intent);
				return true;
			case MENU_SYNC:
				intent = new Intent(PlantList.this, Sync.class);
				intent.putExtra("sync_instantly", true);
				startActivity(intent);
				finish();
				return true;
			case MENU_HELP:
				Toast.makeText(PlantList.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				return true;
		}
		return false;
	}
	//Menu option
	/////////////////////////////////////////////////////////////
	
	

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
			
			Log.i("K", "COUNT : " + getCount());
			
			Log.i("K", "Position : " + position);

			if(arSrc.get(position).Monitor) {
				if(arSrc.get(position).TopItem) {
					site_header.setVisibility(View.VISIBLE);
					site_header.setText(getString(R.string.Monitor_Plant));
				}
				else {
					site_header.setVisibility(View.GONE);
				}
			}
			else if(!arSrc.get(position).Monitor) {
				if(arSrc.get(position).TopItem) {
					site_header.setVisibility(View.VISIBLE);
					site_header.setText(getString(R.string.Quick_Plant));
				}
				else {
					site_header.setVisibility(View.GONE);
				}
			}
			
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);
			
			
			Log.i("K", "IMAGE RESOURCES : " + arSrc.get(position).Picture);
			
			img.setImageResource(arSrc.get(position).Picture);
			
			TextView textname = (TextView)convertView.findViewById(R.id.commonname);
			textname.setText(arSrc.get(position).CommonName);
			
			TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
			textdesc.setText(arSrc.get(position).Site);
			//String [] splits = arSrc.get(position).SpeciesName.split(" ");
			//textdesc.setText(splits[0] + " " + splits[1]);
			
			// call View from the xml and link the view to current position.
			View thumbnail = convertView.findViewById(R.id.wrap_icon);
			thumbnail.setTag(arSrc.get(position));
			thumbnail.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					PlantItem pi = (PlantItem)v.getTag();
					
					Intent intent = new Intent(PlantList.this, SpeciesDetail.class);
					intent.putExtra("id", pi.SpeciesID);
					intent.putExtra("site_id", pi.siteID);
					startActivity(intent);
				}
			});
			
			TextView pheno_stat = (TextView)convertView.findViewById(R.id.pheno_stat);
			if(arSrc.get(position).total_pheno != 0) {
				pheno_stat.setText(arSrc.get(position).current_pheno + " / " + arSrc.get(position).total_pheno + " ");
			}
			else {
				pheno_stat.setVisibility(View.GONE);
			}

			return convertView;
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

