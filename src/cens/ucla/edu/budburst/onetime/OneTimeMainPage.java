package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cens.ucla.edu.budburst.PBBAddPlant;
import cens.ucla.edu.budburst.PBBAddSite;
import cens.ucla.edu.budburst.GetPhenophaseObserver;
import cens.ucla.edu.budburst.PBBHelpPage;
import cens.ucla.edu.budburst.PBBLogin;
import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.PBBPlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperSettings;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.helper.HelperListItem;
import cens.ucla.edu.budburst.lists.ListSubCategory;
import cens.ucla.edu.budburst.lists.ListUserPlants;
import cens.ucla.edu.budburst.lists.ListMain;
import cens.ucla.edu.budburst.lists.ListUserTrees;
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
	private String mCameraImageID = "";
	
	private TextView myTitleText = null;
	private EditText unknownText = null;
	private Button skipBtn = null;
	private EditText et1 = null;
	
	private MyListAdapter mylistapdater;
	private SharedPreferences pref;
	private HelperFunctionCalls helper;
	private Dialog noteDialog;
	
	private PBBItems pbbItem;
	
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
	    
	    pref = getSharedPreferences("userinfo",0);
	    SharedPreferences.Editor edit = pref.edit();				
		edit.putString("visited","false");
		edit.commit();
		
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		mPreviousActivity = bundle.getInt("from");
		
		helper = new HelperFunctionCalls();
		//LinearLayout ll = (LinearLayout)findViewById(R.id.header_item);
		
	    // TODO Auto-generated method stub
	}

	public void onResume() {
		super.onResume();
		
		ArrayList<HelperListItem> listArr = new ArrayList<HelperListItem>();
		HelperListItem iItem;;
		
		/*
		 *  1 : Project Budburst
		 *  2 : What's Invasive
		 *  3 : Native <- temporarily not used
		 *  4 : Poisonous
		 *  5 : Endangered
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

		// 3 : Local Native
		//iItem = new HelperListItem("none", getString(R.string.List_Native_title), "pbb_icon_main", getString(R.string.List_Whats_native));
		//listArr.add(iItem);
		
		// 4 : Local Poisonous
		iItem = new HelperListItem("none", "Local Poisonous", "poisonous", getString(R.string.List_Whats_poisonous));
		listArr.add(iItem);
		
		// 5 : Local Endangered
		iItem = new HelperListItem("none", getString(R.string.List_Whatsendangered_title), "endangered", getString(R.string.List_Whats_endangered));
		listArr.add(iItem);

		// 10 : UCLA treelists
		iItem = new HelperListItem(getString(R.string.List_User_Plant_Header), "UCLA Trees", "s1000", getString(R.string.List_User_Plant_UCLA_trees));
		listArr.add(iItem);
		
		// 11 : Whats blooming
		iItem = new HelperListItem("none", getString(R.string.List_Whatsblooming_title), "pbbicon", getString(R.string.List_User_Plant_SAMO));
		listArr.add(iItem);

		mylistapdater = new MyListAdapter(OneTimeMainPage.this, R.layout.onetime_list ,listArr);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);
	}


	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<HelperListItem> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<HelperListItem> aarSrc){
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}
		
		public int getCount(){
			return arSrc.size();
		}
		
		public String getItem(int position){
			return arSrc.get(position).Title;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);
			
			img.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/"+arSrc.get(position).ImageUrl, null, null));
			//img.setBackgroundResource(R.drawable.shapedrawable);
			
			TextView header_view = (TextView) convertView.findViewById(R.id.list_header);
			TextView title_view = (TextView) convertView.findViewById(R.id.list_name);
			TextView title_desc = (TextView) convertView.findViewById(R.id.list_name_detail);
			
			// if the header is not "none", show the header on the screen.
			if(!arSrc.get(position).Header.equals("none")) {
				header_view.setText(" " + arSrc.get(position).Header);
				header_view.setVisibility(View.VISIBLE);
			}
			else {
				header_view.setVisibility(View.GONE);
			}
			
			title_view.setText(arSrc.get(position).Title);
			title_desc.setText(arSrc.get(position).Description + " ");
	
			return convertView;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
	
		Intent intent = null;
		
		switch(position) {
		/*
		 *  1 : Project Budburst
		 *  2 : What's Invasive
		 *  3 : Native <- temporarily not used
		 *  4 : Poisonous
		 *  5 : Endangered
		 *  10 : Tree lists 
		 *  11 : What's Blooming
		 *  and more later.
		 */
		case 0:
			if(mPreviousActivity == HelperValues.FROM_PLANT_LIST) {
				intent = new Intent(OneTimeMainPage.this, PBBAddPlant.class);
				intent.putExtra("pbbItem", pbbItem);
				startActivity(intent);
			} 
			// else from Quick_Capture...
			else {
				intent = new Intent(OneTimeMainPage.this, OneTimePBBLists.class);
				pbbItem.setCategory(HelperValues.LOCAL_BUDBURST_LIST);
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", mPreviousActivity);
				startActivity(intent);
			}
			break;
		case 1:
			/*
			 * Call WhatsInvasive
			 */
			if(!pref.getBoolean("listDownloaded", false)) {
				Toast.makeText(OneTimeMainPage.this, "Local lists are still downloading...", Toast.LENGTH_SHORT).show();
			}
			else {
				intent = new Intent(OneTimeMainPage.this, OneTimeAddMyPlant.class);
				pbbItem.setCategory(HelperValues.LOCAL_WHATSINVASIVE_LIST);
				intent.putExtra("pbbItem", pbbItem);
			    intent.putExtra("from", mPreviousActivity);
				startActivity(intent);
			}
		   	
			break;
		case 2:
			/*
			 * Whats Poisonous
			 */
			if(!pref.getBoolean("listDownloaded", false)) {
				Toast.makeText(OneTimeMainPage.this, getString(R.string.Still_Downloading), Toast.LENGTH_SHORT).show();
			}
			else {
				intent = new Intent(OneTimeMainPage.this, OneTimeAddMyPlant.class);
				pbbItem.setCategory(HelperValues.LOCAL_POISONOUS_LIST);
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", mPreviousActivity);
				startActivity(intent);
			}
			
			break;
		case 3:
			/*
			 * Whats Endangered
			 */
			// Endangered Plants
			if(!pref.getBoolean("listDownloaded", false)) {
				Toast.makeText(OneTimeMainPage.this, getString(R.string.Still_Downloading), Toast.LENGTH_SHORT).show();
			}
			else {
				intent = new Intent(OneTimeMainPage.this, OneTimeAddMyPlant.class);
				pbbItem.setCategory(HelperValues.LOCAL_THREATENED_ENDANGERED_LIST);
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", mPreviousActivity);
				startActivity(intent);
			}
			break;
		case 4:
			/*
			 * User Defined Lists
			 *  - UCLA Tree Lists
			 */
			if(pref.getBoolean("getTreeLists", false)) {
				intent = new Intent(OneTimeMainPage.this, ListUserTrees.class);
				pbbItem.setCategory(HelperValues.USER_DEFINED_TREE_LISTS);
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", mPreviousActivity);
				startActivity(intent);
			}
			else {
				if(pref.getBoolean("firstDownloadTreeList", true)) {
					Toast.makeText(OneTimeMainPage.this, "To download the list, Go 'Settings' page", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(OneTimeMainPage.this, "Still downloading - Tree lists", Toast.LENGTH_SHORT).show();
				}	
			}
			break;
		case 5:
			/*
			 * User Defined Lists
			 *  - What's blooming
			 */
			Toast.makeText(OneTimeMainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
			break;
		}
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











