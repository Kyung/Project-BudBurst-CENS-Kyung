package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.BackgroundService;
import cens.ucla.edu.budburst.helper.Media;
import cens.ucla.edu.budburst.helper.Values;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

public class QuickCapture extends Activity {

	protected static final int PHOTO_CAPTURE_CODE = 0;
	private String camera_image_id 	= null;
	private SharedPreferences pref;
	private LocationManager lm		= null;
	private int previous_activity = 0;
	private String common_name = "";
	private String science_name = "";
	private int tree_id;
	private int category;
	
	/*
	 * Values.FROM_QC_PHENOPHASE part...
	 */
	private int phenoID;
	private int protocolID;
	private int speciesID;
	private int plantID;
	private int siteID;
	private double latitude;
	private double longitude;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // check if there is any attributes....
	    Intent p_intent = getIntent();
		previous_activity = p_intent.getExtras().getInt("from");
		
		Log.i("K", "Previous_activity : " + previous_activity);
		
		common_name = p_intent.getExtras().getString("cname");
		science_name = p_intent.getExtras().getString("sname");
		
		if(previous_activity == Values.FROM_UCLA_TREE_LISTS || previous_activity == Values.FROM_QUICK_CAPTURE) {
			tree_id = p_intent.getExtras().getInt("tree_id");
		}
		
		if(previous_activity == Values.FROM_LOCAL_PLANT_LISTS) {
			category = p_intent.getExtras().getInt("category");
		}
		
		if(previous_activity == Values.FROM_QC_PHENOPHASE || previous_activity == Values.FROM_PBB_PHENOPHASE) {
			phenoID = p_intent.getExtras().getInt("pheno_id");
			protocolID = p_intent.getExtras().getInt("protocol_id");
			speciesID = p_intent.getExtras().getInt("species_id");
			latitude = p_intent.getExtras().getInt("latitude");
			longitude = p_intent.getExtras().getInt("longitude");
			
			if(previous_activity == Values.FROM_QC_PHENOPHASE) {
				plantID = p_intent.getExtras().getInt("plant_id");
			}
			if(previous_activity == Values.FROM_PBB_PHENOPHASE) {
				siteID = p_intent.getExtras().getInt("site_id");
			}
		}
		
		try {
			File ld = new File(Values.BASE_PATH);
			if(ld.exists()) {
				if (!ld.isDirectory()) {
					// Should probably inform user ... hmm!
					Toast.makeText(QuickCapture.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
					QuickCapture.this.finish();
				}
			}
			else {
				if(!ld.mkdir()) {
					Toast.makeText(QuickCapture.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
					QuickCapture.this.finish();
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
					Uri.fromFile(new File(Values.BASE_PATH, camera_image_id + ".jpg")));
			startActivityForResult(mediaCaptureIntent, PHOTO_CAPTURE_CODE);
			
		}
		catch (Exception e) {
			Log.e("K", e.toString());
		}
	    
	    // TODO Auto-generated method stub
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		
		if(resultCode == Activity.RESULT_CANCELED) {
			if (requestCode == PHOTO_CAPTURE_CODE) {
				new AlertDialog.Builder(QuickCapture.this)
				.setTitle("Quit Camera")
				.setMessage("Make Observation without a photo?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						if(previous_activity != Values.FROM_QC_PHENOPHASE) {
							Intent intent = null;
							
							if(previous_activity == Values.FROM_UCLA_TREE_LISTS) {
								intent = new Intent(QuickCapture.this, GetPhenophase.class);
								intent.putExtra("camera_image_id", "");
								intent.putExtra("from", Values.FROM_UCLA_TREE_LISTS);
								intent.putExtra("cname", common_name);
								intent.putExtra("sname", science_name);
								intent.putExtra("tree_id", tree_id);
							}
							else if(previous_activity == Values.FROM_LOCAL_PLANT_LISTS) {
								intent = new Intent(QuickCapture.this, GetPhenophase.class);
								intent.putExtra("camera_image_id", "");
								intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
								intent.putExtra("cname", common_name);
								intent.putExtra("sname", science_name);
								intent.putExtra("category", category);
							}
							else if(previous_activity == Values.FROM_QUICK_CAPTURE) {
								intent = new Intent(QuickCapture.this, GetPhenophase.class);
								intent.putExtra("camera_image_id", "");
								intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
								intent.putExtra("cname", common_name);
								intent.putExtra("sname", science_name);
								intent.putExtra("tree_id", tree_id);
							}
							
							else if(previous_activity == Values.FROM_QC_PHENOPHASE || previous_activity == Values.FROM_PBB_PHENOPHASE) {
								intent = new Intent(QuickCapture.this, AddNotes.class);
								intent.putExtra("camera_image_id", "");
								intent.putExtra("from", previous_activity);
								intent.putExtra("pheno_id", phenoID);
								intent.putExtra("protocol_id", protocolID);
								intent.putExtra("species_id", speciesID);
								intent.putExtra("cname", common_name);
								intent.putExtra("sname", science_name);
								intent.putExtra("latitude", latitude);
								intent.putExtra("longitude", longitude);
								
								if(previous_activity == Values.FROM_QC_PHENOPHASE) {
									intent.putExtra("plant_id", plantID);
								}
								if(previous_activity == Values.FROM_PBB_PHENOPHASE) {
									intent.putExtra("site_id", siteID);
								}
							}
							
							finish();
							
							startActivity(intent);

						}
						else {
							
						}
						
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.i("K"," No Clicked!");
					    finish();
					}
				})
				.show();
				
			}
		}
		
