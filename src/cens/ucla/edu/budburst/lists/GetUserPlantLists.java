package cens.ucla.edu.budburst.lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.Values;

public class GetUserPlantLists extends AsyncTask<Context, Void, Void>{

	@Override
	protected Void doInBackground(Context... context) {
		

		Log.i("K", "Start Downloading User-Defined-Lists : UCLA Tree List.");
		
		HttpClient httpClient = new DefaultHttpClient();
		String url = new String("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/get_tree_lists.php");
		HttpPost httpPost = new HttpPost(url);
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				/*
				 * Set the boolean variable to TRUE
				 */
				SharedPreferences pref = context[0].getSharedPreferences("userinfo", 0);
				SharedPreferences.Editor edit = pref.edit();
				edit.putBoolean("getTreeLists", true);
				edit.commit();
				
				/*
				 * Make the folder 
				 */
				File f = new File(Values.TREE_PATH);
				if(!f.exists()) {
					f.mkdir();
				}
				
				/*
				 * Delete values in UCLAtreeLists table
				 */
				OneTimeDBHelper onehelper = new OneTimeDBHelper(context[0]);
				onehelper.clearUCLAtreeLists(context[0]);
				onehelper.close();
				
				
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String serverResponse = ""; 
				serverResponse += br.readLine();
			
				
				JSONObject jsonObj = new JSONObject(serverResponse);
				if(jsonObj.getBoolean("success")) {
					JSONArray jsonAry = jsonObj.getJSONArray("results");
					for(int i = 0 ; i < jsonAry.length() ; i++) {
						
						
						OneTimeDBHelper otDBH = new OneTimeDBHelper(context[0]);
						SQLiteDatabase otDB = otDBH.getWritableDatabase();
						
						otDB.execSQL("INSERT INTO userDefineLists VALUES(" +
								jsonAry.getJSONObject(i).getString("Tree_ID") + "," +
								"'" + jsonAry.getJSONObject(i).getString("Common_Name") + "'," +
								"'" + jsonAry.getJSONObject(i).getString("Science_Name") + "'," + 
								"'" + jsonAry.getJSONObject(i).getString("Credit") + "'" +
								");"
								);
						
						otDBH.close();
						otDB.close();
						
						FunctionsHelper helper = new FunctionsHelper();
						helper.download_image(R.string.user_plant_lists_image + (i+1) + "_thumb.jpg", (i+1) + "", Values.TREE_PATH);
					}

				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}