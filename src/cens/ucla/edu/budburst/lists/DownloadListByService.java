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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.Values;

public class DownloadListByService extends AsyncTask<Items, Integer, Void>{
	
	@Override
	protected Void doInBackground(Items... item) {
		// TODO Auto-generated method stub
		
		/*
		 * Change the boolean value of the category to TRUE
		 * 
		 */
		
		SharedPreferences pref = item[0].context.getSharedPreferences("userinfo", 0);
		SharedPreferences.Editor edit = pref.edit();
		
		Log.i("K", "Downloading complete - category : " + item[0].category);
		
		switch(item[0].category) {
		case 1:
			edit.putBoolean("localbudburst", true);
			break;
		case 2:
			edit.putBoolean("localwhatsinvasive", true);
			break;
		case 3:
			edit.putBoolean("localnative", true);
			break;
		case 4:
			edit.putBoolean("localpoisonous", true);
			break;
		}
	
		edit.commit();
		
		
		
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
				SQLiteDatabase otDB = otDBH.getWritableDatabase();
				
				// Delete the list first - by category
				otDBH.clearLocalListsByCategory(item[0].context, item[0].category);
				
				Log.i("K", "Json Array Length : " + jsonAry.length());	
				
				// ReINSERT the values
				for(int i = 0 ; i < jsonAry.length() ; i++) {
					
					/*
					 * Set the first char to upper case.
					 */
					String commonName = jsonAry.getJSONObject(i).getString("common");
					Log.i("K", "commonName : " + commonName);
					
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
}
