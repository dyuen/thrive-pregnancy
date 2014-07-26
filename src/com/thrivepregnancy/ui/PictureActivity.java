package com.thrivepregnancy.ui;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.imagezoom.ImageAttacher;
import com.imagezoom.ImageAttacher.OnMatrixChangedListener;
import com.imagezoom.ImageAttacher.OnPhotoTapListener;
import com.thrivepregnancy.R;
import com.thrivepregnancy.data.Event;

public class PictureActivity extends Activity {
    ImageView m_imageView;
    
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        
        Bundle parentBundle = getIntent().getExtras();
	    
	    String imagePath = parentBundle.getString("imagepath");
	    String activityTitle = parentBundle.getString("imagetitle");
	    Integer eventId = parentBundle.getInt("eventid");
	    
	    m_imageView = (ImageView) findViewById(R.id.image_view);
	    
	    File file = new File(imagePath);
	    Bitmap bitmap = decodeSampledBitmapFromPath(file.getAbsolutePath(),1000,1000);
		
	    m_imageView.setImageBitmap(bitmap);
	    
    		ActionBar actionBar = getActionBar();
    	    actionBar.setLogo(R.drawable.ic_logo_arrow);
    	    actionBar.setTitle(activityTitle);
    	    actionBar.setDisplayUseLogoEnabled(true);
    	    actionBar.setHomeButtonEnabled(true);
    	    
            /**
             * Use Simple ImageView
             */
            usingSimpleImage(m_imageView);
           
    }

    public void usingSimpleImage(ImageView imageView) {
        ImageAttacher mAttacher = new ImageAttacher(imageView);
        ImageAttacher.MAX_ZOOM = 2.0f; // Double the current Size
        ImageAttacher.MIN_ZOOM = 0.5f; // Half the current Size
        MatrixChangeListener mMaListener = new MatrixChangeListener();
        mAttacher.setOnMatrixChangeListener(mMaListener);
        PhotoTapListener mPhotoTap = new PhotoTapListener();
        mAttacher.setOnPhotoTapListener(mPhotoTap);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case android.R.id.home:
	            // app icon in action bar clicked; goto parent activity.
	            this.finish();
            return true;
	    }
	    
	    return true;
	}
    
    private class PhotoTapListener implements OnPhotoTapListener {
		@Override
		public void onPhotoTap(View arg0, float arg1, float arg2) {
			// TODO Auto-generated method stub
			
		}
    }

    private class MatrixChangeListener implements OnMatrixChangedListener {

		@Override
		public void onMatrixChanged(RectF arg0) {
			// TODO Auto-generated method stub
			
		}
    }
}
