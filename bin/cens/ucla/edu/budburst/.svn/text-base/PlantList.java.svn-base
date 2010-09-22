package cens.ucla.edu.budburst;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.Helloscr.UpdateThread;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.OneTimeMain;

public class PlantList extends ListActivity {
	
	final private String TAG = "PlantList"; 
	
	private String username;
	private String password;
	private SharedPreferences pref;
	private SyncDBHelper syncDBHelper;
	private StaticDBHelper staticDBHelper;
	private ListView MyList;
	private ArrayList<PlantItem> user_species_list;
	
	//MENU contants
	final private int MENU_ADD_PLANT = 1;
	final private int MENU_ADD_SITE = 2;
	final private int MENU_LOGOUT = 3;
	final private int MENU_SYNC = 4;
	
	ArrayList<PlantItem> arPlantItem;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plantlist);

	}
	
	public void onResume(){
		super.onResume();
		MyList = getListView();
		
		//Initiate ArrayList
		user_species_list = new ArrayList<PlantItem>();
		
		//Retrieve username and password
		pref = getSharedPreferences("userinfo",0);
		username = pref.getString("Username","");
		password = pref.getString("Password","");
		
		//My plant button
		Button buttonMyplant = (Button)findViewById(R.id.myplant);
		buttonMyplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {}
		});
		
		//Shared plant button
		Button buttonSharedplant = (Button)findViewById(R.id.sharedplant);
		buttonSharedplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(PlantList.this, OneTimeMain.class);
				startActivity(intent);
			}
		});
		buttonMyplant.setSelected(true);
		
		arPlantItem = new ArrayList<PlantItem>();
		syncDBHelper = new SyncDBHelper(PlantList.this);
		SQLiteDatabase syncDB  = syncDBHelper.getReadableDatabase();
		
		staticDBHelper = new StaticDBHelper(PlantList.this);
        try {
        	staticDBHelper.createDataBase();
	 	} catch (IOException ioe) {
	 		Log.e(TAG, ioe.toString());
	 		throw new Error("Unable to create database");
	 	}
 
	 	try {
	 		staticDBHelper.openDataBase();
	 	}catch(SQLException sqle){
	 		Log.e(TAG, sqle.toString());
	 		throw sqle;
	 	}
		SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();

		ArrayList<String> user_station_name = new ArrayList<String>();
		ArrayList<Integer> user_station_id = new ArrayList<Integer>();
		Cursor cursor;

		try{

			//Check user site is empty.			
			cursor = syncDB.rawQuery("SELECT site_id FROM my_sites;", null);
			Log.d(TAG, String.valueOf(cursor.getCount()));
			if(cursor.getCount() == 0)
			{
				cursor.close();
//				Intent intent = new Intent(PlantList.this, AddSite.class);
//				startActivity(intent);
//				finish();
				
				TextView instruction = (TextView)findViewById(R.id.instruction);
				instruction.setVisibility(View.VISIBLE);
				MyList.setVisibility(View.GONE); 
				return;
			}else{
				cursor.close();
			}
			
			//Check user plant is empty.			
			cursor = syncDB.rawQuery("SELECT site_id FROM my_plants;", null);
			Log.d(TAG, String.valueOf(cursor.getCount()));
			if(cursor.getCount() == 0)
			{
				cursor.close();
//				Intent intent = new Intent(PlantList.this, AddSite.class);
//				startActivity(intent);
//				finish();
				
				TextView instruction = (TextView)findViewById(R.id.instruction);
				instruction.setVisibility(View.VISIBLE);
				MyList.setVisibility(View.GONE); 
				return;
			}else{
				cursor.close();
			}			
			
			
			//Retreive site name and site id from my_plant table to draw plant list.
			cursor = syncDB.rawQuery("SELECT site_name, site_id FROM my_plants GROUP BY site_name;",null);

			while(cursor.moveToNext()){
				user_station_name.add(cursor.getString(0));
				user_station_id.add(cursor.getInt(1));
			}
			cursor.close();
			
			arPlantItem.clear();
			for(int i=0; i<user_station_name.size(); i++){
				
				PlantItem pi;
				
				//Retrieves plants from each site.
				Cursor cursor2 = syncDB.rawQuery("SELECT species_id FROM my_plants " +
						"WHERE site_name = '" + user_station_name.get(i) + "';",null);

				while(cursor2.moveToNext()){
					String qry = "SELECT _id, species_name, common_name, protocol_id FROM species WHERE _id = " + cursor2.getInt(0) + ";";
					Cursor cursor3 = staticDB.rawQuery(qry, null);
					
					cursor3.moveToNext();
					int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor3.getString(0), null, null);


					//PlantItem structure = >int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int siteID)
					pi = new PlantItem(resID, cursor3.getString(2), cursor3.getString(1)+" (" + user_station_name.get(i) + ")"
							, cursor3.getInt(0), user_station_id.get(i), cursor3.getInt(3));
					arPlantItem.add(pi);
					cursor3.close();
				}
				cursor2.close();
			}
			//To synchronize user_species_list with actual listview contents order.
			MyListAdapter mylistapdater = new MyListAdapter(this, R.layout.plantlist_item, arPlantItem);
			MyList.setAdapter(mylistapdater);
		}catch(Exception e){
			Log.e(TAG, e.toString());
		}
		finally{
			staticDBHelper.close();
			syncDBHelper.close();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		Intent intent = new Intent(this, PlantInfo.class);
		intent.putExtra("species_id", arPlantItem.get(position).SpeciesID);
		intent.putExtra("site_id", arPlantItem.get(position).siteID);
		intent.putExtra("protocol_id", arPlantItem.get(position).protocolID);
		startActivity(intent);
	}
		
	/////////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		SubMenu addButton = menu.addSubMenu("Add")
			.setIcon(android.R.drawable.ic_menu_add);
		addButton.add(0,MENU_ADD_SITE,0,"Add Site");
		addButton.add(0,MENU_ADD_PLANT,0,"Add Plant");
				
		
		menu.add(0,MENU_SYNC,0,"Sync").setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0,MENU_LOGOUT,0,"Log out").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case MENU_ADD_PLANT:
				intent = new Intent(PlantList.this, AddPlant.class);
				startActivity(intent);
				finish();
				return true;
			case MENU_ADD_SITE:
				intent = new Intent (PlantList.this, AddSite.class);
				startActivity(intent);
				//finish();
				return true;
			case MENU_SYNC:
				intent = new Intent(PlantList.this, Sync.class);
				intent.putExtra("sync_instantly", true);
				startActivity(intent);
				finish();
				return true;
			case MENU_LOGOUT:
				new AlertDialog.Builder(PlantList.this)
					.setTitle("Question")
					.setMessage("You might lose your unsynced data if you log out. Do you want to log out?")
					.setPositiveButton("Yes",mClick)
					.setNegativeButton("no",mClick)
					.show();
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
	
	//Dialog confirm message if user clicks logout button
	DialogInterface.OnClickListener mClick =
		new DialogInterface.OnClickListener(){
		public void onClick(DialogInterface dialog, int whichButton){
			if(whichButton == DialogInterface.BUTTON1){
				
				SharedPreferences.Editor edit = pref.edit();				
				edit.putString("Username","");
				edit.putString("Password","");
				edit.putString("synced", "false");
				edit.commit();
				
				//Drop user table in database
				SyncDBHelper dbhelper = new SyncDBHelper(PlantList.this);
				dbhelper.clearAllTable(PlantList.this);
				dbhelper.close();
				
				Intent intent = new Intent(PlantList.this, Login.class);
				startActivity(intent);
				finish();
			}else{
			}
		}
	};
	//Menu option
	/////////////////////////////////////////////////////////////
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
	
	PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aSpeciesID, int aSiteID, int aProtocolID){
		Picture = aPicture;
		CommonName = aCommonName;
		SpeciesName = aSpeciesName;
		SpeciesID = aSpeciesID;
		siteID = aSiteID;
		protocolID = aProtocolID;
	}
	
	int Picture;
	String CommonName;
	String SpeciesName;
	int SpeciesID;
	int siteID;
	int protocolID;
}

