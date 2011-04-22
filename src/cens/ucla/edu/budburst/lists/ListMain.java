package cens.ucla.edu.budburst.lists;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cens.ucla.edu.budburst.PBBAddPlant;
import cens.ucla.edu.budburst.PBBAddSite;
import cens.ucla.edu.budburst.PBBHelpPage;
import cens.ucla.edu.budburst.PBBLogin;
import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.PBBPlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapterMainPage;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperListItem;
import cens.ucla.edu.budburst.helper.HelperSettings;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.onetime.OneTimePBBLists;
import cens.ucla.edu.budburst.onetime.OneTimeMainPage;
import cens.ucla.edu.budburst.onetime.OneTimeAddMyPlant;
import cens.ucla.edu.budburst.utils.PBBItems;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListMain extends ListActivity {
	
	private TextView myTitleText = null;
	private MyListAdapterMainPage mylistapdater;
	private SharedPreferences pref;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.onetimemain);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);
		
		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.Menu_Lists));

	    // TODO Auto-generated method stub
	}

	public void onResume() {
		super.onResume();
		
		
		OneTimeDBHelper otDBH = new OneTimeDBHelper(this);
		SQLiteDatabase otDB = otDBH.getReadableDatabase();

		Cursor cursor = otDB.rawQuery("SELECT category, common_name, photo_url FROM localPlantLists", null);
		
		while(cursor.moveToNext()) {
			//Log.i("K", "Category : " + cursor.getInt(0) + " common_name : " + cursor.getString(1));
		}
		
		otDBH.close();
		otDB.close();
		cursor.close();
		
		
		ArrayList<HelperListItem> listArr = new ArrayList<HelperListItem>();
		HelperListItem iItem;
		
		/*
		 * oneTime(Header String, title, icon_name, sub_title)
		 * - Note : don't put the icon_name in the string file.
		 */
		
		/*
		 *  1 : Project Budburst
		 *  2 : What's Invasive
		 *  3 : Poisonous
		 *  4 : Endangered
		 *  10 : Tree lists 
		 *  11 : What's Blooming
		 *  and more later.
		 */

		// 1 : Budburst
		iItem = new HelperListItem(getString(R.string.List_Official_Header), getString(R.string.List_Project_Budburst_title), "pbb_icon_main", getString(R.string.List_Budburst));
		listArr.add(iItem);
		
		// 2 :  Local invasive
		iItem = new HelperListItem("none", getString(R.string.List_Whatsinvasive_title), "invasive_plant", getString(R.string.List_Whatsinvasive));
		listArr.add(iItem);
		
		// 3 : Local Poisonous
		iItem = new HelperListItem("none", "Local Poisonous", "poisonous", getString(R.string.List_Whats_poisonous));
		listArr.add(iItem);
		
		// 4 : Local Endangered
		iItem = new HelperListItem("none", getString(R.string.List_Whatsendangered_title), "endangered", getString(R.string.List_Whats_endangered));
		listArr.add(iItem);

		// 10 : UCLA treelists
		iItem = new HelperListItem(getString(R.string.List_User_Plant_Header), "UCLA Trees", "s1000", getString(R.string.List_User_Plant_UCLA_trees));
		listArr.add(iItem);
		
		// 11 : Whats blooming
		iItem = new HelperListItem("none", getString(R.string.List_Whatsblooming_title), "pbbicon", getString(R.string.List_User_Plant_SAMO));
		listArr.add(iItem);
		
		mylistapdater = new MyListAdapterMainPage(ListMain.this, R.layout.onetime_list ,listArr);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);
	
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
	
		Intent intent = null;
		
		pref = getSharedPreferences("userinfo", 0);
		double latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
		double longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
	
		/*
		 *  1 : Project Budburst
		 *  2 : What's Invasive
		 *  3 : Poisonous
		 *  4 : Endangered
		 *  10 : Tree lists 
		 *  11 : What's Blooming
		 *  and more later.
		 */
		switch(position) {
		case 0:
			if(!pref.getBoolean("listDownloaded", false)) {
				Toast.makeText(ListMain.this, getString(R.string.Still_Downloading), Toast.LENGTH_SHORT).show();
			}
			else {
				intent = new Intent(ListMain.this, ListSubCategory.class);
				intent.putExtra("category", HelperValues.LOCAL_BUDBURST_LIST);
				startActivity(intent);
			}
			break;
		case 1:
			if(!pref.getBoolean("listDownloaded", false)) {
				Toast.makeText(ListMain.this, getString(R.string.Still_Downloading), Toast.LENGTH_SHORT).show();
			}
			else {
				intent = new Intent(ListMain.this, ListSubCategory.class);
				intent.putExtra("category", HelperValues.LOCAL_WHATSINVASIVE_LIST);
				startActivity(intent);
			}	
			break;
			
		case 2:
			// Poisonous Plants
			if(!pref.getBoolean("listDownloaded", false)) {
				Toast.makeText(ListMain.this, getString(R.string.Still_Downloading), Toast.LENGTH_SHORT).show();
			}
			else {
				intent = new Intent(ListMain.this, ListSubCategory.class);
				intent.putExtra("category", HelperValues.LOCAL_POISONOUS_LIST);
				startActivity(intent);
			}
			
			//Toast.makeText(ListMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
			break;
			
		case 3:
			// Endangered Plants
			if(!pref.getBoolean("listDownloaded", false)) {
				Toast.makeText(ListMain.this, getString(R.string.Still_Downloading), Toast.LENGTH_SHORT).show();
			}
			else {
				intent = new Intent(ListMain.this, ListSubCategory.class);
				intent.putExtra("category", HelperValues.LOCAL_THREATENED_ENDANGERED_LIST);
				startActivity(intent);
			}
			break;
			
		case 4:
			// Local Plant From Users (TreeLists)
			if(pref.getBoolean("getTreeLists", false)) {
				intent = new Intent(ListMain.this, ListUserTrees.class);
				PBBItems pbbItem = new PBBItems();
				pbbItem.setCategory(HelperValues.USER_DEFINED_TREE_LISTS);
				
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", 0);
				startActivity(intent);
			}
			else {
				if(pref.getBoolean("firstDownloadTreeList", true)) {
					Toast.makeText(ListMain.this, "To download the list, Go 'Settings' page", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(ListMain.this, "Still downloading - Tree lists", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case 5:
			// Whats Blooming
			Toast.makeText(ListMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
			break;		
		}
	}
	

	
	/*
	 * Menu option(non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, getString(R.string.Menu_help)).setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 2, 0, getString(R.string.Menu_settings)).setIcon(android.R.drawable.ic_menu_preferences);
		
		return true;
	}
	
	/*
	 * Menu option selection handling(non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){
		
		switch(item.getItemId()){
		case 1:
			Toast.makeText(ListMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
			return true;
		case 2:			
			Intent intent = new Intent(ListMain.this, HelperSettings.class);
			intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
			startActivity(intent);
			return true;
		}
		return false;
	}
}