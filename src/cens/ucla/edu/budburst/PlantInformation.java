package cens.ucla.edu.budburst;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.GetSpeciesInfo;
import cens.ucla.edu.budburst.onetime.Queue;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlantInformation extends Activity {

	private int phenophase_id = 0;
	private int species_id 	= 0;
	private int site_id 	= 0;
	private int stage_id 	= 0;
	private int protocol_id = 0;
	private String pheno_name = null;
	private String photo_name	= null;
	private String pheno_text = null;
	private String time 	= null;
	private String cname	= null;
	private String sname	= null;
	private String note		= null;
	private File dict_tmp 			= null;
	private String camera_image_id 	= null;
	private Button saveBtn 			= null;
	private EditText notes			= null;
	private String dt_taken			= null;
	private TextView photo_des		= null;
	private int p_id;
	private String p_text 			= null;
	private View take_photo 		= null;
	private View replace_photo 		= null;
	private Bitmap bitmap 			= null;
	private ImageButton photo_image 		= null;
	protected static final int PHOTO_CAPTURE_CODE = 0;
	protected static final int GET_SUMMARY_CODE = 1;
	public final String BASE_PATH = "/sdcard/pbudburst/";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.plantinformation);
	    
	    Intent p_intent = getIntent();
		phenophase_id = p_intent.getExtras().getInt("pheno_id",0);
		species_id = p_intent.getExtras().getInt("species_id",0);
		protocol_id = p_intent.getExtras().getInt("protocol_id", 0);
		pheno_text = p_intent.getExtras().getString("pheno_text");
		photo_name = p_intent.getExtras().getString("photo_name");
		pheno_name = p_intent.getExtras().getString("pheno_name");
		site_id = p_intent.getExtras().getInt("site_id",0);
		cname = p_intent.getExtras().getString("cname");
		sname = p_intent.getExtras().getString("sname");
		dt_taken = p_intent.getExtras().getString("dt_taken");
		note = p_intent.getExtras().getString("notes");
		
		Log.i("K", "pheno_ID : " + phenophase_id + " IMAGE_ID : " + photo_name + " SPECIES ID : " + species_id
					+ "PROTOCOL_ID : " + protocol_id);
		Log.i("K", "SITE_ID : " + site_id + " , SPECIES_ID : " + species_id );
		
		camera_image_id = photo_name;
		
		// set xml
		notes = (EditText) findViewById(R.id.notes);
		take_photo = this.findViewById(R.id.take_photo);
		replace_photo = this.findViewById(R.id.replace_photo);
		photo_des = (TextView) findViewById(R.id.photo_description);
		
		// show pheno_image and pheno_text
		ImageView phenoImg = (ImageView) findViewById(R.id.pheno_image);
		TextView phenoTxt = (TextView) findViewById(R.id.pheno_text);
		phenoImg.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + phenophase_id, null, null));
		phenoImg.setBackgroundResource(R.drawable.shapedrawable);
		phenoTxt.setText(pheno_text);
		notes.setText(note);
		
		photo_image = (ImageButton) findViewById(R.id.image);
		photo_image.setVisibility(View.VISIBLE);
		
	    dict_tmp = new File(Environment.getExternalStorageDirectory(), "pbudburst/pbb/");
	    if(!dict_tmp.exists()) {
	    	dict_tmp.mkdir();
	    }
	    
	    File file_size = new File(BASE_PATH + camera_image_id + ".jpg");
	    take_photo.setVisibility(View.VISIBLE);
	    replace_photo.setVisibility(View.GONE);

	    // if there's a photo in the table show that with replace_photo_button
	    if(file_size.exists()) {
	    	ShowPhotoTaken(BASE_PATH + camera_image_id + ".jpg", photo_image);
	    	Log.i("K", BASE_PATH + camera_image_id + ".jpg");
	    	
	    	take_photo.setVisibility(View.GONE);
		    replace_photo.setVisibility(View.VISIBLE);
	    }
	    else {
	    	photo_image.setVisibility(View.GONE);
	    	photo_des.setText("Add photo for this phenophase.");
	    	//photo_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo_small", null, null));
	    }
	    
	    photo_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				photo_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
				// TODO Auto-generated method stub
				final LinearLayout linear = (LinearLayout) View.inflate(PlantInformation.this, R.layout.image_popup, null);
				
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(PlantInformation.this);
				ImageView image_view = (ImageView) linear.findViewById(R.id.image_btn);
				
			    String imagePath = "/sdcard/pbudburst/" + camera_image_id + ".jpg";

			    File file = new File(imagePath);
			    Bitmap bitmap = null;
			    
			    // if file exists show the photo on the ImageButton
			    if(file.exists()) {
			    	imagePath = "/sdcard/pbudburst/" + camera_image_id + ".jpg";
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
						photo_image.setBackgroundResource(R.drawable.shapedrawable);
					}
				});
		        dialog.setView(linear);
		        dialog.show();
			}
		});
	    
	    // when click take_photo button
		take_photo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					File file = new File(BASE_PATH);
					if (file.exists()) {
						if(!file.isDirectory()) {
							Toast.makeText(PlantInformation.this, "Error: Please check your sdcard", Toast.LENGTH_SHORT).show();
							finish();
						}
					}
					else {
						if (!file.mkdir()) {
							Toast.makeText(PlantInformation.this, "Error: Please check your sdcard", Toast.LENGTH_SHORT).show();
							finish();
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
				catch(Exception e) {
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
						Toast.makeText(PlantInformation.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
						PlantInformation.this.finish();
					}
				} else {
					if (!ld.mkdir()) {
						Toast.makeText(PlantInformation.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
						PlantInformation.this.finish();
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
		});
		
		saveBtn = (Button) findViewById(R.id.save_changes);
		saveBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{

					SyncDBHelper syncDBHelper = new SyncDBHelper(PlantInformation.this);

					SQLiteDatabase r_db = syncDBHelper.getReadableDatabase();
					
					String find_species = "SELECT _id FROM my_observation WHERE phenophase_id=" 
									+ protocol_id + " AND species_id=" + species_id + " AND site_id=" + site_id;
					Log.i("K", "QUERY : " + find_species);
					Cursor cursor = r_db.rawQuery(find_species, null);
					cursor.moveToNext();
					
					int count = cursor.getCount();
					Log.i("K", "COUNT : " + count);
		
					r_db.close();
					
					SQLiteDatabase db = syncDBHelper.getWritableDatabase();
					String query;
					
					dt_taken = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
					
					//INSERT INTO my_observation VALUES (null,56,3339,24,'3224193362','2010-09-16 04:08:35','ppp',9);
					if(count == 0){
						query = "INSERT INTO my_observation VALUES (" +
								"null," +
								species_id + "," +
								site_id + "," +
								protocol_id+"," +
								"'" + camera_image_id + "'," +
								"'" + dt_taken + "'," +
								"'" + notes.getText().toString() + "'," +
								SyncDBHelper.SYNCED_NO + ");";
						Toast.makeText(PlantInformation.this, "Successfully Added", Toast.LENGTH_SHORT).show();
					}else{
						int c_id = cursor.getInt(0);
						Log.i("K", "C_ID : " + c_id);
						
						query = "UPDATE my_observation SET " +
								"image_id='" + camera_image_id + "'," +
								"time='" + dt_taken + "'," +
								"note='" + notes.getText().toString() + "'" + "," +
								"synced=" + SyncDBHelper.SYNCED_NO + " " + 
								"WHERE _id=" + c_id + ";"; 
						
						Toast.makeText(PlantInformation.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
					}
					cursor.close();
					Log.i("K", "QUERY : " + query);
					db.execSQL(query);
					db.close();
					syncDBHelper.close();
				}catch(Exception e){
					Log.e("K", e.toString());
				}

				Intent intent = getIntent();
				setResult(RESULT_OK, intent);
				finish();	
				
			}
		});
		
	    // TODO Auto-generated method stub
	}
	
	
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		Log.d("K", "onActivityResult");
		
		if(resultCode == Activity.RESULT_OK) {
			
			if (requestCode == PHOTO_CAPTURE_CODE) {
				
				String imagePath = BASE_PATH + camera_image_id + ".jpg";
				
				Log.i("K", "IMAGE PATH : " + imagePath);
				
				ShowPhotoTaken(imagePath, photo_image);
				
				Toast.makeText(this, "Photo added!", Toast.LENGTH_SHORT).show();
				
				
				photo_des.setText("Tap the photo to enlarge.");
				photo_image.setVisibility(View.VISIBLE);
				take_photo.setVisibility(View.GONE);
			    replace_photo.setVisibility(View.VISIBLE);
			}
			if (requestCode == GET_SUMMARY_CODE) {
			
				Intent intent = getIntent();
				setResult(RESULT_OK, intent);
				finish();
			}
		}			
	}
	
	public void ShowPhotoTaken(String imagePath, ImageView image) {
		
		Log.i("K", "IMAGE PATH : " + imagePath);
		
		// we can put the option for the bitmap
		BitmapFactory.Options options = new BitmapFactory.Options();
		File file = new File(imagePath);
		
		Log.i("K", "FILE LENGTH : " + file.length());

		// change the sampleSize to 4 (which will be resulted in 1/4 of original size)
		if (file.length() > 1000000)
			options.inSampleSize = 4;
		else if (file.length() > 500000)
			options.inSampleSize = 2;
		else
			options.inSampleSize = 1;
		
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
		
		image.setBackgroundResource(R.drawable.shapedrawable);
		
		int width = bitmap.getWidth();
	   	int height = bitmap.getHeight();
	   	
	    Bitmap resized_bitmap = null;
	    // set new width and height of the phone_image
	    int new_width = 60;
	    int new_height = 60;
	   	
	   	float scale_width = ((float) new_width) / width;
	   	float scale_height = ((float) new_height) / height;
	   	Matrix matrix = new Matrix();
	   	matrix.postScale(scale_width, scale_height);
	   	resized_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		
	   	photo_image.setImageBitmap(resized_bitmap);
	}
	
	public Observation getObservation(Integer phenophase_id, int species_id, int site_id){

		try{
			SyncDBHelper syncDBHelper = new SyncDBHelper(PlantInformation.this);
			SQLiteDatabase syncDB = syncDBHelper.getReadableDatabase();

			String query = "SELECT " +
					"_id, image_id, time, note, synced FROM my_observation WHERE " +
					"species_id=" + species_id + " AND " +
					"site_id=" + site_id + " AND " +
					"phenophase_id=" + phenophase_id +
					" ORDER BY time DESC " +
					" LIMIT 1";
			Cursor cursor = syncDB.rawQuery(query, null);

			if(cursor.getCount() == 0){
				cursor.close();
				syncDBHelper.close();
				return null;
			}

			cursor.moveToNext();
			Observation obs = new Observation(cursor.getInt(0), species_id, phenophase_id, site_id, cursor.getLong(1), 
					cursor.getString(2), cursor.getString(3));

			cursor.close();
			syncDBHelper.close();

			return obs;
		}
		catch(Exception e){
			e.printStackTrace();
			Log.e("K", e.toString());
			return null;
		}

	}
	
	class Observation{

		public int observation_id = 0;
		public int species_id = 0;
		public int phenophase_id = 0;
		public int site_id = 0;
		public Long image_id = new Long(0);
		public String time = "";
		public String note = "";

		public final String TAG = "PlantInfo";

		public boolean img_replaced = false; //A flag to show if image has been replaced
		public boolean img_removed = false; //A flag to show if image has been replaced
		public boolean note_edited = false; //A flag to show if image has been replaced
		public boolean saved = true; //A flag to show if it is saved or not
		public Long unsaved_image_id; //temporarily saved image id
		public String unsaved_note; //temporarily stored notes

		public Observation(int aObservation_id, int aSpeciesID, int aPhenoID, int aSiteID, Long aImgID, 
				String aTime, String aNote){

			observation_id = aObservation_id;
			species_id = aSpeciesID;
			phenophase_id = aPhenoID;
			site_id = aSiteID;
			image_id = aImgID;
			time = aTime;
			note = aNote;
		}	

		public Observation(int aSpeciesID, int aPhenoID, int aSiteID, Long aImgID, 
				String aTime, String aNote){

			species_id = aSpeciesID;
			phenophase_id = aPhenoID;
			site_id = aSiteID;
			image_id = aImgID;
			time = aTime;
			note = aNote;
		}

		public void copy(Observation o){
			if(o != null){
				this.image_id = o.image_id;
				this.note = o.note;
				this.observation_id = o.observation_id;
				this.site_id = o.site_id;
				this.phenophase_id = o.phenophase_id;
				this.time = o.time;
				this.species_id = o.species_id;
			}
		}

		//obs_id denotes this observation is new or update.
		public void put(Context cont, Integer obs_id){

			try{
				SyncDBHelper syncDBHelper = new SyncDBHelper(cont);
				SQLiteDatabase db = syncDBHelper.getWritableDatabase();
				String query;

				if(obs_id == 0){
					query = "INSERT INTO my_observation VALUES (" +
							"null," +
							species_id + "," +
							site_id + "," +
							phenophase_id +"," +
							image_id + "," +
							"'" + time + "'," +
							"'" + note + "'," +
							SyncDBHelper.SYNCED_NO + ");";
				}else{
					query = "UPDATE my_observation SET " +
							"image_id=" + image_id + "," +
							"time='" + time + "'" + "," +
							"note='" + note + "'" + "," +
							"synced=" + SyncDBHelper.SYNCED_NO + " " + 
							"WHERE _id=" + obs_id + ";"; 
				}
				db.execSQL(query);
				syncDBHelper.close();
			}catch(Exception e){
				Log.e(TAG, e.toString());
			}

		}
	}
}

