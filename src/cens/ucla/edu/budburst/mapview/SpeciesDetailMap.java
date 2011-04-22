package cens.ucla.edu.budburst.mapview;

import java.io.File;
import java.util.List;

import cens.ucla.edu.budburst.DetailPlantInfo;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.SummaryPlantInfo;
import cens.ucla.edu.budburst.UpdatePlantInfo;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperDrawableManager;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.lists.ListDetail;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.QuickCapture;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpeciesDetailMap extends MapActivity {

	private OneTimeDBHelper otDBH = null;
	private String mCommonName = null;
	private String mScienceName = null;
	private String mDate = null;
	private String mNotes = null;
	private String mPhotoName = null;
	private String mImageID = null;
	private int mPhenoID = 0;
	private int mPhenoIcon = 0;
	private String mUserName = null;
	private String mPhenoName = null;
	private String mPhenoText = null;
	private int mSpeciesID = 0;
	private int mProtocolID = 0;
	private int mCategory;
	private double mLatitude = 0.0;
	private double mLongitude = 0.0;
	protected static final int GET_CHANGE_CODE = 1;

	private PopupWindow popup = null;
	private View popupview = null;
	private ImageView phone_image = null;
	private ImageView species_image = null;
	private TextView pheno_title = null;
	private TextView cnameTxt = null;
	private TextView snameTxt = null;
	private TextView dt_takenTxt = null;
	private EditText notesTxt = null;
	private Button AddBtn = null;
	private List<Overlay> mapOverlays = null;
	private SpeciesItemizedOverlay itemizedOverlay = null;
	private HelperDrawableManager dm;
	private ProgressBar mSpinner;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitleBar();
		getIntentValue();
		setUpLayout();
	    showMapView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	
	    AddBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(SpeciesDetailMap.this)
				.setTitle(getString(R.string.Menu_addQCPlant))
				.setMessage(getString(R.string.Start_Shared_Plant))
				.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						/*
						 * Move to QuickCapture
						 */
						Intent intent = new Intent(SpeciesDetailMap.this, QuickCapture.class);
						
						intent.putExtra("cname", mCommonName);
						intent.putExtra("sname", mScienceName);
						intent.putExtra("protocol_id", mProtocolID);
						intent.putExtra("category", mCategory);
						intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
						
						startActivity(intent);
					}
				})
				.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						Log.i("K", "SpeciesID (SpeciesDetailMap) : " + mSpeciesID);
						Log.i("K", "CommonName (SpeciesDetailMap) : " + mCommonName);
						Log.i("K", "ScienceName (SpeciesDetailMap) : " + mScienceName);
						Log.i("K", "ProtocolID (SpeciesDetailMap) : " + mProtocolID);
						Log.i("K", "category (SpeciesDetailMap) : " + mCategory);
						
						/*
						 * Move to Getphenophase without a photo.
						 */
						Intent intent = new Intent(SpeciesDetailMap.this, GetPhenophase.class);
						intent.putExtra("camera_image_id", "");
						intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
						intent.putExtra("cname", mCommonName);
						intent.putExtra("sname", mScienceName);
						intent.putExtra("protocol_id", mProtocolID);
						intent.putExtra("species_id", mSpeciesID);
						intent.putExtra("category", mCategory);
												
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
		});
	    
	    mSpinner = (ProgressBar) findViewById(R.id.progressbar);
	    
	    if(mCategory == HelperValues.LOCAL_FLICKR) {
	    	dm = new HelperDrawableManager(mSpinner);
		    if(mImageID != null)
		    	dm.fetchDrawableOnThread(mImageID, phone_image);
		    
		    notesTxt.setEnabled(false);
		    notesTxt.setClickable(false);
		    
			if(mNotes.length() != 0) {
				notesTxt.setText(mNotes);
			}
			else {
				notesTxt.setText(getString(R.string.No_Notes));
			}
	    }
	    else {
	    	mSpinner.setVisibility(View.GONE);
	    	phone_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s" + mSpeciesID, null, null));
	    }
	}
	
	public void setTitleBar() {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.species_detail_map);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.Observation_Summary));
	}
	
	public void getIntentValue() {
		Intent intent = getIntent();
	    mCommonName = intent.getExtras().getString("cname");
	    mScienceName = intent.getExtras().getString("sname");
	    mUserName = intent.getExtras().getString("username");
	    mDate = intent.getExtras().getString("dt_taken");
	    mNotes = intent.getExtras().getString("notes");
	    mImageID = intent.getExtras().getString("imageID");
	    mSpeciesID = intent.getExtras().getInt("species_id", 0);
	    mPhenoID = intent.getExtras().getInt("phenophase_id", 0);
	    mProtocolID = intent.getExtras().getInt("protocol_id", 0);
	    mLatitude = intent.getExtras().getDouble("latitude", 0.0);
	    mLongitude = intent.getExtras().getDouble("longitude", 0.0);
	    mCategory = intent.getExtras().getInt("category", 0);
	    
	    StaticDBHelper sDBH = new StaticDBHelper(SpeciesDetailMap.this);
	    SQLiteDatabase sDB = sDBH.getReadableDatabase();
	    Cursor cursor = sDB.rawQuery("SELECT Phenophase_Icon, Type, Description FROM Onetime_Observation WHERE _id=" + mPhenoID, null);
	    while(cursor.moveToNext()) {
	    	mPhenoIcon = cursor.getInt(0);
	    	mPhenoName = cursor.getString(2);
	    	mPhenoText = cursor.getString(1);
	    }

	    cursor.close();
	    sDB.close();
	}
	
	public void setUpLayout() {
	    // setting up layout
	    phone_image = (ImageView) findViewById(R.id.phone_image);
	    //ImageView pheno_image = (ImageView) findViewById(R.id.pheno_image);
	    pheno_title = (TextView) findViewById(R.id.pheno_title);
	    cnameTxt = (TextView) findViewById(R.id.common_name);
	    snameTxt = (TextView) findViewById(R.id.science_name);
	    dt_takenTxt = (TextView) findViewById(R.id.timestamp_text);
	    notesTxt = (EditText) findViewById(R.id.mynotes);
	    AddBtn = (Button) findViewById(R.id.edit);
	    phone_image.setVisibility(View.VISIBLE);
	    
	    AddBtn.setText(getString(R.string.PlantInfo_makeObs));

	    // put cname and sname in the textView
	    pheno_title.setText("Credit: " + mUserName);
	    dt_takenTxt.setText(mDate + " ");
	    
	    cnameTxt.setText(mCommonName + " ");
	    snameTxt.setText(mScienceName + " ");

	}
	
	public void showMapView() {
		MapView myMap = (MapView) findViewById(R.id.simpleGM_map);
	    GeoPoint p = new GeoPoint((int)(mLatitude * 1000000), (int)(mLongitude * 1000000));
	    
	    myMap.setClickable(false);
	    MapController mc = myMap.getController();
	    mc.animateTo(p);
	    mc.setZoom(10);
	    
	    mapOverlays = myMap.getOverlays();
	    Drawable marker = getResources().getDrawable(R.drawable.marker);
	    itemizedOverlay = new SpeciesItemizedOverlay(marker, this);
	    
	    OverlayItem overlayitem = new OverlayItem(p, "spot", "Species found");
	    
		itemizedOverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedOverlay);
		
		myMap.setSatellite(false);
		myMap.setBackgroundResource(R.drawable.shapedrawable);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}