package cens.ucla.edu.budburst.floracaching;

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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;

import com.google.android.maps.GeoPoint;

// retrieve floracaching lists from the server.
public class FloracacheGetLists extends AsyncTask<String, Integer, Void> {
	
	private ProgressDialog mDialog;
	private Context mContext;
	
	private NotificationManager notificationMgr = null;
	private Notification noti = null;
	private int SIMPLE_NOTFICATION_ID = HelperValues.NOTIFI_FLORACACHE_LISTS;
	
	public FloracacheGetLists(Context context) {
		mContext = context;
	}
	
	protected void onPreExecute() {
		notificationMgr = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
		noti = new Notification(android.R.drawable.stat_sys_download, 
				mContext.getString(R.string.Floracache_List_Download_Text), 
				System.currentTimeMillis());
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, null, PendingIntent.FLAG_CANCEL_CURRENT);
		
		noti.setLatestEventInfo(mContext, "Project Budburst", mContext.getString(R.string.Floracache_List_Downloading), contentIntent);
		
		notificationMgr.notify(SIMPLE_NOTFICATION_ID, noti);
	}
	@Override
	protected Void doInBackground(String... url) {
		
		//mPlantList = new ArrayList<FloracacheItem>();
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url[0]);
		String result = "";
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
				StringBuilder result_str = new StringBuilder();
				for(;;){
					String line = br.readLine();
					if (line == null) 
						break;
					result_str.append(line+'\n');
				}
				result = result_str.toString();
				
				JSONObject jsonObj = new JSONObject(result);
				if(jsonObj.getBoolean("success")) {
					JSONArray jsonAry = jsonObj.getJSONArray("results");
					
					OneTimeDBHelper oDBH = new OneTimeDBHelper(mContext);
					oDBH.clearFloracacheList(mContext);

					for(int i = 0 ; i < jsonAry.length() ; i++) {					
						//jsonAry.getJSONObject(i).getString("Tree_ID") + "," +
						
						int floracacheID = jsonAry.getJSONObject(i).getInt("Floracache_ID");
						String userName = jsonAry.getJSONObject(i).getString("User_Name");
						int userPlantID = jsonAry.getJSONObject(i).getInt("User_Plant_ID");
						int userSpeciesID = jsonAry.getJSONObject(i).getInt("User_Species_ID");
						int userSpeciesCategoryID = jsonAry.getJSONObject(i).getInt("User_Species_Category_ID");
						int userStationID = jsonAry.getJSONObject(i).getInt("User_Station");
						int floracacheRank = jsonAry.getJSONObject(i).getInt("Floracache_Rank");
						double latitude = Double.parseDouble(jsonAry.getJSONObject(i).getString("Latitude"));
						double longitude = Double.parseDouble(jsonAry.getJSONObject(i).getString("Longitude"));
						String floracacheName = jsonAry.getJSONObject(i).getString("Floracache_Notes");
						String floracacheDate = jsonAry.getJSONObject(i).getString("Floracache_Date");
						String fCommonName = jsonAry.getJSONObject(i).getString("Common_Name");
						String fScienceName = jsonAry.getJSONObject(i).getString("Science_Name");
						int fProtocolID = jsonAry.getJSONObject(i).getInt("Protocol_ID");
						int fGroupID = jsonAry.getJSONObject(i).getInt("Floracache_Group_ID");
						int fImageID = jsonAry.getJSONObject(i).getInt("Image_ID");
						String fObservedDate = jsonAry.getJSONObject(i).getString("Dates");
						
						
						SQLiteDatabase oDB = oDBH.getWritableDatabase();

						oDB.execSQL("INSERT INTO floracacheLists VALUES(" +
								floracacheID + ",\"" +
								userName + "\"," +
								userSpeciesID + "," +
								userPlantID + "," +
								userSpeciesCategoryID + "," +
								userStationID + ",\"" +
								floracacheName + "\",\"" +
								floracacheDate + "\"," +
								floracacheRank + "," +
								latitude + "," +
								longitude + ",\"" +
								fCommonName + "\",\"" +
								fScienceName + "\"," +
								fProtocolID + "," + 
								fGroupID + "," + 
								fImageID + ",\"" +
								fObservedDate + "\"" + ");");
						
						oDB.close();
						
						HelperSharedPreference hPref = new HelperSharedPreference(mContext);
						hPref.setPreferencesBoolean("floracache", true);
					}
				}
			}
			
			getFloracacheGroups();
			
			
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
	
	protected void onPostExecute(Void unused) {
		noti = new Notification(R.drawable.compass_default, mContext.getString(R.string.Floracache_List_Download_Success2), System.currentTimeMillis());
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, null, PendingIntent.FLAG_CANCEL_CURRENT);
		
		noti.setLatestEventInfo(mContext, "Project Budburst", 
				mContext.getString(R.string.Floracache_List_Download_Success), contentIntent);
		
		notificationMgr.notify(SIMPLE_NOTFICATION_ID, noti);
	}
	
	private void getFloracacheGroups() {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(mContext.getString(R.string.get_floracaching_lists));
		String result = "";
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
				StringBuilder result_str = new StringBuilder();
				for(;;){
					String line = br.readLine();
					if (line == null) 
						break;
					result_str.append(line+'\n');
				}
				result = result_str.toString();
				
				JSONObject jsonObj = new JSONObject(result);
				if(jsonObj.getBoolean("success")) {
					JSONArray jsonAry = jsonObj.getJSONArray("results");

					OneTimeDBHelper oDBH = new OneTimeDBHelper(mContext);
					SQLiteDatabase oDB = oDBH.getWritableDatabase();
					
					for(int i = 0 ; i < jsonAry.length() ; i++) {					
						
						int groupID = jsonAry.getJSONObject(i).getInt("Group_ID");
						String groupName = jsonAry.getJSONObject(i).getString("Name");
						String groupDate = jsonAry.getJSONObject(i).getString("Date_Created");
						String groupLat = jsonAry.getJSONObject(i).getString("Latitude");
						String groupLon = jsonAry.getJSONObject(i).getString("Longitude");
						String groupRad = jsonAry.getJSONObject(i).getString("Radius");
						String groupDis = jsonAry.getJSONObject(i).getString("Description");
						String groupIcon = jsonAry.getJSONObject(i).getString("Icon_url");
						
						
						
						oDB.execSQL("INSERT INTO floracacheGroups VALUES(" + 
									groupID + ",'" +
									groupName + "','" +
									groupDate + "'," +
									Double.parseDouble(groupLat) + "," +
									Double.parseDouble(groupLon) + "," +
									Integer.parseInt(groupRad) + ",'" +
									groupDis + "','" +
									groupIcon + "');");
						
						
						Log.i("K", "ID : " + groupID 
								+ ", Name: " + groupName);
						
						
						
						//mItem = new HelperListItem("none", 
						//						groupName, 
						//						"pbb_icon_main", 
						//						getString(R.string.List_Budburst));
						//mListArr.add(mItem);
						
					}
					
					oDB.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
