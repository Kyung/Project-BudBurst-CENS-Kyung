package cens.ucla.edu.budburst.lists;

import java.util.ArrayList;

import cens.ucla.edu.budburst.Help;
import cens.ucla.edu.budburst.Login;
import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.Sync;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ListSubCategory extends Activity {

	private ListView list;
	private LazyAdapter lazyadapter;
	private ArrayList <PlantItem> localArray;
	private SharedPreferences pref;
	private double latitude;
	private double longitude;
	private Items item;
	private int category;
	
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

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.Local_BudBurst));
        
		// Initialize ArrayList
		localArray = new ArrayList<PlantItem>();
		
		// Receive intent value from the previous activity
		Intent p_intent = getIntent();
		category = p_intent.getExtras().getInt("category");
		
		/*
		 * retrieve lat / lng
		 */
		pref = getSharedPreferences("userinfo", 0);
		latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
		longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
		
		// Set Items object to pass to the DownloadListFromServer
		item = new Items(latitude, longitude, category);
		
		list=(ListView)findViewById(R.id.list);
				
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
			callFromDatabase();
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
	
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 0, getString(R.string.Menu_Refresh)).setIcon(android.R.drawable.ic_menu_rotate);
		return true;
	}
	
	//Menu option selection handling
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
        list.setAdapter(null);
        super.onDestroy();
    }
}
