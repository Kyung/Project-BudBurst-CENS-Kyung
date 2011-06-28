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
	
    private int msb32(int x) {
        x |= (x >> 1);
        x |= (x >> 2);
        x |= (x >> 4);
        x |= (x >> 8);
        x |= (x >> 16);
        return (x & ~(x >> 1));
    }
	
    /**
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
