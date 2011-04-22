package cens.ucla.edu.budburst.lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;

public class ListUserPlants extends AsyncTask<Void, Void, Void>{

	private Context mContext;
	private int mCategory = 10;
	private NotificationManager notificationMgr = null;
	private Notification noti = null;
	private int SIMPLE_NOTFICATION_ID = 1234567890;
	
	public ListUserPlants(Context context) {
		mContext = context;
	}
	
	@Override
	protected void onPreExecute() {
		notificationMgr = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
		noti = new Notification(android.R.drawable.stat_sys_download, "Start downloading - Tree lists", System.currentTimeMillis());
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, null, PendingIntent.FLAG_CANCEL_CURRENT);
		
		noti.setLatestEventInfo(mContext, "Project Budburst", "Downloading user plant lists", contentIntent);
		
		notificationMgr.notify(SIMPLE_NOTFICATION_ID, noti);
	}
	
	@Override
	protected Void doInBackground(Void... unused) {

		Log.i("K", "Start Downloading User-Defined-Lists : UCLA Tree List.");
		
		HttpClient httpClient = new DefaultHttpClient();
		String url = new String("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/get_tree_lists.php");
		HttpPost httpPost = new HttpPost(url);
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				/*
				 * Make the folder 
				 */
				File f = new File(HelperValues.TREE_PATH);
				if(!f.exists()) {
					f.mkdir();
				}
				
				/*
				 * Delete values in UCLAtreeLists table
				 */
				OneTimeDBHelper onehelper = new OneTimeDBHelper(mContext);
				onehelper.clearUserDefineList(mContext);
				onehelper.close();
				
				
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String serverResponse = ""; 
				serverResponse += br.readLine();
			
				
				JSONObject jsonObj = new JSONObject(serverResponse);
				if(jsonObj.getBoolean("success")) {
					JSONArray jsonAry = jsonObj.getJSONArray("results");

					for(int i = 0 ; i < jsonAry.length() ; i++) {
						
						OneTimeDBHelper otDBH = new OneTimeDBHelper(mContext);
						SQLiteDatabase otDB = otDBH.getWritableDatabase();
						HelperFunctionCalls helper = new HelperFunctionCalls();
						
						try {
							
							otDB.execSQL("INSERT INTO userDefineLists VALUES(" +
									jsonAry.getJSONObject(i).getString("Tree_ID") + "," +
									"'" + jsonAry.getJSONObject(i).getString("Common_Name") + "'," +
									"'" + jsonAry.getJSONObject(i).getString("Science_Name") + "'," + 
									"'" + jsonAry.getJSONObject(i).getString("Credit") + "'," +
									"" + mCategory +
									");"
									);
							
							Log.i("K", "INSERT INTO userDefineLists VALUES(" +
									jsonAry.getJSONObject(i).getString("Tree_ID") + "," +
									"'" + jsonAry.getJSONObject(i).getString("Common_Name") + "'," +
									"'" + jsonAry.getJSONObject(i).getString("Science_Name") + "'," + 
									"'" + jsonAry.getJSONObject(i).getString("Credit") + "'," +
									"" + mCategory +
									");");
							
							URL urls = new URL("http://networkednaturalist.org/User_Plant_Lists_Images/" + jsonAry.getJSONObject(i).getString("Tree_ID") + "_thumb.jpg");
							HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
							conn.connect();
							
							/*
							 * ResponseCode 404, means there is no photo related to the corresponding id
							 * So in this case, we alternatively link to basic tree photo. (100_thumb.jpg)
							 * 
							 */
							if(conn.getResponseCode() == 404) {
							
								urls = new URL("http://networkednaturalist.org/User_Plant_Lists_Images/100_thumb.jpg");
								conn = (HttpURLConnection)urls.openConnection();
								conn.connect();
							}
							
							File hasImage = new File(HelperValues.TREE_PATH + jsonAry.getJSONObject(i).getString("Tree_ID") + ".jpg");
							if(hasImage.exists()) {
								Log.i("K", "already has tree image");
							}
							else {
								int read;
								try {
									
									Object getContent = urls.getContent();
									
									Log.i("K", "getContent : " + getContent);
									conn = (HttpURLConnection)urls.openConnection();
									conn.connect();
									
									int len = conn.getContentLength();
									
									byte[] buffer = new byte[len];
									InputStream is = conn.getInputStream();
									FileOutputStream fos = new FileOutputStream(HelperValues.TREE_PATH + jsonAry.getJSONObject(i).getString("Tree_ID") + ".jpg");
									
									while ((read = is.read(buffer)) > 0) {
										fos.write(buffer, 0, read);
									}
									fos.close();
									is.close();
								}
								catch(Exception e) {
									
								}
							}

							otDBH.close();
							otDB.close();
						}
						catch(Exception e) {
							otDBH.close();
							otDB.close();
						}
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
	
	@Override
	protected void onPostExecute(Void unused) {
		/*
		 * Set the boolean variable to TRUE
		 */
		HelperSharedPreference hPref = new HelperSharedPreference(mContext);
		hPref.setPreferencesBoolean("getTreeLists", true);
		hPref.setPreferencesBoolean("firstDownloadTreeList", true);
		
		noti = new Notification(R.drawable.s1000, "Download complete - user plant lists", System.currentTimeMillis());
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, null, PendingIntent.FLAG_CANCEL_CURRENT);
		
		noti.setLatestEventInfo(mContext, "Project Budburst", "Successfully download user plant lists", contentIntent);
		
		notificationMgr.notify(SIMPLE_NOTFICATION_ID, noti);
	}
}