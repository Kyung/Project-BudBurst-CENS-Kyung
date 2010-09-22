//Excerpted From "http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/"
//This is a database helper class that retrieves static database, which is in assets folder.

package cens.ucla.edu.budburst.helper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StaticDBHelper extends SQLiteOpenHelper{
 
	final private String TAG = "StaticDBHelper";
	
	//The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/cens.ucla.edu.budburst/databases/";
    private static String DB_NAME = "staticBudburst.db";
    private SQLiteDatabase myDataBase; 
    private final Context myContext;
 
    public StaticDBHelper(Context context) {
    	 
    	super(context, DB_NAME, null, 1);
        this.myContext = context;
    }	
    
    public void createDataBase() throws IOException{
    	 
    	boolean dbExist = checkDataBase();
 
    	if(dbExist){
    		//do nothing - database already exist
    	}else{
     		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
 
        	try {
     			copyDataBase();
     		} catch (IOException e) {
     			Log.e(TAG, e.toString());
         		throw new Error("Error copying database");
         	}
    	}
     }
    

	
	private boolean checkDataBase(){
		 
		SQLiteDatabase checkDB = null;
	
		try{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	
		}catch(SQLiteException e){
	
			Log.e(TAG, e.toString());
			//database does't exist yetO.
		}
	
		if(checkDB != null){
			checkDB.close();
		}
	
		return checkDB != null ? true : false;
	}
	
	private void copyDataBase() throws IOException{
		 
		//Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);
	
		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;
	
		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);
	
		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}
	
		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	
	}
	
    public void openDataBase() throws SQLException{
    	 
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    }
 
    @Override
	public synchronized void close() {
 
    	    if(myDataBase != null)
    		    myDataBase.close();
 
    	    super.close();
 
	}
    
	@Override
	public void onCreate(SQLiteDatabase db) {
 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}

}