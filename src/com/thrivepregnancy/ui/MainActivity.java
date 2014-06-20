package com.thrivepregnancy.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.thrivepregnancy.R;
import com.thrivepregnancy.data.Event;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Contains the My Timeline, My Care and I Need screens ("pages")
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	static final String DEBUG_TAG = "THRIVE";
	
    private MainPagerAdapter 	mAppSectionsPagerAdapter;
    /**
     * The {@link ViewPager} implements the page swipe animation
     */
    private ViewPager 			mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d(DEBUG_TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the pages
        FragmentManager fragmentManager = getSupportFragmentManager();
        mAppSectionsPagerAdapter = new MainPagerAdapter(fragmentManager);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar if space allows (e.g. in landscape mode).
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between pages.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different pages, select the corresponding tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the page fragments, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
    
    public List<Event> getTimelineList(){
    	List<Event> events = new ArrayList<Event>();
     	
    	Event e = new Event();
    	e.setType(Event.Type.DIARY_ENTRY);
    	e.setDate(new Date());
    	e.setPhotoFile("rosy.jpg");
    	e.setText("My first entry!");
    	events.add(e);
     	
    	e = new Event();
    	e.setType(Event.Type.APPOINTMENT);
    	e.setDate(new Date());
    	e.setPurpose("General Checkup");
    	e.setText("...appointment notes");
    	e.setAddress("My doctor's address");
    	events.add(e);
    	
    	return events;    	
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to the My Timeline,
     * My Care or I Need page.
     */
    public static class MainPagerAdapter extends FragmentPagerAdapter {
    	
    	public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * @return the Fragment belonging to the tab
         */
        @Override
        public Fragment getItem(int i) {
        	// Determine screen orientation
        	switch (i) {
        	case 0:
        		return new TimelineFragment();
        	case 1:
        		return new CareFragment();
        	default:
        		return new NeedFragment();
            }
        }

        /**
         * @return the number of tabs
         */
        @Override
        public int getCount() {
            return 3;
        }

        /**
         * @return the label for the tab corresponding to the page
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0:
                return "My Timeline";
            case 1:
                return "My Care";
            default:
                return "I Need";
            }
        }
    }
}
