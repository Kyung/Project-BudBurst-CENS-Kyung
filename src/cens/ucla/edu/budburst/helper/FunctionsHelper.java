package cens.ucla.edu.budburst.helper;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FunctionsHelper {
	public boolean checkIfNewPlantAlreadyExists(int speciesid, int siteid, Context context){

		SyncDBHelper syncDBHelper = new SyncDBHelper(context);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase();
				
		try{
			Cursor cursor = syncDB.rawQuery("SELECT species_id FROM my_plants" +
					" WHERE species_id = " + speciesid +
					" AND active = 1" +
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
			Log.e("K",e.toString());
			return false;
		}
		finally{
			syncDBHelper.close();
		}
	}
	
	public CharSequence[] getUserSite(Context context){
		
		SyncDBHelper syncDBHelper = new SyncDBHelper(context);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase();
		CharSequence[] arrUsersite;
		try{
			Cursor cursor = syncDB.rawQuery("SELECT site_name FROM my_sites;", null);
			
			arrUsersite = new String[cursor.getCount()+1];
			int i=0;
			while(cursor.moveToNext()){
				arrUsersite[i++] = cursor.getString(0);
				
				Log.i("K", "SITE : " + cursor.getString(0));
			}
			arrUsersite[i++] = "Add New Site";
			
			cursor.close();
			return arrUsersite;
		}
		catch(Exception e){
			Log.e("K",e.toString());
			return null;
		}
		finally{
			syncDBHelper.close();
		}
	}

	public boolean insertNewObservation(Context context, int plant_id, int phenophase_id, 
			double lat, double lng, String image_id, String dt_taken, String notes) {
		try{
			Log.i("K", "INSIDE THE insertNewPlantDB function");
			
			OneTimeDBHelper onetime = new OneTimeDBHelper(context);
			SQLiteDatabase ot = onetime.getWritableDatabase();
			
			// species_id, site_id, cname, sname, lat, lng, dt_taken, notes, image_name, synced
			ot.execSQL("INSERT INTO onetimeob_observation VALUES(" +
					plant_id + "," +
					phenophase_id + "," +
					"" + lat + "," +
					"" + lng + "," +
					"'" + image_id +"'," +
					"'" + dt_taken + "'," +
					"'" + notes + "'," +
					SyncDBHelper.SYNCED_NO + ");"
					);
			onetime.close();
			ot.close();
			return true;
		}
		catch(Exception e){
			Log.e("K",e.toString());
			return false;
		}
	}
	
	public int getID(Context context) {
		
		int return_id = 0;
		
		try {
			Log.i("K", "INSIDE the getID function");
			
			OneTimeDBHelper onetime = new OneTimeDBHelper(context);
			SQLiteDatabase ot = onetime.getReadableDatabase();
			
			Cursor c = ot.rawQuery("SELECT MAX(_id) as max FROM onetimeob;", null);
			while(c.moveToNext()) {
				return_id = c.getInt(0);
			}
			
			onetime.close();
			ot.close();
			c.close();
			return return_id;
		}
		catch(Exception e) {
			return 0;
		}
	}
	
	public boolean insertNewPlantToDB(Context context, int speciesid, int siteid, int protocolid, String cname, String sname){
		
		try{
			Log.i("K", "INSIDE THE insertNewPlantDB function");
			
			OneTimeDBHelper onetime = new OneTimeDBHelper(context);
			SQLiteDatabase ot = onetime.getWritableDatabase();
			
			// species_id, site_id, cname, sname, lat, lng, dt_taken, notes, image_name, synced
			ot.execSQL("INSERT INTO onetimeob VALUES(" +
					"null," +
					speciesid + "," +
					siteid + "," +
					protocolid + "," +
					"'" + cname + "',"+
					"'" + sname + "',"+
					//"" + lat + "," +
					//"" + lng + "," +
					//"'" + dt_taken + "'," +
					//"'" + notes + "'," +
					//"'" + image_name + "'," +
					SyncDBHelper.SYNCED_NO + ");"
					);
			onetime.close();
			ot.close();
			return true;
		}
		catch(Exception e){
			Log.e("K",e.toString());			
			return false;
		}
	}
	
	public HashMap<String, Integer> getUserSiteIDMap(Context context){

		HashMap<String, Integer> localMapUserSiteNameID = new HashMap<String, Integer>(); 
		
		//Open plant list db from static db
		SyncDBHelper syncDBHelper = new SyncDBHelper(context);
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
