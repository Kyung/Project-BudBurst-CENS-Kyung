package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cens.ucla.edu.budburst.PlantInfo;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GetSpeciesInfo extends Activity {
	
	protected static final int PHOTO_CAPTURE_CODE = 0;
	protected static final int GET_PHENOPHASE_CODE = 1;
	public final String BASE_PATH = "/sdcard/pbudburst/";
	public final String TEMP_PATH = "/sdcard/pbudburst/tmp/";
	protected long camera_image_id = 0;
	private View take_photo = null;
	private View replace_photo = null;
	private ImageView image = null;
	private Button phenophaseBtn = null;
	private Button saveBtn = null;
	private int protocol_id;
	private int p_id;
	private String cname = null;
	private String sname = null;
	private Bitmap bitmap = null;
	private File dict_tmp = null;
	private OneTimeDBHelper otDBH;
	private String currentDateTimeString = null;
	private EditText notes = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.getspeciesinfo);
	    
	    // call one-time database helper
	    otDBH = new OneTimeDBHelper(GetSpeciesInfo.this);
	    
	    Intent intent = getIntent();
	    cname = intent.getExtras().getString("cname");
	    sname = intent.getExtras().getString("sname");
	    protocol_id = intent.getExtras().getInt("protocol_id");
	    
	    //Log.i("K", " protocol_id : " + protocol_id);
	    
	    notes = (EditText) findViewById(R.id.notes);
	    
	    TextView common_name = (TextView) findViewById(R.id.common_name);
	    TextView science_name = (TextView) findViewById(R.id.science_name);
	    common_name.setText(" " + cname + " ");
	    science_name.setText(" (" + sname + ") ");
	    
	    // TODO Auto-generated method stub
	    
	    take_photo = this.findViewById(R.id.take_photo);
	    replace_photo = this.findViewById(R.id.replace_photo);
	    
	    take_photo.setVisibility(View.VISIBLE);
	    replace_photo.setVisibility(View.GONE);
	    
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
					
					
					camera_image_id = new Date().getTime();
					
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

				camera_image_id = new Date().getTime();

				Intent mediaCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				mediaCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
						Uri.fromFile(new File(TEMP_PATH, camera_image_id + ".jpg")));
				startActivityForResult(mediaCaptureIntent, PHOTO_CAPTURE_CODE);
			}
		});
		
		phenophaseBtn = (Button) findViewById(R.id.phenophase);
		
		phenophaseBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(GetSpeciesInfo.this, GetPhenophase.class);
				intent.putExtra("protocol_id", protocol_id);
				startActivityForResult(intent, GET_PHENOPHASE_CODE);
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
				
				// set db writable
				db = otDBH.getWritableDatabase();
				// set values which will be inserted in the table
				// this is one way to do, there's another way also.
				
				db.execSQL("INSERT INTO onetimeob VALUES(" + p_id + "," 
						+ "'" + cname + "',"
						+ "'" + sname + "',"
						+ "'" + currentDateTimeString + "',"
						+ "'" + getNote + "',"
						+ camera_image_id + ");");
				
				// should close the databasehelper; otherwise, memory leaks
				otDBH.close();
				Log.i("K", "info saved in the table");
				Toast.makeText(GetSpeciesInfo.this, "Your observation is in the Queue!", Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}
	

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		Log.d("K", "onActivityResult");
		
		if(resultCode == Activity.RESULT_OK) {
			
			if (requestCode == PHOTO_CAPTURE_CODE) {
				
				//Bitmap bitmap = (Bitmap) data.getExtras().get("data");
				
				//int w = bitmap.getWidth();
				//int h = bitmap.getHeight();
				
				//if (w < h) {
				//	
				//} 
				//else {
					
				//}
				
				//((ImageView)findViewById(R.id.image)).setImageBitmap(bitmap);
				//ContentValues values = new ContentValues();
				
				//values.put(Images.Media.TITLE, "title");
				//values.put(Images.Media.BUCKET_ID, "test");
				//values.put(Images.Media.DESCRIPTION, "test Image taken");
				//values.put(Images.Media.MIME_TYPE, "image/jpeg");
				
				//Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
				
				//OutputStream outstream;
				
				//File imagePath = new File(BASE_PATH + camera_image_id + ".jpg");
				String imagePath = TEMP_PATH + camera_image_id + ".jpg";
				//outstream = new FileOutputStream(imagePath);
				bitmap = BitmapFactory.decodeFile(imagePath);
				
				//outstream = getContentResolver().openOutputStream(uri);
				//bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
				
				image = (ImageView) findViewById(R.id.image);
				image.setBackgroundResource(R.drawable.shapedrawable);
				image.setImageBitmap(bitmap);
				
				Toast.makeText(this, "Photo added!", Toast.LENGTH_SHORT).show();
				
				currentDateTimeString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
				TextView time = (TextView) findViewById(R.id.timestamp_text);
				time.setText(" " + currentDateTimeString + " ");
				
				TextView title = (TextView) findViewById(R.id.make_obs_text);
				title.setText(" [ Set Your Observation ] ");
				
				take_photo.setVisibility(View.GONE);
			    replace_photo.setVisibility(View.VISIBLE);
			}
			
			// requestCode == GET_PHENOPHASE_CODE
			else if(requestCode == GET_PHENOPHASE_CODE){
				p_id = data.getIntExtra("species_id", 0);
				
				ImageView imgv = (ImageView) findViewById(R.id.pheno_image);
				Log.i("K", "id : " + p_id);
				imgv.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + p_id, null, null));
				imgv.setBackgroundResource(R.drawable.shapedrawable);
				imgv.setVisibility(View.VISIBLE);
				phenophaseBtn.setText("Change Phenophase");
			}
		}			
	}
}
