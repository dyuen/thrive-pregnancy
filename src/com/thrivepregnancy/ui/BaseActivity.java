package com.thrivepregnancy.ui;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.thrivepregnancy.R;
import com.thrivepregnancy.data.DatabaseHelper;
import com.thrivepregnancy.data.Event;
/**
 * A convenience superclass for Activity classes which may require
 * the ORMLITE DatabaseHelper object
 */
public class BaseActivity extends FragmentActivity implements OnDateSetListener, OnTimeSetListener {

	private DatabaseHelper 	databaseHelper = null;
	
	private String m_mode;
	private Dao<Event, Integer> m_eventDao;
	protected Event m_event;
	
	private static SimpleDateFormat m_dateFormat = new SimpleDateFormat("MMMMMMMMM d, yyyy", Locale.CANADA);
	private static SimpleDateFormat m_timeFormat = new SimpleDateFormat("hh:mm a", Locale.CANADA);
	private static SimpleDateFormat m_combinedFormat = new SimpleDateFormat("MMMMMMMMM d, yyyy hh:mm a", Locale.CANADA);
	
	private Integer m_result = 0;
	private OnTimeSetListener m_timeListener;
	private Calendar m_date;
	private Calendar m_CalFirstWeek;
	private long m_dueDate;
	private long m_selectedDate;
	
	private TextView m_warnView;
	private String m_warning;
	
	private EditText m_dateView;
	private EditText m_noteView;
	private EditText m_purposeView;
	private EditText m_timeView;
	private EditText m_addressView;
	private EditText m_doctorView;
	
	private ImageView m_photoView;
	private ImageButton m_buttonDelete;
	private ImageButton m_buttonCreate;
	private TextView m_photoText;
	
	private String m_currentPhotoPath;
	
	private LinearLayout m_layout;
	private Event.Type m_eventType;
	
	private ActionBar m_actionBar;
	
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final String IMAGE_PATH = "imagePath";
	private static final String DELETED = "deleted";
	
	private static final String BLANK_APPT = "Appointment";
	private static final String BLANK_DIARY = "Diary Entry";
	private static final String BLANK_TEST = "Test Result";
	
	private static final String APPT_WARNING = "Please enter date and time of your appointment";
	private static final String DIARY_WARNING = "Please enter a diary note or a picture";
	private static final String TEST_WARNING = "Please enter a test result note or a picture";
	
