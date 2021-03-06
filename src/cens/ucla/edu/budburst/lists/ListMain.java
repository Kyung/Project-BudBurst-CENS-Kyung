package cens.ucla.edu.budburst.lists;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cens.ucla.edu.budburst.PBBHelpPage;
import cens.ucla.edu.budburst.PBBLogin;
import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapter;
import cens.ucla.edu.budburst.adapter.MyListAdapterMainPage;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperListItem;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperSettings;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.myplants.PBBAddPlant;
import cens.ucla.edu.budburst.myplants.PBBAddSite;
import cens.ucla.edu.budburst.myplants.PBBPlantList;
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

/**
 * The first page of the List
 * @author kyunghan
 *
 */
public class ListMain extends ListActivity {
	
	private TextView myTitleText = null;
	private MyListAdapterMainPage mylistapdater;
	private HelperSharedPreference mPref;
	private ArrayList<ListGroupItem> mArr = new ArrayList<ListGroupItem>();
	private boolean isUserDefinedListOn = true;
	
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
	
	private void getLists() {
		
		isUserDefinedListOn = true;
		
		ArrayList<HelperListItem> listArr = new ArrayList<HelperListItem>();
		HelperListItem iItem = new HelperListItem();
		
		/*
		 *  1 : Project Budburst
		 *  2 : What's Invasive
		 *  3 : Poisonous
		 *  4 : Endangered
		 *  10 : Tree lists 
		 *  11 : What's Blooming
		 *  and more later.
		 */
		iItem.setHeaderText(getString(R.string.List_Official_Header));
		iItem.setTitle(getString(R.string.List_Project_Budburst_title));
		iItem.setImageURL("pbb_icon_main2");
		iItem.setDescription(getString(R.string.List_Budburst));
		listArr.add(iItem);
		
		iItem = new HelperListItem();
		iItem.setHeaderText("none");
		iItem.setTitle(getString(R.string.List_USDA_PlantLists_title));
		iItem.setImageURL("poisonous");
		iItem.setDescription(getString(R.string.List_USDA_PlantLists_content));
		listArr.add(iItem);
	
		// user defined lists - dynamically added. 
		OneTimeDBHelper oDBH = new OneTimeDBHelper(this);
		mArr = oDBH.getListGroupItem(this);
		
		if(mArr.size() == 0) {
			iItem = new HelperListItem();
			iItem.setHeaderText(getString(R.string.List_User_Plant_Header));
			iItem.setTitle(getString(R.string.No_Lists_Title));
			iItem.setImageURL("yellow_triangle_exclamation50");
			iItem.setDescription(getString(R.string.No_Lists_Text));
			listArr.add(iItem);
			
			isUserDefinedListOn = false;
		}
		else {
			
			for(int i = 0 ; i < mArr.size() ; i++) {
				String header = "none";
				
				// index 0 only holds the header at the top.
				if(i == 0) {
					header = getString(R.string.List_User_Plant_Header);
				}
				iItem = new HelperListItem();
				
				iItem.setHeaderText(header);
				iItem.setTitle(mArr.get(i).getCategoryName());
				iItem.setImageURL(String.valueOf(mArr.get(i).getCategoryID()));
				iItem.setDescription(mArr.get(i).getDescription());
				listArr.add(iItem);
			}
		}
		
		mylistapdater = new MyListAdapterMainPage(ListMain.this, R.layout.onetime_list ,listArr);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);
	}

	public void onResume() {
		super.onResume();
		
		getLists();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){

		mPref = new HelperSharedPreference(ListMain.this);
		double latitude = Double.parseDouble(mPref.getPreferenceString("latitude", "0.0"));
		double longitude = Double.parseDouble(mPref.getPreferenceString("longitude", "0.0"));
	
		/*
		 *  1 : Project Budburst
		 *  2 : What's Invasive
		 *  3 : Poisonous
		 *  4 : Endangered
		 *  > 10 : User Defined Lists
		 *  and more later.
		 */
		
		if(position == 0) {
			if(mPref.getPreferenceBoolean("localbudburst")) {
				Intent intent = new Intent(ListMain.this, ListSubCategory.class);
				intent.putExtra("category", HelperValues.LOCAL_BUDBURST_LIST);
				startActivity(intent);
			}
			else {
				Toast.makeText(ListMain.this, getString(R.string.go_to_settings_page), Toast.LENGTH_SHORT).show();
			}
		}
		else if(position == 1) {
			showLocalListDialog();
		}
		// if user defined lists selected.
		else {
			if(isUserDefinedListOn) {
				Intent intent = new Intent(ListMain.this, ListUserDefinedSpecies.class);
				PBBItems pbbItem = new PBBItems();			
				pbbItem.setCategory(mArr.get(position-2).getCategoryID());
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", 0);
				startActivity(intent);
			}
			else {
				Intent intent = new Intent(ListMain.this, HelperSettings.class);
				intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
				startActivity(intent);
			}
		}
	}

	private void showLocalListDialog() {
		new AlertDialog.Builder(ListMain.this)
		.setTitle(getString(R.string.AddPlant_SelectCategory))
		.setIcon(android.R.drawable.ic_menu_more)
		.setItems(R.array.plantcategory, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
				switch(which) {
				case 0:
					if(mPref.getPreferenceBoolean("localwhatsinvasive")) {
						Intent intent = new Intent(ListMain.this, ListSubCategory.class);
						intent.putExtra("category", HelperValues.LOCAL_WHATSINVASIVE_LIST);
						startActivity(intent);
					}
					else {
						Toast.makeText(ListMain.this, getString(R.string.go_to_settings_page), Toast.LENGTH_SHORT).show();
					}
					break;
					
				case 1:
					// Poisonous Plants
					if(mPref.getPreferenceBoolean("localpoisonous")) {
						Intent intent = new Intent(ListMain.this, ListSubCategory.class);
						intent.putExtra("category", HelperValues.LOCAL_POISONOUS_LIST);
						startActivity(intent);
					}
					else {
						Toast.makeText(ListMain.this, getString(R.string.go_to_settings_page), Toast.LENGTH_SHORT).show();
					}
					
					break;
					
				case 2:
					// Endangered Plants
					if(mPref.getPreferenceBoolean("localendangered")) {
						Intent intent = new Intent(ListMain.this, ListSubCategory.class);
						intent.putExtra("category", HelperValues.LOCAL_THREATENED_ENDANGERED_LIST);
						startActivity(intent);
					}
					else {
						Toast.makeText(ListMain.this, getString(R.string.go_to_settings_page), Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_back), null)
		.show();
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