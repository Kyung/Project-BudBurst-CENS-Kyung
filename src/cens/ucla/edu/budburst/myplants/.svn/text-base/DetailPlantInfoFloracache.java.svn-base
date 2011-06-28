package cens.ucla.edu.budburst.myplants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapterFloracacheOther;
import cens.ucla.edu.budburst.adapter.MyListAdapterMainPage;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.floracaching.DownloadFloracacheObInfo;
import cens.ucla.edu.budburst.floracaching.FloracacheItem;
import cens.ucla.edu.budburst.helper.HelperDrawableManager;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperListItem;
import cens.ucla.edu.budburst.helper.HelperLocalPlantListItem;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.lists.ListGroupItem;
import cens.ucla.edu.budburst.lists.ListMain;
import cens.ucla.edu.budburst.utils.PBBItems;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DetailPlantInfoFloracache extends Activity {

	private StaticDBHelper sDBH;
	private SyncDBHelper syncDBH;
	
	private int mSpeciesID;
	private int mCategory = 1;
	private int mFloracacheID = 1;
	private int mTotalObserved = 0;
	private String mCommonName;
	private String mScienceName;
	
	private ListView listView;
    private ImageView speciesImage;
    private TextView snameTxt;
    private TextView cnameTxt;
    private TextView notesTxt;
    private TextView otherTxt;
    
    private HelperFunctionCalls mHelper;
    private MyListAdapterFloracacheOther mFloraApdater;
    private ArrayList<FloracacheItem> mArr;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	   
		setContentView(R.layout.detail_plantinfo_floracache);
		
		mHelper = new HelperFunctionCalls();
		
		Bundle bundle = getIntent().getExtras();
		PBBItems pbbItem = bundle.getParcelable("pbbItem");
		
		mSpeciesID = pbbItem.getSpeciesID();
		mCategory = pbbItem.getCategory();
		mFloracacheID = pbbItem.getFloracacheID();
		mCommonName = pbbItem.getCommonName();
		mScienceName = pbbItem.getScienceName();
		
		Log.i("K", "speciesID : " + mSpeciesID + ", Category:" +mCategory + 
				",Cname:" +mCommonName +
				",Sname:" + mScienceName);
		
	    // set the layout
		listView = (ListView) findViewById(R.id.listview);
		speciesImage = (ImageView) findViewById(R.id.species_image);
		speciesImage.setBackgroundResource(R.drawable.shapedrawable);
	    snameTxt = (TextView) findViewById(R.id.science_name);
	    cnameTxt = (TextView) findViewById(R.id.common_name);
	    notesTxt = (TextView) findViewById(R.id.text);
	    otherTxt = (TextView) findViewById(R.id.num_others_floracache);
	    
	    printUserCreatedSpecies();
	    
	    // TODO Auto-generated method stub
	    // Context context, ArrayList<FloracacheItem> fArr, int floracacheID, ListView listView, TextView oText
	    DownloadFloracacheObInfo fInfo = new DownloadFloracacheObInfo(DetailPlantInfoFloracache.this, mArr, mFloracacheID, listView, otherTxt, 4);
	    fInfo.execute();
	}
	
	/**
	 * show the information of user defined species.
	 */
	public void printUserCreatedSpecies() {
		
		OneTimeDBHelper otDBH = new OneTimeDBHelper(DetailPlantInfoFloracache.this);
		SQLiteDatabase db = otDBH.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("" +
				"SELECT id, common_name, science_name, credit, description " +
				"FROM userDefineLists " +
				"WHERE science_name = \"" + mScienceName + "\" " +
				"AND category =" + mCategory + ";", null);
		
		if(cursor.getCount() > 0) {
			while(cursor.moveToNext()) {
		    	snameTxt.setText(" " + cursor.getString(2) + " ");
		    	cnameTxt.setText(" " + cursor.getString(1) + " ");
		    	notesTxt.setText("Credit: " + cursor.getString(3) + "\n" +
		    			"Description: " + cursor.getString(4));
		    	
		    	Log.i("K", "cursor.getInt(0) : " + cursor.getInt(0));
		    	
		    	Bitmap icon = null;
		    	icon = mHelper.getUserDefinedListImageFromSDCard(DetailPlantInfoFloracache.this, cursor.getInt(0), icon);
		    	
		    	speciesImage.setImageBitmap(icon);
		    }
		}
		else {
			// if nothing returns, show the default image.
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/pbb_icon_main2", null, null);
			speciesImage.setImageResource(resID);
		}
		
	    
	    cursor.close();
	    db.close();
	}

	
	public void onResume() {
		super.onResume();
	}

	
	
}
