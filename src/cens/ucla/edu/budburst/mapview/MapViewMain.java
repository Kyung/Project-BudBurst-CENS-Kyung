package cens.ucla.edu.budburst.mapview;

import java.util.ArrayList;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.adapter.MyListAdapter;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.FunctionsHelper;
import cens.ucla.edu.budburst.helper.Values;
import cens.ucla.edu.budburst.lists.GetUserPlantLists;
import cens.ucla.edu.budburst.lists.ListMain;
import cens.ucla.edu.budburst.lists.ListSubCategory;
import cens.ucla.edu.budburst.lists.UserDefinedTreeLists;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MapViewMain extends ListActivity {

	private TextView mMyTitleText = null;
	private MyListAdapter mMylistapdater;
	private SharedPreferences pref;
	private FunctionsHelper mHelper;
	private ArrayList<listItem> mItemArray; 
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
		
		mMyTitleText = (TextView) findViewById(R.id.my_title);
		mMyTitleText.setText(" Plant Maps");

	    // TODO Auto-generated method stub
	}
	
	public void onResume() {
		super.onResume();

		mItemArray = new ArrayList<listItem>();
		listItem iItem;
		
		/*
		 * oneTime(Header String, title, icon_name, sub_title)
		 * - Note : don't put the icon_name in the string file.
		 */
		
		iItem = new listItem("My Observations", "My Observations", "pbb_icon_main", "See My Observations");
		mItemArray.add(iItem);

		iItem = new listItem(getString(R.string.List_Official_Header), getString(R.string.List_Project_Budburst_title), "pbb_icon_main", getString(R.string.List_Budburst));
		mItemArray.add(iItem);
		
		iItem = new listItem("none", getString(R.string.List_Whatsinvasive_title), "invasive_plant", getString(R.string.List_Whatsinvasive));
		mItemArray.add(iItem);
		
		iItem = new listItem("none", getString(R.string.List_Whatsendangered_title), "endangered", getString(R.string.List_Whats_endangered));
		mItemArray.add(iItem);
		
		iItem = new listItem("none", getString(R.string.List_Whatspoisonous_title), "poisonous", getString(R.string.List_Whats_poisonous));
		mItemArray.add(iItem);
		
		//iItem = new listItem("none", "Local Poisonous", "", "Native and cultural plants");
		//onetime_title.add(iItem);
		
		iItem = new listItem(getString(R.string.List_User_Plant_Header), "UCLA Trees", "s1000", getString(R.string.List_User_Plant_UCLA_trees));
		mItemArray.add(iItem);
		
		iItem = new listItem("none", getString(R.string.List_Whatsblooming_title), "pbbicon", getString(R.string.List_User_Plant_SAMO));
		mItemArray.add(iItem);
		
		mMylistapdater = new MyListAdapter(MapViewMain.this, R.layout.onetime_list ,mItemArray);
		ListView MyList = getListView();
		MyList.setAdapter(mMylistapdater);
	
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
			
			/*
			 *  If the header is not "none", show the header on the screen.
			 */
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

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
	
		Intent intent = null;
		
		pref = getSharedPreferences("userinfo", 0);
		double latitude = Double.parseDouble(pref.getString("latitude", "0.0"));
		double longitude = Double.parseDouble(pref.getString("longitude", "0.0"));
		
		if(!pref.getBoolean("listDownloaded", false)) {
			Toast.makeText(MapViewMain.this, "Local lists are still downloading...", Toast.LENGTH_SHORT).show();
		}
		else {
			/*
			 *  0 : My Observation
			 *  1 : Project Budburst
			 *  2 : What's Invasive
			 *  3 : Native
			 *  4 : Local Plants
			 *  5 : What's Blooming
			 *  and more later.
			 */
			switch(position) {
			case 0:
				intent = new Intent(MapViewMain.this, PBB_map.class);
				//intent.putExtra("category", Values.BUDBURST_LIST);
				startActivity(intent);
				
				break;
			case 1:
				// Budburst Plants
				Toast.makeText(MapViewMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				break;
			case 2:
				// Endangered Plants
				Toast.makeText(MapViewMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				break;
			case 3:
				// Poisonous Plants
				Toast.makeText(MapViewMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				break;
			case 4:
				// Local Plant From Users
				Toast.makeText(MapViewMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				break;
			case 5:
				// Local Plant From Users
				Toast.makeText(MapViewMain.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}
}
