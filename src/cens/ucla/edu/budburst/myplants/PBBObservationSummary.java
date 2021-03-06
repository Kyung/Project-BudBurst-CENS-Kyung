package cens.ucla.edu.budburst.myplants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.R.string;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.mapview.BalloonItemizedOverlay;
import cens.ucla.edu.budburst.mapview.SpeciesItemizedOverlay;
import cens.ucla.edu.budburst.utils.PBBItems;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PBBObservationSummary extends MapActivity {

	private OneTimeDBHelper otDBH = null;
	private String mCommonName = null;
	private String mScienceName = null;
	private String mDate = null;
	private String mNotes = null;
	private String mPhotoName = null;
	private String mPhenoName = null;
	private String mPhenoDescription = null;
	
	private int mPhenoID = 0;
	private int mPhenoIcon = 0;
	private int mPlantID = 0;
	private int mPreviousActivity = 0;
	private int mSpeciesID = 0;
	private int mSiteID = 0;
	private int mProtocolID = 0;
	private int mCategory;
	
	private double mLatitude;
	private double mLongitude;
	protected static final int GET_CHANGE_CODE = 1;

	private PopupWindow popup = null;
	private View popupview = null;
	private ImageView phone_image = null;
	private List<Overlay> mapOverlays = null;
	private SpeciesItemizedOverlay itemizedOverlay = null;
	private HelperFunctionCalls mHelper;
	
	private PBBItems pbbItem;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		setContentView(R.layout.plantsummary);
		
	    // set database
	    otDBH = new OneTimeDBHelper(PBBObservationSummary.this);
	    
	    Bundle b = getIntent().getExtras();
	    pbbItem = b.getParcelable("pbbItem");
	    
	    mCommonName = pbbItem.getCommonName();
	    mScienceName = pbbItem.getScienceName();
	    mDate = pbbItem.getDate();
	    mNotes = pbbItem.getNote();
	    mPhotoName = pbbItem.getCameraImageName();
	    mCategory = pbbItem.getCategory();
	    mPhenoID = pbbItem.getPhenophaseID();
	    mProtocolID = pbbItem.getProtocolID();
	    mSpeciesID = pbbItem.getSpeciesID();
	    mSiteID = pbbItem.getSiteID();
	    mPlantID = pbbItem.getPlantID(); 
	    mPreviousActivity = b.getInt("from");
	    
	    if(mPreviousActivity == HelperValues.FROM_PLANT_LIST) {
	    	SyncDBHelper syncDBHelper = new SyncDBHelper(PBBObservationSummary.this);
			SQLiteDatabase syncDB  = syncDBHelper.getReadableDatabase();
			
			Cursor cur = syncDB.rawQuery("SELECT latitude, longitude FROM my_sites WHERE site_id = " + mSiteID, null);
			while(cur.moveToNext()) {
				mLatitude = Double.parseDouble(cur.getString(0));
				mLongitude = Double.parseDouble(cur.getString(1));
			}
			
			cur.close();
			syncDB.close();
	    }
	    if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE) {
	    	OneTimeDBHelper otDBHelper = new OneTimeDBHelper(PBBObservationSummary.this);
			SQLiteDatabase otDB  = otDBHelper.getReadableDatabase();
			
			Cursor cur = otDB.rawQuery("SELECT lat, lng FROM oneTimeObservation WHERE plant_id = " + mPlantID, null);
			while(cur.moveToNext()) {
				mLatitude = cur.getDouble(0);
				mLongitude = cur.getDouble(1);
				if(mLatitude != 0.0)
					break;
			}
			
			cur.close();
			otDB.close();
	    }
	    
	    // setting up layout
	    phone_image = (ImageView) findViewById(R.id.phone_image);
	    ImageView species_image = (ImageView) findViewById(R.id.species_image);
	    //ImageView pheno_image = (ImageView) findViewById(R.id.pheno_image);
	    TextView pheno_title = (TextView) findViewById(R.id.pheno_title);
	    TextView cnameTxt = (TextView) findViewById(R.id.common_name);
	    TextView snameTxt = (TextView) findViewById(R.id.science_name);
	    TextView dt_takenTxt = (TextView) findViewById(R.id.timestamp_text);
	    EditText notesTxt = (EditText) findViewById(R.id.mynotes);
	    Button editBtn = (Button) findViewById(R.id.edit);
	    phone_image.setVisibility(View.VISIBLE);
	    
	    
	    // Start mapView
	    MapView myMap = (MapView) findViewById(R.id.simpleGM_map);
	    GeoPoint p = new GeoPoint((int)(mLatitude * 1000000), (int)(mLongitude * 1000000));
	    
	    myMap.setClickable(false);
	    MapController mc = myMap.getController();
	    mc.animateTo(p);
	    mc.setZoom(10);
	    
	    mapOverlays = myMap.getOverlays();
	    Drawable marker = getResources().getDrawable(R.drawable.marker);
	    itemizedOverlay = new SpeciesItemizedOverlay(marker, this);
	    
	    OverlayItem overlayitem = new OverlayItem(p, "", "");
	    
		itemizedOverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedOverlay);
		
		myMap.setSatellite(false);
		myMap.setBackgroundResource(R.drawable.shapedrawable);
		
		StaticDBHelper sDBH = new StaticDBHelper(this);
		
	    dt_takenTxt.setText(mDate + " ");
	    
	    cnameTxt.setText(mCommonName + " ");
	    snameTxt.setText(mScienceName + " ");
	    
	    species_image.setBackgroundResource(R.drawable.shapedrawable);
	    
		// species_image view
		// should be dealt differently by category
		species_image.setVisibility(View.VISIBLE);
		mHelper = new HelperFunctionCalls();
		
		if(mPreviousActivity == HelperValues.FROM_PBB_PHENOPHASE
				|| mPreviousActivity == HelperValues.FROM_PLANT_LIST) {
			mHelper.showSpeciesThumbNailObserver(this, mCategory, mSpeciesID, mScienceName, species_image);
			HashMap pInfo = sDBH.getPhenoNameObserver(this, mPhenoID, mProtocolID);
			pheno_title.setText(pInfo.get("pType") + " ");
		}
		else {
			mHelper.showSpeciesThumbNail(this, mCategory, mSpeciesID, mScienceName, species_image);
			HashMap pInfo = sDBH.getPhenoName(this, mPhenoID);
		    pheno_title.setText(pInfo.get("pType") + " ");
		}

	    // when click species image
	    species_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PBBObservationSummary.this, DetailPlantInfo.class);
				intent.putExtra("pbbItem", pbbItem);
				startActivity(intent);
			}
		});
	    /*
	    // when click phenophase image
	    pheno_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PlantSummary.this, PhenophaseDetail.class);
				if(previous_activity == Values.FROM_PLANT_LIST) {
					intent.putExtra("id", pheno_id);
					intent.putExtra("protocol_id", protocol_id);
					intent.putExtra("from", Values.FROM_PBB_PHENOPHASE);
				}
				else {
					intent.putExtra("id", pheno_id);
					intent.putExtra("from", Values.FROM_QC_PHENOPHASE);
				}
				startActivity(intent);
			}
		});
	    */
	    String imagePath = null;
	    File file = new File(HelperValues.BASE_PATH + mPhotoName + ".jpg");
	    Bitmap bitmap = null;
	    Bitmap resized_bitmap = null;
	    
	    // set new width and height of the phone_image
	    int new_width = 110;
	    int new_height = 110;
	   
	    if(file.exists()) {
	    	imagePath = HelperValues.BASE_PATH + mPhotoName + ".jpg";
	    	Log.i("K", "imagePath : " + imagePath);
	    	bitmap = BitmapFactory.decodeFile(imagePath);
	    	
		   	int width = bitmap.getWidth();
		   	int height = bitmap.getHeight();
		   	
		   	float scale_width = ((float) new_width) / width;
		   	float scale_height = ((float) new_height) / height;
		   	Matrix matrix = new Matrix();
		   	matrix.postScale(scale_width, scale_height);
		   	resized_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		   	
	    	phone_image.setImageBitmap(resized_bitmap);
	    	phone_image.setVisibility(View.VISIBLE);
	    }
	    else {
	    	phone_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo", null, null));
	   	    phone_image.setVisibility(View.VISIBLE);
	   	}
	    
	    phone_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
	    // when click the image taken through camera
	    phone_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				phone_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
				
				final RelativeLayout linear = (RelativeLayout) View.inflate(PBBObservationSummary.this, R.layout.image_popup, null);
				
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(PBBObservationSummary.this);
				ImageView image_view = (ImageView) linear.findViewById(R.id.main_image);
				
			    String imagePath = "/sdcard/pbudburst/" + mPhotoName + ".jpg";

			    File file = new File(imagePath);
			    Bitmap bitmap = null;
			    
			    // if file exists show the photo on the ImageButton
			    if(file.exists()) {
			    	imagePath = "/sdcard/pbudburst/" + mPhotoName + ".jpg";
				   	bitmap = BitmapFactory.decodeFile(imagePath);
				   	image_view.setImageBitmap(bitmap);
			    }
			    // if not, show 'no image' ImageButton
			    else {
			    	image_view.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo", null, null));
			    }
			    
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
	    
	    notesTxt.setEnabled(false);
	    notesTxt.setClickable(false);
	    
		if(mNotes.length() != 0) {
			notesTxt.setText(mNotes);
		}
		else {
			notesTxt.setText(getString(R.string.No_Notes));
		}
		
		editBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PBBObservationSummary.this, UpdatePlantInfo.class);
				intent.putExtra("pbbItem", pbbItem);
				
				if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE) {
					intent.putExtra("from", HelperValues.FROM_QUICK_CAPTURE);
				}
				else {
					intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
				}
				
				startActivityForResult(intent, GET_CHANGE_CODE);
			}
		});
	    // TODO Auto-generated method stub
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.	
		if(resultCode == Activity.RESULT_OK) {			

			if (requestCode == GET_CHANGE_CODE) {
			
				Intent intent = getIntent();
				setResult(RESULT_OK, intent);
				finish();
			
			}
		}			
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}	
}
