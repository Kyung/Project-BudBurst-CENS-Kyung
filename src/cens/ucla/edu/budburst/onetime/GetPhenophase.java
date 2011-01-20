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
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
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
	private String cname = null;
	private String sname = null;
	private String dt_taken = null;
	private String camera_image_id = null;
	
	private int species_id = 0;
	private int protocol_id;
	private int _position = 0;
	private int previous_activity;
	
	private Button submitBtn = null;
	private TextView myTitleText = null;
	private MyListAdapter MyAdapter = null;
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
		//latitude = intent.getExtras().getDouble("latitude");
		//longitude = intent.getExtras().getDouble("longitude");
		dt_taken = intent.getExtras().getString("dt_taken");
		
		
		SharedPreferences pref = getSharedPreferences("userinfo", 0);
		if(pref.getBoolean("new", false)) {
		    latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
		    
		    longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
		}
		
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

		MyAdapter = new MyListAdapter(GetPhenophase.this, R.layout.phenophaselist, pItem);
		myList = getListView(); 
		myList.setAdapter(MyAdapter);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		Intent service = new Intent(GetPhenophase.this, BackgroundService.class);
	    stopService(service);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){

		//GetPhenophase.this.unregisterReceiver(receiver);
		
		Intent intent = new Intent(GetPhenophase.this, OneTimeMain.class);
		
		intent.putExtra("camera_image_id", camera_image_id);
		intent.putExtra("latitude", latitude);
		intent.putExtra("longitude", longitude);
		intent.putExtra("dt_taken", dt_taken);
		intent.putExtra("pheno_id", position + 1);
		intent.putExtra("from", Values.FROM_QUICK_CAPTURE);
		
		startActivity(intent);
	}
	
	class PlantItem{
		PlantItem(int aPicture, String aNote, int pheno_img_id, String aPheno_name, int aPheno_id, Boolean aHeader){
			Picture = aPicture;
			Note = aNote;
			Pheno_image = pheno_img_id;
			Pheno_name = aPheno_name;
			Header = aHeader;
			Pheno_id = aPheno_id;
		}
		int Picture;
		String Note;
		int Pheno_image;
		int Pheno_id;
		String Pheno_name;
		Boolean Header;
	}
	
	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<PlantItem> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<PlantItem> aarSrc){
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}
		
		public int getCount(){
			return arSrc.size();
		}
		
		public String getItem(int position){
			return arSrc.get(position).Note;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			ImageView img = (ImageView)convertView.findViewById(R.id.pheno_img);
			img.setImageResource(arSrc.get(position).Picture);

			TextView header = (TextView)convertView.findViewById(R.id.list_header);
			if(arSrc.get(position).Header) {
				header.setVisibility(View.VISIBLE);
				header.setText(arSrc.get(position).Pheno_name);
			}
			else {
				header.setVisibility(View.GONE);
			}
			
			
			View thumbnail = convertView.findViewById(R.id.wrap_icon);
			thumbnail.setTag(arSrc.get(position));
			thumbnail.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					PlantItem pi = (PlantItem)v.getTag();
					
					Intent intent = new Intent(GetPhenophase.this, PhenophaseDetail.class);
					intent.putExtra("id", pi.Pheno_id);
					intent.putExtra("frome", Values.FROM_QC_PHENOPHASE);
					startActivity(intent);
				}
			});
			
			
			TextView pheno_name = (TextView)convertView.findViewById(R.id.pheno_name);
			pheno_name.setVisibility(View.GONE);
			
			TextView textname = (TextView)convertView.findViewById(R.id.pheno_text);
			textname.setText(arSrc.get(position).Note);

			return convertView;
		}
	}
}
