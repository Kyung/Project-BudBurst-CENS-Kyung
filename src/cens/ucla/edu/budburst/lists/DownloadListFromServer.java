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
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
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
				//+ "42.1497&lon=-74.9384&type=1";
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
					
					OneTimeDBHelper otDBH = new OneTimeDBHelper(context);
					SQLiteDatabase otDB = otDBH.getWritableDatabase();
					
					// Delete the list first - by category
					otDBH.clearLocalListsByCategory(context, item[0].category);
					
					mStrings = new String[jsonAry.length()];
					
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
						
						
						// Put the values into the PlantItem class
						// and pass them to the ArrayLists
						PlantItem pi = new PlantItem(jsonAry.getJSONObject(i).getString("common")
								, jsonAry.getJSONObject(i).getString("scientific")
								, jsonAry.getJSONObject(i).getString("image_url")
								, item[0].category);
						localArray.add(pi);
					}
					
					// Set boolean flag to false to stop this loop
					running = false;
					
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
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void onPostExecute(Void unused) {
		
		lazyadapter = new LazyAdapter(context, localArray);
        list.setAdapter(lazyadapter);
		
		dialog.dismiss();
	}
}
