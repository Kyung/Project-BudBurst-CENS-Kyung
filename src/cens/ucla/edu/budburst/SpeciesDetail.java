package cens.ucla.edu.budburst;


import cens.ucla.edu.budburst.helper.AnimationHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class SpeciesDetail extends Activity {

	private StaticDBHelper sDBH;
	private SyncDBHelper syncDBH;
	private int species_id;
	private int site_id;
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
		myTitleText.setText("  Species Information");
	    
		Intent intent = getIntent();
	    species_id = intent.getExtras().getInt("id");
	    site_id = intent.getExtras().getInt("site_id");
	    
	    vf = (ViewFlipper) findViewById(R.id.layoutswitcher);
	    
	    ImageView image = (ImageView) findViewById(R.id.species_image);
	    TextView snameTxt = (TextView) findViewById(R.id.science_name);
	    TextView cnameTxt = (TextView) findViewById(R.id.common_name);
	    TextView notesTxt = (TextView) findViewById(R.id.text);
	    
	    
	    SQLiteDatabase db;
	    Cursor cursor;
	    
	    sDBH = new StaticDBHelper(SpeciesDetail.this);
	    
	    db = sDBH.getReadableDatabase();
	
	    cursor = db.rawQuery("SELECT _id, species_name, common_name, description FROM species WHERE _id = " + species_id + ";", null);
	    
	    Log.i("K", "SELECT _id, species_name, common_name, description FROM species WHERE _id = " + species_id);
	    
	    while(cursor.moveToNext()) {
	    	snameTxt.setText(" " + cursor.getString(1) + " ");
	    	cnameTxt.setText(" " + cursor.getString(2) + " ");
	    	notesTxt.setText("" + cursor.getString(3) + " ");
	    	
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor.getInt(0), null, null);
		
			image.setImageResource(resID);
	    }
	    
 
	    syncDBH = new SyncDBHelper(SpeciesDetail.this);
	    db = syncDBH.getReadableDatabase();
	    
	    cursor = db.rawQuery("SELECT species_id from my_plants;", null);
	    
	    Log.i("K", "COUNT : " + cursor.getCount());
	    
	    while(cursor.moveToNext()) {
	    	
	    	Log.i("K", "CURSOR : " + cursor.getInt(0));
	    	
		    db = sDBH.getReadableDatabase();
		    Cursor cursor2;
		    cursor2 = db.rawQuery("SELECT _id, species_name, common_name, description FROM species WHERE _id = " + cursor.getInt(0) + ";", null);
	    	
		    while(cursor2.moveToNext()) {
		    	
		    	if(cursor2.getInt(0) == species_id)
		    		continue;
		    	
		    	LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		 	    View itemView = inflater.inflate(R.layout.species_detail, null);
		 	    
		 	    TextView t1 = (TextView)itemView.findViewById(R.id.science_name);
		 	    t1.setText(" " + cursor2.getString(1) + " ");
		 	    
		 	    TextView t2 = (TextView)itemView.findViewById(R.id.common_name);
		 	    t2.setText(" " + cursor2.getString(2) + " ");
		 	    
		 	    TextView note = (TextView)itemView.findViewById(R.id.text);
		 	    note.setText("" + cursor2.getString(3) + " ");
		 	    
		 	    ImageView image2 = (ImageView) itemView.findViewById(R.id.species_image);
		 	    int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor2.getInt(0), null, null);
		 	    image2.setImageResource(resID);
		 	    
		 	    vf.addView(itemView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		 	    
		    }
		    cursor2.close();
	    }
	    
		cursor.close();
		db.close();
		sDBH.close();
		syncDBH.close();

	    // TODO Auto-generated method stub
	}
	
	public boolean onTouchEvent(MotionEvent touchevent) {
		switch (touchevent.getAction()) {
			case MotionEvent.ACTION_DOWN:
			{
				oldTouchValue = touchevent.getX();
				oldTouchValue2 = touchevent.getY();

				Log.i("K","oldTouchValue : " + oldTouchValue);
				Log.i("K","oldTouchValue2 : " + oldTouchValue2);
				Log.i("K", "Action Started");

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
		return true;
	}

}
