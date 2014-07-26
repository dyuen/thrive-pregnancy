package com.thrivepregnancy.ui;

import java.io.File;
import java.io.InputStream;

import com.thrivepregnancy.data.Event;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Methods for setting the content of an ImageView. The image is pre-scaled before
 * loading it. This prevents exceeding available heap size when trying to load large
 * photo images.
 */

public class ImageLoader {
	private ImageView m_photoView;
	private String m_photo;
	private Integer m_id;
	private Boolean m_quality = false;
	private Event.Type m_type;
	private Context m_context;
	private TextView m_divider = null;
	
	public ImageLoader(String photo, ImageView photoView, Context context, Event.Type type) {
		m_photoView = photoView;
		m_photo = photo;
		m_type = type;
		m_context = context;
		
		BitmapCache.InitBitmapCache();
	}
	
	public ImageLoader(Event[] events) {
		BitmapCache.InitBitmapCache();
	}
	
	public void setDivider(TextView divider) {
		m_divider = divider;
	}
	
	public void setQuality(Boolean quality) {
		m_quality = quality;
	}
	
	public void loadBitmap(Integer id, Integer position) {
		m_id = id;
		
		Bitmap bitmap = null;
		
		if (m_id != null) bitmap = BitmapCache.getBitmapFromMemCache(m_id);
	    
		if (bitmap != null) {
			//Log.d("bitmap retrieved from cache: ", Integer.toString(m_id));
			m_photoView.setVisibility(View.VISIBLE);
	    	m_photoView.setImageBitmap(bitmap);
	    	if (m_divider!=null) m_divider.setVisibility(View.VISIBLE);
		} else {
			if (m_photo != null && !m_photo.equals("")) {
				ViewHolder.position = position;
				ViewHolder.picture = m_photoView;
				if (m_divider!=null) ViewHolder.text = m_divider;
		        new ImageLoaderTask().execute(position);
		    }
		}
	}
	
	private static class ViewHolder {
	    public static ImageView picture;
	    public static TextView text;
	    public static int position;
	}
	
	private class ImageLoaderTask extends AsyncTask<Integer, String, Bitmap> {
		Context context;
		String photo;
		Event.Type type;
		Integer id , position;
		TextView divider;
		ImageView photoView;
		Boolean quality;
		
		private Bitmap decodeSampledBitmapFromAssets(int reqWidth, int reqHeight) {
			Bitmap bitmap;
			AssetManager assetManager = context.getAssets();
    		InputStream in;

			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				
				in = assetManager.open(photo);
				
				BitmapFactory.decodeStream(in, null, options);
				
				in.close();
				
				BitmapFactory.Options options1 = new BitmapFactory.Options();
				options1.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
				
				in = assetManager.open(photo);
				
				bitmap = BitmapFactory.decodeStream(in, null, options1);
				
				in.close();
				
				return bitmap;
			} catch (Exception e) {
	        	return null;
	        }
		}
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
		protected void onPreExecute()
		{
			context = m_context;
			photo = m_photo;
			type = m_type;
			id = m_id;
			divider = m_divider;
			photoView = m_photoView;
			quality = m_quality;
		}
		
		@Override
		protected Bitmap doInBackground(Integer... param) {
			Bitmap bitmap = null;
			position = param[0];
			Integer a,b;
			
			if (quality) {
				a = 2;
				b = 2;
			} else {
				a = 200;
				b = 200;
			}
			
	        try {
	        	if (type.equals(Event.Type.TIP)){
	 				bitmap = decodeSampledBitmapFromAssets(a,b);	
				} else {
		        	File file = new File(photo);
		
					bitmap = decodeSampledBitmapFromPath(file.getAbsolutePath(),a,b);
				}
	        	
	        	if (id != null) BitmapCache.addBitmapToMemoryCache(id, bitmap);
	        	
	            return bitmap;
	        } catch (Exception e) {
	            Log.e(MainActivity.DEBUG_TAG,e.toString());
	            return null;
	        }
		}
		
		protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
            	/*
            	if (type.equals(Event.Type.TIP)){
            		if (ViewHolder.position==position) {
            			ViewHolder.picture.setVisibility(View.VISIBLE);
            			ViewHolder.picture.setImageBitmap(bitmap);
            			if (ViewHolder.text!=null) ViewHolder.text.setVisibility(View.VISIBLE);
            		} else {
            			ViewHolder.picture.setVisibility(View.GONE);
            			if (ViewHolder.text!=null) ViewHolder.text.setVisibility(View.GONE);
            		}
            	} else {
            		photoView.setVisibility(View.VISIBLE);
            		photoView.setImageBitmap(bitmap);
            	}
            	*/
            	photoView.setVisibility(View.VISIBLE);
        		photoView.setImageBitmap(bitmap);
        		if (divider!=null) divider.setVisibility(View.VISIBLE);
            } else {
                Log.e("ImageLoaderTask", "failed to load image");
            }
        }
	}
}
