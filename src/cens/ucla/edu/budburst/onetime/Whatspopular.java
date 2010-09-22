package cens.ucla.edu.budburst.onetime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.maps.Overlay;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.JSONHelper;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;

public class Whatspopular extends ListActivity {
	
	private SharedPreferences pref;
	private ArrayList<species> arSpeciesList;
	private MyListAdapter mylistapdater;
	private OneTimeDBHelper otDBH;
	private String area_id;
	private String area_name;
	private Button areaBtn = null;
	private TextView areaTxt = null;
	private TextView areaTxt2 = null;
	public final String TEMP_PATH = "/sdcard/pbudburst/tmp/";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.whatspopular);
	    arSpeciesList = new ArrayList<species>();
	    
		pref = getSharedPreferences("userinfo",0);
		String username = pref.getString("Username","");
		String password = pref.getString("Password","");
	    
	    
	    new DoAsyncTask().execute();
	    // TODO Auto-generated method stub
	}
	
	public void onResume() {
		super.onResume();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK) {
			if(requestCode == 1) {
		
			}
		}
	}
	
	
	class species{	
		species(String aPheno, String aCommon_name, String aScience_name, String aLatitude, String aLongitude, String aDt_taken, int aCount, String aComment) {
			pheno = aPheno;
			common_name = aCommon_name;
			science_name = aScience_name;
			latitude = aLatitude;
			longitude = aLongitude;
			dt_taken = aDt_taken;
			comment_count = aCount;
			comment = aComment;
			//image_url = aImage_url;
		}
		
		String pheno;
		String common_name;
		String science_name;
		String latitude;
		String longitude;
		String dt_taken;
		int comment_count;
		String comment;
		//String text;
		//String image_url;
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
			return arSrc.get(position).science_name;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			ImageView img = (ImageView)convertView.findViewById(R.id.icon);
/*
			String imagePath = TEMP_PATH + arSrc.get(position).image_url + ".jpg";
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			
			try{
				FileOutputStream out = new FileOutputStream(imagePath);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
			}catch(Exception e){
				Log.e("K", e.toString());
			}
		
			img.setBackgroundResource(R.drawable.shapedrawable);
			img.setImageBitmap(bitmap);
*/			
			TextView textname = (TextView)convertView.findViewById(R.id.commonname);
			textname.setText(arSrc.get(position).common_name);
			
			TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
			textdesc.setText(arSrc.get(position).science_name);

			return convertView;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		Intent intent = new Intent(Whatspopular.this, WPinfo.class);
		intent.putExtra("common_name", arSpeciesList.get(position).common_name);
		intent.putExtra("science_name", arSpeciesList.get(position).science_name);
		intent.putExtra("comment", arSpeciesList.get(position).comment);
		intent.putExtra("comment_count", arSpeciesList.get(position).comment_count);
		intent.putExtra("latitude", arSpeciesList.get(position).latitude);
		intent.putExtra("longitude", arSpeciesList.get(position).longitude);
		intent.putExtra("dt_taken", arSpeciesList.get(position).dt_taken);
		intent.putExtra("pheno", arSpeciesList.get(position).pheno);
		startActivityForResult(intent, 1);
	}

	class DoAsyncTask extends AsyncTask<Void, Integer, Void> {
		ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(Whatspopular.this, "Loading...", "Getting Species...", true);
		}
		@Override
		protected Void doInBackground(Void... unused) {
			
			// load data from the database...
			SQLiteDatabase db;
			otDBH = new OneTimeDBHelper(Whatspopular.this);
			db = otDBH.getReadableDatabase();

			String query = null;
			query = "SELECT pheno, cname, sname, lat, lng, dt_taken, c_count, comments FROM popularLists ORDER BY c_count LIMIT 10";
			
			Cursor cursor = db.rawQuery(query, null);

		    while(cursor.moveToNext()) {
		  
				species pi;
				// pheno, cname, sname, latitude, longitude, dt_taken, comment_count, comment
				pi = new species(cursor.getString(0), cursor.getString(1), cursor.getString(2), 
							cursor.getString(3), cursor.getString(4), cursor.getString(5), 
							Integer.parseInt(cursor.getString(6)), cursor.getString(7));
				arSpeciesList.add(pi);
		
			}
		    
		    cursor.close();
		    db.close();
			otDBH.close();

			// TODO Auto-generated method stub
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			mylistapdater = new MyListAdapter(Whatspopular.this, R.layout.plantlist_item ,arSpeciesList);
			ListView MyList = getListView();
			MyList.setAdapter(mylistapdater);
			
			dialog.dismiss();

		}
	}
	
	private void download_image(String url, String fileName) {
		
		URL imageURL;
		int read;
		
		try {
			imageURL = new URL(url);
			Log.i("K", "IMAGE URL : " + imageURL);
			HttpURLConnection conn = (HttpURLConnection)imageURL.openConnection();
			conn.connect();
			
			int len = conn.getContentLength();
			byte[] buffer = new byte[len];
			InputStream is = conn.getInputStream();
			FileOutputStream fos = new FileOutputStream(TEMP_PATH + fileName + ".jpg");
			
			while ((read = is.read(buffer)) > 0) {
				fos.write(buffer, 0, read);
			}
			fos.close();
			is.close();
		}
		catch(Exception e) {
			
		}
	}
	
	static private String hexEncode( byte[] aInput){
		   StringBuilder result = new StringBuilder();
		   char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m'};
		   for ( int idx = 0; idx < 4; ++idx) {
			   byte b = aInput[idx];
			   result.append( digits[ (b&0xf0) >> 4 ] );
			   result.append( digits[ b&0x0f] );
		   }
		   return result.toString();
		}
}
