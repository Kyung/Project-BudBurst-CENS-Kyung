package cens.ucla.edu.budburst.myplants;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.R.string;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.AnimationHelper;
import cens.ucla.edu.budburst.helper.HelperValues;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class PBBPhenophaseInfo extends Activity {

	private StaticDBHelper sDBH;
	private int pheno_id;
	private int previous_activity;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.species_detail);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);
		
		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.PhenophaseDetail_info));
	    // end set-title-bar
		
		// get previous intent information
		Intent intent = getIntent();
		pheno_id = intent.getExtras().getInt("id");
		previous_activity = intent.getExtras().getInt("from");
	    
	    // set the layout
	    ImageView phenoImage = (ImageView) findViewById(R.id.species_image);
	    phenoImage.setBackgroundResource(R.drawable.shapedrawable);
	    TextView phenoName = (TextView) findViewById(R.id.common_name);
	    TextView phenoDescription = (TextView) findViewById(R.id.text);
	    
	    // open database
	    SQLiteDatabase db;
	    Cursor cursor;
	    
	    sDBH = new StaticDBHelper(PBBPhenophaseInfo.this);
	    
	    db = sDBH.getReadableDatabase();
	    
	    if(previous_activity == HelperValues.FROM_PBB_PHENOPHASE) {
	    	int protocol_id = intent.getExtras().getInt("protocol_id");
	    	
		    cursor = db.rawQuery("SELECT Phenophase_Name, Detail_Description, Phenophase_Icon FROM Phenophase_Protocol_Icon WHERE Phenophase_ID = " + pheno_id + " AND Protocol_ID = " + protocol_id +";", null);

		    while(cursor.moveToNext()) {
		    	phenoName.setText(" " + cursor.getString(0) + " ");
		    	phenoDescription.setText("" + cursor.getString(1) + " ");
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p"+cursor.getInt(2), null, null);
				phenoImage.setImageResource(resID);
			}
		    
		    cursor.close();
	    }
	    else {
	    	cursor = db.rawQuery("SELECT Type, Detail_Description, Phenophase_Icon FROM Onetime_Observation WHERE _id = " + pheno_id + ";", null);

		    while(cursor.moveToNext()) {
		    	phenoName.setText(" " + cursor.getString(0) + " ");
		    	phenoDescription.setText("" + cursor.getString(1) + " ");
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + cursor.getInt(2), null, null);
				phenoImage.setImageResource(resID);
			}
		    cursor.close();
	    }

	    db.close();
		sDBH.close();
	    // TODO Auto-generated method stub

	}
}
