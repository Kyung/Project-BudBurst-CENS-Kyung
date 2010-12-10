package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.Media;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	private static GpsListener gpsListener;
	private LocationManager lm		= null;
	private double latitude 		= 0.0;
	private double longitude 		= 0.0;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    gpsListener = new GpsListener();
	    lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    // set update the location data in 3secs or 30meters
	    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 30, gpsListener);
	 
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		Log.d("K", "onActivityResult");
		
		if(resultCode == Activity.RESULT_CANCELED) {
			if (requestCode == PHOTO_CAPTURE_CODE) {
				new AlertDialog.Builder(QuickCapture.this)
				.setTitle("Quit Camera")
				.setMessage("Make Observation without photo?")
				.setIcon(R.drawable.pbbicon_small)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String currentDateTimeString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
						
						Intent intent = new Intent(QuickCapture.this, GetPhenophase.class);
						intent.putExtra("camera_image_id", "");
						intent.putExtra("latitude", latitude);
						intent.putExtra("longitude", longitude);
						intent.putExtra("dt_taken", currentDateTimeString);
	
						finish();
						
						startActivity(intent);
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
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
				
				String currentDateTimeString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
				
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
				intent.putExtra("dt_taken", currentDateTimeString);
				
				finish();
				startActivity(intent);
				
			}
		}			
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
