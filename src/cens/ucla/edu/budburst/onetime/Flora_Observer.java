package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import cens.ucla.edu.budburst.AddPlant;
import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.MyListAdapter;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;

public class Flora_Observer extends ListActivity{
	private final String TAG = "Flora_Observer";
	private ArrayList<PlantItem> arPlantList;
	
	private StaticDBHelper staticDBHelper = null;
	private SQLiteDatabase staticDB = null;
	private MyListAdapter mylistapdater = null;
	private ListView MyList = null;
	
	private Button rb1 = null;
	private Button rb2 = null;
	private Button rb3 = null;
	private Button rb4 = null;
	private EditText et1 = null;
	private Dialog dialog = null;
	
	//private TextView header = null;
	private TextView myTitleText = null;
	
	private int current_position = 0;
	private int pheno_id = 0;
	private double latitude = 0.0;
	private double longitude = 0.0;
	private String camera_image_id = null;
	private String dt_taken = null;
	private String notes = "";
	private String cname = "Unknown/Other";
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.flora_observer);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" One Time Observation > Top 10");
		
		Intent p_intent = getIntent();
		
		camera_image_id = p_intent.getExtras().getString("camera_image_id");
		latitude = p_intent.getExtras().getDouble("latitude");
		longitude = p_intent.getExtras().getDouble("longitude");
		dt_taken = p_intent.getExtras().getString("dt_taken");
		pheno_id = p_intent.getExtras().getInt("pheno_id");
		notes = p_intent.getExtras().getString("notes");
		
		Log.i("K", "Flora_OBSERVER = camera_image_id : " + camera_image_id + " , pheno_id : " + pheno_id);
		
		rb1 = (Button)findViewById(R.id.option1);
		rb2 = (Button)findViewById(R.id.option2);
		rb3 = (Button)findViewById(R.id.option3);
		rb4 = (Button)findViewById(R.id.option4);
		
		rb1.setOnClickListener(radio_listener);
		rb2.setOnClickListener(radio_listener);
		rb3.setOnClickListener(radio_listener);
		rb4.setOnClickListener(radio_listener);
		
		//rb1.setSelected(true);
		
		//Check if site table is empty
		staticDBHelper = new StaticDBHelper(Flora_Observer.this);
		staticDB = staticDBHelper.getReadableDatabase();
		 
		arPlantList = new ArrayList<PlantItem>();
 		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species ORDER BY common_name;",null);
 		while(cursor.moveToNext()){
			Integer id = cursor.getInt(0);
			if(id == 70 || id == 69 || id == 45 || id == 59 || id == 60 || id == 19 || id == 32 || id == 34 || id == 24) {
				String species_name = cursor.getString(1);
				String common_name = cursor.getString(2);
				int protocol_id = cursor.getInt(3);
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
				
				PlantItem pi;
				//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
				pi = new PlantItem(resID, common_name, species_name, id, protocol_id);
				arPlantList.add(pi);
			}
		}
 		
		// add plant at the last.
		PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown/Other", "Unknown/Other", 999);
		arPlantList.add(pi);
		
		mylistapdater = new MyListAdapter(Flora_Observer.this, R.layout.plantlist_item2, arPlantList);
		MyList = getListView(); 
		MyList.setAdapter(mylistapdater);
		
		//Close DB and cursor
		staticDB.close();
		cursor.close();
		
	}

	private OnClickListener radio_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			staticDBHelper = new StaticDBHelper(Flora_Observer.this);
			staticDB = staticDBHelper.getReadableDatabase();
			
			if(v == rb1) {
				//header.setText("'TOP 10' list of the plants.");
				myTitleText.setText(" One Time Observation > Top 10");
				arPlantList = new ArrayList<PlantItem>();
		 		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species ORDER BY common_name;", null);
				while(cursor.moveToNext()){
					Integer id = cursor.getInt(0);
					if(id == 70 || id == 69 || id == 45 || id == 59 || id == 60 || id == 19 || id == 32 || id == 34 || id == 24) {
						String species_name = cursor.getString(1);
						String common_name = cursor.getString(2);
						int protocol_id = cursor.getInt(3);
									
						int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
						
						PlantItem pi;
						//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
						pi = new PlantItem(resID, common_name, species_name, id, protocol_id);
						arPlantList.add(pi);
					}
				}
				
				// add plant at the last.
				PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown/Other", "Unknown/Other", 999);
				arPlantList.add(pi);
				
				mylistapdater = new MyListAdapter(Flora_Observer.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
				
			}
			else if (v == rb2) {
				//header.setText("'ALL' list of the plants.");
				myTitleText.setText(" One Time Observation > All");
				//Rereive syncDB and add them to arUserPlatList arraylist
				arPlantList = new ArrayList<PlantItem>();
		 		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species ORDER BY common_name;", null);
				while(cursor.moveToNext()){
					Integer id = cursor.getInt(0);
				
					String species_name = cursor.getString(1);
					String common_name = cursor.getString(2);
					int protocol_id = cursor.getInt(3);
									
					int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
						
					PlantItem pi;
					//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
					pi = new PlantItem(resID, common_name, species_name, id, protocol_id);
					arPlantList.add(pi);
				}
				
				// add plant at the last.
				PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown/Other", "Unknown/Other", 999);
				arPlantList.add(pi);
				
				mylistapdater = new MyListAdapter(Flora_Observer.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
			}
			else if(v == rb3){
				//header.setText("By Group.");
				new AlertDialog.Builder(Flora_Observer.this)
				.setTitle("Select Category")
				.setIcon(android.R.drawable.ic_menu_more)
				.setItems(R.array.category, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] category = getResources().getStringArray(R.array.category);
						StaticDBHelper staticDBHelper = new StaticDBHelper(Flora_Observer.this);
						SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase(); 
						
						arPlantList = new ArrayList<PlantItem>();
						Cursor cursor = null;

						if(category[which].equals("Wild Flowers and Herbs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.WILD_FLOWERS + " ORDER BY common_name;",null);
							myTitleText.setText(" One Time Observation > Flowers");
						}
						else if(category[which].equals("Grass")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.GRASSES + " ORDER BY common_name;",null);
							myTitleText.setText(" One Time Observation > Grass");
						}
						else if(category[which].equals("Deciduous Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.DECIDUOUS_TREES + " ORDER BY common_name;",null);
							myTitleText.setText(" One Time Observation > Deciduous");
						}
						else if(category[which].equals("Evergreen Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.EVERGREEN_TREES + " ORDER BY common_name;",null);
							myTitleText.setText(" One Time Observation > Evergreen");
						}
						else if(category[which].equals("Conifer")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.CONIFERS + " ORDER BY common_name;",null);
							myTitleText.setText(" One Time Observation > Conifer");
						}
						else {
						}
						
						//header.setText(" " + category[which]);
						while(cursor.moveToNext()){
							Integer id = cursor.getInt(0);
							String species_name = cursor.getString(1);
							String common_name = cursor.getString(2);
							Integer protocol_id = cursor.getInt(3);
										
							PlantItem pi;
							
							int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
							
							//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
							pi = new PlantItem(resID, common_name, species_name, id, protocol_id);
							arPlantList.add(pi);
						}
						
						// add plant at the last.
						PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown/Other", "Unknown/Other", 999);
						arPlantList.add(pi);
						
						mylistapdater = new MyListAdapter(Flora_Observer.this, R.layout.plantlist_item2, arPlantList);
						MyList = getListView(); 
						MyList.setAdapter(mylistapdater);
						
						cursor.close();
						staticDB.close();
						staticDBHelper.close();
						
					}
				})
				.setNegativeButton("Back", null)
				.show();
				
			}
			
			else {
				myTitleText.setText(" " + getString(R.string.AddPlant_local));
				
				arPlantList = new ArrayList<PlantItem>();
				
				OneTimeDBHelper otDBH = new OneTimeDBHelper(Flora_Observer.this);
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
								
				mylistapdater = new MyListAdapter(Flora_Observer.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
			}
			
			staticDBHelper.close();
		}
	};
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){

		current_position = position;
		
		if(arPlantList.get(position).SpeciesID == Values.UNKNOWN_SPECIES) {
			dialog = new Dialog(Flora_Observer.this);
			
			dialog.setContentView(R.layout.species_name_custom_dialog);
			dialog.setTitle(getString(R.string.GetPhenophase_PBB_message));
			dialog.setCancelable(true);
			dialog.show();
			
			et1 = (EditText)dialog.findViewById(R.id.custom_common_name);
			Button doneBtn = (Button)dialog.findViewById(R.id.custom_done);
			doneBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					cname = et1.getText().toString();
					if(cname.equals("")) {
						cname = "Unknown/Other";
					}
					Intent intent = new Intent(Flora_Observer.this, AddNotes.class);
					
					intent.putExtra("cname", cname);
					intent.putExtra("sname", "Unknown/Other");
					intent.putExtra("protocol_id", arPlantList.get(current_position).ProtocolID);
					intent.putExtra("pheno_id", pheno_id);
					intent.putExtra("species_id", Values.UNKNOWN_SPECIES);
					intent.putExtra("camera_image_id", camera_image_id);
					intent.putExtra("latitude", latitude);
					intent.putExtra("longitude", longitude);
					intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
					intent.putExtra("category", Values.BUDBURST_LIST);
					
					dialog.dismiss();
					
					startActivity(intent);
				}
			});
		}
		else {
			Intent intent = new Intent(Flora_Observer.this, AddNotes.class);
			
			intent.putExtra("cname", arPlantList.get(position).CommonName);
			intent.putExtra("sname", arPlantList.get(position).SpeciesName);
			intent.putExtra("protocol_id", arPlantList.get(position).ProtocolID);
			intent.putExtra("pheno_id", pheno_id);
			intent.putExtra("species_id", arPlantList.get(position).SpeciesID);
			intent.putExtra("camera_image_id", camera_image_id);
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
			intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
			intent.putExtra("category", Values.BUDBURST_LIST);
		
			startActivity(intent);
		}
	}
}

