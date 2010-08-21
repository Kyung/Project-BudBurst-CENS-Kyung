package cens.ucla.edu.budburst.onetime;

import cens.ucla.edu.budburst.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MyPhotoDetail extends Activity {

	public final String TEMP_PATH = "/sdcard/pbudburst/tmp/";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.myphotodetail);
	    
	    Intent intent = getIntent();
	    String cname = intent.getExtras().getString("cname");
	    String sname = intent.getExtras().getString("sname");
	    int image_id = intent.getExtras().getInt("image_id");
	    String dt_taken = intent.getExtras().getString("dt_taken");
	    String notes = intent.getExtras().getString("notes");
	    String photo_name = intent.getExtras().getString("photo_name");
	    
	    Log.i("K", "" + image_id);
	    
	    ImageView pheno_image = (ImageView) findViewById(R.id.phone_image);
	    ImageView image = (ImageView) findViewById(R.id.pheno_image);
	    TextView cnameTxt = (TextView) findViewById(R.id.common_name);
	    TextView snameTxt = (TextView) findViewById(R.id.science_name);
	    TextView dt_takenTxt = (TextView) findViewById(R.id.timestamp_text);
	    TextView notesTxt = (TextView) findViewById(R.id.mynotes);
	    
		String imagePath = TEMP_PATH + photo_name + ".jpg";
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
		pheno_image.setBackgroundResource(R.drawable.shapedrawable);
		pheno_image.setImageBitmap(bitmap);
		
		image.setImageResource(getResources().getIdentifier("cens.ucla.edu.budburst:drawable/p" + image_id, null, null));
		image.setBackgroundResource(R.drawable.shapedrawable);
		image.setVisibility(View.VISIBLE);
		
		cnameTxt.setText(" " + cname + " ");
		snameTxt.setText(" " + sname + " ");
		dt_takenTxt.setText(" " + dt_taken + " ");
		notesTxt.setText(notes);   
	    // TODO Auto-generated method stub
	}

}
