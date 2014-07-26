package com.thrivepregnancy.ui;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.imagezoom.ImageAttacher;
import com.imagezoom.ImageAttacher.OnMatrixChangedListener;
import com.imagezoom.ImageAttacher.OnPhotoTapListener;
import com.thrivepregnancy.R;

public class PictureActivity extends Activity {
    ImageView m_imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        
        Bundle parentBundle = getIntent().getExtras();
	    
	    String imagePath = parentBundle.getString("imagepath");
	    String activityTitle = parentBundle.getString("imagetitle");
	    Integer eventId = parentBundle.getInt("eventid");
	    
	    m_imageView = (ImageView) findViewById(R.id.image_view);
	    	
	    	ImageLoader imageloader = new ImageLoader(imagePath,m_imageView,this, null);
	    	
	    	imageloader.setQuality(true);
	    	
			imageloader.loadBitmap(eventId,0);
			
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
