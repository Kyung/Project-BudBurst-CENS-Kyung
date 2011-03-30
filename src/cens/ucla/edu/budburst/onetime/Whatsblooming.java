package cens.ucla.edu.budburst.onetime;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.apache.http.util.ByteArrayBuffer;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.database.OneTimeDBHelper;

public class Whatsblooming extends ListActivity {
	
	private ArrayList<species> arSpeciesList;
	private MyListAdapter mylistapdater;
	private OneTimeDBHelper otDBH;

	public final String WB_PATH = "/sdcard/pbudburst/wb/";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		  // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.whatsblooming);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.flora_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  Species lists");
	    
	    arSpeciesList = new ArrayList<species>();
	    
	    // TODO Auto-generated method stub
		
		// if there is no folder, create it
		File file = new File(WB_PATH);
		if(!file.exists()) {
			file.mkdir();
		}
	}

	public void onResume() {
		super.onResume();
		
		new DoAsyncTask().execute();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK) {
			if(requestCode == 1) {
		
			}
		}
	}
	
	//query = "SELECT id, secret, farm, title, dt_taken, lat, lon, owner, server FROM flickrLists";
	class species{	
		species(int _id, String _secret, String _farm, String _title, String _dt_taken, String _lat, String _lon, String _owner, String _server, double _distance) {
			id = _id;
			secret = _secret;
			farm = _farm;
			title = _title;
			dt_taken = _dt_taken;
			lat = _lat;
			lon = _lon;
			owner = _owner;
			server = _server;
			distance = _distance;
		}
		
		int id;
		String secret;
		String farm;
		String title;
		String dt_taken;
		String lat;
		String lon;
		String owner;
		String server;
		double distance;
	}
	
	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<species> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<species> aarSrc){
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
			if(convertView == null) {
				convertView = Inflater.inflate(layout, parent, false);
			}
			
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);

			String imagePath = WB_PATH + arSrc.get(position).id + ".jpg";
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
		
			//img.setBackgroundResource(R.drawable.shapedrawable);
			img.setImageBitmap(bitmap);
	
			TextView textname = (TextView)convertView.findViewById(R.id.commonname);
			textname.setText(arSrc.get(position).title);
			
			TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
			textdesc.setText("By " + arSrc.get(position).owner + "\n" + arSrc.get(position).distance + " miles away");

			return convertView;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		//Intent intent = new Intent(Whatspopular.this, WPinfo.class);
		/*
		intent.putExtra("common_name", arSpeciesList.get(position).common_name);
		intent.putExtra("science_name", arSpeciesList.get(position).science_name);
		intent.putExtra("comment", arSpeciesList.get(position).comment);
		intent.putExtra("comment_count", arSpeciesList.get(position).comment_count);
		intent.putExtra("latitude", arSpeciesList.get(position).latitude);
		intent.putExtra("longitude", arSpeciesList.get(position).longitude);
		intent.putExtra("dt_taken", arSpeciesList.get(position).dt_taken);
		intent.putExtra("pheno", arSpeciesList.get(position).pheno);
		*/
		//startActivityForResult(intent, 1);
	}

	class DoAsyncTask extends AsyncTask<Void, Integer, Void> {
		ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(Whatsblooming.this, "Loading...", "Loading species information...", true);
		}
		@Override
		protected Void doInBackground(Void... unused) {
			
			// load data from the database...
			SQLiteDatabase db;
			otDBH = new OneTimeDBHelper(Whatsblooming.this);
			db = otDBH.getReadableDatabase();

			String query = null;
			//query = "SELECT pheno, cname, sname, lat, lng, dt_taken, c_count, comments FROM popularLists ORDER BY c_count LIMIT 20";
			query = "SELECT id, secret, farm, title, dt_taken, lat, lon, owner, server, distance FROM flickrLists ORDER BY distance ASC LIMIT 20";
			
			Cursor cursor = db.rawQuery(query, null);

		    while(cursor.moveToNext()) {
		  
				species pi;
				// SELECT id, secret, farm, title, dt_taken, lat, lon, owner, server FROM flickrLists LIMIT 10
				pi = new species(cursor.getInt(0), cursor.getString(1), cursor.getString(2), 
							cursor.getString(3), cursor.getString(4), cursor.getString(5), 
							cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getDouble(9));
				arSpeciesList.add(pi);
			
				String imageURL = "http://farm" + cursor.getString(2) 
							+ ".static.flickr.com/" + cursor.getString(8) 
							+ "/" + cursor.getString(0) + "_" 
							+ cursor.getString(1) + "_s.jpg";
				
				Log.i("K", "URL : " + imageURL);
				
				try {
					URL getURL = new URL(imageURL);
					File file = new File(cursor.getInt(0) + "");
					HttpURLConnection conn = (HttpURLConnection) getURL.openConnection();
					conn.setDoInput(true);
					conn.connect();
					
					InputStream is = conn.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);
					ByteArrayBuffer baf = new ByteArrayBuffer(50);
					int current = 0;
					while((current = bis.read()) != -1) {
						baf.append((byte) current);
					}
					
					FileOutputStream fos = new FileOutputStream(WB_PATH + file + ".jpg");
					fos.write(baf.toByteArray());
					fos.close();
				}
				catch(Exception io) {
				}
		    }
		    
		    cursor.close();
		    db.close();
			otDBH.close();
	
			// TODO Auto-generated method stub
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			mylistapdater = new MyListAdapter(Whatsblooming.this, R.layout.plantlist_item2 ,arSpeciesList);
			ListView MyList = getListView();
			MyList.setAdapter(mylistapdater);
			dialog.dismiss();
		}
	}
	
	void deleteContents(String path) {
		File file = new File(path);
		if(file.isDirectory()) {
			String[] fileList = file.list();
			
			for(int i = 0 ; i < fileList.length ; i++) {
				Log.i("K", "FILE NAME : " + "/sdcard/pbudburst/wb/" + fileList[i] + " IS DELETED.");
				new File("/sdcard/pbudburst/wb/" + fileList[i]).delete();
			}
		}
	}
	
	// or when user press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			deleteContents(WB_PATH);
			finish();
			return true;
		}
		return false;
	}
}
