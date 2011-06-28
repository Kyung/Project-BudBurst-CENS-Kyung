package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;
import java.util.HashMap;

import cens.ucla.edu.budburst.PBBMainPage;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapter2;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.database.SyncDBHelper;
import cens.ucla.edu.budburst.helper.HelperBackgroundService;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.myplants.GetPhenophaseShared;
import cens.ucla.edu.budburst.myplants.PBBAddNotes;
import cens.ucla.edu.budburst.myplants.PBBAddPlant;
import cens.ucla.edu.budburst.myplants.PBBAddSite;
import cens.ucla.edu.budburst.myplants.PBBPhenophaseInfo;
import cens.ucla.edu.budburst.myplants.PBBPlantList;
import cens.ucla.edu.budburst.myplants.UpdatePlantInfo;
import cens.ucla.edu.budburst.utils.PBBItems;
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

public class OneTimePhenophase extends ListActivity {
	
	private ArrayList<HelperPlantItem> pItem;
	private String mCommonName = null;
	private String mScienceName = null;
	private String mCameraImageID = null;
	private String mImageID;
	
	private int mSpeciesID;
	private int mProtocolID;
	private int mPreviousActivity;
	private int mCategory;
	
	private Button submitBtn = null;
	private TextView myTitleText = null;
	private MyListAdapter2 MyAdapter = null;
	private ListView myList = null;
	
	private PBBItems pbbItem;
	
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

		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		
		mCommonName = pbbItem.getCommonName();
		mScienceName = pbbItem.getScienceName();
		mCameraImageID = pbbItem.getCameraImageName();
		mProtocolID = pbbItem.getProtocolID();
		mSpeciesID = pbbItem.getSpeciesID();
		mCategory = pbbItem.getCategory();
		
		mPreviousActivity = bundle.getInt("from");
		Log.i("K", "Previous_activity (GetPhenophase) : " + mPreviousActivity);
		Log.i("K", "Category (Getphenophase) : " + mCategory);
		Log.i("K", "protocolID (Getphenophase) : " + mProtocolID);
		Log.i("K", "mCameraImageID (Getphenophase) : " + mCameraImageID);
		// if the previous activity is from LOCAL_PLANT_LISTS,
		// there is one more value, "image_id"
		if(mPreviousActivity == HelperValues.FROM_LOCAL_PLANT_LISTS) {
			mImageID = bundle.getString("image_id");
			
			Log.i("K", "GetPhenophase(imageID) : " + mImageID);
		}
				
		pItem = new ArrayList<HelperPlantItem>();

		SQLiteDatabase db;
		StaticDBHelper staticDB = new StaticDBHelper(OneTimePhenophase.this);
		
		db = staticDB.getReadableDatabase();
		
		String query = null;
		if(mProtocolID == 1) {
			query = "SELECT _id, Type, Phenophase_Icon, Description FROM Onetime_Observation WHERE Protocol_ID=1";
		}
		else if(mProtocolID == 2) {
			query = "SELECT _id, Type, Phenophase_Icon, Description FROM Onetime_Observation WHERE Protocol_ID=2";
		}
		else {
			query = "SELECT _id, Type, Phenophase_Icon, Description FROM Onetime_Observation WHERE Protocol_ID=3";
		}
		
		Cursor cursor = db.rawQuery(query, null);
		while(cursor.moveToNext()) {
			boolean header = false;
			
			Log.i("K", "PROTOCOL ID : " + mProtocolID);
			
			/*
			 * This is for choosing the list items with the header information.
			 */
			if(mProtocolID == 1) {
				// to show the header, we need to know the first index of each category.
				if(cursor.getInt(0) == 1 
						|| cursor.getInt(0) == 4 
						|| cursor.getInt(0) == 7 
						|| cursor.getInt(0) == 10
						|| cursor.getInt(0) == 13) {
					header = true;
				}
			}
			else if(mProtocolID == 2) {
				// to show the header, we need to know the first index of each category.
				if(cursor.getInt(0) == 16 
						|| cursor.getInt(0) == 19) {
					header = true;
				}

			}
			else if(mProtocolID == 3) {
				// to show the header, we need to know the first index of each category.
				if(cursor.getInt(0) == 22 
						|| cursor.getInt(0) == 23 
						|| cursor.getInt(0) == 26 ) {
					header = true;
				}
			}
			
			int resID = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + cursor.getInt(2), null, null);
			//public HelperPlantItem(int aPicture, String aNote, int aPhenoImageID, String aPhenoName, int aPhenoID, Boolean aHeader){
			HelperPlantItem pi = new HelperPlantItem();
			pi.setPicture(resID);
			pi.setNote(cursor.getString(3));
			pi.setPhenoImageID(cursor.getInt(2));
			pi.setPhenoName(cursor.getString(1));
			pi.setPhenoID(cursor.getInt(0));
			pi.setHeader(header);
			pItem.add(pi);
		}
		
		cursor.close();
		db.close();

		MyAdapter = new MyListAdapter2(OneTimePhenophase.this, R.layout.phenophaselist, pItem);
		myList = getListView(); 
		myList.setAdapter(MyAdapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){

		//GetPhenophase.this.unregisterReceiver(receiver);
		if(mPreviousActivity == HelperValues.FROM_USER_DEFINED_LISTS) {
			Intent intent = new Intent(OneTimePhenophase.this, PBBAddNotes.class);
			pbbItem.setPhenophaseID(pItem.get(position).getPhenoID());
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("from", HelperValues.FROM_USER_DEFINED_LISTS);
			startActivity(intent);
		}
		else if(mPreviousActivity == HelperValues.FROM_LOCAL_PLANT_LISTS 
				|| mPreviousActivity == HelperValues.FROM_PLANT_LIST_ADD_SAMESPECIES) {
			Intent intent = new Intent(OneTimePhenophase.this, PBBAddNotes.class);
			pbbItem.setPhenophaseID(pItem.get(position).getPhenoID());
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("image_id", mImageID);
			intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
			startActivity(intent);
		}
		else if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE
				|| mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE_ADD_SAMESPECIES) {
			Intent intent = new Intent(OneTimePhenophase.this, PBBAddNotes.class);
			pbbItem.setPhenophaseID(pItem.get(position).getPhenoID());
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("from", HelperValues.FROM_QUICK_CAPTURE);
			startActivity(intent);
		}
	}
}
