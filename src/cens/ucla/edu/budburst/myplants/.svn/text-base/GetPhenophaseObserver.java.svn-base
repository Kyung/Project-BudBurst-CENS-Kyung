package cens.ucla.edu.budburst.myplants;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.R.string;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.utils.PBBItems;
import cens.ucla.edu.budburst.utils.QuickCapture;
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

public class GetPhenophaseObserver extends ListActivity {
	
	private ArrayList<HelperPlantItem> pItem;
	private int mProtocolID		= 0;
	private int mSpeciesID 		= 0;
	private int mSiteID 		= 0;
	private int mCategory		= 0;
	private int mPreviousActivity = 0;
	private String mCommonName = null;
	private String mScienceName = null;
	private String mNote;
	
	private EditText et1 = null;
	private EditText et2 = null;
	private ImageView img = null;
	private ImageView species_image = null;
	private Dialog dialog = null;
	private TextView common_name = null;
	private TextView science_name = null;
	private HelperFunctionCalls mHelper;
	private SyncDBHelper sDBH;
	private String mPhotoName = "none";
	protected static final int RETURN_FROM_PLANT_INFORMATION = 0;
	protected static final int RETURN_FROM_PLANT_QUICK = 1;
	private int currentPosition;
	
	private PBBItems pbbItem;
	
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
		
		// getting parcelable value
		Bundle bundle = getIntent().getExtras();
		mPreviousActivity = bundle.getInt("from");
		
		pbbItem = bundle.getParcelable("pbbItem");
		mSpeciesID = pbbItem.getSpeciesID();
		mSiteID = pbbItem.getSiteID();
		mCommonName = pbbItem.getCommonName();
		mScienceName = pbbItem.getScienceName();
		mProtocolID = pbbItem.getProtocolID();
		mCategory = pbbItem.getCategory();
		
