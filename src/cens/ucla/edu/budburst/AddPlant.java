package cens.ucla.edu.budburst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;

public class AddPlant extends ListActivity{
	private final String TAG = "AddPlant";
	private ArrayList<PlantItem> arPlantList;
	
	private int mSelect = 0;
	private int WILD_FLOWERS = 0;
	private int GRASSES = 1;
	private int DECIDUOUS_TREES = 2;
	private int EVERGREEN_TREES = 3;
	private int CONIFERS = 4;
	private Integer new_plant_species_id;
	private Integer new_plant_site_id; 
	private String new_plant_site_name;
	
	private StaticDBHelper staticDBHelper = null;
	private SQLiteDatabase staticDB = null;
	private MyListAdapter mylistapdater = null;
	private ListView MyList = null;
	
	private RadioButton rb1 = null;
	private RadioButton rb2 = null;
	private RadioButton rb3 = null;
	
	//private TextView header = null;
	private TextView myTitleText = null;
	
	
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
		
		staticDBHelper = new StaticDBHelper(AddPlant.this);
		staticDB = staticDBHelper.getReadableDatabase();
		 
		arPlantList = new ArrayList<PlantItem>();
 		cursor = staticDB.rawQuery("SELECT _id, species_name, common_name FROM species ORDER BY common_name;",null);
 		while(cursor.moveToNext()){
			Integer id = cursor.getInt(0);
			if(id == 70 || id == 69 || id == 45 || id == 59 || id == 60 || id == 19 || id == 32 || id == 34 || id == 24) {
				String species_name = cursor.getString(1);
				String common_name = cursor.getString(2);
							
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
				
				PlantItem pi;
				//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
				pi = new PlantItem(resID, common_name, species_name, id);
				arPlantList.add(pi);
			}
		}
		
		mylistapdater = new MyListAdapter(AddPlant.this, R.layout.plantlist_item2, arPlantList);
		MyList = getListView(); 
		MyList.setAdapter(mylistapdater);
		
		//Close DB and cursor
		staticDB.close();
		cursor.close();

		//Get User site name and id using Map.
		mapUserSiteNameID = getUserSiteIDMap();
		Iterator<String> itr = mapUserSiteNameID.keySet().iterator();
		while(itr.hasNext()){
			Log.d(TAG, itr.next());
		}
	}
	
	private OnClickListener radio_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			staticDBHelper = new StaticDBHelper(AddPlant.this);
			staticDB = staticDBHelper.getReadableDatabase();
			
			if(v == rb1) {
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
						//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
						pi = new PlantItem(resID, common_name, species_name, id);
						arPlantList.add(pi);
					}
				}
				
				mylistapdater = new MyListAdapter(AddPlant.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
				
			}
			else if (v == rb2) {
				myTitleText.setText(getString(R.string.AddPlant_all));
				//header.setText("'ALL' list of the plants.");
				//Rereive syncDB and add them to arUserPlatList arraylist
				arPlantList = new ArrayList<PlantItem>();
		 		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name FROM species ORDER BY common_name;", null);
				while(cursor.moveToNext()){
					Integer id = cursor.getInt(0);
				
					String species_name = cursor.getString(1);
					String common_name = cursor.getString(2);
									
					int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
						
					PlantItem pi;
					//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
					pi = new PlantItem(resID, common_name, species_name, id);
					arPlantList.add(pi);
				}
				
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
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + WILD_FLOWERS + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addFlowers));
						}
						else if(category[which].equals("Grass")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + GRASSES + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addGrass));
						}
						else if(category[which].equals("Deciduous Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + DECIDUOUS_TREES + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addDecid));
						}
						else if(category[which].equals("Evergreen Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + EVERGREEN_TREES + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addEvergreen));
						}
						else if(category[which].equals("Conifer")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + CONIFERS + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addConifer));
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
			
			staticDBHelper.close();
		}
	};
	
	public String[] getUserSite(){
		
		SyncDBHelper syncDBHelper = new SyncDBHelper(this);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase();
		String[] arrUsersite;
		try{
			Cursor cursor = syncDB.rawQuery("SELECT site_name FROM my_sites;", null);
			
			arrUsersite = new String[cursor.getCount()];
			int i=0;
			while(cursor.moveToNext()){
				arrUsersite[i++] = cursor.getString(0);
			}
			cursor.close();
			return arrUsersite;
		}
		catch(Exception e){
			Log.e(TAG,e.toString());
			return null;
		}
		finally{
			syncDBHelper.close();
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		new_plant_species_id = arPlantList.get(position).SpeciesID;
		
		Log.i("K", "POSITION : " + position);
		Log.i("K", "SPECIES_ID : " + arPlantList.get(position).SpeciesID);
		
		//Retreive user sites from data base.
		//seqUserSite = mapUserSiteNameID.keySet().toArray(new String[0]);
		seqUserSite = getUserSite();
		
		//Pop up choose site dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setSingleChoiceItems(seqUserSite, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mSelect = which;				
				new_plant_site_id = mapUserSiteNameID.get(seqUserSite[mSelect].toString());
				new_plant_site_name = seqUserSite[mSelect].toString();
			}
		})
		.setPositiveButton(getString(R.string.Button_select), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(new_plant_site_id == null){
					Toast.makeText(AddPlant.this, getString(R.string.AddPlant_pleaseSelectSite), Toast.LENGTH_SHORT).show();
				}
				else{
					if(checkIfNewPlantAlreadyExists(new_plant_species_id, new_plant_site_id)){
						Toast.makeText(AddPlant.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{
						if(insertNewPlantToDB(new_plant_species_id, new_plant_site_id, new_plant_site_name)){
							Intent intent = new Intent(AddPlant.this, PlantList.class);
							Toast.makeText(AddPlant.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(AddPlant.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
	
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
	
	
	
	public boolean checkIfNewPlantAlreadyExists(int speciesid, int siteid){

		SyncDBHelper syncDBHelper = new SyncDBHelper(this);
		SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase();
		
		try{
			Cursor cursor = syncDB.rawQuery("SELECT species_id FROM my_plants " +
					"WHERE species_id = " + speciesid +
					" AND site_id = " + siteid, null);
			if(cursor.getCount() > 0){
				cursor.close();
				return true;
			}
			else{
				cursor.close();
				return false;
			}
		}
		catch(Exception e){
			Log.e(TAG,e.toString());
			return false;
		}
		finally{
			syncDBHelper.close();
		}
	}
	
	public boolean insertNewPlantToDB(int speciesid, int siteid, String sitename){
		try{
			SyncDBHelper syncDBHelper = new SyncDBHelper(this);
			SQLiteDatabase syncDB = syncDBHelper.getWritableDatabase();
			
			syncDB.execSQL("INSERT INTO my_plants VALUES(" +
					"null," +
					speciesid + "," +
					siteid + "," +
					"'" + sitename + "',"+
					"0,"+
					SyncDBHelper.SYNCED_NO + ");"
					);
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
		
		
		//Close DB and cursor
		cursor.close();
		//syncDB.close();
		syncDBHelper.close();
	
		return localMapUserSiteNameID;
	}
}

