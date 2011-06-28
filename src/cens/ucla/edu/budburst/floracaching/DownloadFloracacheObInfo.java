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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapterFloracacheOther;
import cens.ucla.edu.budburst.myplants.DetailPlantInfoFloracache;

public class DownloadFloracacheObInfo extends AsyncTask<Void, Void, Void>{
	
	private ProgressDialog mDialog;
	private Context mContext;
	private int mFloracacheID;
	private ArrayList<FloracacheItem> mArr;
	private MyListAdapterFloracacheOther mFloraApdater;
	private ListView mListView;
	private TextView mTextView;
	private int mTotalObserved;
	private int mMaximum;
	
	public DownloadFloracacheObInfo(Context context, ArrayList<FloracacheItem> fArr, int floracacheID, ListView listView, TextView oText, int max) {
		mContext = context;
		mArr = fArr;
		mFloracacheID = floracacheID;
		mListView = listView;
		mTextView = oText;
		mMaximum = max;
	}
	
	@Override
	protected void onPreExecute() {
		
		mDialog = ProgressDialog.show(mContext, mContext.getString(R.string.Alert_pleaseWait), 
				"Downloading floracache info...", true);
		mDialog.setCancelable(true);
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		
		mArr = new ArrayList<FloracacheItem>();
		
		HttpClient httpClient = new DefaultHttpClient();
		String url = new String("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/get_floracache_info.php?floracache_id=" + mFloracacheID);
		Log.i("K", "url : " + url);
		HttpPost httpPost = new HttpPost(url);

		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				/*
				 * Delete values in UCLAtreeLists table
				 */
				
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String serverResponse = ""; 
				serverResponse += br.readLine();
			
				JSONObject jsonObj = new JSONObject(serverResponse);
				
				if(jsonObj.getBoolean("success")) {
					
					
					mTotalObserved = jsonObj.getInt("total");
					JSONArray jsonAry = jsonObj.getJSONArray("results");
					
					int length = 4;
					if(jsonAry.length() < 4) {
						length = jsonAry.length();
					}
					for(int i = 0 ; i < length ; i++) {
						
						Log.i("K", "http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/upload_images/onetime.php?image_id=" + jsonAry.getJSONObject(i).getString("Image_ID") + " , " + jsonAry.getJSONObject(i).getString("User_Name") + " , " + jsonAry.getJSONObject(i).getString("Date"));
						
						FloracacheItem iItem = new FloracacheItem();
						
						iItem.setImageURL("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/upload_images/onetime.php?image_id=" + jsonAry.getJSONObject(i).getString("Image_ID"));
						iItem.setUserName(jsonAry.getJSONObject(i).getString("User_Name"));
						iItem.setDate(jsonAry.getJSONObject(i).getString("Date"));
						iItem.setPhenophase(jsonAry.getJSONObject(i).getInt("Phenophase_ID"));
						iItem.setUserNote(jsonAry.getJSONObject(i).getString("Notes"));
						
						mArr.add(iItem);
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
		mDialog.dismiss();
		
		mTextView.setText(mTotalObserved + " observation(s) by others.\n(show " + mMaximum + " recent observations)");
		
		if(mTotalObserved > 0) {
			mFloraApdater = new MyListAdapterFloracacheOther(mContext, R.layout.floracache_observed_info ,mArr);
			mListView.setAdapter(mFloraApdater);
		}
		else {
			mTextView.setText(mTotalObserved + " observation(s) by others.\n(show " + mMaximum + " recent observations)\n\n" +
					"Be the first finder to this plant!");
		}
		
		
	}
}