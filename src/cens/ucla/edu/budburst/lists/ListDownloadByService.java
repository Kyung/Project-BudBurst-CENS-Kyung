package cens.ucla.edu.budburst.lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;

public class ListDownloadByService extends AsyncTask<ListItems, Integer, Void>{
	
	private SharedPreferences pref;
	private int category;
	
	@Override
	protected void onPreExecute() {
		File f = new File(HelperValues.LOCAL_LIST_PATH);
		if(!f.exists()) {
			f.mkdir();
		}
	}
	
	
	@Override
	protected Void doInBackground(ListItems... item) {
		// TODO Auto-generated method stub
		/*
		 * Change the boolean value of the category to TRUE
		 */
		// type=1 BudBurst  
		// type=2 WhatsInvasive
		// type=3 WhatsPoisonous
		// type=4 WhatsEndangered
		category = item[0].category;
		
		pref = item[0].context.getSharedPreferences("userinfo", 0);
		
		String url = "http://networkednaturalist.org/python_scripts/cens-dylan/list.py?lat=" 
			+ item[0].latitude
			+ "&lon=" + item[0].longitude 
			+ "&type=" + item[0].category;
			
		Log.i("K", "URL : " + url);
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		String result = "";
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
				StringBuilder result_str = new StringBuilder();
				String line = "";
				
				while((line = br.readLine()) != null){
					result_str.append(line);
				}
				
				result = result_str.toString();
				
				JSONObject jsonObj = new JSONObject(result);
				String county = jsonObj.getString("county");
				String state = jsonObj.getString("state");
				
				SharedPreferences.Editor edit = pref.edit();
				/*
				 * Save state and county values into SharedPreferences.
				 */
				edit = pref.edit();
				edit.putString("state", state);
				edit.putString("county", county);
				edit.commit();
				
				JSONArray jsonAry = jsonObj.getJSONArray("results");
				
				/*
				 * Open OneTime Database and delete the list first - by category
				 * After that, reINSERT the values into the database
				 * 
				 */
				
				OneTimeDBHelper otDBH = new OneTimeDBHelper(item[0].context);
				// Delete the list first - by category
				otDBH.clearLocalListsByCategory(item[0].context, item[0].category);
				
				// Delete all images in the folder
				//HelperFunctionCalls helper = new HelperFunctionCalls();
				//helper.deleteContents(HelperValues.LOCAL_LIST_PATH);
				
				Log.i("K", "Json Array Length : " + jsonAry.length());	
				SQLiteDatabase otDB = null;
				
				// ReINSERT the values
				for(int i = 0 ; i < jsonAry.length() ; i++) {
					
					try {
						otDB = otDBH.getWritableDatabase();
						
						/*
						 * Set the first char to upper case.
						 */
						String commonName = jsonAry.getJSONObject(i).getString("common");
						//Log.i("K", "commonName : " + commonName);
						
						/*
						 * If there's a common name, change the first char to upper case.
						 */
						if(!commonName.equals("")) {
							commonName = commonName.substring(0,1).toUpperCase() + commonName.substring(1);
						}
						else {
							commonName = "Unknown/Other";
						}

						int imageID = Integer.parseInt(jsonAry.getJSONObject(i).getString("id"));
						
						otDB.execSQL("INSERT INTO localPlantLists VALUES(" +
								item[0].category + ",\"" +
								commonName + "\",\"" +
								jsonAry.getJSONObject(i).getString("scientific") + "\",\"" +
								county + "\",\"" +
								state + "\",\"" +
								jsonAry.getJSONObject(i).getString("usda_url") + "\",\"" +
								jsonAry.getJSONObject(i).getString("image_url") + "\",\"" +
								jsonAry.getJSONObject(i).getString("image_copyright").replace("&amp;copy;", "") + "\"," +
								Integer.parseInt(jsonAry.getJSONObject(i).getString("id")) + ");"
							);
						
						// don't need to download budburst photos 
						// we are using our local budburst image for this.
						if(item[0].category != 1) {
							File checkFileExist = new File(HelperValues.LOCAL_LIST_PATH + imageID + ".jpg");
							if(!checkFileExist.exists()) {
								Log.i("K", "Need to download the local species");
								downloadImages(jsonAry.getJSONObject(i).getString("image_url"), imageID);
							}
						}
					}
					catch(SQLiteException ex) {
						ex.printStackTrace();
						otDB.close();
					}
				}
					
				// DB close
				otDB.close();
				otDBH.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		// TODO Auto-generated method stub
		return null;
	}
	
	private void downloadImages(String imageUrl, int imageID) {
		
		int read;
		try {
			
			Log.i("K", "ImageDownload : " + imageUrl);
			
			URL urls = new URL(imageUrl);
			
			Object getContent = urls.getContent();
			
			Log.i("K", "getContent : " + getContent);
			HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
			conn.connect();
			
			int len = conn.getContentLength();
			
			byte[] buffer = new byte[len];
			InputStream is = conn.getInputStream();
			FileOutputStream fos = new FileOutputStream(HelperValues.LOCAL_LIST_PATH + imageID + ".jpg");
			
			while ((read = is.read(buffer)) > 0) {
				fos.write(buffer, 0, read);
			}
			fos.close();
			is.close();
		}
		catch(Exception e) {
		
		}
	}
	
	@Override
	protected void onPostExecute(Void unused) {
		
		Log.i("K", "Downloading complete - category : " + category);
		
		SharedPreferences.Editor edit = pref.edit();
		
		switch(category) {
		case 1:
			edit.putBoolean("localbudburst", true);
			break;
		case 2:
			edit.putBoolean("localwhatsinvasive", true);
			break;
		case 3:
			edit.putBoolean("localpoisonous", true);
			break;
		case 4:
			edit.putBoolean("localendangered", true);
			break;
		}
	
		edit.commit();
	}
}
