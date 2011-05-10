package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cens.ucla.edu.budburst.PBBHelpPage;
import cens.ucla.edu.budburst.PBBLogin;
import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.adapter.MyListAdapterMainPage;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperSettings;
import cens.ucla.edu.budburst.helper.HelperSharedPreference;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.helper.HelperListItem;
import cens.ucla.edu.budburst.lists.ListGroupItem;
import cens.ucla.edu.budburst.lists.ListSubCategory;
import cens.ucla.edu.budburst.lists.ListUserDefinedSpeciesDownload;
import cens.ucla.edu.budburst.lists.ListMain;
import cens.ucla.edu.budburst.lists.ListUserDefinedSpecies;
import cens.ucla.edu.budburst.myplants.GetPhenophaseObserver;
import cens.ucla.edu.budburst.myplants.PBBAddPlant;
import cens.ucla.edu.budburst.myplants.PBBAddSite;
import cens.ucla.edu.budburst.myplants.PBBPlantList;
import cens.ucla.edu.budburst.utils.PBBItems;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceActivity;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OneTimeMainPage extends ListActivity {

	ArrayList<Button> buttonBar = new ArrayList<Button>();
	
	private int mPhenoID;
	private int mPreviousActivity = 0;
	
	private TextView myTitleText = null;
	private EditText unknownText = null;
	private Button skipBtn = null;
	private EditText et1 = null;
	
	private MyListAdapterMainPage mylistapdater;
	private HelperSharedPreference mPref;
	private HelperFunctionCalls helper;
	private Dialog noteDialog;
	
	private ArrayList<ListGroupItem> mArr = new ArrayList<ListGroupItem>();
	
	private PBBItems pbbItem;
	
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
		myTitleText.setText(" " + getString(R.string.OneTimeMain_Header));
	    
		mPref = new HelperSharedPreference(this);
		mPref.setPreferencesBoolean("visited", false);
	    
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		mPreviousActivity = bundle.getInt("from");
		
		helper = new HelperFunctionCalls();
		//LinearLayout ll = (LinearLayout)findViewById(R.id.header_item);
		
		showCategory();
	    // TODO Auto-generated method stub
	}
	
	
	private void showCategory() {
		ArrayList<HelperListItem> listArr = new ArrayList<HelperListItem>();
		HelperListItem iItem = new HelperListItem();
		
		/*
		 *  1 : Project Budburst
		 *  2 : What's Invasive
		 *  3 : Native <- temporarily not used
		 *  4 : Poisonous
		 *  5 : Endangered
		 *  > 10 : User Defined Lists
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
			iItem.setTitle("No list yet");
			iItem.setImageURL("yellow_triangle_exclamation50");
			iItem.setDescription("Please download the user defined lists. Menu->Settings->Download User Defined List");
			listArr.add(iItem);
			
			isUserDefinedListOn = false;
		}
		
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

		mylistapdater = new MyListAdapterMainPage(OneTimeMainPage.this, R.layout.onetime_list ,listArr);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
	
		if(position == 0) {
			if(mPreviousActivity == HelperValues.FROM_PLANT_LIST) {
				Intent intent = new Intent(OneTimeMainPage.this, PBBAddPlant.class);
				pbbItem.setCategory(HelperValues.LOCAL_BUDBURST_LIST);
				
				intent.putExtra("pbbItem", pbbItem);
				startActivity(intent);
			} 
			// else from Quick_Capture...
			else {
				Intent intent = new Intent(OneTimeMainPage.this, OneTimePBBLists.class);
				pbbItem.setCategory(HelperValues.LOCAL_BUDBURST_LIST);
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", mPreviousActivity);
				startActivity(intent);
			}
		}
		else if(position == 1) {
			showLocalListDialog();
		}
		// if user defined lists selected.
		else {
			if(isUserDefinedListOn) {
				Intent intent = new Intent(OneTimeMainPage.this, ListUserDefinedSpecies.class);
				PBBItems pbbItem = new PBBItems();			
				pbbItem.setCategory(mArr.get(position-2).getCategoryID());
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", mPreviousActivity);
				startActivity(intent);
			}
			else {
				Intent intent = new Intent(OneTimeMainPage.this, HelperSettings.class);
				intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
				startActivity(intent);
			}
		}
	}
	
	private void showLocalListDialog() {
		new AlertDialog.Builder(OneTimeMainPage.this)
		.setTitle(getString(R.string.AddPlant_SelectCategory))
		.setIcon(android.R.drawable.ic_menu_more)
		.setItems(R.array.plantcategory, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
				switch(which) {
				case 0:
					if(mPref.getPreferenceBoolean("localwhatsinvasive")) {
						Intent intent = new Intent(OneTimeMainPage.this, OneTimeAddMyPlant.class);
						pbbItem.setCategory(HelperValues.LOCAL_WHATSINVASIVE_LIST);
						intent.putExtra("pbbItem", pbbItem);
					    intent.putExtra("from", mPreviousActivity);
						startActivity(intent);
					}
					else {
						Toast.makeText(OneTimeMainPage.this, getString(R.string.go_to_settings_page), Toast.LENGTH_SHORT).show();
					}
					break;
					
				case 1:
					// Poisonous Plants
					if(mPref.getPreferenceBoolean("localpoisonous")) {
						Intent intent = new Intent(OneTimeMainPage.this, OneTimeAddMyPlant.class);
						pbbItem.setCategory(HelperValues.LOCAL_POISONOUS_LIST);
						intent.putExtra("pbbItem", pbbItem);
						intent.putExtra("from", mPreviousActivity);
						startActivity(intent);
					}
					else {
						Toast.makeText(OneTimeMainPage.this, getString(R.string.go_to_settings_page), Toast.LENGTH_SHORT).show();
					}
					
					break;
					
				case 2:
					// Endangered Plants
					if(mPref.getPreferenceBoolean("localendangered")) {
						Intent intent = new Intent(OneTimeMainPage.this, OneTimeAddMyPlant.class);
						pbbItem.setCategory(HelperValues.LOCAL_THREATENED_ENDANGERED_LIST);
						intent.putExtra("pbbItem", pbbItem);
						intent.putExtra("from", mPreviousActivity);
						startActivity(intent);
					}
					else {
						Toast.makeText(OneTimeMainPage.this, getString(R.string.go_to_settings_page), Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_back), null)
		.show();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */

	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0,"Help").setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 2, 0, getString(R.string.Menu_settings)).setIcon(android.R.drawable.ic_menu_preferences);
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case 1:
				intent = new Intent(OneTimeMainPage.this, PBBHelpPage.class);
				intent.putExtra("from", HelperValues.FROM_ONE_TIME_MAIN);
				startActivity(intent);
				return true;
			case 2:
				intent = new Intent(OneTimeMainPage.this, HelperSettings.class);
				intent.putExtra("from", HelperValues.FROM_ONE_TIME_MAIN);
				startActivity(intent);
				return true;

		}
		return false;
	}
}