//Adapters:MyListAdapter and SeparatedAdapter
class MyListAdapter extends BaseAdapter{
	Context maincon;
	LayoutInflater Inflater;
	ArrayList<PlantItem> arSrc;
	int layout;
	
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
		
		ImageView img = (ImageView)convertView.findViewById(R.id.icon);
		img.setImageResource(arSrc.get(position).Picture);
		
		TextView textname = (TextView)convertView.findViewById(R.id.commonname);
		textname.setText(arSrc.get(position).CommonName);
		
		TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
		textdesc.setText(arSrc.get(position).SpeciesName);
		
		return convertView;
	}
}


class SeparatedListAdapter extends BaseAdapter {

	public Map<String,Adapter> sections = new LinkedHashMap<String,Adapter>();
	public final ArrayAdapter<String> headers;
	public final static int TYPE_SECTION_HEADER = 0;

	public SeparatedListAdapter(Context context) {
		headers = new ArrayAdapter<String>(context, R.layout.list_section_header);
	}

	public void addSection(String section, Adapter adapter) {
		this.headers.add(section);
		this.sections.put(section, adapter);
		Log.d("SeparatedListAdapter",sections.toString());
	}

	public Object getItem(int position) {
		for(Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if(position == 0) return section;
			if(position < size) return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for(Adapter adapter : this.sections.values())
			total += adapter.getCount() + 1;
		return total;
	}

	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for(Adapter adapter : this.sections.values())
			total += adapter.getViewTypeCount();
		return total;
	}

	public int getItemViewType(int position) {
		int type = 1;
		for(Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if(position == 0) return TYPE_SECTION_HEADER;
			if(position < size) return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for(Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if(position == 0) return headers.getView(sectionnum, convertView, parent);
			if(position < size) return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

}
