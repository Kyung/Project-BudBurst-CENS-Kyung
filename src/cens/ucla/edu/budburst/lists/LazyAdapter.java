package cens.ucla.edu.budburst.lists;

import java.util.ArrayList;

import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.SpeciesDetail;
import cens.ucla.edu.budburst.helper.PlantItem;
import cens.ucla.edu.budburst.helper.Values;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class LazyAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<PlantItem> localArray;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
	
	public LazyAdapter(Context context, ArrayList<PlantItem> localArray) {
	    this.context = context;
	    this.localArray = localArray;
	    
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(context.getApplicationContext());
	    // TODO Auto-generated method stub
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return localArray.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

    public static class ViewHolder{
        public TextView cname;
        public TextView sname;
        public ImageView image;
        public View thumbnail;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        ViewHolder holder;
        if(convertView == null){
            vi = inflater.inflate(R.layout.locallist_item, null);
            holder = new ViewHolder();
            holder.cname = (TextView)vi.findViewById(R.id.common_name);
            holder.sname = (TextView)vi.findViewById(R.id.science_name);
            holder.image = (ImageView)vi.findViewById(R.id.thumbnail);
            holder.thumbnail = vi.findViewById(R.id.wrap_icon);
            vi.setTag(holder);
        }
        else
            holder = (ViewHolder)vi.getTag();
        
        holder.cname.setText(localArray.get(position).CommonName);
        holder.sname.setText(localArray.get(position).SpeciesName);
        holder.image.setTag(localArray.get(position).imageUrl);
        imageLoader.DisplayImage(localArray.get(position).imageUrl, context, holder.image);
        
        /*
        holder.thumbnail.setTag(localArray.get(position));
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PlantItem pi = (PlantItem)v.getTag();

				Intent intent = new Intent(context, ListsDetail.class);
				intent.putExtra("from", Values.FROM_LOCAL_PLANT_LISTS);
				intent.putExtra("category", pi.Category);
				intent.putExtra("science_name", pi.SpeciesName);
				context.startActivity(intent);
			}
		});
		*/
        return vi;
	}
}
