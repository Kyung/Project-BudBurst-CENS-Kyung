package cens.ucla.edu.budburst.helper;

import java.io.File;
import java.io.FileOutputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class Media {
	
	public Bitmap ShowPhotoTaken(String imagePath) {
		
		Log.i("K", "IMAGE PATH : " + imagePath);
		
		// we can put the option for the bitmap
		BitmapFactory.Options options = new BitmapFactory.Options();
		File file = new File(imagePath);
		
		Log.i("K", "FILE LENGTH : " + file.length());

		// change the sampleSize to 4 (which will be resulted in 1/4 of original size)
		if (file.length() > 1000000)
			options.inSampleSize = 8;
		else if (file.length() > 500000)
			options.inSampleSize = 4;
		else
			options.inSampleSize = 2;
		
		// use tempstorage
		options.inTempStorage = new byte[16*1024];
		
		// put image Path and the options
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
		
		try{
			FileOutputStream out = new FileOutputStream(imagePath);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
		}catch(Exception e){
			Log.e("K", e.toString());
		}
		
		int width = bitmap.getWidth();
	   	int height = bitmap.getHeight();
	   	
	    Bitmap resized_bitmap = null;
	    // set new width and height of the phone_image
	    int new_width = 100;
	    int new_height = 100;
	   	
	   	float scale_width = ((float) new_width) / width;
	   	float scale_height = ((float) new_height) / height;
	   	Matrix matrix = new Matrix();
	   	matrix.postScale(scale_width, scale_height);
	   	resized_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		
	   	return resized_bitmap;
	}
}
