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
		myTitleText.setText(" " + getString(R.string.SpeciesDetail_info));
	    
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
	    
	    
	    if(species_id > 76) {
	    	image.setBackgroundResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
	    }
	    else {
	    	cursor = db.rawQuery("SELECT _id, species_name, common_name, description FROM species WHERE _id = " + species_id + ";", null);
		    
		    Log.i("K", "SELECT _id, species_name, common_name, description FROM species WHERE _id = " + species_id);
		    
		    Log.i("K", "COUNT : " + cursor.getCount());
		    
		    while(cursor.moveToNext()) {
		    	snameTxt.setText(" " + cursor.getString(1) + " ");
		    	cnameTxt.setText(" " + cursor.getString(2) + " ");
		    	notesTxt.setText("" + cursor.getString(3) + " ");
		    	
				int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor.getInt(0), null, null);
			
				image.setImageResource(resID);
		    }
		    
		    cursor.close();
	    }
	        
	    syncDBH = new SyncDBHelper(SpeciesDetail.this);
	    db = syncDBH.getReadableDatabase();
	    
	    cursor = db.rawQuery("SELECT species_id from my_plants;", null);
	    
	    //Log.i("K", "COUNT : " + cursor.getCount());

		while(cursor.moveToNext()) {
			
			//Log.i("K", "CURSOR : " + cursor.getInt(0));
			//Log.i("K", "SPECIES ID : " + species_id);
			
			if(cursor.getInt(0) == species_id)
				continue;
			
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
	 	    View itemView = inflater.inflate(R.layout.species_detail, null);
	 	    TextView t1 = (TextView)itemView.findViewById(R.id.science_name);
	 	    TextView t2 = (TextView)itemView.findViewById(R.id.common_name);
	 	    TextView note = (TextView)itemView.findViewById(R.id.text);
	 	    ImageView image2 = (ImageView) itemView.findViewById(R.id.species_image);
	 	    
		    if(cursor.getInt(0) > 76) {
		    	image2.setBackgroundResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s999", null, null));
		    	vf.addView(itemView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		    }

			db = sDBH.getReadableDatabase();
			
			Cursor cursor2;
			cursor2 = db.rawQuery("SELECT _id, species_name, common_name, description FROM species WHERE _id = " + cursor.getInt(0) + ";", null);
		    	
			while(cursor2.moveToNext()) {	    
			    if(cursor2.getInt(0) == species_id)
					continue;
				    	
				t1.setText(" " + cursor2.getString(1) + " ");
				t2.setText(" " + cursor2.getString(2) + " ");
				note.setText("" + cursor2.getString(3) + " ");
				image2.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+cursor2.getInt(0), null, null));
				 	    
				vf.addView(itemView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		    }
			cursor2.close();

	    }
	    
		cursor.close();
		db.close();
		sDBH.close();
		syncDBH.close();
	    // TODO Auto-generated method stub
	}
	
	
	
    // or when user press back button
	// when you hold the button for 3 sec, the app will be exited
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			vf.removeAllViews();
			finish();
			return true;
		}
		return false;
	}
	
	
	public boolean onTouchEvent(MotionEvent touchevent) {
			// TODO Auto-generated method stub
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
