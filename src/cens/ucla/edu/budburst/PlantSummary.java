package cens.ucla.edu.budburst;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.WPinfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class PlantSummary extends Activity {

	public final String BASE_PATH = "/sdcard/pbudburst/";
	private OneTimeDBHelper otDBH = null;
	private String cname = null;
	private String sname = null;
	private String dt_taken = null;
	private String notes = null;
	private String photo_name = null;
	private int pheno_id = 0;
	private String pheno_name = null;
	private String pheno_text = null;
	private int species_id = 0;
	private int site_id = 0;
	private int protocol_id = 0;
	protected static final int GET_CHANGE_CODE = 1;
	private PopupWindow popup = null;
	private View popupview = null;
	private ImageButton phone_image = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.plantsummary);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  Summary Species");
	    // set database
	    otDBH = new OneTimeDBHelper(PlantSummary.this);
	    
	    // get intent data from previous activity
	    Intent intent = getIntent();
	    cname = intent.getExtras().getString("cname");
	    sname = intent.getExtras().getString("sname");
	    dt_taken = intent.getExtras().getString("dt_taken");
	    notes = intent.getExtras().getString("notes");
	    photo_name = intent.getExtras().getString("photo_name");
	    pheno_name = intent.getExtras().getString("pheno_name");
	    pheno_text = intent.getExtras().getString("pheno_text");
	    pheno_id = intent.getExtras().getInt("pheno_id", 0);
	    protocol_id = intent.getExtras().getInt("protocol_id", 0);
	    species_id = intent.getExtras().getInt("species_id", 0);
	    site_id = intent.getExtras().getInt("site_id", 0);
	    
	    Log.i("K", "NOTES : " + dt_taken);

	    // setting up layout
	    phone_image = (ImageButton) findViewById(R.id.phone_image);
	    ImageView species_image = (ImageView) findViewById(R.id.species_image);
	    ImageView pheno_image = (ImageView) findViewById(R.id.pheno_image);
	    TextView pheno_title = (TextView) findViewById(R.id.pheno_title);
	    TextView cnameTxt = (TextView) findViewById(R.id.common_name);
	    TextView snameTxt = (TextView) findViewById(R.id.science_name);
	    TextView dt_takenTxt = (TextView) findViewById(R.id.timestamp_text);
	    EditText notesTxt = (EditText) findViewById(R.id.mynotes);
	    Button editBtn = (Button) findViewById(R.id.edit);
	    phone_image.setVisibility(View.VISIBLE);
	    
	    String[] sname_split;
	    sname_split = sname.split(" ");
	    int sname_length = sname_split[0].length() + sname_split[1].length();
	    
	    // put cname and sname in the textView
	    pheno_title.setText("'" + pheno_name + "' Observed");
	    dt_takenTxt.setText(dt_taken + " ");
	    
	    cnameTxt.setText(cname + " ");
	    snameTxt.setText(sname + " ");
	    
	    snameTxt.setText(sname_split[0] + " " + sname_split[1] + " ");
	    if(species_id > 76) {
	    	species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
	    }
	    else {
	    	species_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+species_id, null, null));
	    }
	    
	    species_image.setBackgroundResource(R.drawable.shapedrawable);
	    phone_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
	    
	    String imagePath = null;
	    File file = new File(BASE_PATH + photo_name + ".jpg");
	    Bitmap bitmap = null;
	    Bitmap resized_bitmap = null;
	    
	    // set new width and height of the phone_image
	    int new_width = 110;
	    int new_height = 110;
	   
	    if(file.exists()) {
	    	imagePath = BASE_PATH + photo_name + ".jpg";
	    	bitmap = BitmapFactory.decodeFile(imagePath);
	    	
		   	int width = bitmap.getWidth();
		   	int height = bitmap.getHeight();
		   	
		   	float scale_width = ((float) new_width) / width;
		   	float scale_height = ((float) new_height) / height;
		   	Matrix matrix = new Matrix();
		   	matrix.postScale(scale_width, scale_height);
		   	resized_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		   	
	    	phone_image.setImageBitmap(resized_bitmap);
	    	phone_image.setVisibility(View.VISIBLE);
	    }
	    else {
	    	phone_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo", null, null));
	   	    phone_image.setVisibility(View.VISIBLE);
	   	}
	    
	    phone_image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				phone_image.setBackgroundResource(R.drawable.shapedrawable_yellow);
				
				final LinearLayout linear = (LinearLayout) View.inflate(PlantSummary.this, R.layout.image_popup, null);
				
				// TODO Auto-generated method stub
				AlertDialog.Builder dialog = new AlertDialog.Builder(PlantSummary.this);
				ImageView image_view = (ImageView) linear.findViewById(R.id.image_btn);
				
			    String imagePath = "/sdcard/pbudburst/" + photo_name + ".jpg";

			    File file = new File(imagePath);
			    Bitmap bitmap = null;
			    
			    // if file exists show the photo on the ImageButton
			    if(file.exists()) {
			    	imagePath = "/sdcard/pbudburst/" + photo_name + ".jpg";
				   	bitmap = BitmapFactory.decodeFile(imagePath);
				   	image_view.setImageBitmap(bitmap);
			    }
			    // if not, show 'no image' ImageButton
			    else {
			    	image_view.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo", null, null));
			    }
			    
			    // when press 'Back', close the dialog
				dialog.setPositiveButton("Back", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				});
		        dialog.setView(linear);
		        dialog.show();
			}
		});
	    

	    if(pheno_id == 0) {
	    	pheno_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
	    }
	    else {
	    	pheno_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + pheno_id, null, null));
	    }
		
	    
	    pheno_image.setBackgroundResource(R.drawable.shapedrawable);
	    pheno_image.setVisibility(View.VISIBLE);
	    //phenoTxt.setText(pheno_text + " ");

	    
	    notesTxt.setEnabled(false);
	    notesTxt.setClickable(false);
	    
		if(notes.length() != 0) {
			notesTxt.setText(notes);
		}
		else {
			notesTxt.setText("Add notes for this species.");
		}
		
		editBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PlantSummary.this, PlantInformation.class);
				intent.putExtra("pheno_id", pheno_id);
				intent.putExtra("protocol_id", protocol_id);
				intent.putExtra("pheno_name", pheno_name);
				intent.putExtra("pheno_text", pheno_text);
				intent.putExtra("species_id", species_id);
				intent.putExtra("site_id", site_id);
				intent.putExtra("dt_taken", dt_taken);
				intent.putExtra("notes", notes);
				intent.putExtra("photo_name", photo_name);
				intent.putExtra("cname", cname);
				intent.putExtra("sname", sname);
				startActivityForResult(intent, GET_CHANGE_CODE);
			}
		});
	    // TODO Auto-generated method stub
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// You can use the requestCode to select between multiple child
		// activities you may have started. Here there is only one thing
		// we launch.
		Log.d("K", "onActivityResult");
		
		if(resultCode == Activity.RESULT_OK) {			

			if (requestCode == GET_CHANGE_CODE) {
			
				Intent intent = getIntent();
				setResult(RESULT_OK, intent);
				finish();
			
			}
		}			
	}
}
