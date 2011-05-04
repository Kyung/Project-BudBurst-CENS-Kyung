package cens.ucla.edu.budburst.myplants;

import java.text.SimpleDateFormat;
import java.util.Date;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.id;
import cens.ucla.edu.budburst.R.layout;
import cens.ucla.edu.budburst.R.string;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.utils.PBBItems;
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

public class PBBAddNotes extends Activity {

	
	private String mNotes;	
	private String mImageID;
	private int mPreviousActivity;
	
	private Button doneBtn;
	private Button skipBtn;
	private EditText noteTxt;
	
	private PBBItems pbbItem;
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
		//Intent p_intent = getIntent();
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
	
		mImageID = bundle.getString("image_id");
		mPreviousActivity = bundle.getInt("from", 0);
	    
	    doneBtn = (Button) findViewById(R.id.notes_done);
	    skipBtn = (Button) findViewById(R.id.skip);
	    noteTxt = (EditText) findViewById(R.id.notes);
	    
	    noteTxt.setText("");
	    // TODO Auto-generated method stub
	}
	
	public void onResume() {
		
		doneBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				pbbItem.setNote(noteTxt.getText().toString());
				
				if(mPreviousActivity == HelperValues.FROM_QC_PHENOPHASE || mPreviousActivity == HelperValues.FROM_PBB_PHENOPHASE) {
					
					Intent intent = new Intent(PBBAddNotes.this, PBBObservationPage.class);
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", mPreviousActivity);
					startActivity(intent);
					
				}
				else {
					Intent intent = new Intent(PBBAddNotes.this, PBBAddSite.class);
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", mPreviousActivity);
					intent.putExtra("image_id", mImageID);
					
					startActivity(intent);
				}
				
				
			}
		});
		
		skipBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				pbbItem.setNote(noteTxt.getText().toString());
				// TODO Auto-generated method stub
				if(mPreviousActivity == HelperValues.FROM_QC_PHENOPHASE || mPreviousActivity == HelperValues.FROM_PBB_PHENOPHASE) {
					Intent intent = new Intent(PBBAddNotes.this, PBBObservationPage.class);
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", mPreviousActivity);
				
					startActivity(intent);
				}
				else {
					Intent intent = new Intent(PBBAddNotes.this, PBBAddSite.class);
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("image_id", mImageID);
					intent.putExtra("from", mPreviousActivity);
					
					startActivity(intent);
				}
			}
		});
		

		super.onResume();
	}
}
