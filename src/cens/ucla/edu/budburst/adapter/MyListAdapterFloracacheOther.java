package cens.ucla.edu.budburst.adapter;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.StaticDBHelper;
import cens.ucla.edu.budburst.floracaching.FloracacheItem;
import cens.ucla.edu.budburst.helper.HelperDrawableManager;
import cens.ucla.edu.budburst.myplants.DetailPlantInfoFloracache;

public class MyListAdapterFloracacheOther extends BaseAdapter{
	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<FloracacheItem> mArrList;
	private int mLayout;
	
	public MyListAdapterFloracacheOther(Context context, int alayout, ArrayList<FloracacheItem> mArrList){
		mContext = context;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mArrList = mArrList;
		mLayout = alayout;
	}
	
	public int getCount(){
		return mArrList.size();
	}
	
	public String getItem(int position){
		return mArrList.get(position).getCommonName();
	}
	
	public long getItemId(int position){
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null)
			convertView = mInflater.inflate(mLayout, parent, false);
		ProgressBar spinner =(ProgressBar) convertView.findViewById(R.id.spinner);
		
		ImageView img = (ImageView)convertView.findViewById(R.id.icon);
		HelperDrawableManager hDrawManager = new HelperDrawableManager(mContext, spinner, img);
		hDrawManager.fetchDrawableOnThread(mArrList.get(position).getImageURL());
		img.setBackgroundResource(R.drawable.shapedrawable);

		TextView textName = (TextView)convertView.findViewById(R.id.list_name);
		textName.setText("By " + mArrList.get(position).getUserName() + " ");
		
		TextView textDesc = (TextView)convertView.findViewById(R.id.list_name_detail);
		textDesc.setText("Observed Date : " + mArrList.get(position).getDate() + " ");
		
		// call View from the xml and link the view to current position.
		View thumbnail = convertView.findViewById(R.id.wrap_icon);
		thumbnail.setTag(mArrList.get(position));
		thumbnail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FloracacheItem pi = (FloracacheItem)v.getTag();
				
				final RelativeLayout linear = (RelativeLayout) View.inflate(mContext, R.layout.image_popup, null);
				
				AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
				ImageView imageView = (ImageView) linear.findViewById(R.id.main_image);
				ProgressBar spinner = (ProgressBar) linear.findViewById(R.id.spinner);
				spinner.setVisibility(View.VISIBLE);
				
				String getPhotoImageURL = pi.getImageURL();
				
				HelperDrawableManager dm = new HelperDrawableManager(mContext, spinner, imageView);
				dm.fetchDrawableOnThread(getPhotoImageURL);
				
				/**
				 * Get Pheno Name
				 */
				StaticDBHelper sDBHelper = new StaticDBHelper(mContext);
				SQLiteDatabase sDB = sDBHelper.getReadableDatabase();
				Cursor getPhenoName = sDB.rawQuery("SELECT Phenophase_Icon, Description FROM Onetime_Observation WHERE _id=" + pi.getPhenophase(), null);
				String phenoDesc = "";
				int phenoIcon = 0;
				while(getPhenoName.moveToNext()) {
					phenoIcon = getPhenoName.getInt(0);
					phenoDesc = getPhenoName.getString(1);
				}
				getPhenoName.close();
				sDB.close();
				
				/**
				 * Set layouts
				 */
				LinearLayout ll = (LinearLayout) linear.findViewById(R.id.layout1);
				ImageView phenoView = (ImageView) linear.findViewById(R.id.pheno_image);
				TextView phenoTxt = (TextView) linear.findViewById(R.id.pheno_text);
				TextView creditTxt = (TextView) linear.findViewById(R.id.credit);
				TextView dateTxt = (TextView) linear.findViewById(R.id.dates);
				TextView noteTxt = (TextView) linear.findViewById(R.id.notes);
				
				ll.setVisibility(View.VISIBLE);
				
				phenoView.setImageResource(mContext.getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + phenoIcon, null, null));
				phenoTxt.setText(phenoDesc + " ");
				creditTxt.setText("Observed By " + pi.getUserName());
				dateTxt.setText("Date : " + pi.getDate());
				noteTxt.setText("Users' note : " + pi.getUserNote());
				
		        dialog.setView(linear);
		        dialog.show();
			}
		});
		
		return convertView;
	}
}