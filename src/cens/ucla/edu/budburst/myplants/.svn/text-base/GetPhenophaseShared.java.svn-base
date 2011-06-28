package cens.ucla.edu.budburst.myplants;

import java.io.File;
import java.util.ArrayList;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.R.string;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.onetime.OneTimePhenophase;
import cens.ucla.edu.budburst.utils.PBBItems;
import cens.ucla.edu.budburst.utils.QuickCapture;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class GetPhenophaseShared extends ListActivity {

	private ArrayList<HelperPlantItem> pItem;
	private MyListAdapter MyAdapter = null;
	private ListView mMyList = null;
	private Integer mPlantID = 0;
	private TextView myTitleText = null;
	private ImageView species_image;
	private String mCommonName;
	private String mScienceName;
	private int mSpeciesID;
	private int mProtocolID;
	private int mCategory;
	private int mCurrentPosition;
	private int mImageID = 0;
	private HelperFunctionCalls mHelper;
	private OneTimeDBHelper otDBH;
	private SQLiteDatabase oDB;
		
	private PBBItems pbbItem = new PBBItems();

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
		
		mHelper = new HelperFunctionCalls();

		Intent intent = getIntent();
		mPlantID = intent.getExtras().getInt("id");
		
		Log.i("K", "PlantID : " + mPlantID);
		
		otDBH = new OneTimeDBHelper(GetPhenophaseShared.this);
		oDB  = otDBH.getReadableDatabase();
		Cursor cursor = oDB.rawQuery("SELECT cname, sname, species_id, protocol_id, category " +
				"FROM oneTimePlant WHERE plant_id=" + mPlantID, null);
		
		while(cursor.moveToNext()) {
			mCommonName = cursor.getString(0);
			mScienceName = cursor.getString(1);
			mSpeciesID = cursor.getInt(2);
			mProtocolID = cursor.getInt(3);
			mCategory = cursor.getInt(4);
		}
		cursor.close();
		oDB.close();
		
		// display species at the top
		species_image = (ImageView) findViewById(R.id.species_image);
		TextView common_name = (TextView) findViewById(R.id.common_name);
		TextView science_name = (TextView) findViewById(R.id.science_name);
		
		// species_image view
		// should be dealt differently by category
		species_image.setVisibility(View.VISIBLE);
		mHelper.showSpeciesThumbNail(this, mCategory, mSpeciesID, mScienceName, species_image);
	    
	    species_image.setBackgroundResource(R.drawable.shapedrawable);
	    if(mScienceName.equals("Unknown Plant")) {
	    	mScienceName = "";
	    }
	    common_name.setText(mCommonName + " ");
	    science_name.setText(mScienceName + " ");
	    
		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + mCommonName + " > Phenophase");

		
		species_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(GetPhenophaseShared.this, DetailPlantInfo.class);

				PBBItems pbbItem = new PBBItems();
				pbbItem.setSpeciesID(mSpeciesID);
				pbbItem.setCommonName(mCommonName);
				pbbItem.setScienceName(mScienceName);
				pbbItem.setCategory(mCategory);
				intent.putExtra("pbbItem", pbbItem);
				startActivity(intent);
			}
		});

		getPhenophaseLists();
	    // TODO Auto-generated method stub
	}
	
	public void onResume() {
		super.onResume();
	}
	
	private void getPhenophaseLists() {
		StaticDBHelper staticDB = new StaticDBHelper(GetPhenophaseShared.this);
		SQLiteDatabase sDB = staticDB.getReadableDatabase();
		oDB  = otDBH.getReadableDatabase();
		
		String query = "SELECT _id, Type, Phenophase_Icon, Description FROM Onetime_Observation WHERE Protocol_ID";
		Cursor cursor = sDB.rawQuery(query + "=" + mProtocolID, null);

		// initialize arraylist
		pItem = new ArrayList<HelperPlantItem>();
		
		while(cursor.moveToNext()) {
	
			int phenoID = cursor.getInt(0);
			int phenoIcon = cursor.getInt(2);
			String phenoType = cursor.getString(1);
			String phenoDesc = cursor.getString(3);
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + phenoIcon, null, null);
			
		
			boolean flag = false;
			boolean header = false;
			
			if(mProtocolID == HelperValues.QUICK_TREES_AND_SHRUBS) {
				// to show the header, we need to know the first index of each category.
				if(phenoID == 1 
						|| phenoID == 4 
						|| phenoID == 7 
						|| phenoID == 10
						|| phenoID == 13) {
					header = true;
				}
			}
			else if(mProtocolID == HelperValues.QUICK_WILD_FLOWERS) {
				// to show the header, we need to know the first index of each category.
				if(phenoID == 16 
						|| phenoID == 19) {
					header = true;
				}

			}
			else if(mProtocolID == HelperValues.QUICK_GRASSES) {
				// to show the header, we need to know the first index of each category.
				if(phenoID == 22 
						|| phenoID == 23 
						|| phenoID == 26 ) {
					header = true;
				}
			}

			Cursor cursor2 = oDB.rawQuery("SELECT phenophase_id, image_id, dt_taken, notes " +
					"FROM oneTimeObservation WHERE plant_id=" + mPlantID + 
					" AND phenophase_id=" + phenoID, null);
			
			String imageName = "";
			String date = "";
			String note = "";
			while(cursor2.moveToNext()) {
				imageName = cursor2.getString(1);
				date = cursor2.getString(2);
				note = cursor2.getString(3);
			}
			
			Log.i("K", "Note:" + note);
			
			if(cursor2.getCount() > 0) {
				//int aPicture, String aDescription, int aPheno_ID, int aPhenoImageID, String aPheno_name, boolean aFlag, String aCamera_image, String aDate, int aOneTimePlantID, String aNote, boolean aHeader)
				HelperPlantItem pi = new HelperPlantItem();
				pi.setPicture(resID);
				pi.setDescription(phenoDesc);
				pi.setPhenoID(phenoID);
				pi.setPhenoImageID(phenoIcon);
				pi.setPhenoName(phenoType);
				pi.setFlag(true);
				pi.setImageName(imageName);
				pi.setDate(date);
				pi.setPlantID(mPlantID);
				pi.setNote(note);
				pi.setHeader(header);
				
				pItem.add(pi);
			}
			else {
				HelperPlantItem pi = new HelperPlantItem();
				pi.setPicture(resID);
				pi.setDescription(phenoDesc);
				pi.setPhenoID(phenoID);
				pi.setPhenoImageID(phenoIcon);
				pi.setPhenoName(phenoType);
				pi.setFlag(false);
				pi.setImageName(imageName);
				pi.setDate(date);
				pi.setPlantID(mPlantID);
				pi.setNote(note);
				pi.setHeader(header);
			
				pItem.add(pi);
			}
			
			cursor2.close();
		}
		
		
		MyAdapter = new MyListAdapter(GetPhenophaseShared.this, R.layout.phenophaselist, pItem);
		mMyList = getListView(); 
		mMyList.setAdapter(MyAdapter);
		
		cursor.close();
		oDB.close();
		sDB.close();

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
		boolean observed_phenophase = pItem.get(position).getFlag();
		
		mCurrentPosition = position;

		pbbItem.setSpeciesID(mSpeciesID);
		pbbItem.setCommonName(mCommonName);
		pbbItem.setScienceName(mScienceName);
		pbbItem.setCategory(mCategory);
		pbbItem.setProtocolID(mProtocolID);
		pbbItem.setDate(pItem.get(position).getDate());
		pbbItem.setNote(pItem.get(position).getNote());
		pbbItem.setPhenophaseID(pItem.get(position).getPhenoID());
		pbbItem.setLocalImageName(pItem.get(position).getImageName());
		pbbItem.setPlantID(pItem.get(position).getPlantID());
		pbbItem.setLatitude(0.0);
		pbbItem.setLongitude(0.0);
		pbbItem.setSiteID(0);
				
		Log.i("K", "Note: " + pItem.get(position).getNote());
		
		if(observed_phenophase) {
			Log.i("K","go PlantSummary");
			
			Intent intent = new Intent(GetPhenophaseShared.this, PBBObservationSummary.class);
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("from", HelperValues.FROM_QUICK_CAPTURE);
			startActivity(intent);
			
		}
		else {
			// from GetPhenophase_PBB
			
			/*
			 * Ask users if they are ready to take a photo.
			 */
			new AlertDialog.Builder(GetPhenophaseShared.this)
			.setTitle(getString(R.string.Menu_addQCPlant))
			.setMessage(getString(R.string.Start_Shared_Plant))
			.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					/*
					 * Move to QuickCapture
					 */
					Intent intent = new Intent(GetPhenophaseShared.this, QuickCapture.class);
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_QC_PHENOPHASE);
					startActivity(intent);
					
				}
			})
			.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(GetPhenophaseShared.this, PBBAddNotes.class);
					intent.putExtra("camera_image_id", "");
					intent.putExtra("from", HelperValues.FROM_QC_PHENOPHASE);
					intent.putExtra("pbbItem", pbbItem);
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
		}
		
	}
	

	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<HelperPlantItem> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<HelperPlantItem> aarSrc){
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}
		
		public int getCount(){
			return arSrc.size();
		}
		
		public String getItem(int position){
			return arSrc.get(position).getNote();
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			ImageView img = (ImageView)convertView.findViewById(R.id.pheno_img);
			
			if(arSrc.get(position).getFlag()) {
				Bitmap icon = overlay(BitmapFactory.decodeResource(getResources(), arSrc.get(position).getPicture()));
				icon = overlay(icon, BitmapFactory.decodeResource(getResources(), R.drawable.check_mark));
				img.setImageBitmap(icon);
			}
			else {
				img.setImageResource(arSrc.get(position).getPicture());
			}
			
			
			TextView header = (TextView)convertView.findViewById(R.id.list_header);
			if(arSrc.get(position).getHeader()) {
				header.setText(arSrc.get(position).getPhenoName());
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
					HelperPlantItem pi = (HelperPlantItem)v.getTag();
					
					Intent intent = new Intent(GetPhenophaseShared.this, PBBPhenophaseInfo.class);
					intent.putExtra("id", pi.getPhenoID());
					intent.putExtra("frome", HelperValues.FROM_QC_PHENOPHASE);
					startActivity(intent);
				}
			});

	
			TextView pheno_name = (TextView)convertView.findViewById(R.id.pheno_name);
			pheno_name.setVisibility(View.GONE);
			//pheno_name.setText(arSrc.get(position).Pheno_name);
			
			TextView textname = (TextView)convertView.findViewById(R.id.pheno_text);
			textname.setText(arSrc.get(position).getDescription());
		
			return convertView;
		}
	}
}

