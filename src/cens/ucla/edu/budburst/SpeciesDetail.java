package cens.ucla.edu.budburst;


import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.AnimationHelper;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.Values;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

//public class SpeciesDetail extends Activity implements View.OnTouchListener{
public class SpeciesDetail extends Activity {
	private StaticDBHelper sDBH;
	private SyncDBHelper syncDBH;
	private int species_id;
	private int category = 0;
    private ViewFlipper vf;
    private float oldTouchValue;
    private float oldTouchValue2;
    
    private VelocityTracker vTracker = null;
	
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
		myTitleText.setText(" " + getString(R.string.SpeciesDetail_info));
	    // end set-title-bar
		
		// get previous intent information
		Intent intent = getIntent();
	    species_id = intent.getExtras().getInt("id");
	    category = intent.getExtras().getInt("category");
	    
	    // declare ViewFlipper
	    //vf = (ViewFlipper) findViewById(R.id.layoutswitcher);
	    //vf.setOnTouchListener(this);
	    
	    // set the layout
	    ImageView image = (ImageView) findViewById(R.id.species_image);
	    TextView snameTxt = (TextView) findViewById(R.id.science_name);
	    TextView cnameTxt = (TextView) findViewById(R.id.common_name);
	    TextView notesTxt = (TextView) findViewById(R.id.text);
	    
	    // open database
	    SQLiteDatabase db;
	    Cursor cursor;
	    
	    sDBH = new StaticDBHelper(SpeciesDetail.this);
	    
	    db = sDBH.getReadableDatabase();
	    
	    if(species_id > 76 || category == Values.TREE_LISTS_QC) {
	    	// tree lists
	    	if(category == Values.TREE_LISTS_QC) {
	    		
	    		OneTimeDBHelper oDB = new OneTimeDBHelper(SpeciesDetail.this);
	    	    db = oDB.getReadableDatabase();
	    	    
	    	    Log.i("K", "SELECT common_name, science_name FROM uclaTreeLists WHERE id=" + species_id);
	    	    
	    		Cursor cursorTree = db.rawQuery("SELECT common_name, science_name, credit FROM userDefineLists WHERE id=" + species_id, null);
	    		while(cursorTree.moveToNext()) {
	    			String imagePath = Values.TREE_PATH + species_id + ".jpg";
	    			Log.i("K", "image Path : " + imagePath);
		    		FunctionsHelper helper = new FunctionsHelper();
		    		image.setImageBitmap(helper.showImage(SpeciesDetail.this, imagePath));
		    		
		    		snameTxt.setText(" " + cursorTree.getString(0) + " ");
		    		notesTxt.setText("Photo By " + cursorTree.getString(2));
	    		}

	    		cursorTree.close();
	    		oDB.close();
	    	}
	    	// unknown species
	    	else {
	    		cursor = db.rawQuery("SELECT _id, species_name, common_name, description FROM species WHERE _id = " + 999 + ";", null);

			    while(cursor.moveToNext()) {
			    	//snameTxt.setText(" " + cursor.getString(2) + " ");
			    	cnameTxt.setText(" " + cursor.getString(1) + " ");
			    	notesTxt.setText("" + cursor.getString(3) + " ");
					int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor.getInt(0), null, null);
					image.setImageResource(resID);
			    }
			    cursor.close();
	    	}
	    }
	    // official species
	    else {
	    	cursor = db.rawQuery("SELECT _id, species_name, common_name, description FROM species WHERE _id = " + species_id + ";", null);

		    while(cursor.moveToNext()) {
		    	snameTxt.setText(" " + cursor.getString(2) + " ");
		    	cnameTxt.setText(" " + cursor.getString(1) + " ");
		    	notesTxt.setText("" + cursor.getString(3) + " ");
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor.getInt(0), null, null);
				image.setImageResource(resID);
		    }
		    
		    cursor.close();
	    }
	        
	    LinearLayout llayout = (LinearLayout)findViewById(R.id.observation_linear_layout);
	    
	    // setting the viewflipper components
	    syncDBH = new SyncDBHelper(SpeciesDetail.this);
	    db = syncDBH.getReadableDatabase();
	    
	    cursor = db.rawQuery("SELECT species_id from my_plants;", null);

		while(cursor.moveToNext()) {
			
			if(cursor.getInt(0) == species_id)
				continue;

			// open database to read the static table
			db = sDBH.getReadableDatabase();
			
			Cursor cursor2;
			cursor2 = db.rawQuery("SELECT _id, species_name, common_name, description FROM species WHERE _id = " + cursor.getInt(0) + ";", null);
		    	
			while(cursor2.moveToNext()) {	    
			    if(cursor2.getInt(0) == species_id)
					continue;
			    
			    LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			    View itemView = inflater.inflate(R.layout.species_detail, null);
		 	     
		 	    TextView t1 = (TextView)itemView.findViewById(R.id.science_name);
		 	    TextView t2 = (TextView)itemView.findViewById(R.id.common_name);
		 	    TextView note = (TextView)itemView.findViewById(R.id.text);
		 	    ImageView image2 = (ImageView)itemView.findViewById(R.id.species_image);
				    	
			    t1.setText(" " + cursor2.getString(2) + " ");
			    t2.setText(" " + cursor2.getString(1) + " ");
			    note.setText("" + cursor2.getString(3) + " ");
			    image2.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor2.getInt(0), null, null));
				 	
			    //llayout.addView(itemView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				//vf.addView(itemView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		    }
			cursor2.close();
	    }
		//vf.removeAllViews();
		//vf.addView(llayout);
	    
		cursor.close();
		db.close();
		sDBH.close();
		syncDBH.close();
	    // TODO Auto-generated method stub

	}
	
	public void onResume() {
		super.onResume();
		
		//vf.setOnTouchListener(SpeciesDetail.this);
	}
	
	/*
    // press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			vf.removeAllViews();
			finish();
			return true;
		}
		return false;
	}
	*/
/*	
	public boolean onTouchEvent(MotionEvent touchevent) {
		// TODO Auto-generated method stub
			return true;
	}

*/
/*
	@Override
	public boolean onTouch(View v, MotionEvent touchevent) {
		// TODO Auto-generated method stub
		if(v != vf) {
			Log.i("K", "FALSE~~ touch event");
			return false;
		}
			
		else {
			switch (touchevent.getAction()) {
				case MotionEvent.ACTION_DOWN:
				{
					oldTouchValue = touchevent.getX();
					Log.i("K","oldTouchValue : " + oldTouchValue);
					break;
				}
				case MotionEvent.ACTION_UP:
				{
					float currentX = touchevent.getX();
	
					Log.i("K","currentX : " + currentX);
					if (oldTouchValue < currentX) {
						vf.setInAnimation(AnimationHelper.inFromLeftAnimation());
						vf.setOutAnimation(AnimationHelper.outToRightAnimation());
						vf.showNext();
						Log.i("K", "Right to Left");
					}
					
					if (oldTouchValue > currentX) {
						vf.setInAnimation(AnimationHelper.inFromRightAnimation());
						vf.setOutAnimation(AnimationHelper.outToLeftAnimation());
						vf.showPrevious();
						Log.i("K", "Left to Right");
					}
					
					break;
				}
			}
		}
		return true;
	}
*/
}
