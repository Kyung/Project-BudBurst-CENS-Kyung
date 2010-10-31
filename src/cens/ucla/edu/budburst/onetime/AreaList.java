package cens.ucla.edu.budburst.onetime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import cens.ucla.edu.budburst.PlantInformation;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.JSONHelper;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AreaList extends ListActivity {
	private ArrayList<area> arAreaList;
	private MyListAdapter mylistapdater;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	  
	    // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.arealist);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.flora_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  Select the area");
	    
	    getData();
	    // TODO Auto-generated method stub
	}
	
	public void getData() {
		arAreaList = new ArrayList<area>();
		
		LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		double latitude = 0.0;
		double longitude = 0.0;
		
		if(!(manager.isProviderEnabled("gps") || manager.isProviderEnabled("network"))) {
			Toast.makeText(AreaList.this, "Network problem. Cannot get the list from WI Server", Toast.LENGTH_SHORT).show();
		}
		else {
			Location location = null;
			
			if(manager.isProviderEnabled("gps")) {
				location = manager.getLastKnownLocation("gps");
			}
			else if(manager.isProviderEnabled("network")) {
				location = manager.getLastKnownLocation("network");
			}
			
			if(location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}
		}

		String url = new String("http://sm.whatsinvasive.com/phone/getareas.php?lat=" + latitude + "&lon=" + longitude + "&r=7");
		Log.i("K", "url : " + url);

		new DoAsyncTask().execute(url);
	}
	
	
	class area{	
		area(String aId, String aTitle, String aLatitude, String aLongitude, String aDistance){
			id = aId;
			title = aTitle;
			latitude = aLatitude;
			longitude = aLongitude;
			distance = aDistance;
		}
		
		String id;
		String title;
		String latitude;
		String longitude;
		String distance;
	}
	
	
	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<area> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<area> aarSrc){
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}
		
		public int getCount(){
			return arSrc.size();
		}
		
		public String getItem(int position){
			return arSrc.get(position).title;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			TextView areaname = (TextView)convertView.findViewById(R.id.areaname);
			areaname.setText(arSrc.get(position).title);
			
			TextView textdesc = (TextView)convertView.findViewById(R.id.distance);
			
			// set the format of distance
			String distance = String.format("%7.3f", Double.parseDouble(arSrc.get(position).distance));
			textdesc.setText("Distance: " + distance + " miles");

			return convertView;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		Intent intent = new Intent(this, Whatsinvasive.class);

		intent.putExtra("selected_park", arAreaList.get(position).id);
		intent.putExtra("park_name", arAreaList.get(position).title);
		intent.putExtra("clicked_me", true);
		setResult(RESULT_OK, intent);
		finish();
		//startActivity(intent);
	}
	

	class DoAsyncTask extends AsyncTask<String, Integer, Void> {
		ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(AreaList.this, "Loading...", "Loading registered area based on your location", true);
		}
		@Override
		protected Void doInBackground(String... url) {
			
			HttpPost httpPost = new HttpPost(url[0]);
			try {
				
				HttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = httpClient.execute(httpPost);
				
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					
					BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
					String line = "{'area':";
					String[] str = null;
			
					line += br.readLine();
					line += "}";
					Log.i("K", "Line : " + line);
						
					JSONHelper jHelper = new JSONHelper();
					String getAreaByJSON = jHelper.getArea(line);
					str = getAreaByJSON.split("\n");
					

					for(int i = 0 ; i < str.length ; i++) {
						String[] split = str[i].split(";");
						
						Log.i("K", split[0] + " , " + split[1] + " , " + split[2] + " , " + split[3] + " , " + split[4]);
						
						area pi;
						pi = new area(split[0], split[1], split[2], split[3], split[4]);
						arAreaList.add(pi);
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
			mylistapdater = new MyListAdapter(AreaList.this, R.layout.arealist_item ,arAreaList);
			ListView MyList = getListView();
			MyList.setAdapter(mylistapdater);
			
			dialog.dismiss();
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);

		menu.add(0, 1, 0,"Queue").setIcon(android.R.drawable.ic_menu_sort_by_size);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case 1:
				intent = new Intent(AreaList.this, Queue.class);
				startActivity(intent);
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////

}
