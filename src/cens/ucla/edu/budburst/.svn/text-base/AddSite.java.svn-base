package cens.ucla.edu.budburst;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import cens.ucla.edu.budburst.helper.SyncDBHelper;

public class AddSite extends Activity{
	
	final String TAG = "AddSite.class"; 
	
	EditText latitude;
	EditText longitude;
	EditText sitename;
	EditText comment;

	ArrayAdapter<CharSequence> adspin;
	String selectedState;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addsite);

	}
	
	public void onResume(){
		super.onResume();
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		LocationManager lmanager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		String bestprovider = lmanager.getBestProvider(criteria, true);
		
		Location cur_location = lmanager.getLastKnownLocation(bestprovider);
		
		latitude = (EditText)this.findViewById(R.id.latitude);
		latitude.setText(String.valueOf(cur_location.getLatitude()));

		longitude = (EditText)this.findViewById(R.id.longitude);
		longitude.setText(String.valueOf(cur_location.getLongitude()));
		
		sitename = (EditText)this.findViewById(R.id.sitename);
		comment = (EditText)this.findViewById(R.id.comment);
		
		Spinner spinner = (Spinner)findViewById(R.id.state);
		spinner.setPrompt("Choose your state.");
		
		adspin = ArrayAdapter.createFromResource(this, R.array.state, 
				android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adspin);
		spinner.setSelection(4);
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
				selectedState = adspin.getItem(position).toString();
				//Toast.makeText(AddSite.this, adspin.getItem(position), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//Cancel button handler
		Button cancelButton = (Button)this.findViewById(R.id.cancel);	
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AddSite.this, Sync.class);
				startActivity(intent);
				finish();
			}
		});
		
		
		//Save button handler
		Button saveButton = (Button)this.findViewById(R.id.save);	
		saveButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				SyncDBHelper syncDBHelper = new SyncDBHelper(AddSite.this);
				SQLiteDatabase syncWDB = syncDBHelper.getWritableDatabase();
								
				try{
					String usertype_stname = sitename.getText().toString();
					
					//Check site name is empty
					if(usertype_stname.equals("")){
						Toast.makeText(AddSite.this,"Please check your site name", Toast.LENGTH_SHORT).show();
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(sitename.getWindowToken(), 0);
						return;
					}
	
					//Check if site name is duplicated
					String query = "SELECT site_id FROM my_sites WHERE site_name='" 
						+ usertype_stname + "';";
					Cursor cursor = syncWDB.rawQuery(query, null);
					if(cursor.getCount() != 0){
						Toast.makeText(AddSite.this,"The site name is already in your site list. Please " +
								"type another name.", Toast.LENGTH_SHORT).show();
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(sitename.getWindowToken(), 0);
						cursor.close();
						return;
					}
					
					//Insert user typed site name into database
					//Calendar now = Calendar.getInstance();
					long epoch = System.currentTimeMillis()/1000;
					query = "INSERT INTO my_sites VALUES(" +
					"null, " + 
					"'" + epoch + "'," + 
					"'" + usertype_stname + "'," +
					"'" + latitude.getText().toString() + "'," + 
					"'" + longitude.getText().toString() + "'," +
					"''," +
					"'" + selectedState + "'," +
					"''," +
					"''," +
					"'" + comment.getText().toString() + "'," +
					SyncDBHelper.SYNCED_NO + ");";
					syncWDB.execSQL(query);
					cursor.close();
					
					Intent intent = new Intent(AddSite.this, PlantList.class);
					startActivity(intent);
					finish();
				}catch(Exception e){
					Log.e(TAG, e.toString());
				}finally{
					syncDBHelper.close();					
				}
			}
		});
	}
}