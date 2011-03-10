package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cens.ucla.edu.budburst.AddPlant;
import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.GetPhenophase_PBB;
import cens.ucla.edu.budburst.Help;
import cens.ucla.edu.budburst.Login;
import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.helper.BackgroundService;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.lists.ListMain;
import cens.ucla.edu.budburst.lists.UserDefinedTreeLists;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OneTimeMain extends ListActivity {

	ArrayList<Button> buttonBar = new ArrayList<Button>();

	private String camera_image_id = "";
	private String new_plant_site_name;
	private String notes = "";
	private String common_name = "Unknown/Other";
	
	private double latitude = 0.0;
	private double longitude = 0.0;
	private float accuracy = 0;
	
	private int new_plant_species_id;
	private int new_plant_site_id;
	private int pheno_id;
	private int previous_activity = 0;
	
	private TextView myTitleText = null;
	private EditText unknownText = null;
	//private Button noteBtn = null;
	//private Button submitBtn = null;
	//private Button siteBtn = null;
	private Button skipBtn = null;
	private EditText et1 = null;
	
	private MyListAdapter mylistapdater;
	private SharedPreferences pref;
	FunctionsHelper helper;
	Dialog noteDialog = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.onetimemain);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);
		
		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.OneTimeMain_Header));
	    
	    pref = getSharedPreferences("userinfo",0);
	    SharedPreferences.Editor edit = pref.edit();				
		edit.putString("visited","false");
		edit.commit();
		
		Intent p_intent = getIntent();
		previous_activity = p_intent.getExtras().getInt("FROM");
		
		helper = new FunctionsHelper();
		LinearLayout ll = (LinearLayout)findViewById(R.id.header_item);
		
		Log.i("K","previous_activity : " + previous_activity);

		// if previous activity is "PlantList.java"
		// this page view is different by the previous activity
		if(previous_activity == Values.FROM_PLANT_LIST) {
			ll.setVisibility(View.GONE);
			latitude = 0.0;
			longitude = 0.0;
			camera_image_id = "none";
		}
		// else
		else {
			ll.setVisibility(View.VISIBLE);
			camera_image_id = p_intent.getExtras().getString("camera_image_id");
			pheno_id = p_intent.getExtras().getInt("pheno_id");
			
		}
	    // TODO Auto-generated method stub
	}

	public void onResume() {
		super.onResume();
		
		ArrayList<oneTime> onetime_title = new ArrayList<oneTime>();
		oneTime otime;
		
		//oneTime(Header String, title, icon_name, sub_title)
		otime = new oneTime("Local plants from national plant lists", "Project Budburst", "pbb_icon_main", "Project Budburst");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Local Invasives", "invasive_plant", "Help locate invasive plants");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Local Native", "whatsnative", "Native and cultural plants");
		onetime_title.add(otime);
		
		otime = new oneTime("Locally created lists of interest", "UCLA Trees", "s1000", "Tree species on campus");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "What's Blooming", "pbbicon", "Santa Monica blooming plants");
		onetime_title.add(otime);
		
		

		mylistapdater = new MyListAdapter(OneTimeMain.this, R.layout.onetime_list ,onetime_title);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);
		
		skipBtn = (Button)findViewById(R.id.skip);
		
		skipBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String dt_taken = new SimpleDateFormat("dd MMMMM yyy").format(new Date());
				
				Intent intent = new Intent(OneTimeMain.this, AddNotes.class);
				intent.putExtra("cname", "Unknown/Other");
				intent.putExtra("sname", "Unknown/Other");
				intent.putExtra("dt_taken", dt_taken);
				intent.putExtra("protocol_id", 9); // temporary put protocol_id to 9
				intent.putExtra("pheno_id", pheno_id);
				intent.putExtra("species_id", Values.UNKNOWN_SPECIES);
				intent.putExtra("camera_image_id", camera_image_id);
				intent.putExtra("latitude", latitude);
				intent.putExtra("longitude", longitude);
				intent.putExtra("from", previous_activity);
				
				startActivity(intent);
			}
		});
		
		/*
		
		submitBtn = (Button)findViewById(R.id.submit);
		
		submitBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				SharedPreferences pref = getSharedPreferences("userinfo", 0);
				
				boolean highly = pref.getBoolean("highly", false);
				latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
				longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
				accuracy = Float.parseFloat(pref.getString("accuracy", "0"));
				
				Log.i("K", "HIGHLY : " + highly + " latitude : " + latitude + " accuracy : " + accuracy);
				
				
				if(latitude == 0.0 || longitude == 0.0 || accuracy == 0) {
					new AlertDialog.Builder(OneTimeMain.this)
					.setTitle("Done Quick Capture")
					.setMessage("Save without GeoLocation?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							user_define_name();
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					})
					.show();
				}
				else if(highly == false){
					new AlertDialog.Builder(OneTimeMain.this)
					.setTitle("Done Quick Capture")
					.setMessage("Save with GPS info - \n" + String.format("%6.3f / %6.3f \u00b1 %3.1fm", latitude, longitude, accuracy) + "?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							user_define_name();
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					})
					.show();
				}
				else {
					user_define_name();
				}
			}
		});
		*/
		/*
		siteBtn = (Button) findViewById(R.id.movetosite);
		siteBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				String dt_taken = new SimpleDateFormat("dd MMMMM yyy").format(new Date());
				
				Intent intent = new Intent(OneTimeMain.this, AddSite.class);
				intent.putExtra("cname", "Unknown/Other");
				intent.putExtra("sname", "Unknown/Other");
				intent.putExtra("dt_taken", dt_taken);
				intent.putExtra("protocol_id", 9); // temporary put protocol_id to 9
				intent.putExtra("pheno_id", pheno_id);
				intent.putExtra("species_id", Values.UNKNOWN_SPECIES);
				intent.putExtra("camera_image_id", camera_image_id);
				intent.putExtra("latitude", latitude);
				intent.putExtra("longitude", longitude);
				intent.putExtra("notes", notes);
				intent.putExtra("from", Values.FROM_ONETIME_DIRECT);
				
				startActivity(intent);

			}
		});
		*/
		/*
		noteBtn = (Button) findViewById(R.id.notes);
		
		noteBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				noteDialog = new Dialog(OneTimeMain.this);
				
				noteDialog.setContentView(R.layout.add_note_custom_dialog);
				noteDialog.setCancelable(true);
				noteDialog.show();
				
				et1 = (EditText)noteDialog.findViewById(R.id.custom_notes);
				if(!notes.equals("")) {
					et1.setText(notes);
				}
				
				Button doneBtn = (Button)noteDialog.findViewById(R.id.custom_done);
				
				doneBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if(notes != "") {
							Toast.makeText(OneTimeMain.this, "Updated Notes", Toast.LENGTH_SHORT).show();
						}
						else {
							Toast.makeText(OneTimeMain.this, "Added Notes", Toast.LENGTH_SHORT).show();
						}
						notes = et1.getText().toString();
						noteDialog.dismiss();
					}
				});
			}
		});
		*/
	}
	
	/*
	public void user_define_name() {
		Dialog dialog = new Dialog(OneTimeMain.this);
		
		dialog.setContentView(R.layout.species_name_custom_dialog);
		dialog.setTitle(getString(R.string.GetPhenophase_PBB_message));
		dialog.setCancelable(true);
		dialog.show();
		
		et1 = (EditText)dialog.findViewById(R.id.custom_common_name);
		Button doneBtn = (Button)dialog.findViewById(R.id.custom_done);
		
		doneBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				common_name= et1.getText().toString();
				if(common_name.equals("")) {
					common_name = "Unknown/Other";
				}
				
				helper.insertNewSharedPlantToDB(OneTimeMain.this, Values.UNKNOWN_SPECIES, 0, 9, common_name, "Unknown/Other", Values.NORMAL_QC);
				int getID = helper.getID(OneTimeMain.this);
				helper.insertNewObservation(OneTimeMain.this, getID, pheno_id, latitude, longitude, accuracy, camera_image_id, notes);
				
				Intent intent = new Intent(OneTimeMain.this, PlantList.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				
				// add vibration when done
				Toast.makeText(OneTimeMain.this, getString(R.string.QuickCapture_Added), Toast.LENGTH_SHORT).show();
				Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(500);
				finish();
			}
		});
	}
	*/

	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<oneTime> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<oneTime> aarSrc){
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}
		
		public int getCount(){
			return arSrc.size();
		}
		
		public String getItem(int position){
			return arSrc.get(position).title;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);
			
			img.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/"+arSrc.get(position).image_url, null, null));
			//img.setBackgroundResource(R.drawable.shapedrawable);
			
			TextView header_view = (TextView) convertView.findViewById(R.id.list_header);
			TextView title_view = (TextView) convertView.findViewById(R.id.list_name);
			TextView title_desc = (TextView) convertView.findViewById(R.id.list_name_detail);
			
			// if the header is not "none", show the header on the screen.
			if(!arSrc.get(position).header.equals("none")) {
				header_view.setText(" " + arSrc.get(position).header);
				header_view.setVisibility(View.VISIBLE);
			}
			else {
				header_view.setVisibility(View.GONE);
			}
			
			title_view.setText(arSrc.get(position).title);
			title_desc.setText(arSrc.get(position).description + " ");
	
			return convertView;
		}
	}
	
	class oneTime{	
		oneTime(String aHeader, String aTitle, String aImage_url, String aDescription){
			header = aHeader;
			title = aTitle;
			image_url = aImage_url;
			description = aDescription;
		}
		
		String header;
		String title;
		String image_url;
		String description;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
	
		Intent intent = null;
		
		switch(position) {
		/*
		 * 0 : BudBurst
		 * 1 : Invasive
		 * 2 : Native
		 * 3 : UCLA Trees
		 * 4 : Blooming
		 */
		case 0:
			if(previous_activity == Values.FROM_PLANT_LIST) {
				intent = new Intent(OneTimeMain.this, AddPlant.class);
				startActivity(intent);
			}
			else {
				intent = new Intent(OneTimeMain.this, Flora_Observer.class);
				intent.putExtra("camera_image_id", camera_image_id);
				intent.putExtra("latitude", latitude);
				intent.putExtra("longitude", longitude);
				intent.putExtra("pheno_id", pheno_id);
				intent.putExtra("notes", notes);
				startActivity(intent);
			}
			break;
		case 1:
			
		   	intent = new Intent(OneTimeMain.this, Whatsinvasive.class);
		    intent.putExtra("FROM", previous_activity);
		    intent.putExtra("camera_image_id", camera_image_id);
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
			intent.putExtra("pheno_id", pheno_id);
			intent.putExtra("notes", notes);
			
			//Something is wrong about the View - need to fix
			startActivity(intent);
			break;
		case 2:
			//Whats Native
			Toast.makeText(OneTimeMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
			break;
		case 3:
			//UCLA Trees
			if(pref.getBoolean("getTreeLists", false)) {
				intent = new Intent(OneTimeMain.this, UserDefinedTreeLists.class);
				intent.putExtra("from", previous_activity);
				intent.putExtra("camera_image_id", camera_image_id);
				intent.putExtra("latitude", latitude);
				intent.putExtra("longitude", longitude);
				intent.putExtra("pheno_id", pheno_id);
				startActivity(intent);
			}
			else {
				Toast.makeText(OneTimeMain.this, "Still downloading the tree lists", Toast.LENGTH_SHORT).show();
			}
			break;
		case 4:
			//What's blooming
			Toast.makeText(OneTimeMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
			break;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */

	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0,"Help").setIcon(android.R.drawable.ic_menu_help);
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case 1:
				intent = new Intent(OneTimeMain.this, Help.class);
				intent.putExtra("from", Values.FROM_ONE_TIME_MAIN);
				startActivity(intent);
				return true;
		}
		return false;
	}
}











