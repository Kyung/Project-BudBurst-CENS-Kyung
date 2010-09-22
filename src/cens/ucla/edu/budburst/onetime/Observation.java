package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;

import cens.ucla.edu.budburst.PlantInfo;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Observation extends ListActivity {
	private ArrayList<PlantItem> arPlantList;
	private int WILD_FLOWERS = 0;
	private int GRASSES = 1;
	private int DECIDUOUS_TREES = 2;
	private int EVERGREEN_TREES = 3;
	private int CONIFERS = 4;
	private int category;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.observation);
	    
	    Intent intent = getIntent();
	    String selectedItem = intent.getExtras().getString("SelectedList");
	    
	    Log.i("K", "selected : " + selectedItem);
	    
	    if(selectedItem.equals("Wildflowers and Herbs")) {
	    	category = WILD_FLOWERS;
	    }
	    else if(selectedItem.equals("Grass")) {
	    	category = GRASSES;
	    }
	    else if(selectedItem.equals("Deciduous Trees and Shrubs")) {
	    	category = DECIDUOUS_TREES;
	    }
	    else if(selectedItem.equals("Evergreen Trees and Shrubs")) {
	    	category = EVERGREEN_TREES;
	    }
	    else if(selectedItem.equals("Conifer")) {
	    	category = CONIFERS;
	    }
	    
	    Log.i("K", "CATEGORY : " + category);
    
		//Get all plant list
		//Open plant list db from static db
		StaticDBHelper staticDBHelper = new StaticDBHelper(this);
		SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase(); 
		
		
		//Rereive syncDB and add them to arUserPlatList arraylist
		arPlantList = new ArrayList<PlantItem>();
		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE category=" + category + " ORDER BY common_name;",null);
		while(cursor.moveToNext()){
			Integer id = cursor.getInt(0);
			String species_name = cursor.getString(1);
			String common_name = cursor.getString(2);
			Integer protocol_id = cursor.getInt(3);
						
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+id, null, null);
			
			PlantItem pi;
			//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
			pi = new PlantItem(resID, common_name, species_name, protocol_id);
			arPlantList.add(pi);
		}
		
		MyListAdapter mylistapdater = new MyListAdapter(this, R.layout.plantlist_item, arPlantList);
		ListView MyList = getListView(); 
		MyList.setAdapter(mylistapdater);
		
		//Close DB and cursor
		cursor.close();
		staticDB.close();
		staticDBHelper.close();

	    // TODO Auto-generated method stub
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		Intent intent = new Intent(Observation.this, GetSpeciesInfo.class);
		intent.putExtra("cname", arPlantList.get(position).CommonName);
		intent.putExtra("sname", arPlantList.get(position).SpeciesName);
		intent.putExtra("protocol_id", arPlantList.get(position).protocolID);
		startActivity(intent);
	}
	
	class PlantItem{	
		PlantItem(int aPicture, String aCommonName, String aSpeciesName, int aProtocolID){
			Picture = aPicture;
			CommonName = aCommonName;
			SpeciesName = aSpeciesName;
			protocolID = aProtocolID;
		}
		
		int Picture;
		String CommonName;
		String SpeciesName;
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
				intent = new Intent(Observation.this, Queue.class);
				startActivity(intent);
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
}
