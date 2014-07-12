package com.thrivepregnancy.ui;

import java.io.File;
import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.thrivepregnancy.R;
import com.thrivepregnancy.data.DatabaseHelper;
import com.thrivepregnancy.data.Event;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.DatePicker;

/**
 * Contains the My Timeline, My Care and I Need screens ("pages")
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener, OnDateSetListener {
	
	public static final String DEBUG_TAG = "THRIVE";
	
	public static final String	REQUEST_MODE 		= "mode";
	public static final String	REQUEST_MODE_NEW 	= "new";
	public static final String	REQUEST_MODE_EDIT 	= "edit";
	public static final String	REQUEST_PRIMARY_KEY = "pk";
	
	public static final int REQUEST_CODE_DIARY_ENTRY = 1;
	public static final int REQUEST_CODE_APPOINTMENT = 2;
	public static final int REQUEST_CODE_TEST_RESULT = 3;
	
	private static final int DIALOG_ID_CONFIRM 	= 0;
	private static String	DIALOG_TITLE 		= "DIALOG_TITLE";
	private static String	DIALOG_TEXT			= "DIALOG_TEXT";
	private static String	DIALOG_VISIBLE		= "DIALOG_VISIBLE";
	
	static final int	DELETE_FROM_TIMELINE = 0;
	static final int	DELETE_FROM_CARE = 1;
	
	private static int  	currentTab;
	private static boolean	rotating;
	
	private boolean		dialogVisible;
	private int 		dialogTitleId;
	private int 		deletingFrom;
	
	private Integer 	eventId;
	
    private MainPagerAdapter 						mainPageAdapter;
    private TimelineFragment.TimelineListAdapter 	timelineListAdapter;
    private CareFragment.CareListAdapter 			careListAdapter;
    /**
     * The {@link ViewPager} implements the page swipe animation
     */
    private ViewPager 			viewPager;
    
    private TimelineFragment	timelineFragment;
    private CareFragment		careFragment;
//    private NeedFragment		needFragment;
    
    private MainActivity		mainActivity;
	private DatabaseHelper 		databaseHelper;	
	private ActionBar 			actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        
		if (savedInstanceState != null){
			if (savedInstanceState.containsKey(DIALOG_VISIBLE)){
				dialogVisible = savedInstanceState.getBoolean(DIALOG_VISIBLE);
				if (dialogVisible){
					dialogTitleId = savedInstanceState.getInt(DIALOG_TITLE);
					eventId = savedInstanceState.getInt("eventId");
					deletingFrom= savedInstanceState.getInt("deletingFrom");
				}
			}
		}
        // Create the adapter that will return a fragment for each of the pages
        FragmentManager fragmentManager = getSupportFragmentManager();
        mainPageAdapter = new MainPagerAdapter(fragmentManager, this);
