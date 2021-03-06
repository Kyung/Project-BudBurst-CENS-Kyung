package cens.ucla.edu.budburst.helper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.lists.ListItems;
import cens.ucla.edu.budburst.lists.ListLocalDownload;
import cens.ucla.edu.budburst.lists.ListUserDefinedSpeciesDownload;

/**
 * This class is for announcing my location to the server.
 * Once the server receives the location values from the phone,
 * it will start searching the local lists with Flickr species.
 * @author kyunghan
 *
 */
public class HelperAnnounceMyLocation extends AsyncTask<Void, Void, Void>{
	
	private double mLatitude;
	private double mLongitude;
	
	public HelperAnnounceMyLocation(double latitude, double longitude) {
		mLatitude = latitude;
		mLongitude = longitude;
	}
	
	@Override
	protected void onPreExecute() {}
	
	@Override
	protected Void doInBackground(Void... Void) {
		// TODO Auto-generated method stub
		
		Log.i("K", "Announce my location / URL : " + "http://networkednaturalist.org/python_scripts/cens-dylan/announce.py?lat=" 
				+ mLatitude + "&lon=" + mLongitude);
		
		String getResponse = getRequest("http://networkednaturalist.org/python_scripts/cens-dylan/announce.py" + "?lat=" 
				+ mLatitude + "&lon=" + mLongitude);
		
		Log.i("K", "Response Message(Early announcement) : " + getResponse.toString());
		
		if(getResponse.toString().equals("OK")) {
		}
		else {
		}
		
		return null;
	}
	
	public String getRequest(String getUrl) {
		
		String getResponse = "";
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet getHttp = new HttpGet(getUrl);
			HttpResponse responseGet = client.execute(getHttp);
			HttpEntity resEntityGet = responseGet.getEntity();
			
			if(resEntityGet != null) {
				getResponse = EntityUtils.toString(resEntityGet);
			}
		}
		catch(Exception e) {
			Log.i("K", "Exception in HttpRequest");
		}
		
		return getResponse;
	}
	
	@Override
	protected void onPostExecute(Void unused) {}

}
