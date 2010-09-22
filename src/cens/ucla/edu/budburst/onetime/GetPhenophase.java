package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;

import cens.ucla.edu.budburst.PlantInfo;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.StaticDBHelper;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class GetPhenophase extends ListActivity {
	
	private ArrayList<PlantItem> pItem;
	private int protocol_id;
	private String cname = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.getphenophase);
	    
	    Intent intent = getIntent();
	    protocol_id = intent.getExtras().getInt("protocol_id");
	    cname = intent.getExtras().getString("cname");
	    
	    Log.i("K", protocol_id + "");
	    
	    pItem = new ArrayList<PlantItem>();
	    PlantItem pi;
	    
	    ArrayList<Integer> id = new ArrayList<Integer>();
	    ArrayList<Integer> phenophase = new ArrayList<Integer>();
	    ArrayList<String> description = new ArrayList<String>();
	    
		StaticDBHelper sDBHelper = new StaticDBHelper(GetPhenophase.this);
		SQLiteDatabase sDB = sDBHelper.getReadableDatabase();
	    
		String query = null;
		if(cname.equals("Others")) {
			query = "SELECT _id, Phenophase_Icon, description FROM Phenophase_Protocol_Icon ORDER BY Phenophase_Icon ASC";
		}
		else {
			query = "SELECT _id, Phenophase_Icon, description FROM Phenophase_Protocol_Icon WHERE Protocol_ID=" + protocol_id + " ORDER BY Phenophase_Icon ASC";
		}
	    
	    
	    Cursor cursor = sDB.rawQuery(query, null);
		
	    while(cursor.moveToNext()) {
	    	id.add(cursor.getInt(0));
	    	phenophase.add(cursor.getInt(1));
	    	description.add(cursor.getString(2));
	    }
	     
	    for(int i = 0 ; i < phenophase.size() ; i++) {
	    	int _id = getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + phenophase.get(i), null, null);
	    	String des = description.get(i);
	    	pi = new PlantItem(_id, des, phenophase.get(i));
	    	pItem.add(pi);
	    }
	    
	    MyListAdapter MyAdapter = new MyListAdapter(this, R.layout.phenophaselist, pItem);
	    
	    ListView myList = getListView();
	    myList.setAdapter(MyAdapter);
	    
	    // TODO Auto-generated method stub
	    cursor.close();
	    sDBHelper.close();
		sDB.close();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		Intent intent = new Intent(this, PlantInfo.class);
		intent.putExtra("species_id", pItem.get(position).Pheno_image);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	class PlantItem{
		PlantItem(int aPicture, String aNote, int pheno_img_id){
			Picture = aPicture;
			Note = aNote;
			Pheno_image = pheno_img_id;
		}
		int Picture;
		String Note;
		int Pheno_image;
	}
	
	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<PlantItem> arSrc;
		int layout;
		
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
			return arSrc.get(position).Note;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			ImageView img = (ImageView)convertView.findViewById(R.id.pheno_img);
			img.setImageResource(arSrc.get(position).Picture);
			
			TextView textname = (TextView)convertView.findViewById(R.id.pheno_text);
			textname.setText(arSrc.get(position).Note);
		
			return convertView;
		}
	}
}
