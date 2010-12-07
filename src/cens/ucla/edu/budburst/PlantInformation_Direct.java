package cens.ucla.edu.budburst;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import cens.ucla.edu.budburst.helper.Media;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlantInformation_Direct extends Activity {

	private int phenophase_id = 0;
	private int species_id 	= 0;
	private int site_id 	= 0;
	private int protocol_id = 0;
	private int wherefrom = 0;
	private String pheno_name = null;
	private String photo_name	= null;
	private String pheno_text = null;
	private String cname	= null;
	private String sname	= null;
	private String note		= null;
	private String imagePath = null;
	private Double lat = null;
	private Double lng = null;
	private File dict_tmp 			= null;
	private String camera_image_id 	= null;
	private Button saveBtn 			= null;
	private Button siteBtn			= null;
	private EditText notes			= null;
	private String dt_taken			= null;
	private View take_photo 		= null;
	private View replace_photo 		= null;
	private ImageView photo_image 		= null;
	protected static final int PHOTO_CAPTURE_CODE = 0;
	protected static final int GET_SUMMARY_CODE = 1;
	private int SELECT_PLANT_NAME = 100;
	public final String BASE_PATH = "/sdcard/pbudburst/";
	
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
		myTitleText.setText(getString(R.string.PlantInfo_makeObs));
	    
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
		wherefrom = p_intent.getExtras().getInt("from");
		lat = p_intent.getExtras().getDouble("lat");
		lng = p_intent.getExtras().getDouble("lng");
		boolean direct = p_intent.getExtras().getBoolean("direct");
		camera_image_id = photo_name;
		
		ImageView species_image = (ImageView) findViewById(R.id.species_image);
	    TextView species_name = (TextView) findViewById(R.id.species_name);
		ImageView phenoImg = (ImageView) findViewById(R.id.pheno_image);
		TextView phenoTxt = (TextView) findViewById(R.id.pheno_text);
		TextView phenoName = (TextView) findViewById(R.id.pheno_name);
		photo_image = (ImageView) findViewById(R.id.image);
		photo_image.setVisibility(View.VISIBLE);
		
		
		if(species_id == 0) {
			species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
			species_image.setBackgroundResource(R.drawable.shapedrawable);
		    species_name.setText(cname + " \n" + sname + " ");
		}
		else {
		    if(species_id > 76) {
		    	species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
		    }
		    else {
		    	species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+species_id, null, null));
		    }
		    species_image.setBackgroundResource(R.drawable.shapedrawable);
		    species_name.setText(cname + " \n" + sname + " ");
		}
		
		// set xml
		notes = (EditText) findViewById(R.id.notes);
		take_photo = this.findViewById(R.id.take_photo);
		replace_photo = this.findViewById(R.id.replace_photo);
		// show pheno_image and pheno_text

		phenoImg.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + phenophase_id, null, null));
		phenoImg.setBackgroundResource(R.drawable.shapedrawable);
		phenoName.setText(pheno_name);
		phenoTxt.setText(pheno_text);
		notes.setText(note);

	    dict_tmp = new File(Environment.getExternalStorageDirectory(), "pbudburst/pbb/");
	    if(!dict_tmp.exists()) {
	    	dict_tmp.mkdir();
	    }
	    
	    take_photo.setVisibility(View.VISIBLE);
	    replace_photo.setVisibility(View.GONE);

	    		
		if(wherefrom == SELECT_PLANT_NAME) {
			
			camera_image_id = p_intent.getExtras().getString("camera_image_id");
			
			File file = new File(BASE_PATH + camera_image_id + ".jpg");
			
			if(file.exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(BASE_PATH + camera_image_id + ".jpg");
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
		}
		else {
			photo_image.setVisibility(View.VISIBLE);
	    	photo_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
	    	photo_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo", null, null));
		}
	    
	    photo_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				photo_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
				// TODO Auto-generated method stub
				final RelativeLayout linear = (RelativeLayout) View.inflate(PlantInformation_Direct.this, R.layout.image_popup, null);
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(PlantInformation_Direct.this);
				ImageView image_view = (ImageView) linear.findViewById(R.id.image_btn);
				
			    String imagePath = BASE_PATH + camera_image_id + ".jpg";

			    File file = new File(imagePath);
			    Bitmap bitmap = null;
			    
			    // if file exists show the photo on the ImageButton
			    if(file.exists()) {
			    	imagePath = BASE_PATH + camera_image_id + ".jpg";
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
	    
	    // when click take_photo button
		take_photo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					File file = new File(BASE_PATH);
					if (file.exists()) {
						if(!file.isDirectory()) {
							Toast.makeText(PlantInformation_Direct.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
							finish();
						}
					}
					else {
						if (!file.mkdir()) {
							Toast.makeText(PlantInformation_Direct.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
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
						Toast.makeText(PlantInformation_Direct.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
						PlantInformation_Direct.this.finish();
					}
				} else {
					if (!ld.mkdir()) {
						Toast.makeText(PlantInformation_Direct.this, getString(R.string.Alert_errorCheckSD), Toast.LENGTH_SHORT).show();
						PlantInformation_Direct.this.finish();
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
		if(direct) {
			saveBtn.setText(getString(R.string.PlantInfo_makeObs));
		}
		saveBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
					/*
					OneTimeDBHelper onetime = new OneTimeDBHelper(PlantInformation_Direct.this);
					SQLiteDatabase ot = onetime.getWritableDatabase();
					
					dt_taken = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					
					ot.execSQL("INSERT INTO onetimeob VALUES ('" 
							+ cname + "', '" 
							+ sname + "', "
							+ lat + ", "
							+ lng + ", '"
							+ dt_taken + "', '"
							+ note + "', '"
							+ camera_image_id + "', "
							+ "9"
							+ ");");
					
					Log.i("K","INSERT INTO onetimeob VALUES ('" 
							+ cname + "', '" 
							+ sname + "', "
							+ lat + ", "
							+ lng + ", '"
							+ dt_taken + "', '"
							+ note + "', '"
							+ camera_image_id + "', "
							+ "9"
							+ ");" );
					
					ot.close();
					onetime.close();*/
					
					SyncDBHelper syncDBHelper = new SyncDBHelper(PlantInformation_Direct.this);

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
					
					dt_taken = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					
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
						Toast.makeText(PlantInformation_Direct.this, getString(R.string.PlantInfo_successAdded), Toast.LENGTH_SHORT).show();
					}else{
						int c_id = cursor.getInt(0);
						Log.i("K", "C_ID : " + c_id);
						
						query = "UPDATE my_observation SET " +
								"image_id='" + camera_image_id + "'," +
								"time='" + dt_taken + "'," +
								"note='" + notes.getText().toString() + "'" + "," +
								"synced=" + SyncDBHelper.SYNCED_NO + " " + 
								"WHERE _id=" + c_id + ";"; 
						
						Toast.makeText(PlantInformation_Direct.this, getString(R.string.PlantInfo_successUpdate), Toast.LENGTH_SHORT).show();
					}
					cursor.close();
					Log.i("K", "QUERY : " + query);
					db.execSQL(query);
					db.close();
					syncDBHelper.close();
					
					
					// add vibration when done
					Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
					vibrator.vibrate(1000);
				}catch(Exception e){
					Log.e("K", e.toString());
				}

				Intent intent = getIntent();
				setResult(RESULT_OK, intent);
				finish();	
				
			}
		});
		/*
		siteBtn = (Button)findViewById(R.id.site_information);
		siteBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PlantInformation_Direct.this, AddSite.class);
				// change the site text indicating - done!
				siteBtn.setText(getString(R.string.Button_SiteInfo2));
				startActivity(intent);
			}
			
		});
		*/
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
}

