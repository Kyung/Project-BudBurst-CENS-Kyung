package cens.ucla.edu.budburst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;

public class AddPlant extends ListActivity{
	private final String TAG = "AddPlant";
	private ArrayList<PlantItem> arPlantList;
	
	private int mSelect = 0;
	private Integer new_plant_species_id;
	private Integer new_plant_site_id; 
	private String new_plant_site_name;
	
	
	CharSequence[] seqUserSite;
	
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>(); 
		
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addplant);
		setTitle("Add plant");
		
		//Check if site table is empty
		SyncDBHelper syncDBHelper = new SyncDBHelper(AddPlant.this);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase();
		
		Cursor cursor = syncDB.rawQuery("SELECT site_id FROM my_sites;", null);
		Log.d(TAG, String.valueOf(cursor.getCount()));
		if(cursor.getCount() == 0)
		{
			Toast.makeText(AddPlant.this, "Please add your site first.", 
					Toast.LENGTH_LONG).show();
			cursor.close();
			syncDBHelper.close();
			Intent intent = new Intent(AddPlant.this, PlantList.class);
			startActivity(intent);
			finish();		
			return;
		}else{
			syncDBHelper.close();
			cursor.close();
		}
		
		//Get all plant list
		//Open plant list db from static db
		StaticDBHelper staticDBHelper = new StaticDBHelper(this);
		SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase(); 
		
		//Rereive syncDB and add them to arUserPlatList arraylist
		arPlantList = new ArrayList<PlantItem>();
 		cursor = staticDB.rawQuery("SELECT _id, species_name, common_name FROM species ORDER BY common_name;",null);
		while(cursor.moveToNext()){
			Integer id = cursor.getInt(0);
			String species_name = cursor.getString(1);
			String common_name = cursor.getString(2);
						
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
			
			PlantItem pi;
			//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
			pi = new PlantItem(resID, common_name, species_name, id);
			arPlantList.add(pi);
		}
		
		MyListAdapter mylistapdater = new MyListAdapter(this, R.layout.plantlist_item, arPlantList);
		ListView MyList = getListView(); 
		MyList.setAdapter(mylistapdater);
		
		//Close DB and cursor
		cursor.close();
		staticDBHelper.close();
		
		
		//Get User site name and id using Map.

		mapUserSiteNameID = getUserSiteIDMap();
		Iterator<String> itr = mapUserSiteNameID.keySet().iterator();
		while(itr.hasNext()){
			Log.d(TAG, itr.next());
		}
		
	}
	
	public String[] getUserSite(){
		
		SyncDBHelper syncDBHelper = new SyncDBHelper(this);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase();
		String[] arrUsersite;
		try{
			Cursor cursor = syncDB.rawQuery("SELECT site_name FROM my_sites;", null);
			
			arrUsersite = new String[cursor.getCount()];
			int i=0;
			while(cursor.moveToNext()){
				arrUsersite[i++] = cursor.getString(0);
			}
			cursor.close();
			return arrUsersite;
		}
		catch(Exception e){
			Log.e(TAG,e.toString());
			return null;
		}
		finally{
			syncDBHelper.close();
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		new_plant_species_id = arPlantList.get(position).SpeciesID;
		
		//Retreive user sites from data base.
		//seqUserSite = mapUserSiteNameID.keySet().toArray(new String[0]);
		seqUserSite = getUserSite();
		
		//Pop up choose site dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose site")
		.setSingleChoiceItems(seqUserSite, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mSelect = which;				
				new_plant_site_id = mapUserSiteNameID.get(seqUserSite[mSelect].toString());
				new_plant_site_name = seqUserSite[mSelect].toString();
			}
		})
		.setPositiveButton("Select", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(new_plant_site_id == null){
					Toast.makeText(AddPlant.this, " Please select site.", Toast.LENGTH_SHORT).show();
				}
				else{
					if(checkIfNewPlantAlreadyExists(new_plant_species_id, new_plant_site_id)){
						Toast.makeText(AddPlant.this, "The selected plant already exists in the site.", Toast.LENGTH_LONG).show();
					}else{
						if(insertNewPlantToDB(new_plant_species_id, new_plant_site_id, new_plant_site_name)){
							Intent intent = new Intent(AddPlant.this, PlantList.class);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(AddPlant.this, "Database error.", Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton("Cancel", null)
		.show();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			Intent intent = new Intent(AddPlant.this, PlantList.class);
			startActivity(intent);
			finish();
			return true;
	    }
		return super.onKeyDown(keyCode, event);
	}
	
	public boolean checkIfNewPlantAlreadyExists(int speciesid, int siteid){

		SyncDBHelper syncDBHelper = new SyncDBHelper(this);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase();
		
		try{
			Cursor cursor = syncDB.rawQuery("SELECT species_id FROM my_plants " +
					"WHERE species_id = " + speciesid +
					" AND site_id = " + siteid, null);
			if(cursor.getCount() > 0){
				cursor.close();
				return true;
			}
			else{
				cursor.close();
				return false;
			}
		}
		catch(Exception e){
			Log.e(TAG,e.toString());
			return false;
		}
		finally{
			syncDBHelper.close();
		}
	}
	
	public boolean insertNewPlantToDB(int speciesid, int siteid, String sitename){
		try{
			SyncDBHelper syncDBHelper = new SyncDBHelper(this);
			SQLiteDatabase syncDB = syncDBHelper.getWritableDatabase();
			
			syncDB.execSQL("INSERT INTO my_plants VALUES(" +
					"null," +
					speciesid + "," +
					siteid + "," +
					"'" + sitename + "',"+
					"0,"+
					SyncDBHelper.SYNCED_NO + ");"
					);
			syncDBHelper.close();
			return true;
		}
		catch(Exception e){
			Log.e(TAG,e.toString());
			return false;
		}
	}
	
	public HashMap<String, Integer> getUserSiteIDMap(){

		HashMap<String, Integer> localMapUserSiteNameID = new HashMap<String, Integer>(); 
		
		//Open plant list db from static db
		SyncDBHelper syncDBHelper = new SyncDBHelper(this);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase(); 
		
		//Rereive syncDB and add them to arUserPlatList arraylist
		Cursor cursor = syncDB.rawQuery("SELECT site_name, site_id FROM my_sites;",null);
		while(cursor.moveToNext()){
			localMapUserSiteNameID.put(cursor.getString(0), cursor.getInt(1));
		}
		
		
		//Close DB and cursor
		cursor.close();
		//syncDB.close();
		syncDBHelper.close();
	
		return localMapUserSiteNameID;
	}
}

