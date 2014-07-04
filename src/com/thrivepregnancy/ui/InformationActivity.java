package com.thrivepregnancy.ui;

import com.thrivepregnancy.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class InformationActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    // Get the message from the intent
	    Bundle parentBundle = getIntent().getExtras();
	    
	    String htmlFile = parentBundle.getString("htmlfile");
	    String activityTitle = parentBundle.getString("needtitle");
	    
	    String url = "file:///android_asset/" + htmlFile;
	    
	    WebView myWebView = new WebView(this);
	    setContentView(myWebView);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setLogo(R.drawable.ic_logo_arrow);
	    actionBar.setTitle(activityTitle);
	    actionBar.setDisplayUseLogoEnabled(true);
	    actionBar.setHomeButtonEnabled(true);
        
	    myWebView.loadUrl(url);
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
}
