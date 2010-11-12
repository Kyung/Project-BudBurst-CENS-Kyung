package cens.ucla.edu.budburst.onetime;

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

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SummarySpecies extends MapActivity {

	public final String TEMP_PATH = "/sdcard/pbudburst/tmp/";
	private OneTimeDBHelper otDBH = null;
	private String cname = null;
	private String sname = null;
	private double lat = 0.0;
	private double lng = 0.0;
	private int image_id = 0;
	private String dt_taken = null;
	private String notes = null;
	private String photo_name = null;
	private HelloItemizedOverlay itemizedOverlay = null;
	private List<Overlay> mapOverlays = null;
	private ImageView phone_image = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.summaryspecies);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.flora_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  Observation Summery");
	    
	    
	    otDBH = new OneTimeDBHelper(SummarySpecies.this);
	    
	    // get intent data from previous activity
	    Intent intent = getIntent();
	    cname = intent.getExtras().getString("cname");
	    sname = intent.getExtras().getString("sname");
	    lat = intent.getExtras().getDouble("lat");
	    lng = intent.getExtras().getDouble("lng");
	    image_id = intent.getExtras().getInt("image_id");
	    dt_taken = intent.getExtras().getString("dt_taken");
	    notes = intent.getExtras().getString("notes");
	    photo_name = intent.getExtras().getString("photo_name");
	    
	    // Start mapView
	    MapView myMap = (MapView) findViewById(R.id.simpleGM_map);
	    // need to multiply 1000000 to get the proper data
		GeoPoint p = new GeoPoint((int)(lat * 1000000), (int)(lng * 1000000));
		//myMap.setBuiltInZoomControls(true);
		myMap.setClickable(false);
		MapController mc = myMap.getController();
		mc.animateTo(p);
		mc.setZoom(10);
		
		mapOverlays = myMap.getOverlays();
		Drawable marker = getResources().getDrawable(R.drawable.marker);
		itemizedOverlay = new HelloItemizedOverlay(marker, this);
		
		OverlayItem overlayitem = new OverlayItem(p, "spot", "Species found!");
		itemizedOverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedOverlay);
		
		myMap.setSatellite(false);
		myMap.setBackgroundResource(R.drawable.shapedrawable);
		// end the mapView
		
	    // setting up layout
	    phone_image = (ImageView) findViewById(R.id.phone_image);
	    ImageView pheno_image = (ImageView) findViewById(R.id.pheno_image);
	    TextView cnameTxt = (TextView) findViewById(R.id.common_name);
	    TextView snameTxt = (TextView) findViewById(R.id.science_name);
	    TextView dt_takenTxt = (TextView) findViewById(R.id.timestamp_text);
	    EditText notesTxt = (EditText) findViewById(R.id.mynotes);
	    //TextView geoTxt = (TextView) findViewById(R.id.geolocation);
	    Button saveBtn = (Button) findViewById(R.id.save);
	    Button editBtn = (Button) findViewById(R.id.edit);
	    
	    String imagePath = null;
	    File file = new File(TEMP_PATH + photo_name + ".jpg");
	    Bitmap bitmap = null;
	    Bitmap resized_bitmap = null;
	    
	    // set new width and height of the phone_image
	    int new_width = 110;
	    int new_height = 110;
	   
	    if(file.exists()) {
	    	imagePath = TEMP_PATH + photo_name + ".jpg";
	    	bitmap = BitmapFactory.decodeFile(imagePath);
	    	
		   	int width = bitmap.getWidth();
		   	int height = bitmap.getHeight();
		   	
		   	float scale_width = ((float) new_width) / width;
		   	float scale_height = ((float) new_height) / height;
		   	Matrix matrix = new Matrix();
		   	matrix.postScale(scale_width, scale_height);
		   	resized_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		   	
	    	phone_image.setImageBitmap(resized_bitmap);
	    	phone_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
	    	phone_image.setVisibility(View.VISIBLE);
	    }
	    else {
	    	phone_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo", null, null));
	   	    phone_image.setVisibility(View.VISIBLE);
	   	    phone_image.setEnabled(false);
	   	}
	    
	    phone_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				phone_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
				// TODO Auto-generated method stub
				final LinearLayout linear = (LinearLayout) View.inflate(SummarySpecies.this, R.layout.image_popup, null);
				
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(SummarySpecies.this);
				ImageView image_view = (ImageView) linear.findViewById(R.id.image_btn);
				
			    String imagePath = TEMP_PATH + photo_name + ".jpg";

			    File file = new File(imagePath);
			    Bitmap bitmap = null;
			    
			    // if file exists show the photo on the ImageButton
			    if(file.exists()) {
			    	imagePath = TEMP_PATH + photo_name + ".jpg";
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
	    
	    
	    if(image_id == 0) {
	    	pheno_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
	    }
	    else {
	    	pheno_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + image_id, null, null));
	    }
		
	    
	    pheno_image.setBackgroundResource(R.drawable.shapedrawable);
	    pheno_image.setVisibility(View.VISIBLE);
		
		cnameTxt.setText(" " + cname + " ");
		snameTxt.setText(" " + sname + " ");
		//geoTxt.setText(" " + String.format("%10.5f , %10.5f", lat, lng) + " ");
		dt_takenTxt.setText(" " + dt_taken + " ");
		notesTxt.setText(notes);   
		
		saveBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				SQLiteDatabase db;
				//ContentValues row;

				// set db writable
				db = otDBH.getWritableDatabase();
				// set values which will be inserted in the table
				// this is one way to do, there's another way also.

				// insert data into onetimeob table
				db.execSQL("INSERT INTO onetimeob VALUES(" + image_id + "," 
						+ "'" + cname + "',"
						+ "'" + sname + "',"
						+ lat + ","
						+ lng + ","
						+ "'" + dt_taken + "',"
						+ "'" + notes + "',"
						+ "'" + photo_name + "',"
						+ "'0');");
				
				// should close the databasehelper; otherwise, memory leaks
				otDBH.close();
				
				// add vibration when done
				Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(1000);
				
				Log.i("K", "info saved in the QUEUE.");
				Toast.makeText(SummarySpecies.this, "Your observation is in the Queue!", Toast.LENGTH_SHORT).show();
				Intent intent = getIntent();
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		editBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	    
	    // TODO Auto-generated method stub
	}
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
