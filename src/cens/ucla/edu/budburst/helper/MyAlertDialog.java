package cens.ucla.edu.budburst.helper;

import cens.ucla.edu.budburst.MainPage;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class MyAlertDialog extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    
	    new AlertDialog.Builder(MyAlertDialog.this)
	    .setTitle("Message")
	    .setMessage("Download Complete - Tree Lists")
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				finish();
			}
		})
		.show();
	    
	    // TODO Auto-generated method stub
	}

}
