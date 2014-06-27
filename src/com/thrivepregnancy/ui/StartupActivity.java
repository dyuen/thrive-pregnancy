package com.thrivepregnancy.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import 	android.view.MotionEvent;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.app.DatePickerDialog.OnDateSetListener;

import com.thrivepregnancy.R;

/**
 * Displays the registration screen for the user to enter name and due date.
 * This is executed only upon a new install (but not an update)
 */
public class StartupActivity extends FragmentActivity implements OnDateSetListener{
	
	/**
	 * Name of Preferences
	 */
	public static final String PREFERENCES = "preferences";
	/**
	 * User name Preferences key
	 */
	public static final String PREFERENCE_NAME = "name";
	/**
	 * Due date Preferences key. The date is saved as a long
	 */	
	public static final String PREFERENCE_DUE_DATE = "date";
	/**
	 * Provider name Preferences key
	 */
	public static final String PREFERENCE_PROVIDER_NAME = "provider";
	/**
	 * Provider location Preferences key
	 */
	public static final String PREFERENCE_PROVIDER_LOCATION = "location";
	/**
	 * Provider 24 hour on call number  Preferences key
	 */
	public static final String PREFERENCE_ONCALL_NUMBER = "number";
	/**
	 * Week number of first tip
	 */
	public static final String PREFERENCE_FIRST_WEEK = "first";
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMMMMM d, yyyy", Locale.CANADA);

	private String 				name = null;
	private FragmentManager		fragmentManager;
	private Calendar 			calendar;
	private OnDateSetListener 	dateListener;
	private final static Integer MIN_NAME_LENGTH = 3;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        fragmentManager = getSupportFragmentManager();
        dateListener = this;
        
        // If name has been set, proceed immediately to main screen
    	SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    	if (preferences.getString(PREFERENCE_NAME, null) != null){
        	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        	startActivity(intent);
        	this.finish();
    	}
    	
    	EditText dateView = (EditText)findViewById(R.id.startup_date);
        dateView.setOnClickListener(new OnClickListener() {        
            public void onClick(View v) {
              	DateDialogFragment fragment = DateDialogFragment.newInstance("1", dateListener);
               	fragment.show(fragmentManager, "1");
            }
        });
    }
    
    /**
     * Called when the date has been set 
     */
	public void onDateSet(DatePicker view, int year, int month, int day) {
		// Save the new date in the calendar
		if (calendar == null){
			calendar = Calendar.getInstance();
			calendar.clear();        
		}
		calendar.set(Calendar.YEAR, year);		
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
        
		// Update the display
        TextView date = (TextView)findViewById(R.id.startup_date);
        date.setText(dateFormat.format(calendar.getTime()));
    }

    /**
     * Called when the Save menu item is pressed
     */
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_save:
	        	// Confirm that name and date are set. For confirming
	        	// that a "full" name is entered, check its length
	        	EditText nameView = (EditText)findViewById(R.id.startup_name);       
	        	name = nameView.getText().toString();	        	
	        	if (name != null && (name.length() >= MIN_NAME_LENGTH) && calendar != null){
	        		// Save name and date in preferences
	        		saveValues();
	        		// Proceed to the main screen
		        	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		        	startActivity(intent);
	    		}
	        	else {
	        		// Remind user to enter both name and date
		        	TextView textView = (TextView)findViewById(R.id.startup_warning);
	        		textView.setText(R.string.startup_warning);
	        	}
	            return true;
	        
	        default:	        	
	            return super.onOptionsItemSelected(item);
	    }
	}
    
    private void saveValues(){
    	long dateInMillis = calendar.getTimeInMillis();
    	SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
    	editor.putString(PREFERENCE_NAME, name);
    	editor.putLong(PREFERENCE_DUE_DATE, dateInMillis);
    	editor.commit();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.save, menu);
	    return true;
	}		
}
