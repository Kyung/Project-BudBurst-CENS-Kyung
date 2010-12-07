package cens.ucla.edu.budburst.onetime;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.AddPlant;
import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.Login;
import cens.ucla.edu.budburst.PlantInformation;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.AreaList.DoAsyncTask;
import cens.ucla.edu.budburst.onetime.AreaList.area;
import cens.ucla.edu.budburst.onetime.Queue.MyListAdapter;
import cens.ucla.edu.budburst.onetime.Queue.myItem;

public class Whatsinvasive extends ListActivity {
	private SharedPreferences pref;
	private ArrayList<species> arSpeciesList;
	private MyListAdapter mylistapdater;
	private OneTimeDBHelper otDBH;
	private String area_id;
	private String area_name;
	private String cname;
	private String sname;
	private Double latitude;
	private Double longitude;
	private String image_path;
	private String dt_taken;
	private String camera_image_id;
	private int new_plant_species_id;
	private int protocol_id;
	private String new_plant_species_name;
	private CharSequence[] seqUserSite;
	final private int PLANT_LIST = 99;
	private TextView areaTxt2 = null;
	private int previous_activity = 0;
	private int current_position = 0;
	private int WILD_FLOWERS = 2;
	private int GRASSES = 3;
	private int DECIDUOUS_TREES = 4;
	private int DECIDUOUS_TREES_WIND = 5;
	private int EVERGREEN_TREES = 6;
	private int EVERGREEN_TREES_WIND = 7;
	private int CONIFERS = 8;
	public final String TEMP_PATH = "/sdcard/pbudburst/wi_list/";
	protected static int GET_AREA_LIST = 1;
	protected static int TO_WI_INFO = 2;
	private ProgressDialog dialog = null;
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>();
	private FunctionsHelper helper = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.whatsinvasive);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  Select the species");
		
		Intent p_intent = getIntent();
		previous_activity = p_intent.getExtras().getInt("FROM");
		camera_image_id = p_intent.getExtras().getString("camera_image_id");
		latitude = p_intent.getExtras().getDouble("latitude");
		longitude = p_intent.getExtras().getDouble("longitude");
		dt_taken = p_intent.getExtras().getString("dt_taken"); 
			
	    arSpeciesList = new ArrayList<species>();
	    otDBH = new OneTimeDBHelper(Whatsinvasive.this);
	    
	    areaTxt2 = (TextView) findViewById(R.id.title2);
	   
		//Get User site name and id using Map.
		mapUserSiteNameID = getUserSiteIDMap();
		helper = new FunctionsHelper();
	    
	    //Intent intent = new Intent(Whatsinvasive.this, AreaList.class);
		//startActivityForResult(intent, GET_AREA_LIST);
	    // TODO Auto-generated method stub
	    getData();
	}
	
	public void getData() {
		
		LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		double latitude = 0.0;
		double longitude = 0.0;
		
		if(!(manager.isProviderEnabled("gps") || manager.isProviderEnabled("network"))) {
			Toast.makeText(Whatsinvasive.this, "Network problem. Cannot get the list from WI Server", Toast.LENGTH_SHORT).show();
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

		String url = new String("http://sm.whatsinvasive.com/phone/getareas.php?lat=" + latitude + "&lon=" + longitude + "&r=1");
		
		HttpPost httpPost = new HttpPost(url);
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
					area_id = split[0];
					area_name = split[1];
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		getSpecies();
	}
	
	
	/*
	class getAreaId extends AsyncTask<String, Integer, Void> {
		ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(Whatsinvasive.this, "Loading...", "Loading registered area based on your location", true);
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
						area_id = split[0];
						area_name = split[1];
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
			dialog.dismiss();
		}
	}
	*/
	public void getSpecies(){
		areaTxt2.setText(area_name);
		
		SQLiteDatabase db;
		db = otDBH.getWritableDatabase();
		
		Cursor cursor;
		cursor = db.rawQuery("SELECT * FROM speciesLists WHERE id=" + area_id +";", null);
		int count = cursor.getCount();
		
		Log.i("K", "AREA : " + area_id);
		Log.i("K", "count : " + cursor.getCount());
		cursor.close();
		db.close();
		otDBH.close();
		
		// if count is 0, it means there are no data related to the selected park
		if(count == 0) {
			arSpeciesList = null;
			arSpeciesList = new ArrayList<species>();
			//if(area_id)
			new DoAsyncTask().execute(area_id);
		}
		else {
			showExistedSpecies(area_id);
		}
	}

	public void showExistedSpecies(String area_id) {
		arSpeciesList = null;
		arSpeciesList = new ArrayList<species>();
		
		SQLiteDatabase db;
		
		OneTimeDBHelper otDBH = new OneTimeDBHelper(Whatsinvasive.this);
		
		db = otDBH.getReadableDatabase();
		    
		Cursor cursor;
		cursor = db.rawQuery("SELECT title, cname, sname, text, image_url FROM speciesLists WHERE id = " 
		    		+ area_id + ";", null);
		    
		while(cursor.moveToNext()) {

			species pi;
			pi = new species(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
			arSpeciesList.add(pi);
		}
		
		mylistapdater = new MyListAdapter(Whatsinvasive.this, R.layout.plantlist_item2 ,arSpeciesList);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);
		
		cursor.close();
		otDBH.close();
		db.close();
	}
	
	class species{	
		species(String aTitle, String aCommon_name, String aScience_name, String aText, String aImage_url){
			title = aTitle;
			common_name = aCommon_name;
			science_name = aScience_name;
			text = aText;
			image_url = aImage_url;
		}
		
		String title;
		String common_name;
		String science_name;
		String text;
		String image_url;
	}
	
	
	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<species> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<species> aarSrc){
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
			
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);

			String imagePath = TEMP_PATH + arSrc.get(position).image_url + ".jpg";
			//Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
	
			try{
				//FileOutputStream out = new FileOutputStream(imagePath);
				//bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
			}catch(Exception e){
				Log.e("K", e.toString());
			}
			image_path = imagePath;
			
			cname = arSrc.get(position).common_name;
			sname = arSrc.get(position).science_name;
			
			img.setBackgroundResource(R.drawable.shapedrawable);
			//img.setImageBitmap(bitmap);
			Log.i("K", "IMAGE PATH : " + imagePath);
			img.setImageBitmap(resizeImage(imagePath));
			
			TextView textname = (TextView)convertView.findViewById(R.id.commonname);
			textname.setText(arSrc.get(position).common_name);
			
			TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
			textdesc.setText(arSrc.get(position).science_name);

			return convertView;
		}
	}
	
	private Bitmap resizeImage(String path){
		FileInputStream fin = null;
		BufferedInputStream buf = null;
		
		File existFile = new File(path);
		if(!existFile.exists()) {
			Toast.makeText(Whatsinvasive.this, "Error occurs while processing the species list. ", Toast.LENGTH_SHORT).show();
			finish();
		}
		
    	Log.i("K", "PATH : " + path);
    	
		try {
			fin = new FileInputStream(path);
			Log.i("K", "FILE INPUT : " + fin);
			
			
			buf = new BufferedInputStream(fin);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bitmap bitmap = BitmapFactory.decodeStream(buf);
		
		Log.i("K", "" + bitmap);
		
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int newWidth = 60;
		int newHeight = 60;
		
		//Bitmap thumb = BitmapFactory.decodeFile(path, options);
		
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		
		Log.i("K", "SCALE WIDTH : " + scaleWidth);
		Log.i("K", "SCALE HEIGHT : " + scaleHeight);
		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		
		Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

    	return resized;
    }
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == GET_AREA_LIST) {
			if(resultCode == RESULT_OK) {
				area_id = data.getExtras().getString("selected_park");
				area_name = data.getExtras().getString("park_name");
				//clicked_me = data.getExtras().getString("clicked_me");
				Log.i("K","AREAID " + area_id + " AREANAME : " + area_name );
				getSpecies();
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		String imagePath = TEMP_PATH + arSpeciesList.get(position).image_url + ".jpg";
		
		// if the previous activity is from "Add_PLANT", we just add a new species and end
		if(previous_activity == PLANT_LIST) {
			unKnownPlantDialog(position);
		}
		else {
			
			Intent intent = new Intent(Whatsinvasive.this, GetPhenophase.class);
			intent.putExtra("from", "Whatsinvasive");
			intent.putExtra("cname", arSpeciesList.get(position).common_name);
			intent.putExtra("sname", arSpeciesList.get(position).science_name);
			intent.putExtra("protocol_id", protocol_id);
			intent.putExtra("dt_taken", dt_taken);
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
			intent.putExtra("species_id", 999);
			intent.putExtra("camera_image_id", "");
			
			startActivity(intent);
			
			/*
			Intent intent = new Intent(Whatsinvasive.this, WI_observation.class);
			intent.putExtra("sname", arSpeciesList.get(position).science_name);
			intent.putExtra("cname", arSpeciesList.get(position).common_name);
			intent.putExtra("image_path", imagePath);
			intent.putExtra("title", arSpeciesList.get(position).title);
			intent.putExtra("area_id", area_id);
			startActivity(intent);
			*/
		}
	}
	
	private void unKnownPlantDialog(int position) {
		
		current_position = position;
		
		new AlertDialog.Builder(Whatsinvasive.this)
		.setTitle(getString(R.string.AddPlant_SelectCategory))
		.setIcon(android.R.drawable.ic_menu_more)
		.setItems(R.array.category2, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String[] category = getResources().getStringArray(R.array.category2);
	
				if(category[which].equals("Wild Flowers and Herbs")) {
					protocol_id = WILD_FLOWERS;
				}
				else if(category[which].equals("Grass")) {
					protocol_id = GRASSES;
				}
				else if(category[which].equals("Deciduous Trees and Shrubs")) {
					protocol_id = DECIDUOUS_TREES;
				}
				else if(category[which].equals("Deciduous Trees and Shrubs - Wind")) {
					protocol_id = DECIDUOUS_TREES_WIND;
				}
				else if(category[which].equals("Evergreen Trees and Shrubs")) {
					protocol_id = EVERGREEN_TREES;
				}
				else if(category[which].equals("Evergreen Trees and Shrubs - Wind")) {
					protocol_id = EVERGREEN_TREES_WIND;
				}
				else if(category[which].equals("Conifer")) {
					protocol_id = CONIFERS;
				}
				else {
				}
				
				popupDialog(current_position);
			}
		})
		.setNegativeButton(getString(R.string.Button_back), null)
		.show();
		
	}
	
	private void popupDialog(int position) {
		//Pop up choose site dialog box
		new_plant_species_id = 999;
		new_plant_species_name = arSpeciesList.get(position).common_name;
		
		seqUserSite = helper.getUserSite(Whatsinvasive.this);
		
		//Pop up choose site dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setItems(seqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				int new_plant_site_id = mapUserSiteNameID.get(seqUserSite[which].toString());
				String new_plant_site_name = seqUserSite[which].toString();
				
				if(new_plant_site_name == "Add New Site") {
					Intent intent = new Intent(Whatsinvasive.this, AddSite.class);
					startActivity(intent);
				}
				else {
					if(helper.checkIfNewPlantAlreadyExists(new_plant_species_id, new_plant_site_id, Whatsinvasive.this)){
						Toast.makeText(Whatsinvasive.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{
						if(insertNewPlantToDB(new_plant_species_id, new_plant_species_name, new_plant_site_id, new_plant_site_name, protocol_id)){
							Intent intent = new Intent(Whatsinvasive.this, PlantList.class);
							Toast.makeText(Whatsinvasive.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							//clear all stacked activities.
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(Whatsinvasive.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
	
	public boolean insertNewPlantToDB(int speciesid, String speciesname, int siteid, String sitename, int protocol_id){

		int s_id = speciesid;
		SharedPreferences pref = getSharedPreferences("userinfo",0);
		
		if(speciesid == 999) {
			s_id = pref.getInt("other_species_id", 0);
			s_id++;
		}
		
		try{
			SyncDBHelper syncDBHelper = new SyncDBHelper(this);
			SQLiteDatabase syncDB = syncDBHelper.getWritableDatabase();
			
			syncDB.execSQL("INSERT INTO my_plants VALUES(" +
					"null," +
					 "999," +
					siteid + "," +
					"'" + sitename + "',"+
					protocol_id + "," +
					//"1,"+ // 1 means it's official, from add plant list
					"'" + speciesname + "'," +
					"1, " +
					SyncDBHelper.SYNCED_NO + ");"
					);
			
			if(speciesid == 999) {
				SharedPreferences.Editor edit = pref.edit();				
				edit.putInt("other_species_id", s_id);
				edit.commit();
			}
			
			syncDBHelper.close();
			return true;
		}
		catch(Exception e){
			Log.e("K",e.toString());
			return false;
		}
	}
	
	
	class DoAsyncTask extends AsyncTask<String, Integer, Void> {
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(Whatsinvasive.this, "Loading...", "Getting species from the server...", true);
		}
		@Override
		protected Void doInBackground(String... area_id) {
			
			HttpClient httpClient = new DefaultHttpClient();
			String url = new String("http://sm.whatsinvasive.com/phone/gettags.php?id=" + area_id[0]);
			Log.i("K", "URL : " + url);
			HttpPost httpPost = new HttpPost(url);
			
			try {
				HttpResponse response = httpClient.execute(httpPost);
				
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					String line = "{'tag':";
					String[] str = null;
					
					line += br.readLine();
					line += "}";
					
					Log.i("K", "Line : " + line);
					
					JSONHelper jHelper = new JSONHelper();
					String getAreaByJSON = jHelper.getPlantTags(line);
					str = getAreaByJSON.split("\n\n\n\n");

					Log.i("K", "length : " + str.length);
					
					// open database and set it writable
					SQLiteDatabase db;
					db = otDBH.getWritableDatabase();

					for(int i = 0 ; i < str.length ; i++) {
						String[] split = str[i].split(";;");

						String image_name = null;
						
						try {
							SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
							String randomNum = new Integer(prng.nextInt()).toString();
							MessageDigest sha = MessageDigest.getInstance("SHA-1");
							byte[] result = sha.digest(randomNum.getBytes());
							image_name = area_id[0] + "_" + hexEncode(result);
						} catch (NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if(!split[1].equals("Other")) {
							//insert into table						
							db.execSQL("INSERT INTO speciesLists VALUES(" + area_id[0] + ","
									+ "'" + split[0] + "',"
									+ "'" + split[1] + "',"
									+ "'" + split[2] + "',"
									+ "'" + split[3] + "',"
									+ "'" + image_name + "');");
							
							Log.i("K", "inserted into the table....");
							// insert items into the list
							
							download_image(split[4], image_name);
							species pi;
							pi = new species(split[0], split[1], split[2], split[3], image_name);
							arSpeciesList.add(pi);

						}
					}
					
					db.close();
					otDBH.close();
					
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
			mylistapdater = new MyListAdapter(Whatsinvasive.this, R.layout.plantlist_item2 ,arSpeciesList);
			ListView MyList = getListView();
			MyList.setAdapter(mylistapdater);
			
			dialog.dismiss();
		}
	}
	
	private void download_image(String url, String fileName) {
		
		URL imageURL;
		int read;
		
		try {
			imageURL = new URL(url);
			Log.i("K", "IMAGE URL : " + imageURL);
			HttpURLConnection conn = (HttpURLConnection)imageURL.openConnection();
			conn.connect();
			
			File f = new File(TEMP_PATH);
			if(!f.exists()) {
				f.mkdir();
			}
			
			int len = conn.getContentLength();
			byte[] buffer = new byte[len];
			InputStream is = conn.getInputStream();
			FileOutputStream fos = new FileOutputStream(TEMP_PATH + fileName + ".jpg");
			
			while ((read = is.read(buffer)) > 0) {
				fos.write(buffer, 0, read);
			}
			fos.close();
			is.close();
		}
		catch(Exception e) {
			
		}
	}
	
	///////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 0, "Change Area").setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, 2, 0, "Update Lists").setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, 3, 0, "Queue").setIcon(android.R.drawable.ic_menu_sort_by_size);		
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case 1:
				Intent intents = new Intent(Whatsinvasive.this, AreaList.class);
				startActivityForResult(intents, GET_AREA_LIST);
				return true;
			case 2:
				deleteContents("/sdcard/pbudburst/tmp/");
				new DoAsyncTask().execute(area_id);
				return true;
			case 3:
				Intent intent = new Intent(Whatsinvasive.this, Queue.class);
				startActivity(intent);
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
	
	void deleteContents(String path) {
		File file = new File(path);
		if(file.isDirectory()) {
			String[] fileList = file.list();
			
			arSpeciesList = new ArrayList<species>();
			
			for(int i = 0 ; i < fileList.length ; i++) {
				//File newFile = new File(fileList[i]);
				String delete_file_name = fileList[i];
				//Downloaded names of plantlists are starting with area_id
				//to prevent the duplication, we delete the species images on the park
				String[] split_delete_file_name = delete_file_name.split("_");
				Log.i("K", "SPLIT : " + split_delete_file_name[0]);
				Log.i("K", "AREA ID: " + area_id);
				SQLiteDatabase db;
				db = otDBH.getWritableDatabase();
				if(split_delete_file_name[0].equals(area_id)) {
					Log.i("K", "FILE_NAME : " + delete_file_name);
					delete_file_name = delete_file_name.replace(".jpg", "");
					Log.i("K", "IMAGE_NAME_IN_TABLE " + delete_file_name);
					//delete speciesList						
					db.execSQL("DELETE FROM speciesLists WHERE image_url='" + delete_file_name + "';");
					Log.i("K", "FILE NAME : " + "/sdcard/pbudburst/tmp/" + delete_file_name + " IS DELETED FROM THE TABLE.");
					
					new File("/sdcard/pbudburst/tmp/" + fileList[i]).delete();
					Log.i("K", "FILE NAME : " + "/sdcard/pbudburst/tmp/" + fileList[i] + " IS DELETED.");
				}
				db.close();
			}
		}
	}
	
	public HashMap<String, Integer> getUserSiteIDMap(){

		HashMap<String, Integer> localMapUserSiteNameID = new HashMap<String, Integer>(); 
		
		//Open plant list db from static db
		SyncDBHelper syncDBHelper = new SyncDBHelper(this);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase(); 
		
		//Rereive syncDB and add them to arUserPlatList arraylist
		Cursor cursor = syncDB.rawQuery("SELECT site_name, site_id FROM my_sites;",null);
		while(cursor.moveToNext()){
			localMapUserSiteNameID.put(cursor.getString(0), cursor.getInt(1));
		}
		localMapUserSiteNameID.put("Add New Site", 10000);
		
		
		//Close DB and cursor
		cursor.close();
		//syncDB.close();
		syncDBHelper.close();
	
		return localMapUserSiteNameID;
	}
	
	static private String hexEncode( byte[] aInput){
		   StringBuilder result = new StringBuilder();
		   char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m'};
		   for ( int idx = 0; idx < 4; ++idx) {
			   byte b = aInput[idx];
			   result.append( digits[ (b&0xf0) >> 4 ] );
			   result.append( digits[ b&0x0f] );
		   }
		   return result.toString();
	}
}
