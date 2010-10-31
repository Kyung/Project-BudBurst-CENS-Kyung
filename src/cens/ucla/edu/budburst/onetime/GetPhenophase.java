package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GetPhenophase extends ListActivity {
	
	private ArrayList<PlantItem> pItem;
	private int protocol_id;
	private String cname = null;
	private String sname = null;
	private int species_id = 0;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.getphenophase);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.flora_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);
		
		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" Select Phenophase");
	    
	    Intent intent = getIntent();
	    protocol_id = intent.getExtras().getInt("protocol_id");
	    cname = intent.getExtras().getString("cname");
	    sname = intent.getExtras().getString("sname");
	    species_id = intent.getExtras().getInt("species_id");
	    
	    ImageView species_image = (ImageView) findViewById(R.id.species_image);
	    TextView species_name = (TextView) findViewById(R.id.species_name);
	    
	    species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s" + species_id, null, null));	    
	    species_image.setBackgroundResource(R.drawable.shapedrawable);
	    
	    
	    species_image.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				final LinearLayout linear = (LinearLayout) View.inflate(GetPhenophase.this, R.layout.image_popup, null);
				
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(GetPhenophase.this);
				ImageView image_view = (ImageView) linear.findViewById(R.id.image_btn);
				
				image_view.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+species_id, null, null));
			   
				TextView species_info = (TextView) linear.findViewById(R.id.species_info);
				species_info.setText(cname + "\n" + sname);
			    
			    // when press 'Back', close the dialog
				dialog.setPositiveButton("Back", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
					}
				});
		        dialog.setView(linear);
		        dialog.show();
			}
		});
	    
	    
	    
	    
	    species_name.setText(cname + " \n" + sname + " ");
	    
	    
	    
	    
	    pItem = new ArrayList<PlantItem>();
	    PlantItem pi;
	    
	    ArrayList<Integer> id = new ArrayList<Integer>();
	    ArrayList<Integer> phenophase = new ArrayList<Integer>();
	    ArrayList<String> description = new ArrayList<String>();
	    ArrayList<String> pheno_name = new ArrayList<String>();
	    
		StaticDBHelper sDBHelper = new StaticDBHelper(GetPhenophase.this);
		SQLiteDatabase sDB = sDBHelper.getReadableDatabase();
	    
		String query = null;
		if(cname.equals("Others")) {
			query = "SELECT _id, Phenophase_Icon, description, Phenophase_Name FROM Phenophase_Protocol_Icon GROUP BY Phenophase_Icon ORDER BY Phenophase_Icon ASC";
		}
		else {
			query = "SELECT _id, Phenophase_Icon, description, Phenophase_Name FROM Phenophase_Protocol_Icon WHERE Protocol_ID=" + protocol_id + " ORDER BY Phenophase_Icon ASC";
		}
	    
	    
	    Cursor cursor = sDB.rawQuery(query, null);
		
	    while(cursor.moveToNext()) {
	    	id.add(cursor.getInt(0));
	    	phenophase.add(cursor.getInt(1));
	    	description.add(cursor.getString(2));
	    	pheno_name.add(cursor.getString(3));
	    }
	     
	    for(int i = 0 ; i < phenophase.size() ; i++) {
	    	int _id = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + phenophase.get(i), null, null);
	    	String des = description.get(i);
	    	pi = new PlantItem(_id, des, phenophase.get(i), pheno_name.get(i));
	    	pItem.add(pi);
	    }
	    
	    MyListAdapter MyAdapter = new MyListAdapter(this, R.layout.phenophaselist, pItem);
	    
	    ListView myList = getListView();
	    myList.setAdapter(MyAdapter);
	    
	    // TODO Auto-generated method stub
	    cursor.close();
	    sDBHelper.close();
		sDB.close();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		Intent intent = new Intent(this, GetSpeciesInfo.class);
		intent.putExtra("pheno_id", pItem.get(position).Pheno_image);
		intent.putExtra("pheno_name", pItem.get(position).Pheno_name);
		intent.putExtra("pheno_text", pItem.get(position).Note);
		intent.putExtra("cname", cname);
		intent.putExtra("sname", sname);
		intent.putExtra("species_id", species_id);
		startActivity(intent);
	}
	
	class PlantItem{
		PlantItem(int aPicture, String aNote, int pheno_img_id, String aPheno_name){
			Picture = aPicture;
			Note = aNote;
			Pheno_image = pheno_img_id;
			Pheno_name = aPheno_name;
		}
		int Picture;
		String Note;
		int Pheno_image;
		String Pheno_name;
	}
	
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
			return arSrc.get(position).Note;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			ImageView img = (ImageView)convertView.findViewById(R.id.pheno_img);
			img.setImageResource(arSrc.get(position).Picture);
			
			TextView pheno_name = (TextView)convertView.findViewById(R.id.pheno_name);
			pheno_name.setText(arSrc.get(position).Pheno_name);
			
			TextView textname = (TextView)convertView.findViewById(R.id.pheno_text);
			textname.setText(arSrc.get(position).Note);
			
			
		
			return convertView;
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);

		menu.add(0, 1, 0,"Queue").setIcon(android.R.drawable.ic_menu_sort_by_size);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case 1:
				intent = new Intent(GetPhenophase.this, Queue.class);
				startActivity(intent);
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
}
