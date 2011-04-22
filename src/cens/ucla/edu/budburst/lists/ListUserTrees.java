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

import cens.ucla.edu.budburst.PBBAddNotes;
import cens.ucla.edu.budburst.PBBAddSite;
import cens.ucla.edu.budburst.PBBPlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapterWithIndex;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperJSONParser;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.OneTimeMainPage;
import cens.ucla.edu.budburst.onetime.QuickCapture;
import cens.ucla.edu.budburst.onetime.OneTimeAddMyPlant;
import cens.ucla.edu.budburst.utils.PBBItems;
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

public class ListUserTrees extends ListActivity {
	
	private ArrayList<HelperPlantItem> treeLists;
	private MyListAdapterWithIndex MyAdapter = null;
	private ListView myList = null;
	private ProgressDialog dialog = null;
	private SharedPreferences pref;
	private HelperFunctionCalls helper;
	
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>();
	
	//private String mImagePath;
	private String mDate;
	private String mNewPlantSpeciesName;
	
	private int mCurrentPosition = 0;
	private int mProtocolID;
	private int mPreviousActivity = 0;
	private int mNewPlantSpeciesID;
	
	private Double mLatitude;
	private Double mLongitude;
	
	private CharSequence[] mSeqUserSite;
	
	private PBBItems pbbItem;
	
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
		
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		mPreviousActivity = bundle.getInt("from");
		
		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" UCLA Tree Lists");
	
		helper = new HelperFunctionCalls();
		mapUserSiteNameID = helper.getUserSiteIDMap(this);
		
		getTreeLists();
		// TODO Auto-generated method stub
	}
	
	public void getTreeLists() {
		// initialize treeLists
		treeLists = null;
		treeLists = new ArrayList<HelperPlantItem>();
		
		// open database and put all tree lists into the PlantItem
		OneTimeDBHelper otDBH = new OneTimeDBHelper(ListUserTrees.this);
		SQLiteDatabase otDB = otDBH.getReadableDatabase();
		
		Cursor cursor;
		cursor = otDB.rawQuery("SELECT id, common_name, science_name, credit FROM userDefineLists WHERE category = " + HelperValues.USER_DEFINED_TREE_LISTS + " ORDER BY common_name;", null);
		while(cursor.moveToNext()) {
			HelperPlantItem pi;
			pi = new HelperPlantItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
			treeLists.add(pi);
		}
		
		MyAdapter = new MyListAdapterWithIndex(ListUserTrees.this, R.layout.plantlist_item, treeLists);
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
		
		
		mCurrentPosition = position;
		
		/*
		 * Act differently based on the previous activity
		 *  1. FROM_PLANT_LIST : directly add the species into the database.
		 *  2. others : move to ListsDetail page.
		 */
		
		if(mPreviousActivity == HelperValues.FROM_PLANT_LIST) {
			showPlantDialog(position);
		}
		else if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE) {
			
			Intent intent = new Intent(ListUserTrees.this, GetPhenophase.class);

			pbbItem.setSpeciesID(treeLists.get(mCurrentPosition).SpeciesID);
			pbbItem.setCommonName(treeLists.get(mCurrentPosition).CommonName);
			pbbItem.setScienceName(treeLists.get(mCurrentPosition).SpeciesName);
			pbbItem.setProtocolID(HelperValues.QUICK_TREES_AND_SHRUBS);
			
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("from", HelperValues.FROM_UCLA_TREE_LISTS);
			
			startActivity(intent);
		}
		else {
			Intent intent = new Intent(ListUserTrees.this, ListDetail.class);
			pbbItem.setSpeciesID(treeLists.get(mCurrentPosition).SpeciesID);
			pbbItem.setCommonName(treeLists.get(mCurrentPosition).CommonName);
			pbbItem.setScienceName(treeLists.get(mCurrentPosition).SpeciesName);
			pbbItem.setProtocolID(HelperValues.QUICK_TREES_AND_SHRUBS);
			
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("from", HelperValues.FROM_UCLA_TREE_LISTS);

			startActivity(intent);
		}
	}
	
	
	private void showPlantDialog(int position) {
		
		mCurrentPosition = position;
		
		new AlertDialog.Builder(ListUserTrees.this)
		.setTitle(getString(R.string.AddPlant_SelectCategory))
		.setIcon(android.R.drawable.ic_menu_more)
		.setItems(R.array.category_only_trees, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String[] category = getResources().getStringArray(R.array.category_only_trees);
				
				if(category[which].equals("Deciduous Trees and Shrubs")) {
					mProtocolID = HelperValues.DECIDUOUS_TREES;
				}
				else if(category[which].equals("Evergreen Trees and Shrubs")) {
					mProtocolID = HelperValues.EVERGREEN_TREES;
				}
				else {
					
				}
				
				/*
				 * If the previous activity is from MY_PLANT, show the popup message.
				 */
				if(mPreviousActivity == HelperValues.FROM_PLANT_LIST) {
					popupDialog(mCurrentPosition);
				}
				/*
				 * Else, move to AddNotes page.
				 */
				else {
					
					Intent intent = new Intent(ListUserTrees.this, PBBAddNotes.class);
				
					pbbItem.setSpeciesID(treeLists.get(mCurrentPosition).SpeciesID);
					pbbItem.setCommonName(treeLists.get(mCurrentPosition).CommonName);
					pbbItem.setScienceName(treeLists.get(mCurrentPosition).SpeciesName);
					pbbItem.setProtocolID(mProtocolID);
					pbbItem.setDate(mDate);
					pbbItem.setLatitude(mLatitude);
					pbbItem.setLongitude(mLongitude);
					
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_QUICK_CAPTURE);
					
					startActivity(intent);
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_back), null)
		.show();
		
	}
	
	private void popupDialog(int position) {
		//Pop up choose site dialog box
		mNewPlantSpeciesID = treeLists.get(position).SpeciesID;
		mNewPlantSpeciesName = treeLists.get(position).CommonName;
		
		mSeqUserSite = helper.getUserSite(ListUserTrees.this);
		
		//Pop up choose site dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setItems(mSeqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				int new_plant_site_id = mapUserSiteNameID.get(mSeqUserSite[which].toString());
				String new_plant_site_name = mSeqUserSite[which].toString();
				
				if(new_plant_site_name == "Add New Site") {
					Intent intent = new Intent(ListUserTrees.this, PBBAddSite.class);
					pbbItem.setSpeciesID(HelperValues.UNKNOWN_SPECIES);
					pbbItem.setCommonName(treeLists.get(mCurrentPosition).CommonName);
					pbbItem.setProtocolID(mProtocolID);
					
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
					
					startActivity(intent);
				}
				else {
					if(helper.checkIfNewPlantAlreadyExists(HelperValues.UNKNOWN_SPECIES, 
							new_plant_site_id, ListUserTrees.this)){
						Toast.makeText(ListUserTrees.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{
						if(helper.insertNewMyPlantToDB(ListUserTrees.this, HelperValues.UNKNOWN_SPECIES, 
								mNewPlantSpeciesName, new_plant_site_id, new_plant_site_name, 
								mProtocolID, HelperValues.USER_DEFINED_TREE_LISTS)){
							Intent intent = new Intent(ListUserTrees.this, PBBPlantList.class);
							Toast.makeText(ListUserTrees.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							//clear all stacked activities.
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(ListUserTrees.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
}