	 /**
     * Setup global variables at startup
     */
	protected void StartUp(Event.Type type) {
        // Mode will be one of MainActivity.REQUEST_MODE_NEW or MainActivity.REQUEST_MODE_EDIT
        // If mode is edit, then REQUEST_PRIMARY_KEY will contain the primary key of the Event
		String activityTitle;
		SharedPreferences preferences =  getSharedPreferences(StartupActivity.PREFERENCES, MODE_PRIVATE);
		m_dueDate = preferences.getLong(StartupActivity.PREFERENCE_DUE_DATE, 0);
		m_CalFirstWeek = Calendar.getInstance();
		m_CalFirstWeek.setTimeInMillis(m_dueDate);
		m_CalFirstWeek.add(Calendar.DAY_OF_YEAR, 7 * (-40));
		
        m_mode = getIntent().getStringExtra(MainActivity.REQUEST_MODE);
        m_eventType = type;
        
        m_eventDao = getHelper().getEventDao();
        
        m_date = Calendar.getInstance();        
        
        m_timeListener = this;
        
        if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_EDIT)) {
        	activityTitle = "Edit ";
        } else {
        	activityTitle = "New ";
        }
        
        switch(m_eventType) {
        case DIARY_ENTRY:
        	activityTitle += getString(R.string.Diary_Action);
        	break;
        case APPOINTMENT:
        	activityTitle += getString(R.string.Appt_Action);
        	break;
        case TEST_RESULT:
        	activityTitle += getString(R.string.Test_Action);
        	break;	
        }
        
        m_actionBar = getActionBar();
        m_actionBar.setLogo(R.drawable.ic_logo_arrow);
        m_actionBar.setTitle(activityTitle);
        m_actionBar.setDisplayUseLogoEnabled(true);
        m_actionBar.setHomeButtonEnabled(true);
        
        CreateEvent();
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	/**
	 * 
     * Saves Application State
     */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    savedInstanceState.putString(IMAGE_PATH, m_currentPhotoPath);
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	/**
	 * 
     * Restores Application State
     */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // Always call the superclass so it can restore the view hierarchy
	    super.onRestoreInstanceState(savedInstanceState);
	    
	    // Restore state members from saved instance
	    m_currentPhotoPath = savedInstanceState.getString(IMAGE_PATH);
	    
	    if (m_currentPhotoPath != null) {
	    	Log.d("onRestoreInstanceState", m_currentPhotoPath);
	    	
	    	if(m_currentPhotoPath.equalsIgnoreCase(DELETED)) {
	    		m_event.setPhotoFile(null);
	    	} else {
	    		m_event.setPhotoFile(m_currentPhotoPath);
	    	}
	    	
	    	if (SetPhoto()) {
            	setContentView(m_layout);
            }
	    }
	}

    /**
     * Creates Event object either from existing record or new
     */
    private void CreateEvent() {
    	
    	//Edit existing event
        if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_EDIT)) {
        	try {
        		m_event = m_eventDao.queryForId(getIntent().getIntExtra(MainActivity.REQUEST_PRIMARY_KEY,0));
        		m_date.setTime(m_event.getDate());
        	} catch (SQLException e) {
    			Log.e(BaseActivity.class.getName(), "Unable to query event", e);
    		}
        	
        //create new event
        } else if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_NEW)) {
        	Log.d(MainActivity.DEBUG_TAG, "CreateEvent craeting new Event : mode = " + m_mode);
       		m_event = new Event();
        	
        	switch (m_eventType) {
        	case APPOINTMENT:
        		m_date.set(Calendar.HOUR_OF_DAY, 12);
        		m_date.set(Calendar.MINUTE, 0);
        		
        		m_event.setDate(m_date.getTime());
        		break;
        	default:
        		m_event.setDate(m_date.getTime());
        		break;
        	}
        	
        	m_event.setType(m_eventType);
        } 
    }
    
    /**
     * Fills views object from events
     */
    protected void FillViews(LinearLayout layout) {
    	m_layout = layout;
    	
    	if (m_dateView != null) {
    		setupDatePicker();	    	
	    	String strDate = m_dateFormat.format(m_event.getDate());	    	
	        m_dateView.setText(strDate);
    	}
    	
    	if (m_timeView != null) {
    		m_timeView.setOnClickListener(new OnClickListener() {        
	            public void onClick(View v) {
	              	TimeDialogFragment fragment = TimeDialogFragment.newInstance(
	              			"1", m_date.get(Calendar.HOUR_OF_DAY), m_date.get(Calendar.MINUTE), m_timeListener);
	               	fragment.show(getSupportFragmentManager(), "1");
	            }
	        });
	    	
	    	String strDate = m_timeFormat.format(m_event.getDate());	    	
	        m_timeView.setText(strDate);
		}

    	if (m_noteView != null) {
    		if (m_event.getText() != null && (m_event.getText().length() > 0)) {
    			m_noteView.setText(m_event.getText());
    		}
    	}    	
    	
    	if (m_purposeView != null) {
    		if (m_event.getPurpose() != null && (m_event.getPurpose().length() > 0)) m_purposeView.setText(m_event.getPurpose());
    	}
    	
		if (m_addressView != null) {
			if (m_event.getAddress() != null && (m_event.getAddress().length() > 0)) m_addressView.setText(m_event.getAddress());		
		}
		
		if (m_doctorView != null) {
			if (m_event.getDoctor() != null && (m_event.getDoctor().length() > 0)) m_doctorView.setText(m_event.getDoctor());
		}
		
        if (m_buttonCreate != null) {
	        m_buttonCreate.setOnClickListener(new OnClickListener() {        
	            public void onClick(View v) {
	            	DispatchTakePictureIntent();
	            }
	        });
        }
        
        if (m_photoText != null) {
        	m_photoText.setOnClickListener(new OnClickListener() {        
	            public void onClick(View v) {
	            	DispatchTakePictureIntent();
	            }
	        });
        }
        
        if (m_buttonDelete != null) {
	 		m_buttonDelete.setOnClickListener(new OnClickListener() {        
	            public void onClick(View v) {
	            	m_event.setPhotoFile(null);
	            	m_currentPhotoPath = DELETED;
	            	
	            	if (SetPhoto()) {
	                	setContentView(m_layout);
	                }
	            }
	        });
        }
        
        if (SetPhoto()) {
        	setContentView(m_layout);
        }
    }
    
    private void setupDatePicker(){
    	m_dateView.setOnClickListener(new OnClickListener() {        
            public void onClick(View v) {
            	int title = R.string.Diary_Popup_Date;
            	if (m_eventType.equals(Event.Type.APPOINTMENT)){
            		title = R.string.Appt_Popup_Date;
            	}
            	else if (m_eventType.equals(Event.Type.TEST_RESULT)){
            		title = R.string.Test_Popup_Date;
            	}
            	long today = Calendar.getInstance().getTimeInMillis();
    			
            	long earliest;
        		if (m_event.getType().equals(Event.Type.DIARY_ENTRY)){
        			earliest = m_CalFirstWeek.getTimeInMillis();
        		}
        		else {
        			earliest = today;
        		}
        		
            	long latest = m_dueDate;
            	
            	long initial;
            	if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_EDIT)){
        			initial = m_event.getDate().getTime();
            	}
            	else {
        			initial = m_selectedDate == 0 ? today : m_selectedDate;
            	}
            	DateDialogFragment fragment = DateDialogFragment.newInstance("1", title, earliest, latest, initial);
               	fragment.show(getSupportFragmentManager(), "1");
            }
        });    	
    }
    
    /**
     * Called when the date has been set 
     */
	public void onDateSet(DatePicker view, int year, int month, int day) {
		if (m_date == null){
			m_date = Calendar.getInstance();
			m_date.clear();        
		}
		
		m_date.set(Calendar.YEAR, year);		
		m_date.set(Calendar.MONTH, month);
		m_date.set(Calendar.DAY_OF_MONTH, day);
		
		Date date = m_date.getTime();
		// Save the new date in the event
		m_event.setDate(date);
		m_selectedDate = date.getTime();
		// Must recreate 
		setupDatePicker();        
		// Update the display
        m_dateView.setText(m_dateFormat.format(date.getTime()));
    }
    
    /**
     * Sets photo or add photo icon
     */
    private boolean SetPhoto() {
        if (m_event.getPhotoFile() != null && (m_event.getPhotoFile().length() > 0)) {
        	File filePhoto = new File(m_event.getPhotoFile());
        	
        	m_photoView.setVisibility(View.VISIBLE);
        	m_buttonDelete.setVisibility(View.VISIBLE);
        	
        	m_buttonCreate.setVisibility(View.GONE);
    		m_photoText.setVisibility(View.GONE);
    		
        	if (filePhoto.exists()) {
        		Bitmap photoBit = BitmapFactory.decodeFile(filePhoto.getAbsolutePath());
        		m_photoView.setImageBitmap(photoBit);
        		
        		return true;
        	} else {
        		Log.e(BaseActivity.class.getName(), "stored photo file does not exist");
        	}
        } else {
        	m_photoView.setVisibility(View.GONE);
        	m_buttonDelete.setVisibility(View.GONE);
        	
        	m_buttonCreate.setVisibility(View.VISIBLE);
    		m_photoText.setVisibility(View.VISIBLE);
    		
    		return true;
        }
        
        return false;
    }
    
    /**
     * Dispatches picture intent
     */
    private void DispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = CreateImageFile();
            } catch (IOException e) {
                // Error occurred while creating the File
            	Log.e(BaseActivity.class.getName(), "error creating photo file", e);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(photoFile));
                Log.d("DispatchTakePictureIntent", Uri.fromFile(photoFile).toString());
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    
    /**
     * Creates Image File and Sets Path
     */
    private File CreateImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(new Date());
        String imageFileName = m_eventType.toString() + "_" + timeStamp + ".jpg";

        File image = new File(this.getExternalFilesDir(null), imageFileName);

        m_currentPhotoPath = image.getAbsolutePath();
        
        Log.d("CreateImageFile", image.getAbsolutePath());
        
        return image;
    }
    
    /**
     * Take picture result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		Log.d("onActivityResult", "OK");
    		
    		if (m_currentPhotoPath != null && (m_currentPhotoPath.length() > 0)) m_event.setPhotoFile(m_currentPhotoPath);
    		
    		if (SetPhoto()) {
            	setContentView(m_layout);
            }
    		
    	} else if (resultCode == RESULT_CANCELED) {
    		Log.d("onActivityResult", "Cancelled");
    	}
    }
    
    /**
     * Saves Event Object to database
     */
    protected boolean SaveEvent() {
    	try {
	        if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_EDIT)) {
	        	m_eventDao.update(m_event);
	        }  else if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_NEW)) {
	        	m_eventDao.create(m_event);
	        }
	        
	        return true;
    	} catch (SQLException e) {
    		Log.e(BaseActivity.class.getName(), "Unable to save event", e);
    		return false;
    	}
    }

	/**
     * Called when the time has been set 
     */
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// Save the new time in the calendar
		if (m_date == null){
			m_date = Calendar.getInstance();
			m_date.clear();        
		}
		
		m_date.set(Calendar.HOUR_OF_DAY, hourOfDay);		
		m_date.set(Calendar.MINUTE, minute);
        
		// Update the display
        m_timeView.setText(m_timeFormat.format(m_date.getTime()));
    }
	
    /**
     * Called when the Save menu item is pressed
     */
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	boolean warning = true;
    	String purpose;
    	
	    switch (item.getItemId()) {
	    	case android.R.id.home:
	            // app icon in action bar clicked; goto parent activity.
	            this.finish();
            return true;
            
	        case R.id.menu_save:
	        	String notes = m_noteView.getText().toString();	
	        	
				try {
					if (m_timeView != null) {
						m_date.setTime(m_combinedFormat.parse(m_dateView.getText().toString() + " " + m_timeView.getText().toString()));
					} else {
						m_date.setTime(m_dateFormat.parse(m_dateView.getText().toString()));
					}
				} catch (ParseException e) {
					m_date = null;
					Log.e(BaseActivity.class.getName(), "Unable to parse date", e);
				}
	        	
				switch(m_eventType) {
		        case DIARY_ENTRY:
		        	m_warning = DIARY_WARNING;
		        	
		        	if (m_date != null) {
		        		String audioFileName = m_event.getAudioFile();
			        	if ((notes != null && (notes.length() != 0))){ 
			        		warning = false;
			        	} else {
			        		if (m_currentPhotoPath != null && (!m_currentPhotoPath.equalsIgnoreCase(DELETED)) 
		        				&& (m_currentPhotoPath.length() != 0)) {
			        			notes = BLANK_DIARY;
			        			warning = false;
			        		}
			        		
			        		if (audioFileName != null && audioFileName.length() != 0) {
			        			notes = BLANK_DIARY;
			        			warning = false;
			        		}
			        	}	
		        	}
		        	
		        	break;
		        case APPOINTMENT:
		        	m_warning = APPT_WARNING;
		        	
		        	if (m_date != null) warning = false;
		        	
		        	if (m_purposeView != null && (m_purposeView.getText().toString().length() != 0)) {
		        		purpose = m_purposeView.getText().toString();
		        	} else {
		        		purpose = BLANK_APPT;
		        	}
		        	
		        	m_event.setPurpose(purpose);
		        	
	        		if (m_doctorView != null && (m_doctorView.getText().toString().length() != 0)) 
	        			m_event.setDoctor(m_doctorView.getText().toString());
	        		if (m_addressView != null && (m_addressView.getText().toString().length() != 0)) 
	        			m_event.setAddress(m_addressView.getText().toString());
	        		
		        	break;
		        case TEST_RESULT:
		        	m_warning = TEST_WARNING;
		        	
		        	if (m_date != null) {
			        	if (notes != null && (notes.length() != 0)) { 
			        		warning = false;
			        	} else {
			        		if (m_currentPhotoPath != null && (!m_currentPhotoPath.equalsIgnoreCase(DELETED)) 
		        				&& (m_currentPhotoPath.length() != 0)) {
			        			notes = BLANK_TEST;
			        			warning = false;
			        		}
			        	}	
		        	}
		        	
		        	break;	
		        }
				
				if (warning) {
					// Remind user to enter information
		        	m_warnView.setText(m_warning);
				} else {
		        	if (notes != null && (notes.length() != 0)) m_event.setText(notes);
		        	if (m_date != null) m_event.setDate(m_date.getTime());
		        	
		        	if (SaveEvent()) { 
		        		m_result = Activity.RESULT_OK;
		        		closeActivity();
		    		}
				}
				
	            return true;
	        
	        default:	        	
	            return super.onOptionsItemSelected(item);
	    }
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.save, menu);
	    return true;
	}	
    
    protected void closeActivity() {
    	// When finished, create an Intent, adding extra:
        // - MainActivity.MainActivity.REQUEST_MODE with the mode
        // - MainActivity.RESULT_KEY_DIARY_ENTRY containing the primary key
        // Then call setResult(code, Intent) and finish();
        // For now let's just use Activity.RESULT_OK for the return code
        // Just doing it here for temporary testing
		
		// return to the main timeline
		setResult(m_result, getIntent());
        finish();	
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

	public TextView get_warnView() {
		return m_warnView;
	}

	public void set_warnView(TextView warnView) {
		this.m_warnView = warnView;
	}

	public String get_warning() {
		return m_warning;
	}

	public void set_warning(String warning) {
		this.m_warning = warning;
	}

	public EditText get_dateView() {
		return m_dateView;
	}

	public void set_dateView(EditText dateView) {
		this.m_dateView = dateView;
	}

	public EditText get_noteView() {
		return m_noteView;
	}

	public void set_noteView(EditText noteView) {
		this.m_noteView = noteView;
	}
	
	public EditText get_purposeView() {
		return m_purposeView;
	}

	public void set_purposeView(EditText purposeView) {
		this.m_purposeView = purposeView;
	}
	
	public EditText get_timeView() {
		return m_timeView;
	}

	public void set_timeView(EditText timeView) {
		this.m_timeView = timeView;
	}
	
	public EditText get_addressView() {
		return m_addressView;
	}

	public void set_addressView(EditText addressView) {
		this.m_addressView = addressView;
	}
	
	public EditText get_doctorView() {
		return m_doctorView;
	}

	public void set_doctorView(EditText doctorView) {
		this.m_doctorView = doctorView;
	}
	
	public ImageView get_photoView() {
		return m_photoView;
	}

	public void set_photoView(ImageView photoView) {
		this.m_photoView = photoView;
	}

	public ImageButton get_buttonDelete() {
		return m_buttonDelete;
	}

	public void set_buttonDelete(ImageButton buttonDelete) {
		this.m_buttonDelete = buttonDelete;
	}

	public ImageButton get_buttonCreate() {
		return m_buttonCreate;
	}

	public void set_buttonCreate(ImageButton buttonCreate) {
		this.m_buttonCreate = buttonCreate;
	}

	public TextView get_photoText() {
		return m_photoText;
	}

	public void set_photoText(TextView photoText) {
		this.m_photoText = photoText;
	}
	
	
}
