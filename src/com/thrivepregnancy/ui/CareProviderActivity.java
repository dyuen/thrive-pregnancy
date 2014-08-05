package com.thrivepregnancy.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.app.DatePickerDialog.OnDateSetListener;

import com.thrivepregnancy.R;

/**
 * Activity for editing the care provider information on the MyCare page
 */
public class CareProviderActivity extends FragmentActivity implements OnDateSetListener{
	private static final SimpleDateFormat dueDateFormat = new SimpleDateFormat("MMM d", Locale.CANADA);

	private String providerName;
	private String providerLocation;
	private String oncallPhone;	
	private long   dueDate;

	private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);    	
    	LayoutInflater inflater = getLayoutInflater();    	
    	LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.activity_careprovider, null);    	
    	setContentView(layout);
        
    	preferences =  getSharedPreferences(StartupActivity.PREFERENCES, MODE_PRIVATE);
        
    	providerName = preferences.getString(StartupActivity.PREFERENCE_PROVIDER_NAME, "");
    	providerLocation = preferences.getString(StartupActivity.PREFERENCE_PROVIDER_LOCATION, "");
    	oncallPhone = preferences.getString(StartupActivity.PREFERENCE_ONCALL_NUMBER, "");
    	((EditText)findViewById(R.id.provider_name_edit)).setText(providerName);
    	((EditText)findViewById(R.id.provider_location_edit)).setText(providerLocation);
    	((EditText)findViewById(R.id.provider_oncall_phone_edit)).setText(oncallPhone);

	    if (savedInstanceState == null){
        	dueDate = preferences.getLong(StartupActivity.PREFERENCE_DUE_DATE, 0);
        }
	    String strDueDate = dueDateFormat.format(new Date(dueDate));
	    
	    EditText dueDateField = (EditText)findViewById(R.id.delivery_date_edit);
	    dueDateField.setText(strDueDate);
	    dueDateField.setOnClickListener(new OnClickListener() {        
            public void onClick(View v) {
            	Calendar earliest = Calendar.getInstance();
            	earliest.setTimeInMillis(dueDate);
            	earliest.add(Calendar.DAY_OF_YEAR, -60);
            	
            	Calendar latest = Calendar.getInstance();
            	latest.setTimeInMillis(dueDate);
            	latest.add(Calendar.DAY_OF_YEAR, 60);
            	
            	DateDialogFragment fragment = DateDialogFragment.newInstance("1", R.string.PersonalInfo_Due_Date, 
            			earliest.getTimeInMillis(), latest.getTimeInMillis(), dueDate);
               	fragment.show(getSupportFragmentManager(), "1");
            }
        }); 

        ActionBar actionBar = getActionBar();
        actionBar.setLogo(R.drawable.ic_logo_arrow);
        actionBar.setTitle(R.string.PersonalInfo_Provider_Edit);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }
    
    /**
     * Sets up the Save menu
     */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.save, menu);
	    return true;
	}	
    
    /**
     * Called when either Save or Home is pressed
     */
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	
	    switch (item.getItemId()) {
	    	case android.R.id.home:
	            // app icon in action bar clicked; goto parent activity.
	            finish();
	            return true;
            
	        case R.id.menu_save:
				SharedPreferences.Editor editor = preferences.edit();
				// Update the "cached" values and store them in preferences
				providerName = ((EditText)findViewById(R.id.provider_name_edit)).getText().toString();
				providerLocation = ((EditText)findViewById(R.id.provider_location_edit)).getText().toString();
				oncallPhone = ((EditText)findViewById(R.id.provider_oncall_phone_edit)).getText().toString();				

				editor.putString(StartupActivity.PREFERENCE_PROVIDER_NAME, providerName);
				editor.putString(StartupActivity.PREFERENCE_PROVIDER_LOCATION, providerLocation);
				editor.putString(StartupActivity.PREFERENCE_ONCALL_NUMBER, oncallPhone);

				// Due date may have changed: recalculate the timeline
				editor.putLong(StartupActivity.PREFERENCE_DUE_DATE,  dueDate);
				editor.commit();
				
				setResult(Activity.RESULT_OK, getIntent());
		        finish();
		        return true;

	        default:	        	
	            return super.onOptionsItemSelected(item);
	    }
	}
    /**
     * Called when the date has been set 
     */
	public void onDateSet(DatePicker view, int year, int month, int day) {
		Calendar date = Calendar.getInstance();
		
		date.set(Calendar.YEAR, year);		
		date.set(Calendar.MONTH, month);
		date.set(Calendar.DAY_OF_MONTH, day);
		
		dueDate = date.getTimeInMillis();
		String strDueDate = dueDateFormat.format(new Date(dueDate));
		((EditText)findViewById(R.id.delivery_date_edit)).setText(strDueDate);
    }
}
