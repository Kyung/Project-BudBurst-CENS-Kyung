package cens.ucla.edu.budburst.onetime;

import java.io.FileOutputStream;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.AnimationHelper;
import cens.ucla.edu.budburst.helper.DrawableManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class WIinfo extends Activity {

	private OneTimeDBHelper otDBH;
	private String area_id;
    private SharedPreferences pref = null;
    private ProgressBar mSpinner;
    public final String TEMP_PATH = "/sdcard/pbudburst/wi_list/";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.wiinfo);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" Species Info");
	     
	    ImageView image = (ImageView) findViewById(R.id.species_image);
	    TextView titleTxt = (TextView) findViewById(R.id.title);
	    TextView snameTxt = (TextView) findViewById(R.id.science_name);
	    TextView notesTxt = (TextView) findViewById(R.id.text);
	    mSpinner = (ProgressBar) findViewById(R.id.progressbar);
	    

	    Intent intent = getIntent();
	    area_id = intent.getExtras().getString("area_id");
	    String cname = intent.getExtras().getString("cname");
	    
	    otDBH = new OneTimeDBHelper(WIinfo.this);
	    SQLiteDatabase db;
	    db = otDBH.getReadableDatabase();
	    Cursor cursor;
	    cursor = db.rawQuery("SELECT cname, sname, text, image_name FROM speciesLists WHERE id = " 
	    		+ area_id + " AND cname = '" + cname + "';", null);
	    
	    while(cursor.moveToNext()) {
	    	titleTxt.setText(" " + cursor.getString(0) + " ");
	    	snameTxt.setText(" " + cursor.getString(1) + " ");
	    	notesTxt.setText("" + cursor.getString(2) + " ");

	    	Log.i("K", "" + cursor.getString(3));
	    	
	    	DrawableManager dm = new DrawableManager(mSpinner);
	    	dm.fetchDrawableOnThread("http://www.whatsinvasive.com/ci/images/" + cursor.getString(3) + ".jpg", image);
	    }

	    pref = getSharedPreferences("userinfo",0);
		SharedPreferences.Editor edit = pref.edit();				
		edit.putString("visited","true");
		
		edit.commit();
		
		cursor.close();
		db.close();
		otDBH.close();
	
	    // TODO Auto-generated method stub
	}
	
	public void onPause() {
		super.onPause();
		
		SharedPreferences pref = getSharedPreferences("Onetime", 0);
		SharedPreferences.Editor edit = pref.edit();
		
		edit.putBoolean("visited", true);
		edit.commit();
	}
}
