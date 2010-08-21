package cens.ucla.edu.budburst.onetime;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
	private String intent_dt_taken;
	private String intent_notes;
	private String intent_photo_name;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.observation);
	
		//Get all plant list
		//Open plant list db from static db
	    OneTimeDBHelper oneDBH = new OneTimeDBHelper(this);
		SQLiteDatabase db = oneDBH.getReadableDatabase(); 
		
		
		//Rereive syncDB and add them to arUserPlatList arraylist
		arPlantList = new ArrayList<myItem>();
		Cursor cursor = db.rawQuery("select image_id, cname, sname, dt_taken, notes, photo_name FROM onetimeob;",null);
		while(cursor.moveToNext()){
			Integer image_id = cursor.getInt(0);
			String common_name = cursor.getString(1);
			String species_name = cursor.getString(2);
			String dt_taken = cursor.getString(3);
			if(dt_taken == null) {
				dt_taken = "No Photo";
			}
			String notes = cursor.getString(4);
			if(notes == null) {
				notes = "No Notes";
			}
			String photo_name = cursor.getString(5);
			
			Log.i("K", "current_image_id : " + image_id);
			
			myItem pi;
			pi = new myItem(image_id, common_name, species_name, dt_taken, notes, photo_name);
			arPlantList.add(pi);
		}
		
		mylistapdater = new MyListAdapter(this, R.layout.myplantlist_item ,arPlantList);
		ListView MyList = getListView(); 
		MyList.setAdapter(mylistapdater);
		
		//Close DB and cursor
		cursor.close();
		oneDBH.close();
	    // TODO Auto-generated method stub
	}
	
	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		intent_cname = arPlantList.get(position).CommonName;
		intent_sname = arPlantList.get(position).SpeciesName;
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
					
					//mylistapdater.notifyDataSetChanged();
					Intent intent = new Intent(Queue.this, Queue.class);
					finish();
					startActivity(intent);
					Toast.makeText(Queue.this, "Successfully Deleted", Toast.LENGTH_SHORT).show();
				}
			}
		})
		.setNegativeButton("Back", null)
		.show();
	}
	
	
	class myItem{	
		myItem(int aPicture, String aCommonName, String aSpeciesName, String aDt_taken, String aNotes, String aPhoto_name){
			Picture = aPicture;
			CommonName = aCommonName;
			SpeciesName = aSpeciesName;
			dt_taken = aDt_taken;
			notes = aNotes;
			photo_name = aPhoto_name;
		}
		
		int Picture;
		String CommonName;
		String SpeciesName;
		String dt_taken;
		String notes;
		String photo_name;
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
			String imagePath = TEMP_PATH + arSrc.get(position).photo_name + ".jpg";
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			image.setBackgroundResource(R.drawable.shapedrawable);
			image.setImageBitmap(bitmap);
			
			TextView textname = (TextView)convertView.findViewById(R.id.commonname);
			textname.setText(arSrc.get(position).CommonName);
			
			TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
			textdesc.setText(" " + arSrc.get(position).SpeciesName + " ");
			
			TextView dt_taken = (TextView)convertView.findViewById(R.id.dt_taken);
			dt_taken.setText(arSrc.get(position).dt_taken);
			
			ImageView pheno = (ImageView)convertView.findViewById(R.id.pheno);
			pheno.setImageResource(arSrc.get(position).Picture);
			
			return convertView;
		}
	}
	/////////////////////////////////////////////////////////////
	//Menu option
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0,1,0,"Upload").setIcon(android.R.drawable.ic_menu_rotate);
			
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
				cursor = db.rawQuery("SELECT image_id, cname, sname, dt_taken, notes, photo_name from onetimeob", null);
				
				String result = "";
				
				while(cursor.moveToNext()) {
					result += cursor.getInt(0) + "|" + 
							  cursor.getString(1) + "|" + 
							  cursor.getString(2) + "|" + 
							  cursor.getString(3) + "|" + 
							  cursor.getString(4) + "|" + 
							  cursor.getString(5) + "\n"; 
				}
				oneDBH.close();
				
				new FileUploads().execute(result);
				
				//Intent intent = new Intent(Queue.this, Queue.class);
				//finish();
				//startActivity(intent);
				
				return true;
		}
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////////
	
	
	// FileUploads using asyncTask
	class FileUploads extends AsyncTask<String, Integer, Void> {
		ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(Queue.this, "Loading", "Please wait", true);
		}
		
		@Override
		protected Void doInBackground(String... params) {

			HttpClient httpClient = new DefaultHttpClient();
			String url = new String("" + "http://sm.whatsinvasive.com/phone");
			HttpPost httpPost = new HttpPost(url);
			
			String []split_by_line = params[0].split("\n");
			for(int i = 0 ; i < split_by_line.length ; i++) {
				String []line_by_separator = split_by_line[i].split("|");
				
				try {
					MultipartEntity entity = new MultipartEntity();
					entity.addPart("image_id", new StringBody(line_by_separator[0]));
					entity.addPart("cname", new StringBody(line_by_separator[1]));
					entity.addPart("sname", new StringBody(line_by_separator[2]));
					entity.addPart("dt_taken", new StringBody(line_by_separator[3]));
					entity.addPart("notes", new StringBody(line_by_separator[4]));
					
					
					if(!line_by_separator[5].equals("")) {
						File file = new File(TEMP_PATH + line_by_separator[5] + ".jpg");
						entity.addPart("image", new FileBody(file));
					}
					
					httpPost.setEntity(entity);
					
					HttpResponse response = httpClient.execute(httpPost);
					
					Log.i("K", response.toString());
					
					if(response.getStatusLine().equals("200_OK")) {
						if(response.getEntity().getContent().equals("UPLOAD_OK")) {
							Log.i("K", "UPLOADED SUCCESSFULLY");
							
							OneTimeDBHelper oneDBH = new OneTimeDBHelper(Queue.this);
							SQLiteDatabase db = oneDBH.getWritableDatabase();
							db.execSQL("DELETE FROM onetimeob WHERE dt_taken = '" + line_by_separator[3] + "';");
							
							oneDBH.close();
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

			// TODO Auto-generated method stub
			return null;
		}
		
		protected void onProgressUpdate(Integer... progress) {
			
		}
		
		protected void onPostExecute(Void unused) {
			dialog.dismiss();
		}
	}
}
