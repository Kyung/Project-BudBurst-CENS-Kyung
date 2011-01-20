package cens.ucla.edu.budburst.helper;

import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.SpeciesDetail;

public class MyListAdapter extends BaseAdapter{
	Context maincon;
	LayoutInflater Inflater;
	ArrayList<PlantItem> arSrc;
	int layout;
	int previous_site = 0;
	
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
		return arSrc.get(position).CommonName;
	}
	
	public long getItemId(int position){
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null)
			convertView = Inflater.inflate(layout, parent, false);
	
		//current_position = arSrc.get(position).SpeciesID;
		
		ImageView img = (ImageView)convertView.findViewById(R.id.icon);
		img.setImageResource(arSrc.get(position).Picture);

		TextView textname = (TextView)convertView.findViewById(R.id.commonname);
		textname.setText(arSrc.get(position).CommonName);
		
		TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
		
		if(arSrc.get(position).SpeciesName.equals("Unknown/Other")) {
			textdesc.setText("Unknown/Other");
		}
		else {
			String [] splits = arSrc.get(position).SpeciesName.split(" ");
			textdesc.setText(splits[0] + " " + splits[1]);
		}
		
		// call View from the xml and link the view to current position.
		View thumbnail = convertView.findViewById(R.id.wrap_icon);
		thumbnail.setTag(arSrc.get(position));
		thumbnail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PlantItem pi = (PlantItem)v.getTag();
				
				Intent intent = new Intent(maincon, SpeciesDetail.class);
				intent.putExtra("id", pi.SpeciesID);
				intent.putExtra("site_id", "");
				maincon.startActivity(intent);
			}
		});
		return convertView;
	}
}
