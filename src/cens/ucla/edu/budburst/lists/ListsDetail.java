package cens.ucla.edu.budburst.lists;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.DrawableManager;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ListsDetail extends Activity {

	private static final int COMPLETE = 0;
	private int species_id;
	private int previous_activity = 0;
	private TextView myTitleText;
	private ImageView speciesImage;
	private ProgressBar mSpinner;
	private TextView cName;
	private TextView sName;
	private TextView credit;
	private Intent p_intent;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.listdetail);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" Species Info");
	    
	    p_intent = getIntent();
	    species_id = p_intent.getIntExtra("id", 0);
	    previous_activity = p_intent.getIntExtra("from", 0);
	    
	    // setup the layout
		speciesImage = (ImageView) findViewById(R.id.webimage);
		mSpinner = (ProgressBar) findViewById(R.id.progressbar);
		//mSpinner = new ProgressBar(this);
		//this.addContentView(mSpinner, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		//mSpinner.setIndeterminate(true);
		
		cName = (TextView) findViewById(R.id.common_name);
		sName = (TextView) findViewById(R.id.science_name);
		credit = (TextView) findViewById(R.id.credit);

	}
	
	@Override
	public void onResume() {
		
		// get data from the table.
	    OneTimeDBHelper otDBH = new OneTimeDBHelper(ListsDetail.this);
	    SQLiteDatabase db = otDBH.getReadableDatabase();
	    
	    Cursor cursor;
	    
	    /*
	     * If the previous activity is from "Local plants from national plant lists"
	     * 
	     */
	    if(previous_activity == Values.FROM_LOCAL_PLANT_LISTS) {
	    	
	    	int category = p_intent.getIntExtra("category", 0);
	    	String science_name = p_intent.getStringExtra("science_name");
	    	
	    	cursor = db.rawQuery("SELECT common_name, science_name, county, state, usda_url, photo_url, copy_right FROM localPlantLists WHERE category=" 
	    			+ category 
	    			+ " AND science_name=\"" + science_name 
	    			+"\";", null);
	    	
	    	String image_url = "";
	    	
			while(cursor.moveToNext()) {
				
				/*
				 * This is how to link the page dynamically by using Pattern and Linkify.
				 */
				
				cName.setText(cursor.getString(0));
				sName.setText(cursor.getString(1));
				credit.setText(
						"\nCounty - " + cursor.getString(2) +
						"\n\nState - " + cursor.getString(3) +
						"\n\nUSDA link : " + cursor.getString(4) +
						"\n\nPhoto By - " + cursor.getString(6));
				
				//Linkify.addLinks(credit, pattern, "");
				Linkify.addLinks(credit, Linkify.WEB_URLS);
				image_url = cursor.getString(5);
			}
			otDBH.close();
			db.close();
			cursor.close();
			
			/*
			 * Change the size of it...
			 */
			
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150,150);
			speciesImage.setLayoutParams(layoutParams);
			
			// load image from the server
			DrawableManager dm = new DrawableManager(mSpinner);
			dm.fetchDrawableOnThread(image_url, speciesImage);
	    }
	    /*
	     * If from UCLA tree lists
	     * 
	     */
	    else {
	    	cursor = db.rawQuery("SELECT common_name, science_name, credit FROM uclaTreeLists WHERE id=" + species_id +";", null);
			while(cursor.moveToNext()) {
				cName.setText(cursor.getString(0));
				sName.setText(cursor.getString(1));
				credit.setText("Photo By - " + cursor.getString(2));
			}
			otDBH.close();
			db.close();
			cursor.close();
			
			// load image from the server
			DrawableManager dm = new DrawableManager(mSpinner);
			dm.fetchDrawableOnThread("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/images/treelists/" + species_id + ".jpg", speciesImage);
	    }

	    // TODO Auto-generated method stub
		super.onResume();
	}
}
