package cens.ucla.edu.budburst.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.SpeciesDetail;
import cens.ucla.edu.budburst.lists.ListsDetail;
import cens.ucla.edu.budburst.onetime.Whatsinvasive;

public class MyListAdapterWithIndex extends ArrayAdapter implements SectionIndexer {
	private Context maincon;
	private LayoutInflater Inflater;
	private ArrayList<PlantItem> items;
	private int resourceID;
	private HashMap<String, Integer> alphaIndexer;
	private String[] sections;
	private String oldChar = "";

	public MyListAdapterWithIndex(Context context, int resource, ArrayList<PlantItem> items){
		super(context, 0, items);
		
		resourceID = resource;
		maincon = context;
		Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = items;
		
		alphaIndexer = new HashMap<String, Integer>();
		int size = items.size();
		
		
		
		//Toast.makeText(maincon, "" + size, Toast.LENGTH_SHORT).show();
		
		for(int i = 0 ; i < size ; i++) {
			PlantItem pi = items.get(i);
			String firstChar = pi.CommonName.substring(0,1);
			firstChar = firstChar.toUpperCase();
			
			if(oldChar.equals(firstChar)) {
				//nothing...
			}
			else {
				alphaIndexer.put(firstChar, i);
				oldChar = firstChar;
			}
		}
		
		Set<String> sectionLetters = alphaIndexer.keySet();
		
		// create a list from the set to sort
		ArrayList<String> sectionLists = new ArrayList<String>(sectionLetters);
		
		Collections.sort(sectionLists);
		
		sections = new String[sectionLists.size()];
		sectionLists.toArray(sections);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null)
			convertView = Inflater.inflate(resourceID, parent, false);
		
		//View v = super.getView(position, convertView, parent);
		//current_position = arSrc.get(position).SpeciesID;
		
		ImageView img = (ImageView)convertView.findViewById(R.id.icon);
		String imagePath = Values.TREE_PATH + items.get(position).SpeciesID + ".jpg";
		Log.i("K", "imagePath : " + imagePath);
		
		
		FunctionsHelper helper = new FunctionsHelper();
		img.setImageBitmap(helper.showImage(maincon, imagePath));

		TextView cname = (TextView)convertView.findViewById(R.id.commonname);
		cname.setText(items.get(position).CommonName);
		
		TextView sname = (TextView)convertView.findViewById(R.id.speciesname);
		sname.setText(items.get(position).SpeciesName);
		
		// we don' need to show the stat of phenophase in this view
		TextView phenoStat = (TextView)convertView.findViewById(R.id.pheno_stat);
		TextView phenoObsr = (TextView)convertView.findViewById(R.id.pheno_observed);
		
		phenoStat.setVisibility(View.GONE);
		phenoObsr.setVisibility(View.GONE);
		
		// call View from the xml and link the view to current position.
		View thumbnail = convertView.findViewById(R.id.wrap_icon);
		thumbnail.setTag(items.get(position));
		thumbnail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PlantItem pi = (PlantItem)v.getTag();

				Intent intent = new Intent(maincon, ListsDetail.class);
				intent.putExtra("id", pi.SpeciesID);
				maincon.startActivity(intent);
			}
		});
		
		return convertView;
	}

	@Override
	public int getPositionForSection(int section) {
		// TODO Auto-generated method stub
		return alphaIndexer.get(sections[section]);
	}

	@Override
	public int getSectionForPosition(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object[] getSections() {
		// TODO Auto-generated method stub
		return sections;
	}
}
