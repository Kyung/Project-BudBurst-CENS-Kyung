package cens.ucla.edu.budburst.adapter;

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
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.myplants.DetailPlantInfo;
import cens.ucla.edu.budburst.utils.PBBItems;

public class MyListAdapter extends BaseAdapter{
	Context maincon;
	LayoutInflater Inflater;
	ArrayList<HelperPlantItem> arSrc;
	int layout;
	int previous_site = 0;
	
	public MyListAdapter(Context context, int alayout, ArrayList<HelperPlantItem> aarSrc){
		maincon = context;
		Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		arSrc = aarSrc;
		layout = alayout;
	}
	
	public int getCount(){
		return arSrc.size();
	}
	
	public String getItem(int position){
		return arSrc.get(position).getCommonName();
	}
	
	public long getItemId(int position){
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null)
			convertView = Inflater.inflate(layout, parent, false);
	
		//current_position = arSrc.get(position).SpeciesID;
		
		ImageView img = (ImageView)convertView.findViewById(R.id.icon);
		img.setImageResource(arSrc.get(position).getPicture());

		TextView textname = (TextView)convertView.findViewById(R.id.commonname);
		textname.setText(arSrc.get(position).getCommonName());
		
		TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
		
		if(arSrc.get(position).getSpeciesName().equals("Unknown/Other")) {
			textdesc.setText("Unknown/Other");
		}
		else {
			String [] splits = arSrc.get(position).getSpeciesName().split(" ");
			textdesc.setText(splits[0] + " " + splits[1]);
		}
		
		// call View from the xml and link the view to current position.
		View thumbnail = convertView.findViewById(R.id.wrap_icon);
		thumbnail.setTag(arSrc.get(position));
		thumbnail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HelperPlantItem pi = (HelperPlantItem)v.getTag();
				
				Intent intent = new Intent(maincon, DetailPlantInfo.class);
				PBBItems pbbItem = new PBBItems();
				pbbItem.setSpeciesID(pi.getSpeciesID());
				pbbItem.setCommonName(pi.getCommonName());
				pbbItem.setCategory(pi.getCategory());
				intent.putExtra("pbbItem", pbbItem);
				maincon.startActivity(intent);
			}
		});
		return convertView;
	}
}
