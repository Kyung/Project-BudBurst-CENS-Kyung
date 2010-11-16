package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.util.ArrayList;
import cens.ucla.edu.budburst.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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

public class SelectPlantName extends ListActivity {

	ArrayList<Button> buttonBar = new ArrayList<Button>();
	private MyListAdapter mylistapdater;
	private SharedPreferences pref;
	private ImageView image = null;
	private String imagePath = null;
	private double latitude = 0.0;
	private double longitude = 0.0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.selectplantname);
	    
	    pref = getSharedPreferences("userinfo",0);
	    SharedPreferences.Editor edit = pref.edit();				
		edit.putString("visited","false");
		edit.commit();
		
		Intent p_intent = getIntent();
		
		imagePath = p_intent.getExtras().getString("imagePath");
		latitude = p_intent.getExtras().getDouble("latitude");
		longitude = p_intent.getExtras().getDouble("longitude");
		
		image = (ImageView) findViewById(R.id.species_image);

	    File file = new File(imagePath);
	    Bitmap bitmap = null;
	    
	    // if file exists show the photo on the ImageButton
	    if(file.exists()) {
		   	bitmap = BitmapFactory.decodeFile(imagePath);
		   	image.setImageBitmap(bitmap);
	    }
	    // if not, show 'no image' ImageButton
	    else {
	    	Log.i("K", "ERROR : NO image in the sd card!");
	    }
		
		Log.i("K", "Image Path : " + imagePath + " , lat : " + latitude + " lon : " + longitude);
	    // TODO Auto-generated method stub
	}

	public void onResume() {
		super.onResume();
		
		ArrayList<oneTime> onetime_title = new ArrayList<oneTime>();
		oneTime otime;
		
		otime = new oneTime("Flowers", "Early", "p4", "");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Peak", "p5", "");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Late", "p6", "");
		onetime_title.add(otime);
		
		otime = new oneTime("Leaves", "Early", "p18", "");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Peak", "p19", "");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Color Change", "p21", "");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Drop", "p22", "");
		onetime_title.add(otime);
		
		otime = new oneTime("Fruit", "Early", "p7", "");
		onetime_title.add(otime);
		
		otime = new oneTime("none", "Peak", "p8", "");
		onetime_title.add(otime);

		// What's popular is currently not available
		//otime = new oneTime("none", "What's Popular", "");
		//onetime_title.add(otime);

		mylistapdater = new MyListAdapter(SelectPlantName.this, R.layout.onetime_list ,onetime_title);
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

	}

	///////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0,"Help").setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 2, 0, "Update").setIcon(android.R.drawable.ic_menu_help);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case 1:
				return true;
			case 2:
				return true;
		}
		return false;
	}
}
