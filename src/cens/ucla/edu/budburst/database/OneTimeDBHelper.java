package cens.ucla.edu.budburst.database;

import java.util.ArrayList;

import cens.ucla.edu.budburst.helper.PlantItem;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OneTimeDBHelper extends SQLiteOpenHelper {

	public OneTimeDBHelper(Context context){
		super(context, "onetimeBudburst.db", null, 36);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		Log.i("K", "make a oneTimePlant table");
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE oneTimePlant (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"plant_id NUMERIC," +
				"species_id NUMERIC," +
				"site_id NUMERIC," +
				"protocol_id NUMERIC," +
				"cname TEXT," +
				"sname TEXT," +
				"active NUMERIC," + // deleted(0) or not(1)
				"category NUMERIC, " +
				/*
				 * 0 - normal QC, 
				 * 1 - tree_lists,
				 * 2 - local budburst
				 * 3 - local invasive
				 * 4 - local native
				 * and more...
				 * 
				 * Note : category number is different from the category in the server.
				 * 
				 */
				"synced NUMERIC);");
		
		db.execSQL("CREATE TABLE oneTimeObservation (" +
				"plant_id NUMERIC," +
				"phenophase_id NUMERIC," +
				"lat NUMERIC," +
				"lng NUMERIC," +
				"accuracy NUMERIC," +
				"image_id TEXT," +
				"dt_taken TEXT," +
				"notes TEXT," +
				"synced NUMERIC);"
				);
		
		db.execSQL("CREATE TABLE speciesLists (" +
				"id NUMERIC, " +
				"cname TEXT, " +
				"sname TEXT, " +
				"text TEXT, " + 
				"image_name TEXT, " +
				"image_url TEXT);");
		
		db.execSQL("CREATE TABLE pbbFlickrLists (" +
				"common_name TEXT, " +
				"science_name TEXT, " +
				"phenophase TEXT, " + 
				"dt_taken TEXT, " +
				"lat TEXT, " + 
				"lon TEXT, " + 
				"distance NUMERIC);");
		
		db.execSQL("CREATE TABLE userDefineLists (" + 
				"id INTEGER PRIMARY KEY, " +
				"common_name TEXT, " +
				"science_name TEXT, " +
				"credit TEXT " +
				"); ");
		
		db.execSQL("CREATE TABLE localPlantLists (" +
				"category INTEGER, " +
				"common_name TEXT, " +
				"science_name TEXT, " +
				"county TEXT, " +
				"state TEXT, " +
				"usda_url TEXT, " +
				"photo_url TEXT, " +
				"copy_right TEXT); ");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS oneTimePlant");
		db.execSQL("DROP TABLE IF EXISTS oneTimeObservation");
		db.execSQL("DROP TABLE IF EXISTS speciesLists");
		db.execSQL("DROP TABLE IF EXISTS pbbFlickrLists");
		db.execSQL("DROP TABLE IF EXISTS userDefineLists");
		db.execSQL("DROP TABLE IF EXISTS localPlantLists");
		
		onCreate(db);
	}
	
	public void clearAllTable(Context cont){
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		
		db.execSQL("DELETE FROM oneTimePlant;");
		db.execSQL("DELETE FROM speciesLists;");
		db.execSQL("DELETE FROM oneTimeObservation;");
		
		db.close();
	 	dbhelper.close();
 	}
	
	public void clearUCLAtreeLists(Context cont) {
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		
		db.execSQL("DELETE FROM userDefineLists;");
		
		db.close();
	 	dbhelper.close();
	}
	
	public void clearLocalListAll(Context cont) {
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		
		db.execSQL("DELETE FROM localPlantLists;");
	
		db.close();
	 	dbhelper.close();
	}
	
	public void clearLocalListsByCategory(Context cont, int category) {
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		
		db.execSQL("DELETE FROM localPlantLists WHERE category=" + category + ";");
		
		db.close();
	 	dbhelper.close();
	}
	
	public int getTotalNumberOfQCOPlants(Context cont) {
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT * FROM oneTimePlant WHERE synced=" + SyncDBHelper.SYNCED_YES, null);
		int count = cursor.getCount();
		
		db.close();
		cursor.close();
		
		return count;
	}
	
	public int getTotalNumberOfQCObservations(Context cont) {
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT * FROM oneTimeObservation WHERE synced=" + SyncDBHelper.SYNCED_YES, null);
		int count = cursor.getCount();
		
		db.close();
		cursor.close();
		
		return count;
	}
	
	public ArrayList<PlantItem> getAllMyListInformation(Context cont) {
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getReadableDatabase();
		
		ArrayList<PlantItem> plantList = new ArrayList<PlantItem>();
		
		PlantItem pi = null;
		
		Cursor cursor = db.rawQuery("SELECT _id, species_id, plant_id, protocol_id, cname, sname FROM oneTimePlant;", null);
		Cursor cursor2 = null;
		while(cursor.moveToNext()) {
			
			 cursor2 = db.rawQuery("SELECT plant_id, phenophase_id, lat, lng, image_id, dt_taken, notes FROM oneTimeObservation WHERE plant_id = " + cursor.getInt(2) + " ORDER BY dt_taken;", null);
			 
			 int speciesID = cursor.getInt(1);
			 String commonName = cursor.getString(4);
			 String scienceName = cursor.getString(5);
			 
			 
			 while(cursor2.moveToNext()) {
				 int phenophaseID = cursor2.getInt(1);
				 double latitude = cursor2.getDouble(2);
				 double longitude = cursor2.getDouble(3);
				 String imageName = cursor2.getString(4);
				 String dtTaken = cursor2.getString(5);
				 String notes = cursor2.getString(6);
				 
				 pi = new PlantItem(speciesID, commonName, scienceName, phenophaseID, latitude, longitude, imageName, dtTaken, notes);
				 plantList.add(pi);
			 }
		}
		
		db.close();
		cursor.close();
		cursor2.close();
		
		return plantList;
	}
	
	public void clearLocalLists(Context cont) {
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		
		db.execSQL("DELETE FROM localPlantLists;");
		
		db.close();
	 	dbhelper.close();
	}
}
