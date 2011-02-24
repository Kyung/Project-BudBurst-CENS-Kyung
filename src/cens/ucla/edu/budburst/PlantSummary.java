package cens.ucla.edu.budburst;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.onetime.HelloItemizedOverlay;
import cens.ucla.edu.budburst.onetime.WPinfo;
import android.app.Activity;
import android.app.AlertDialog;
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

public class PlantSummary extends MapActivity {

	private OneTimeDBHelper otDBH = null;
	private String cname = null;
	private String sname = null;
	private String dt_taken = null;
	private String notes = null;
	private String photo_name = null;
	private int pheno_id = 0;
	private int pheno_icon = 0;
	private int onetimeplant_id = 0;
	private int previous_activity = 0;
	private String pheno_name = null;
	private String pheno_text = null;
	private int species_id = 0;
	private int site_id = 0;
	private int protocol_id = 0;
	private int category;
	private double latitude = 0.0;
	private double longitude = 0.0;
	protected static final int GET_CHANGE_CODE = 1;

	private PopupWindow popup = null;
	private View popupview = null;
	private ImageButton phone_image = null;
	private List<Overlay> mapOverlays = null;
	private HelloItemizedOverlay itemizedOverlay = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.plantsummary);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.Observation_Summary));
	    // set database
	    otDBH = new OneTimeDBHelper(PlantSummary.this);
	    
	    // get intent data from previous activity
	    Intent intent = getIntent();
	    cname = intent.getExtras().getString("cname");
	    sname = intent.getExtras().getString("sname");
	    dt_taken = intent.getExtras().getString("dt_taken");
	    notes = intent.getExtras().getString("notes");
	    photo_name = intent.getExtras().getString("photo_name");
	    category = intent.getExtras().getInt("category");
	    
	    // get phenophase information
	    pheno_name = intent.getExtras().getString("pheno_name");
	    pheno_text = intent.getExtras().getString("pheno_text");
	    pheno_id = intent.getExtras().getInt("pheno_id", 0);
	    pheno_icon = intent.getExtras().getInt("pheno_icon", 0);
	    
	    onetimeplant_id = intent.getExtras().getInt("onetimeplant_id", 0);
	    protocol_id = intent.getExtras().getInt("protocol_id", 0);
	    species_id = intent.getExtras().getInt("species_id", 0);
	    site_id = intent.getExtras().getInt("site_id", 0);
	    
	    previous_activity = intent.getExtras().getInt("from");
	    
	    if(previous_activity == Values.FROM_PLANT_LIST) {
	    	SyncDBHelper syncDBHelper = new SyncDBHelper(PlantSummary.this);
			SQLiteDatabase syncDB  = syncDBHelper.getReadableDatabase();
			
			Cursor cur = syncDB.rawQuery("SELECT latitude, longitude FROM my_sites WHERE site_id = " + site_id, null);
			while(cur.moveToNext()) {
				latitude = Double.parseDouble(cur.getString(0));
				longitude = Double.parseDouble(cur.getString(1));
			}
			
			cur.close();
			syncDB.close();
	    }
	    if(previous_activity == Values.FROM_QUICK_CAPTURE) {
	    	OneTimeDBHelper otDBHelper = new OneTimeDBHelper(PlantSummary.this);
			SQLiteDatabase otDB  = otDBHelper.getReadableDatabase();
			
			Cursor cur = otDB.rawQuery("SELECT lat, lng FROM onetimeob_observation WHERE plant_id = " + onetimeplant_id, null);
			while(cur.moveToNext()) {
				latitude = cur.getDouble(0);
				longitude = cur.getDouble(1);
				if(latitude != 0.0)
					break;
			}
			
			cur.close();
			otDB.close();
	    }
	    
	    
	    Log.i("K", "previous_activity : " + previous_activity + " , plant_id :" + pheno_id + " , pheno_image_id : " + pheno_icon + " onetimeplant_id : " + onetimeplant_id);

	    // setting up layout
	    phone_image = (ImageButton) findViewById(R.id.phone_image);
	    ImageView species_image = (ImageView) findViewById(R.id.species_image);
	    ImageView pheno_image = (ImageView) findViewById(R.id.pheno_image);
	    TextView pheno_title = (TextView) findViewById(R.id.pheno_title);
	    TextView cnameTxt = (TextView) findViewById(R.id.common_name);
	    TextView snameTxt = (TextView) findViewById(R.id.science_name);
	    TextView dt_takenTxt = (TextView) findViewById(R.id.timestamp_text);
	    EditText notesTxt = (EditText) findViewById(R.id.mynotes);
	    Button editBtn = (Button) findViewById(R.id.edit);
	    phone_image.setVisibility(View.VISIBLE);
	    
	    
	    // Start mapView
	    MapView myMap = (MapView) findViewById(R.id.simpleGM_map);
	    GeoPoint p = new GeoPoint((int)(latitude * 1000000), (int)(longitude * 1000000));
	    
	    myMap.setClickable(false);
	    MapController mc = myMap.getController();
	    mc.animateTo(p);
	    mc.setZoom(10);
	    
	    mapOverlays = myMap.getOverlays();
	    Drawable marker = getResources().getDrawable(R.drawable.marker);
	    itemizedOverlay = new HelloItemizedOverlay(marker, this);
	    
	    OverlayItem overlayitem = new OverlayItem(p, "spot", "Species found");
	    
		itemizedOverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedOverlay);
		
		myMap.setSatellite(false);
		myMap.setBackgroundResource(R.drawable.shapedrawable);
		
	    // put cname and sname in the textView
	    pheno_title.setText(pheno_name + " ");
	    dt_takenTxt.setText(dt_taken + " ");
	    
	    cnameTxt.setText(cname + " ");
	    snameTxt.setText(sname + " ");
	    
	    if(species_id > 76 || category == Values.TREE_LISTS_QC) {
	    	// check out for the tree_list
	    	if(category == Values.TREE_LISTS_QC) {
	    		String imagePath = Values.TREE_PATH + species_id + ".jpg";
	    		FunctionsHelper helper = new FunctionsHelper();
	    		species_image.setImageBitmap(helper.showImage(PlantSummary.this, imagePath));
	    	}
	    	else {
	    		species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
	    	}
	    	
	    }
	    else {
	    	species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+species_id, null, null));
	    }
	    
	    species_image.setBackgroundResource(R.drawable.shapedrawable);
	    phone_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
	    if(pheno_id == 0) {
	    	pheno_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
	    }
	    else {
	    	pheno_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + pheno_icon, null, null));
	    }
	    
	    pheno_image.setBackgroundResource(R.drawable.shapedrawable);
	    pheno_image.setVisibility(View.VISIBLE);

	    // when click species image
	    species_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PlantSummary.this, SpeciesDetail.class);
				intent.putExtra("id", species_id);
				intent.putExtra("site_id", "");
				intent.putExtra("category", category);
				startActivity(intent);
			}
		});
	    
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
	    
	    String imagePath = null;
	    File file = new File(Values.BASE_PATH + photo_name + ".jpg");
	    Bitmap bitmap = null;
	    Bitmap resized_bitmap = null;
	    
	    // set new width and height of the phone_image
	    int new_width = 110;
	    int new_height = 110;
	   
	    if(file.exists()) {
	    	imagePath = Values.BASE_PATH + photo_name + ".jpg";
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
	    
	    // when click the image taken through camera
	    phone_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				phone_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
				
				final RelativeLayout linear = (RelativeLayout) View.inflate(PlantSummary.this, R.layout.image_popup, null);
				
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(PlantSummary.this);
				ImageView image_view = (ImageView) linear.findViewById(R.id.image_btn);
				
			    String imagePath = "/sdcard/pbudburst/" + photo_name + ".jpg";

			    File file = new File(imagePath);
			    Bitmap bitmap = null;
			    
			    // if file exists show the photo on the ImageButton
			    if(file.exists()) {
			    	imagePath = "/sdcard/pbudburst/" + photo_name + ".jpg";
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
	    
		if(notes.length() != 0) {
			notesTxt.setText(notes);
		}
		else {
			notesTxt.setText(getString(R.string.No_Notes));
		}
		
		editBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PlantSummary.this, PlantInformation_Direct.class);

				intent.putExtra("protocol_id", protocol_id);
				intent.putExtra("pheno_name", pheno_name);
				intent.putExtra("pheno_text", pheno_text);
				intent.putExtra("species_id", species_id);
				intent.putExtra("site_id", site_id);
				intent.putExtra("dt_taken", dt_taken);
				intent.putExtra("notes", notes);
				intent.putExtra("photo_name", photo_name);
				intent.putExtra("cname", cname);
				intent.putExtra("sname", sname);
				intent.putExtra("category", category);
				
				if(previous_activity == Values.FROM_QUICK_CAPTURE) {
					intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
					intent.putExtra("pheno_id", pheno_id);
					intent.putExtra("pheno_icon", pheno_icon);
					intent.putExtra("onetimeplant_id", onetimeplant_id);
				}
				else {
					intent.putExtra("pheno_id", pheno_id);
					intent.putExtra("pheno_icon", pheno_icon);
					intent.putExtra("from", Values.FROM_PLANT_LIST);
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
		Log.d("K", "onActivityResult");
		
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
