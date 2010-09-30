package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MyLocationOverlay;

import cens.ucla.edu.budburst.PlantInfo;
import cens.ucla.edu.budburst.PlantInformation;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
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

public class GetSpeciesInfo extends Activity{
	
	protected static final int PHOTO_CAPTURE_CODE = 0;
	protected static final int GET_PHENOPHASE_CODE = 1;
	protected static final int FINISH_CODE = 2;
	public final String BASE_PATH = "/sdcard/pbudburst/";
	public final String TEMP_PATH = "/sdcard/pbudburst/tmp/";
	private String camera_image_id 	= null;
	private View take_photo		   	= null;
	private View replace_photo 		= null;
	private ImageButton image 		= null;
	private Button phenophaseBtn 	= null;
	private Button saveBtn 			= null;
	private int species_id			= 0;
	private String cname 			= null;
	private String sname 			= null;
	private Bitmap bitmap 			= null;
	private File dict_tmp 			= null;
	private EditText notes 			= null;
	private double latitude 		= 0.0;
	private double longitude 		= 0.0;
	private int pheno_id;
	private int protocol_id;
	private int p_id;
	private OneTimeDBHelper otDBH;
	private LocationManager lm		= null;
	private String currentDateTimeString = null;
	private static GpsListener gpsListener;
	private LinearLayout phenoBox = null;

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.getspeciesinfo);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.flora_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  Make Observation");
	    
	    gpsListener = new GpsListener();
	    lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    // set update the location data in 3secs or 30meters
	    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 30, gpsListener);
	    
	    // call one-time database helper
	    otDBH = new OneTimeDBHelper(GetSpeciesInfo.this);
	    
	    // get the data from previous activity
	    Intent intent = getIntent();
	    species_id = intent.getExtras().getInt("species_id");
	    cname = intent.getExtras().getString("cname");
	    sname = intent.getExtras().getString("sname");
	    protocol_id = intent.getExtras().getInt("protocol_id");
	    pheno_id = intent.getExtras().getInt("pheno_id");
	    
	    notes = (EditText) findViewById(R.id.notes);
	    
	    ImageView species_image = (ImageView) findViewById(R.id.species_image);
	    ImageView pheno_image = (ImageView) findViewById(R.id.pheno_image);
	    TextView common_name = (TextView) findViewById(R.id.common_name);
	    TextView science_name = (TextView) findViewById(R.id.science_name);
	    Log.i("K", " SPECIES ID : " + species_id);
	   
	    species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s" + species_id, null, null));
	    species_image.setBackgroundResource(R.drawable.shapedrawable);
	    
	    pheno_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + pheno_id, null, null));
	    pheno_image.setBackgroundResource(R.drawable.shapedrawable);
	    // TODO Auto-generated method stub
	    common_name.setText(cname + " ");
	    science_name.setText(sname + " ");	    
	    
	    take_photo = this.findViewById(R.id.take_photo);
	    replace_photo = this.findViewById(R.id.replace_photo);
	    
	    take_photo.setVisibility(View.VISIBLE);
	    replace_photo.setVisibility(View.GONE);
	    
	    // make temp directory , temp directory will save user_photo data
	    // if there no directory, make one
	    dict_tmp = new File(Environment.getExternalStorageDirectory(), "pbudburst/tmp/");
	    if(!dict_tmp.exists()) {
	    	dict_tmp.mkdir();
	    }

	    take_photo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					File ld = new File(BASE_PATH);
					if(ld.exists()) {
						if (!ld.isDirectory()) {
							// Should probably inform user ... hmm!
							Toast.makeText(GetSpeciesInfo.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
							GetSpeciesInfo.this.finish();
						}
					}
					else {
						if(!ld.mkdir()) {
							Toast.makeText(GetSpeciesInfo.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
							GetSpeciesInfo.this.finish();
						}
					}
					
					try {
						SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
						String randomNum = new Integer(prng.nextInt()).toString();
						MessageDigest sha = MessageDigest.getInstance("SHA-1");
						byte[] result = sha.digest(randomNum.getBytes());
						camera_image_id = hexEncode(result);
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
					
					
					Intent mediaCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					mediaCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
							Uri.fromFile(new File(TEMP_PATH, camera_image_id + ".jpg")));
					startActivityForResult(mediaCaptureIntent, PHOTO_CAPTURE_CODE);
					
				}
				catch (Exception e) {
					Log.e("K", e.toString());
				}
			}
		});
	    
		//Take replace button
		replace_photo.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				File ld = new File(BASE_PATH);
				if (ld.exists()) {
					if (!ld.isDirectory()) {
						// Should probably inform user ... hmm!
						Toast.makeText(GetSpeciesInfo.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
						GetSpeciesInfo.this.finish();
					}
				} else {
					if (!ld.mkdir()) {
						Toast.makeText(GetSpeciesInfo.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
						GetSpeciesInfo.this.finish();
					}
				}

				try {
					SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
					String randomNum = new Integer(prng.nextInt()).toString();
					MessageDigest sha = MessageDigest.getInstance("SHA-1");
					byte[] result = sha.digest(randomNum.getBytes());
					camera_image_id = hexEncode(result);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Intent mediaCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				mediaCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
						Uri.fromFile(new File(TEMP_PATH, camera_image_id + ".jpg")));
				startActivityForResult(mediaCaptureIntent, PHOTO_CAPTURE_CODE);
			}
		});
		
		saveBtn = (Button) findViewById(R.id.save);
		
		saveBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				SQLiteDatabase db;
				//ContentValues row;
				
				String getNote = notes.getText().toString();
				if(getNote.equals("")) {
					getNote = "No Notes";
				}
				
				currentDateTimeString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
				
				Log.i("K", "info saved in the QUEUE.");
				
				Intent intent = new Intent(GetSpeciesInfo.this, SummarySpecies.class);
				intent.putExtra("cname", cname);
				intent.putExtra("sname", sname);
				intent.putExtra("lat", latitude);
				intent.putExtra("lng", longitude);
				intent.putExtra("image_id", pheno_id);
				intent.putExtra("dt_taken", currentDateTimeString);
				intent.putExtra("notes", getNote);
				intent.putExtra("photo_name", camera_image_id);
				startActivityForResult(intent, FINISH_CODE);
			}
		});
		
		image = (ImageButton) findViewById(R.id.image);
		image.setVisibility(View.GONE);
		
		image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				image.setBackgroundResource(R.drawable.shapedrawable_yellow);
				// TODO Auto-generated method stub
				final LinearLayout linear = (LinearLayout) View.inflate(GetSpeciesInfo.this, R.layout.image_popup, null);
				
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(GetSpeciesInfo.this);
				ImageView image_view = (ImageView) linear.findViewById(R.id.image_btn);
				
			    String imagePath = "/sdcard/pbudburst/tmp/" + camera_image_id + ".jpg";

			    File file = new File(imagePath);
			    Bitmap bitmap = null;
			    
			    // if file exists show the photo on the ImageButton
			    if(file.exists()) {
			    	imagePath = "/sdcard/pbudburst/tmp/" + camera_image_id + ".jpg";
				   	bitmap = BitmapFactory.decodeFile(imagePath);
				   	image_view.setImageBitmap(bitmap);
			    }
			    // if not, show 'no image' ImageButton
			    else {
			    	//image_view.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo", null, null));
			    	image_view.setVisibility(View.GONE);
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
		
	}
	
	static private String hexEncode( byte[] aInput){
	   StringBuilder result = new StringBuilder();
	   char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m'};
	   for ( int idx = 0; idx < 5; ++idx) {
		   byte b = aInput[idx];
		   result.append( digits[ (b&0xf0) >> 4 ] );
		   result.append( digits[ b&0x0f] );
	   }
	   return result.toString();
	}
	
	public void ShowPhotoTaken(String imagePath) {
		
		Log.i("K", "IMAGE PATH : " + imagePath);
		
		// we can put the option for the bitmap
		BitmapFactory.Options options = new BitmapFactory.Options();
		File file = new File(imagePath);
		
		Log.i("K", "FILE LENGTH : " + file.length());

		// change the sampleSize to 4 (which will be resulted in 1/4 of original size)
		if (file.length() > 1000000)
			options.inSampleSize = 8;
		else if (file.length() > 500000)
			options.inSampleSize = 4;
		else
			options.inSampleSize = 2;
		
		// use tempstorage
		options.inTempStorage = new byte[16*1024];
		
		// put image Path and the options
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		
		try{
			FileOutputStream out = new FileOutputStream(imagePath);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
		}catch(Exception e){
			Log.e("K", e.toString());
		}
		
		image.setBackgroundResource(R.drawable.shapedrawable_yellow);
		
		int width = bitmap.getWidth();
	   	int height = bitmap.getHeight();
	   	
	    Bitmap resized_bitmap = null;
	    // set new width and height of the phone_image
	    int new_width = 110;
	    int new_height = 110;
	   	
	   	float scale_width = ((float) new_width) / width;
	   	float scale_height = ((float) new_height) / height;
	   	Matrix matrix = new Matrix();
	   	matrix.postScale(scale_width, scale_height);
	   	resized_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		
	   	image.setImageBitmap(resized_bitmap);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		Log.d("K", "onActivityResult");
		
		if(resultCode == Activity.RESULT_OK) {
			
			if (requestCode == PHOTO_CAPTURE_CODE) {
				
				image.setVisibility(View.VISIBLE);
				String imagePath = TEMP_PATH + camera_image_id + ".jpg";
				
				ShowPhotoTaken(imagePath);
				
				Toast.makeText(this, "Photo added!", Toast.LENGTH_SHORT).show();
				
				currentDateTimeString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
				
				take_photo.setVisibility(View.GONE);
			    replace_photo.setVisibility(View.VISIBLE);
			}

			else if(requestCode == FINISH_CODE) {
				finish();
			}
		}			
	}
	
    @Override
    public void onDestroy() {
    	// when user finish this activity, turn off the gps
    	lm.removeUpdates(gpsListener);
        super.onDestroy();
    }
	
    // or when user press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			lm.removeUpdates(gpsListener);
			return true;
		}
		return false;
	}
	
	
	private class GpsListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {
			// TODO Auto-generated method stub
			if(loc != null) {
				latitude = loc.getLatitude();
				longitude = loc.getLongitude();
				
				//String strLoc = String.format("Current Location : %10.5f, %10.5f", latitude, longitude);
			
				//myLoc.setText(strLoc);
			}
		}
		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}	
	}
}
