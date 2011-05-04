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
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.myplants.PBBPhenophaseInfo;
import cens.ucla.edu.budburst.onetime.OneTimePhenophase;

public class MyListAdapter2 extends BaseAdapter{
	Context maincon;
	LayoutInflater Inflater;
	ArrayList<HelperPlantItem> arSrc;
	int layout;
	
	public MyListAdapter2(Context context, int alayout, ArrayList<HelperPlantItem> aarSrc){
		maincon = context;
		Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		arSrc = aarSrc;
		layout = alayout;
	}
	
	public int getCount(){
		return arSrc.size();
	}
	
	public String getItem(int position){
		return arSrc.get(position).getNote();
	}
	
	public long getItemId(int position){
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null)
			convertView = Inflater.inflate(layout, parent, false);
		
		ImageView img = (ImageView)convertView.findViewById(R.id.pheno_img);
		img.setImageResource(arSrc.get(position).getPicture());

		TextView header = (TextView)convertView.findViewById(R.id.list_header);
		if(arSrc.get(position).getHeader()) {
			header.setVisibility(View.VISIBLE);
			header.setText(arSrc.get(position).getPhenoName());
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
				HelperPlantItem pi = (HelperPlantItem)v.getTag();
				
				Intent intent = new Intent(maincon, PBBPhenophaseInfo.class);
				intent.putExtra("id", pi.getPhenoID());
				intent.putExtra("frome", HelperValues.FROM_QC_PHENOPHASE);
				maincon.startActivity(intent);
			}
		});
		
		
		TextView pheno_name = (TextView)convertView.findViewById(R.id.pheno_name);
		pheno_name.setVisibility(View.GONE);
		
		TextView textname = (TextView)convertView.findViewById(R.id.pheno_text);
		textname.setText(arSrc.get(position).getNote());

		return convertView;
	}
}
