package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import cens.ucla.edu.budburst.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyPhotoDetail extends MapActivity {

	public final String TEMP_PATH = "/sdcard/pbudburst/tmp/";
	private HelloItemizedOverlay itemizedOverlay = null;
	private List<Overlay> mapOverlays = null;
	private ImageView phone_image = null;
	private String photo_name = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.myphotodetail);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.flora_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  Species in the queue");
	    
	    // get intent data from previous activity
	    Intent intent = getIntent();
	    String cname = intent.getExtras().getString("cname");
	    String sname = intent.getExtras().getString("sname");
	    double lat = intent.getExtras().getDouble("lat");
	    double lng = intent.getExtras().getDouble("lng");
	    int image_id = intent.getExtras().getInt("image_id");
	    String dt_taken = intent.getExtras().getString("dt_taken");
	    String notes = intent.getExtras().getString("notes");
	    photo_name = intent.getExtras().getString("photo_name");
	    
	    // setting up layout
	    phone_image = (ImageView) findViewById(R.id.phone_image);
	    ImageView pheno_image = (ImageView) findViewById(R.id.pheno_image);
	    TextView cnameTxt = (TextView) findViewById(R.id.common_name);
	    TextView snameTxt = (TextView) findViewById(R.id.science_name);
	    TextView dt_takenTxt = (TextView) findViewById(R.id.timestamp_text);
	    TextView notesTxt = (TextView) findViewById(R.id.mynotes);
	    Button okBtn = (Button) findViewById(R.id.okay);
	    
	    okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
	    });
	    
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
				final LinearLayout linear = (LinearLayout) View.inflate(MyPhotoDetail.this, R.layout.image_popup, null);
				
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(MyPhotoDetail.this);
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
		itemizedOverlay = new HelloItemizedOverlay(marker, this, myMap);
		
		OverlayItem overlayitem = new OverlayItem(p, "spot", "Species found!");
		itemizedOverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedOverlay);
		
		myMap.setSatellite(false);
		myMap.setBackgroundResource(R.drawable.shapedrawable);
		// end the mapView
	    
	    pheno_image.setBackgroundResource(R.drawable.shapedrawable);
	    pheno_image.setVisibility(View.VISIBLE);
		
		cnameTxt.setText(" " + cname + " ");
		snameTxt.setText(" " + sname + " ");
		dt_takenTxt.setText(" " + dt_taken + " ");
		notesTxt.setText(notes);   
	    // TODO Auto-generated method stub
	}
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
