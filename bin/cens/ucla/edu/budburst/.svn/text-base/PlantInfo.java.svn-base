package cens.ucla.edu.budburst;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;

public class PlantInfo extends Activity{
	
	private final int LEAVES = 0;
	private final int FLOWERS = 1;
	private final int FRUITS = 2;
	protected static final int PHOTO_CAPTURE_CODE = 0;

	
	public final String TAG = "PlantInfo";
	public final String BASE_PATH = "/sdcard/pbudburst/";

	
	public Integer current_phenophase_id;
	public Integer current_species_id;
	public Integer current_protocol_id;
	public Integer current_site_id;
	public Integer current_stage_id;
	public ArrayList<Integer>  phenophases_in_this_tab;
	public ArrayList<Integer> phenophases_in_next_tab;
	
	private Intent global_intent;
	
	ArrayList<Button> buttonBar = new ArrayList<Button>();
	public Observation temporary_obs = new Observation();
	public Observation observation;
	public int k;
	
	protected Long camera_image_id;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plantinfo);
		
		Intent p_intent = getIntent();
		current_phenophase_id = p_intent.getExtras().getInt("phenophase_id",0);
		current_species_id = p_intent.getExtras().getInt("species_id",0);
		current_protocol_id = p_intent.getExtras().getInt("protocol_id",0);
		current_site_id = p_intent.getExtras().getInt("site_id",0);
		current_stage_id = p_intent.getExtras().getInt("stage_id",0);

		phenophases_in_this_tab = get_phenophase_in_this_tab(current_stage_id, current_protocol_id);
		
		if(current_phenophase_id == 0)
			current_phenophase_id = phenophases_in_this_tab.get(0);
		
		setTitle(get_common_name(current_species_id)+" - "+get_phenophase_name(current_phenophase_id));
			
