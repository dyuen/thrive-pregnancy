package com.thrivepregnancy.ui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.thrivepregnancy.R;
import com.thrivepregnancy.data.DatabaseHelper;
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
	private DatabaseHelper 	databaseHelper = null;
	
	public static final String DEBUG_TAG = "THRIVE";
	
	public static final String	REQUEST_MODE = "mode";
	public static final String	REQUEST_MODE_NEW = "new";
	public static final String	REQUEST_MODE_EDIT = "edit";
	public static final String	REQUEST_PRIMARY_KEY = "pk";
	public static final int REQUEST_CODE_DIARY_ENTRY = 1;
	public static final int REQUEST_CODE_APPOINTMENT = 2;
	public static final int REQUEST_CODE_TEST_RESULT = 3;
	
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
        mAppSectionsPagerAdapter = new MainPagerAdapter(fragmentManager, this);

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
    
    /**
	 * @return a DatabaseHelper
	 */
	protected DatabaseHelper getHelper() {
	    if (databaseHelper == null) {
	        databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }
	    return databaseHelper;
	}

	/**
	 * Releases the database helper
	 */
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
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
    	try {
    		Dao<Event, Integer> eventDao = getHelper().getDao(Event.class);
    		QueryBuilder<Event, Integer> builder = eventDao.queryBuilder();
    		Where<Event, Integer> where = builder.where();
    		where.eq("type", Event.Type.APPOINTMENT).or();
    		where.eq("type", Event.Type.TIP).or();
    		where.eq("type", Event.Type.DIARY_ENTRY);
    		builder.orderBy("date", true);
    		PreparedQuery<Event> preparedQuery = builder.prepare();
    		events = eventDao.query(preparedQuery);
    		
    		/*
    		Event e = new Event();
    		e.setType(Event.Type.DIARY_ENTRY);
    		e.setDate(new Date());
    		e.setPhotoFile("rosy.jpg");
    		e.setText("Hard coded test entry!");
    		events.add(2, e);

    		e = new Event();
    		e.setType(Event.Type.APPOINTMENT);
    		e.setDate(new Date());
    		e.setDoctor("Hard coded appointment");
    		e.setText("...appointment notes");
    		e.setAddress("My doctor's address");
    		events.add(20, e);
    		*/
    		
    	}
    	catch (SQLException e){
    		Log.e(DEBUG_TAG, "Can't read events table", e);
    	}
    	return events;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to the My Timeline,
     * My Care or I Need page.
     */
    public static class MainPagerAdapter extends FragmentPagerAdapter {

    	private MainActivity activity;

    	public MainPagerAdapter(FragmentManager fm, MainActivity activity) {
    		super(fm);
    		this.activity = activity;
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
    			return activity.getText(R.string.TabName_Timeline);
    		case 1:
    			return activity.getText(R.string.TabName_MyCare);
    		default:
    			return activity.getText(R.string.TabName_INeed);
    		}
    	}
    }
}
