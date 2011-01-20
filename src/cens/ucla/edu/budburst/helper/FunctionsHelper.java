package cens.ucla.edu.budburst.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

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
		
		Cursor cc = syncDB.rawQuery("SELECT site_name, official FROM my_sites;", null);
		while(cc.moveToNext()) {
			Log.i("K", "site_name : " + cc.getString(0) + " , official : " + cc.getInt(1));
		}
		
		cc.close();
		
		CharSequence[] arrUsersite;
		try{
			Cursor cursor = syncDB.rawQuery("SELECT site_name FROM my_sites WHERE official=" + Values.OFFICIAL, null);
			
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
			double lat, double lng, float accuracy, String image_id, String notes) {
		try{
			Log.i("K", "INSIDE THE insertNewObservation function");
			
			OneTimeDBHelper onetime = new OneTimeDBHelper(context);
			SQLiteDatabase ot = onetime.getWritableDatabase();
			
			Log.i("K", "image_id : " + image_id);
			
			String dt_taken = new SimpleDateFormat("dd MMMMM yyy").format(new Date());

			// species_id, site_id, cname, sname, lat, lng, accuracy, dt_taken, notes, image_name, synced
			ot.execSQL("INSERT INTO onetimeob_observation VALUES(" +
					plant_id + "," +
					phenophase_id + "," +
					"" + lat + "," +
					"" + lng + "," +
					"" + accuracy + "," +
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
			
			Cursor c = ot.rawQuery("SELECT MAX(plant_id) as max FROM onetimeob;", null);
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
			
			int plant_id = getID(context);
			plant_id += 1;
			
			Log.i("K", "plant_id : " + plant_id);
			
			// _id, plant_id, species_id, site_id, protocol_id, cname, sname, active, synced
			ot.execSQL("INSERT INTO onetimeob VALUES(" +
					"null," +
					plant_id + "," +
					speciesid + "," +
					siteid + "," +
					protocolid + "," +
					"'" + cname + "',"+
					"'" + sname + "',"+
					1 + "," + // active
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
		Cursor cursor = syncDB.rawQuery("SELECT site_name, site_id FROM my_sites WHERE official =" + Values.OFFICIAL ,null);
		while(cursor.moveToNext()){
			localMapUserSiteNameID.put(cursor.getString(0), cursor.getInt(1));
		}
		
		
		//Close DB and cursor
		cursor.close();
		//syncDB.close();
		syncDBHelper.close();
	
		return localMapUserSiteNameID;
	}
	
	public boolean isGPSEnabled(Context context) {
		LocationManager lm = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
		List<String> accessibleProviders = lm.getProviders(true);
		return accessibleProviders != null && accessibleProviders.size() > 0;
		
	}
	
	
	public String hexEncode(byte[] aInput){
		   StringBuilder result = new StringBuilder();
		   char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m'};
		   for ( int idx = 0; idx < 4; ++idx) {
			   byte b = aInput[idx];
			   result.append( digits[ (b&0xf0) >> 4 ] );
			   result.append( digits[ b&0x0f] );
		   }
		   return result.toString();
	}
	
	public Bitmap resizeImage(Context context, String path){
		FileInputStream fin = null;
		BufferedInputStream buf = null;
		
		File existFile = new File(path);
		if(!existFile.exists()) {
			Toast.makeText(context, "Error occurs while processing the species list. ", Toast.LENGTH_SHORT).show();
			return null;
		}
		
    	Log.i("K", "PATH : " + path);
    	
		try {
			fin = new FileInputStream(path);
			Log.i("K", "FILE INPUT : " + fin);
			
			
			buf = new BufferedInputStream(fin);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bitmap bitmap = BitmapFactory.decodeStream(buf);
		
		Log.i("K", "" + bitmap);
		
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int newWidth = 60;
		int newHeight = 60;
		
		//Bitmap thumb = BitmapFactory.decodeFile(path, options);
		
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		
		Log.i("K", "SCALE WIDTH : " + scaleWidth);
		Log.i("K", "SCALE HEIGHT : " + scaleHeight);
		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		
		Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

    	return resized;
    }

}
