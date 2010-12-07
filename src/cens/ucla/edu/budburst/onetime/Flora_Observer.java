package cens.ucla.edu.budburst.onetime;

import java.io.File;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.PlantInformation;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;


public class Flora_Observer extends ListActivity{
	private final String TAG = "Flora_Observer";
	private ArrayList<PlantItem> arPlantList;

	private int WILD_FLOWERS = 0;
	private int GRASSES = 1;
	private int DECIDUOUS_TREES = 2;
	private int EVERGREEN_TREES = 3;
	private int CONIFERS = 4;
	
	private StaticDBHelper staticDBHelper = null;
	private SQLiteDatabase staticDB = null;
	private MyListAdapter mylistapdater = null;
	private ListView MyList = null;
	
	private RadioButton rb1 = null;
	private RadioButton rb2 = null;
	private RadioButton rb3 = null;
	
	//private TextView header = null;
	private TextView myTitleText = null;
	
	private int current_position = 0;
	private String camera_image_id = null;
	private double latitude = 0.0;
	private double longitude = 0.0;
	private String dt_taken = null;
	
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
		
		Log.i("K", "camera_image_id : " + camera_image_id);
		
		
		rb1 = (RadioButton)findViewById(R.id.option1);
		rb2 = (RadioButton)findViewById(R.id.option2);
		rb3 = (RadioButton)findViewById(R.id.option3);
		
		rb1.setOnClickListener(radio_listener);
		rb2.setOnClickListener(radio_listener);
		rb3.setOnClickListener(radio_listener);
		
		rb1.setSelected(true);
		
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
		PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown Plant", "Unknown Plant", 999);
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
				PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown Plant", "Unknown Plant", 999);
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
				PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown Plant", "Unknown Plant", 999);
				arPlantList.add(pi);
				
				mylistapdater = new MyListAdapter(Flora_Observer.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
			}
			else {
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
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + WILD_FLOWERS + " ORDER BY common_name;",null);
							myTitleText.setText(" One Time Observation > Flowers");
						}
						else if(category[which].equals("Grass")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + GRASSES + " ORDER BY common_name;",null);
							myTitleText.setText(" One Time Observation > Grass");
						}
						else if(category[which].equals("Deciduous Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + DECIDUOUS_TREES + " ORDER BY common_name;",null);
							myTitleText.setText(" One Time Observation > Deciduous");
						}
						else if(category[which].equals("Evergreen Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + EVERGREEN_TREES + " ORDER BY common_name;",null);
							myTitleText.setText(" One Time Observation > Evergreen");
						}
						else if(category[which].equals("Conifer")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + CONIFERS + " ORDER BY common_name;",null);
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
						PlantItem pi = new PlantItem(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null), "Unknown Plant", "Unknown Plant", 999);
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
			
			staticDBHelper.close();
		}
	};
	
	/////////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0,1,0,"Queue").setIcon(android.R.drawable.ic_menu_sort_by_size);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case 1:
				intent = new Intent(Flora_Observer.this, Queue.class);
				startActivity(intent);
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){

		Intent intent = new Intent(Flora_Observer.this, GetPhenophase.class);
		intent.putExtra("cname", arPlantList.get(position).CommonName);
		intent.putExtra("sname", arPlantList.get(position).SpeciesName);
		intent.putExtra("dt_taken", dt_taken);
		intent.putExtra("protocol_id", arPlantList.get(position).protocolID);
		intent.putExtra("species_id", arPlantList.get(position).SpeciesID);
		intent.putExtra("camera_image_id", camera_image_id);
		intent.putExtra("latitude", latitude);
		intent.putExtra("longitude", longitude);
		
		startActivity(intent);
	}
}