		getPhenophaseList();
	}
	
	private void getPhenophaseList() {
		sDBH = new SyncDBHelper(this);
		mNote = sDBH.getNote(this, mSpeciesID, mSiteID);
		pbbItem.setNote(mNote);
		mPhotoName = sDBH.getImageName(this, mSpeciesID, mSiteID);
		pbbItem.setLocalImageName(mPhotoName);
		
		// setting layout
	    LinearLayout add_species_name = (LinearLayout)findViewById(R.id.add_species_name);
	    species_image = (ImageView) findViewById(R.id.species_image);
	    species_image.setBackgroundResource(R.drawable.shapedrawable);
	    
	    common_name = (TextView) findViewById(R.id.common_name);
	    science_name = (TextView) findViewById(R.id.science_name);
	    common_name.setText(mCommonName + " ");
	    science_name.setText(mScienceName + " ");
	    
	    mHelper = new HelperFunctionCalls();
	    mHelper.showSpeciesThumbNailObserver(this, mCategory, mSpeciesID, mScienceName, species_image);
	    
	    
	    // setting species information into arraylists
	    species_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(GetPhenophaseObserver.this, DetailPlantInfo.class);
				intent.putExtra("pbbItem", pbbItem);
				startActivity(intent);
			}
		});
	    
	    pItem = new ArrayList<HelperPlantItem>();
	    HelperPlantItem pi;
	    
		StaticDBHelper statDBH = new StaticDBHelper(GetPhenophaseObserver.this);
		SQLiteDatabase sDB = statDBH.getReadableDatabase();
		SQLiteDatabase syncDB  = sDBH.getReadableDatabase();
	    
		String query = null;
		query = "SELECT Phenophase_Icon, description, Phenophase_ID, Phenophase_Name, Protocol_ID FROM Phenophase_Protocol_Icon WHERE Protocol_ID=" + mProtocolID + " ORDER BY Chrono_Order ASC";
		
	    Cursor cursor = sDB.rawQuery(query, null);
		
	    while(cursor.moveToNext()) {
	    	
	    	String pheno_existed = "SELECT species_id, site_id, phenophase_id, image_id, time, note FROM my_observation WHERE phenophase_id=" + cursor.getInt(2)
	    				+ " AND species_id=" + mSpeciesID
	    				+ " AND site_id=" + mSiteID 
	    				+ " ORDER BY time DESC LIMIT 1";

	    	Cursor cursor2 = syncDB.rawQuery(pheno_existed, null);
	    	cursor2.moveToNext();
	    	if(cursor2.getCount() > 0) {
	    		Log.i("K", "item already in the table...observed phenophase");
	    		
	    		pi = new HelperPlantItem();
	    		pi.setPhenoImageID(cursor.getInt(0));
	    		pi.setDescription(cursor.getString(1));
	    		pi.setPhenoID(cursor.getInt(2));
	    		pi.setPhenoName(cursor.getString(3));
	    		pi.setProtocolID(cursor.getInt(4));
	    		pi.setSpeciesID(cursor2.getInt(0));
	    		pi.setSiteID(cursor2.getInt(1));
	    		pi.setImageName(cursor2.getString(3));
	    		pi.setDate(cursor2.getString(4));
	    		pi.setNote(cursor2.getString(5));	    				
	    		pi.setFlag(true);				
	    		
		    	pItem.add(pi);
	    	}
	    	else {

	    		pi = new HelperPlantItem();
	    		pi.setPhenoImageID(cursor.getInt(0));
	    		pi.setDescription(cursor.getString(1));
	    		pi.setPhenoID(cursor.getInt(2));
	    		pi.setProtocolID(cursor.getInt(4));
	    		pi.setPhenoName(cursor.getString(3));
	    		pi.setSpeciesID(0);
	    		pi.setSiteID(0);
	    		pi.setImageName("");
	    		pi.setDate("");
	    		pi.setNote("");
	    		pi.setFlag(false);

	    		pItem.add(pi);
	 
	    	}
	    	cursor2.close();
	    }
	     
	    PhenophaseListAdapter MyAdapter = new PhenophaseListAdapter(this, R.layout.phenophaselist, pItem);
	    
	    ListView myList = getListView();
	    myList.setAdapter(MyAdapter);
	    
	    // TODO Auto-generated method stub
	    cursor.close();
	    statDBH.close();
		sDB.close();
		sDBH.close();
		syncDB.close();
	}
	
	public void onResume() {
		super.onResume();

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		currentPosition = position;
		pbbItem.setPhenophaseID(pItem.get(position).getPhenoID());
		pbbItem.setDate(pItem.get(position).getDate());
		pbbItem.setNote(pItem.get(position).getNote());
		pbbItem.setLocalImageName(pItem.get(position).getImageName());
		
		/*
		 * Flag == True, means this position of phenophase has been observed.
		 */
		if(pItem.get(position).getFlag()) {
			Intent intent = new Intent(GetPhenophaseObserver.this, PBBObservationSummary.class);
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
			startActivity(intent);
		}
		else {
			
			/*
			 * Ask users if they are ready to take a photo.
			 */
			new AlertDialog.Builder(GetPhenophaseObserver.this)
			.setTitle(getString(R.string.Menu_addPlant))
			.setMessage(getString(R.string.Start_Shared_Plant))
			.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
					// Move to QuickCapture
					Intent intent = new Intent(GetPhenophaseObserver.this, QuickCapture.class);
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_PBB_PHENOPHASE);
					startActivity(intent);
					
				}
			})
			.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(GetPhenophaseObserver.this, PBBAddNotes.class);
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_PBB_PHENOPHASE);
					
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
	
	class PhenophaseListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<HelperPlantItem> arSrc;
		int layout;
		
		public PhenophaseListAdapter(Context context, int alayout, ArrayList<HelperPlantItem> aarSrc){
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}
		
		public int getCount(){
			return arSrc.size();
		}
		
		public String getItem(int position){
			return arSrc.get(position).getDescription();
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			String yes_or_no = "";
			
			int resId = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+arSrc.get(position).getPhenoImageID(), null, null);
			Bitmap icon = overlay(BitmapFactory.decodeResource(getResources(), resId));
			if(arSrc.get(position).getFlag() == true) {
			
				File file_size = new File(HelperValues.BASE_PATH + arSrc.get(position).getImageName() + ".jpg");
				
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
					HelperPlantItem pi = (HelperPlantItem)v.getTag();
					
					Intent intent = new Intent(GetPhenophaseObserver.this, PBBPhenophaseInfo.class);
					intent.putExtra("id", pi.getPhenoID());
					intent.putExtra("protocol_id", pi.getProtocolID());
					intent.putExtra("from", HelperValues.FROM_PBB_PHENOPHASE);
					startActivity(intent);
				}
			});
			
			TextView yesorno_photo = (TextView)convertView.findViewById(R.id.yesorno_photo);
			TextView phenoName = (TextView)convertView.findViewById(R.id.pheno_name);
			TextView textname = (TextView)convertView.findViewById(R.id.pheno_text);
			
			yesorno_photo.setText(yes_or_no);
			phenoName.setText(arSrc.get(position).getPhenoName());
			textname.setText(arSrc.get(position).getDescription());
			
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
}
