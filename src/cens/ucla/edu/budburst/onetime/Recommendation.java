package cens.ucla.edu.budburst.onetime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Recommendation extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Intent intent = getIntent();
	    String selectedItem = intent.getExtras().getString("SelectedList");
	    
	    if(selectedItem.equals("What's Blooming")) {
	    	finish();
	    	Toast.makeText(Recommendation.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
	    	//Intent ints = new Intent(Recommendation.this, )
	    }
	    else if(selectedItem.equals("What's Invasive")) {
	    	//Intent ints = new Intent(Recommendation.this, Whatsinvasive.class);
	    	finish();
	    	Toast.makeText(Recommendation.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
	    	//startActivity(ints);
	    }
	    else if(selectedItem.equals("What's Native")) {
	    	finish();
	    	Toast.makeText(Recommendation.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
	    }
	    // What's Popular
	    else {
	    	Intent ints = new Intent(Recommendation.this, Whatspopulars.class);
	    	finish();
	    	startActivity(ints);
	    }
	    // TODO Auto-generated method stub
	}
}
