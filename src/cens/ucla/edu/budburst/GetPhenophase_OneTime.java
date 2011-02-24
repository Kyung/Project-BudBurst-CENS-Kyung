package cens.ucla.edu.budburst;

import java.io.File;
import java.util.ArrayList;

import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GetPhenophase_OneTime extends ListActivity {

	private ArrayList<PlantItem> pItem;
	private MyListAdapter MyAdapter = null;
	private ListView myList = null;
	private Integer id = 0;
	private TextView myTitleText = null;
	private String cname;
	private String sname;
	private int species_id;
	private int protocol_id;
	private int category;


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

		pItem = new ArrayList<PlantItem>();

		Intent intent = getIntent();
		id = intent.getExtras().getInt("id");
		
		OneTimeDBHelper onetime = new OneTimeDBHelper(GetPhenophase_OneTime.this);
		SQLiteDatabase onetimeDB  = onetime.getReadableDatabase();
		Cursor cursor = onetimeDB.rawQuery("SELECT cname, sname, species_id, protocol_id, category FROM onetimeob WHERE plant_id=" + id, null);
		
		while(cursor.moveToNext()) {
			cname = cursor.getString(0);
			sname = cursor.getString(1);
			species_id = cursor.getInt(2);
			protocol_id = cursor.getInt(3);
			category = cursor.getInt(4);
		}
		
		cursor.close();
		
		// display species at the top
		ImageView species_image = (ImageView) findViewById(R.id.species_image);
		TextView common_name = (TextView) findViewById(R.id.common_name);
		TextView science_name = (TextView) findViewById(R.id.science_name);
		
		species_image.setVisibility(View.VISIBLE);
	    species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s" + species_id, null, null));
	    if(category == Values.TREE_LISTS_QC) {
    		String imagePath = Values.TREE_PATH + species_id + ".jpg";
    		FunctionsHelper helper = new FunctionsHelper();
    		species_image.setImageBitmap(helper.showImage(GetPhenophase_OneTime.this, imagePath));

	    }
	    species_image.setBackgroundResource(R.drawable.shapedrawable);
	    if(sname.equals("Unknown Plant")) {
	    	sname = "";
	    }
	    common_name.setText(cname);
	    science_name.setText(sname);
	    
	    
	    species_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(GetPhenophase_OneTime.this, SpeciesDetail.class);
				intent.putExtra("id", species_id);
				intent.putExtra("category", category);
				intent.putExtra("site_id", "");
				startActivity(intent);
			}
		});
		
		StaticDBHelper staticDB = new StaticDBHelper(GetPhenophase_OneTime.this);
		SQLiteDatabase sDB = staticDB.getReadableDatabase();
		
		cursor = sDB.rawQuery("SELECT _id, Type, Phenophase_ID, Phenophase_Icon, Description FROM Onetime_Observation", null);

		while(cursor.moveToNext()) {
			
			// get the _id value from Onetime_Observation table
			int cursor_get_id = cursor.getInt(0);

			
			Cursor cursor2 = onetimeDB.rawQuery("SELECT phenophase_id, image_id, dt_taken, notes FROM onetimeob_observation WHERE plant_id=" + id, null);
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + cursor.getInt(3), null, null);
			
			boolean flag = false;
			boolean header = false;
			String camera_image = "";
			String date = "";
			
			// 1, 4, 6, 8, 11 will show the header
			if(cursor_get_id == 1 || cursor_get_id == 4 || cursor_get_id == 6 || cursor_get_id == 8 || cursor_get_id == 11) {
				header = true;
			}
			
			Log.i("K", "cursor_get_id : " + cursor_get_id + " , header : " + header );

			while(cursor2.moveToNext()) {
				if(cursor.getInt(0) == cursor2.getInt(0)) {
					flag = true;
					PlantItem pi = new PlantItem(resID, cursor.getString(4), cursor.getInt(2), cursor.getInt(3), cursor.getString(1), flag, cursor2.getString(1), cursor2.getString(2), id, cursor2.getString(3), header);
					pItem.add(pi);
				}
			}
			
			if(!flag) {
				// no notes
				PlantItem pi = new PlantItem(resID, cursor.getString(4), cursor.getInt(2), cursor.getInt(3), cursor.getString(1), flag, camera_image, date, id, "", header);
				pItem.add(pi);
			}
			
			
			cursor2.close();
		}
		
		
		MyAdapter = new MyListAdapter(GetPhenophase_OneTime.this, R.layout.phenophaselist, pItem);
		myList = getListView(); 
		myList.setAdapter(MyAdapter);
		
		cursor.close();
		onetimeDB.close();
		sDB.close();

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + cname + " > Phenophase");

	    // TODO Auto-generated method stub
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
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		boolean observed_phenophase = pItem.get(position).Flag;
		
		Log.i("K", "onListItem Click -- ID : " + pItem.get(position).OneTimePlantID);
		Log.i("K", "pItem.get(position).Camera_image : " + pItem.get(position).ImageName);
		
		if(observed_phenophase) {
			Log.i("K","go PlantSummary");
			
			Intent intent = new Intent(GetPhenophase_OneTime.this, PlantSummary.class);
						
			intent.putExtra("pheno_id", pItem.get(position).PhenoID);
			intent.putExtra("pheno_icon", pItem.get(position).PhenoImageID);
			intent.putExtra("pheno_text", pItem.get(position).Description);
			intent.putExtra("pheno_name", pItem.get(position).PhenoName);
			intent.putExtra("protocol_id", protocol_id);
			intent.putExtra("category", category);
			intent.putExtra("photo_name", pItem.get(position).ImageName);
			intent.putExtra("species_id", species_id);
			intent.putExtra("site_id", 0);
			intent.putExtra("dt_taken", pItem.get(position).Date);
			intent.putExtra("notes", pItem.get(position).Note);
			intent.putExtra("cname", cname);
			intent.putExtra("sname", sname);
			intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
			intent.putExtra("onetimeplant_id", pItem.get(position).OneTimePlantID);
			
			startActivityForResult(intent, Values.RETURN_FROM_PLANT_INFORMATION);
		}
		else {
			// from GetPhenophase_PBB
			
			Intent intent = new Intent(GetPhenophase_OneTime.this, PlantInformation_Direct.class);
			intent.putExtra("pheno_id", pItem.get(position).PhenoID);
			intent.putExtra("pheno_icon", pItem.get(position).PhenoImageID);
			intent.putExtra("pheno_text", pItem.get(position).Description);
			intent.putExtra("pheno_name", pItem.get(position).PhenoName);
			intent.putExtra("protocol_id", protocol_id);
			intent.putExtra("category", category);
			intent.putExtra("photo_name", pItem.get(position).ImageName);
			intent.putExtra("species_id", species_id);
			intent.putExtra("site_id", 0);
			intent.putExtra("dt_taken", pItem.get(position).Date);
			intent.putExtra("notes", pItem.get(position).Note);
			intent.putExtra("cname", cname);
			intent.putExtra("sname", sname);
			intent.putExtra("direct", true);
			intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
			intent.putExtra("onetimeplant_id", pItem.get(position).OneTimePlantID);

			startActivityForResult(intent, Values.RETURN_FROM_PLANT_INFORMATION);
			
		}
		
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
			
			if(arSrc.get(position).Flag) {
				Bitmap icon = overlay(BitmapFactory.decodeResource(getResources(), arSrc.get(position).Picture));
				icon = overlay(icon, BitmapFactory.decodeResource(getResources(), R.drawable.check_mark));
				img.setImageBitmap(icon);
			}
			else {
				img.setImageResource(arSrc.get(position).Picture);
			}
			
			
			TextView header = (TextView)convertView.findViewById(R.id.list_header);
			if(arSrc.get(position).Header) {
				header.setText(arSrc.get(position).PhenoName);
				header.setVisibility(View.VISIBLE);
			}
			else {
				header.setVisibility(View.GONE);
			}
			
			View thumbnail = convertView.findViewById(R.id.wrap_icon);
			thumbnail.setTag(arSrc.get(position));
			thumbnail.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					PlantItem pi = (PlantItem)v.getTag();
					
					Intent intent = new Intent(GetPhenophase_OneTime.this, PhenophaseDetail.class);
					intent.putExtra("id", pi.PhenoID);
					intent.putExtra("frome", Values.FROM_QC_PHENOPHASE);
					startActivity(intent);
				}
			});

	
			TextView pheno_name = (TextView)convertView.findViewById(R.id.pheno_name);
			pheno_name.setVisibility(View.GONE);
			//pheno_name.setText(arSrc.get(position).Pheno_name);
			
			TextView textname = (TextView)convertView.findViewById(R.id.pheno_text);
			textname.setText(arSrc.get(position).Description);
		
			return convertView;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			if (requestCode == Values.RETURN_FROM_PLANT_INFORMATION) {
				
				Log.i("K", "In GetPhenophase_OneTime");
			    finish();
			}
			else if(requestCode == Values.RETURN_FROM_PLANT_QUICK) {
				finish();
			}
		}			
	}
}

