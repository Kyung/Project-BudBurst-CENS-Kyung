package cens.ucla.edu.budburst.weeklyplant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.helper.DrawableManager;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.lists.ListsDetail;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.QuickCapture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WeeklyPlant extends Activity {

	
	private int mSpeciesID;
	private int mProtocolID;
	private int mCategory = Values.BUDBURST_LIST;
	private String mCommonName;
	private String mScienceName;
	
	private List<String> mList;
	private ImageView mUrl;
	private TextView mCredit;
	private TextView mLink;
	private TextView mSpecies;
	private TextView mHaiku;
	private ProgressBar mSpinner;
	private Button myplantBtn;
	private Button sharedplantBtn;
	private CharSequence[] mSeqUserSite;
	private FunctionsHelper mHelper;
	private HashMap<String, Integer> mMapUserSiteNameID;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.weeklyplant);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.WeeklyPlant));
	    
	    
	    mList = new ArrayList<String>();
	    mMapUserSiteNameID = new HashMap<String, Integer>();
	    
	    // setup the layout
	    mUrl = (ImageView) findViewById(R.id.species_image);
	    mCredit = (TextView) findViewById(R.id.credit);
	    mLink = (TextView) findViewById(R.id.link);
	    mSpecies = (TextView) findViewById(R.id.species_name);
	    mHaiku = (TextView) findViewById(R.id.haiku);
	    mSpinner = (ProgressBar) findViewById(R.id.progressbar);
	   
	    myplantBtn = (Button) findViewById(R.id.to_myplant);
		sharedplantBtn = (Button) findViewById(R.id.to_shared_plant);
	   
		/*
		 * Call FunctionsHelper();
		 */
		mHelper = new FunctionsHelper();
		mMapUserSiteNameID = mHelper.getUserSiteIDMap(WeeklyPlant.this);
		
		/*
		 * Download weekly plant information from the server.
		 */
	    DownloadWeeklyPlant weeklyPlant = new DownloadWeeklyPlant(this);
	    weeklyPlant.execute();	   
	    
	    // TODO Auto-generated method stub
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		/*
		 * Retreive user sites from database.
		 */
		mSeqUserSite = mHelper.getUserSite(WeeklyPlant.this);
		
		myplantBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				StaticDBHelper staticDBHelper = new StaticDBHelper(WeeklyPlant.this);
				SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
				Cursor c = staticDB.rawQuery("SELECT protocol_id FROM species WHERE _id = " + mSpeciesID, null);
				while(c.moveToNext()) {
					mProtocolID = c.getInt(0);
				}
				c.close();
				staticDB.close();
				
				popupDialog();
			}
		});	
		
		sharedplantBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				 * We already know the Shared Plant category if we choose species from official budburst lists.
				 * - hardcoded mProtocolID
				 */
				
				int getSpeciesID = mSpeciesID;
				int getProtocolID = 2;
				
				StaticDBHelper staticDBH = new StaticDBHelper(WeeklyPlant.this);
				SQLiteDatabase staticDB = staticDBH.getReadableDatabase();
				
				Cursor cursor = staticDB.rawQuery("SELECT protocol_id FROM species WHERE _id=" + getSpeciesID + ";", null);
				while(cursor.moveToNext()) {
					getProtocolID = cursor.getInt(0);
				}
				
				switch(getProtocolID) {
				case Values.WILD_FLOWERS:
					mProtocolID = Values.QUICK_WILD_FLOWERS; 
					break;
				case Values.DECIDUOUS_TREES:
					mProtocolID = Values.QUICK_TREES_AND_SHRUBS;
					break;
				case Values.EVERGREEN_TREES:
					mProtocolID = Values.QUICK_TREES_AND_SHRUBS;
					break;
				case Values.CONIFERS:
					mProtocolID = Values.QUICK_TREES_AND_SHRUBS;
					break;
				case Values.GRASSES:
					mProtocolID = Values.QUICK_GRASSES;
					break;
				}
				
				cursor.close();
				staticDB.close();
				
				/*
				 * Ask users if they are ready to take a photo.
				 */
				new AlertDialog.Builder(WeeklyPlant.this)
				.setTitle(getString(R.string.Menu_addQCPlant))
				.setMessage(getString(R.string.Start_Shared_Plant))
				.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						/*
						 * Move to QuickCapture
						 */
						
						Intent intent = new Intent(WeeklyPlant.this, QuickCapture.class);
						
						intent.putExtra("cname", mCommonName);
						intent.putExtra("sname", mScienceName);
						intent.putExtra("protocol_id", mProtocolID);
						intent.putExtra("category", mCategory);
						intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
						
						startActivity(intent);

						
						
					}
				})
				.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						/*
						 * Move to Getphenophase without a photo.
						 */
						Intent intent = new Intent(WeeklyPlant.this, GetPhenophase.class);
						intent.putExtra("camera_image_id", "");
						intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
						intent.putExtra("cname", mCommonName);
						intent.putExtra("sname", mScienceName);
						intent.putExtra("protocol_id", mProtocolID);
						intent.putExtra("species_id", mSpeciesID);
						intent.putExtra("category", mCategory);
						
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
				
			}
		});
	    
	}
	
	private void popupDialog() {
		/*
		 * Pop up choose site dialog box
		 */
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setCancelable(true)
		.setItems(mSeqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			
				int new_plant_site_id = mMapUserSiteNameID.get(mSeqUserSite[which].toString());
				String new_plant_site_name = mSeqUserSite[which].toString();
				
				if(new_plant_site_name == "Add New Site") {
					Intent intent = new Intent(WeeklyPlant.this, AddSite.class);
					intent.putExtra("species_id", mSpeciesID);
					intent.putExtra("common_name", mCommonName);
					intent.putExtra("protocol_id", mProtocolID);
					intent.putExtra("from", Values.FROM_PLANT_LIST);
					startActivity(intent);
				}
				else {
					if(mHelper.checkIfNewPlantAlreadyExists(mSpeciesID, new_plant_site_id, WeeklyPlant.this)){
						Toast.makeText(WeeklyPlant.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{

						if(mHelper.insertNewMyPlantToDB(WeeklyPlant.this, mSpeciesID, mCommonName, new_plant_site_id, new_plant_site_name, mProtocolID)){
							Intent intent = new Intent(WeeklyPlant.this, PlantList.class);
							Toast.makeText(WeeklyPlant.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							
							/*
							 * Clear all stacked activities.
							 */
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(WeeklyPlant.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
	
	/*
	 * Receive the weekly plant from the server in json format
	 */
	public class DownloadWeeklyPlant extends AsyncTask<Void, Void, Void>{

		private Context mContext;
		private ProgressDialog mDialog;
		private List<String> mList;
		private String getUrl;
		private String getCredit;
		private String getLink;
		private String getSpecies;
		private String getHaiku;
		
		public DownloadWeeklyPlant(Context context) {
			mContext = context;
			
			mList = new ArrayList<String>();
		}

		@Override
		protected void onPreExecute() {
			
			mDialog = ProgressDialog.show(mContext, "Please wait...", "Plant of the week!", true);
			mDialog.setCancelable(true);
		}

		@Override
		protected Void doInBackground(Void... unused) {
			// TODO Auto-generated method stub
		
			String url = "http://networkednaturalist.org/python_scripts/plant_of_the_week_scrape.py";
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			String result = "";
				
			try {
				HttpResponse response = httpClient.execute(httpPost);
				
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					
					StringBuilder result_str = new StringBuilder();
					String line = "";
					
					while((line = br.readLine()) != null){
						result_str.append(line);
					}
					
					result = result_str.toString();
					
					JSONObject jsonObj = new JSONObject(result);
					
					getUrl = jsonObj.getString("url");
					getCredit = jsonObj.getString("credit");
					getLink = jsonObj.getString("link");
					getSpecies = jsonObj.getString("species");
					getHaiku = jsonObj.getString("haiku");
					
					StaticDBHelper staticDBHelper = new StaticDBHelper(WeeklyPlant.this);
					OneTimeDBHelper onetimeDBHelper = new OneTimeDBHelper(WeeklyPlant.this);
					
					mSpeciesID = staticDBHelper.getSpeciesID(WeeklyPlant.this, getSpecies);
					mScienceName = staticDBHelper.getSpeciesName(WeeklyPlant.this, getSpecies);
					mCommonName = getSpecies;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		protected void onPostExecute(Void unused) {
			mDialog.dismiss();
		
		    mCredit.setText(getCredit);
		    mLink.setText(getLink);
		    mSpecies.setText(getSpecies);
		    mHaiku.setText(getHaiku);
		    
		    Linkify.addLinks(mLink, Linkify.WEB_URLS);
		    
		    DrawableManager dm = new DrawableManager(mSpinner);
			dm.fetchDrawableOnThread(getUrl, mUrl);
			
		}
		
		public List<String> onGetResults() {
			return mList;
		}
	}
}
