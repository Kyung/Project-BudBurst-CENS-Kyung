package cens.ucla.edu.budburst;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.lists.ListsDetail;
import cens.ucla.edu.budburst.onetime.Flora_Observer;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.OneTimeMain;
import cens.ucla.edu.budburst.onetime.QuickCapture;

public class PlantList extends ListActivity {
	
	final private String TAG = "PlantList"; 
	
	private int first_site_id = 0;
	private int pos = 0;
	private int dialog_species_id = 0;
	private int dialog_site_id = 0;
	private int onetime_start_point = 0;
	private int totalObservedPlants = 0;
	private int totalObservedObservations = 0;
	private int speciesID;
	private int protocolID;
	private String commonName;
	
	private static boolean first_site_flag = true;
	
	private SharedPreferences pref;
	private SyncDBHelper syncDBHelper;
	private StaticDBHelper staticDBHelper;
	
	private ListView MyList;
	private Dialog dialog = null;
	private EditText et1 = null;
	private ArrayList<PlantItem> user_species_list;
	private LinearLayout lout = null;
	private LinearLayout summaryFooter = null;	

	private CharSequence[] seqUserSite;
	private FunctionsHelper helper;
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>();
	
	//MENU contants
	final private int MENU_ADD_PLANT = 1;
	final private int MENU_ADD_QC_PLANT = 2;
	final private int MENU_SYNC = 6;
	final private int MENU_HELP = 7;
	
