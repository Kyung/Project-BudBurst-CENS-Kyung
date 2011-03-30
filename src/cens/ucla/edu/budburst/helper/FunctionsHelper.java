package cens.ucla.edu.budburst.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;

import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;

import android.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.os.AsyncTask;
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
			ot.execSQL("INSERT INTO oneTimeObservation VALUES(" +
					plant_id + "," +
					phenophase_id + "," +
					"" + lat + "," +
					"" + lng + "," +
					"" + accuracy + "," +
					"\"" + image_id +"\"," +
					"\"" + dt_taken + "\"," +
					"\"" + notes + "\"," +
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
			
			Cursor c = ot.rawQuery("SELECT MAX(plant_id) as max FROM oneTimePlant;", null);
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
	
	public boolean insertNewMyPlantToDB(Context context, int speciesid, String speciesname, int siteid, String sitename, int protocol_id){

		int s_id = speciesid;
		SharedPreferences pref = context.getSharedPreferences("userinfo",0);
		
		if(speciesid == Values.UNKNOWN_SPECIES) {
			s_id = pref.getInt("other_species_id", 0);
			s_id++;
		}
		
		Log.i("K", "INSERT PROTOCOL_ID : " + protocol_id);
		
		try{
			SyncDBHelper syncDBHelper = new SyncDBHelper(context);
			SQLiteDatabase syncDB = syncDBHelper.getWritableDatabase();
			
			Log.i("K", "INSERTED STRING : " + "INSERT INTO my_plants VALUES(" +
					"null," +
					speciesid + "," +
					siteid + "," +
					"'" + sitename + "',"+
					protocol_id + "," + 
					"'" + speciesname + "'," +
					"1, " + // active 0(need to be removed), 1(nothing), 2(update the species)
					SyncDBHelper.SYNCED_NO + ");");
			
			syncDB.execSQL("INSERT INTO my_plants VALUES(" +
					"null," +
					speciesid + "," +
					siteid + "," +
					"'" + sitename + "',"+
					protocol_id + "," + 
					"'" + speciesname + "'," +
					"1, " + // active 0(need to be removed), 1(nothing), 2(update the species)
					SyncDBHelper.SYNCED_NO + ");"
					);
			
			if(speciesid == Values.UNKNOWN_SPECIES) {
				SharedPreferences.Editor edit = pref.edit();				
				edit.putInt("other_species_id", s_id);
				edit.commit();
			}
			
			syncDBHelper.close();
			return true;
		}
		catch(Exception e){
			return false;
		}
	}
	
	public boolean insertNewSharedPlantToDB(Context context, int speciesid, int siteid, int protocolid, String cname, String sname, int category){
		
		try{
			OneTimeDBHelper onetime = new OneTimeDBHelper(context);
			SQLiteDatabase ot = onetime.getWritableDatabase();
			
			int plant_id = getID(context);
			
			// for the next plant id
			plant_id += 1;
			
			// _id, plant_id, species_id, site_id, protocol_id, cname, sname, active, synced
			ot.execSQL("INSERT INTO oneTimePlant VALUES(" +
					"null," +
					plant_id + "," +
					speciesid + "," +
					siteid + "," +
					protocolid + "," +
					"\"" + cname + "\","+
					"\"" + sname + "\","+
					Values.ACTIVE_SPECIES + "," + // active
					category + "," + 
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
		localMapUserSiteNameID.put("Add New Site", 10000);

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
	
	
	public String hexEncode(Context context, byte[] aInput){
		   StringBuilder result = new StringBuilder();
		   char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m'};
		   for ( int idx = 0; idx < 4; ++idx) {
			   byte b = aInput[idx];
			   result.append( digits[ (b&0xf0) >> 4 ] );
			   result.append( digits[ b&0x0f] );
		   }
		   
		   return result.toString();
	}
	
	public Bitmap showImage(Context context, String path) {
		FileInputStream fin = null;
		BufferedInputStream buf = null;
		
		File existFile = new File(path);
		if(!existFile.exists()) {
			return null;
		}
    	
		try {
			fin = new FileInputStream(path);
			buf = new BufferedInputStream(fin);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(buf));

    	return bitmap;
	}
	
	public Bitmap resizeImage(Context context, String path){
		FileInputStream fin = null;
		BufferedInputStream buf = null;
		
		File existFile = new File(path);
		if(!existFile.exists()) {
			return null;
		}
		
    	Log.i("K", "PATH : " + path);
    	
		try {
			fin = new FileInputStream(path);
			buf = new BufferedInputStream(fin);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(buf));
		
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int newWidth = 60;
		int newHeight = 60;
		
		//Bitmap thumb = BitmapFactory.decodeFile(path, options);
		
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		
		Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

    	return resized;
    }
	
	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		
		@Override
		protected Bitmap doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
    /*
     * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
     * Source from : http://code.google.com/p/android-imagedownloader/source/checkout
     */
	
	static class FlushedInputStream extends FilterInputStream {

		protected FlushedInputStream(InputStream inputStream) {
			super(inputStream);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while(totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if(bytesSkipped == 0L) {
					int b = read();
					if(b < 0) {
						break; // we reached EOF
					}
					else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}
	
	public void download_image(String url, String fileName, String path) {
		
		URL imageURL;
		int read;
		
		try {
			imageURL = new URL(url);
			
			Object getContent = imageURL.getContent();
			
			Log.i("K", "getContent : " + getContent);
			HttpURLConnection conn = (HttpURLConnection)imageURL.openConnection();
			conn.connect();
			
			int len = conn.getContentLength();
			
			byte[] buffer = new byte[len];
			InputStream is = conn.getInputStream();
			FileOutputStream fos = new FileOutputStream(path + fileName + ".jpg");
			
			while ((read = is.read(buffer)) > 0) {
				fos.write(buffer, 0, read);
			}
			fos.close();
			is.close();
		}
		catch(Exception e) {
			
		}
	}
}
