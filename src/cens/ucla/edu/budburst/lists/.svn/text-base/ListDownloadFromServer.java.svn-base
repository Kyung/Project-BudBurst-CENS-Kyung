package cens.ucla.edu.budburst.lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import cens.ucla.edu.budburst.PBBLogin;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.PBBSync;
import cens.ucla.edu.budburst.adapter.MyListAdapter;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.helper.HelperLazyAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class ListDownloadFromServer extends AsyncTask<ListItems, Integer, Void>{

	private Context context;
	private ProgressDialog dialog;
	private ListView list;
	private HelperLazyAdapter lazyadapter;
	private String[] mStrings;
	private ArrayList <HelperPlantItem> localArray;
	private boolean running = true;
	private ListItems item;
	private int mCategory;
	
	public ListDownloadFromServer(Context context, ListView list, HelperLazyAdapter lazyadapter, ListItems item, int category) {
		this.context = context;
		this.list = list;
		this.lazyadapter = lazyadapter;
		this.item = item;
		mCategory = category;
		
		// Initialize ArrayList
		localArray = new ArrayList<HelperPlantItem>();
	}
	
	@Override
	protected void onPreExecute() {
		
		dialog = ProgressDialog.show(context, "Please wait...", "Download the lists from the server. This may take some time...", true);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub
				cancel(true);
			}
			
		});		
	}
	
	/*
	 * If user pressed the "back" button,
	 * the download process will be stopped
	 * and change the boolean flag value to false
	 * for downloading the lists again..
	 * 
	 */
	
	@Override
	protected void onCancelled() {
		running = false;
		
		int cancelCategory = item.category;
		String cancelCategoryString = "";
		
		switch(cancelCategory) {
		case 1:
			cancelCategoryString = "localbudburst";
			break;
		case 2:
			cancelCategoryString = "localwhatsinvasive";
			break;
		case 3:
			cancelCategoryString = "localnative";
			break;
		case 4:
			cancelCategoryString = "localpoisonous";
			break;
		case 5:
			cancelCategoryString = "localendangered";
			break;
		}
		
		SharedPreferences pref = context.getSharedPreferences("userinfo", 0);
		SharedPreferences.Editor edit = pref.edit();				
		edit.putBoolean(cancelCategoryString, false);
		edit.commit();
		
		Toast.makeText(context, "Download cancelled. Press back button again.", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected Void doInBackground(ListItems... item) {
		// TODO Auto-generated method stub
		
		/*
		 * Use the boolean value to check the process will be contiunued
		 * 
		 */
		
		while(running) {
			
			String url = "http://networkednaturalist.org/python_scripts/cens-dylan/list.py?lat=" 
				+ item[0].latitude
				+ "&lon=" + item[0].longitude 
				+ "&type=" + mCategory;
			
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
					
					JSONArray jsonAry = jsonObj.getJSONArray("results");
					
					/*
					 * Open OneTime Database and delete the list first - by category
					 * After that, reINSERT the values into the database
					 * 
					 */
					
					OneTimeDBHelper otDBH = new OneTimeDBHelper(context);
					SQLiteDatabase otDB = otDBH.getWritableDatabase();
					
					// Delete the list first - by category
					otDBH.clearLocalListsByCategory(context, item[0].category);
					
					mStrings = new String[jsonAry.length()];
					
					/*
					 * Change the boolean value of the category to TRUE
					 */
					// type=1 BudBurst  
					// type=2 WhatsInvasive
					// type=3 WhatsPoisonous
					// type=4 WhatsEndangere
					
					// ReINSERT the values
					for(int i = 0 ; i < jsonAry.length() ; i++) {
					
						/*
						 * Set the first char to upper case.
						 */
						String commonName = jsonAry.getJSONObject(i).getString("common");
						
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
					
					/*
					 * Retrieve values from the table and put that into the PlantItem class.
					 */

					if(mCategory == HelperValues.LOCAL_BUDBURST_LIST) {
						otDB = otDBH.getReadableDatabase();

						Cursor cursor = otDB.rawQuery("SELECT science_name FROM localPlantLists WHERE category=" 
								+ mCategory 
								+ " ORDER BY LOWER(common_name) ASC;", null);
						
						while(cursor.moveToNext()) {
							
							StaticDBHelper staticDB = new StaticDBHelper(context);
							SQLiteDatabase sDB = staticDB.getReadableDatabase();
							
							Cursor getSpeciesInfo = sDB.rawQuery("SELECT _id, species_name, common_name, protocol_id, category, description FROM species WHERE species_name=\"" 
									+ cursor.getString(0) + "\";", null);
							
							
							while(getSpeciesInfo.moveToNext()) {
								int resID = context.getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+getSpeciesInfo.getInt(0), null, null);
								
								HelperPlantItem pi;
								/*
								 *  pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
								 */
								
								/*
								 * Insert into PlantItem object
								 * public HelperPlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int aProtocolID){
								 */
								pi = new HelperPlantItem();
								pi.setPicture(resID);
								pi.setCommonName(getSpeciesInfo.getString(2));
								pi.setSpeciesName(getSpeciesInfo.getString(1));
								pi.setSpeciesID(getSpeciesInfo.getInt(0));
								pi.setProtocolID(getSpeciesInfo.getInt(3));

								localArray.add(pi);
							}
							
							getSpeciesInfo.close();
							sDB.close();
						}
						
						cursor.close();
					}
					else {
						Cursor cursor = otDB.rawQuery("SELECT category, common_name, science_name, photo_url FROM localPlantLists WHERE category=" 
								+ mCategory 
								+ " ORDER BY LOWER(common_name) ASC;", null);
						
						//public HelperPlantItem(String aCommonName, String aSpeciesName, String aImageUrl, int aCategory) {
						while(cursor.moveToNext()) {
							HelperPlantItem pi = new HelperPlantItem();
							pi.setCommonName(cursor.getString(1));
							pi.setSpeciesName(cursor.getString(2));
							pi.setImageURL(cursor.getString(3));
							pi.setCategory(cursor.getInt(0));
									
							localArray.add(pi);
						}
						
						cursor.close();
					}
					/*
					 *  DB close
					 */
					otDB.close();
					otDBH.close();
					
					
					/*
					 *  Set boolean flag to false to stop this loop
					 */
					running = false;

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		dialog.dismiss();
		
		String doneCategoryString = "";
		
		switch(mCategory) {
		case 1:
			doneCategoryString = "localbudburst";
			break;
		case 2:
			doneCategoryString = "localwhatsinvasive";
			break;
		case 3:
			doneCategoryString = "localnative";
			break;
		case 4:
			doneCategoryString = "localpoisonous";
			break;
		case 5:
			doneCategoryString = "localendangered";
			break;
		}
		
		SharedPreferences pref = context.getSharedPreferences("userinfo", 0);
		SharedPreferences.Editor edit = pref.edit();				
		edit.putBoolean(doneCategoryString, true);
		edit.commit();
		
		
		if(mCategory == HelperValues.LOCAL_BUDBURST_LIST) {
			MyListAdapter mylistapdater = new MyListAdapter(context, R.layout.plantlist_item2, localArray);
			list.setAdapter(mylistapdater);
		}
		else {
			lazyadapter = new HelperLazyAdapter(context, localArray);
	        list.setAdapter(lazyadapter);
		}
	}
}
