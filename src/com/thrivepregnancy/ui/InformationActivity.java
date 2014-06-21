package com.thrivepregnancy.ui;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class InformationActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    // Get the message from the intent
	    Bundle parentBundle = getIntent().getExtras();
	    
	    String htmlFile = parentBundle.getString("htmlfile");
	    String url = "file:///android_asset/" + htmlFile;
	    
	    WebView myWebView = new WebView(this);
	    setContentView(myWebView);

	    myWebView.loadUrl(url);
	}
}
