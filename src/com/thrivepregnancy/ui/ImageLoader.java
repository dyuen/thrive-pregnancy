package com.thrivepregnancy.ui;

import java.io.File;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Methods for setting the content of an ImageView. The image is pre-scaled before
 * loading it. This prevents exceeding available heap size when trying to load large
 * photo images.
 */

public class ImageLoader {
	private ImageView m_photoView;
	private String m_photo;
	private Integer m_id;
	 
	public ImageLoader(String photo, ImageView photoView) {
		m_photoView = photoView;
		m_photo = photo;
		BitmapCache.InitBitmapCache();
	}
	
	public void loadBitmap(Integer id) {
		m_id = id;
		Bitmap bitmap = null;
		
		bitmap = BitmapCache.getBitmapFromMemCache(m_id);
	    
		if (bitmap != null) {
			Log.d("bitmap retrieved from cache: ", Integer.toString(m_id));
			m_photoView.setVisibility(View.VISIBLE);
	    	m_photoView.setImageBitmap(bitmap);
		} else {
			if (m_photo != null && !m_photo.equals("")) {
		           new ImageLoaderTask().execute(m_photo);
		    }
		}
	}
	
	private class ImageLoaderTask extends AsyncTask<String, String, Bitmap> {

		/**
		 * Scales image to fix out of memory crash
		 */
		private Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
	
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(path, options);
	
	        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	
	        // Decode bitmap with inSampleSize set
	        options.inJustDecodeBounds = false;
	        Bitmap bm = BitmapFactory.decodeFile(path, options);
	        Bitmap bitmap;
	        try {
		        Matrix m = new Matrix();
		        ExifInterface exif = new ExifInterface(path);
		        
		        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
		        
		        if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
		            m.postRotate(180);
		        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
		            m.postRotate(90); 
		        }
		        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
		            m.postRotate(270);     
		        } 
		        
		        Log.d("in orientation", "" + orientation);
		        bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),bm.getHeight(), m, true);
		        
	            return bitmap;
	        } catch (Exception e) {
	        	return null;
	        }
	    }
	
		private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	        final int height = options.outHeight;
	        final int width = options.outWidth;
	        int inSampleSize = 1;
	
	        if (height > reqHeight || width > reqWidth) {
	            if (width > height) {
	                inSampleSize = Math.round((float) height / (float) reqHeight);
	            } else {
	                inSampleSize = Math.round((float) width / (float) reqWidth);
	             }
	         }
	         return inSampleSize;
	      }
	
		@Override
		protected Bitmap doInBackground(String... param) {
	        try {
	        	File file = new File(param[0]);
	
				Bitmap bitmap = decodeSampledBitmapFromPath(file.getAbsolutePath(),200,200);
				
				BitmapCache.addBitmapToMemoryCache(m_id, bitmap);
				
	            return bitmap;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
		}
		
		protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
            	m_photoView.setVisibility(View.VISIBLE);
            	m_photoView.setImageBitmap(bitmap);
            } else {
                Log.e("ImageLoaderTask", "failed to load image");
            }
        }
	}
}
