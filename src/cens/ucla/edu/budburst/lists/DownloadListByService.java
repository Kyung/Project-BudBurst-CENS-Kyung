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
				
				JSONArray jsonAry = new JSONArray(result);
				
				/*
				 * Open OneTime Database and delete the list first - by category
				 * After that, reINSERT the values into the database
				 * 
				 */
				
				OneTimeDBHelper otDBH = new OneTimeDBHelper(item[0].context);
				SQLiteDatabase otDB = otDBH.getWritableDatabase();
				
				// Delete the list first - by category
				otDBH.clearLocalListsByCategory(item[0].context, item[0].category);
				
					
				// ReINSERT the values
				for(int i = 0 ; i < jsonAry.length() ; i++) {
				
					otDB.execSQL("INSERT INTO localPlantLists VALUES(" +
							item[0].category + ",\"" +
							jsonAry.getJSONObject(i).getString("common") + "\",\"" +
							jsonAry.getJSONObject(i).getString("scientific") + "\",\"" +
							jsonAry.getJSONObject(i).getString("county") + "\",\"" +
							jsonAry.getJSONObject(i).getString("state") + "\",\"" +
							jsonAry.getJSONObject(i).getString("usda_url") + "\",\"" +
							jsonAry.getJSONObject(i).getString("image_url") + "\",\"" +
							jsonAry.getJSONObject(i).getString("image_copyright") + "\");"
							);
					}
					
					
					// DB close
					otDB.close();
					otDBH.close();

				}
			
			
			/*
			 * Change the boolean value of the category to TRUE
			 * 
			 */
			String stringCategory = "";
			
			switch(item[0].category) {
			case 1:
				stringCategory = "localbudburst";
				break;
			case 2:
				stringCategory = "localwhatsinvasive";
				break;
			case 3:
				stringCategory = "localnative";
				break;
			case 4:
				stringCategory = "localpoisonous";
				break;
			}
			
			SharedPreferences pref = item[0].context.getSharedPreferences("userinfo", 0);
			SharedPreferences.Editor edit = pref.edit();
			
			edit.putBoolean(stringCategory, true);
			edit.commit();
			
			
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
