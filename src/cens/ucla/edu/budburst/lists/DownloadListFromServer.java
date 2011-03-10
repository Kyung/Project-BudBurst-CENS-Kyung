package cens.ucla.edu.budburst.lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import cens.ucla.edu.budburst.Login;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.Sync;
import cens.ucla.edu.budburst.helper.MyListAdapter;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
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

public class DownloadListFromServer extends AsyncTask<Items, Integer, Void>{

	private Context context;
	private ProgressDialog dialog;
	private ListView list;
	private LazyAdapter lazyadapter;
	private String[] mStrings;
	private ArrayList <PlantItem> localArray;
	private boolean running = true;
	private Items item;
	private int category;
	
	public DownloadListFromServer(Context context, ListView list, LazyAdapter lazyadapter, Items item) {
		this.context = context;
		this.list = list;
		this.lazyadapter = lazyadapter;
		this.item = item;
		
		// Initialize ArrayList
		localArray = new ArrayList<PlantItem>();
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
		}
		
		SharedPreferences pref = context.getSharedPreferences("userinfo", 0);
		SharedPreferences.Editor edit = pref.edit();				
		edit.putBoolean(cancelCategoryString, false);
		edit.commit();
		
		Toast.makeText(context, "Download cancelled. Press back button again.", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected Void doInBackground(Items... item) {
		// TODO Auto-generated method stub
		
		/*
		 * Use the boolean value to check the process will be contiunued
		 * 
		 */
		
		while(running) {
			
			String url = "http://networkednaturalist.org/python_scripts/cens-dylan/list.py?lat=" 
				+ item[0].latitude
				+ "&lon=" + item[0].longitude 
				+ "&type=" + item[0].category;
			
			category = item[0].category;
			
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
					 * Category :
					 *  - Budburst : 2
					 *  - Whatsinvasive : 3
					 *  - Whatsnative : 4
					 *  - Whatsposionous : 5
					 */
					
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
						
						otDB.execSQL("INSERT INTO localPlantLists VALUES(" +
								item[0].category + ",\"" +
								commonName + "\",\"" +
								jsonAry.getJSONObject(i).getString("scientific") + "\",\"" +
								county + "\",\"" +
								state + "\",\"" +
								jsonAry.getJSONObject(i).getString("usda_url") + "\",\"" +
								jsonAry.getJSONObject(i).getString("image_url") + "\",\"" +
								jsonAry.getJSONObject(i).getString("image_copyright") + "\");"
								);
					}
					
					/*
					 * Retrieve values from the table and put that into the PlantItem class.
					 */

					if(category == Values.BUDBURST_LIST) {
						otDB = otDBH.getReadableDatabase();

						Cursor cursor = otDB.rawQuery("SELECT science_name FROM localPlantLists WHERE category=" 
								+ category 
								+ " ORDER BY LOWER(common_name) ASC;", null);
						
						while(cursor.moveToNext()) {
							
							StaticDBHelper staticDB = new StaticDBHelper(context);
							SQLiteDatabase sDB = staticDB.getReadableDatabase();
							
							Cursor getSpeciesInfo = sDB.rawQuery("SELECT _id, species_name, common_name, protocol_id, category, description FROM species WHERE species_name=\"" 
									+ cursor.getString(0) + "\";", null);
							
							
							while(getSpeciesInfo.moveToNext()) {
								int resID = context.getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+getSpeciesInfo.getInt(0), null, null);
								
								PlantItem pi;
								/*
								 *  pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
								 */
								
								/*
								 * Insert into PlantItem object
								 */
								pi = new PlantItem(resID, getSpeciesInfo.getString(2), getSpeciesInfo.getString(1), getSpeciesInfo.getInt(0), getSpeciesInfo.getInt(3));

								localArray.add(pi);
							}
							
							getSpeciesInfo.close();
							sDB.close();
						}
						
						cursor.close();
					}
					else {
						Cursor cursor = otDB.rawQuery("SELECT category, common_name, science_name, photo_url FROM localPlantLists WHERE category=" 
								+ category 
								+ " ORDER BY LOWER(common_name) ASC;", null);
						
						while(cursor.moveToNext()) {
							PlantItem pi = new PlantItem(cursor.getString(1) 
									, cursor.getString(2)
									, cursor.getString(3)
									, cursor.getInt(0));
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
	
	@Override
	protected void onPostExecute(Void unused) {
		dialog.dismiss();
		
		if(category == Values.BUDBURST_LIST) {
			MyListAdapter mylistapdater = new MyListAdapter(context, R.layout.plantlist_item2, localArray);
			list.setAdapter(mylistapdater);
		}
		else {
			lazyadapter = new LazyAdapter(context, localArray);
	        list.setAdapter(lazyadapter);
		}
	}
}
