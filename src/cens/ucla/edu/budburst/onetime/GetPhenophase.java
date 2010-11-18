package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;

import cens.ucla.edu.budburst.GetPhenophase_PBB;
import cens.ucla.edu.budburst.PlantInformation_Direct;
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
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class GetPhenophase extends ListActivity {
	
	private ArrayList<PlantItem> pItem;
	private int protocol_id;
	private String cname = null;
	private String sname = null;
	private int species_id = 0;
	private RadioButton rb1 = null;
	private RadioButton rb2 = null;
	private RadioButton rb3 = null;
	private TextView myTitleText = null;
	private MyListAdapter MyAdapter = null;
	private ListView myList = null;
	private String camera_image_id = null;
	private double latitude = 0.0;
	private double longitude = 0.0;
	private String dt_taken = null;
	private int SELECT_PLANT_NAME = 100;
	
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
		
	    Intent intent = getIntent();
	    protocol_id = intent.getExtras().getInt("protocol_id");
	    cname = intent.getExtras().getString("cname");
	    sname = intent.getExtras().getString("sname");
	    species_id = intent.getExtras().getInt("species_id");
	    camera_image_id = intent.getExtras().getString("camera_image_id");
		latitude = intent.getExtras().getDouble("latitude");
		longitude = intent.getExtras().getDouble("longitude");
		dt_taken = intent.getExtras().getString("dt_taken");
	    
	    myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(cname + " > Phenophase");
	    
		rb1 = (RadioButton)findViewById(R.id.option1);
		rb2 = (RadioButton)findViewById(R.id.option2);
		rb3 = (RadioButton)findViewById(R.id.option3);
		
		rb1.setOnClickListener(radio_listener);
		rb2.setOnClickListener(radio_listener);
		rb3.setOnClickListener(radio_listener);
	    
		
		Log.i("K", "camera_image_id : " + camera_image_id);
		
	    //ImageView species_image = (ImageView) findViewById(R.id.species_image);
	    //TextView species_name = (TextView) findViewById(R.id.species_name);
	    
	    //species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s" + species_id, null, null));	    
	    //species_image.setBackgroundResource(R.drawable.shapedrawable);
	    
	    /*
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
		*/
	    //species_name.setText(cname + " \n" + sname + " ");

	    pItem = new ArrayList<PlantItem>();
	    
		myTitleText.setText(" " + cname + " > Leaves");
		
		//PlantItem(int aPicture, String aNote, int pheno_img_id, String aPheno_name)
		int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p18", null, null);
		Log.i("K","RESID : " + resID);
		PlantItem pi = new PlantItem(resID, "10% budburst", 18, "10% budburst");
		pItem.add(pi);
		
		resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p19", null, null);
		pi = new PlantItem(resID, "full leaf", 19, "full leaf");
		pItem.add(pi);
		
		resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p20", null, null);
		pi = new PlantItem(resID, "10% leaf color", 20, "10% leaf color");
		pItem.add(pi);
		
		resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p19", null, null);
		pi = new PlantItem(resID, "full leaf color", 19, "full leaf color");
		pItem.add(pi);
		
		resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p22", null, null);
		pi = new PlantItem(resID, "90% leaf drop", 22, "90% leaf drop");
		pItem.add(pi);
		
		MyAdapter = new MyListAdapter(GetPhenophase.this, R.layout.phenophaselist, pItem);
		myList = getListView(); 
		myList.setAdapter(MyAdapter);
	}
	
	private OnClickListener radio_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			
			if(v == rb1) {
				//header.setText("'TOP 10' list of the plants.");
				pItem = new ArrayList<PlantItem>();
				
				myTitleText.setText(" " + cname + " > Leaves");
				
				//PlantItem(int aPicture, String aNote, int pheno_img_id, String aPheno_name)
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+18, null, null);
				Log.i("K","RESID : " + resID);
				PlantItem pi = new PlantItem(resID, "10% budburst", 18, "10% budburst");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+19, null, null);
				pi = new PlantItem(resID, "full leaf", 19, "full leaf");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+20, null, null);
				pi = new PlantItem(resID, "10% leaf color", 20, "10% leaf color");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+19, null, null);
				pi = new PlantItem(resID, "full leaf color", 19, "full leaf color");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+22, null, null);
				pi = new PlantItem(resID, "90% leaf drop", 22, "90% leaf drop");
				pItem.add(pi);
				
				MyAdapter = new MyListAdapter(GetPhenophase.this, R.layout.phenophaselist, pItem);
				myList = getListView(); 
				myList.setAdapter(MyAdapter);

			}
			else if (v == rb2) {
				//header.setText("'ALL' list of the plants.");
				pItem = new ArrayList<PlantItem>();
				
				myTitleText.setText(" " + cname + " > Flowers");
				
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+1, null, null);
				PlantItem pi = new PlantItem(resID, "10% flowers", 1, "10% budburst");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+1, null, null);
				pi = new PlantItem(resID, "10% budburst", 1, "10% budburst");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+1, null, null);
				pi = new PlantItem(resID, "10% budburst", 1, "10% budburst");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+1, null, null);
				pi = new PlantItem(resID, "10% budburst", 1, "10% budburst");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+1, null, null);
				pi = new PlantItem(resID, "10% budburst", 1, "10% budburst");
				pItem.add(pi);
				
				MyAdapter = new MyListAdapter(GetPhenophase.this, R.layout.phenophaselist, pItem);
				myList = getListView(); 
				myList.setAdapter(MyAdapter);
			}
			else {
				pItem = new ArrayList<PlantItem>();
				
				myTitleText.setText(" " + cname + " > Fruits");
				
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+1, null, null);
				PlantItem pi = new PlantItem(resID, "10% budburst", 1, "10% budburst");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+1, null, null);
				pi = new PlantItem(resID, "10% budburst", 1, "10% budburst");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+1, null, null);
				pi = new PlantItem(resID, "10% budburst", 1, "10% budburst");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+1, null, null);
				pi = new PlantItem(resID, "10% budburst", 1, "10% budburst");
				pItem.add(pi);
				
				resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+1, null, null);
				pi = new PlantItem(resID, "10% budburst", 1, "10% budburst");
				pItem.add(pi);	
				
				MyAdapter = new MyListAdapter(GetPhenophase.this, R.layout.phenophaselist, pItem);
				myList = getListView(); 
				myList.setAdapter(MyAdapter);
			}
		}
	};
	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		Intent intent = new Intent(this, PlantInformation_Direct.class);
		intent.putExtra("pheno_id", pItem.get(position).Pheno_image);
		intent.putExtra("pheno_name", pItem.get(position).Pheno_name);
		intent.putExtra("pheno_text", pItem.get(position).Note);
		intent.putExtra("protocol_id", protocol_id);
		intent.putExtra("pheno_id", pItem.get(position).Pheno_image);
		intent.putExtra("cname", cname);
		intent.putExtra("sname", sname);
		intent.putExtra("lat", latitude);
		intent.putExtra("lng", longitude);
		intent.putExtra("species_id", species_id);
		intent.putExtra("camera_image_id", camera_image_id);
		intent.putExtra("dt_taken", dt_taken);
		intent.putExtra("notes", "");
		intent.putExtra("from", SELECT_PLANT_NAME);
		intent.putExtra("direct", true);
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
