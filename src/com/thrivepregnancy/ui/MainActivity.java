package com.thrivepregnancy.ui;

import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.thrivepregnancy.R;
import com.thrivepregnancy.data.DatabaseHelper;
import com.thrivepregnancy.data.Event;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

/**
 * Contains the My Timeline, My Care and I Need screens ("pages")
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener, OnDateSetListener {
	
	public static final String DEBUG_TAG = "THRIVE";
	
	public static final String	REQUEST_MODE 		= "mode";
	public static final String	REQUEST_MODE_NEW 	= "new";
	public static final String	REQUEST_MODE_EDIT 	= "edit";
	public static final String	REQUEST_PRIMARY_KEY = "pk";
	
	// Request codes passed to activities by startActivityForResult()
	public static final int REQUEST_CODE_DIARY_ENTRY 	= 1;
	public static final int REQUEST_CODE_APPOINTMENT 	= 2;
	public static final int REQUEST_CODE_TEST_RESULT 	= 3;
	public static final int REQUEST_CODE_PROVIDER_EDIT 	= 4;
	
	private static final int DIALOG_ID_CONFIRM 	= 0;
	private static final int DIALOG_ID_DATE 	= 1;
	private static final int DIALOG_ID_QUESTION	= 2;
	
	private static String	KEY_DIALOG_TITLE 		= "DIALOG_TITLE";
	private static String	KEY_DIALOG_ID 			= "DIALOG_ID";
	private static String	KEY_DIALOG_BUTTON 		= "DIALOG_BUTTON";
	private static String	KEY_DIALOG_VIEW 		= "DIALOG_VIEW";
	private static String	KEY_DIALOG_VISIBLE		= "DIALOG_VISIBLE";	
	private static String	KEY_DATE_EARLIEST		= "EARLIEST";	
	private static String	KEY_DATE_LATEST			= "LATEST";	
	private static String	KEY_DATE_DEFAULT		= "DEFAULT";	
	
	private static int  	currentTab;
	private static boolean	rotating;
	
	private boolean		dialogVisible;
	private int 		dialogTitleId;
	private int 		dialogId;
	private DatePicker	datePicker;
	private EditText 	newQuestion;
	
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
    private OnDateSetListener	dateSetListener;
    
    private MainActivity		mainActivity;
	private DatabaseHelper 		databaseHelper;	
	private ActionBar 			actionBar;
	
	private Bundle				careFragmentBundle;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        
		if (savedInstanceState != null){
			if (savedInstanceState.containsKey(KEY_DIALOG_VISIBLE)){
				dialogVisible = savedInstanceState.getBoolean(KEY_DIALOG_VISIBLE);
				if (dialogVisible){
					dialogTitleId = savedInstanceState.getInt(KEY_DIALOG_TITLE);
					eventId = savedInstanceState.getInt("eventId");
				}
			}
		}
		// Will be non null if device was rotated with a date dialog visible 
		MainActivity previousInstance = (MainActivity)getLastCustomNonConfigurationInstance();
		if (previousInstance != null){
			this.datePicker = previousInstance.datePicker;
			this.dateSetListener = previousInstance.dateSetListener;
			this.careFragmentBundle = previousInstance.careFragmentBundle;
			previousInstance = null;
		}
		
        // Create the adapter that will return a fragment for each of the pages
        FragmentManager fragmentManager = getSupportFragmentManager();
        mainPageAdapter = new MainPagerAdapter(fragmentManager, this);

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
    	rotating = false;
    	actionBar.setSelectedNavigationItem(currentTab);
    	
    }
    
	@Override
	protected void onSaveInstanceState (Bundle outState){
		if (dialogVisible){
			outState.putBoolean(KEY_DIALOG_VISIBLE, true);
			outState.putInt(KEY_DIALOG_TITLE, dialogTitleId);
			if (eventId != null){
				outState.putInt("eventId", eventId);
			}
		}
	}
	@Override
	protected void onRestoreInstanceState (Bundle savedState){
		if (savedState.containsKey(KEY_DIALOG_VISIBLE)){
			dialogVisible = true;
			dialogTitleId = savedState.getInt(KEY_DIALOG_TITLE);
			eventId = savedState.getInt("eventId");
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
		if (!rotating){
			currentTab = tab.getPosition();			
		}
		viewPager.setCurrentItem(tab.getPosition());
    }
    
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft){}
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft){}

    /**
     * Interface allowing child fragments to save state in ConfigurationInstance
     */
    interface InstanceSaver {
    	public Bundle saveInstanceData();
    	public void restoreInstanceData(Bundle data);
    }
    Bundle getCareFragmentBundle(){
    	return careFragmentBundle;
    }
    /**
     * Called when screen rotates
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance(){
    	rotating = true;
    	careFragmentBundle = ((InstanceSaver)careFragment).saveInstanceData();
    	return this;
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
    			((InstanceSaver)careFragment).restoreInstanceData(careFragmentBundle);
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
    
	/**
	 * Displays a delete confirmation popup
	 * @param dialogTitleId resource id of the dialog title string
	 * @param dialogListener listener for the positive and negative dialog buttons
	 */
    void showConfirmationDialog(int dialogTitleId, Event event){
		this.eventId = event.getId();
    	
    	Bundle params = new Bundle();
    	params.putString(KEY_DIALOG_TITLE, getString(dialogTitleId));
		dialogVisible = true;
    	showDialog(DIALOG_ID_CONFIRM, params);
    }
    
	/**
	 * Displays a single button dialog containing an arbitrary View as the content
	 * @param dialogTitleId resource id of the dialog title string
	 * @param dialogListener listener for the button press
	 * @param contentViewId the view to be displayed in the dialog
	 * @param buttonTextId resource id of the button text. 
	 */
    void showQuestionDialog(int dialogTitleId, int contentViewId, int buttonTextId){
    	Bundle params = new Bundle();
    	params.putString(KEY_DIALOG_TITLE, getString(dialogTitleId));
    	params.putString(KEY_DIALOG_BUTTON, getString(buttonTextId));
    	params.putInt(KEY_DIALOG_VIEW, contentViewId);
		dialogVisible = true;
    	showDialog(DIALOG_ID_QUESTION, params);
    }
    
	/**
	 * Displays a DateDialog using the legacy, i.e. non fragment method. This avoids
	 * problems encountered nesting  a DateDialogFragment within another fragment
	 * @param titleId
	 * @param earliestDate
	 * @param latestDate
	 * @param defaultDate
	 * @param dateSetListener
	 */
	DatePicker showDateDialog(int titleId, long earliestDate, long latestDate, long defaultDate, OnDateSetListener dateSetListener){
		this.dateSetListener = dateSetListener;
    	Bundle params = new Bundle();
    	params.putString(KEY_DIALOG_TITLE, getString(titleId));
    	if (earliestDate != -1){
    		params.putLong(KEY_DATE_EARLIEST, earliestDate);
    	}
    	if (latestDate != -1){
    		params.putLong(KEY_DATE_LATEST, latestDate);
		}
    	if (defaultDate != -1){
    		params.putLong(KEY_DATE_DEFAULT, defaultDate);
    	}
		showDialog(this.DIALOG_ID_DATE, params);
		return datePicker;
	}
    /**
     * Called by the system when the app calls showDialog
     */
	@Override
    protected Dialog onCreateDialog(int id, Bundle params) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(params.getString(KEY_DIALOG_TITLE));
        
        if (id == DIALOG_ID_CONFIRM){
            // If title and message not set here, no title or message area will be generated
            builder.setMessage(getString(R.string.dlg_prompt));
            builder.setPositiveButton(getString(R.string.dlg_yes), new DialogButtonListener(DIALOG_ID_CONFIRM));
            builder.setNegativeButton(getString(R.string.dlg_no), new DialogButtonListener(DIALOG_ID_CONFIRM));
        }
        else  if (id == DIALOG_ID_QUESTION){
        	builder.setPositiveButton(params.getString(KEY_DIALOG_BUTTON), new DialogButtonListener(DIALOG_ID_QUESTION));
        	LayoutInflater inflater = getLayoutInflater();
        	View layout = (View)inflater.inflate(params.getInt(KEY_DIALOG_VIEW), null);
        	newQuestion = (EditText)layout.findViewById(R.id.new_question_text);
            builder.setView(layout);
            Dialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					newQuestion.setText("");
					showKeyboard(newQuestion);
				}
			});
            return dialog;
        }
        else {
        	// Dialog is a DatePickerDialog
        	builder.setPositiveButton(R.string.save, null);
        	DatePicker oldDatePicker = datePicker;
        	datePicker = (DatePicker)getLayoutInflater().inflate(R.layout.date_picker, null);
    		long param = params.getLong(KEY_DATE_EARLIEST, 0);
    		/***** Minimum and maximum date setting removed due to bug in Android Galaxy 3 (API level 15)
    		if (param != 0){
    			datePicker.setMinDate(params.getLong(KEY_DATE_EARLIEST, 0));
    		}
    		param = params.getLong(KEY_DATE_LATEST, 0);
    		if (param != 0){
    			datePicker.setMaxDate(params.getLong(KEY_DATE_LATEST, 0));
    		}
    		*****/
        	if (oldDatePicker == null){
        		param = params.getLong(KEY_DATE_DEFAULT, 0);
        		if (param != 0){
        			Calendar calendar = Calendar.getInstance();
        			calendar.setTimeInMillis(param);
        			datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        		}
        	}
        	else {
        		datePicker.updateDate(oldDatePicker.getYear(), oldDatePicker.getMonth(), oldDatePicker.getDayOfMonth());
        	}
         	builder.setView(datePicker);
        }
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	@Override
	protected void onPrepareDialog (int id, final Dialog dialog, Bundle params){
        if (id == DIALOG_ID_CONFIRM){
        	((AlertDialog)dialog).setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dlg_yes), new DialogButtonListener(DIALOG_ID_CONFIRM));
        	((AlertDialog)dialog).setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dlg_no), new DialogButtonListener(DIALOG_ID_CONFIRM));
        	((AlertDialog)dialog).setTitle(params.getString(KEY_DIALOG_TITLE));
        }
        else if (id == DIALOG_ID_DATE){
        	// Dialog is DatePickerDialog
        	Button button = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        	button.setText(getString(R.string.save));
        	button.setOnClickListener(new View.OnClickListener(){
        		public void onClick(View v){
        			dateSetListener.onDateSet(datePicker, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        			dialog.dismiss();
        		}
        	});
        }
        else if (id == DIALOG_ID_QUESTION){
        	
 //       	((AlertDialog)dialog).setContentView(params.getInt(KEY_DIALOG_VIEW));
        }
 	}
	
	/**
	 * A listener for dialog button clicks. It stores the id of the dialog to
	 * allow its use by multiple dialog types
	 */
	private class DialogButtonListener implements DialogInterface.OnClickListener {
		private int dialogId;
		
		DialogButtonListener(int dialogId){
			this.dialogId = dialogId;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which){
			if (dialogId == DIALOG_ID_CONFIRM){
				if (which == DialogInterface.BUTTON_POSITIVE){
					// Delete the event
					try {
						Dao<Event, Integer> dao = databaseHelper.getDao(Event.class);
						Event event = dao.queryForId(mainActivity.eventId);
						dao.delete(event);
						deleteMediaFile(event.getAudioFile());
						deleteMediaFile(event.getPhotoFile());
						mainActivity.timelineListAdapter.refresh(TimelineFragment.RefreshType.ON_DELETE);
						careFragment.refresh();
					}
					catch (SQLException e){
						//Log.e(DEBUG_TAG, "Can't delete event", e);
					}
				}
			}
			else if (dialogId == DIALOG_ID_QUESTION){
				// Create a new question event
				try {
					String text = newQuestion.getText().toString();
					if (text != null && text.length() > 0){
						Dao<Event, Integer> dao = databaseHelper.getDao(Event.class);
						Event question = new Event();
						question.setType(Event.Type.QUESTION);
						question.setDate(new Date());
						question.setText(text);
						dao.create(question);
						careFragment.refresh();
					}
				}
				catch (SQLException e){
					//Log.e(MainActivity.DEBUG_TAG, "Can't create question Event",  e);
				}
			}
			dialogVisible = false;	
		}
    	private void deleteMediaFile(String fileName){
    		if (fileName != null && fileName.length() > 0){
    			File file = new File(fileName);
    			if (!file.delete()){
    				//Log.e(MainActivity.DEBUG_TAG, "********** Can't delete " + fileName);
    			}
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
	private void showKeyboard(View view){
     	InputMethodManager inputManager = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
    	inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}

}
