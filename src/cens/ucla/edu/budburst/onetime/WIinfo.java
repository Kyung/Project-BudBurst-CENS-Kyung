package cens.ucla.edu.budburst.onetime;

import java.io.FileOutputStream;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.AnimationHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
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
import android.widget.TextView;
import android.widget.ViewFlipper;

public class WIinfo extends Activity {

	private OneTimeDBHelper otDBH;
	private String area_id;
    private SharedPreferences pref = null;
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
		myTitleText.setText("  Species Info");
	     
	    ImageView image = (ImageView) findViewById(R.id.species_image);
	    TextView titleTxt = (TextView) findViewById(R.id.title);
	    TextView snameTxt = (TextView) findViewById(R.id.science_name);
	    TextView notesTxt = (TextView) findViewById(R.id.text);
	    

	    Intent intent = getIntent();
	    area_id = intent.getExtras().getString("area_id");
	    String title = intent.getExtras().getString("title");
	    
	    otDBH = new OneTimeDBHelper(WIinfo.this);
	    SQLiteDatabase db;
	    db = otDBH.getReadableDatabase();
	    Cursor cursor;
	    cursor = db.rawQuery("SELECT title, cname, sname, text, image_url FROM speciesLists WHERE id = " 
	    		+ area_id + " AND title = '" + title + "';", null);
	    
	    while(cursor.moveToNext()) {
	    	titleTxt.setText(" " + cursor.getString(0) + " ");
	    	snameTxt.setText(" " + cursor.getString(2) + " ");
	    	notesTxt.setText("" + cursor.getString(3) + " ");
	    	
	    	Log.i("K", "cursor : " + cursor.getString(0));
	    
			String imagePath = TEMP_PATH + cursor.getString(4) + ".jpg";
			Log.i("K", "imagePath : " + imagePath);
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			
			try{
				FileOutputStream out = new FileOutputStream(imagePath);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
			}catch(Exception e){
				Log.e("K", e.toString());
			}
		
			image.setImageBitmap(bitmap);
	    }

	    pref = getSharedPreferences("userinfo",0);
		SharedPreferences.Editor edit = pref.edit();				
		edit.putString("visited","true");
		
		edit.commit();
		
		cursor.close();
		db.close();
		otDBH.close();
	    
	    // query database by using title...
	
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
