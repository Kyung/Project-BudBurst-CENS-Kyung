package cens.ucla.edu.budburst;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GetPhenophase_PBB extends ListActivity {
	
	private ArrayList<PlantItem> pItem;
	private int protocol_ids	= 0;
	private int species_ids 	= 0;
	private int site_ids 	= 0;
	private String common_name = null;
	private String science_name = null;
	private ImageView img = null;
	protected static final int RETURN_FROM_PLANT_INFORMATION = 0;
	public final String BASE_PATH = "/sdcard/pbudburst/pbb/";
	public final String CALL_IMAGE_PATH = "/sdcard/pbudburst/";
	
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
		myTitleText.setText(" Choose PhenoPhase");
	    
	    
	    Intent intent = getIntent();
	    protocol_ids = intent.getExtras().getInt("protocol_id");
	    species_ids = intent.getExtras().getInt("species_id");
	    site_ids = intent.getExtras().getInt("site_id");
	    common_name = intent.getExtras().getString("cname");
	    science_name = intent.getExtras().getString("sname");
	    
	    Log.i("K", "GetPhenophase_PBB : " + species_ids + ", " + site_ids);
	    
	    ImageView species_image = (ImageView) findViewById(R.id.species_image);
	    TextView species_name = (TextView) findViewById(R.id.species_name);
	   
	    
	    species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+species_ids, null, null));
	    species_image.setBackgroundResource(R.drawable.shapedrawable);
	    species_name.setText(common_name + " \n" + science_name + " ");
	    
	    
	    pItem = new ArrayList<PlantItem>();
	    PlantItem pi;
	    
	    ArrayList<Integer> phenophase = new ArrayList<Integer>();
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
		query = "SELECT Phenophase_Icon, description, Phenophase_ID, Phenophase_Name, Protocol_ID FROM Phenophase_Protocol_Icon WHERE Protocol_ID=" + protocol_ids + " ORDER BY Chrono_Order ASC";
		
		//Log.i("K", query);
		
	    Cursor cursor = sDB.rawQuery(query, null);
		
	    while(cursor.moveToNext()) {
	    	
	    	int pheno_id = cursor.getInt(2);
	    	String pheno_existed = "SELECT species_id, site_id, phenophase_id, image_id, time, note FROM my_observation WHERE phenophase_id=" + pheno_id 
	    				+ " AND species_id=" + species_ids
	    				+ " AND site_id=" + site_ids 
	    				+ " ORDER BY time DESC LIMIT 1";
	    	
	    	Log.i("K", pheno_existed);
	    	
	    	Cursor cursor2 = syncDB.rawQuery(pheno_existed, null);
	    	cursor2.moveToNext();
	    	
	    	Log.i("K", "CURSOR COUNT : " + cursor2.getCount());
	    	
	    	if(cursor2.getCount() > 0) {
	    		Log.i("K", "IN THE TABLE!!!");
	    		phenophase.add(cursor.getInt(0));
	    		description.add(cursor.getString(1));
	    		protocol_id.add(cursor.getInt(2));
	    		pheno_name.add(cursor.getString(3));
	    		species_id.add(cursor2.getInt(0));
	    		site_id.add(cursor2.getInt(1));
	    		image_id.add(cursor2.getString(3));
	    		time.add(cursor2.getString(4));
	    		notes.add(cursor2.getString(5));
	    		flag.add(true);
	    	}
	    	else {
		    	phenophase.add(cursor.getInt(0));
		    	description.add(cursor.getString(1));
		    	protocol_id.add(cursor.getInt(2));
		    	pheno_name.add(cursor.getString(3));
		    	species_id.add(0);
	    		site_id.add(0);
	    		image_id.add("");
	    		time.add("");
	    		notes.add("");
	    		flag.add(false);
	    	}
	    	cursor2.close();
	    }
	    
	    Log.i("K", "SIZE : " + phenophase.size());
	     
	    for(int i = 0 ; i < phenophase.size() ; i++) {

	    	pi = new PlantItem(phenophase.get(i), protocol_id.get(i), description.get(i), pheno_name.get(i), image_id.get(i), species_id.get(i), site_id.get(i), time.get(i), notes.get(i), flag.get(i));
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
		
		Log.i("K", "POSITION_CLICKED : " + position);
		
		img.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("K", "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
			}
		});
		
		if(get_time.length() > 0) {
			Intent intent = new Intent(GetPhenophase_PBB.this, PlantSummary.class);
			intent.putExtra("pheno_id", pItem.get(position).Pheno_id);
			intent.putExtra("protocol_id", pItem.get(position).Protocol_id);
			intent.putExtra("pheno_text", pItem.get(position).Pheno_Description);
			intent.putExtra("pheno_name", pItem.get(position).Pheno_Name);
			intent.putExtra("photo_name", pItem.get(position).Image_id);
			intent.putExtra("species_id", species_ids);
			intent.putExtra("site_id", site_ids);
			intent.putExtra("dt_taken", pItem.get(position).Time);
			intent.putExtra("notes", pItem.get(position).Notes);
			intent.putExtra("cname", common_name);
			intent.putExtra("sname", science_name);
			startActivityForResult(intent, RETURN_FROM_PLANT_INFORMATION);
		}
		else {
			Intent intent = new Intent(GetPhenophase_PBB.this, PlantInformation_Direct.class);
			intent.putExtra("pheno_id", pItem.get(position).Pheno_id);
			intent.putExtra("protocol_id", pItem.get(position).Protocol_id);
			intent.putExtra("pheno_text", pItem.get(position).Pheno_Description);
			intent.putExtra("pheno_name", pItem.get(position).Pheno_Name);
			intent.putExtra("photo_name", pItem.get(position).Image_id);
			intent.putExtra("species_id", species_ids);
			intent.putExtra("site_id", site_ids);
			intent.putExtra("dt_taken", pItem.get(position).Time);
			intent.putExtra("notes", pItem.get(position).Notes);
			intent.putExtra("cname", common_name);
			intent.putExtra("sname", science_name);
			intent.putExtra("direct", true);
	
			startActivityForResult(intent, RETURN_FROM_PLANT_INFORMATION);
		}
	}
	
	class PlantItem{
		PlantItem(int aPheno_id, int aProtocol_id, String aPheno_Description, String aPheno_Name, String image_id, int aSpecies_id, int aSite_id, String aTime, String aNotes, boolean aFlag){
			
			Pheno_id = aPheno_id;
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
			
			Log.i("K","POSITION_PHENO : " + position);
			Log.i("K","ID : " + getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+arSrc.get(position).Pheno_id, null, null));
			Log.i("K",arSrc.get(position).Pheno_Name + " , " + arSrc.get(position).Pheno_id);
			
			int resId = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+arSrc.get(position).Pheno_id, null, null);
		
			Bitmap icon = overlay(BitmapFactory.decodeResource(getResources(), resId));
			if(arSrc.get(position).Flag == true) {
				
				//Log.i("K", "IMAGE_ID : " + arSrc.get(position).Image_id);
				
				File file_size = new File(CALL_IMAGE_PATH + arSrc.get(position).Image_id + ".jpg");
				
			    // if there's a photo in the table show that with replace_photo_button
			    if(file_size.length() != 0) {
			    	icon = overlay(icon, BitmapFactory.decodeResource(getResources(), R.drawable.check_mark));
			    }
			    else {
			    	File secondary_file_size = new File(BASE_PATH + arSrc.get(position).Image_id + ".jpg");
			    	if(secondary_file_size.length() != 0) {
			    		icon = overlay(icon, BitmapFactory.decodeResource(getResources(), R.drawable.check_mark));
			    	}
			    	else {
			    		icon = overlay(icon, BitmapFactory.decodeResource(getResources(), R.drawable.check_mark));
			    		yes_or_no = "(No Photo)";
			    	}
			    }
			}
			
			img = (ImageView)convertView.findViewById(R.id.pheno_img);
			img.setImageBitmap(icon);
			
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
		Log.i("K", " " + bitmaps);
		
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
				intent.putExtra("protocol_id", protocol_ids);
				intent.putExtra("species_id", species_ids);
			    intent.putExtra("site_id", site_ids);
			    intent.putExtra("cname", common_name);
			    intent.putExtra("sname", science_name);
			    
			    finish();
			    startActivity(intent);
			}
		}			
	}
}
