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
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class SpeciesOthersFromServer extends AsyncTask<String, Void, Void>{
	private ProgressDialog mDialog;
	private Context mContext;
	private ArrayList<PlantItem> mPlantList;
	private boolean flagFinish = false;
	private MapController mMapController = null;
	private MyLocationOverlay mMyOverLay = null;
	private MapView mMapView = null;
	private int mCategory = 0;
	
	public SpeciesOthersFromServer(Context context, MapView mapView, MyLocationOverlay myOverLay, int category) {
		mContext = context;
		mMapView = mapView;
		mMyOverLay = myOverLay;
		mPlantList = new ArrayList<PlantItem>();
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
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				JSONHelper jHelper = new JSONHelper();
				
				String result = "";
				
				StringBuilder result_str = new StringBuilder();
				String line = "";
				
				while((line = br.readLine()) != null){
					result_str.append(line);
				}
				
				result = result_str.toString();
				
				Log.i("K", "RESULT : " + result);
				
				JSONArray jsonAry = new JSONArray(result);
				
				Log.i("K", "Length : " + jsonAry.length());
				
				// pi = new PlantItem(speciesID, commonName, scienceName, phenophaseID, latitude, longitude, imageName, dtTaken, notes);
				// plantList.add(pi);
				
				for(int i = 0 ; i < jsonAry.length() ; i++) {
					
					String imageID = jsonAry.getJSONObject(i).getString("Image_ID");
					int speciesID = Integer.parseInt(jsonAry.getJSONObject(i).getString("Species_ID"));
					int phenophaseID = Integer.parseInt(jsonAry.getJSONObject(i).getString("Phenophase_ID"));
					String commonName = jsonAry.getJSONObject(i).getString("Common_Name");
					String scienceName = jsonAry.getJSONObject(i).getString("Science_Name");
					double latitude = Double.parseDouble(jsonAry.getJSONObject(i).getString("Latitude"));
					double longitude = Double.parseDouble(jsonAry.getJSONObject(i).getString("Longitude"));
					String date = jsonAry.getJSONObject(i).getString("Date");
					String notes = jsonAry.getJSONObject(i).getString("Notes");
					String hasImage = jsonAry.getJSONObject(i).getString("Has_Image");
					String distance = jsonAry.getJSONObject(i).getString("Distance");
					
					Log.i("K", "imageID : " + imageID
							+ " speciesID : " + speciesID 
							+ " phenophaseID : " + phenophaseID
							+ " commonName : " + commonName
							+ " scienceName : " + scienceName
							+ " latitude : " + latitude
							+ " longitude : " + longitude
							+ " date : " + date
							+ " note : " + notes
							);
					
					
					PlantItem pi = new PlantItem(speciesID, commonName, scienceName, phenophaseID, latitude, longitude, imageID, date, notes);
					
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
		
		// TODO Auto-generated method stub
		return null;
	}
	
	protected void onPostExecute(Void unused) {
		mDialog.dismiss();
		
		
		Log.i("K", "in onPostExecute function");
		
		//GeoPoint gPoint = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));
		mMapView.setBuiltInZoomControls(true);
		
		mMapView.invalidate();
		
		Drawable marker = mContext.getResources().getDrawable(R.drawable.marker);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		
		//MapView mapView, Drawable marker, ArrayList<PlantItem> plantList
		mMapView.getOverlays().add(new SpeciesMapOverlay(mMapView, marker, mPlantList));
		mMapView.getOverlays().add(mMyOverLay);
		
		//mMapController.setCenter(gPoint);
		
	}
	
	public ArrayList<PlantItem> getPlantList() {
		return mPlantList;
	}
	
	public boolean finishDownloading() {
		return flagFinish;
	}
}