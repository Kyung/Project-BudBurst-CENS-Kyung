package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;
import java.util.HashMap;

import cens.ucla.edu.budburst.AddPlant;
import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.PlantInformation_Direct;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class GetPhenophase extends ListActivity {
	
	private ArrayList<PlantItem> pItem;
	private int protocol_id;
	private String cname = null;
	private String sname = null;
	private int species_id = 0;
	private int _position = 0;
	//private RadioButton rb1 = null;
	//private RadioButton rb2 = null;
	//private RadioButton rb3 = null;
	private Button submitBtn = null;
	private String previous_activity;
	private EditText et1;
	private TextView myTitleText = null;
	private MyListAdapter MyAdapter = null;
	private ListView myList = null;
	private String camera_image_id = null;
	private double latitude = 0.0;
	private double longitude = 0.0;
	private String dt_taken = null;
	private int SELECT_PLANT_NAME = 100;
	private Integer new_plant_site_id; 
	private String new_plant_site_name;
	private CharSequence[] seqUserSite;
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>();
	FunctionsHelper helper;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.getphenophase);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);
		
		helper = new FunctionsHelper();
		mapUserSiteNameID = helper.getUserSiteIDMap(GetPhenophase.this);
		
	    Intent intent = getIntent();
	    protocol_id = intent.getExtras().getInt("protocol_id");
	    cname = intent.getExtras().getString("cname");
	    sname = intent.getExtras().getString("sname");
	    species_id = intent.getExtras().getInt("species_id");
	    camera_image_id = intent.getExtras().getString("camera_image_id");
		latitude = intent.getExtras().getDouble("latitude");
		longitude = intent.getExtras().getDouble("longitude");
		dt_taken = intent.getExtras().getString("dt_taken");
		previous_activity = intent.getExtras().getString("from");
	    
	    myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(cname + " > Phenophase");
		
		submitBtn = (Button) findViewById(R.id.submit);
		submitBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				helper.insertNewPlantToDB(GetPhenophase.this, species_id, 0, 9, cname, sname);
				int getID = helper.getID(GetPhenophase.this);
				helper.insertNewObservation(GetPhenophase.this, getID, protocol_id, latitude, longitude, camera_image_id, dt_taken, "");
				
				//helper.insertNewPlantToDB(GetPhenophase.this, species_id, 0, protocol_id, cname, sname, latitude, longitude, dt_taken, "", camera_image_id);
				Toast.makeText(GetPhenophase.this, getString(R.string.PlantInfo_successAdded), Toast.LENGTH_SHORT).show();
				
				Intent intent = new Intent(GetPhenophase.this, MainPage.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
	    
		/*
		rb1 = (RadioButton)findViewById(R.id.option1);
		rb2 = (RadioButton)findViewById(R.id.option2);
		rb3 = (RadioButton)findViewById(R.id.option3);
		
		rb1.setOnClickListener(radio_listener);
		rb2.setOnClickListener(radio_listener);
		rb3.setOnClickListener(radio_listener);
	    */
		
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
		myTitleText.setText(" " + cname);
		
		SQLiteDatabase db;
		StaticDBHelper staticDB = new StaticDBHelper(GetPhenophase.this);
		
		db = staticDB.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT _id, Type, Phenophase_ID, Phenophase_Icon, Chrono_Order, Description FROM Onetime_Observation ORDER BY Chrono_Order;", null);
		while(cursor.moveToNext()) {
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + cursor.getInt(3), null, null);
			PlantItem pi = new PlantItem(resID, cursor.getString(5), cursor.getInt(3), cursor.getString(1));
			pItem.add(pi);
		}
		
		cursor.close();
		db.close();

		MyAdapter = new MyListAdapter(GetPhenophase.this, R.layout.phenophaselist, pItem);
		myList = getListView(); 
		myList.setAdapter(MyAdapter);
	}
	
	/*
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
   */
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		/*
		
		Intent intent = new Intent(this, AddSite.class);
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
		startActivity(intent);*/
		
		
		
		_position = position;
		seqUserSite = helper.getUserSite(GetPhenophase.this);
		
		//Pop up choose site dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setItems(seqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			
				new_plant_site_id = mapUserSiteNameID.get(seqUserSite[which].toString());
				new_plant_site_name = seqUserSite[which].toString();
				
				if(new_plant_site_name == "Add New Site") {
					Intent intent = new Intent(GetPhenophase.this, AddSite.class);
					intent.putExtra("pheno_id", pItem.get(_position).Pheno_image);
					intent.putExtra("protocol_id", protocol_id);
					intent.putExtra("cname", cname);
					intent.putExtra("sname", sname);
					intent.putExtra("lat", latitude);
					intent.putExtra("lng", longitude);
					intent.putExtra("species_id", species_id);
					intent.putExtra("camera_image_id", camera_image_id);
					intent.putExtra("dt_taken", dt_taken);
					intent.putExtra("notes", "");
					intent.putExtra("from", SELECT_PLANT_NAME);
					startActivity(intent);
				}
				else {	
					if(species_id == 999 && !previous_activity.equals("Whatsinvasive")) {
						newDialog();
					}
					else {
						
						Log.i("K", "species_id : " + species_id + ", new_plant_site_id : " + new_plant_site_id + " , protocol : " + protocol_id + " , cname : " + cname + " , sname : " + sname + " , latitude : " + latitude + " , longitude : " + longitude + " , dt_taken : " + dt_taken + " , camera_image_id : " + camera_image_id);
						
						if(helper.insertNewPlantToDB(GetPhenophase.this, species_id, new_plant_site_id, 9, cname, sname)){							
							int getID = helper.getID(GetPhenophase.this);
							helper.insertNewObservation(GetPhenophase.this, getID, protocol_id, latitude, longitude, camera_image_id, dt_taken, "");
							Intent intent = new Intent(GetPhenophase.this, MainPage.class);
							Toast.makeText(GetPhenophase.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							//clear all stacked activities.
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(GetPhenophase.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();

	}
	
	private void newDialog() {
		Dialog dialog = new Dialog(GetPhenophase.this);
		
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
				
				if(helper.insertNewPlantToDB(GetPhenophase.this, species_id, new_plant_site_id, 9, cname, sname)){
					int getID = helper.getID(GetPhenophase.this);
					helper.insertNewObservation(GetPhenophase.this, getID, protocol_id, latitude, longitude, camera_image_id, dt_taken, "");
					
					Toast.makeText(GetPhenophase.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
					
					Intent intent = new Intent(GetPhenophase.this, PlantList.class);
					//clear all stacked activities.
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}else{
					Toast.makeText(GetPhenophase.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
				}
			}
		});
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
