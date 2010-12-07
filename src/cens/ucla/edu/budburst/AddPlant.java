package cens.ucla.edu.budburst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;

public class AddPlant extends ListActivity{
	private final String TAG = "AddPlant";
	private ArrayList<PlantItem> arPlantList;
	
	private int mSelect = 0;
	private int WILD_FLOWERS = 2;
	private int GRASSES = 3;
	private int DECIDUOUS_TREES = 4;
	private int DECIDUOUS_TREES_WIND = 5;
	private int EVERGREEN_TREES = 6;
	private int EVERGREEN_TREES_WIND = 7;
	private int CONIFERS = 8;
	private Integer new_plant_species_id;
	private String new_plant_species_name;
	private Integer new_plant_site_id; 
	private String new_plant_site_name;
	private Integer protocol_id;
	
	private StaticDBHelper staticDBHelper = null;
	private SQLiteDatabase staticDB = null;
	private MyListAdapter mylistapdater = null;
	private ListView MyList = null;
	
	private RadioButton rb1 = null;
	private RadioButton rb2 = null;
	private RadioButton rb3 = null;
	
	private EditText et1;
	
	//private TextView header = null;
	private TextView myTitleText = null;
	
	FunctionsHelper helper = null;
	CharSequence[] seqUserSite;
	
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>(); 
		
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.addplant);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  " + getString(R.string.AddPlant_top10));
		
		helper = new FunctionsHelper();
		
		rb1 = (RadioButton)findViewById(R.id.option1);
		rb2 = (RadioButton)findViewById(R.id.option2);
		rb3 = (RadioButton)findViewById(R.id.option3);
		
		rb1.setOnClickListener(radio_listener);
		rb2.setOnClickListener(radio_listener);
		rb3.setOnClickListener(radio_listener);
		
		//header = (TextView) findViewById(R.id.header_text);
		//header.setText("'TOP 10' list of the plants.");
		
		//Check if site table is empty
		SyncDBHelper syncDBHelper = new SyncDBHelper(AddPlant.this);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase();
		
		Cursor cursor = syncDB.rawQuery("SELECT site_id FROM my_sites;", null);
		Log.d(TAG, String.valueOf(cursor.getCount()));
		if(cursor.getCount() == 0)
		{
			Toast.makeText(AddPlant.this, "Please add your site first.", 
					Toast.LENGTH_LONG).show();
			cursor.close();
			syncDBHelper.close();
			Intent intent = new Intent(AddPlant.this, PlantList.class);
			startActivity(intent);
			finish();		
			return;
		}else{
			syncDBHelper.close();
			cursor.close();
		}
		
		// show the top 10 lists first
		top10List();

		//Get User site name and id using Map.
		mapUserSiteNameID = getUserSiteIDMap();
		Iterator<String> itr = mapUserSiteNameID.keySet().iterator();
		while(itr.hasNext()){
			Log.d(TAG, itr.next());
		}
	}
	
	private void top10List() {
		staticDBHelper = new StaticDBHelper(AddPlant.this);
		staticDB = staticDBHelper.getReadableDatabase();
		
		myTitleText.setText(getString(R.string.AddPlant_top10));
		//header.setText("'TOP 10' list of the plants.");
		arPlantList = new ArrayList<PlantItem>();
	 	Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name FROM species ORDER BY common_name;", null);
		while(cursor.moveToNext()){
			Integer id = cursor.getInt(0);
			if(id == 70 || id == 69 || id == 45 || id == 59 || id == 60 || id == 19 || id == 32 || id == 34 || id == 24) {
				String species_name = cursor.getString(1);
				String common_name = cursor.getString(2);
							
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
				
				PlantItem pi;
				pi = new PlantItem(resID, common_name, species_name, id);
				arPlantList.add(pi);
			}
		}
			
		// add plant at the last.
		PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown Plant", "Unknown Plant", 999);
		arPlantList.add(pi);
			
		mylistapdater = new MyListAdapter(AddPlant.this, R.layout.plantlist_item2, arPlantList);
		MyList = getListView(); 
		MyList.setAdapter(mylistapdater);
		cursor.close();		
		staticDB.close();
	}
	
	private OnClickListener radio_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			staticDBHelper = new StaticDBHelper(AddPlant.this);
			staticDB = staticDBHelper.getReadableDatabase();
			
			if(v == rb1) {
				top10List();
			}
			else if (v == rb2) {
				myTitleText.setText(getString(R.string.AddPlant_all));
				//header.setText("'ALL' list of the plants.");
				//Rereive syncDB and add them to arUserPlatList arraylist
				arPlantList = new ArrayList<PlantItem>();
		 		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name FROM species ORDER BY common_name;", null);
				while(cursor.moveToNext()){
					Integer id = cursor.getInt(0);
					
					// if id is 999, skip that.
					if(id == 999) 
						continue;
				
					String species_name = cursor.getString(1);
					String common_name = cursor.getString(2);
									
					int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
						
					PlantItem pi;
					//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
					pi = new PlantItem(resID, common_name, species_name, id);
					arPlantList.add(pi);
				}
				
				// add plant at the last.
				PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown Plant", "Unknown Plant", 999);
				arPlantList.add(pi);
				
				mylistapdater = new MyListAdapter(AddPlant.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
			}
			else {
				
				//header.setText("By Group.");
				new AlertDialog.Builder(AddPlant.this)
				.setTitle(getString(R.string.AddPlant_SelectCategory))
				.setIcon(android.R.drawable.ic_menu_more)
				.setItems(R.array.category, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] category = getResources().getStringArray(R.array.category);
						StaticDBHelper staticDBHelper = new StaticDBHelper(AddPlant.this);
						SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase(); 
						
						arPlantList = new ArrayList<PlantItem>();
						Cursor cursor = null;

						if(category[which].equals("Wild Flowers and Herbs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + WILD_FLOWERS + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addFlowers));
							protocol_id = WILD_FLOWERS;
						}
						else if(category[which].equals("Grass")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + GRASSES + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addGrass));
							protocol_id = GRASSES;
						}
						else if(category[which].equals("Deciduous Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + DECIDUOUS_TREES + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addDecid));
							protocol_id = DECIDUOUS_TREES;
						}
						else if(category[which].equals("Evergreen Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + EVERGREEN_TREES + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addEvergreen));
							protocol_id = EVERGREEN_TREES;
						}
						else if(category[which].equals("Conifer")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + CONIFERS + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addConifer));
							protocol_id = CONIFERS;
						}
						else {
						}

						//header.setText("By Group > " + category[which]);
						while(cursor.moveToNext()){
							Integer id = cursor.getInt(0);
							String species_name = cursor.getString(1);
							String common_name = cursor.getString(2);
							Integer protocol_id = cursor.getInt(3);
										
							PlantItem pi;
							
							int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
							
							//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
							pi = new PlantItem(resID, common_name, species_name, id);
							arPlantList.add(pi);
						}
						
						PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown Plant", "Unknown Plant", 999);
						arPlantList.add(pi);
						
						mylistapdater = new MyListAdapter(AddPlant.this, R.layout.plantlist_item2, arPlantList);
						MyList = getListView(); 
						MyList.setAdapter(mylistapdater);
						
						cursor.close();
						staticDB.close();
						staticDBHelper.close();
						
					}
				})
				.setNegativeButton(getString(R.string.Button_back), null)
				.show();
				
			}
			staticDB.close();
			staticDBHelper.close();
		}
	};
	

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		new_plant_species_id = arPlantList.get(position).SpeciesID;
		new_plant_species_name = arPlantList.get(position).CommonName;

		//Retreive user sites from data base.
		//seqUserSite = mapUserSiteNameID.keySet().toArray(new String[0]);
		seqUserSite = helper.getUserSite(AddPlant.this);
		
		if(new_plant_species_id == 999) {
			unKnownPlantDialog();
		}
		else {
			popupDialog();
		}
	}
	
	private void unKnownPlantDialog() {
		
		new AlertDialog.Builder(AddPlant.this)
		.setTitle(getString(R.string.AddPlant_SelectCategory))
		.setIcon(android.R.drawable.ic_menu_more)
		.setCancelable(true)
		.setItems(R.array.category2, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String[] category = getResources().getStringArray(R.array.category2);
				
				if(category[which].equals("Wild Flowers and Herbs")) {
					protocol_id = WILD_FLOWERS;
				}
				else if(category[which].equals("Grass")) {
					protocol_id = GRASSES;
				}
				else if(category[which].equals("Deciduous Trees and Shrubs")) {
					protocol_id = DECIDUOUS_TREES;
				}
				else if(category[which].equals("Deciduous Trees and Shrubs - Wind")) {
					protocol_id = DECIDUOUS_TREES_WIND;
				}
				else if(category[which].equals("Evergreen Trees and Shrubs")) {
					protocol_id = EVERGREEN_TREES;
				}
				else if(category[which].equals("Evergreen Trees and Shrubs - Wind")) {
					protocol_id = EVERGREEN_TREES_WIND;
				}
				else if(category[which].equals("Conifer")) {
					protocol_id = CONIFERS;
				}
				else {
				}
				
				popupDialog();
			}
		})
		.setNegativeButton(getString(R.string.Button_back), null)
		.show();
		
	}
	
	private void popupDialog() {
		//Pop up choose site dialog box
		
		Log.i("K", "PROTOCOL ID : " + protocol_id);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setCancelable(true)
		.setItems(seqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			
				new_plant_site_id = mapUserSiteNameID.get(seqUserSite[which].toString());
				new_plant_site_name = seqUserSite[which].toString();
				
				if(new_plant_site_name == "Add New Site") {
					Intent intent = new Intent(AddPlant.this, AddSite.class);
					intent.putExtra("species_id", new_plant_species_id);
					intent.putExtra("species_name", new_plant_species_name);
					startActivity(intent);
				}
				else {
					if(helper.checkIfNewPlantAlreadyExists(new_plant_species_id, new_plant_site_id, AddPlant.this)){
						Toast.makeText(AddPlant.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{
						
						if(new_plant_species_id == 999) {
							newDialog();
						}
						else {
							if(insertNewPlantToDB(new_plant_species_id, new_plant_species_name, new_plant_site_id, new_plant_site_name, 0)){
								Intent intent = new Intent(AddPlant.this, PlantList.class);
								Toast.makeText(AddPlant.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
								//clear all stacked activities.
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								finish();
							}else{
								Toast.makeText(AddPlant.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
	
	private void newDialog() {
		Dialog dialog = new Dialog(AddPlant.this);
		
		dialog.setContentView(R.layout.species_name_custom_dialog);
		dialog.setTitle(getString(R.string.GetPhenophase_PBB_message));
		dialog.setCancelable(true);
		dialog.show();
		
		et1 = (EditText)dialog.findViewById(R.id.custom_common_name);
		Button doneBtn = (Button)dialog.findViewById(R.id.custom_done);
		
		doneBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String common_name = et1.getText().toString();
				
				if(insertNewPlantToDB(new_plant_species_id, common_name, new_plant_site_id, new_plant_site_name, protocol_id)){
					Intent intent = new Intent(AddPlant.this, PlantList.class);
					Toast.makeText(AddPlant.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
					//clear all stacked activities.
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}else{
					Toast.makeText(AddPlant.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			Intent intent = new Intent(AddPlant.this, PlantList.class);
			startActivity(intent);
			finish();
			return true;
	    }
		return super.onKeyDown(keyCode, event);
	}
	*/
	
	//Adapters:MyListAdapter and SeparatedAdapter
	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<PlantItem> arSrc;
		int layout;
		int previous_site = 0;
		
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
			
			TextView site_header = (TextView)convertView.findViewById(R.id.list_header);
			
			Log.i("K", "POSITION : " + position + " TOPITEM : " + arSrc.get(position).TopItem);
			
			if(arSrc.get(position).TopItem) {
				site_header.setVisibility(View.VISIBLE);
				site_header.setText("  " + arSrc.get(position).Site);
				
			}
			else {
				site_header.setVisibility(View.GONE);
			}
			
			ImageView img = (ImageView) convertView.findViewById(R.id.icon);
			img.setImageResource(arSrc.get(position).Picture);
			
			TextView textname = (TextView)convertView.findViewById(R.id.commonname);
			textname.setText(arSrc.get(position).CommonName);
			
			TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
			Log.i("K", "NAME : " + arSrc.get(position).SpeciesName);
			String [] splits = arSrc.get(position).SpeciesName.split(" ");
			textdesc.setText(splits[0] + " " + splits[1]);
			
			
			TextView pheno_stat = (TextView)convertView.findViewById(R.id.pheno_stat);
			if(arSrc.get(position).total_pheno != 0) {
				pheno_stat.setText(arSrc.get(position).current_pheno + " / " + arSrc.get(position).total_pheno);
			}
			else {
				pheno_stat.setVisibility(View.GONE);
			}

			return convertView;
		}
	}
	

	public boolean insertNewPlantToDB(int speciesid, String speciesname, int siteid, String sitename, int protocol_id){

		int s_id = speciesid;
		SharedPreferences pref = getSharedPreferences("userinfo",0);
		
		if(speciesid == 999) {
			s_id = pref.getInt("other_species_id", 0);
			s_id++;
		}
		
		try{
			SyncDBHelper syncDBHelper = new SyncDBHelper(this);
			SQLiteDatabase syncDB = syncDBHelper.getWritableDatabase();
			
			syncDB.execSQL("INSERT INTO my_plants VALUES(" +
					"null," +
					speciesid + "," +
					siteid + "," +
					"'" + sitename + "',"+
					protocol_id + "," +
					//"1,"+ // 1 means it's official, from add plant list
					"'" + speciesname + "'," +
					"1, " +
					SyncDBHelper.SYNCED_NO + ");"
					);
			
			if(speciesid == 999) {
				SharedPreferences.Editor edit = pref.edit();				
				edit.putInt("other_species_id", s_id);
				edit.commit();
			}
			
			syncDBHelper.close();
			return true;
		}
		catch(Exception e){
			Log.e(TAG,e.toString());
			return false;
		}
	}
	
	public HashMap<String, Integer> getUserSiteIDMap(){

		HashMap<String, Integer> localMapUserSiteNameID = new HashMap<String, Integer>(); 
		
		//Open plant list db from static db
		SyncDBHelper syncDBHelper = new SyncDBHelper(this);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase(); 
		
		//Rereive syncDB and add them to arUserPlatList arraylist
		Cursor cursor = syncDB.rawQuery("SELECT site_name, site_id FROM my_sites;",null);
		while(cursor.moveToNext()){
			localMapUserSiteNameID.put(cursor.getString(0), cursor.getInt(1));
		}
		localMapUserSiteNameID.put("Add New Site", 10000);
		
		
		//Close DB and cursor
		cursor.close();
		//syncDB.close();
		syncDBHelper.close();
	
		return localMapUserSiteNameID;
	}
}

class PlantItem{
	PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID){
		Picture = aPicture;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		SpeciesID = aSpeciesID;
	}

	PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int aSiteID){
		Picture = aPicture;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		SpeciesID = aSpeciesID;
		siteID = aSiteID;
	}
	
	PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int aSiteID, int aProtocolID, int aPheno_done, int aTotal_pheno, boolean aTopItem, String aSiteName, boolean aMonitor){
		Picture = aPicture;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		Site = aSiteName;
		SpeciesID = aSpeciesID;
		siteID = aSiteID;
		protocolID = aProtocolID;
		current_pheno = aPheno_done;
		total_pheno = aTotal_pheno;
		TopItem = aTopItem;
		Monitor = aMonitor;
	}
	
	int Picture;
	String CommonName;
	String SpeciesName;
	int SpeciesID;
	int siteID;
	int protocolID;
	int current_pheno;
	int total_pheno;
	String Site;
	boolean TopItem;
	boolean Monitor;
}
