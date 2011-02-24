package cens.ucla.edu.budburst.lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.helper.MyListAdapterWithIndex;
import cens.ucla.edu.budburst.onetime.GetPhenophase;
import cens.ucla.edu.budburst.onetime.OneTimeMain;
import cens.ucla.edu.budburst.onetime.QuickCapture;
import cens.ucla.edu.budburst.onetime.Whatsinvasive;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

public class UserDefinedTreeLists extends ListActivity {
	
	private ArrayList<PlantItem> treeLists;
	private MyListAdapterWithIndex MyAdapter = null;
	private ListView myList = null;
	private ProgressDialog dialog = null;
	private SharedPreferences pref;
	
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
		
		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" UCLA Tree Lists");
	
		getTreeLists();
		// TODO Auto-generated method stub
	}
	
	public void getTreeLists() {
		// initialize treeLists
		treeLists = null;
		treeLists = new ArrayList<PlantItem>();
		
		// open database and put all tree lists into the PlantItem
		OneTimeDBHelper otDBH = new OneTimeDBHelper(UserDefinedTreeLists.this);
		SQLiteDatabase otDB = otDBH.getReadableDatabase();
		
		Cursor cursor;
		cursor = otDB.rawQuery("SELECT id, common_name, science_name, credit FROM uclaTreeLists ORDER BY common_name;", null);
		while(cursor.moveToNext()) {
			PlantItem pi;
			pi = new PlantItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
			treeLists.add(pi);
		}
		
		MyAdapter = new MyListAdapterWithIndex(UserDefinedTreeLists.this, R.layout.plantlist_item, treeLists);
		myList = getListView();
		// need to add setFastScrollEnalbed(true) for showing the index box in the list...
		myList.setFastScrollEnabled(true);
		myList.setAdapter(MyAdapter);
		
		cursor.close();
		otDBH.close();
		otDB.close();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		Intent intent = new Intent(UserDefinedTreeLists.this, QuickCapture.class);
		intent.putExtra("from", Values.FROM_UCLA_TREE_LISTS);
		intent.putExtra("tree_id", treeLists.get(position).SpeciesID);
		intent.putExtra("cname", treeLists.get(position).CommonName);
		intent.putExtra("sname", treeLists.get(position).SpeciesName);
		startActivity(intent);
	}
}
