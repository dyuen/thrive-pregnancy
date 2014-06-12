package com.past5.thrivepregnancy;

import com.past5.thrivepregnancy.TabListener;
import com.thrivepregnancy.R;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class BaseActivity extends Activity {
		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// setup action bar for tabs
	    ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);

	    Tab tab = actionBar.newTab()
	                       .setText(R.string.menu_timeline)
	                       .setTabListener(new TabListener<TimelineFragment>(
	                               this, "timeline", TimelineFragment.class));
	    
	    actionBar.addTab(tab);
	    
	    tab = actionBar.newTab()
                .setText(R.string.menu_care)
                .setTabListener(new TabListener<TimelineFragment>(
                        this, "care", TimelineFragment.class));
	    
	    actionBar.addTab(tab);

		tab = actionBar.newTab()
		.setText(R.string.menu_need)
		.setTabListener(new TabListener<NeedFragment>(
		        this, "need", NeedFragment.class));
		
		actionBar.addTab(tab);
		
		this.setTitle(R.string.app_name);
		this.setTitleColor(getResources().getColor(R.color.actionTextColor));
		
		Toast.makeText(this, this.getTitle(), Toast.LENGTH_SHORT).show(); 
	}
}

