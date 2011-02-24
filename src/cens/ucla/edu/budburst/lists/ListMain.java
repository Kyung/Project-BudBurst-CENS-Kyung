package cens.ucla.edu.budburst.lists;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cens.ucla.edu.budburst.AddPlant;
import cens.ucla.edu.budburst.AddSite;
import cens.ucla.edu.budburst.Help;
import cens.ucla.edu.budburst.MainPage;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.onetime.Flora_Observer;
import cens.ucla.edu.budburst.onetime.OneTimeMain;
import cens.ucla.edu.budburst.onetime.Whatsinvasive;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
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

public class ListMain extends ListActivity {
	
	private TextView myTitleText = null;
	private MyListAdapter mylistapdater;
	private SharedPreferences pref;
	FunctionsHelper helper;
	
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
		myTitleText.setText(" " + getString(R.string.Menu_Lists));

		LinearLayout ll = (LinearLayout)findViewById(R.id.header_item);
		ll.setVisibility(View.GONE);

	    // TODO Auto-generated method stub
	}

	public void onResume() {
		super.onResume();
		
		
		OneTimeDBHelper otDBH = new OneTimeDBHelper(this);
		SQLiteDatabase otDB = otDBH.getReadableDatabase();

		Cursor cursor = otDB.rawQuery("SELECT category, common_name, photo_url FROM localPlantLists", null);
		
		while(cursor.moveToNext()) {
			Log.i("K", "Category : " + cursor.getInt(0) + " common_name : " + cursor.getString(1));
		}
		
		otDBH.close();
		otDB.close();
		cursor.close();
		
		
		ArrayList<listItem> onetime_title = new ArrayList<listItem>();
		listItem iItem;
		
		//oneTime(Header String, title, icon_name, sub_title)
		iItem = new listItem("Local plants from national plant lists", "Project Budburst", "pbb_icon_main", "Local BudBurst plants");
		onetime_title.add(iItem);
		
		iItem = new listItem("none", "Local Invasives", "invasive_plant", "Help locate local invasive");
		onetime_title.add(iItem);
		
		iItem = new listItem("none", "Local Native", "whatsnative", "Local natives from the USDA");
		onetime_title.add(iItem);
		
		//iItem = new listItem("none", "Local Poisonous", "", "Native and cultural plants");
		//onetime_title.add(iItem);
		
		iItem = new listItem("Locally created lists of interest", "UCLA Trees", "s1000", "Tree species on campus");
		onetime_title.add(iItem);
		
		iItem = new listItem("none", "What's Blooming", "pbbicon", "Santa Monica blooming plants");
		onetime_title.add(iItem);
		
		mylistapdater = new MyListAdapter(ListMain.this, R.layout.onetime_list ,onetime_title);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);
	
	}
	
	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<listItem> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<listItem> aarSrc){
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}
		
		public int getCount(){
			return arSrc.size();
		}
		
		public String getItem(int position){
			return arSrc.get(position).Title;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);
			
			img.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/"+arSrc.get(position).Image_Url, null, null));
			//img.setBackgroundResource(R.drawable.shapedrawable);
			
			
			TextView header_view = (TextView) convertView.findViewById(R.id.list_header);
			TextView title_view = (TextView) convertView.findViewById(R.id.list_name);
			TextView title_desc = (TextView) convertView.findViewById(R.id.list_name_detail);
			
			// if the header is not "none", show the header on the screen.
			if(!arSrc.get(position).Header.equals("none")) {
				header_view.setText(" " + arSrc.get(position).Header);
				header_view.setVisibility(View.VISIBLE);
			}
			else {
				header_view.setVisibility(View.GONE);
			}
			
			title_view.setText(arSrc.get(position).Title);
			title_desc.setText(arSrc.get(position).Description + " ");
	
			return convertView;
		}
	}
	
	class listItem{	
		listItem(String aHeader, String aTitle, String aImage_url, String aDescription){
			Header = aHeader;
			Title = aTitle;
			Image_Url = aImage_url;
			Description = aDescription;
		}
		
		String Header;
		String Title;
		String Image_Url;
		String Description;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
	
		Intent intent = null;
		
		pref = getSharedPreferences("userinfo", 0);
		double latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
		double longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
		
		if(latitude == 0.0 || longitude == 0.0) {
			Toast.makeText(ListMain.this, "Wait until GPS data received...", Toast.LENGTH_SHORT).show();
		}
		else {
			switch(position) {
			case 0:
				// Project Budburst
				//Toast.makeText(ListMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				intent = new Intent(ListMain.this, ListSubCategory.class);
				intent.putExtra("category", Values.BUDBURST_LIST);
				startActivity(intent);
				
				break;
			case 1:
				// What's Invasives
				intent = new Intent(ListMain.this, ListSubCategory.class);
				intent.putExtra("category", Values.WHATSINVASIVE_LIST);
				startActivity(intent);
				break;
			case 2:
				// Native Plants
				//intent = new Intent(ListMain.this, LocalBudBurst.class);
				//intent.putExtra("category", Values.NATIVE_LIST);
				//startActivity(intent);
				Toast.makeText(ListMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				break;
			case 3:
				// Local Plant From Users

				if(pref.getBoolean("getTreeLists", false)) {
					intent = new Intent(ListMain.this, UserDefinedTreeLists.class);
					startActivity(intent);
				}
				else {
					Toast.makeText(ListMain.this, "Still downloading the tree lists", Toast.LENGTH_SHORT).show();
				}
				break;
			case 4:
				// Whats Blooming
				Toast.makeText(ListMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				break;		
			}
		}
	}
}