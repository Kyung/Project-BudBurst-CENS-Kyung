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
				"dt_taken TEXT," +
				"notes TEXT," +
				"photo_name TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
