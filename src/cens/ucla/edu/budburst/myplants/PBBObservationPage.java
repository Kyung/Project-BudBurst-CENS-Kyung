package cens.ucla.edu.budburst.myplants;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.R.string;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperMedia;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.utils.PBBItems;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PBBObservationPage extends Activity {

	private String mCommonName;
	private String mScienceName;
	private String mCameraImageID;
	private String mNotes;
	
	private int mProtocolID;
	private int mPhenoID;
	private int mSpeciesID;
	private int mCategory;
	private int mPreviousActivity;
	private int mPlantID;
	private int mPhenoIcon = 0;
	private int mSiteID;
	
	private double mLatitude;
	private double mLongitude;
	
	private String mPhenoName = null;
	private String mPhotoName = null;
	private String mPhenoDescription = null;
	
	private HelperFunctionCalls mHelper;
	
	/*
	 * Layout Components
	 */
	private File dict_tmp 			= null;
	private Button saveBtn 			= null;
	private Button siteBtn			= null;
	private EditText noteTxt		= null;
	private View take_photo 		= null;
	private View replace_photo 		= null;
	private ImageView photo_image 	= null;
	
	
	private PBBItems pbbItem;
	/*
	 * Variables
	 */
	protected static final int PHOTO_CAPTURE_CODE = 0;
	protected static final int GET_SUMMARY_CODE = 1;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.plantinformation);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);
		
		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.Observation_Summary));
		Intent p_intent = getIntent();
		
		mHelper = new HelperFunctionCalls();
	    
		/*
		 * Getting values from the previous activity.
		 */
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		mCommonName = pbbItem.getCommonName();
		mScienceName = pbbItem.getScienceName();
		mCameraImageID = pbbItem.getCameraImageName();
		mProtocolID = pbbItem.getProtocolID();
		mSpeciesID = pbbItem.getSpeciesID();
		mPhenoID = pbbItem.getPhenophaseID();
		mLatitude = pbbItem.getLatitude();
		mLongitude = pbbItem.getLongitude();
		mCategory = pbbItem.getCategory();
		mPlantID = pbbItem.getPlantID();
		mSiteID = pbbItem.getSiteID();
		mNotes = pbbItem.getNote();
		
		mPreviousActivity = p_intent.getExtras().getInt("from", 0);
		
		Log.i("K", "PlantObservationSummary(mCategory) : " + mCategory + ", mSpeciesID: " + mSpeciesID);
	    /*
	     * Set the layout components
	     */
		ImageView species_image = (ImageView) findViewById(R.id.species_image);
	    TextView species_name = (TextView) findViewById(R.id.species_name);
	    TextView science_name = (TextView) findViewById(R.id.science_name);
		ImageView pheno_image = (ImageView) findViewById(R.id.pheno_image);
		TextView phenoDescriptionTxt = (TextView) findViewById(R.id.pheno_text);
		TextView phenoNameText = (TextView) findViewById(R.id.pheno_name);
		
		
		photo_image = (ImageView) findViewById(R.id.image);
		photo_image.setVisibility(View.VISIBLE);
		
		species_name.setText(mCommonName);
		science_name.setText(mScienceName);
		
		// species_image view
		// should be dealt differently by category
		species_image.setVisibility(View.VISIBLE);
		if(mPreviousActivity == HelperValues.FROM_PBB_PHENOPHASE) {
			mHelper.showSpeciesThumbNailObserver(this, mCategory, mSpeciesID, mScienceName, species_image);
		}
		else {
			mHelper.showSpeciesThumbNail(this, mCategory, mSpeciesID, mScienceName, species_image);
		}

		
		/*
		 *  Set xml
		 */
		noteTxt = (EditText) findViewById(R.id.notes);
		take_photo = this.findViewById(R.id.take_photo);
		replace_photo = this.findViewById(R.id.replace_photo);
		
		/*
		 * Retrieve Phenophase information from the table.
		 * - This is different from the previous activity.
		 *   - 1. PBB Phenophase
		 *   - 2. Quick Share Phenophase
		 */
		if(mPreviousActivity == HelperValues.FROM_PBB_PHENOPHASE) {
			StaticDBHelper sDBHelper = new StaticDBHelper(PBBObservationPage.this);
			SQLiteDatabase sDB = sDBHelper.getReadableDatabase();
			
			Cursor getPhenoInfo = sDB.rawQuery("SELECT Phenophase_Icon, Phenophase_Name, description FROM Phenophase_Protocol_Icon WHERE Phenophase_ID=" + mPhenoID + " AND Protocol_ID=" + mProtocolID, null);
			
			while(getPhenoInfo.moveToNext()) {
				mPhenoIcon = getPhenoInfo.getInt(0);
				mPhenoName = getPhenoInfo.getString(1);
				mPhenoDescription= getPhenoInfo.getString(2);
			}
			
			sDB.close();
			getPhenoInfo.close();
		}
		
		if(mPreviousActivity == HelperValues.FROM_QC_PHENOPHASE) {
			StaticDBHelper sDBHelper = new StaticDBHelper(PBBObservationPage.this);
			SQLiteDatabase sDB = sDBHelper.getReadableDatabase();
			
			Cursor getPhenoInfo = sDB.rawQuery("SELECT Phenophase_Icon, Type, Description FROM Onetime_Observation WHERE _id =" + mPhenoID, null);
			
			while(getPhenoInfo.moveToNext()) {
				mPhenoIcon = getPhenoInfo.getInt(0);
				mPhenoName = getPhenoInfo.getString(1);
				mPhenoDescription= getPhenoInfo.getString(2);
			}
			
			sDB.close();
			getPhenoInfo.close();
		}
		
		
		pheno_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + mPhenoIcon, null, null));
		pheno_image.setBackgroundResource(R.drawable.shapedrawable);
		phenoNameText.setText(mPhenoName);
		phenoDescriptionTxt.setText(mPhenoDescription);
		noteTxt.setText(mNotes);

		/*
		 * Check the sd card and the folder if existed.
		 */
	    dict_tmp = new File(Environment.getExternalStorageDirectory(), "pbudburst/pbb/");
	    if(!dict_tmp.exists()) {
	    	dict_tmp.mkdir();
	    }
	    
	    take_photo.setVisibility(View.VISIBLE);
	    replace_photo.setVisibility(View.GONE);

		
	    File file = new File(HelperValues.BASE_PATH + mCameraImageID + ".jpg");
		
		if(file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(HelperValues.BASE_PATH + mCameraImageID + ".jpg");
			photo_image.setImageBitmap(bitmap);
			take_photo.setVisibility(View.GONE);
		    replace_photo.setVisibility(View.VISIBLE);
		    photo_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
		}
		else {
			photo_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo", null, null));
			photo_image.setEnabled(false);
			take_photo.setVisibility(View.VISIBLE);
		    replace_photo.setVisibility(View.GONE);
		}
	    
		/*
		 * When click species image, move to the detail view.
		 */
		species_image.setBackgroundResource(R.drawable.shapedrawable);
	    species_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PBBObservationPage.this, DetailPlantInfo.class);
				intent.putExtra("pbbItem", pbbItem);
				startActivity(intent);
			}
		});
	    
	    /*
		 * When click phenophase image, move to the detail view.
		 */
	    pheno_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PBBObservationPage.this, PBBPhenophaseInfo.class);
				if(mPreviousActivity == HelperValues.FROM_PBB_PHENOPHASE) {
					intent.putExtra("from", HelperValues.FROM_PBB_PHENOPHASE);
					intent.putExtra("protocol_id", mProtocolID);
				}
				else {
					intent.putExtra("from", HelperValues.FROM_QC_PHENOPHASE);
				}
				
				intent.putExtra("id", mPhenoID);
				startActivity(intent);
			}
		});
	    

	    // TODO Auto-generated method stub
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
	    /*
		 * When click photo image, move to the detail view.
		 */
	    photo_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				photo_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
				// TODO Auto-generated method stub
				final RelativeLayout linear = (RelativeLayout) View.inflate(PBBObservationPage.this, R.layout.image_popup, null);
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(PBBObservationPage.this);
				ImageView image_view = (ImageView) linear.findViewById(R.id.main_image);
				
			    String imagePath = HelperValues.BASE_PATH + mCameraImageID + ".jpg";

			    File file = new File(imagePath);
			    Bitmap bitmap = null;
			    
			    // if file exists show the photo on the ImageButton
			    if(file.exists()) {
			    	imagePath = HelperValues.BASE_PATH + mCameraImageID + ".jpg";
				   	bitmap = BitmapFactory.decodeFile(imagePath);
				   	image_view.setImageBitmap(bitmap);
			    }
			    // if not, show 'no image' ImageButton
			    else {
			    	image_view.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo", null, null));
			    }
			    
			    // when press 'Back', close the dialog
				dialog.setPositiveButton(getString(R.string.Button_back), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub	
					}
				});
		        dialog.setView(linear);
		        dialog.show();
			}
		});
	    
	    /*
	     *  When click take_photo button
	     */
		take_photo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					File file = new File(HelperValues.BASE_PATH);
					if (file.exists()) {
						if(!file.isDirectory()) {
							Toast.makeText(PBBObservationPage.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
							finish();
						}
					}
					else {
						if (!file.mkdir()) {
							Toast.makeText(PBBObservationPage.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
							finish();
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
				catch(Exception e) {
					Log.e("K", e.toString());
				}
			}
		});
	    
		
		/*
		 * Take replace button
		 */
		replace_photo.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				File ld = new File(HelperValues.BASE_PATH);
				if (ld.exists()) {
					if (!ld.isDirectory()) {
						// Should probably inform user ... hmm!
						Toast.makeText(PBBObservationPage.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
						PBBObservationPage.this.finish();
					}
				} else {
					if (!ld.mkdir()) {
						Toast.makeText(PBBObservationPage.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
						PBBObservationPage.this.finish();
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
		});
		
		/*
		 * When click save button
		 * - acts differently based on the previous activity. 
		 * - 1. My Plant Observation
		 * - 2. My Shared Plant Observation
		 */
		saveBtn = (Button) findViewById(R.id.save_changes);
		saveBtn.setText(getString(R.string.Button_save));
		
		saveBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
					
					Log.i("K", "PREVIOUS ACTIVITY : " + mPreviousActivity);
					
					if(mPreviousActivity == HelperValues.FROM_PBB_PHENOPHASE) {
						addSpeciesFromPlantlist();
					}
					else if(mPreviousActivity == HelperValues.FROM_QC_PHENOPHASE) {
						addSpeciesFromQuickcapture();
					}
					else {
						// nothing is here.
					}
					
					Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
					vibrator.vibrate(500);
					
					Intent intent = new Intent(PBBObservationPage.this, PBBPlantList.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					
				}catch(Exception e){
					Log.e("K", e.toString());
				}
			}
		});
		
		
	}

	/*
	 * Add Monitored Plant Observation into the database.
	 */
	public void addSpeciesFromPlantlist() {
		SyncDBHelper syncDBHelper = new SyncDBHelper(PBBObservationPage.this);

		SQLiteDatabase db = syncDBHelper.getWritableDatabase();
		String dt_taken = new SimpleDateFormat("dd MMMMM yyy").format(new Date());
		
		String query = "INSERT INTO my_observation VALUES (" +
		"null," +
		mSpeciesID + "," +
		mSiteID + "," +
		mPhenoID + "," +
		"'" + mCameraImageID + "'," +
		"'" + dt_taken + "'," +
		"'" + noteTxt.getText().toString() + "'," +
		SyncDBHelper.SYNCED_NO + ");";
			
		db.execSQL(query);
		db.close();
		syncDBHelper.close();
		
		
		Toast.makeText(PBBObservationPage.this, getString(R.string.PlantInfo_successAdded), Toast.LENGTH_SHORT).show();


	}
	
	/*
	 * Add Quick Shared Observation into the database.
	 */
	public void addSpeciesFromQuickcapture() {
		
		mHelper.insertNewObservation(PBBObservationPage.this, mPlantID, mPhenoID,
				mLatitude, mLongitude, 0, mCameraImageID, noteTxt.getText().toString());

		Toast.makeText(PBBObservationPage.this, getString(R.string.QuickCapture_Added), Toast.LENGTH_SHORT).show();

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*
		 *  You can use the requestCode to select between multiple child
		 *  activities you may have started. Here there is only one thing
		 *  we launch.
		 */
		
		if(resultCode == Activity.RESULT_OK) {
			
			if (requestCode == PHOTO_CAPTURE_CODE) {
				
				String imagePath = HelperValues.BASE_PATH + mCameraImageID + ".jpg";
				
				HelperMedia media = new HelperMedia();
				photo_image.setImageBitmap(media.ShowPhotoTaken(imagePath));
				photo_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
				photo_image.setEnabled(true);
				
				Toast.makeText(this, getString(R.string.PlantInfo_photoAdded), Toast.LENGTH_SHORT).show();
				
				photo_image.setVisibility(View.VISIBLE);
				take_photo.setVisibility(View.GONE);
			    replace_photo.setVisibility(View.VISIBLE);
			}
		}			
	}
	
	
	/*
	 * To generate complex value not to be identical.
	 */
	static private String hexEncode( byte[] aInput){
		StringBuilder result = new StringBuilder();
		char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m'};
		for ( int idx = 0; idx < 5; ++idx) {
			byte b = aInput[idx];
			result.append( digits[ (b&0xf0) >> 5 ] );
			result.append( digits[ b&0x0f] );
		}
		return result.toString();
	}
}






