package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import cens.ucla.edu.budburst.Login;
import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.R.drawable;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import cens.ucla.edu.budburst.helper.SyncDBHelper;
import cens.ucla.edu.budburst.onetime.Whatsinvasive.MyListAdapter;
import cens.ucla.edu.budburst.onetime.Whatsinvasive.species;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OneTimeMain extends ListActivity {

	ArrayList<Button> buttonBar = new ArrayList<Button>();
	private MyListAdapter mylistapdater;
	private SharedPreferences pref;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.onetimemain);
	    
	    pref = getSharedPreferences("userinfo",0);
	    SharedPreferences.Editor edit = pref.edit();				
		edit.putString("visited","false");
		edit.commit();

	    // TODO Auto-generated method stub
	}
	

	public void onResume() {
		super.onResume();
		
		//My plant button
		Button buttonMyplant = (Button)findViewById(R.id.myplant);
		buttonMyplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(OneTimeMain.this, PlantList.class);
				startActivity(intent);
			}
		});
		
		ArrayList<oneTime> onetime_title = new ArrayList<oneTime>();
		oneTime otime;
		
		otime = new oneTime("Map", "My Nearest Plants", "map", "");
		onetime_title.add(otime);
		
		otime = new oneTime("Recommendation List", "One Time Observation", "pbbicon", "Project Budburst");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "What's Invasive", "invasive_plant", "Help locate invasive plants");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "What's Blooming", "whatsblooming", "Local plants in flower now");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "What's Native", "whatsnative", "Native and cultural plants");
		onetime_title.add(otime);

		// What's popular is currently not available
		//otime = new oneTime("none", "What's Popular", "");
		//onetime_title.add(otime);

		mylistapdater = new MyListAdapter(OneTimeMain.this, R.layout.onetime_list ,onetime_title);
		ListView MyList = getListView();
		MyList.setAdapter(mylistapdater);

	}
	
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
			
			
			if(!arSrc.get(position).header.equals("none")) {
				header_view.setText(" " + arSrc.get(position).header);
				header_view.setVisibility(View.VISIBLE);
			}
			else {
				header_view.setVisibility(View.GONE);
			}
			
			Log.i("K", "TITLE : " + arSrc.get(position).title);
			
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
		
		if(position == 0) {
			Intent intent = new Intent(OneTimeMain.this, MyNearestPlants.class);
			startActivity(intent);
		}
		else if(position == 1) {
			Intent intent = new Intent(OneTimeMain.this, Flora_Observer.class);
			startActivity(intent);
		}
		else {
			Intent intent = new Intent(OneTimeMain.this, Recommendation.class);
			
			switch(position) {
			case 2:
				intent.putExtra("SelectedList", "What's Invasive");
				break;
			case 3:
				intent.putExtra("SelectedList", "What's Blooming");
				break;
			case 4:
				intent.putExtra("SelectedList", "What's Native");
				break;
			case 5:
				intent.putExtra("SelectedList", "What's Popular");
				break;
			}
			startActivity(intent);
		}
	}

	///////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0,"Queue").setIcon(android.R.drawable.ic_menu_sort_by_size);
		menu.add(0, 2, 0, "Help").setIcon(android.R.drawable.ic_menu_help);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case 1:
				intent = new Intent(OneTimeMain.this, Queue.class);
				startActivity(intent);
				return true;
			case 2:
				return true;
		}
		return false;
	}
}