//		Toast.makeText(PlantInfo.this, current_phenophase_id.toString() +  "," +
//				current_species_id.toString()+ "," +
//				current_protocol_id.toString() + "," +
//				current_site_id.toString(), Toast.LENGTH_SHORT).show();

		//populate the buttonbar
		LinearLayout phenophaseBar = (LinearLayout) this.findViewById(R.id.phenophase_bar);
		
		Observation temp_o = getObservation(current_phenophase_id, current_species_id, current_site_id);
		observation = new Observation();
		observation.copy(temp_o);
		
		for(k=0; k<phenophases_in_this_tab.size(); k++){
			ImageView button = new ImageView(this);
			
			final int phenophase_id_of_this = phenophases_in_this_tab.get(k);
			// button.setImageBitmap(Drawable.createFromPath(pathName)
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = getIntent();
					intent.putExtra("phenophase_id", phenophase_id_of_this);
					intent.putExtra("species_id",current_species_id);
					intent.putExtra("protocol_id",current_protocol_id);
					intent.putExtra("site_id", current_site_id);
					intent.putExtra("stage_id", current_stage_id);
					
					//Observation cur_obs = getObservation(current_phenophase_id, current_species_id, current_site_id);
					//Check if note has been edited
					EditText note = (EditText) findViewById(R.id.notes);
					String current_note = note.getText().toString();

//TODO: to fix this.					
//					if(!current_note.equals(cur_obs.note))
//						temporary_obs.saved = false;	

					//Check if there is unsaved data
					if(temporary_obs.saved == true){
						startActivity(intent);
						finish();
					}
					else{ //Prompts users where save it or not
						global_intent = intent;
						new AlertDialog.Builder(PlantInfo.this)
						.setTitle("Question")
						.setMessage(getString(R.string.submit_question))
						.setPositiveButton("Yes",mClick) //BUTTON1
						.setNeutralButton("No",mClick) //BUTTON3
						.setNegativeButton("Cancel",mClick) //BUTTON2
						.show();
					}
				}
			});
			button.setPadding(0, 0, 5, 0);
			Integer icon_num = get_phenophase_icon(phenophase_id_of_this, current_protocol_id);
			//String res_path = "cens.ucla.edu.budburst:drawable/p"+icon_num.toString();
			//int resID = getResources().getIdentifier(res_path, null, null);
			//int resID = PlantInfo.this.getResources().getIdentifier("p"+icon_num.toString(), "drawable", "cens.ucla.edu.budburst");
			
			//Bitmap icon = overlay(BitmapFactory.decodeResource(PlantInfo.this.getResources(), resID));
			InputStream asst = null;
			try{
				 asst = PlantInfo.this.getAssets().open("phenophase_images/p" + icon_num + ".png");
				
			}catch(Exception e){
				Log.e(TAG, e.toString());
			}
			Bitmap icon = overlay(BitmapFactory.decodeStream(asst));

			if (phenophases_in_this_tab.get(k) != current_phenophase_id)
				icon = overlay(icon, BitmapFactory.decodeResource(
					getResources(),R.drawable.translucent_gray35));

			Observation current_obs = getObservation(phenophases_in_this_tab.get(k), current_species_id, current_site_id);
			if (current_obs != null){
				icon = overlay(icon, BitmapFactory.decodeResource(
					getResources(),R.drawable.check_mark));
			}
			button.setImageBitmap(icon);
			phenophaseBar.addView(button);
			
		}

		
		//Image view setting
		showImageCameraButton(observation);
		
		//Desc view setting
		TextView phenophase_comment = (TextView) this.findViewById(R.id.phenophase_desc_text);
		phenophase_comment.setText(get_phenophase_desc(current_phenophase_id));
		
		
		if(!observation.time.equals("")) {
			
			//Set Pheonphase text
			TextView phenophase_text = (TextView) this.findViewById(R.id.phenophase_text);
			phenophase_text.setText(get_phenophase_name(current_phenophase_id));
			
			//set date
			TextView timestamp = (TextView) this.findViewById(R.id.timestamp_text);
			timestamp.setText(observation.time + " ");
			
			//put the note in the edittext
			EditText note = (EditText) this.findViewById(R.id.notes);
			note.setText(observation.note);
			
			//Make make_obs_text unvisible
			this.findViewById(R.id.make_obs_text).setVisibility(View.GONE);
			
			//show image
			
		} else {
			this.findViewById(R.id.timestamp_text).setVisibility(View.GONE);
			this.findViewById(R.id.timestamp_label).setVisibility(View.GONE);
			
			//Make make_obs_text unvisible
			this.findViewById(R.id.make_obs_text).setVisibility(View.VISIBLE);
			
			//Set Phenophase text
			TextView phenophase_text = (TextView) this.findViewById(R.id.phenophase_text);
			phenophase_text.setText(get_phenophase_name(current_phenophase_id));
		}
		
		//Leave button
		buttonBar.add((Button) this.findViewById(R.id.button1));
		buttonBar.get(0).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				phenophases_in_next_tab = get_phenophase_in_this_tab(LEAVES, current_protocol_id);

				final Intent intent = getIntent();
				intent.putExtra("stage_id", LEAVES);
				intent.putExtra("phenophase_id", phenophases_in_next_tab.get(0));
				intent.putExtra("species_id",current_species_id);
				intent.putExtra("protocol_id",current_protocol_id);
				intent.putExtra("site_id", current_site_id);
			
				
				//Check if note has been edited
				EditText note = (EditText) findViewById(R.id.notes);
				String current_note = note.getText().toString();
				if(!current_note.equals(observation.note))
					temporary_obs.saved = false;					
				
				//Check if there is unsaved data
				if(temporary_obs.saved == true){
					startActivity(intent);
					finish();
				}
				else{ //Prompts users where save it or not
					global_intent = intent;				
					
					new AlertDialog.Builder(PlantInfo.this)
					.setTitle("Question")
					.setMessage(getString(R.string.submit_question))
					.setPositiveButton("Yes",mClick) //BUTTON1
					.setNeutralButton("No",mClick) //BUTTON3
					.setNegativeButton("Cancel",mClick) //BUTTON2
					.show();
				}
			}
		});
		
		//Flower button
		buttonBar.add((Button) this.findViewById(R.id.button2));
		buttonBar.get(1).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				phenophases_in_next_tab = get_phenophase_in_this_tab(FLOWERS, current_protocol_id);
				
				Intent intent = getIntent();
				intent.putExtra("stage_id", FLOWERS);
				intent.putExtra("phenophase_id", phenophases_in_next_tab.get(0));
				intent.putExtra("species_id",current_species_id);
				intent.putExtra("protocol_id",current_protocol_id);
				intent.putExtra("site_id", current_site_id);

				//Check if note has been edited
				EditText note = (EditText) findViewById(R.id.notes);
				String current_note = note.getText().toString();
				if(!current_note.equals(observation.note))
					temporary_obs.saved = false;
				
				//Check if there is unsaved data
				if(temporary_obs.saved == true){
					startActivity(intent);
					finish();
				}
				else{ //Prompts users where save it or not
					global_intent = intent;
				
					new AlertDialog.Builder(PlantInfo.this)
					.setTitle("Question")
					.setMessage(getString(R.string.submit_question))
					.setPositiveButton("Yes",mClick) //BUTTON1
					.setNeutralButton("No",mClick) //BUTTON3
					.setNegativeButton("Cancel",mClick) //BUTTON2	
					.show();
				}

			}
		});

		//Fruits button
		buttonBar.add((Button) this.findViewById(R.id.button3));
		buttonBar.get(2).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				phenophases_in_next_tab = get_phenophase_in_this_tab(FRUITS, current_protocol_id);
				
				Intent intent = getIntent();
				intent.putExtra("stage_id", FRUITS);
				intent.putExtra("phenophase_id", phenophases_in_next_tab.get(0));
				intent.putExtra("species_id",current_species_id);
				intent.putExtra("protocol_id",current_protocol_id);
				intent.putExtra("site_id", current_site_id);

				//Check if note has been edited
				EditText note = (EditText) findViewById(R.id.notes);
				String current_note = note.getText().toString();
				if(!current_note.equals(observation.note))
					temporary_obs.saved = false;
				
				//Check if there is unsaved data
				if(temporary_obs.saved == true){
					startActivity(intent);
					finish();
				}
				else{ //Prompts users where save it or not
					global_intent = intent;
					
					new AlertDialog.Builder(PlantInfo.this)
					.setTitle("Question")
					.setMessage(getString(R.string.submit_question))
					.setPositiveButton("Yes",mClick) //BUTTON1
					.setNeutralButton("No",mClick) //BUTTON3
					.setNegativeButton("Cancel",mClick) //BUTTON2
					.show();
				}
			}
		});
		// set selected button
		buttonBar.get(current_stage_id).setSelected(true);
		
		//Save Button
		Button save = (Button) this.findViewById(R.id.save);
		save.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { 
				
				Intent intent = getIntent();
				intent.putExtra("stage_id", current_stage_id);
				intent.putExtra("phenophase_id", current_phenophase_id);
				intent.putExtra("species_id",current_species_id);
				intent.putExtra("protocol_id",current_protocol_id);
				intent.putExtra("site_id", current_site_id);
				
				//resizeImage(BASE_PATH+camera_image_id+".jpg");
				
				//Check the previous observation date is same as today
				if(observation.time.equals("") || 
						observation.time.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))){
					Toast.makeText(PlantInfo.this, 
							"Thank you for your observation!", Toast.LENGTH_SHORT).show();				

					EditText note = (EditText) findViewById(R.id.notes);
					//Check if img has been replaced
					if(temporary_obs.img_replaced == true){
						//Delete older picture file
						File file = new File(BASE_PATH + 
								temporary_obs.unsaved_image_id + ".jpg");
						if(file != null)
							file.delete();
						temporary_obs.img_replaced = false;
					}
					
					//Check if img has been removed
					if(temporary_obs.img_removed == true){
						//Delete older picture file
						File file = new File(BASE_PATH + 
								temporary_obs.unsaved_image_id + ".jpg");
						if(file != null)
							file.delete();
						temporary_obs.img_removed = false;
					}
					

					observation.site_id = current_site_id;
					observation.phenophase_id = current_phenophase_id;
					observation.species_id = current_species_id;
					
					if(camera_image_id != null)
						observation.image_id = camera_image_id;
					
					observation.note = note.getText().toString();
					observation.time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					observation.put(PlantInfo.this, observation.observation_id);
					
					startActivity(intent);
					finish();
				}
				else{
					global_intent = intent;
					
					new AlertDialog.Builder(PlantInfo.this)
					.setTitle("Question")
					.setMessage("Do you want to change the observation date to today?")
					.setPositiveButton("Yes",mClickSaveSubQuestion)
					.setNeutralButton("No", mClickSaveSubQuestion)
					.setNegativeButton("Cance", mClickSaveSubQuestion)
					.show();					
				}
			}
		});

		//Take photo button
		View take_photo = this.findViewById(R.id.take_photo);
		take_photo.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				try{
					File ld = new File(BASE_PATH);
					if (ld.exists()) {
						if (!ld.isDirectory()) {
							// Should probably inform user ... hmm!
							Toast.makeText(PlantInfo.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
							PlantInfo.this.finish();
						}
					} else {
						if (!ld.mkdir()) {
							Toast.makeText(PlantInfo.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
							PlantInfo.this.finish();
						}
					}
	
					camera_image_id = new Date().getTime();
	
					Intent mediaCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					mediaCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
							Uri.fromFile(new File(BASE_PATH, camera_image_id + ".jpg")));
					startActivityForResult(mediaCaptureIntent, PHOTO_CAPTURE_CODE);
										
				}catch(Exception e){
					Log.e(TAG, e.toString());
				}
			}
			
		});
		
		//Take replace button
		View replace_photo = this.findViewById(R.id.replace_photo);
		replace_photo.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				File ld = new File(BASE_PATH);
				if (ld.exists()) {
					if (!ld.isDirectory()) {
						// Should probably inform user ... hmm!
						Toast.makeText(PlantInfo.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
						PlantInfo.this.finish();
					}
				} else {
					if (!ld.mkdir()) {
						Toast.makeText(PlantInfo.this, "Error: Please check your sdcard.", Toast.LENGTH_SHORT).show();
						PlantInfo.this.finish();
					}
				}

				camera_image_id = new Date().getTime();

				Intent mediaCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				mediaCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
						Uri.fromFile(new File(BASE_PATH, camera_image_id + ".jpg")));
				startActivityForResult(mediaCaptureIntent, PHOTO_CAPTURE_CODE);
			}
			
		});
		
		//Remove_photo button
		View remove_photo = this.findViewById(R.id.remove_photo);
		remove_photo.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				temporary_obs.unsaved_image_id = observation.image_id;
				temporary_obs.saved = false;
				temporary_obs.img_removed = true;

				observation.image_id = new Long(0);
				showImageCameraButton(observation);
			}
		});
		
	}
	
	public void resizeImage(String path){
		Bitmap bitmapOrg = BitmapFactory.decodeFile(path);
		
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();
		int newWidth;
		int newHeight;
		if(width > height){
			newWidth = 1024/2;
			newHeight = 768/2;
		}else{
			newWidth = 768/2;
			newHeight = 1024/2;
		}
		
		float scaleWidth = ((float)newWidth)/width;
		float scaleHeight = ((float)newHeight)/height;
		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg,0,0,width,height,matrix, true);
		try{
			FileOutputStream out = new FileOutputStream(path);
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
		}catch(Exception e){
			Log.e(TAG, e.toString());
		}
		
	}
	
	public void showImageCameraButton(Observation current_obs){

		if(current_obs != null &&current_obs.image_id != 0){

				View Take_photo = (View) this.findViewById(R.id.take_photo);
				Take_photo.setVisibility(View.GONE);
				
				View Remove_photo = (View) this.findViewById(R.id.remove_photo);
				Remove_photo.setVisibility(View.VISIBLE);

				View Replace_photo = (View) this.findViewById(R.id.replace_photo);
				Replace_photo.setVisibility(View.VISIBLE);

		}else{
			View Take_photo = (View) this.findViewById(R.id.take_photo);
			Take_photo.setVisibility(View.VISIBLE);
			
			View Remove_photo = (View) this.findViewById(R.id.remove_photo);
			Remove_photo.setVisibility(View.GONE);

			View Replace_photo = (View) this.findViewById(R.id.replace_photo);
			Replace_photo.setVisibility(View.GONE);
		}
		
		if(current_obs.image_id != 0){
			String image_path = BASE_PATH + current_obs.image_id + ".jpg";
			File file = new File(image_path);

			BitmapFactory.Options options = new BitmapFactory.Options();
			if(file.length() > 500000)
 				options.inSampleSize=4;
			else if(file.length() > 100000)
				options.inSampleSize=2;
			else
				options.inSampleSize=1;
			
			ImageView img = (ImageView) this.findViewById(R.id.image);
			img.setVisibility(View.VISIBLE);
			img.setImageBitmap(BitmapFactory.decodeFile(image_path,options));
		}
		else{
			ImageView img = (ImageView) this.findViewById(R.id.image);
			img.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		Log.d(TAG, "onActivityResult");
		if (requestCode == PHOTO_CAPTURE_CODE) {

			// This is a standard resultCode that is sent back if the
			// activity doesn't supply an explicit result. It will also
			// be returned if the activity failed to launch.
			if (resultCode == RESULT_CANCELED) {
				Log.d(TAG, "Photo returned canceled code.");
				Toast.makeText(this, "Picture cancelled.", Toast.LENGTH_SHORT).show();
			} else {
				
				if (camera_image_id != null) {
					temporary_obs.saved = false;
					observation.image_id = camera_image_id;
					showImageCameraButton(observation);
				}
			}
		}
	}
	
	public Observation getObservation(Integer phenophase_id, int species_id, int site_id){
		
		try{
			SyncDBHelper syncDBHelper = new SyncDBHelper(PlantInfo.this);
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
			Log.e(TAG, e.toString());
			return null;
		}
		
	}
	
	public ArrayList<Integer> get_phenophase_in_this_tab(int stage_id, int protocol_id){
		try{
			ArrayList<Integer> arPhenophase= new ArrayList<Integer>();
			
			StaticDBHelper staticDBHelper = new StaticDBHelper(PlantInfo.this);
			SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
			
			String query = "SELECT Phenophase_ID FROM Phenophase_Protocol_Icon WHERE" +
					" Protocol_ID=" + protocol_id + 
					" AND type='" + stage_id_to_string(stage_id) + "' " +
					" ORDER BY Chrono_Order ASC";
			Cursor cursor = staticDB.rawQuery(query, null);
			
			while(cursor.moveToNext()){
				arPhenophase.add(cursor.getInt(0));
			}
			cursor.close();
			staticDBHelper.close();
			return arPhenophase;
		}catch(Exception e){
			e.printStackTrace();
			Log.e(TAG, e.toString());
			return null;
		}
	}
	
	public String get_common_name(int species_id){
		
		try{

			StaticDBHelper staticDBHelper = new StaticDBHelper(PlantInfo.this);
			SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
			
			String query = "SELECT common_name FROM species WHERE" +
					" _id = " + species_id;
			Cursor cursor = staticDB.rawQuery(query, null);
			
			cursor.moveToNext();
			String common_name = cursor.getString(0);
			cursor.close();
			staticDBHelper.close();
			return common_name;
		}catch(Exception e){
			Log.e(TAG,e.toString());
			return null;
		}
	}
	
	public String get_phenophase_name(int phenophase_id){
		try{

			StaticDBHelper staticDBHelper = new StaticDBHelper(PlantInfo.this);
			SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
			
			String query = "SELECT Phenophase_Name FROM Phenophase_Protocol_Icon WHERE" +
					" Phenophase_ID = " + phenophase_id + ";";
			Cursor cursor = staticDB.rawQuery(query, null);
			
			cursor.moveToNext();
			String pheno_name =cursor.getString(0);
			
			cursor.close();
			staticDBHelper.close();
			return pheno_name;
			
		}catch(Exception e){
			Log.e(TAG,e.toString());
			return null;
		}
	}

	public String get_phenophase_desc(int phenophase_id){
		try{

			StaticDBHelper staticDBHelper = new StaticDBHelper(PlantInfo.this);
			SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
			
			String query = "SELECT description FROM Phenophase_Protocol_Icon WHERE" +
					" Phenophase_ID = " + phenophase_id + ";";
			Cursor cursor = staticDB.rawQuery(query, null);
			
			cursor.moveToNext();
			String pheno_name =cursor.getString(0);
			
			cursor.close();
			staticDBHelper.close();
			return pheno_name;
			
		}catch(Exception e){
			Log.e(TAG,e.toString());
			return null;
		}
	}
	
	public int get_phenophase_icon(int phenophase_id, int protocol_id){

		
		try{
			int pheno_icon;
			StaticDBHelper staticDBHelper = new StaticDBHelper(PlantInfo.this);
			SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
			
			String query = "SELECT Phenophase_Icon FROM Phenophase_Protocol_Icon WHERE" +
					" Protocol_ID = " + protocol_id + 
					" AND Phenophase_ID = " + phenophase_id + ";";
			Cursor cursor = staticDB.rawQuery(query, null);
			
			cursor.moveToNext();
			pheno_icon=cursor.getInt(0);
			cursor.close();
			staticDBHelper.close();
			return pheno_icon;
		}catch(Exception e){
			Log.e(TAG,e.toString());
			return 0;
		}
	}
	public String stage_id_to_string(int a){
		
		if (a==LEAVES){
			return "Leaves";
		}
		else if(a==FLOWERS){
			return "Flowers";
		}else if(a==FRUITS){
			return "Fruits";
		}
		return null;
		
	}
		
	
	private Bitmap overlay(Bitmap... bitmaps) {
		if (bitmaps.length == 0)
			return null;

		Bitmap bmOverlay = Bitmap.createBitmap(bitmaps[0].getWidth(), bitmaps[0].getHeight(), bitmaps[0].getConfig());

		Canvas canvas = new Canvas(bmOverlay);
		for (int i = 0; i < bitmaps.length; i++)
			canvas.drawBitmap(bitmaps[i], new Matrix(), null);
		
		return bmOverlay;
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
	    	
			//Check if note has been edited
			EditText note = (EditText) findViewById(R.id.notes);
			String current_note = note.getText().toString();
			if(!current_note.equals(observation.note))
				temporary_obs.saved = false;	
			
			if(temporary_obs.saved == false){
				new AlertDialog.Builder(PlantInfo.this)
				.setTitle("Question")
				.setMessage(getString(R.string.submit_question))
				.setPositiveButton("Yes",mClick) //BUTTON1
				.setNeutralButton("No",mClick) //BUTTON3
				.setNegativeButton("Cancel",mClick) //BUTTON2
				.show();
				return true;
			}
	    }
		return super.onKeyDown(keyCode, event);
	}
	
	
	DialogInterface.OnClickListener mClick = 
		new DialogInterface.OnClickListener(){
		public void onClick(DialogInterface dialog, int whichButton){
			if (whichButton == DialogInterface.BUTTON1){ 
				//Submit
				//Check the previous observation date is same as today
				if(observation.time.equals("") || 
						observation.time.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))){
					//Check if note has been edited
					EditText note = (EditText) findViewById(R.id.notes);
					String current_note = note.getText().toString();
					observation.note = current_note;
		
					//Check if img has been replaced
					if(temporary_obs.img_replaced == true){
						//Delete older picture file
						File file = new File(BASE_PATH + temporary_obs.unsaved_image_id + ".jpg");
						if(file != null)
							file.delete();
						temporary_obs.img_replaced = false;
					}
					
					//Check if img has been removed
					if(temporary_obs.img_removed == true){
						//Delete older picture file
						File file = new File(BASE_PATH + temporary_obs.unsaved_image_id + ".jpg");
						if(file != null)
							file.delete();
						temporary_obs.img_removed = false;
					}
					observation.time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					observation.put(PlantInfo.this,observation.observation_id);
					temporary_obs.saved = true;
					Toast.makeText(PlantInfo.this,"Thank you for your observation!", Toast.LENGTH_SHORT).show();
					
					if(global_intent!=null)
						startActivity(global_intent);
					finish();	
				}
				else{
					new AlertDialog.Builder(PlantInfo.this)
					.setTitle("Question")
					.setMessage("Do you want to change the observation date to today?")
					.setPositiveButton("Yes",mClickSaveSubQuestion)
					.setNeutralButton("No", mClickSaveSubQuestion)
					.setNegativeButton("Cance", mClickSaveSubQuestion)
					.show();
				}
				
			}else if(whichButton == DialogInterface.BUTTON3){ 
				//No submit
				
				//Restore note
				EditText note = (EditText) findViewById(R.id.notes);
				note.setText(observation.note);
				
				//Restore replaced image 
				if(temporary_obs.img_replaced == true){
					
					//Delete new photo file
					File file = new File(BASE_PATH + observation.image_id + ".jpg");
					if(file != null)
						file.delete();
					
					//Put new photo file
					observation.image_id = temporary_obs.unsaved_image_id;
					temporary_obs.img_replaced = false; //necessary?
				}
				
				//Restore removed image
				if(temporary_obs.img_removed == true){
					observation.image_id = temporary_obs.unsaved_image_id;
					temporary_obs.img_removed = false;
				}
				
				temporary_obs.saved = true;	
				if(global_intent != null)
					startActivity(global_intent);
				finish();	
				
			}else{ //Cancel
				temporary_obs.saved = true;
			}				
		}
	};
	
	DialogInterface.OnClickListener mClickSaveSubQuestion = 
		new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// TODO Auto-generated method stub
				if(whichButton == DialogInterface.BUTTON1){

					//Check if note has been edited
					EditText note = (EditText) findViewById(R.id.notes);
					String current_note = note.getText().toString();
					observation.note = current_note;
		
					//Check if img has been replaced
					if(temporary_obs.img_replaced == true){
						//Delete older picture file
						File file = new File(BASE_PATH + temporary_obs.unsaved_image_id + ".jpg");
						if(file != null)
							file.delete();
						temporary_obs.img_replaced = false;
					}
					
					//Check if img has been removed
					if(temporary_obs.img_removed == true){
						//Delete older picture file
						File file = new File(BASE_PATH + temporary_obs.unsaved_image_id + ".jpg");
						if(file != null)
							file.delete();
						temporary_obs.img_removed = false;
					}
					
					observation.time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					observation.put(PlantInfo.this, observation.observation_id);
					
					temporary_obs.saved = true;
					Toast.makeText(PlantInfo.this,"Thank you for your observation!", Toast.LENGTH_SHORT).show();
					
					if(global_intent!=null)
						startActivity(global_intent);
					finish();	

				}
				else if(whichButton == DialogInterface.BUTTON3){

					//Check if note has been edited
					EditText note = (EditText) findViewById(R.id.notes);
					String current_note = note.getText().toString();
					observation.note = current_note;
		
					//Check if img has been replaced
					if(temporary_obs.img_replaced == true){
						//Delete older picture file
						File file = new File(BASE_PATH + temporary_obs.unsaved_image_id + ".jpg");
						if(file != null)
							file.delete();
						temporary_obs.img_replaced = false;
					}
					
					//Check if img has been removed
					if(temporary_obs.img_removed == true){
						//Delete older picture file
						File file = new File(BASE_PATH + temporary_obs.unsaved_image_id + ".jpg");
						if(file != null)
							file.delete();
						temporary_obs.img_removed = false;
					}
					
					observation.put(PlantInfo.this, observation.observation_id);
					
					temporary_obs.saved = true;
					Toast.makeText(PlantInfo.this,"Thank you for your observation!", Toast.LENGTH_SHORT).show();
					
					if(global_intent!=null)
						startActivity(global_intent);
					finish();	

				}
				else{
					
				}
				
			}
		};

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
	
	public Observation(){ 
		
		
	}
	
	
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

//
//class Temporary_obs{
//	public boolean img_replaced = false; //A flag to show if image has been replaced
//	public boolean img_removed = false; //A flag to show if image has been replaced
//	public boolean note_edited = false; //A flag to show if image has been replaced
//	public boolean saved = true; //A flag to show if it is saved or not
//	public Integer unsaved_image_id; //temporarily saved image id
//	public String unsaved_note; //temporarily stored notes		
//}

