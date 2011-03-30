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
import cens.ucla.edu.budburst.adapter.MyListAdapter;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.Values;

public class AddPlant extends ListActivity{
	private final String TAG = "AddPlant";
	private ArrayList<PlantItem> arPlantList;
	
	private int mSelect = 0;
	private Integer new_plant_species_id;
	private String new_plant_species_name;
	private Integer new_plant_site_id; 
	private String new_plant_site_name;
	private Integer protocol_id;
	
	private StaticDBHelper staticDBHelper = null;
	private SQLiteDatabase staticDB = null;
	private MyListAdapter mylistapdater = null;
	private ListView MyList = null;
	
	private Button rb1 = null;
	private Button rb2 = null;
	private Button rb3 = null;
	private Button rb4 = null;
	
	private EditText et1;
	
	//private TextView header = null;
	private TextView myTitleText = null;
	private Dialog Name_dialog = null;
	
	FunctionsHelper helper = null;
	CharSequence[] seqUserSite;
	private boolean selectCategory = false;
	
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>(); 
		
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.flora_observer);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  " + getString(R.string.AddPlant_top10));
		
		helper = new FunctionsHelper();
		
		rb1 = (Button)findViewById(R.id.option1);
		rb2 = (Button)findViewById(R.id.option2);
		rb3 = (Button)findViewById(R.id.option3);
		rb4 = (Button)findViewById(R.id.option4);
		
		rb1.setOnClickListener(radio_listener);
		rb2.setOnClickListener(radio_listener);
		rb3.setOnClickListener(radio_listener);
		rb4.setOnClickListener(radio_listener);
	
		
		// show the top 10 lists first
		top10List();

		//Get User site name and id using Map.
		FunctionsHelper helper = new FunctionsHelper();
		mapUserSiteNameID = helper.getUserSiteIDMap(AddPlant.this);
		Iterator<String> itr = mapUserSiteNameID.keySet().iterator();
		while(itr.hasNext()){
			Log.d(TAG, itr.next());
		}
	}
	
	private void top10List() {
		staticDBHelper = new StaticDBHelper(AddPlant.this);
		staticDB = staticDBHelper.getReadableDatabase();
		
		myTitleText.setText(" " + getString(R.string.AddPlant_top10));
		//header.setText("'TOP 10' list of the plants.");
		arPlantList = new ArrayList<PlantItem>();
	 	Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species ORDER BY common_name;", null);
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
		PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown/Other", "Unknown/Other", Values.UNKNOWN_SPECIES);
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
				selectCategory = false;
				top10List();
			}
			else if (v == rb2) {
				selectCategory = false;
				myTitleText.setText(" " + getString(R.string.AddPlant_all));
				//header.setText("'ALL' list of the plants.");
				//Rereive syncDB and add them to arUserPlatList arraylist
				arPlantList = new ArrayList<PlantItem>();
		 		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name FROM species ORDER BY common_name;", null);
				while(cursor.moveToNext()){
					Integer id = cursor.getInt(0);
					
					// if id is 999, skip that.
					if(id == Values.UNKNOWN_SPECIES) 
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
				PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown/Other", "Unknown/Other", Values.UNKNOWN_SPECIES);
				arPlantList.add(pi);
				
				mylistapdater = new MyListAdapter(AddPlant.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
			}
			else if(v == rb3){
				selectCategory = true;
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
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.WILD_FLOWERS + " ORDER BY common_name;",null);
							myTitleText.setText(" " + getString(R.string.AddPlant_addFlowers));
							protocol_id = Values.WILD_FLOWERS;
						}
						else if(category[which].equals("Grass")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.GRASSES + " ORDER BY common_name;",null);
							myTitleText.setText(" " + getString(R.string.AddPlant_addGrass));
							protocol_id = Values.GRASSES;
						}
						else if(category[which].equals("Deciduous Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.DECIDUOUS_TREES + " ORDER BY common_name;",null);
							myTitleText.setText(" " + getString(R.string.AddPlant_addDecid));
							protocol_id = Values.DECIDUOUS_TREES;
						}
						else if(category[which].equals("Evergreen Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.EVERGREEN_TREES + " ORDER BY common_name;",null);
							myTitleText.setText(" " + getString(R.string.AddPlant_addEvergreen));
							protocol_id = Values.EVERGREEN_TREES;
						}
						else if(category[which].equals("Conifer")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.CONIFERS + " ORDER BY common_name;",null);
							myTitleText.setText(" " + getString(R.string.AddPlant_addConifer));
							protocol_id = Values.CONIFERS;
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
						
						PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown/Other", "Unknown/Other", Values.UNKNOWN_SPECIES);
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
			else {
				selectCategory = false;
				myTitleText.setText(" " + getString(R.string.AddPlant_local));
				
				arPlantList = new ArrayList<PlantItem>();
				
				OneTimeDBHelper otDBH = new OneTimeDBHelper(AddPlant.this);
				SQLiteDatabase otDB = otDBH.getReadableDatabase();
				Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name FROM species ORDER BY common_name;", null);
				
				while(cursor.moveToNext()) {
					String sName = cursor.getString(1);
					
					Cursor cursor2 = otDB.rawQuery("SELECT science_name FROM localPlantLists WHERE category=1 AND science_name=\"" + sName + "\"", null);
					if(cursor2.getCount() > 0) {
						int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor.getInt(0), null, null);
						
						String species_name = cursor.getString(1);
						String common_name = cursor.getString(2);
						
						PlantItem pi;
						//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
						pi = new PlantItem(resID, common_name, species_name, cursor.getInt(0));
						arPlantList.add(pi);
					}
					
					cursor2.close();
				}
				
				otDBH.close();
				otDB.close();
								
				mylistapdater = new MyListAdapter(AddPlant.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
			}
			
			staticDB.close();
			staticDBHelper.close();
		}
	};
	

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		new_plant_species_id = arPlantList.get(position).SpeciesID;
		new_plant_species_name = arPlantList.get(position).CommonName;

		//Retreive user sites from database.
		seqUserSite = helper.getUserSite(AddPlant.this);
		
		if(new_plant_species_id == Values.UNKNOWN_SPECIES) {
			newDialog();
		}
		else {
			staticDBHelper = new StaticDBHelper(AddPlant.this);
			SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
			Cursor c = staticDB.rawQuery("SELECT protocol_id FROM species WHERE _id = " + new_plant_species_id, null);
			while(c.moveToNext()) {
				protocol_id = c.getInt(0);
			}
			c.close();
			staticDB.close();
			popupDialog();
		}
	}
	
	private void plantCategory() {
		
		new AlertDialog.Builder(AddPlant.this)
		.setTitle(getString(R.string.AddPlant_SelectCategory))
		.setIcon(android.R.drawable.ic_menu_more)
		.setCancelable(true)
		.setItems(R.array.category, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String[] category = getResources().getStringArray(R.array.category);
				
				if(category[which].equals("Wild Flowers and Herbs")) {
					protocol_id = Values.WILD_FLOWERS;
				}
				else if(category[which].equals("Grass")) {
					protocol_id = Values.GRASSES;
				}
				else if(category[which].equals("Deciduous Trees and Shrubs")) {
					protocol_id = Values.DECIDUOUS_TREES;
				}
				else if(category[which].equals("Evergreen Trees and Shrubs")) {
					protocol_id = Values.EVERGREEN_TREES;
				}
				else if(category[which].equals("Conifer")) {
					protocol_id = Values.CONIFERS;
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
					intent.putExtra("cname", new_plant_species_name);
					intent.putExtra("protocol_id", protocol_id);
					intent.putExtra("from", Values.FROM_PLANT_LIST);
					startActivity(intent);
				}
				else {
					if(helper.checkIfNewPlantAlreadyExists(new_plant_species_id, new_plant_site_id, AddPlant.this)){
						Toast.makeText(AddPlant.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{

						if(helper.insertNewMyPlantToDB(AddPlant.this, new_plant_species_id, new_plant_species_name, new_plant_site_id, new_plant_site_name, protocol_id)){
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
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
	
	private void newDialog() {
		Name_dialog = new Dialog(AddPlant.this);
		
		Name_dialog.setContentView(R.layout.species_name_custom_dialog);
		Name_dialog.setTitle(getString(R.string.GetPhenophase_PBB_message));
		Name_dialog.setCancelable(true);
		Name_dialog.show();
		
		et1 = (EditText)Name_dialog.findViewById(R.id.custom_common_name);
		Button doneBtn = (Button)Name_dialog.findViewById(R.id.custom_done);
		
		doneBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String common_name = et1.getText().toString();
				
				if(common_name.equals("")) {
					common_name = "Unknown/Other";
				}
				new_plant_species_name = common_name;
				if(selectCategory) {
					popupDialog();
				}
				else {
					plantCategory();
				}
				Name_dialog.dismiss();
			}
		});
		
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	staticDB.close();
	    }
	    return super.onKeyDown(keyCode, event);
	}
}