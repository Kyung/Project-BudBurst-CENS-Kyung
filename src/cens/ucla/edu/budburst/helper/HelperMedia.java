package cens.ucla.edu.budburst.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cens.ucla.edu.budburst.onetime.OneTimeAddMyPlant;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.widget.Toast;

public class HelperMedia {
	
	protected static final int MAX_IMAGE_WIDTH = 1280;
	protected static final int MAX_IMAGE_HEIGHT = 960;
	private Bitmap scaledImage;
	
	public Bitmap ShowPhotoTaken(String path) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			
			int factor =  Math.max(options.outWidth / MAX_IMAGE_WIDTH, options.outHeight / MAX_IMAGE_HEIGHT);
			
			options.inSampleSize = msb32(factor);
			options.inPurgeable = true;
			options.inInputShareable = true;
			options.inJustDecodeBounds = false;
			
			Bitmap image = BitmapFactory.decodeFile(path, options);
			
			
			if(image.getWidth() > MAX_IMAGE_WIDTH || image.getHeight() > MAX_IMAGE_HEIGHT) {
				float scale = Math.min(MAX_IMAGE_WIDTH / (float)image.getWidth(), 
						MAX_IMAGE_HEIGHT / (float)image.getHeight());
				
				int newWidth = (int)((image.getWidth() * scale) + 0.5f);
				int newHeight = (int)((image.getHeight() * scale) + 0.5f);
				
				scaledImage = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
				image.recycle();	
			}
			else {
				scaledImage = image;
			}
			
			OutputStream os = new FileOutputStream(path);
			scaledImage.compress(CompressFormat.JPEG, 80, os);
			os.close();
			
			int width = scaledImage.getWidth();
		   	int height = scaledImage.getHeight();
		   	
		    Bitmap resized_bitmap = null;
		    // set new width and height of the phone_image
		    int new_width = 100;
		    int new_height = 100;
		   	
		   	float scale_width = ((float) new_width) / width;
		   	float scale_height = ((float) new_height) / height;
		   	Matrix matrix = new Matrix();
		   	matrix.postScale(scale_width, scale_height);
		   	resized_bitmap = Bitmap.createBitmap(scaledImage, 0, 0, width, height, matrix, true);
			
		   	return resized_bitmap;
			
		}
		catch(FileNotFoundException e){
			
		}
		catch(IOException e) {
			
		}
		catch(OutOfMemoryError e){
			
		}
		return null;
	}
	/* 
	public Bitmap ShowPhotoTaken(String imagePath) {
		
		Log.i("K", "IMAGE PATH : " + imagePath);
		
		// we can put the option for the bitmap
		BitmapFactory.Options options = new BitmapFactory.Options();
		File file = new File(imagePath);
		
		Log.i("K", "FILE LENGTH : " + file.length());

		// change the sampleSize to 4 (which will be resulted in 1/4 of original size)
		if (file.length() > 1000000)
			options.inSampleSize = 4;
		else if (file.length() > 500000)
			options.inSampleSize = 2;
		else
			options.inSampleSize = 1;
		
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
	}*/
	
    private int msb32(int x)
    {
        x |= (x >> 1);
        x |= (x >> 2);
        x |= (x >> 4);
        x |= (x >> 8);
        x |= (x >> 16);
        return (x & ~(x >> 1));
    }
	
    /*
     * Crop the image 
     *  - set the destination length
     *  - if h > w, start from 0
     *  - if h < w, start from the half point of x
     */
    
    public Bitmap resizeImage(Bitmap photo, int length) {
    
    	int sourceWidth = photo.getWidth();
    	int sourceHeight = photo.getHeight();
    	int sourceX = 0;
    	int sourceY = 0;
    	int destLength = length;
    	
    	if(sourceWidth < sourceHeight) {
    		sourceY = 0;
    	}
    	else if(sourceWidth > sourceHeight) {
    		sourceX = (int)((sourceWidth - sourceHeight) / 2);
    	}
    	else {
    		
    	}
    	
    	Bitmap resizedImage = Bitmap.createBitmap(photo, sourceX, sourceY, destLength, destLength);
    	
    	return resizedImage;
    }
}
