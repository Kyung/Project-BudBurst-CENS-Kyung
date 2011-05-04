package cens.ucla.edu.budburst.utils;

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

import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
import cens.ucla.edu.budburst.helper.HelperMedia;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.myplants.PBBAddNotes;
import cens.ucla.edu.budburst.myplants.PBBAddSite;
import cens.ucla.edu.budburst.onetime.OneTimeMainPage;
import cens.ucla.edu.budburst.onetime.OneTimePhenophase;
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
	private String mCameraImageID;
	private String mCommonName;
	private String mScienceName;
	private String mImageID;
	
	private int mPreviousActivity;
	private int mSpeciesID;
	private int mCategory;
	
	
	/*
	 * Values.FROM_QC_PHENOPHASE part...
	 */
	private int mPhenoID;
	private int mProtocolID;
	private int mPlantID;
	private int mSiteID;
	private double mLatitude;
	private double mLongitude;
	
	private PBBItems pbbItem;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // check if there is any attributes....
	    loadPreviousActivity();
	    // check sd card
	    checkSDCard();
	    // TODO Auto-generated method stub
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	private void loadPreviousActivity() {
		//Intent p_intent = getIntent();
		
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		
		mPreviousActivity = bundle.getInt("from");
		
		Log.i("K", "Previous_activity : " + mPreviousActivity);
		
		mCommonName = pbbItem.getCommonName();
		mScienceName = pbbItem.getScienceName();
		mCategory = pbbItem.getCategory();
		mSpeciesID = pbbItem.getSpeciesID();
		mProtocolID = pbbItem.getProtocolID();
		
		
		if(mPreviousActivity == HelperValues.FROM_LOCAL_PLANT_LISTS ) {
			mImageID = bundle.getString("image_id");
		}
		if(mPreviousActivity == HelperValues.FROM_PLANT_LIST_ADD_SAMESPECIES
						|| mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE_ADD_SAMESPECIES) {
			mPlantID = pbbItem.getSpeciesID();
		}
		if(mPreviousActivity == HelperValues.FROM_QC_PHENOPHASE 
				|| mPreviousActivity == HelperValues.FROM_PBB_PHENOPHASE) {
		
			mPhenoID = pbbItem.getPhenophaseID();
			mPlantID = pbbItem.getSpeciesID();
			mLatitude = pbbItem.getLatitude();
			mLongitude = pbbItem.getLongitude();
			mPlantID = pbbItem.getPlantID();
			mSiteID = pbbItem.getSiteID();
		}
	}
	
	private void checkSDCard() {
		try {
			File ld = new File(HelperValues.BASE_PATH);
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
				mCameraImageID = hexEncode(result);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			Intent mediaCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			mediaCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
					Uri.fromFile(new File(HelperValues.BASE_PATH, mCameraImageID + ".jpg")));
			startActivityForResult(mediaCaptureIntent, PHOTO_CAPTURE_CODE);
			
		}
		catch (Exception e) {
			Log.e("K", e.toString());
		}

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
	
		if(resultCode == Activity.RESULT_OK) {
			
			if (requestCode == PHOTO_CAPTURE_CODE) {
				Toast.makeText(this, "Photo added!", Toast.LENGTH_SHORT).show();
				
				Log.i("K","CAMERA_IMAGE_ID : " + mCameraImageID);
				
				
				HelperMedia media = new HelperMedia();

				Bitmap bitmap = media.ShowPhotoTaken(HelperValues.BASE_PATH + mCameraImageID + ".jpg");
				
				Intent intent = null;
				
				pbbItem.setScienceName(mScienceName);
				pbbItem.setCommonName(mCommonName);
				pbbItem.setCategory(mCategory);
				pbbItem.setSpeciesID(mSpeciesID);
				pbbItem.setLocalImageName(mCameraImageID);
				pbbItem.setProtocolID(mProtocolID);
				
				
				if(mPreviousActivity == HelperValues.FROM_USER_DEFINED_LISTS) {
					intent = new Intent(QuickCapture.this, OneTimePhenophase.class);
					intent.putExtra("pbbItem", pbbItem);
					
					intent.putExtra("from", HelperValues.FROM_USER_DEFINED_LISTS);
					
				}
				else if(mPreviousActivity == HelperValues.FROM_LOCAL_PLANT_LISTS) {
					intent = new Intent(QuickCapture.this, OneTimePhenophase.class);
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("image_id", mImageID);
					
					intent.putExtra("from", mPreviousActivity);
					
				}
				else if(mPreviousActivity == HelperValues.FROM_PLANT_LIST_ADD_SAMESPECIES
						|| mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE_ADD_SAMESPECIES) {
					intent = new Intent(QuickCapture.this, OneTimePhenophase.class);
					intent.putExtra("pbbItem", pbbItem);
					
					intent.putExtra("from", mPreviousActivity);
				}
				else if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE) {
					intent = new Intent(QuickCapture.this, OneTimeMainPage.class);
					intent.putExtra("pbbItem", pbbItem);
					
					intent.putExtra("from", HelperValues.FROM_QUICK_CAPTURE);
				}
				else if(mPreviousActivity == HelperValues.FROM_QC_PHENOPHASE 
						|| mPreviousActivity == HelperValues.FROM_PBB_PHENOPHASE) {
					intent = new Intent(QuickCapture.this, PBBAddNotes.class);
					pbbItem.setPhenophaseID(mPhenoID);
					pbbItem.setLatitude(mLatitude);
					pbbItem.setLongitude(mLongitude);
					pbbItem.setSiteID(mSiteID);
					pbbItem.setPlantID(mPlantID);
					intent.putExtra("pbbItem", pbbItem);
					
					intent.putExtra("from", mPreviousActivity);
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
