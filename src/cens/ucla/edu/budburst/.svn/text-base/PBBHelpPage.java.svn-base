package cens.ucla.edu.budburst;

import cens.ucla.edu.budburst.helper.HelperValues;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class PBBHelpPage extends Activity {

	private int previous_activity = 1;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Intent p_intent = getIntent();
		previous_activity = p_intent.getExtras().getInt("from");
		
		switch(previous_activity) {
		case HelperValues.FROM_ABOUT:
			setContentView(R.layout.help_about);
			break;
		case HelperValues.FROM_MAIN_PAGE:
			setContentView(R.layout.help_main);
			break;
		case HelperValues.FROM_PLANT_LIST:
			setContentView(R.layout.help_my_list);
			break;
		case HelperValues.FROM_ONE_TIME_MAIN:
			setContentView(R.layout.help_lists);
			break;
		}
	    // TODO Auto-generated method stub
	}

}
