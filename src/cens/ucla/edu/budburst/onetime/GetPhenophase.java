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
import cens.ucla.edu.budburst.helper.BackgroundService;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.MyListAdapter2;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
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
	private String common_name = null;
	private String science_name = null;
	private String dt_taken = null;
	private String camera_image_id = null;
	
	private int species_id = 0;
	private int protocol_id;
	private int _position = 0;
	private int previous_activity;
	
	private Button submitBtn = null;
	private TextView myTitleText = null;
	private MyListAdapter2 MyAdapter = null;
	private ListView myList = null;
	
	private double latitude = 0.0;
	private double longitude = 0.0;

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
	    camera_image_id = intent.getExtras().getString("camera_image_id");
		dt_taken = intent.getExtras().getString("dt_taken");
		previous_activity = intent.getExtras().getInt("from");
		if(previous_activity == Values.FROM_UCLA_TREE_LISTS) {
			common_name = intent.getExtras().getString("cname");
			science_name = intent.getExtras().getString("sname");
			species_id = intent.getExtras().getInt("tree_id");
		}
		
		
		// retrieve latitude and longitude from SharedPreferences
		SharedPreferences pref = getSharedPreferences("userinfo", 0);
		latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
		longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
		
		pItem = new ArrayList<PlantItem>();

		SQLiteDatabase db;
		StaticDBHelper staticDB = new StaticDBHelper(GetPhenophase.this);
		
		db = staticDB.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("SELECT _id, Type, Phenophase_ID, Phenophase_Icon, Chrono_Order, Description FROM Onetime_Observation ORDER BY Chrono_Order;", null);
		while(cursor.moveToNext()) {
			boolean header = false;
			
			if(cursor.getInt(0) == 1 || cursor.getInt(0) == 4 || cursor.getInt(0) == 6 || cursor.getInt(0) == 8 || cursor.getInt(0) == 11) {
				header = true;
			}
			
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + cursor.getInt(3), null, null);
			PlantItem pi = new PlantItem(resID, cursor.getString(5), cursor.getInt(3), cursor.getString(1), cursor.getInt(2), header);
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
		if(previous_activity == Values.FROM_UCLA_TREE_LISTS) {
			Intent intent = new Intent(GetPhenophase.this, AddSite.class);
			intent.putExtra("cname", common_name);
			intent.putExtra("sname", science_name);
			intent.putExtra("protocol_id", 9); // temporary put protocol_id to 9
			intent.putExtra("camera_image_id", camera_image_id);
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
			intent.putExtra("dt_taken", dt_taken);
			intent.putExtra("pheno_id", position + 1);
			intent.putExtra("species_id", species_id);
			intent.putExtra("notes", "");
			// for the from value, as the parameters passed are the same as FROM_ONETIME_DIRECT, let's use that.
			intent.putExtra("from", Values.FROM_UCLA_TREE_LISTS);
			startActivity(intent);
		}
		else {
			Intent intent = new Intent(GetPhenophase.this, OneTimeMain.class);
			
			intent.putExtra("camera_image_id", camera_image_id);
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);
			intent.putExtra("dt_taken", dt_taken);
			intent.putExtra("pheno_id", position + 1);
			intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
			
			startActivity(intent);
		}
	}
}
