package cens.ucla.edu.budburst;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.Media;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.onetime.AddNotes;
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

public class PlantObservationSummary extends Activity {

	private String cname = "";
	private String sname = "";
	private String cameraImageID = "";
	private String notes = "";
	
	private int protocolID;
	private int phenoID;
	private int speciesID;
	private int category;
	private int previousActivity;
	private int plantID;
	private int phenoIcon = 0;
	private int siteID;
	
	private double latitude;
	private double longitude;
	
	private String phenoName = null;
	private String photoName = null;
	private String phenoDescription = null;
	
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
	    
		/*
		 * Getting values from the previous activity.
		 */
		cname = p_intent.getExtras().getString("cname");
		sname = p_intent.getExtras().getString("sname");
		cameraImageID = p_intent.getExtras().getString("camera_image_id");
		protocolID = p_intent.getExtras().getInt("protocol_id");
		speciesID = p_intent.getExtras().getInt("species_id");
		phenoID = p_intent.getExtras().getInt("pheno_id");
		latitude = p_intent.getExtras().getDouble("latitude");
		longitude = p_intent.getExtras().getDouble("longitude");
		notes = p_intent.getExtras().getString("notes");
		previousActivity = p_intent.getExtras().getInt("from", 0);
		
		if(previousActivity == Values.FROM_QC_PHENOPHASE) {
	    	plantID = p_intent.getExtras().getInt("plant_id", 0);
	    }
		
		if(previousActivity == Values.FROM_PBB_PHENOPHASE) {
			siteID = p_intent.getExtras().getInt("site_id", 0);
		}
	    
	    
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
		
