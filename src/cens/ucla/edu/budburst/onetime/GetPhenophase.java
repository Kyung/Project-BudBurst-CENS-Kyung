package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;
import java.util.HashMap;

import cens.ucla.edu.budburst.AddPlant;
import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.GetPhenophase_OneTime;
import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.PhenophaseDetail;
import cens.ucla.edu.budburst.PlantInformation_Direct;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapter2;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.BackgroundService;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.Values;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class GetPhenophase extends ListActivity {
	
	private ArrayList<PlantItem> pItem;
	private String commonName = null;
	private String scienceName = null;
	//private String dt_taken = null;
	private String cameraImageID = null;
	
	private int speciesID = 0;
	private int protocolID = 1;
	private int _position = 0;
	private int previousActivity;
	private int category = 0;
	
	private Button submitBtn = null;
	private TextView myTitleText = null;
	private MyListAdapter2 MyAdapter = null;
	private ListView myList = null;
	
	//private double latitude = 0.0;
	//private double longitude = 0.0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.getphenophase);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.Best_Phenophase));

		Intent intent = getIntent();
		cameraImageID = intent.getExtras().getString("camera_image_id");
	    commonName = intent.getExtras().getString("cname");
	    scienceName = intent.getExtras().getString("sname");
	    previousActivity = intent.getExtras().getInt("from");
	    protocolID = intent.getExtras().getInt("protocol_id");
		
		Log.i("K", "Previous_activity(GetPhenophase) : " + previousActivity);
		
		if(previousActivity == Values.FROM_UCLA_TREE_LISTS) {
			speciesID = intent.getExtras().getInt("tree_id");
		}
		
		if(previousActivity == Values.FROM_LOCAL_PLANT_LISTS) {
			category = intent.getExtras().getInt("category");
			speciesID = intent.getExtras().getInt("species_id");
		}
		
		if(previousActivity == Values.FROM_QUICK_CAPTURE 
				|| previousActivity == Values.FROM_PLANT_LIST_ADD_SAMESPECIES 
				|| previousActivity == Values.FROM_QUICK_CAPTURE_ADD_SAMESPECIES) {
			speciesID = intent.getExtras().getInt("species_id");
			category = intent.getExtras().getInt("category");
		}
		
		pItem = new ArrayList<PlantItem>();

		SQLiteDatabase db;
		StaticDBHelper staticDB = new StaticDBHelper(GetPhenophase.this);
		
		db = staticDB.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT _id, Type, Phenophase_Icon, Description FROM Onetime_Observation WHERE Category=" + protocolID, null);
		while(cursor.moveToNext()) {
			boolean header = false;
			
			Log.i("K", "PROTOCOL ID : " + protocolID);
			
			/*
			 * This is for choosing the list items with the header information.
			 */
			if(protocolID == 1) {
				// to show the header, we need to know the first index of each category.
				if(cursor.getInt(0) == 1 
						|| cursor.getInt(0) == 4 
						|| cursor.getInt(0) == 7 
						|| cursor.getInt(0) == 10
						|| cursor.getInt(0) == 13) {
					header = true;
				}
			}
			else if(protocolID == 2) {
				// to show the header, we need to know the first index of each category.
				if(cursor.getInt(0) == 16 
						|| cursor.getInt(0) == 19) {
					header = true;
				}

			}
			else if(protocolID == 3) {
				// to show the header, we need to know the first index of each category.
				if(cursor.getInt(0) == 22 
						|| cursor.getInt(0) == 23 
						|| cursor.getInt(0) == 26 ) {
					header = true;
				}
			}
			
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + cursor.getInt(2), null, null);
			PlantItem pi = new PlantItem(resID, cursor.getString(3), cursor.getInt(2), cursor.getString(1), cursor.getInt(0), header);
			pItem.add(pi);
		}
		
		cursor.close();
		db.close();

		MyAdapter = new MyListAdapter2(GetPhenophase.this, R.layout.phenophaselist, pItem);
		myList = getListView(); 
		myList.setAdapter(MyAdapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){

		//GetPhenophase.this.unregisterReceiver(receiver);
		if(previousActivity == Values.FROM_UCLA_TREE_LISTS) {
			Intent intent = new Intent(GetPhenophase.this, AddNotes.class);
			
			intent.putExtra("cname", commonName);
			intent.putExtra("sname", scienceName);
			intent.putExtra("protocol_id", protocolID); // temporary put protocol_id to 9
			intent.putExtra("camera_image_id", cameraImageID);
			intent.putExtra("pheno_id", pItem.get(position).PhenoID);
			intent.putExtra("species_id", speciesID);
			intent.putExtra("category", -1);
			
			// for the from value, as the parameters passed are the same as FROM_ONETIME_DIRECT, let's use that.
			intent.putExtra("from", Values.FROM_UCLA_TREE_LISTS);
			startActivity(intent);
		}
		else if(previousActivity == Values.FROM_LOCAL_PLANT_LISTS 
				|| previousActivity == Values.FROM_PLANT_LIST_ADD_SAMESPECIES) {
			Intent intent = new Intent(GetPhenophase.this, AddNotes.class);
			
			intent.putExtra("cname", commonName);
			intent.putExtra("sname", scienceName);
			intent.putExtra("protocol_id", protocolID); // temporary put protocol_id to 9
			intent.putExtra("camera_image_id", cameraImageID);
			intent.putExtra("pheno_id", pItem.get(position).PhenoID);
			intent.putExtra("species_id", 0); // there's no species_id in LOCAL PLANT LISTS
			intent.putExtra("category", category);
			intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
			startActivity(intent);
		}
		else if(previousActivity == Values.FROM_QUICK_CAPTURE
				|| previousActivity == Values.FROM_QUICK_CAPTURE_ADD_SAMESPECIES) {
			Intent intent = new Intent(GetPhenophase.this, AddNotes.class);
			
			intent.putExtra("cname", commonName);
			intent.putExtra("sname", scienceName);
			intent.putExtra("protocol_id", protocolID); // temporary put protocol_id to 9
			intent.putExtra("camera_image_id", cameraImageID);
			intent.putExtra("pheno_id", pItem.get(position).PhenoID);
			intent.putExtra("species_id", speciesID);
			intent.putExtra("category", category);
			intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
			startActivity(intent);
		}
		/*
		 * If the previous activity is from "Quick Share" on the PlantLists
		 */
		/*
		 * 
		else {
			Intent intent = new Intent(GetPhenophase.this, OneTimeMain.class);
			
			intent.putExtra("camera_image_id", camera_image_id);
			intent.putExtra("pheno_id", position + 1);
			intent.putExtra("FROM", Values.FROM_QUICK_CAPTURE);
			
			startActivity(intent);
		}
		*/
	}
}
