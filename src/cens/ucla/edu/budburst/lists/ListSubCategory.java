package cens.ucla.edu.budburst.lists;

import java.util.ArrayList;
import java.util.Hashtable;

import cens.ucla.edu.budburst.PBBHelpPage;
import cens.ucla.edu.budburst.PBBLogin;
import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapter;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.helper.HelperLazyAdapter;
import cens.ucla.edu.budburst.onetime.OneTimePBBLists;
import cens.ucla.edu.budburst.utils.PBBItems;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ListSubCategory extends ListActivity {

	private ListView mList;
	private HelperLazyAdapter mLazyadapter;
	private ArrayList <HelperPlantItem> localArray;
	private HelperSharedPreference mPref;
	private double mLatitude;
	private double mLongitude;
	private ListItems item;
	private int mCategory;
	private TextView myTitleText;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.locallist);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
        
		/*
		 *  Initialize ArrayList
		 */
		localArray = new ArrayList<HelperPlantItem>();
		
		/*
		 *  Receive intent value from the previous activity
		 */
		Bundle bundle = getIntent().getExtras();
		mCategory = bundle.getInt("category");
		
		Log.i("K", "ListSubCategory - category : " + mCategory);
		
		/*
		 *  Retrieve lat / lng
		 */
		mPref = new HelperSharedPreference(this);
		mLatitude = Double.parseDouble(mPref.getPreferenceString("latitude", "0.0"));
		mLongitude = Double.parseDouble(mPref.getPreferenceString("longitude", "0.0"));
		
		/*
		 * Put the state and county in the title bar.
		 */
		
		String county = mPref.getPreferenceString("county", "");
		String state = mPref.getPreferenceString("state", "");
		
		myTitleText.setText(" " + getString(R.string.Local_BudBurst) + " (" + county + ", " + state + ")");
		
		/*
		 * Set Items object to pass to the DownloadListFromServer
		 */
		item = new ListItems(mLatitude, mLongitude);
		 
		mList = getListView();
				
		switch(mCategory) {
		case HelperValues.LOCAL_BUDBURST_LIST:
			checkLocalBudburst();
			break;
		case HelperValues.LOCAL_WHATSINVASIVE_LIST:
			checkLocalWhatsinvasive();
			break;
		case HelperValues.LOCAL_POISONOUS_LIST:
			checkLocalPoisonous();
			break;
		case HelperValues.LOCAL_THREATENED_ENDANGERED_LIST:
			checkLocalEndangered();
			break;
		}        
	}
	
	public void checkLocalBudburst() {
		/*
		 * If data is stored in the database, no need to request to the server.
		 *  - not implement county checking yet
		 */
		if(!mPref.getPreferenceBoolean("localbudburst")) {
			
			/*
			 *  Download list from the server
			 *  - be based on the type
			 */
			ListDownloadFromServer downloadlist = new ListDownloadFromServer(this, mList, mLazyadapter, item, HelperValues.LOCAL_BUDBURST_LIST);
			downloadlist.execute(item);
			
			/*
			 * Set localbudburst preferece to true 
			 */
			
			mPref.setPreferencesBoolean("localbudburst", true);				
			
			Intent intent = new Intent(ListSubCategory.this, ListSubCategory.class);
			startActivity(intent);
		}
		else {
			callFromDatabaseForBudburst();
		}
	}
	
	public void checkLocalWhatsinvasive() {
		if(!mPref.getPreferenceBoolean("localwhatsinvasive")) {
			
			/*
			 *  Download list from the server
			 *  - be based on the type
			 */
			ListDownloadFromServer downloadlist = new ListDownloadFromServer(this, mList, mLazyadapter, item, HelperValues.LOCAL_WHATSINVASIVE_LIST);
			downloadlist.execute(item);
			
			/*
			 * Set localbudburst preferece to true 
			 */
			mPref.setPreferencesBoolean("localwhatsinvasive", true);		

			Intent intent = new Intent(ListSubCategory.this, ListSubCategory.class);
			startActivity(intent);
			
		}
		else {
			callFromDatabase();
		}
	}
	
	public void checkLocalPoisonous() {
		if(!mPref.getPreferenceBoolean("localpoisonous")) {
			
			/*
			 *  Download list from the server
			 *  - be based on the type
			 */
			ListDownloadFromServer downloadlist = new ListDownloadFromServer(this, mList, mLazyadapter, item, HelperValues.LOCAL_POISONOUS_LIST);
			downloadlist.execute(item);
			/*
			 * Set localbudburst preferece to true 
			 */
			mPref.setPreferencesBoolean("localpoisonous", true);
			
			Intent intent = new Intent(ListSubCategory.this, ListSubCategory.class);
			startActivity(intent);
		
		}
		else {
			callFromDatabase();
		}
	}
	
	public void checkLocalEndangered() {
		if(!mPref.getPreferenceBoolean("localendangered")) {
			
			/*
			 *  Download list from the server
			 *  - be based on the type
			 */
			ListDownloadFromServer downloadlist = new ListDownloadFromServer(this, mList, mLazyadapter, item, HelperValues.LOCAL_THREATENED_ENDANGERED_LIST);
			downloadlist.execute(item);
			/*
			 * Set localbudburst preferece to true 
			 */
			mPref.setPreferencesBoolean("localendangered", true);
			
			Intent intent = new Intent(ListSubCategory.this, ListSubCategory.class);
			startActivity(intent);
		
		}
		else {
			callFromDatabase();
		}
	}
	
	/*
	 *  Only Local budburst is different from other lists
	 */
	public void callFromDatabaseForBudburst() {
		OneTimeDBHelper otDBH = new OneTimeDBHelper(this);
		SQLiteDatabase otDB = otDBH.getReadableDatabase();

		Cursor cursor = otDB.rawQuery("SELECT science_name FROM localPlantLists WHERE category=" 
				+ mCategory 
				+ " ORDER BY LOWER(common_name) ASC;", null);
		
		while(cursor.moveToNext()) {
			
			StaticDBHelper staticDB = new StaticDBHelper(ListSubCategory.this);
			SQLiteDatabase sDB = staticDB.getReadableDatabase();
			
			Cursor getSpeciesInfo = sDB.rawQuery("SELECT _id, species_name, common_name, protocol_id, category, description FROM species WHERE species_name=\"" 
					+ cursor.getString(0) + "\";", null);
			
			
			while(getSpeciesInfo.moveToNext()) {
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+getSpeciesInfo.getInt(0), null, null);
				
				HelperPlantItem pi;
				/*
				 *  pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
				 */
				
				/*
				 * Insert into PlantItem object
				 * public HelperPlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int aProtocolID){
				 */
				pi = new HelperPlantItem();
				pi.setPicture(resID);
				pi.setCommonName(getSpeciesInfo.getString(2));
				pi.setSpeciesName(getSpeciesInfo.getString(1));
				pi.setSpeciesID(getSpeciesInfo.getInt(0));
				pi.setProtocolID(getSpeciesInfo.getInt(3));

				localArray.add(pi);
			}
			
			getSpeciesInfo.close();
			sDB.close();
		}
		
		otDBH.close();
		otDB.close();
		cursor.close();
		
		
		if(localArray.size() == 0) {
			TextView instruction = (TextView)findViewById(R.id.instruction);
			instruction.setVisibility(View.VISIBLE);
		}
		
		
		MyListAdapter mylistapdater = new MyListAdapter(ListSubCategory.this, R.layout.plantlist_item2, localArray);
		mList.setAdapter(mylistapdater);
	}
	
	public void callFromDatabase() {
		/*
		 * Open database and read data from the localPlantLists table.
		 */
		OneTimeDBHelper otDBH = new OneTimeDBHelper(this);
		SQLiteDatabase otDB = otDBH.getReadableDatabase();

		Cursor cursor = otDB.rawQuery("SELECT common_name, science_name, photo_url FROM localPlantLists WHERE category=" 
				+ mCategory 
				+ " ORDER BY LOWER(common_name) ASC;", null);
		
		while(cursor.moveToNext()) {
			
			HelperPlantItem pi = new HelperPlantItem();
			pi.setCommonName(cursor.getString(0));
			pi.setSpeciesName(cursor.getString(1));
			pi.setImageURL(cursor.getString(2));
			pi.setCategory(mCategory);
			
			localArray.add(pi);
		}
		
		otDBH.close();
		otDB.close();
		cursor.close();
		
		if(localArray.size() == 0) {
			TextView instruction = (TextView)findViewById(R.id.instruction);
			instruction.setVisibility(View.VISIBLE);
		}
		
		/*
		 * Connect to the adapter
		 */
		mLazyadapter = new HelperLazyAdapter(this, localArray);
		mList.setAdapter(mLazyadapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		Intent intent = new Intent(ListSubCategory.this, ListDetail.class);
		
		PBBItems pbbItem = new PBBItems();
		pbbItem.setScienceName(localArray.get(position).getSpeciesName());
		pbbItem.setCommonName(localArray.get(position).getCommonName());
		pbbItem.setSpeciesID(localArray.get(position).getSpeciesID());
		pbbItem.setCategory(mCategory);
		
		intent.putExtra("pbbItem", pbbItem);
		intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);		
		
		startActivity(intent);
	}
	
	
	/*
	 * Menu option(non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 0, getString(R.string.Menu_Refresh)).setIcon(android.R.drawable.ic_menu_rotate);
		return true;
	}
	
	/*
	 * Menu option selection handling(non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem _item){

		switch(_item.getItemId()){
			case 1:
				ListDownloadFromServer downloadlist = new ListDownloadFromServer(ListSubCategory.this, mList, mLazyadapter, item, mCategory);
				downloadlist.execute(item);

				return true;
		}
		return false;
	}

	@Override
    public void onDestroy()
    {
    	//ladapter.imageLoader.stopThread();
        //list.setAdapter(null);
        super.onDestroy();
    }
}