	ArrayList<PlantItem> arPlantItem;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plantlist);
	}
	
	public void onResume(){
		super.onResume();

		MyList = getListView();
		
		MyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
		        onListItemClick(v,pos,id);
		    }
		});

		
		MyList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
		    @Override
		    public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
		        return onLongListItemClick(v,pos,id);
		    }
		});

		
		//Initiate ArrayList
		user_species_list = new ArrayList<PlantItem>();
		
		//Retrieve username and password
		pref = getSharedPreferences("userinfo",0);
		String username = pref.getString("Username","");
		String password = pref.getString("Password","");
		
		
		arPlantItem = new ArrayList<PlantItem>();
		syncDBHelper = new SyncDBHelper(PlantList.this);
		SQLiteDatabase syncDB  = syncDBHelper.getReadableDatabase();
		OneTimeDBHelper onetime = new OneTimeDBHelper(PlantList.this);
		SQLiteDatabase ot  = onetime.getReadableDatabase();
		
		staticDBHelper = new StaticDBHelper(PlantList.this);
		SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
		
		//Check user plant is empty.			
		Cursor number_of_my_plants = syncDB.rawQuery("SELECT site_id FROM my_plants;", null);
		Cursor number_of_my_ob = ot.rawQuery("SELECT _id FROM oneTimePlant", null);
		Log.d(TAG, String.valueOf(number_of_my_plants.getCount()));
		
		if(number_of_my_plants.getCount() == 0 && number_of_my_ob.getCount() == 0)
		{	
			number_of_my_plants.close();
			number_of_my_ob.close();
			
			TextView instruction = (TextView)findViewById(R.id.instruction);
			instruction.setVisibility(View.VISIBLE);
			MyList.setVisibility(View.GONE); 
			return;
		}else{
			number_of_my_plants.close();
			number_of_my_ob.close();
		}
		
		number_of_my_plants.close();
		number_of_my_ob.close();
		
		/*
		 * Show the current data in the database...(debug purpose)
		 * 
		 */
		
		Cursor ccc = staticDB.rawQuery("SELECT _id, Phenophase_ID, Protocol_ID FROM Phenophase_Protocol_Icon", null);
		while(ccc.moveToNext()) {
			Log.i("K", "id : " + ccc.getInt(0) + " Phenophase_ID : " + ccc.getInt(1) + " Protocol_ID : " + ccc.getInt(2));
		}

		ccc.close();
		
		Cursor cursorss = syncDB.rawQuery("SELECT _id, species_id, site_id, phenophase_id, image_id, synced FROM my_observation;", null);
		while(cursorss.moveToNext()) {
			Log.i("K", "MY OBSERVATION => id : " + cursorss.getInt(0) + " ,species_id: " + cursorss.getInt(1) + " ,site_id: " + cursorss.getInt(2) + ", phenophase_id: " + cursorss.getInt(3) + ", image_id : " + cursorss.getString(4) + " , synced : " + cursorss.getInt(5));
		}
		cursorss.close();
		
		cursorss = syncDB.rawQuery("SELECT species_id, common_name, active, synced FROM my_plants;", null);
		while(cursorss.moveToNext()) {
			Log.i("K", "MY PLANTS : " + cursorss.getInt(0) + " , " + cursorss.getString(1) + " , active : " + cursorss.getInt(2)+ " , synced : " + cursorss.getInt(3));
		}
		cursorss.close();
		
		cursorss = syncDB.rawQuery("SELECT site_id, site_name, official, synced FROM my_sites;", null);
		while(cursorss.moveToNext()) {
			Log.i("K", "MY SITES : " + cursorss.getInt(0) + " ,name : " + cursorss.getString(1) + " , official : " + cursorss.getInt(2)+ " , synced : " + cursorss.getInt(3));
		}
		cursorss.close();
		
		

		cursorss = ot.rawQuery("SELECT _id, plant_id, species_id, site_id, cname, sname, active, synced, category FROM oneTimePlant;", null);
		while(cursorss.moveToNext()) {
			Log.i("K", "ONETIME OB : " + cursorss.getInt(0) + ", plant_id : " + cursorss.getInt(1) + ", species_id " + cursorss.getInt(2) + " , site_id : " + cursorss.getString(3) + " , cname : " + cursorss.getString(4) + " , sname : " + cursorss.getString(5) + ", Active " + cursorss.getInt(6) + " , SYNCED : " + cursorss.getInt(7) + ", CATEGORY : " + cursorss.getInt(8));
		}
		cursorss.close();
		
		cursorss = ot.rawQuery("SELECT plant_id, phenophase_id, lat, lng, image_id, dt_taken, notes, synced FROM oneTimeObservation;", null);
		while(cursorss.moveToNext()) {
			Log.i("K", "ONETIME OBSERVATION - plant_ID : " + cursorss.getInt(0) + " , Phenophase_id : " + cursorss.getInt(1) + " , lat : " + cursorss.getDouble(2) + " , lng : " + cursorss.getDouble(3) + ", image_id : " + cursorss.getString(4) + " , date_taken : " + cursorss.getString(5) + " , notes : " + cursorss.getString(6) + " ,synced : " + cursorss.getInt(7));
		}
		cursorss.close();
		
		ot.close();
		onetime.close();
		
		//////////////////////////////////////////////////////////////////////////////////////////////////
		summaryFooter = (LinearLayout) findViewById(R.id.footer_item);
		TextView summaryTxt = (TextView) findViewById(R.id.my_summary);
		
		SyncDBHelper dbh = new SyncDBHelper(PlantList.this);
		OneTimeDBHelper otdbh = new OneTimeDBHelper(PlantList.this);
		
		
		Log.i("K", "1 : " + dbh.getTotalNumberOfPlants(this) + " 2 : " + otdbh.getTotalNumberOfQCOPlants(this) + " 3: "+
				dbh.getTotalNumberOfObservations(this) + " 4 : " + 
				otdbh.getTotalNumberOfQCObservations(this));
		
		final int totalNumberOfPlants = dbh.getTotalNumberOfPlants(this);
		final int totalNumberOfQCOPlants = otdbh.getTotalNumberOfQCOPlants(this);
		
		final int totalNumberOfObservations = dbh.getTotalNumberOfObservations(this);
		final int totalNumberOfQCObservations = otdbh.getTotalNumberOfQCObservations(this);
		
		int totalNumberP = totalNumberOfPlants + totalNumberOfQCOPlants;
		int totalNumberO = totalNumberOfObservations + totalNumberOfQCObservations;
		
		summaryTxt.setText("Plants: " + totalNumberP
				+ "  Observations: " + totalNumberO);
		
		
		
		summaryFooter.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				final LinearLayout linear = (LinearLayout) View.inflate(PlantList.this, R.layout.summarypage, null);
				
				TextView numMonitoredPlant = (TextView) linear.findViewById(R.id.num_monitored_plants);
				TextView numMonitoredObservation = (TextView) linear.findViewById(R.id.num_monitored_observations);
				TextView numMonitoredQSPlant = (TextView) linear.findViewById(R.id.num_qc_plants);
				TextView numMonitoredQSObservation = (TextView) linear.findViewById(R.id.num_qc_observations);
				
				numMonitoredPlant.setText("- Plant: " + totalNumberOfPlants);
				numMonitoredObservation.setText("- Observation: " + totalNumberOfObservations);
				numMonitoredQSPlant.setText("- Plant: " + totalNumberOfQCOPlants);
				numMonitoredQSObservation.setText("- Observation: " + totalNumberOfQCObservations);
				
				
				new AlertDialog.Builder(PlantList.this)
				.setTitle(getString(R.string.My_Plant_Summary))
				.setView(linear)
				.setPositiveButton(getString(R.string.Button_save), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();

			}
		});
		
		/*
		 * Getting my plant information from the database
		 */
		ArrayList<String> user_station_name = new ArrayList<String>();
		ArrayList<Integer> user_station_id = new ArrayList<Integer>();
		Cursor cursor;

		try{
			//Retreive site name and site id from my_plant table to draw plant list.
			cursor = syncDB.rawQuery("SELECT site_name, site_id FROM my_plants GROUP BY site_id;",null);
			
			while(cursor.moveToNext()){
				Log.i("K", "ID : " + cursor.getInt(1) + ", Name:" + cursor.getString(0));
				user_station_name.add(cursor.getString(0));
				user_station_id.add(cursor.getInt(1));
			}
			cursor.close();
			
			arPlantItem.clear();
			boolean header = false;
			int count = 0;
			for(int i = 0; i < user_station_id.size(); i++){
				
				PlantItem pi;
				
				//Retrieves plants from each site.
				//Cursor cursor2 = syncDB.rawQuery("SELECT species_id, common_name, active, protocol_id FROM my_plants " +
				//		"WHERE site_name = '" + user_station_name.get(i) + "';", null);
				
				Cursor cursor2 = syncDB.rawQuery("SELECT species_id, common_name, active, protocol_id, synced FROM my_plants " +
						"WHERE site_id =" + user_station_id.get(i) + ";", null);
				
				while(cursor2.moveToNext()){
					// if active flag is 0, skip the operation below...
					// active = 0 means, the corresponding species got deleted
					if(cursor2.getInt(2) == 0) {
						Log.i("K", "MOVE TO THE TOP");
						continue;
					}
					
					count++;
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
					String pheno_done = "SELECT _id, synced FROM my_observation WHERE species_id = " 
							+ cursor2.getInt(0) + " AND site_id = " + user_station_id.get(i) + " GROUP BY phenophase_id;";
					Cursor cursor4 = syncDB.rawQuery(pheno_done, null);
					int synced_species = 5;
					
					Log.i("K", "cursor4.getCount() : " + cursor4.getCount());
					
					while(cursor4.moveToNext()) {
						// check the species has been synced or not.
						// 5 - synced, 9 - not synced
						if(cursor4.getInt(1) == SyncDBHelper.SYNCED_NO) {
							synced_species = SyncDBHelper.SYNCED_NO;
							// once we find the unsync data in the my_observation, change the synced_species = 9 and break
							break;
						}
						else {
							synced_species = SyncDBHelper.SYNCED_YES;
						}
					}

					Log.i("K", "cursor2.getInt(3) : " + cursor2.getInt(3));
					
					// get total_number_of phenophases from species
					String total_pheno = "SELECT Phenophase_ID FROM Phenophase_Protocol_Icon WHERE Protocol_ID = " + cursor2.getInt(3) + ";";
					Cursor cursor5 = staticDB.rawQuery(total_pheno, null);
					
					Log.i("K", "GetCount : " + cursor5.getCount());
					
					String science_name = cursor3.getString(1);
					
					if(cursor3.getString(1).equals("Unknown Plant")) {
						science_name = "";
					}
						
					if(!header) {
						pi = new PlantItem(resID, common_name, science_name
								, cursor2.getInt(0), user_station_id.get(i), cursor2.getInt(3), cursor4.getCount(), cursor5.getCount(), true, user_station_name.get(i), true, synced_species);
						header = true;
					}
					else {
						pi = new PlantItem(resID, common_name, science_name
								, cursor2.getInt(0), user_station_id.get(i), cursor2.getInt(3), cursor4.getCount(), cursor5.getCount(), false, user_station_name.get(i), true, synced_species);
					}
					//PlantItem structure = >int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int siteID)
						
					arPlantItem.add(pi);
						
					cursor3.close();
					cursor4.close();
					cursor5.close();
				}
				
				cursor2.close();
				
				// show one_time_observation species
			}
			//To synchronize user_species_list with actual listview contents order.
			
			add_oneTimeObs();

			onetime_start_point = count;
			
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
		
		Cursor cursor = ot.rawQuery("SELECT _id, plant_id, species_id, site_id, protocol_id, cname, sname, synced, category FROM oneTimePlant WHERE active=1", null);
		PlantItem pi;
		
		// header is called only once. (top)
		boolean header = false;
		
		int count = 0;
		while(cursor.moveToNext()) {
			count++;
			
			int speciesID = cursor.getInt(2);
			
			/*
			 *  category == 1 : treelists
			 *  category == 2 : local budburst
			 *  category == 3 : local invasive
			 *  category == 4 : local native...
			 *  and more later.
			 *  
			 */
			int resID = 0;
			if(cursor.getInt(8) == 1) {
				resID = 0;
			}
			else if(cursor.getInt(8) == 2) {
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s" + cursor.getInt(2), null, null);
			}
			
			else if(cursor.getInt(8) == 3) {
				//resID = Values.WHATSINVASIVE_LIST + Values.COMPLICATED;
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null);
			}
			/*
			else if(cursor.getInt(8) == 4) {
				resID = Values.NATIVE_LIST + Values.COMPLICATED;
			}
			*/
			else {
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s" + cursor.getInt(2), null, null);
			}


			int pheno_count = 0;
			int pheno_current = 0;
			int synced = SyncDBHelper.SYNCED_YES;
			
			//Log.i("K", "PLANT ID : " + cursor.getInt(1) + " resID : " + resID + " sname : " + cursor.getString(6));
			
			int totalNumPheno = 0;
			
			switch(cursor.getInt(4)) {
			case 1:
				totalNumPheno = 15;
				break;
			case 2:
				totalNumPheno = 6;
				break;
			case 3:
				totalNumPheno = 7;
				break;
			}
			
			// query the number of onetime observations have been made by each species
			Cursor pheno_cur = ot.rawQuery("SELECT COUNT(plant_id) as cnt FROM oneTimeObservation WHERE plant_id=" + cursor.getInt(1) + ";", null);
			while(pheno_cur.moveToNext()) {
				pheno_current = pheno_cur.getInt(0);
			}
			
			pheno_cur.close();
			
			pheno_cur = ot.rawQuery("SELECT synced FROM oneTimeObservation WHERE plant_id=" + cursor.getInt(1) + ";", null);
			while(pheno_cur.moveToNext()) {
				if(pheno_cur.getInt(0) == SyncDBHelper.SYNCED_NO) {
					synced = SyncDBHelper.SYNCED_NO;
					break;
				}
			}
			
			pheno_cur.close();
			
					
			/*
			 *  If site_id == 0 meaning one-time observations are added to the database in "OneTimeMain.class" activity
			 */
			
			if(!header) {
				pi = new PlantItem(resID, cursor.getString(5), cursor.getString(6), speciesID,
						cursor.getInt(3), cursor.getInt(4), pheno_current, totalNumPheno, true, "", false, synced, cursor.getInt(8));
				/*
				 *  Make header true (not called any more)
				 */
				header = true;
			}
			else {
				pi = new PlantItem(resID, cursor.getString(5), cursor.getString(6), speciesID,
						cursor.getInt(3), cursor.getInt(4), pheno_current, totalNumPheno, false, "", false, synced, cursor.getInt(8));
			}
					
			arPlantItem.add(pi);
		}
		
		cursor.close();
		onetime.close();
		ot.close();

	}

	protected void onListItemClick(View v, int position, long id){
		//Intent intent = new Intent(this, PlantInfo.class);
		
		Log.i("K", "POSITION : " + position);
		
		// if the position is in the pbb-list and the species id is 999 (which is unknown)
		if((position <= onetime_start_point - 1) && arPlantItem.get(position).SpeciesID == 999) {
			Toast.makeText(PlantList.this, getString(R.string.DoSyncFirst_For_UnknownPlant), Toast.LENGTH_SHORT).show();
		}
		else {
			
			Log.i("K", "ONETIME start point : " + onetime_start_point);
			
			// position > onetime_start_point - 1 => means the position is from the quick capture list
			if(position > onetime_start_point - 1) {
	
				int click_pos = Quick_capture_click_position(position);
				
				Intent intent = new Intent(this, GetPhenophase_OneTime.class);
				intent.putExtra("id", click_pos);
				startActivity(intent);

			}
			else {
				Log.i("K", "POSITION -> SPECIES_ID : " + arPlantItem.get(position).SpeciesID);
				Intent intent = new Intent(this, GetPhenophase_PBB.class);
				intent.putExtra("species_id", arPlantItem.get(position).SpeciesID);
				intent.putExtra("site_id", arPlantItem.get(position).SiteID);
				intent.putExtra("protocol_id", arPlantItem.get(position).ProtocolID);
				intent.putExtra("cname", arPlantItem.get(position).CommonName);
				intent.putExtra("sname", arPlantItem.get(position).SpeciesName);
				intent.putExtra("from", Values.FROM_PLANT_LIST);
				startActivity(intent);
			}
		}
	}
	
	protected boolean onLongListItemClick(View v, int position, long id) {
		pos = position;
		
		Log.i("K", "arPlantItem.get(pos).SpeciesID : " + arPlantItem.get(pos).SpeciesID);

		new AlertDialog.Builder(PlantList.this)
		.setTitle("Select one")
		.setNegativeButton("Back", null)
		.setItems(R.array.plantlist, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String[] category = getResources().getStringArray(R.array.plantlist);
				// so far there are two items - delete species, edit species
				// not allow to change the name of species if the species is from the official ppb-lists
				// deleting item not used
				if(category[which].equals("Delete Species")) {
					confirmDialog();
				}
				else if(category[which].equals("Add Same Species")){
					
					/*
					 * If user choose Monitored Plant 
					 */
					if(pos <= onetime_start_point - 1) {
						speciesID = arPlantItem.get(pos).SpeciesID;
						commonName = arPlantItem.get(pos).CommonName;
						protocolID = arPlantItem.get(pos).ProtocolID;
						chooseSiteDialog();
					}
					/*
					 * else, user choose Quick Shared Plant
					 */
					else {
						new AlertDialog.Builder(PlantList.this)
						.setTitle(getString(R.string.Menu_addQCPlant))
						.setMessage(getString(R.string.Start_Shared_Plant))
						.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								/*
								 * Move to QuickCapture
								 */
								Intent intent = new Intent(PlantList.this, QuickCapture.class);
								intent.putExtra("from", Values.FROM_QUICK_CAPTURE_ADD_SAMESPECIES);
								intent.putExtra("species_id", arPlantItem.get(pos).SpeciesID);
								intent.putExtra("cname", arPlantItem.get(pos).CommonName);
								intent.putExtra("sname", arPlantItem.get(pos).SpeciesName);
								intent.putExtra("protocol_id", arPlantItem.get(pos).ProtocolID);
								intent.putExtra("category", arPlantItem.get(pos).Category);
								
								startActivity(intent);
							}
						})
						.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(PlantList.this, GetPhenophase.class);
								intent.putExtra("camera_image_id", "");
								intent.putExtra("from", Values.FROM_QUICK_CAPTURE_ADD_SAMESPECIES);
								intent.putExtra("species_id", arPlantItem.get(pos).SpeciesID);
								intent.putExtra("cname", arPlantItem.get(pos).CommonName);
								intent.putExtra("sname", arPlantItem.get(pos).SpeciesName);
								intent.putExtra("protocol_id", arPlantItem.get(pos).ProtocolID);
								intent.putExtra("category", arPlantItem.get(pos).Category);
								startActivity(intent);
							}
						})
						.setNegativeButton(getString(R.string.Button_Cancel), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
							}
						})
						.show();
					}
				}
				else {
					/*
					 * if pbb list chosen and 0 < species_id < 77, we don't allow users to change the value
					 */
					if((pos <= onetime_start_point - 1) && arPlantItem.get(pos).SpeciesID > 0 && arPlantItem.get(pos).SpeciesID < 77) {
						Toast.makeText(PlantList.this, getString(R.string.Cannot_Change), Toast.LENGTH_SHORT).show();
					}
					else {
						dialog(arPlantItem.get(pos).SpeciesID, arPlantItem.get(pos).SiteID);	
					}
				}
			}
		})
		.show();
		
		return true;
	}
	
	private void chooseSiteDialog() {
		/*
		 * Pop up choose site dialog box
		 */
		helper = new FunctionsHelper();
		seqUserSite = helper.getUserSite(PlantList.this);
		mapUserSiteNameID = helper.getUserSiteIDMap(PlantList.this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setCancelable(true)
		.setItems(seqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			
				int new_plant_site_id = mapUserSiteNameID.get(seqUserSite[which].toString());
				String new_plant_site_name = seqUserSite[which].toString();
				
				if(new_plant_site_name == "Add New Site") {
					Intent intent = new Intent(PlantList.this, AddSite.class);
					intent.putExtra("species_id", speciesID);
					intent.putExtra("cname", commonName);
					intent.putExtra("protocol_id", protocolID);
					intent.putExtra("from", Values.FROM_PLANT_LIST);
					
					startActivity(intent);
				}
				else {
					if(helper.checkIfNewPlantAlreadyExists(speciesID, new_plant_site_id, PlantList.this)){
						Toast.makeText(PlantList.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{

						if(helper.insertNewMyPlantToDB(PlantList.this, speciesID, commonName, new_plant_site_id, new_plant_site_name, protocolID)){
							Intent intent = new Intent(PlantList.this, PlantList.class);
							Toast.makeText(PlantList.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							
							/*
							 * Clear all stacked activities.
							 */
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(PlantList.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
	
	public void confirmDialog() {
		new AlertDialog.Builder(PlantList.this)
		.setTitle("Confirm")
		.setMessage("Delete the species?")
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try{
					SyncDBHelper syncDBHelper = new SyncDBHelper(PlantList.this);
					SQLiteDatabase syncDB = syncDBHelper.getWritableDatabase();
					
					OneTimeDBHelper otDBHelper = new OneTimeDBHelper(PlantList.this);
					SQLiteDatabase onetime = otDBHelper.getWritableDatabase();
					
					// if the position is in the pbb list
					if(pos <= onetime_start_point - 1) {
						syncDB.execSQL("UPDATE my_plants SET active = 0 AND synced = " + SyncDBHelper.SYNCED_NO + " WHERE species_id=" + arPlantItem.get(pos).SpeciesID 
								+ " AND site_id=" + arPlantItem.get(pos).SiteID + ";");
						syncDB.execSQL("UPDATE my_plants SET synced = " + SyncDBHelper.SYNCED_NO + " WHERE species_id=" + arPlantItem.get(pos).SpeciesID 
								+ " AND site_id=" + arPlantItem.get(pos).SiteID + ";");
						syncDB.execSQL("DELETE FROM my_observation WHERE species_id=" + arPlantItem.get(pos).SpeciesID + " AND site_id=" + arPlantItem.get(pos).SiteID);
					}
					// else if the position is in the quick capture list
					else {
						// delete the onetime species_
						int click_pos = Quick_capture_click_position(pos);
						onetime.execSQL("UPDATE oneTimePlant SET active = 0, synced = " + SyncDBHelper.SYNCED_NO + " WHERE plant_id=" + click_pos);
						onetime.execSQL("DELETE FROM oneTimeObservation WHERE plant_id=" + click_pos);
						//Toast.makeText(PlantList.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
					}
					
					syncDB.close();
					syncDBHelper.close();
					
					onetime.close();
					otDBHelper.close();
						
					Toast.makeText(PlantList.this, getString(R.string.Item_deleted), Toast.LENGTH_SHORT).show();
						
					Intent intent = new Intent(PlantList.this, PlantList.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					finish();
					startActivity(intent);
				}
				catch(Exception e){
					Log.e(TAG,e.toString());
				}				
			}
		})
		.setNegativeButton("No", null)
		.show();
	}
	
	
	public int Quick_capture_click_position(int position) {
		OneTimeDBHelper onetime = new OneTimeDBHelper(PlantList.this);
		SQLiteDatabase onetimeDB  = onetime.getReadableDatabase();
		Cursor cursor = onetimeDB.rawQuery("SELECT plant_id FROM oneTimePlant WHERE active = 1;", null);
		
		int query_count = 1;
		int getID = 0;

		int onetime_point = position - onetime_start_point + 1;
		Log.i("K", "ONETIME POINT : " + onetime_point);
		
		while(cursor.moveToNext()) {
			if(onetime_point == query_count) {
				getID = cursor.getInt(0);
			}
			query_count++;
		}
		
		cursor.close();
		onetimeDB.close();
		
		return getID;
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
					// if the position is in the pbb list
					if(pos <= onetime_start_point - 1) { 
						SyncDBHelper syncDBHelper = new SyncDBHelper(PlantList.this);
						SQLiteDatabase syncDB  = syncDBHelper.getWritableDatabase();
						
						Log.i("K", "UPDATE my_plants SET common_name=\"" + common_name 
								+ "\" WHERE species_id=" + dialog_species_id 
								+ ";");

						syncDB.execSQL("UPDATE my_plants SET common_name=\"" + common_name 
								+ "\" WHERE species_id=" + dialog_species_id + " AND site_id =" + dialog_site_id 
								+ ";");
						
						syncDB.execSQL("UPDATE my_plants SET active=2 WHERE species_id=" + dialog_species_id + ";");
						syncDB.execSQL("UPDATE my_plants SET synced=9 WHERE species_id=" + dialog_species_id + ";");

						syncDBHelper.close();

					}
					else {
						
						OneTimeDBHelper onetimeDB = new OneTimeDBHelper(PlantList.this);
						SQLiteDatabase oneDB = onetimeDB.getWritableDatabase();
						
						int click_id = Quick_capture_click_position(pos);
						// update cname
						oneDB.execSQL("UPDATE oneTimePlant SET cname=\"" + common_name +
								"\" WHERE plant_id=" + click_id + ";");
						// change synced to 9
						oneDB.execSQL("UPDATE oneTimePlant SET synced=9 WHERE plant_id=" + click_id + ";");
						
						onetimeDB.close();
						oneDB.close();
						
					}
					
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
		
	/*
	 * Menu option(non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ADD_PLANT, 0, getString(R.string.Menu_addPlant)).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, MENU_ADD_QC_PLANT, 0, getString(R.string.Menu_addQCPlant_Menu)).setIcon(android.R.drawable.ic_menu_camera);
		menu.add(0, MENU_SYNC, 0, getString(R.string.Menu_sync)).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, MENU_HELP, 0, getString(R.string.Menu_help)).setIcon(android.R.drawable.ic_menu_help);
			
		return true;
	}
	
	/*
	 * Menu option selection handling(non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case MENU_ADD_PLANT:
				intent = new Intent(PlantList.this, OneTimeMain.class);
				intent.putExtra("from", Values.FROM_PLANT_LIST);
				startActivity(intent);
				return true;
			case MENU_ADD_QC_PLANT:
				/*
				 * Ask users if they are ready to take a photo.
				 */
				new AlertDialog.Builder(PlantList.this)
				.setTitle(getString(R.string.Menu_addQCPlant))
				.setMessage(getString(R.string.Start_Shared_Plant))
				.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						/*
						 * Move to QuickCapture
						 */
						Intent intent = new Intent(PlantList.this, QuickCapture.class);
						intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
						startActivity(intent);
					}
				})
				.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(PlantList.this, OneTimeMain.class);
						intent.putExtra("camera_image_id", "");
						intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
						startActivity(intent);
					}
				})
				.setNegativeButton(getString(R.string.Button_Cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();

				return true;
			case MENU_SYNC:
				intent = new Intent(PlantList.this, Sync.class);
				intent.putExtra("sync_instantly", true);
				intent.putExtra("from", Values.FROM_PLANT_LIST);
				startActivity(intent);
				finish();
				return true;
			case MENU_HELP:
				intent = new Intent(PlantList.this, Help.class);
				intent.putExtra("from", Values.FROM_PLANT_LIST);
				startActivity(intent);
				return true;
		}
		return false;
	}

	
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
			Bitmap icon = null;
			
			/*
			 * If the position is in onetime table,
			 * 
			 */
			if(position >= onetime_start_point) {
				/*
				 * Meaning it is from UCLA Tree Lists
				 */
				if(arSrc.get(position).Picture == 0) {
					
					/*
					 * Call images from the TREE_PATH
					 */
					String imagePath = Values.TREE_PATH + arSrc.get(position).SpeciesID + ".jpg";
					File checkExistFile = new File(imagePath);
					if(checkExistFile.exists()) {
						icon = overlay(BitmapFactory.decodeFile(imagePath));
					}
					else {
						//imagePath = Values.TREE_PATH + "s1000.jpg";
						
						icon = overlay(BitmapFactory.decodeFile(imagePath));
					}
					
					
				}
				/*
				else if(arSrc.get(position).Picture == Values.WHATSINVASIVE_LIST + Values.COMPLICATED) {
					
					
				}
				*/
				else {
					icon = overlay(BitmapFactory.decodeResource(getResources(), arSrc.get(position).Picture));
				}
			}
			else {
				icon = overlay(BitmapFactory.decodeResource(getResources(), arSrc.get(position).Picture));
			}
			
			/*
			 *  If not synced,
			 *  put the icon on the species image.
			 *  
			 */
			if(arSrc.get(position).Synced == SyncDBHelper.SYNCED_NO) {
				icon = overlay(icon, BitmapFactory.decodeResource(getResources(), R.drawable.unsynced));
			}
			
			/*
			 *  Show overlayed icon
			 */
			img.setImageBitmap(icon);
		
			
			TextView textname = (TextView)convertView.findViewById(R.id.commonname);
			textname.setText(arSrc.get(position).CommonName);
			
			TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
			if(arSrc.get(position).Monitor) {
				textdesc.setText(arSrc.get(position).SiteName);
				textdesc.setVisibility(View.VISIBLE);
			}
			else {
				textdesc.setVisibility(View.GONE);
			}
			
			/*
			 *  Call View from the xml and link the view to current position.
			 */
			View thumbnail = convertView.findViewById(R.id.wrap_icon);
			thumbnail.setTag(arSrc.get(position));
			thumbnail.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					PlantItem pi = (PlantItem)v.getTag();
					
					Intent intent = new Intent(PlantList.this, SpeciesDetail.class);
					intent.putExtra("id", pi.SpeciesID);
					if(pi.Picture == 0) {
						intent.putExtra("category", Values.TREE_LISTS_QC);
					}
					else {
						intent.putExtra("category", Values.NORMAL_QC);
					}
					
					startActivity(intent);
				}
			});
			
			/*
			 * Show the current phenophase observed and total phenophase
			 */
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
	
	private Bitmap overlay(Bitmap... bitmaps) {
		
		if (bitmaps[0].equals(null))
			return null;

		Bitmap bmOverlay = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_4444);

		Canvas canvas = new Canvas(bmOverlay);
		for (int i = 0; i < bitmaps.length; i++)
			canvas.drawBitmap(bitmaps[i], new Matrix(), null);

		return bmOverlay;
	}
	
    /*
     *  or when user press back button(non-Javadoc)
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     * 
     */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			Intent intent = new Intent(PlantList.this, MainPage.class);
			
			/*
			 *  Remove all previous message, we don't need anything when we go back to mainpage
			 *  
			 */
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;
		}
		return false;
	}
}


