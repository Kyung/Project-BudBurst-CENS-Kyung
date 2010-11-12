package cens.ucla.edu.budburst.onetime;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import cens.ucla.edu.budburst.R;
import cens.ucla.edu.budburst.helper.OneTimeDBHelper;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Queue extends ListActivity {
	private ArrayList<myItem> arPlantList;
	private MyListAdapter mylistapdater;
	public final String TEMP_PATH = "/sdcard/pbudburst/tmp/";
	private int intent_image_id;
	private String intent_cname;
	private String intent_sname;
	private double intent_lat;
	private double intent_lng;
	private String intent_dt_taken;
	private String intent_notes;
	private String intent_photo_name;
	private String username;
	private String password;
	private SharedPreferences pref;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.observation);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.flora_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  What's Invasive");
	    
	    
		//Get all plant list
		//Open plant list db from static db
	    OneTimeDBHelper oneDBH = new OneTimeDBHelper(this);
		SQLiteDatabase db = oneDBH.getReadableDatabase(); 
		
		pref = getSharedPreferences("userinfo",0);
		username = pref.getString("Username","");
		password = pref.getString("Password","");

		//Rereive syncDB and add them to arUserPlatList arraylist
		arPlantList = new ArrayList<myItem>();
		Cursor cursor = db.rawQuery("select image_id, cname, sname, lat, lng, dt_taken, notes, photo_name, uploaded FROM onetimeob ORDER BY rowid DESC;",null);
		if(cursor.getCount() > 0) {
			while(cursor.moveToNext()){
				Integer image_id = cursor.getInt(0);
				String common_name = cursor.getString(1);
				String species_name = cursor.getString(2);
				Double latitude = cursor.getDouble(3);
				Double longitude = cursor.getDouble(4);
				String dt_taken = cursor.getString(5);
				String uploaded = cursor.getString(8);
				if(dt_taken == null) {
					dt_taken = "No Photo";
				}
				String notes = cursor.getString(6);
				if(notes == null) {
					notes = "No Notes";
				}
				String photo_name = cursor.getString(7);
				
				//Log.i("K", "current_image_id : " + image_id);
				
				myItem pi;
				pi = new myItem(image_id, common_name, species_name, latitude, longitude, dt_taken, notes, photo_name, uploaded);
				arPlantList.add(pi);
			}
			
			mylistapdater = new MyListAdapter(this, R.layout.myplantlist_item ,arPlantList);
			ListView MyList = getListView(); 
			MyList.setAdapter(mylistapdater);
			
			//Close DB and cursor
			cursor.close();
			db.close();
			oneDBH.close();
		}
		//if there's no data in the queue...
		else {
			TextView queueTxt = (TextView) findViewById(R.id.queue_empty);
			TextView queue2Txt = (TextView) findViewById(R.id.queue_empty2);
			queueTxt.setText("Queue is Empty.\n\n");
			queue2Txt.setText("Please go to the PBB website to check your data.");
			queueTxt.setVisibility(View.VISIBLE);
			queue2Txt.setVisibility(View.VISIBLE);
		}
		
	    // TODO Auto-generated method stub
	}
	
	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		intent_cname = arPlantList.get(position).CommonName;
		intent_sname = arPlantList.get(position).SpeciesName;
		intent_lat = arPlantList.get(position).Latitude;
		intent_lng = arPlantList.get(position).Longitude;
		intent_image_id = arPlantList.get(position).Picture;
		intent_dt_taken = arPlantList.get(position).dt_taken;
		intent_notes = arPlantList.get(position).notes;
		intent_photo_name = arPlantList.get(position).photo_name;

		
		new AlertDialog.Builder(Queue.this)
		.setTitle("Select Category")
		.setIcon(R.drawable.pbbicon2)
		.setItems(R.array.myphoto, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String[] category = getResources().getStringArray(R.array.myphoto);
				
				if(category[which].equals("See Detail")) {
					Intent intent = new Intent(Queue.this, MyPhotoDetail.class);
					intent.putExtra("cname", intent_cname);
					intent.putExtra("sname", intent_sname);
					intent.putExtra("lat", intent_lat);
					intent.putExtra("lng", intent_lng);
					intent.putExtra("image_id", intent_image_id);
					intent.putExtra("dt_taken", intent_dt_taken);
					intent.putExtra("notes", intent_notes);
					intent.putExtra("photo_name", intent_photo_name);
					//intent.putExtra("protocol_id", arPlantList.get(position).protocolID);
					startActivity(intent);
				}
				else {
					OneTimeDBHelper oneDBH = new OneTimeDBHelper(Queue.this);
					SQLiteDatabase db = oneDBH.getWritableDatabase();
					db.execSQL("DELETE FROM onetimeob WHERE dt_taken = '" + intent_dt_taken + "';");
					
					oneDBH.close();
					
					new File("/sdcard/pbudburst/tmp/" + intent_photo_name + ".jpg").delete();
					Log.i("K", "DELETE UPLOADED ITEM IN THE QUEUE");
					
					Intent intent = new Intent(Queue.this, Queue.class);
					finish();
					startActivity(intent);
					Toast.makeText(Queue.this, "Item Deleted", Toast.LENGTH_SHORT).show();
				}
			}
		})
		.setNegativeButton("Back", null)
		.show();
	}
	
	
	class myItem{	
		myItem(int aPicture, String aCommonName, String aSpeciesName, Double aLatitude, Double aLongitude, String aDt_taken, String aNotes, String aPhoto_name, String aUploaded){
			Picture = aPicture;
			CommonName = aCommonName;
			SpeciesName = aSpeciesName;
			Latitude = aLatitude;
			Longitude = aLongitude;
			dt_taken = aDt_taken;
			notes = aNotes;
			photo_name = aPhoto_name;
			uploaded = aUploaded;
		}
		
		int Picture;
		String CommonName;
		String SpeciesName;
		double Latitude;
		double Longitude;
		String dt_taken;
		String notes;
		String photo_name;
		String uploaded;
	}
	
	//Adapters:MyListAdapter and SeparatedAdapter
	class MyListAdapter extends BaseAdapter{
		Context maincon;
		LayoutInflater Inflater;
		ArrayList<myItem> arSrc;
		int layout;
		
		public MyListAdapter(Context context, int alayout, ArrayList<myItem> aarSrc){
			maincon = context;
			Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arSrc = aarSrc;
			layout = alayout;
		}
		
		public int getCount(){
			return arSrc.size();
		}
		
		public String getItem(int position){
			return arSrc.get(position).CommonName;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null)
				convertView = Inflater.inflate(layout, parent, false);
			
			ImageView image = (ImageView)convertView.findViewById(R.id.myimage);
			String getPhoto = arSrc.get(position).photo_name;
			//Log.i("K", "getPhoto : " + getPhoto);
			String imagePath = null;
			
			File file = new File(TEMP_PATH + getPhoto + ".jpg");
		    Bitmap bitmap = null;
		    Bitmap resized_bitmap = null;
		    
		    // set new width and height of the phone_image
		    int new_width = 110;
		    int new_height = 110;
		    
		    if(file.exists()) {
		    	imagePath = TEMP_PATH + getPhoto + ".jpg";
		    	bitmap = BitmapFactory.decodeFile(imagePath);
		    	
			   	int width = bitmap.getWidth();
			   	int height = bitmap.getHeight();
			   	
			   	float scale_width = ((float) new_width) / width;
			   	float scale_height = ((float) new_height) / height;
			   	Matrix matrix = new Matrix();
			   	matrix.postScale(scale_width, scale_height);
			   	resized_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
			   	
			   	image.setImageBitmap(resized_bitmap);
			   	image.setVisibility(View.VISIBLE);
		    }
		    else {
		    	image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/no_photo", null, null));
		    	image.setVisibility(View.VISIBLE);
		    	image.setEnabled(false);
		   	}
			
			TextView textname = (TextView)convertView.findViewById(R.id.commonname);
			textname.setText(arSrc.get(position).CommonName);
			
			TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
			textdesc.setText(arSrc.get(position).SpeciesName + " ");
			
			TextView dt_taken = (TextView)convertView.findViewById(R.id.dt_taken);
			dt_taken.setText(arSrc.get(position).dt_taken);
			
			
			String uploaded = arSrc.get(position).uploaded;
			Log.i("K", "UPLOADED TAG : " + uploaded);
			if(uploaded.equals("1")) {
				ImageView p_image = (ImageView)convertView.findViewById(R.id.pheno);
				p_image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/check_mark", null, null));
			}
			
			
			return convertView;
		}
	}
	/////////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0,1,0,"Upload").setIcon(android.R.drawable.ic_menu_upload);
			
		return true;
	}
	
	//Menu option selection handling
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case 1:
				// upload all data to the server
				// SELECT * from onetimeob;
				OneTimeDBHelper oneDBH = new OneTimeDBHelper(Queue.this);
				SQLiteDatabase db = oneDBH.getReadableDatabase();
				Cursor cursor;
				
				cursor = db.rawQuery("SELECT image_id, cname, sname, lat, lng, dt_taken, notes, photo_name, uploaded from onetimeob", null);
				String result = "";
				
				while(cursor.moveToNext()) {
					result += cursor.getInt(0) + "," + 
							  cursor.getString(1) + "," + 
							  cursor.getString(2) + "," + 
							  cursor.getDouble(3) + "," +
							  cursor.getDouble(4) + "," +
							  cursor.getString(5) + "," + 
							  cursor.getString(6) + "," +
							  cursor.getString(7) + "," + 
							  cursor.getString(8) + "\n"; 
				}
				oneDBH.close();
				
				// use AsyncTask
				new FileUploads().execute(result);
				
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
	
	
	// FileUploads using asyncTask
	class FileUploads extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(Queue.this, "Uploading...", "Please wait....", true);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {

			HttpClient httpClient = new DefaultHttpClient();
			String url = new String("http://cens.solidnetdns.com/~kshan/PBB/PBsite_CENS/phone/onetime_ob_service?username=" + username + "&password=" + password);
			HttpPost httpPost = new HttpPost(url);
			String []split_by_line = params[0].split("\n");
			
			OneTimeDBHelper oneDBH = new OneTimeDBHelper(Queue.this);
			
			for(int i = 0 ; i < split_by_line.length ; i++) {
				Log.i("K", "attempt to upload the file : #" + i);
				String []line_by_separator = split_by_line[i].split(",");
				
				// if uploaded attribute is equal to 1
				if(line_by_separator[8].equals("1"))
					continue;
			
				try {
					MultipartEntity entity = new MultipartEntity();
					entity.addPart("image_id", new StringBody(line_by_separator[0]));
					entity.addPart("cname", new StringBody(line_by_separator[1]));
					entity.addPart("sname", new StringBody(line_by_separator[2]));
					entity.addPart("latitude", new StringBody(line_by_separator[3]));
					entity.addPart("longitude", new StringBody(line_by_separator[4]));
					entity.addPart("dt_taken", new StringBody(line_by_separator[5]));
					entity.addPart("notes", new StringBody(line_by_separator[6]));
					entity.addPart("photo_id", new StringBody(line_by_separator[7]));
					
					if(!line_by_separator[7].equals("")) {
						File file = new File(TEMP_PATH + line_by_separator[7] + ".jpg");
						if(file.exists()) {
							entity.addPart("image", new FileBody(file));
						}
						else {
							Log.e("K", "There is no file in the SDcard.");
						}
					}
					
					httpPost.setEntity(entity);
					
					//Log.i("K", "HTTP POST : " + httpPost);
					
					HttpResponse response = httpClient.execute(httpPost);
					
					//Log.i("K", "Response from the server : " + response.toString());
					
					if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						
						BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); 
			        	String line = br.readLine();
						
						Log.i("K", "Result : " + line);
						
						if(line.equals("UPLOADED_OK")) {
							

							SQLiteDatabase db = oneDBH.getWritableDatabase();
							//db.execSQL("DELETE FROM onetimeob WHERE dt_taken = '" + line_by_separator[5] + "';");
							//Log.i("K", " : " + line_by_separator[7]);
							db.execSQL("UPDATE onetimeob SET uploaded = 1 WHERE dt_taken = '" + line_by_separator[5] + "' AND image_id = '" + line_by_separator[0] +"';");
							Log.i("K", "UPDATE THE UPLOADED FLAG IN THE QUEUE");
							//new File("/sdcard/pbudburst/tmp/" + line_by_separator[7] + ".jpg").delete();
							
							//Log.i("K", "DELETE UPLOADED ITEM IN THE QUEUE");
							db.close();
						}
						else {
							Log.e("K", "UPLOADED FAILED!!");
						}
					}

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			oneDBH.close();
			// TODO Auto-generated method stub
			return true;
		}
		
		protected void onProgressUpdate(Integer... progress) {
			
		}
		
		protected void onPostExecute(Boolean bool) {
			if(bool) {
				dialog.dismiss();
				Toast.makeText(Queue.this, "Upload Complete", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(Queue.this, Queue.class);
				finish();
				startActivity(intent);
			}
			
		}
		
		protected void onCancelled() {
			dialog.dismiss();
			Toast.makeText(Queue.this, "Uploading Stopped", Toast.LENGTH_SHORT).show();
		}
	}
}
