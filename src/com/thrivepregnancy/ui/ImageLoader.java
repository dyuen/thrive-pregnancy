package com.thrivepregnancy.ui;

import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Methods for setting the content of an ImageView. The image is pre-scaled before
 * loading it. This prevents exceeding available heap size when trying to load large
 * photo images.
 */
public class ImageLoader {

	/**
	 * Load an ImageView with an image defined in a resource 
	 * @param resourceId resource Id
	 * @param resources the resources
	 * @param view view in which the image will be rendered
	 * @param screenWidth width of the screen, used to set upper limit on required width or height, depending on orientation
	 * @param orientation the current screen orientation
	 */
	public static Bitmap compressPicture(int resourceId, Resources resources, int screenWidth){
		// Determine the image dimensions
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, resourceId, options);
		
		options.inSampleSize = calculateInSampleSize(options, screenWidth);
	    
		// Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId, options);
	    return bitmap;
	}
	/*
	private static void saveToFile(){
		FileOutputStream fos;
		String strFileContents = "Some text to write to the file.";
		fos = openFileOutput("Filename.txt", Context.MODE_PRIVATE);
		fos.write(strFileContents.getBytes());
		fos.close();		
	}
	*/
	
	public static void load(InputStream streamIn, Resources resources){
		// Read the stream only to the point of obtaining the image dimensions
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(streamIn, null, options);
		int imageHeight = options.outHeight;
		int imageWidth = options.outWidth;
		String imageType = options.outMimeType;
	}
	
	private static int calculateInSampleSize(BitmapFactory.Options options, int screenWidth) {
		int requiredWidth;
		int requiredHeight;
		
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;

		requiredWidth = screenWidth;
		float ratio = ((float)height) / width;
		requiredHeight = (int)(ratio * requiredWidth);
		int inSampleSize = 1;

		if (height > requiredHeight || width > requiredWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the view height and width.
			while ((halfHeight / inSampleSize) > requiredHeight
			&& (halfWidth / inSampleSize) > requiredWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}	
}
