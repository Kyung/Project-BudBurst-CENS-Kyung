package cens.ucla.edu.budburst;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.onetime.AddNotes;
import cens.ucla.edu.budburst.onetime.QuickCapture;
import android.app.Activity;
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
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GetPhenophase_PBB extends ListActivity {
	
	private ArrayList<PlantItem> pItem;
	private int protocolID		= 0;
	private int speciesID 		= 0;
	private int siteID 			= 0;
	private int previousActivity = 0;
	private String cname = null;
	private String sname = null;
	private EditText et1 = null;
	private EditText et2 = null;
	private ImageView img = null;
	private Dialog dialog = null;
	private TextView common_name = null;
	private TextView science_name = null;
	private String camera_image_id = "none";
	protected static final int RETURN_FROM_PLANT_INFORMATION = 0;
	protected static final int RETURN_FROM_PLANT_QUICK = 1;
	private int currentPosition;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.getphenophase_pbb);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.GetPhenophase_choose));
	    
	    Intent intent = getIntent();
	    protocolID = intent.getExtras().getInt("protocol_id");
	    speciesID = intent.getExtras().getInt("species_id");
	    siteID = intent.getExtras().getInt("site_id");
	    cname = intent.getExtras().getString("cname");
	    sname = intent.getExtras().getString("sname");
	    previousActivity = intent.getExtras().getInt("from");
	    camera_image_id = intent.getExtras().getString("camera_image_id");
	    
	    LinearLayout add_species_name = (LinearLayout)findViewById(R.id.add_species_name);
	    
	    ImageView species_image = (ImageView) findViewById(R.id.species_image);
	    
	    common_name = (TextView) findViewById(R.id.common_name);
	    science_name = (TextView) findViewById(R.id.science_name);
	    
	    if(previousActivity == Values.FROM_PLANT_LIST) {
		    species_image.setVisibility(View.VISIBLE);
		    if(speciesID > 76) {
	    		species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
	    	}
	    	else {
	    		species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s" + speciesID, null, null));
	    	}
		    common_name.setText(cname);
		    science_name.setText(sname);
	    }
	    else {
	    	species_image.setVisibility(View.VISIBLE);
	    	species_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
	    	if(speciesID > 76) {
	    		species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
	    	}
	    	else {
	    		species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s" + speciesID, null, null));
	    	}
	    	common_name.setText(cname);
		    science_name.setText(sname);
	    }
	    
	    species_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(GetPhenophase_PBB.this, SpeciesDetail.class);
				intent.putExtra("id", speciesID);
				intent.putExtra("site_id", "");
				startActivity(intent);
			}
		});
	    
	    pItem = new ArrayList<PlantItem>();
	    PlantItem pi;
	    
	    ArrayList<Integer> pheno_id = new ArrayList<Integer>();
	    ArrayList<Integer> pheno_icon = new ArrayList<Integer>();
	    ArrayList<Integer> protocol_id = new ArrayList<Integer>();
	    ArrayList<String> pheno_name = new ArrayList<String>();
	    ArrayList<String> description = new ArrayList<String>();
	    ArrayList<Integer> species_id = new ArrayList<Integer>();
	    ArrayList<Integer> site_id	= new ArrayList<Integer>();
	    ArrayList<String> image_id = new ArrayList<String>();
	    ArrayList<String> time = new ArrayList<String>();
	    ArrayList<String> notes = new ArrayList<String>();
	    ArrayList<Boolean> flag = new ArrayList<Boolean>();
	    
	    
		StaticDBHelper sDBHelper = new StaticDBHelper(GetPhenophase_PBB.this);
		SQLiteDatabase sDB = sDBHelper.getReadableDatabase();
		
		SyncDBHelper syncDBHelper = new SyncDBHelper(GetPhenophase_PBB.this);
		SQLiteDatabase syncDB  = syncDBHelper.getReadableDatabase();
	    
		String query = null;
		query = "SELECT Phenophase_Icon, description, Phenophase_ID, Phenophase_Name, Protocol_ID FROM Phenophase_Protocol_Icon WHERE Protocol_ID=" + protocolID + " ORDER BY Chrono_Order ASC";
		
	    Cursor cursor = sDB.rawQuery(query, null);
		
	    while(cursor.moveToNext()) {
	    	
	    	String pheno_existed = "SELECT species_id, site_id, phenophase_id, image_id, time, note FROM my_observation WHERE phenophase_id=" + cursor.getInt(2)
	    				+ " AND species_id=" + speciesID
	    				+ " AND site_id=" + siteID 
	    				+ " ORDER BY time DESC LIMIT 1";
	    
	    	Log.i("K", "Pheno_existed : " + pheno_existed);
	    	
	    	Cursor cursor2 = syncDB.rawQuery(pheno_existed, null);
	    	cursor2.moveToNext();
	    	
	    	Log.i("K", "CURSOR COUNT : " + cursor2.getCount());
	    	
	    	if(cursor2.getCount() > 0) {
	    		Log.i("K", "item already in the table...observed phenophase");
	    		pheno_icon.add(cursor.getInt(0));
	    		description.add(cursor.getString(1));
	    		pheno_id.add(cursor.getInt(2));
	    		pheno_name.add(cursor.getString(3));
	    		protocol_id.add(cursor.getInt(4));
	    		
	    		species_id.add(cursor2.getInt(0));
	    		site_id.add(cursor2.getInt(1));
	    		image_id.add(cursor2.getString(3));
	    		time.add(cursor2.getString(4));
	    		notes.add(cursor2.getString(5));
	    		flag.add(true);
	    	}
	    	else {
	    		pheno_icon.add(cursor.getInt(0));
	    		description.add(cursor.getString(1));
	    		pheno_id.add(cursor.getInt(2));
		    	pheno_name.add(cursor.getString(3));
		    	protocol_id.add(cursor.getInt(4));
		    	
		    	species_id.add(0);
	    		site_id.add(0);
	    		image_id.add("");
	    		time.add("");
	    		notes.add("");
	    		flag.add(false);
	    	}
	    	cursor2.close();
	    }
	     
	    for(int i = 0 ; i < pheno_id.size() ; i++) {
	    	pi = new PlantItem(pheno_id.get(i), pheno_icon.get(i), protocol_id.get(i), description.get(i), pheno_name.get(i), image_id.get(i), species_id.get(i), site_id.get(i), time.get(i), notes.get(i), flag.get(i));
	    	pItem.add(pi);
	    }
	    
	    MyListAdapter MyAdapter = new MyListAdapter(this, R.layout.phenophaselist, pItem);
	    
	    ListView myList = getListView();
	    myList.setAdapter(MyAdapter);
	    
	    // TODO Auto-generated method stub
	    cursor.close();
	    sDBHelper.close();
		sDB.close();
		syncDBHelper.close();
		syncDB.close();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		String get_time = pItem.get(position).Time;
		
		currentPosition = position;
		
		/*
		 * Flag == True, means this position of phenophase has been observed.
		 */
		if(pItem.get(position).Flag) {
			Intent intent = new Intent(GetPhenophase_PBB.this, PlantSummary.class);
			intent.putExtra("pheno_id", pItem.get(position).Pheno_id);
			intent.putExtra("pheno_icon", pItem.get(position).Pheno_Icon);
			intent.putExtra("pheno_text", pItem.get(position).Pheno_Description);
			intent.putExtra("pheno_name", pItem.get(position).Pheno_Name);
			intent.putExtra("protocol_id", pItem.get(position).Protocol_id);
			
			intent.putExtra("photo_name", pItem.get(position).Image_id);
			intent.putExtra("species_id", speciesID);
			intent.putExtra("site_id", siteID);
			intent.putExtra("dt_taken", pItem.get(position).Time);
			intent.putExtra("notes", pItem.get(position).Notes);			
			intent.putExtra("cname", cname);
			intent.putExtra("sname", sname);
			intent.putExtra("from", Values.FROM_PLANT_LIST);
			startActivityForResult(intent, RETURN_FROM_PLANT_INFORMATION);
		}
		else {
			
			/*
			 * Ask users if they are ready to take a photo.
			 */
			new AlertDialog.Builder(GetPhenophase_PBB.this)
			.setTitle(getString(R.string.Menu_addPlant))
			.setMessage(getString(R.string.Start_Shared_Plant))
			.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					/*
					 * Move to QuickCapture
					 */
					Intent intent = new Intent(GetPhenophase_PBB.this, QuickCapture.class);
					intent.putExtra("from", Values.FROM_PBB_PHENOPHASE);
					intent.putExtra("cname", cname);
					intent.putExtra("sname", sname);
					intent.putExtra("pheno_id", pItem.get(currentPosition).Pheno_id);
					intent.putExtra("protocol_id", pItem.get(currentPosition).Protocol_id);
					intent.putExtra("species_id", speciesID);
					intent.putExtra("site_id", siteID);
					intent.putExtra("latitude", 0.0);
					intent.putExtra("longitude", 0.0);
					
					startActivity(intent);
					
				}
			})
			.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(GetPhenophase_PBB.this, AddNotes.class);
					intent.putExtra("camera_image_id", "");
					intent.putExtra("from", Values.FROM_PBB_PHENOPHASE);
					intent.putExtra("cname", cname);
					intent.putExtra("sname", sname);
					intent.putExtra("pheno_id", pItem.get(currentPosition).Pheno_id);
					intent.putExtra("protocol_id", pItem.get(currentPosition).Protocol_id);					
					intent.putExtra("species_id", speciesID);
					intent.putExtra("site_id", siteID);
					intent.putExtra("latitude", 0.0);
					intent.putExtra("longitude", 0.0);
					
					startActivity(intent);
				}
			})
			.setNegativeButton(getString(R.string.Button_Cancel), new DialogInterface.OnClickListener() {
					@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			})
			.show();
			
			/*
			Intent intent = new Intent(GetPhenophase_PBB.this, PlantInformation_Direct.class);
			intent.putExtra("pheno_id", pItem.get(position).Pheno_id);
			intent.putExtra("pheno_icon", pItem.get(position).Pheno_Icon);
			intent.putExtra("pheno_text", pItem.get(position).Pheno_Description);
			intent.putExtra("pheno_name", pItem.get(position).Pheno_Name);
			intent.putExtra("protocol_id", pItem.get(position).Protocol_id);
			
			intent.putExtra("photo_name", pItem.get(position).Image_id);
			intent.putExtra("species_id", speciesID);
			intent.putExtra("site_id", siteID);
			intent.putExtra("dt_taken", pItem.get(position).Time);
			intent.putExtra("notes", pItem.get(position).Notes);
			intent.putExtra("cname", cname);
			intent.putExtra("sname", sname);
			intent.putExtra("camera_image_id", camera_image_id);
			intent.putExtra("from", Values.FROM_PLANT_LIST);
			intent.putExtra("direct", true);
			*/
			//startActivityForResult(intent, RETURN_FROM_PLANT_QUICK);
		}
	}
	
	class PlantItem{
		PlantItem(int aPheno_id, int aPheno_Icon, int aProtocol_id, String aPheno_Description, String aPheno_Name, String image_id, int aSpecies_id, int aSite_id, String aTime, String aNotes, boolean aFlag){
			
			Pheno_id = aPheno_id;
			Pheno_Icon = aPheno_Icon;
			Protocol_id = aProtocol_id;
			Pheno_Description = aPheno_Description;
			Pheno_Name = aPheno_Name;
			Image_id = image_id;
			Species_ID = aSpecies_id;
			Site_ID = aSite_id;
			Time = aTime;
			Notes = aNotes;
			Flag = aFlag;
		}
		
		int Pheno_id;
		int Pheno_Icon;
		int Protocol_id;
		String Pheno_Description;
		String Pheno_Name;
		String Image_id;
		int Species_ID;
		int Site_ID;
		String Time;
		String Notes;
		boolean Flag;
		
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
			return arSrc.get(position).Pheno_Description;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			String yes_or_no = "";
			
			int resId = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+arSrc.get(position).Pheno_Icon, null, null);
			Bitmap icon = overlay(BitmapFactory.decodeResource(getResources(), resId));
			if(arSrc.get(position).Flag == true) {
			
				File file_size = new File(Values.BASE_PATH + arSrc.get(position).Image_id + ".jpg");
				
			    // if there's a photo in the table show that with replace_photo_button
			    if(file_size.length() != 0) {
			    	icon = overlay(icon, BitmapFactory.decodeResource(getResources(), R.drawable.check_mark));
			    }
			    else {
		    		icon = overlay(icon, BitmapFactory.decodeResource(getResources(), R.drawable.check_mark));
		    		yes_or_no = getString(R.string.GetPhenophase_noPhoto);
		    	}
			}
			
			img = (ImageView)convertView.findViewById(R.id.pheno_img);
			img.setImageBitmap(icon);
			
			
			// call View from the xml and link the view to current position.
			// need to be fixed....
			View thumbnail = convertView.findViewById(R.id.wrap_icon);
			thumbnail.setTag(arSrc.get(position));
			thumbnail.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					PlantItem pi = (PlantItem)v.getTag();
					
					Intent intent = new Intent(GetPhenophase_PBB.this, PhenophaseDetail.class);
					intent.putExtra("id", pi.Pheno_id);
					intent.putExtra("protocol_id", pi.Protocol_id);
					intent.putExtra("from", Values.FROM_PBB_PHENOPHASE);
					startActivity(intent);
				}
			});
			
			TextView yesorno_photo = (TextView)convertView.findViewById(R.id.yesorno_photo);
			TextView phenoName = (TextView)convertView.findViewById(R.id.pheno_name);
			TextView textname = (TextView)convertView.findViewById(R.id.pheno_text);
			
			yesorno_photo.setText(yes_or_no);
			phenoName.setText(arSrc.get(position).Pheno_Name);
			textname.setText(arSrc.get(position).Pheno_Description);
			
			return convertView;
		}
	}
	
	private Bitmap overlay(Bitmap... bitmaps) {
		
		if (bitmaps[0].equals(null))
			return null;

		Bitmap bmOverlay = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_4444);

		Canvas canvas = new Canvas(bmOverlay);
		for (int i = 0; i < bitmaps.length; i++)
			canvas.drawBitmap(bitmaps[i], new Matrix(), null);

		return bmOverlay;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			if (requestCode == RETURN_FROM_PLANT_INFORMATION) {
				
				Log.i("K", "ARRIVED HERE!!");
				
				Intent intent = new Intent(GetPhenophase_PBB.this, GetPhenophase_PBB.class);
				intent.putExtra("protocol_id", protocolID);
				intent.putExtra("species_id", speciesID);
			    intent.putExtra("site_id", siteID);
			    intent.putExtra("cname", cname);
			    intent.putExtra("sname", sname);
			    
			    finish();
			    startActivity(intent);
			}
			else if(requestCode == RETURN_FROM_PLANT_QUICK) {
				finish();
			}
		}			
	}
}
