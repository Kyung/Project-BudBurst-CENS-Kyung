package cens.ucla.edu.budburst.mapview;

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
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.HelperJSONParser;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

public class SpeciesOthersFromServer extends AsyncTask<String, Void, Void>{
	private ProgressDialog mDialog;
	private Context mContext;
	private ArrayList<HelperPlantItem> mPlantList;
	private boolean flagFinish = false;
	private MapController mMapController = null;
	private MyLocationOverlay mMyOverLay = null;
	private MapView mMapView = null;
	private int mCategory = 0;
	private static final int MAX_SPEICES = 50;
	
	public SpeciesOthersFromServer(Context context, MapView mapView, MyLocationOverlay myOverLay, int category) {
		mContext = context;
		mMapView = mapView;
		mMyOverLay = myOverLay;
		mPlantList = new ArrayList<HelperPlantItem>();
		mCategory = category;
	}
	
	protected void onPreExecute() {
		mDialog = ProgressDialog.show(mContext, mContext.getString(R.string.Alert_loading), mContext.getString(R.string.PBBMap_loadingComponents), true);
	}
	@Override
	protected Void doInBackground(String... get_url) {
		
		HttpClient httpClient = new DefaultHttpClient();
		String url = new String(get_url[0] + "&category=" + mCategory);

		Log.i("K","URL : " + url);
			
		HttpPost httpPost = new HttpPost(url);
		int getCount = 0;
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				HelperJSONParser jHelper = new HelperJSONParser();
				
				String result = "";
				
				StringBuilder result_str = new StringBuilder();
				String line = "";
				
				while((line = br.readLine()) != null){
					result_str.append(line);
				}
				
				result = result_str.toString();
				
				Log.i("K", "RESULT : " + result);
				
				JSONArray jsonAry = new JSONArray(result);
				
				getCount = jsonAry.length();
				
				// pi = new PlantItem(speciesID, commonName, scienceName, phenophaseID, latitude, longitude, imageName, dtTaken, notes);
				// plantList.add(pi);
				
				for(int i = 0 ; i < jsonAry.length() ; i++) {
					
					String imageID = jsonAry.getJSONObject(i).getString("Image_ID");
					int speciesID = Integer.parseInt(jsonAry.getJSONObject(i).getString("Species_ID"));
					int phenophaseID = Integer.parseInt(jsonAry.getJSONObject(i).getString("Phenophase_ID"));
					int protocolID = Integer.parseInt(jsonAry.getJSONObject(i).getString("Protocol_ID"));
					int category = Integer.parseInt(jsonAry.getJSONObject(i).getString("Category"));
					String commonName = jsonAry.getJSONObject(i).getString("Common_Name");
					String scienceName = jsonAry.getJSONObject(i).getString("Science_Name");
					String userName = jsonAry.getJSONObject(i).getString("User_Name");
					double latitude = Double.parseDouble(jsonAry.getJSONObject(i).getString("Latitude"));
					double longitude = Double.parseDouble(jsonAry.getJSONObject(i).getString("Longitude"));
					String date = jsonAry.getJSONObject(i).getString("Date");
					String notes = jsonAry.getJSONObject(i).getString("Notes");
					String hasImage = jsonAry.getJSONObject(i).getString("Has_Image");
					String distance = jsonAry.getJSONObject(i).getString("Distance");
					
					int plantID = 0;
					
					HelperPlantItem pi = new HelperPlantItem(HelperValues.OTHERS_PLANT_LIST, 
							HelperValues.FROM_QUICK_CAPTURE, speciesID, 
							plantID, category, userName, commonName, scienceName, phenophaseID, protocolID, 
							latitude, longitude, imageID, date, notes);
					
					mPlantList.add(pi);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int numMoreSpecies = MAX_SPEICES - getCount;
		getFlickrResults(numMoreSpecies);
				
		// TODO Auto-generated method stub
		return null;
	}
	
	private void getFlickrResults(int numSpecies) {
		
		SharedPreferences pref = mContext.getSharedPreferences("userinfo", 0);;
	
		double getLatitude = Double.parseDouble(pref.getString("latitude", "0.0"));
		double getLongitude = Double.parseDouble(pref.getString("longitude", "0.0"));
		
		String url = new String(mContext.getString(R.string.request_to_get_result) + 
				"?lat=" + getLatitude + "&lon=" + getLongitude + "&limit=" + numSpecies);

		Log.i("K","URL : " + url);
			
		HttpClient httpClient = new DefaultHttpClient();
		
		HttpPost httpPost = new HttpPost(url);
		int getCount = 0;
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				HelperJSONParser jHelper = new HelperJSONParser();
				
				String result = "";
				
				StringBuilder result_str = new StringBuilder();
				String line = "";
				
				while((line = br.readLine()) != null){
					result_str.append(line);
				}
				
				result = result_str.toString();
				
				Log.i("K", "RESULT : " + result);
				
				try {
					JSONArray jsonAry = new JSONArray(result);
					
					for(int i = 0 ; i < jsonAry.length() ; i++) {
						String imageUrl = jsonAry.getJSONObject(i).getString("url");
						int speciesID = 999;
						int phenophaseID = 0;
						int protocolID = 1; // temporary put 1
						int category = HelperValues.LOCAL_FLICKR;
						String commonName = jsonAry.getJSONObject(i).getString("common");
						String scienceName = jsonAry.getJSONObject(i).getString("scientific");
						String userName = jsonAry.getJSONObject(i).getString("user");
						double latitude = Double.parseDouble(jsonAry.getJSONObject(i).getString("lat"));
						double longitude = Double.parseDouble(jsonAry.getJSONObject(i).getString("lon"));
						String date = jsonAry.getJSONObject(i).getString("date");
						String notes = "";
						//String hasImage = "";
						//String distance = "";
						
						int plantID = 0;
						
						HelperPlantItem pi = new HelperPlantItem(HelperValues.OTHERS_PLANT_LIST, 
								HelperValues.FROM_QUICK_CAPTURE, speciesID, 
								plantID, category, userName, commonName, scienceName, phenophaseID, protocolID, 
								latitude, longitude, imageUrl, date, notes);
						
						mPlantList.add(pi);
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		catch(IOException e) {
			
		}
	}
	
	protected void onPostExecute(Void unused) {
		mDialog.dismiss();
		
		Drawable marker = mContext.getResources().getDrawable(R.drawable.marker);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		
		Log.i("K", "in onPostExecute function");
		mMapController = mMapView.getController();
		mMapController.setZoom(12);
		mMapView.setBuiltInZoomControls(true);
		mMapView.invalidate();
		
		//MapView mapView, Drawable marker, ArrayList<PlantItem> plantList
		//delete 
		mMapView.getOverlays().clear();
		mMapView.getOverlays().add(new SpeciesMapOverlay(mMapView, marker, mPlantList));
		mMapView.getOverlays().add(mMyOverLay);
		
		//mMapController.setCenter(gPoint);
	}
}