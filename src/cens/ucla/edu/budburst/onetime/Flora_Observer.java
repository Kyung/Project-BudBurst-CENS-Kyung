package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.onetime.OneTimeMain.MyListAdapter;
import cens.ucla.edu.budburst.onetime.OneTimeMain.oneTime;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Flora_Observer extends ListActivity {

	private MyListAdapter mylistapdater;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.flora_observer);
	
	    // TODO Auto-generated method stub
	}
	
	public void onResume() {
		super.onResume();
		
		ArrayList<oneTime> onetime_title = new ArrayList<oneTime>();
		oneTime otime;
		
		otime = new oneTime("One Time Observation", "Wild Flowers and Herbs", "");
		onetime_title.add(otime);
		otime = new oneTime("none", "Grass", "");
		onetime_title.add(otime);
		otime = new oneTime("none", "Deciduous Trees and Shrubs", "");
		onetime_title.add(otime);
		otime = new oneTime("none", "Evergreen Trees and Shrubs", "");
		onetime_title.add(otime);
		otime = new oneTime("none", "Conifer", "");
		onetime_title.add(otime);
		otime = new oneTime("none", "Others", "");
		onetime_title.add(otime);

		mylistapdater = new MyListAdapter(Flora_Observer.this, R.layout.onetime_list ,onetime_title);
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
			/*
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);

			String imagePath = TEMP_PATH + arSrc.get(position).image_url + ".jpg";
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			
			try{
				FileOutputStream out = new FileOutputStream(imagePath);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
			}catch(Exception e){
				Log.e("K", e.toString());
			}
		
			img.setBackgroundResource(R.drawable.shapedrawable);
			img.setImageBitmap(bitmap);
			*/
			
			TextView header_view = (TextView) convertView.findViewById(R.id.list_header);
			TextView title_view = (TextView) convertView.findViewById(R.id.list_name);
			

			if(!arSrc.get(position).header.equals("none")) {
				header_view.setText(" " + arSrc.get(position).header);
				header_view.setVisibility(View.VISIBLE);
			}
			else {
				header_view.setVisibility(View.GONE);
			}
			
			Log.i("K", "TITLE : " + arSrc.get(position).title);
			
			title_view.setText(" " + arSrc.get(position).title);
	
			return convertView;
		}
	}
	
	class oneTime{	
		oneTime(String aHeader, String aTitle, String aImage_url){
			header = aHeader;
			title = aTitle;
			image_url = aImage_url;
		}
		
		String header;
		String title;
		String image_url;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		String[] category = getResources().getStringArray(R.array.recommend);
		
		if(position != 5) {
			Intent intent = new Intent(Flora_Observer.this, Observation.class);
			
			switch(position) {
			case 0:
				intent.putExtra("SelectedList", "Wild Flowers and Herbs");
				break;
			case 1:
				intent.putExtra("SelectedList", "Grass");
				break;
			case 2:
				intent.putExtra("SelectedList", "Deciduous Trees and Shrubs");
				break;
			case 3:
				intent.putExtra("SelectedList", "Evergreen Trees and Shrubs");
				break;
			case 4:
				intent.putExtra("SelectedList", "Conifer");
				break;
			}
			startActivity(intent);
		}
		else {
			Intent intent = new Intent(Flora_Observer.this, GetSpeciesInfo.class);
			intent.putExtra("cname", "Others");
			intent.putExtra("sname", "Others");
			intent.putExtra("species_id", 999);
			intent.putExtra("protocol_id", 0);
			
			startActivity(intent);
		}
	}
}
