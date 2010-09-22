package cens.ucla.edu.budburst.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SyncDBHelper extends SQLiteOpenHelper{
	
	//Constants for 'synced'
	public static final int IMG_FILE_NEED_TO_BE_DOWNLOADED = 0;
	public static final int SYNCED_YES = 5;
	public static final int SYNCED_NO = 9;
	
	public SyncDBHelper(Context context){
		super(context, "syncBudburst.db", null, 12);
	}
	
	public void onCreate(SQLiteDatabase db){
		
		//This table stores plant list of user.
		db.execSQL("CREATE TABLE my_plants (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"species_id NUMERIC," +
				"site_id NUMERIC," +
				"site_name TEXT," +
				"protocol_id NUMERIC," +
				"synced NUMERIC);");
		//If synced is 
		// : 5, then this plant is synced with server
		// : 9, then this plant is new added plant
		
		//This table stores observation.
		db.execSQL("CREATE TABLE my_observation (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"species_id NUMERIC," +
				"site_id NUMERIC," +
				"phenophase_id NUMERIC," +
				"image_id NUMERIC," +
				"time TEXT," +
				"note TEXT," +
				"synced NUMERIC);");
		//If syned is
		// : 0, then observation image file has not been downloaded. 
		//		So the img files need to be downloaded.
		// : 5, then observation image file has been downloaded.
		// : 9, then this observation is new, and needs to upload for SYNC.
		
		//This table stores site list of user.
		db.execSQL("CREATE TABLE my_sites (" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"site_id TEXT," +
				"site_name TEXT," +
				"latitude TEXT," +
				"longitude TEXT," +
				"city TEXT," +
				"state TEXT," +
				"zipcode TEXT," +
				"country TEXT," +
				"comments TEXT, " +
				"synced NUMERIC);");
		//If syned is
		// : 5, then this site is synced with server
		// : 9, then this site is new, and needs to upload for SYNC.
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL("DROP TABLE IF EXISTS my_plants;");
		db.execSQL("DROP TABLE IF EXISTS my_observation;");
		db.execSQL("DROP TABLE IF EXISTS my_sites;");
		
		onCreate(db);
	}
	
	public void clearAllTable(Context cont){
		SyncDBHelper dbhelper = new SyncDBHelper(cont);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		
		db.execSQL("DELETE FROM my_plants;");
		db.execSQL("DELETE FROM my_observation;");
		db.execSQL("DELETE FROM my_sites;");
		
		dbhelper.close();
	}
}
