package cens.ucla.edu.budburst.lists;

import java.util.ArrayList;
import java.util.Hashtable;

import cens.ucla.edu.budburst.Help;
import cens.ucla.edu.budburst.Login;
import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.MyListAdapter;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.onetime.Flora_Observer;
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

	private ListView list;
	private LazyAdapter lazyadapter;
	private ArrayList <PlantItem> localArray;
	private SharedPreferences pref;
	private double latitude;
	private double longitude;
	private Items item;
	private int category;
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
		localArray = new ArrayList<PlantItem>();
		
		/*
		 *  Receive intent value from the previous activity
		 */
		Intent p_intent = getIntent();
		category = p_intent.getExtras().getInt("category");
		
		/*
		 *  Retrieve lat / lng
		 */
		pref = getSharedPreferences("userinfo", 0);
		latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
		longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
		
		/*
		 * Put the state and county in the title bar.
		 */
		
		String county = pref.getString("county", "");
		String state = pref.getString("state", "");
		
		myTitleText.setText(" " + getString(R.string.Local_BudBurst) + " (" + county + ", " + state + ")");
		
		/*
		 * Set Items object to pass to the DownloadListFromServer
		 */
		item = new Items(latitude, longitude, category);
		 
		list = getListView();
				
		switch(category) {
		case Values.BUDBURST_LIST:
			checkLocalBudburst();
			break;
		case Values.WHATSINVASIVE_LIST:
			checkLocalWhatsinvasive();
			break;
		case Values.NATIVE_LIST:
			checkLocalNative();
			break;
		case Values.POISONOUS_LIST:
			checkLocalPoisonous();
			break;
		}        
	}
	
	public void checkLocalBudburst() {
		/*
		 * If data is stored in the database, no need to request to the server.
		 *  - not implement county checking yet
		 */
		if(!pref.getBoolean("localbudburst", false)) {
			
			/*
			 *  Download list from the server
			 *  - be based on the type
			 */
			DownloadListFromServer downloadlist = new DownloadListFromServer(this, list, lazyadapter, item);
			downloadlist.execute(item);
			
			/*
			 * Set localbudburst preferece to true 
			 */
			pref = getSharedPreferences("userinfo",0);
			SharedPreferences.Editor edit = pref.edit();				
			edit.putBoolean("localbudburst", true);
			edit.commit();

		}
		else {
			callFromDatabaseForBudburst();
		}
	}
	
	public void checkLocalWhatsinvasive() {
		if(!pref.getBoolean("localwhatsinvasive", false)) {
			
			/*
			 *  Download list from the server
			 *  - be based on the type
			 */
			DownloadListFromServer downloadlist = new DownloadListFromServer(this, list, lazyadapter, item);
			downloadlist.execute(item);
			
			/*
			 * Set localbudburst preferece to true 
			 */
			pref = getSharedPreferences("userinfo",0);
			SharedPreferences.Editor edit = pref.edit();				
			edit.putBoolean("localwhatsinvasive", true);
			edit.commit();

		}
		else {
			callFromDatabase();
		}
	}
	
	public void checkLocalNative() {
		if(!pref.getBoolean("localnative", false)) {
			
			/*
			 *  Download list from the server
			 *  - be based on the type
			 */
			DownloadListFromServer downloadlist = new DownloadListFromServer(this, list, lazyadapter, item);
			downloadlist.execute(item);
			
			/*
			 * Set localbudburst preferece to true 
			 */
			pref = getSharedPreferences("userinfo",0);
			SharedPreferences.Editor edit = pref.edit();				
			edit.putBoolean("localnative", true);
			edit.commit();
		
		}
		else {
			callFromDatabase();
		}
	}
	
	public void checkLocalPoisonous() {
		if(!pref.getBoolean("localpoisonous", false)) {
			
			/*
			 *  Download list from the server
			 *  - be based on the type
			 */
			DownloadListFromServer downloadlist = new DownloadListFromServer(this, list, lazyadapter, item);
			downloadlist.execute(item);
			/*
			 * Set localbudburst preferece to true 
			 */
			pref = getSharedPreferences("userinfo",0);
			SharedPreferences.Editor edit = pref.edit();				
			edit.putBoolean("localpoisonous", true);
			edit.commit();
		
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
				+ category 
				+ " ORDER BY LOWER(common_name) ASC;", null);
		
		while(cursor.moveToNext()) {
			
			StaticDBHelper staticDB = new StaticDBHelper(ListSubCategory.this);
			SQLiteDatabase sDB = staticDB.getReadableDatabase();
			
			Cursor getSpeciesInfo = sDB.rawQuery("SELECT _id, species_name, common_name, protocol_id, category, description FROM species WHERE species_name=\"" 
					+ cursor.getString(0) + "\";", null);
			
			
			while(getSpeciesInfo.moveToNext()) {
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+getSpeciesInfo.getInt(0), null, null);
				
				PlantItem pi;
				/*
				 *  pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
				 */
				
				/*
				 * Insert into PlantItem object
				 */
				pi = new PlantItem(resID, getSpeciesInfo.getString(2), getSpeciesInfo.getString(1), getSpeciesInfo.getInt(0), getSpeciesInfo.getInt(3));

				localArray.add(pi);
			}
			
			getSpeciesInfo.close();
			sDB.close();
		}
		
		otDBH.close();
		otDB.close();
		cursor.close();
		
		
		MyListAdapter mylistapdater = new MyListAdapter(ListSubCategory.this, R.layout.plantlist_item2, localArray);
		list.setAdapter(mylistapdater);
	}
	
	public void callFromDatabase() {
		/*
		 * Open database and read data from the localPlantLists table.
		 */
		OneTimeDBHelper otDBH = new OneTimeDBHelper(this);
		SQLiteDatabase otDB = otDBH.getReadableDatabase();

		Cursor cursor = otDB.rawQuery("SELECT category, common_name, science_name, photo_url FROM localPlantLists WHERE category=" 
				+ category 
				+ " ORDER BY LOWER(common_name) ASC;", null);
		
		while(cursor.moveToNext()) {
			PlantItem pi = new PlantItem(cursor.getString(1) 
					, cursor.getString(2)
					, cursor.getString(3)
					, cursor.getInt(0));
			localArray.add(pi);
		}
		
		otDBH.close();
		otDB.close();
		cursor.close();
		
		/*
		 * Connect to the adapter
		 */
		lazyadapter = new LazyAdapter(this, localArray);
        list.setAdapter(lazyadapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		if(category == Values.BUDBURST_LIST) {
			/*
			 *  aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
			 */
			Intent intent = new Intent(ListSubCategory.this, ListsDetail.class);
			
			intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
			intent.putExtra("category", Values.BUDBURST_LIST);
			intent.putExtra("science_name", localArray.get(position).SpeciesName);
			//intent.putExtra("id", localArray.get(position).SpeciesID);

			startActivity(intent);
			
		}
		else {
			Intent intent = new Intent(ListSubCategory.this, ListsDetail.class);
			
			intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
			intent.putExtra("category", localArray.get(position).Category);
			intent.putExtra("science_name", localArray.get(position).SpeciesName);
			//intent.putExtra("id", localArray.get(position).SpeciesID);

			startActivity(intent);
		}
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
				DownloadListFromServer downloadlist = new DownloadListFromServer(ListSubCategory.this, list, lazyadapter, item);
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
