package cens.ucla.edu.budburst.lists;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import cens.ucla.edu.budburst.AddPlant;
import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.DrawableManager;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.onetime.Flora_Observer;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.QuickCapture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ListsDetail extends Activity {

	private static final int COMPLETE = 0;
	private int speciesID;
	private int category;
	private int previous_activity = 0;
	private int protocolID;
	private TextView myTitleText;
	private ImageView speciesImage;
	private ProgressBar mSpinner;
	private TextView cName;
	private TextView sName;
	private TextView credit;
	private Intent p_intent;
	private String commonName;
	private String scienceName;
	private Button myplantBtn;
	private Button sharedplantBtn;
	private LinearLayout usdaLayout;
	private CharSequence[] seqUserSite;
	private FunctionsHelper helper;
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>();
	
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
		
		myplantBtn = (Button) findViewById(R.id.to_myplant);
		sharedplantBtn = (Button) findViewById(R.id.to_shared_plant);
		
		/*
		 * Call FunctionsHelper();
		 */
		helper = new FunctionsHelper();
		
		mapUserSiteNameID = helper.getUserSiteIDMap(ListsDetail.this);

	}
	
	@Override
	public void onResume() {
		
		/*
		 * Retreive user sites from database.
		 */
		seqUserSite = helper.getUserSite(ListsDetail.this);
		
		/*
		 *  Get data from the table.
		 */
	    OneTimeDBHelper otDBH = new OneTimeDBHelper(ListsDetail.this);
	    SQLiteDatabase db = otDBH.getReadableDatabase();
	    
	    Cursor cursor;
	    
	    /*
	     * If the previous activity is from "Local plants from national plant lists"
	     * 
	     */
	    if(previous_activity == Values.FROM_LOCAL_PLANT_LISTS) {
	    	
	    	category = p_intent.getIntExtra("category", 0);
	    	
	    	/*
	    	 * Show footer or not.
	    	 */
	    	boolean showFooter = p_intent.getBooleanExtra("show_footer", true);
	    	if(!showFooter) {
	    		LinearLayout footer = (LinearLayout)findViewById(R.id.lower);
	    		footer.setVisibility(View.GONE);
	    	}
	    	
	    	String science_name = p_intent.getStringExtra("science_name");
	    	
			if(category == Values.BUDBURST_LIST) {
				
				usdaLayout = (LinearLayout)findViewById(R.id.lower_info);
				usdaLayout.setVisibility(View.GONE);
				
				LinearLayout invisibleLayout = (LinearLayout)findViewById(R.id.upper_invisible);
				invisibleLayout.setVisibility(View.VISIBLE);
				
				TextView t1 = (TextView)findViewById(R.id.science_name2);
		 	    TextView t2 = (TextView)findViewById(R.id.common_name2);
		 	    TextView note = (TextView)findViewById(R.id.text);
		 	    ImageView image2 = (ImageView)findViewById(R.id.species_image);
		 	    Button moreInfoBtn = (Button)findViewById(R.id.more_info);
				
				
				StaticDBHelper staticDB = new StaticDBHelper(ListsDetail.this);
				SQLiteDatabase sDB = staticDB.getReadableDatabase();
				
				Cursor getSpeciesInfo = sDB.rawQuery("SELECT _id, species_name, common_name, description FROM species WHERE species_name = \"" + science_name + "\";", null);
				while(getSpeciesInfo.moveToNext()) {
					t1.setText(" " + getSpeciesInfo.getString(2) + " ");
				    t2.setText(" " + getSpeciesInfo.getString(1) + " ");
				    note.setText("" + getSpeciesInfo.getString(3) + " ");
				    image2.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/s"+getSpeciesInfo.getInt(0), null, null));
				    speciesID = getSpeciesInfo.getInt(0);
				}
				
				getSpeciesInfo.close();
				sDB.close();
				
				moreInfoBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						usdaLayout.setVisibility(View.VISIBLE);
					}
				});
			}
	    	
	
	    	/*
	    	 * Retrieve information from localPlantLists
	    	 */
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
				
				/*
				 * Link the url point to the USDA webpage.
				 */
				Linkify.addLinks(credit, Linkify.WEB_URLS);
				image_url = cursor.getString(5);
				
				commonName = cursor.getString(0).toString();
				scienceName = cursor.getString(1).toString();
			}
			otDBH.close();
			db.close();
			cursor.close();
			
			/*
			 * Change the size of it...
			 */
			
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150,150);
			speciesImage.setLayoutParams(layoutParams);
			
			/*
			 *  load image from the server
			 */
			DrawableManager dm = new DrawableManager(mSpinner);
			dm.fetchDrawableOnThread(image_url, speciesImage);
			
			
			myplantBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					if(category == Values.BUDBURST_LIST) {
						StaticDBHelper staticDBHelper = new StaticDBHelper(ListsDetail.this);
						SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
						Cursor c = staticDB.rawQuery("SELECT protocol_id FROM species WHERE _id = " + speciesID, null);
						while(c.moveToNext()) {
							protocolID = c.getInt(0);
						}
						c.close();
						staticDB.close();
						
						popupDialog();
					}
					else {
						speciesID = 999;
						showProtocolDialog();
					}
				}
			});	
			
			sharedplantBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					/*
					 * We already know the Shared Plant category if we choose species from official budburst lists.
					 * - hardcoded protocolID
					 */
					
					int getSpeciesID = speciesID;
					int getProtocolID = 2;
					
					StaticDBHelper staticDBH = new StaticDBHelper(ListsDetail.this);
					SQLiteDatabase staticDB = staticDBH.getReadableDatabase();
					
					Cursor cursor = staticDB.rawQuery("SELECT protocol_id FROM species WHERE _id=" + getSpeciesID + ";", null);
					while(cursor.moveToNext()) {
						getProtocolID = cursor.getInt(0);
					}
					
					switch(getProtocolID) {
					case Values.WILD_FLOWERS:
						protocolID = Values.QUICK_WILD_FLOWERS; 
						break;
					case Values.DECIDUOUS_TREES:
						protocolID = Values.QUICK_TREES_AND_SHRUBS;
						break;
					case Values.EVERGREEN_TREES:
						protocolID = Values.QUICK_TREES_AND_SHRUBS;
						break;
					case Values.CONIFERS:
						protocolID = Values.QUICK_TREES_AND_SHRUBS;
						break;
					case Values.GRASSES:
						protocolID = Values.QUICK_GRASSES;
						break;
					}
					
					cursor.close();
					staticDB.close();
					
					/*
					 * Ask users if they are ready to take a photo.
					 */
					new AlertDialog.Builder(ListsDetail.this)
					.setTitle(getString(R.string.Menu_addQCPlant))
					.setMessage(getString(R.string.Start_Shared_Plant))
					.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							/*
							 * Move to QuickCapture
							 */
							
							Intent intent = new Intent(ListsDetail.this, QuickCapture.class);
							
							intent.putExtra("cname", commonName);
							intent.putExtra("sname", scienceName);
							intent.putExtra("protocol_id", protocolID);
							intent.putExtra("category", category);
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
							Intent intent = new Intent(ListsDetail.this, GetPhenophase.class);
							intent.putExtra("camera_image_id", "");
							intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
							intent.putExtra("cname", commonName);
							intent.putExtra("sname", scienceName);
							intent.putExtra("protocol_id", protocolID);
							intent.putExtra("species_id", speciesID);
							intent.putExtra("category", category);
							
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
	    /*
	     * If from UCLA tree lists
	     * 
	     */
	    else if(previous_activity == Values.FROM_UCLA_TREE_LISTS){
	    	
	    	speciesID = p_intent.getIntExtra("id", 0); 
	    	
	    	cursor = db.rawQuery("SELECT common_name, science_name, credit FROM userDefineLists WHERE id=" + speciesID +";", null);
			while(cursor.moveToNext()) {
				cName.setText(cursor.getString(0));
				sName.setText(cursor.getString(1));
				credit.setText("Photo By - " + cursor.getString(2));
				
				commonName = cursor.getString(0).toString();
				scienceName = cursor.getString(1).toString();
			}
			otDBH.close();
			db.close();
			cursor.close();
			
			/*
			 *  Load image from the server
			 */
			DrawableManager dm = new DrawableManager(mSpinner);
			dm.fetchDrawableOnThread("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/images/treelists/" + speciesID + ".jpg", speciesImage);
			
			
			myplantBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					speciesID = 999;
					showProtocolDialog();
				}
			});
			
			sharedplantBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					/*
					 * Ask users if they are ready to take a photo.
					 */
					new AlertDialog.Builder(ListsDetail.this)
					.setTitle(getString(R.string.Menu_addQCPlant))
					.setMessage(getString(R.string.Start_Shared_Plant))
					.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
	
							/*
							 * Move to QuickCapture
							 */
							Intent intent = new Intent(ListsDetail.this, QuickCapture.class);
							intent.putExtra("from", Values.FROM_UCLA_TREE_LISTS);
							intent.putExtra("tree_id", speciesID);
							intent.putExtra("cname", commonName);
							intent.putExtra("sname", scienceName);
							intent.putExtra("protocol_id", Values.QUICK_TREES_AND_SHRUBS);
							
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
							
							Intent intent = new Intent(ListsDetail.this, GetPhenophase.class);
							intent.putExtra("camera_image_id", "");
							intent.putExtra("from", Values.FROM_UCLA_TREE_LISTS);
							intent.putExtra("tree_id", speciesID);
							intent.putExtra("cname", commonName);
							intent.putExtra("sname", scienceName);
							intent.putExtra("protocol_id", Values.QUICK_TREES_AND_SHRUBS);
							
							startActivity(intent);
						}
					})
					.setNegativeButton("Back", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						
						}
					})
					.show();

					
				}
			});
	    }
	    else {
	    	
	    	speciesID = p_intent.getIntExtra("id", 0); 
	    	
	    	cursor = db.rawQuery("SELECT common_name, science_name, credit FROM userDefineLists WHERE id=" + speciesID +";", null);
			while(cursor.moveToNext()) {
				cName.setText(cursor.getString(0));
				sName.setText(cursor.getString(1));
				credit.setText("Photo By - " + cursor.getString(2));
				
				commonName = cursor.getString(0).toString();
				scienceName = cursor.getString(1).toString();
			}
			otDBH.close();
			db.close();
			cursor.close();
			
			/*
			 *  Load image from the server
			 */
			DrawableManager dm = new DrawableManager(mSpinner);
			dm.fetchDrawableOnThread("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/images/treelists/" + speciesID + ".jpg", speciesImage);
	    	
	    	
	    	LinearLayout footer = (LinearLayout)findViewById(R.id.lower);
	    	footer.setVisibility(View.GONE);
	    }
	    
	    // TODO Auto-generated method stub
		super.onResume();
	}
	
	private void showProtocolDialog() {
		new AlertDialog.Builder(ListsDetail.this)
		.setTitle(getString(R.string.AddPlant_SelectCategory))
		.setIcon(android.R.drawable.ic_menu_more)
		.setItems(R.array.category, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String[] category = getResources().getStringArray(R.array.category);
				StaticDBHelper staticDBHelper = new StaticDBHelper(ListsDetail.this);
				SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase(); 
				
				Cursor cursor = null;
				
				/*
				 * Choose category and set the protocol_id based on that. 
				 */
				
				if(category[which].equals("Wild Flowers and Herbs")) {
					cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.WILD_FLOWERS + " ORDER BY common_name;",null);
					myTitleText.setText(" " + getString(R.string.AddPlant_addFlowers));
					protocolID = Values.WILD_FLOWERS;
				}
				else if(category[which].equals("Grass")) {
					cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.GRASSES + " ORDER BY common_name;",null);
					myTitleText.setText(" " + getString(R.string.AddPlant_addGrass));
					protocolID = Values.GRASSES;
				}
				else if(category[which].equals("Deciduous Trees and Shrubs")) {
					cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.DECIDUOUS_TREES + " ORDER BY common_name;",null);
					myTitleText.setText(" " + getString(R.string.AddPlant_addDecid));
					protocolID = Values.DECIDUOUS_TREES;
				}
				else if(category[which].equals("Evergreen Trees and Shrubs")) {
					cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.EVERGREEN_TREES + " ORDER BY common_name;",null);
					myTitleText.setText(" " + getString(R.string.AddPlant_addEvergreen));
					protocolID = Values.EVERGREEN_TREES;
				}
				else if(category[which].equals("Conifer")) {
					cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + Values.CONIFERS + " ORDER BY common_name;",null);
					myTitleText.setText(" " + getString(R.string.AddPlant_addConifer));
					protocolID = Values.CONIFERS;
				}
				
				cursor.close();
				staticDB.close();
				
				popupDialog();
				
			}
		}).show();
	}
	
	private void popupDialog() {
		/*
		 * Pop up choose site dialog box
		 */
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setCancelable(true)
		.setItems(seqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			
				int new_plant_site_id = mapUserSiteNameID.get(seqUserSite[which].toString());
				String new_plant_site_name = seqUserSite[which].toString();
				
				if(new_plant_site_name == "Add New Site") {
					Intent intent = new Intent(ListsDetail.this, AddSite.class);
					intent.putExtra("species_id", speciesID);
					intent.putExtra("common_name", commonName);
					intent.putExtra("protocol_id", protocolID);
					intent.putExtra("from", Values.FROM_PLANT_LIST);
					startActivity(intent);
				}
				else {
					if(helper.checkIfNewPlantAlreadyExists(speciesID, new_plant_site_id, ListsDetail.this)){
						Toast.makeText(ListsDetail.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{

						if(helper.insertNewMyPlantToDB(ListsDetail.this, speciesID, commonName, new_plant_site_id, new_plant_site_name, protocolID)){
							Intent intent = new Intent(ListsDetail.this, PlantList.class);
							Toast.makeText(ListsDetail.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							
							/*
							 * Clear all stacked activities.
							 */
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(ListsDetail.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
}
