package cens.ucla.edu.budburst.onetime;

import java.io.File;
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
	public final String BASE_PATH = "/sdcard/pbudburst/";
	private String camera_image_id 	= null;
	private SharedPreferences pref;
	private LocationManager lm		= null;
	private double latitude 		= 0.0;
	private double longitude 		= 0.0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // start BackgroundService
	    Intent service = new Intent(QuickCapture.this, BackgroundService.class);
	    startService(service);
	    
	    pref = getSharedPreferences("userinfo", 0);
	    latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
	    longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
	    
	 
		try {
			File ld = new File(BASE_PATH);
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
					Uri.fromFile(new File(BASE_PATH, camera_image_id + ".jpg")));
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
		Log.d("K", "onActivityResult");
		
		if(resultCode == Activity.RESULT_CANCELED) {
			if (requestCode == PHOTO_CAPTURE_CODE) {
				new AlertDialog.Builder(QuickCapture.this)
				.setTitle("Quit Camera")
				.setMessage("Make Observation without a photo?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						Intent intent = new Intent(QuickCapture.this, GetPhenophase.class);
						intent.putExtra("camera_image_id", "");
						intent.putExtra("latitude", latitude);
						intent.putExtra("longitude", longitude);
						
						finish();
						
						startActivity(intent);
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.i("K"," No Clicked!");
						Intent service = new Intent(QuickCapture.this, BackgroundService.class);
					    stopService(service);
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
				
				if(camera_image_id.equals(null)) {
					Toast.makeText(QuickCapture.this, "Please start again", Toast.LENGTH_SHORT).show();
					finish();
				}
				
				Bitmap bitmap = media.ShowPhotoTaken(BASE_PATH + camera_image_id + ".jpg");
				
				Intent intent = new Intent(QuickCapture.this, GetPhenophase.class);
				intent.putExtra("camera_image_id", camera_image_id);
				intent.putExtra("latitude", latitude);
				intent.putExtra("longitude", longitude);
				
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
