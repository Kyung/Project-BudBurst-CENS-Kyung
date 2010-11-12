package cens.ucla.edu.budburst.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OneTimeDBHelper extends SQLiteOpenHelper {

	public OneTimeDBHelper(Context context){
		super(context, "onetimeBudburst.db", null, 1);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		Log.i("K", "make a onetimeob table");
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE onetimeob (" +
				"image_id NUMERIC," +
				"cname TEXT," +
				"sname TEXT," +
				"lat NUMERIC," +
				"lng NUMERIC," +
				"dt_taken TEXT," +
				"notes TEXT," +
				"photo_name TEXT," + 
				"uploaded TEXT);");
		
		db.execSQL("CREATE TABLE speciesLists (" +
				"id NUMERIC, " +
				"title TEXT, " +
				"cname TEXT, " +
				"sname TEXT, " +
				"text TEXT, " + 
				"image_url TEXT);");
		
		db.execSQL("CREATE TABLE popularLists (" +
				"pheno TEXT, " +
				"cname TEXT, " +
				"sname TEXT, " +
				"lat TEXT, " +
				"lng TEXT, " +
				"dt_taken TEXT, " +
				"c_count TEXT, " +
				"comments TEXT );");
		
		db.execSQL("CREATE TABLE flickrLists (" +
				"id NUMERIC, " +
				"secret TEXT, " +
				"farm TEXT, " +
				"title TEXT, " + 
				"dt_taken TEXT, " +
				"lat TEXT, " + 
				"lon TEXT, " + 
				"owner TEXT, " +
				"server TEXT, " +
				"distance NUMERIC, " +
				"category NUMERIC);");
		
		db.execSQL("CREATE TABLE pbbFlickrLists (" +
				"common_name TEXT, " +
				"science_name TEXT, " +
				"phenophase TEXT, " + 
				"dt_taken TEXT, " +
				"lat TEXT, " + 
				"lon TEXT, " + 
				"distance NUMERIC);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	public void clearAllTable(Context cont){
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		
		db.execSQL("DELETE FROM onetimeob;");
		db.execSQL("DELETE FROM speciesLists;");
		db.execSQL("DELETE FROM popularLists;");
		db.execSQL("DELETE FROM flickrLists;");
		
	 	dbhelper.close();
 	}
	
	public void clearPopularLists(Context cont) {
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		
		db.execSQL("DELETE FROM popularLists;");
		
	 	dbhelper.close();
	}
	
	public void clearFlickr(Context cont) {
		OneTimeDBHelper dbhelper = new OneTimeDBHelper(cont);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		
		db.execSQL("DELETE FROM flickrLists;");
		
	 	dbhelper.close();
	}

}
