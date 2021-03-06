package cens.ucla.edu.budburst.floracaching;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.helper.HelperDrawableManager;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.myplants.DetailPlantInfoFloracache;
import cens.ucla.edu.budburst.onetime.OneTimePhenophase;
import cens.ucla.edu.budburst.utils.PBBItems;
import cens.ucla.edu.budburst.utils.QuickCapture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FloracacheDetail extends Activity {

	private int mFloracacheID;
	
	private TextView myTitleText;
	private TextView cName;
	private TextView sName;
	private TextView descriptionTxt1;
	private TextView descriptionTxt2;
	private TextView descriptionTxt3;
	private TextView descriptionTxt4;
	private TextView otherTxt;
	private ListView listView;
	private Button makeObservationTxt;
	private ImageView speciesImage;
	private ProgressBar mSpinner;
	private Intent mPintent;
	private PBBItems pbbItem;
	
	private MyListAdapterFloracacheOther mFloraApdater;
    private ArrayList<FloracacheItem> mArr;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.floracache_detail);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" Floracache Species Info");
	    
		Bundle bundle = getIntent().getExtras();
	    pbbItem = bundle.getParcelable("pbbItem");
	    mFloracacheID = pbbItem.getFloracacheID();
	    
	    speciesImage = (ImageView) findViewById(R.id.webimage);
		mSpinner = (ProgressBar) findViewById(R.id.progressbar);
		cName = (TextView) findViewById(R.id.common_name);
		sName = (TextView) findViewById(R.id.science_name);
		listView = (ListView) findViewById(R.id.listview);
		descriptionTxt1 = (TextView) findViewById(R.id.description1);
		descriptionTxt2 = (TextView) findViewById(R.id.description2);
		descriptionTxt3 = (TextView) findViewById(R.id.description3);
		descriptionTxt4 = (TextView) findViewById(R.id.description4);
		otherTxt = (TextView) findViewById(R.id.num_others_floracache);
		makeObservationTxt = (Button) findViewById(R.id.done);

		
		OneTimeDBHelper oDBH = new OneTimeDBHelper(this);
		FloracacheItem fItem = oDBH.getFloracacheInfo(this, mFloracacheID);
		
		String getPhotoImageURL = getString(R.string.get_floracache_species_image) + fItem.getImageID();
		
		HelperDrawableManager dm = new HelperDrawableManager(this, mSpinner, speciesImage);
		dm.fetchDrawableOnThread(getPhotoImageURL);
		
		String mNote = fItem.getUserNote();
		if(mNote == null) {
			mNote = "(No Notes)";
		}
		
		cName.setText(fItem.getCommonName());
		sName.setText(fItem.getScienceName());
		descriptionTxt1.setText("+ Observed By: " + fItem.getUserName());
		descriptionTxt2.setText("+ Observed Date: " + fItem.getObservedDate());
		descriptionTxt3.setText("+ User's Note: " + mNote);
		descriptionTxt4.setText("+ Floracache Note: " + fItem.getFloracacheNotes());

		/**
		 * Others' Floracache observations
		 */
		DownloadFloracacheObInfo fInfo = new DownloadFloracacheObInfo(FloracacheDetail.this, mArr, mFloracacheID, listView, otherTxt, 2);
	    fInfo.execute();
	
	    
	    // TODO Auto-generated method stub
		makeObservationTxt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				showDialog();
				
			}
		});
	}
	
	private void showDialog() {
		
		// move to a camera mode.
		Intent intent = new Intent(FloracacheDetail.this, QuickCapture.class);
		intent.putExtra("pbbItem", pbbItem);
		intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);	
		startActivity(intent);
		
		/**
		new AlertDialog.Builder(FloracacheDetail.this)
		.setTitle(getString(R.string.PlantInfo_makeObs))
		.setMessage(getString(R.string.Floracache_Easy_Success))
		.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// Move to QuickCapture
				Intent intent = new Intent(FloracacheDetail.this, QuickCapture.class);
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);	
				startActivity(intent);

			}
		})
		.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// Move to Getphenophase without a photo.
				Intent intent = new Intent(FloracacheDetail.this, OneTimePhenophase.class);
				pbbItem.setLocalImageName("");
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
				
				startActivity(intent);
			}
		})
		.setNegativeButton(getString(R.string.Button_Cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		})
		.show();
		*/
	}
}