//		timelineFragment = new TimelineFragment();
//		careFragment = new CareFragment();
//		needFragment = new NeedFragment();

        // Set up the action bar.
        actionBar = getActionBar();
        
        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar if space allows (e.g. in landscape mode).
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between pages.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(mainPageAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different pages, select the corresponding tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the page fragments, add a tab to the action bar.
        for (int i = 0; i < mainPageAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mainPageAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }
    
    @Override
    public void onResume(){
    	super.onResume();
//    	Log.d(DEBUG_TAG, "--- onResume: selecting tab " + currentTab +  " set rotate=false");
    	rotating = false;
    	actionBar.setSelectedNavigationItem(currentTab);    	
    }
    
	@Override
	protected void onSaveInstanceState (Bundle outState){
		if (dialogVisible){
			outState.putBoolean(DIALOG_VISIBLE, true);
			outState.putInt(DIALOG_TITLE, dialogTitleId);
			outState.putInt("eventId", eventId);
			outState.putInt("deletingFrom", deletingFrom);
		}
	}
	@Override
	protected void onRestoreInstanceState (Bundle savedState){
		if (savedState.containsKey(DIALOG_VISIBLE)){
			dialogVisible = true;
			dialogTitleId = savedState.getInt(DIALOG_TITLE);
			eventId = savedState.getInt("eventId");
			deletingFrom= savedState.getInt("deletingFrom");
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
	
	void setTimelineListAdapter(TimelineFragment.TimelineListAdapter timelineListAdapter) {
		this.timelineListAdapter = timelineListAdapter;
	}
	void setCareListAdapter(CareFragment.CareListAdapter careListAdapter) {
		this.careListAdapter = careListAdapter;
	}

	void setCareFragment(CareFragment careFragment) {
		this.careFragment = careFragment;
	}
    CareFragment getCareFragment() {
		return careFragment;
	}
    TimelineFragment getTimelineFragment() {
		return timelineFragment;
	}

	@Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
		if (rotating){
//			Log.d(DEBUG_TAG, "onTabSelected : " + tab.getPosition() + " rotating     currentTab=" + currentTab);			
		}
		else{
			currentTab = tab.getPosition();			
//			Log.d(DEBUG_TAG, "onTabSelected : " + tab.getPosition() + " not rotating currentTab=" + currentTab);
		}
		viewPager.setCurrentItem(tab.getPosition());
    }
    
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft){
//    	Log.d(DEBUG_TAG, "onTabReSelected : " + tab.getPosition());
    }
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft){
//    	Log.d(DEBUG_TAG, "onTabUnSelected : " + tab.getPosition());
    }

    /**
     * Called when screen rotates
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance(){
//    	Log.d(DEBUG_TAG, "onRetainCustomNonConfigurationInstance set rotate=true");
    	rotating = true;
    	return null;
    }    
 
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to the My Timeline,
     * My Care or I Need page.
     */
    public class MainPagerAdapter extends FragmentPagerAdapter {

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
    			timelineFragment = new TimelineFragment();
    			return timelineFragment;
    		case 1:
    			careFragment = new CareFragment();
    			return careFragment;
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
    
	private DialogInterface.OnClickListener dialogListener;

	/**
	 * Displays a delete confirmation popup
	 * @param dialogTitleId resource id of the dialog title string, also used as the dialog id
	 * @param dialogListener listener for the positive and negative dialog buttons
	 */
    void showConfirmationDialog(int dialogTitleId, DialogInterface.OnClickListener dialogListener, Event event, int deletingFrom){
    	this.dialogListener = dialogListener;
		this.eventId = event.getId();
		this.deletingFrom = deletingFrom;
    	
    	Bundle params = new Bundle();
    	params.putString("title", getString(dialogTitleId));
		dialogVisible = true;
    	showDialog(DIALOG_ID_CONFIRM, params);
    }
    
    /**
     * Called by the system when the ap calls showDialog
     */
	@Override
    protected Dialog onCreateDialog(int id, Bundle params) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // If title and message not set here, no title or message area will be generated
        builder.setTitle(params.getString("title"));
        builder.setMessage(getString(R.string.dlg_prompt));
        builder.setPositiveButton(getString(R.string.dlg_yes), new DialogButtonListener());
        builder.setNegativeButton(getString(R.string.dlg_no), new DialogButtonListener());
        // Create the AlertDialog object and return it
        return builder.create();
    }
	@Override
	protected void onPrepareDialog (int id, Dialog dialog, Bundle params){
		((AlertDialog)dialog).setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dlg_yes), new DialogButtonListener());
		((AlertDialog)dialog).setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dlg_no), new DialogButtonListener());
		((AlertDialog)dialog).setTitle(params.getString("title"));
	}
	
	private class DialogButtonListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which){
			if (which == DialogInterface.BUTTON_POSITIVE){
				try {
					Dao<Event, Integer> dao = databaseHelper.getDao(Event.class);
					Log.d(DEBUG_TAG, "Deleting event " + eventId);
					Event event = dao.queryForId(mainActivity.eventId);
					dao.delete(event);
					deleteMediaFile(event.getAudioFile());
					deleteMediaFile(event.getPhotoFile());
					mainActivity.timelineListAdapter.refresh(TimelineFragment.RefreshType.ON_DELETE);
					careFragment.refresh();
				}
				catch (SQLException e){
					Log.e(DEBUG_TAG, "Can't delete event", e);
				}
			}
			dialogVisible = false;	
		}
    	private void deleteMediaFile(String fileName){
    		if (fileName != null && fileName.length() > 0){
    			File file = new File(fileName);
    			Log.d(MainActivity.DEBUG_TAG, file.delete() ? "Deleted " + fileName : "********** Can't delete " + fileName);
    		}
    	}
	}
    /**
     * Called when the date has been set when editing the care provider section
     * in the CareFragment 
     */
	public void onDateSet(DatePicker view, int year, int month, int day) {
		careFragment.onDateSet(view, year, month, day);
	}
}
