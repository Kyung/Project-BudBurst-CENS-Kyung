package cens.ucla.edu.budburst.lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.helper.MyListAdapterWithIndex;
import cens.ucla.edu.budburst.onetime.AddNotes;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.OneTimeMain;
import cens.ucla.edu.budburst.onetime.QuickCapture;
import cens.ucla.edu.budburst.onetime.Whatsinvasive;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UserDefinedTreeLists extends ListActivity {
	
	private ArrayList<PlantItem> treeLists;
	private MyListAdapterWithIndex MyAdapter = null;
	private ListView myList = null;
	private ProgressDialog dialog = null;
	private SharedPreferences pref;
	private FunctionsHelper helper;
	
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>();
	
	private String image_path;
	private String dt_taken;
	private String camera_image_id;
	private String new_plant_species_name;
	
	private int current_position = 0;
	private int protocol_id;
	private int pheno_id;
	private int previousActivity = 0;
	private int new_plant_species_id;
	
	private Double latitude;
	private Double longitude;
	
	private CharSequence[] seqUserSite;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.getphenophase);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);
		
		Intent getIntent = getIntent();
		
		previousActivity = getIntent.getExtras().getInt("from", 0);
		pheno_id = getIntent.getExtras().getInt("pheno_id");
		camera_image_id = getIntent.getExtras().getString("camera_image_id");
		latitude = getIntent.getExtras().getDouble("latitude");
		longitude = getIntent.getExtras().getDouble("longitude");
		dt_taken = getIntent.getExtras().getString("dt_taken"); 
		
		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" UCLA Tree Lists");
	
		helper = new FunctionsHelper();
		mapUserSiteNameID = helper.getUserSiteIDMap(this);
		
		getTreeLists();
		// TODO Auto-generated method stub
	}
	
	public void getTreeLists() {
		// initialize treeLists
		treeLists = null;
		treeLists = new ArrayList<PlantItem>();
		
		// open database and put all tree lists into the PlantItem
		OneTimeDBHelper otDBH = new OneTimeDBHelper(UserDefinedTreeLists.this);
		SQLiteDatabase otDB = otDBH.getReadableDatabase();
		
		Cursor cursor;
		cursor = otDB.rawQuery("SELECT id, common_name, science_name, credit FROM userDefineLists ORDER BY common_name;", null);
		while(cursor.moveToNext()) {
			PlantItem pi;
			pi = new PlantItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
			treeLists.add(pi);
		}
		
		MyAdapter = new MyListAdapterWithIndex(UserDefinedTreeLists.this, R.layout.plantlist_item, treeLists);
		myList = getListView();
		// need to add setFastScrollEnalbed(true) for showing the index box in the list...
		myList.setFastScrollEnabled(true);
		myList.setAdapter(MyAdapter);
		
		cursor.close();
		otDBH.close();
		otDB.close();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		/*
		 * Act differently based on the previous activity
		 *  1. FROM_PLANT_LIST : directly add the species into the database.
		 *  2. others : move to ListsDetail page.
		 */
		
		if(previousActivity == Values.FROM_PLANT_LIST) {
			unKnownPlantDialog(position);
		}
		else if(previousActivity == Values.FROM_QUICK_CAPTURE) {
			
			Intent intent = new Intent(UserDefinedTreeLists.this, AddNotes.class);
			
			intent.putExtra("cname", treeLists.get(position).CommonName);
			intent.putExtra("sname", treeLists.get(position).SpeciesName);
			intent.putExtra("protocol_id", 9); // temporary put protocol_id to 9
			intent.putExtra("camera_image_id", camera_image_id);
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
			intent.putExtra("dt_taken", dt_taken);
			intent.putExtra("pheno_id", pheno_id);
			intent.putExtra("species_id", treeLists.get(position).SpeciesID);
			//intent.putExtra("category", -1);
			intent.putExtra("from", Values.FROM_UCLA_TREE_LISTS);
			
			startActivity(intent);
		}
		else {
			Intent intent = new Intent(UserDefinedTreeLists.this, ListsDetail.class);
			intent.putExtra("from", Values.FROM_UCLA_TREE_LISTS);
			intent.putExtra("id", treeLists.get(position).SpeciesID);
			startActivity(intent);
		}
		
		/*
		Intent intent = new Intent(UserDefinedTreeLists.this, QuickCapture.class);
		intent.putExtra("from", Values.FROM_UCLA_TREE_LISTS);
		intent.putExtra("tree_id", treeLists.get(position).SpeciesID);
		intent.putExtra("cname", treeLists.get(position).CommonName);
		intent.putExtra("sname", treeLists.get(position).SpeciesName);
		*/
	}
	
	
	private void unKnownPlantDialog(int position) {
		
		current_position = position;
		
		new AlertDialog.Builder(UserDefinedTreeLists.this)
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
				
				/*
				 * If the previous activity is from MY_PLANT, show the popup message.
				 */
				if(previousActivity == Values.FROM_PLANT_LIST) {
					popupDialog(current_position);
				}
				/*
				 * Else, move to AddNotes page.
				 */
				else {
					
					Intent intent = new Intent(UserDefinedTreeLists.this, AddNotes.class);
					intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
					intent.putExtra("cname", treeLists.get(current_position).CommonName);
					intent.putExtra("sname", treeLists.get(current_position).SpeciesName);
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
		new_plant_species_name = treeLists.get(position).CommonName;
		
		seqUserSite = helper.getUserSite(UserDefinedTreeLists.this);
		
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
					Intent intent = new Intent(UserDefinedTreeLists.this, AddSite.class);
					intent.putExtra("species_id", Values.UNKNOWN_SPECIES);
					intent.putExtra("from", Values.FROM_PLANT_LIST);
					intent.putExtra("species_name", treeLists.get(current_position).CommonName);
					intent.putExtra("protocol_id", protocol_id);
					intent.putExtra("species_id", Values.UNKNOWN_SPECIES);
					startActivity(intent);
				}
				else {
					if(helper.checkIfNewPlantAlreadyExists(new_plant_species_id, new_plant_site_id, UserDefinedTreeLists.this)){
						Toast.makeText(UserDefinedTreeLists.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{
						if(helper.insertNewMyPlantToDB(UserDefinedTreeLists.this, new_plant_species_id, new_plant_species_name, new_plant_site_id, new_plant_site_name, protocol_id)){
							Intent intent = new Intent(UserDefinedTreeLists.this, PlantList.class);
							Toast.makeText(UserDefinedTreeLists.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							//clear all stacked activities.
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(UserDefinedTreeLists.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
}
