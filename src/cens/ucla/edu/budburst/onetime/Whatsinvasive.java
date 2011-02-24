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
import android.content.DialogInterface.OnCancelListener;
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
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.SpeciesDetail;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.onetime.AreaList.DoAsyncTask;
import cens.ucla.edu.budburst.onetime.AreaList.area;

public class Whatsinvasive extends ListActivity {
	private SharedPreferences pref;
	private ArrayList<PlantItem> arSpeciesList;
	private MyListAdapter mylistapdater;
	private OneTimeDBHelper otDBH;
	private ProgressDialog dialog1 = null;
	private ProgressDialog dialog2 = null;
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>();
	private FunctionsHelper helper;
	private TextView myTitleText;
	
	private String area_id;
	private String area_name;
	private String cname;
	private String sname;
	private String image_path;
	private String dt_taken;
	private String camera_image_id;
	private String new_plant_species_name;
	
	private Double latitude;
	private Double longitude;
	
	private int new_plant_species_id;
	private int protocol_id;
	private int pheno_id;
	
	private CharSequence[] seqUserSite;
	
	private int previous_activity = 0;
	private int current_position = 0;
	protected static int GET_AREA_LIST = 1;
	protected static int TO_WI_INFO = 2;

	
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

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" Whats Invasive");
		
		Intent p_intent = getIntent();
		previous_activity = p_intent.getExtras().getInt("FROM");
		pheno_id = p_intent.getExtras().getInt("pheno_id");
		camera_image_id = p_intent.getExtras().getString("camera_image_id");
		latitude = p_intent.getExtras().getDouble("latitude");
		longitude = p_intent.getExtras().getDouble("longitude");
		dt_taken = p_intent.getExtras().getString("dt_taken"); 
			
	    arSpeciesList = new ArrayList<PlantItem>();
	    otDBH = new OneTimeDBHelper(Whatsinvasive.this);
	    
	    //Intent intent = new Intent(Whatsinvasive.this, AreaList.class);
		//startActivityForResult(intent, GET_AREA_LIST);
	    // TODO Auto-generated method stub
	    //getData();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();

		//Get User site name and id using Map.
	    helper = new FunctionsHelper();
		mapUserSiteNameID = helper.getUserSiteIDMap(Whatsinvasive.this);
		helper = new FunctionsHelper();
		
		File f = new File(Values.WI_PATH);
		if(!f.exists()) {
			f.mkdir();
		}
		getSpecies();
	}
	
	public void getSpecies(){
		
		SQLiteDatabase db;
		db = otDBH.getWritableDatabase();
		
		SharedPreferences pref = getSharedPreferences("userinfo", 0);
		area_name = pref.getString("area_name", "");
		area_id = pref.getString("area_id", "1");
		boolean restart = pref.getBoolean("wi_restart", false);
		myTitleText.setText(" " + area_name);
		
		
		Cursor cursor;
		cursor = db.rawQuery("SELECT * FROM speciesLists WHERE id=" + area_id +";", null);
		int count = cursor.getCount();
	
		cursor.close();
		db.close();
		otDBH.close();
		
		// if count is 0, it means there are no data related to the selected park
		if(count == 0 || restart == true) {
			new DoAsyncTask().execute();
		}
		else {
			showExistedSpecies(area_id);
		}
	}

	public void showExistedSpecies(String area_id) {
		
		arSpeciesList = null;
		arSpeciesList = new ArrayList<PlantItem>();
		
		SQLiteDatabase db;
		
		OneTimeDBHelper otDBH = new OneTimeDBHelper(Whatsinvasive.this);
		
		db = otDBH.getReadableDatabase();
		    
		Cursor cursor;
		cursor = db.rawQuery("SELECT cname, sname, text, image_name, image_url FROM speciesLists WHERE id = " 
		    		+ area_id + ";", null);
		    
		while(cursor.moveToNext()) {

			PlantItem pi;
			pi = new PlantItem(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
			arSpeciesList.add(pi);
		}
		
		mylistapdater = new MyListAdapter(Whatsinvasive.this, R.layout.plantlist_item2 ,arSpeciesList);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);
		
		cursor.close();
		otDBH.close();
		db.close();
	}

	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<PlantItem> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<PlantItem> aarSrc){
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}
		
		public int getCount(){
			return arSrc.size();
		}
		
		public String getItem(int position){
			return arSrc.get(position).CommonName;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);
			
			String imagePath = Values.WI_PATH + arSrc.get(position).imageUrl + ".jpg";
			
			cname = arSrc.get(position).CommonName;
			sname = arSrc.get(position).SpeciesName;
			
			Log.i("K", "IMAGE PATH : " + imagePath);
			img.setImageBitmap(helper.resizeImage(Whatsinvasive.this, imagePath));
			
			TextView textname = (TextView)convertView.findViewById(R.id.commonname);
			textname.setText(arSrc.get(position).CommonName);
			
			TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
			textdesc.setText(arSrc.get(position).SpeciesName);
			
			// call View from the xml and link the view to current position.
			View thumbnail = convertView.findViewById(R.id.wrap_icon);
			thumbnail.setTag(arSrc.get(position));
			thumbnail.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					PlantItem pi = (PlantItem)v.getTag();
					
					Intent intent = new Intent(Whatsinvasive.this, WIinfo.class);
					intent.putExtra("area_id", area_id);
					intent.putExtra("cname", pi.CommonName);
					startActivity(intent);
				}
			});
			
			return convertView;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		String imagePath = Values.WI_PATH + arSpeciesList.get(position).imageUrl + ".jpg";
		
		// if the previous activity is from "Add_PLANT", we just add a new species and end
		if(previous_activity == Values.FROM_PLANT_LIST) {
			unKnownPlantDialog(position);
		}
		else {
			unKnownPlantDialog(position);
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
					protocol_id = Values.WILD_FLOWERS;
				}
				else if(category[which].equals("Grass")) {
					protocol_id = Values.GRASSES;
				}
				else if(category[which].equals("Deciduous Trees and Shrubs")) {
					protocol_id = Values.DECIDUOUS_TREES;
				}
				else if(category[which].equals("Deciduous Trees and Shrubs - Wind")) {
					protocol_id = Values.DECIDUOUS_TREES_WIND;
				}
				else if(category[which].equals("Evergreen Trees and Shrubs")) {
					protocol_id = Values.EVERGREEN_TREES;
				}
				else if(category[which].equals("Evergreen Trees and Shrubs - Wind")) {
					protocol_id = Values.EVERGREEN_TREES_WIND;
				}
				else if(category[which].equals("Conifer")) {
					protocol_id = Values.CONIFERS;
				}
				else {
				}
				
				if(previous_activity == Values.FROM_PLANT_LIST) {
					popupDialog(current_position);
				}
				else {
					Intent intent = new Intent(Whatsinvasive.this, AddSite.class);
					intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
					intent.putExtra("cname", arSpeciesList.get(current_position).CommonName);
					intent.putExtra("sname", arSpeciesList.get(current_position).SpeciesName);
					intent.putExtra("pheno_id", pheno_id);
					intent.putExtra("protocol_id", protocol_id);
					intent.putExtra("dt_taken", dt_taken);
					intent.putExtra("latitude", latitude);
					intent.putExtra("longitude", longitude);
					intent.putExtra("species_id", Values.UNKNOWN_SPECIES);
					intent.putExtra("camera_image_id", camera_image_id);
					startActivity(intent);
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_back), null)
		.show();
		
	}
	
	private void popupDialog(int position) {
		//Pop up choose site dialog box
		new_plant_species_id = Values.UNKNOWN_SPECIES;
		new_plant_species_name = arSpeciesList.get(position).CommonName;
		
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
					intent.putExtra("species_id", Values.UNKNOWN_SPECIES);
					intent.putExtra("from", Values.FROM_PLANT_LIST);
					intent.putExtra("species_name", arSpeciesList.get(current_position).CommonName);
					intent.putExtra("protocol_id", protocol_id);
					intent.putExtra("species_id", Values.UNKNOWN_SPECIES);
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
	
	public boolean insertNewPlantToDB(int speciesid, String speciesname, int siteid, String sitename, int protocolid){

		int s_id = speciesid;
		SharedPreferences pref = getSharedPreferences("userinfo",0);
		
		Log.i("K", "PROTOCOL ID : " + protocolid);
		
		if(speciesid == Values.UNKNOWN_SPECIES) {
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
					protocolid + "," +
					//"1,"+ // 1 means it's official, from add plant list
					"'" + speciesname + "'," +
					"1, " +
					SyncDBHelper.SYNCED_NO + ");"
					);
			
			if(speciesid == Values.UNKNOWN_SPECIES) {
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
	
	class DoAsyncTask extends AsyncTask<Void, Integer, Void> {
		
		private boolean running = true;
		
		protected void onPreExecute() {
			deleteContents(Values.WI_PATH);
			
			dialog2 = ProgressDialog.show(Whatsinvasive.this, "Loading...", "Getting species from the server...", true);
			dialog2.setCancelable(true);
			dialog2.setOnCancelListener(new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					cancel(true);
				}
				
			});
			arSpeciesList = null;
			arSpeciesList = new ArrayList<PlantItem>();
		}
		
		@Override
		protected void onCancelled() {
			running = false;
			pref = getSharedPreferences("userinfo", 0);
			SharedPreferences.Editor edit = pref.edit();				
			edit.putBoolean("wi_restart", true);
			edit.commit();
			finish();
		}
		
		@Override
		protected Void doInBackground(Void... Void) {

			while(running) {
				SharedPreferences pref = getSharedPreferences("userinfo", 0);
				latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
				longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
				area_id = pref.getString("area_id", "9");
				area_name = pref.getString("area_name", "DEMO");
				
				HttpClient httpClient = new DefaultHttpClient();
				String url = new String("http://sm.whatsinvasive.com/phone/get_wi_lists.php?lat=" + latitude + "&lon=" + longitude);
				Log.i("K", "URL : " + url);
				HttpPost httpPost = new HttpPost(url);
				
				try {
					HttpResponse response = httpClient.execute(httpPost);
					
					if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						String[] str = null;
						String line = "{'tag':";

						line += br.readLine();
						line += "}";
						
						Log.i("K", "Line : " + line);
						
						JSONHelper jHelper = new JSONHelper();
						area_id = jHelper.getSiteId(line);
						area_name = jHelper.getSiteName(line);
						Log.i("K", "AREA ID : " + area_id);
						
						// save the site_id into the preference.
						pref = getSharedPreferences("userinfo", 0);
						SharedPreferences.Editor edit = pref.edit();
						edit.putString("area_id", area_id);
						edit.putString("area_name", area_name);
						edit.commit();
					
						// \n\n\n\n is used as divider
						

						String getAreaByJSON = jHelper.getPlantTags(line);
						
						str = getAreaByJSON.split("\n\n\n\n");
						
						// open database and set it writable
						SQLiteDatabase db;
						db = otDBH.getWritableDatabase();

						for(int i = 0 ; i < str.length ; i++) {
							String[] split = str[i].split(";;");

							String image_name = null;
							
							try {
								// creating unique name for downloaded species
								SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
								String randomNum = new Integer(prng.nextInt()).toString();
								MessageDigest sha = MessageDigest.getInstance("SHA-1");
								byte[] result = sha.digest(randomNum.getBytes());
		
								image_name = area_id + "_" + helper.hexEncode(Whatsinvasive.this, result);
							} catch (NoSuchAlgorithmException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							// don't insert the value "Other"
							if(!split[1].equals("Other")) {
								//insert into table						
								db.execSQL("INSERT INTO speciesLists VALUES(" + area_id + ","
										+ "'" + split[0] + "',"
										+ "'" + split[1] + "',"
										+ "'" + split[2] + "',"
										+ "'" + split[3] + "',"
										+ "'" + image_name + "');");
								
								// insert items into the list							
								helper.download_image("http://www.whatsinvasive.com/ci/images/" + split[3] + "_thumb.jpg", image_name, Values.WI_PATH);
								PlantItem pi;
								pi = new PlantItem(split[0], split[1], split[2], split[3], image_name);
								arSpeciesList.add(pi);
							}
						}
						
						running = false;
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
			}

			// TODO Auto-generated method stub
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			myTitleText.setText(" " + area_name);
			mylistapdater = new MyListAdapter(Whatsinvasive.this, R.layout.plantlist_item2 ,arSpeciesList);
			ListView MyList = getListView();
			MyList.setAdapter(mylistapdater);
			
			pref = getSharedPreferences("userinfo", 0);
			SharedPreferences.Editor edit = pref.edit();				
			edit.putBoolean("wi_restart", false);
			edit.commit();
			
			dialog2.dismiss();
		}
	}
	///////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		//menu.add(0, 1, 0, "Change Area").setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, 2, 0, "Refresh the List").setIcon(android.R.drawable.ic_menu_rotate);
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
				// delete the invasive lists from the same park...
				deleteContents(Values.WI_PATH);
				new DoAsyncTask().execute();
				return true;
		}
		return false;
	}
	
	public void deleteContents(String path) {
		File file = new File(path);
		if(file.isDirectory()) {
			String[] fileList = file.list();
			
			arSpeciesList = new ArrayList<PlantItem>();
			
			SQLiteDatabase db;
			db = otDBH.getWritableDatabase();
			db.execSQL("DELETE FROM speciesLists;");
			db.close();
			
			for(int i = 0 ; i < fileList.length ; i++) {
				new File(path + fileList[i]).delete();
				
			}
		}
	}
}
