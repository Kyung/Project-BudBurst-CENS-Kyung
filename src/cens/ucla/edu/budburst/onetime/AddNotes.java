package cens.ucla.edu.budburst.onetime;

import java.text.SimpleDateFormat;
import java.util.Date;

import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.GetPhenophase_OneTime;
import cens.ucla.edu.budburst.PlantInformation_Direct;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.PlantObservationSummary;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.Values;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddNotes extends Activity {

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
	private int siteID;
	
	private double latitude;
	private double longitude;
	
	private Button doneBtn;
	private Button skipBtn;
	private EditText noteTxt;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
			
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.addnotes);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);
		
		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.AddNotes));
		Intent p_intent = getIntent();
	    
		cname = p_intent.getExtras().getString("cname");
		sname = p_intent.getExtras().getString("sname");
		cameraImageID = p_intent.getExtras().getString("camera_image_id");
		protocolID = p_intent.getExtras().getInt("protocol_id");
		speciesID = p_intent.getExtras().getInt("species_id");
		phenoID = p_intent.getExtras().getInt("pheno_id");
		latitude = p_intent.getExtras().getDouble("latitude");
		longitude = p_intent.getExtras().getDouble("longitude");
		category = p_intent.getExtras().getInt("category", 0);
		previousActivity = p_intent.getExtras().getInt("from", 0);
	    
		Log.i("K", "category : " + category);
		
	    doneBtn = (Button) findViewById(R.id.notes_done);
	    skipBtn = (Button) findViewById(R.id.skip);
	    noteTxt = (EditText) findViewById(R.id.notes);
	    
	    noteTxt.setText("");
	    
	    if(previousActivity == Values.FROM_QC_PHENOPHASE) {
	    	plantID = p_intent.getExtras().getInt("plant_id", 0);
	    }
		if(previousActivity == Values.FROM_PBB_PHENOPHASE) {
			siteID = p_intent.getExtras().getInt("site_id");
		}
	    
	    // TODO Auto-generated method stub
	}
	
	public void onResume() {
		
		doneBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				/*
				 * If the previous activity is from QC Phenophase, next move to the final decision page.
				 */
				if(previousActivity == Values.FROM_QC_PHENOPHASE || previousActivity == Values.FROM_PBB_PHENOPHASE) {
					
					Intent intent = new Intent(AddNotes.this, PlantObservationSummary.class);
					intent.putExtra("cname", cname);
					intent.putExtra("sname", sname);
					intent.putExtra("protocol_id", protocolID);
					intent.putExtra("pheno_id", phenoID);
					intent.putExtra("species_id", speciesID);
					intent.putExtra("camera_image_id", cameraImageID);
					intent.putExtra("latitude", latitude);
					intent.putExtra("longitude", longitude);
					intent.putExtra("notes", noteTxt.getText().toString());
					intent.putExtra("from", previousActivity);
					
					if(previousActivity == Values.FROM_QC_PHENOPHASE) {
						intent.putExtra("plant_id", plantID);
					}
					if(previousActivity == Values.FROM_PBB_PHENOPHASE) {
						intent.putExtra("site_id", siteID);
					}
					
					startActivity(intent);
					
				}
				else {
					Intent intent = new Intent(AddNotes.this, AddSite.class);
					intent.putExtra("cname", cname);
					intent.putExtra("sname", sname);
					intent.putExtra("protocol_id", protocolID);
					intent.putExtra("pheno_id", phenoID);
					intent.putExtra("species_id", speciesID);
					intent.putExtra("camera_image_id", cameraImageID);
					intent.putExtra("latitude", latitude);
					intent.putExtra("longitude", longitude);
					intent.putExtra("notes", noteTxt.getText().toString());
					intent.putExtra("from", previousActivity);
					intent.putExtra("category", category);
					
					startActivity(intent);
				}
				
				
			}
		});
		
		skipBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(previousActivity == Values.FROM_QC_PHENOPHASE || previousActivity == Values.FROM_PBB_PHENOPHASE) {
					Intent intent = new Intent(AddNotes.this, PlantObservationSummary.class);
					intent.putExtra("cname", cname);
					intent.putExtra("sname", sname);
					intent.putExtra("protocol_id", protocolID);
					intent.putExtra("pheno_id", phenoID);
					intent.putExtra("species_id", speciesID);
					intent.putExtra("plant_id", plantID);
					intent.putExtra("camera_image_id", cameraImageID);
					intent.putExtra("latitude", latitude);
					intent.putExtra("longitude", longitude);
					intent.putExtra("notes", noteTxt.getText().toString());
					intent.putExtra("from", previousActivity);
					
					if(previousActivity == Values.FROM_QC_PHENOPHASE) {
						intent.putExtra("plant_id", plantID);
					}
					if(previousActivity == Values.FROM_PBB_PHENOPHASE) {
						intent.putExtra("site_id", siteID);
					}
					
					startActivity(intent);
				}
				else {
					Intent intent = new Intent(AddNotes.this, AddSite.class);
					intent.putExtra("cname", cname);
					intent.putExtra("sname", sname);
					intent.putExtra("protocol_id", protocolID);
					intent.putExtra("pheno_id", phenoID);
					intent.putExtra("species_id", speciesID);
					intent.putExtra("camera_image_id", cameraImageID);
					intent.putExtra("latitude", latitude);
					intent.putExtra("longitude", longitude);
					intent.putExtra("notes", noteTxt.getText().toString());
					intent.putExtra("from", previousActivity);
					intent.putExtra("category", category);
					
					startActivity(intent);
				}
			}
		});
		

		super.onResume();
	}
}