		if(resultCode == Activity.RESULT_OK) {
			
			if (requestCode == PHOTO_CAPTURE_CODE) {
				Toast.makeText(this, "Photo added!", Toast.LENGTH_SHORT).show();
				
				Log.i("K","CAMERA_IMAGE_ID : " + camera_image_id);
				
				Media media = new Media();

				Bitmap bitmap = media.ShowPhotoTaken(Values.BASE_PATH + camera_image_id + ".jpg");
				
				Intent intent = null;
				
				if(previous_activity == Values.FROM_UCLA_TREE_LISTS) {
					intent = new Intent(QuickCapture.this, GetPhenophase.class);
					intent.putExtra("camera_image_id", camera_image_id);
					intent.putExtra("from", Values.FROM_UCLA_TREE_LISTS);
					intent.putExtra("cname", common_name);
					intent.putExtra("sname", science_name);
					intent.putExtra("tree_id", tree_id);
				}
				
				else if(previous_activity == Values.FROM_LOCAL_PLANT_LISTS) {
					intent = new Intent(QuickCapture.this, GetPhenophase.class);
					intent.putExtra("camera_image_id", camera_image_id);
					intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
					intent.putExtra("cname", common_name);
					intent.putExtra("sname", science_name);
					intent.putExtra("category", category);
				}
				else if(previous_activity == Values.FROM_QUICK_CAPTURE) {
					intent = new Intent(QuickCapture.this, GetPhenophase.class);
					intent.putExtra("camera_image_id", camera_image_id);
					intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
					intent.putExtra("cname", common_name);
					intent.putExtra("sname", science_name);
					intent.putExtra("tree_id", tree_id);
				}
				else if(previous_activity == Values.FROM_QC_PHENOPHASE || previous_activity == Values.FROM_PBB_PHENOPHASE) {
					intent = new Intent(QuickCapture.this, AddNotes.class);
					intent.putExtra("camera_image_id", camera_image_id);
					intent.putExtra("from", previous_activity);
					intent.putExtra("pheno_id", phenoID);
					intent.putExtra("protocol_id", protocolID);
					intent.putExtra("species_id", speciesID);
					intent.putExtra("cname", common_name);
					intent.putExtra("sname", science_name);
					intent.putExtra("latitude", latitude);
					intent.putExtra("longitude", longitude);
					
					if(previous_activity == Values.FROM_QC_PHENOPHASE) {
						intent.putExtra("plant_id", plantID);
					}
					if(previous_activity == Values.FROM_PBB_PHENOPHASE) {
						intent.putExtra("site_id", siteID);
					}
				}
				
				
				finish();
				startActivity(intent);
				
			}
		}			
	}
	
	static private String hexEncode(byte[] aInput){
		StringBuilder result = new StringBuilder();
		char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m'};
		for ( int idx = 0; idx < 5; ++idx) {
			byte b = aInput[idx];
			result.append( digits[ (b&0xf0) >> 4 ] );
			result.append( digits[ b&0x0f] );
		}
		return result.toString();
	}
}