		/*
		 * Species Image, Name
		 */
		if(speciesID == 0) {
			species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
			species_image.setBackgroundResource(R.drawable.shapedrawable);
		    species_name.setText(cname + " ");
		    science_name.setText(sname + " ");
		}
		else {
		    if(speciesID > 76 || category == Values.TREE_LISTS_QC) {
		    	// check out for the tree_list
		    	if(category == Values.TREE_LISTS_QC) {
		    		String imagePath = Values.TREE_PATH + speciesID + ".jpg";
		    		FunctionsHelper helper = new FunctionsHelper();
		    		species_image.setImageBitmap(helper.showImage(PlantObservationSummary.this, imagePath));
		    	}
		    	else {
		    		species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
		    	}
		    }
		    else {
		    	species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+speciesID, null, null));
		    }
		    species_image.setBackgroundResource(R.drawable.shapedrawable);
		    species_name.setText(cname + " ");
		    science_name.setText(sname + " ");
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
		
		if(previousActivity == Values.FROM_PBB_PHENOPHASE) {
			StaticDBHelper sDBHelper = new StaticDBHelper(PlantObservationSummary.this);
			SQLiteDatabase sDB = sDBHelper.getReadableDatabase();
			
			Cursor getPhenoInfo = sDB.rawQuery("SELECT Phenophase_Icon, Phenophase_Name, description FROM Phenophase_Protocol_Icon WHERE Phenophase_ID=" + phenoID + " AND Protocol_ID=" + protocolID, null);
			
			while(getPhenoInfo.moveToNext()) {
				phenoIcon = getPhenoInfo.getInt(0);
				phenoName = getPhenoInfo.getString(1);
				phenoDescription= getPhenoInfo.getString(2);
			}
			
			sDB.close();
			getPhenoInfo.close();
		}
		
		if(previousActivity == Values.FROM_QC_PHENOPHASE) {
			StaticDBHelper sDBHelper = new StaticDBHelper(PlantObservationSummary.this);
			SQLiteDatabase sDB = sDBHelper.getReadableDatabase();
			
			Cursor getPhenoInfo = sDB.rawQuery("SELECT Phenophase_Icon, Type, Description FROM Onetime_Observation WHERE _id =" + phenoID, null);
			
			while(getPhenoInfo.moveToNext()) {
				phenoIcon = getPhenoInfo.getInt(0);
				phenoName = getPhenoInfo.getString(1);
				phenoDescription= getPhenoInfo.getString(2);
			}
			
			sDB.close();
			getPhenoInfo.close();
		}
		
		
		pheno_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + phenoIcon, null, null));
		pheno_image.setBackgroundResource(R.drawable.shapedrawable);
		phenoNameText.setText(phenoName);
		phenoDescriptionTxt.setText(phenoDescription);
		noteTxt.setText(notes);

		/*
		 * Check the sd card and the folder if existed.
		 */
	    dict_tmp = new File(Environment.getExternalStorageDirectory(), "pbudburst/pbb/");
	    if(!dict_tmp.exists()) {
	    	dict_tmp.mkdir();
	    }
	    
	    take_photo.setVisibility(View.VISIBLE);
	    replace_photo.setVisibility(View.GONE);

		
	    File file = new File(Values.BASE_PATH + cameraImageID + ".jpg");
		
		if(file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(Values.BASE_PATH + cameraImageID + ".jpg");
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
	    species_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PlantObservationSummary.this, SpeciesDetail.class);
				intent.putExtra("id", speciesID);
				intent.putExtra("site_id", "");
				intent.putExtra("category", category);
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
				Intent intent = new Intent(PlantObservationSummary.this, PhenophaseDetail.class);
				if(previousActivity == Values.FROM_PBB_PHENOPHASE) {
					intent.putExtra("from", Values.FROM_PBB_PHENOPHASE);
					intent.putExtra("protocol_id", protocolID);
				}
				else {
					intent.putExtra("from", Values.FROM_QC_PHENOPHASE);
				}
				
				intent.putExtra("id", phenoID);
				startActivity(intent);
			}
		});
	    
	    /*
		 * When click photo image, move to the detail view.
		 */
	    photo_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				photo_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
				// TODO Auto-generated method stub
				final RelativeLayout linear = (RelativeLayout) View.inflate(PlantObservationSummary.this, R.layout.image_popup, null);
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(PlantObservationSummary.this);
				ImageView image_view = (ImageView) linear.findViewById(R.id.image_btn);
				
			    String imagePath = Values.BASE_PATH + cameraImageID + ".jpg";

			    File file = new File(imagePath);
			    Bitmap bitmap = null;
			    
			    // if file exists show the photo on the ImageButton
			    if(file.exists()) {
			    	imagePath = Values.BASE_PATH + cameraImageID + ".jpg";
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
					File file = new File(Values.BASE_PATH);
					if (file.exists()) {
						if(!file.isDirectory()) {
							Toast.makeText(PlantObservationSummary.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
							finish();
						}
					}
					else {
						if (!file.mkdir()) {
							Toast.makeText(PlantObservationSummary.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
							finish();
						}
					}
					
					try {
						SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
						String randomNum = new Integer(prng.nextInt()).toString();
						MessageDigest sha = MessageDigest.getInstance("SHA-1");
						byte[] result = sha.digest(randomNum.getBytes());
						cameraImageID = hexEncode(result);
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					
					Intent mediaCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					mediaCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
							Uri.fromFile(new File(Values.BASE_PATH, cameraImageID + ".jpg")));
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
				File ld = new File(Values.BASE_PATH);
				if (ld.exists()) {
					if (!ld.isDirectory()) {
						// Should probably inform user ... hmm!
						Toast.makeText(PlantObservationSummary.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
						PlantObservationSummary.this.finish();
					}
				} else {
					if (!ld.mkdir()) {
						Toast.makeText(PlantObservationSummary.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
						PlantObservationSummary.this.finish();
					}
				}

				try {
					SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
					String randomNum = new Integer(prng.nextInt()).toString();
					MessageDigest sha = MessageDigest.getInstance("SHA-1");
					byte[] result = sha.digest(randomNum.getBytes());
					cameraImageID = hexEncode(result);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Intent mediaCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				mediaCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
						Uri.fromFile(new File(Values.BASE_PATH, cameraImageID + ".jpg")));
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
					
					Log.i("K", "PREVIOUS ACTIVITY : " + previousActivity);
					
					if(previousActivity == Values.FROM_PBB_PHENOPHASE) {
						addSpeciesFromPlantlist();
					}
					else if(previousActivity == Values.FROM_QC_PHENOPHASE) {
						addSpeciesFromQuickcapture();
					}
					else {
						// nothing is here.
					}
					
					Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
					vibrator.vibrate(500);
					
					Intent intent = new Intent(PlantObservationSummary.this, PlantList.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					
				}catch(Exception e){
					Log.e("K", e.toString());
				}
			}
		});
	    // TODO Auto-generated method stub
	}

	/*
	 * Add Monitored Plant Observation into the database.
	 */
	public void addSpeciesFromPlantlist() {
		SyncDBHelper syncDBHelper = new SyncDBHelper(PlantObservationSummary.this);

		//SQLiteDatabase r_db = syncDBHelper.getReadableDatabase();
		
		//String find_species = "SELECT _id FROM my_observation WHERE phenophase_id=" 
		//				+ phenoID + " AND species_id=" + speciesID + " AND site_id=" + siteID;
		//Log.i("K", "QUERY : " + find_species);
		//Cursor cursor = r_db.rawQuery(find_species, null);
		//cursor.moveToNext();
		
		//int count = cursor.getCount();
		//Log.i("K", "COUNT : " + count);

		//r_db.close();
		
		SQLiteDatabase db = syncDBHelper.getWritableDatabase();
		String query;
		
		String dt_taken = new SimpleDateFormat("dd MMMMM yyy").format(new Date());
		
		//INSERT INTO my_observation VALUES (null,56,3339,24,'3224193362','28 December 2010','ppp',9);
		//if(count == 0){
			query = "INSERT INTO my_observation VALUES (" +
					"null," +
					speciesID + "," +
					siteID + "," +
					phenoID+"," +
					"'" + cameraImageID + "'," +
					"'" + dt_taken + "'," +
					"'" + noteTxt.getText().toString() + "'," +
					SyncDBHelper.SYNCED_NO + ");";
		//}else{
		//	int c_id = cursor.getInt(0);
		//	Log.i("K", "C_ID : " + c_id);
			
			
		//	query = "UPDATE my_observation SET " +
		//			"image_id='" + camera_image_id + "'," +
		//			"time='" + dt_taken + "'," +
		//			"note='" + notes.getText().toString() + "'" + "," +
		//			"synced=" + SyncDBHelper.SYNCED_NO + " " + 
		//			"WHERE _id=" + c_id + ";"; 
			
			//Toast.makeText(PlantObservationSummary.this, getString(R.string.PlantInfo_successUpdate), Toast.LENGTH_SHORT).show();
		//}
		//cursor.close();
		//Log.i("K", "QUERY : " + query);
		db.execSQL(query);
		db.close();
		syncDBHelper.close();
		
		
		Toast.makeText(PlantObservationSummary.this, getString(R.string.PlantInfo_successAdded), Toast.LENGTH_SHORT).show();


	}
	
	/*
	 * Add Quick Shared Observation into the database.
	 */
	public void addSpeciesFromQuickcapture() {
		FunctionsHelper helper = new FunctionsHelper();
		helper.insertNewObservation(PlantObservationSummary.this, plantID, phenoID,
				latitude, longitude, 0, cameraImageID, noteTxt.getText().toString());

		Toast.makeText(PlantObservationSummary.this, getString(R.string.QuickCapture_Added), Toast.LENGTH_SHORT).show();

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
				
				String imagePath = Values.BASE_PATH + cameraImageID + ".jpg";
				
				Media media = new Media();
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






