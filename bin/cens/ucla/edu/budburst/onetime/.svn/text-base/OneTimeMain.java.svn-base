package cens.ucla.edu.budburst.onetime;

import java.util.ArrayList;

import cens.ucla.edu.budburst.PlantList;
import cens.ucla.edu.budburst.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OneTimeMain extends Activity {

	ArrayList<Button> buttonBar = new ArrayList<Button>();
	private Button observationBtn;
	private Button recommendationBtn;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.onetimemain);
	    // TODO Auto-generated method stub
	}
	
	public void onResume() {
		super.onResume();
		
		//My plant button
		Button buttonMyplant = (Button)findViewById(R.id.myplant);
		buttonMyplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(OneTimeMain.this, PlantList.class);
				startActivity(intent);
			}
		});
		
		//Shared plant button
		Button buttonSharedplant = (Button)findViewById(R.id.sharedplant);
		buttonSharedplant.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
			}
		});
		
		buttonSharedplant.setSelected(true);
		
		
		buttonBar.add((Button) this.findViewById(R.id.observation_btn));
		buttonBar.get(0).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(OneTimeMain.this)
					.setTitle("Select Category")
					.setIcon(R.drawable.pbbicon2)
					.setItems(R.array.category, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String[] category = getResources().getStringArray(R.array.category);
							
							Intent intent = new Intent(OneTimeMain.this, Observation.class);
							intent.putExtra("SelectedList", category[which]);
							startActivity(intent);
						}
					})
					.setNegativeButton("Back", null)
					.show();
			}		
		});
		
		
		buttonBar.add((Button) this.findViewById(R.id.recommend_btn));
		buttonBar.get(1).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(OneTimeMain.this)
				.setTitle("Select Category")
				.setIcon(R.drawable.pbbicon2)
				.setItems(R.array.recommend, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] category = getResources().getStringArray(R.array.recommend);
						
						Intent intent = new Intent(OneTimeMain.this, Recommendation.class);
						intent.putExtra("SelectedList", category);
						startActivity(intent);
					}
				})
				.setNegativeButton("Back", null)
				.show();
	
			}		
		});
	}
}
