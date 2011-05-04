package cens.ucla.edu.budburst.adapter;

import java.io.File;
import java.util.ArrayList;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;
import cens.ucla.edu.budburst.floracaching.FloracacheDetail;
import cens.ucla.edu.budburst.helper.HelperFunctionCalls;
import cens.ucla.edu.budburst.helper.HelperListItem;
import cens.ucla.edu.budburst.helper.HelperPlantItem;
import cens.ucla.edu.budburst.helper.HelperValues;
import cens.ucla.edu.budburst.myplants.DetailPlantInfo;
import cens.ucla.edu.budburst.utils.PBBItems;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyListAdapterFloracache extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<HelperPlantItem> mArr;
	private int mLayout;
	
	public MyListAdapterFloracache(Context context, int alayout, ArrayList<HelperPlantItem> aarSrc){
		mContext = context;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mArr = aarSrc;
		mLayout = alayout;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mArr.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null)
			convertView = mInflater.inflate(mLayout, parent, false);
		
		ImageView img = (ImageView)convertView.findViewById(R.id.icon);
		
		HelperFunctionCalls helper = new HelperFunctionCalls();
		helper.showSpeciesThumbNail(mContext
				, mArr.get(position).getCategory()
				, mArr.get(position).getSpeciesID()
				, mArr.get(position).getSpeciesName()
				, img);

		TextView vTitle = (TextView) convertView.findViewById(R.id.text1);
		TextView vCredit = (TextView) convertView.findViewById(R.id.text2);
		TextView vDescription = (TextView) convertView.findViewById(R.id.text3);
		
		vTitle.setText(mArr.get(position).getCommonName());
		vCredit.setText("Credit: " + mArr.get(position).getUserName() + " ");
		vDescription.setText(mArr.get(position).getDescription());

		
		// call View from the xml and link the view to current position.
		/*
		View thumbnail = convertView.findViewById(R.id.wrap_icon);
		thumbnail.setTag(mArr.get(position));
		thumbnail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HelperPlantItem pi = (HelperPlantItem)v.getTag();
				
				Intent intent = new Intent(mContext, FloracacheDetail.class);
				intent.putExtra("floracache_id", pi.getFloracacheID());
				mContext.startActivity(intent);
			}
		});
		*/
		
		
		
		return convertView;

	}

}
