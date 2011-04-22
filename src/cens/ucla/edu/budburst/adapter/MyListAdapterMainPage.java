package cens.ucla.edu.budburst.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.HelperListItem;

public class MyListAdapterMainPage extends BaseAdapter{
	Context maincon;
	LayoutInflater Inflater;
	ArrayList<HelperListItem> arSrc;
	int layout;
	
	public MyListAdapterMainPage(Context context, int alayout, ArrayList<HelperListItem> aarSrc){
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
		
		img.setImageResource(maincon.getResources().getIdentifier("cens.ucla.edu.budburst:drawable/"+arSrc.get(position).ImageUrl, null, null));
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
