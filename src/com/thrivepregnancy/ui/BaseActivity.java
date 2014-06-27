package com.thrivepregnancy.ui;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
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
	
	private SimpleDateFormat m_dateFormat;
	private SimpleDateFormat m_timeFormat;
	private SimpleDateFormat m_combinedFormat;
	
	private Integer m_result = 0;
	private OnDateSetListener m_dateListener;
	private OnTimeSetListener m_timeListener;
	private Calendar m_date;
	
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
	
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final String IMAGE_PATH = "imagePath";
	private static final String DELETED = "deleted";
	
	 /**
     * Setup global variables at startup
     */
	protected void StartUp(Event.Type type) {
        // Mode will be one of MainActivity.REQUEST_MODE_NEW or MainActivity.REQUEST_MODE_EDIT
        // If mode is edit, then REQUEST_PRIMARY_KEY will contain the primary key of the Event
        m_mode = getIntent().getStringExtra(MainActivity.REQUEST_MODE);
        m_eventType = type;
        
        m_eventDao = getHelper().getEventDao();
        
        m_date = Calendar.getInstance();
        
        m_dateFormat = new SimpleDateFormat("MMMMMMMMM d, yyyy", Locale.CANADA);
        m_timeFormat = new SimpleDateFormat("hh:mm a", Locale.CANADA);
        m_combinedFormat = new SimpleDateFormat("MMMMMMMMM d, yyyy hh:mm a", Locale.CANADA);
        
        m_dateListener = this;
        m_timeListener = this;
        
        CreateEvent();
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
    	Log.d("m_mode: ", m_mode);
    	
    	//Edit existing event
        if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_EDIT)) {
        	try {
        		m_event = m_eventDao.queryForId(getIntent().getIntExtra(MainActivity.REQUEST_PRIMARY_KEY,0));
        	} catch (SQLException e) {
    			Log.e(BaseActivity.class.getName(), "Unable to query event", e);
    		}
        	
        //create new event
        } else if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_NEW)) {
        	m_event = new Event();
        	m_event.setDate(m_date.getTime());
        	m_event.setType(m_eventType);
        } 
    }
    
    /**
     * Fills views object from events
     */
    protected void FillViews(LinearLayout layout) {
    	m_layout = layout;
    	
    	if (m_dateView != null) {
	    	m_dateView.setOnClickListener(new OnClickListener() {        
	            public void onClick(View v) {
	              	DateDialogFragment fragment = DateDialogFragment.newInstance("1", m_dateListener);
	               	fragment.show(getSupportFragmentManager(), "1");
	            }
	        });
	    	
	    	String strDate;
	    	
	    	strDate = m_dateFormat.format(m_event.getDate());
	    	
	        m_dateView.setText(strDate);
    	}
    	
    	if (m_timeView != null) {
    		m_timeView.setOnClickListener(new OnClickListener() {        
	            public void onClick(View v) {
	              	TimeDialogFragment fragment = TimeDialogFragment.newInstance("1", m_timeListener);
	               	fragment.show(getSupportFragmentManager(), "1");
	            }
	        });
	    	
	    	String strDate;
	    	
	    	strDate = m_timeFormat.format(m_event.getDate());
	    	
	        m_timeView.setText(strDate);
		}

    	if (m_noteView != null) {
    		if (m_event.getText() != null && (m_event.getText().length() > 0)) m_noteView.setText(m_event.getText());
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
     * Called when the date has been set 
     */
	public void onDateSet(DatePicker view, int year, int month, int day) {
		// Save the new date in the calendar
		if (m_date == null){
			m_date = Calendar.getInstance();
			m_date.clear();        
		}
		
		m_date.set(Calendar.YEAR, year);		
		m_date.set(Calendar.MONTH, month);
		m_date.set(Calendar.DAY_OF_MONTH, day);
        
		// Update the display
        m_dateView.setText(m_dateFormat.format(m_date.getTime()));
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
	    switch (item.getItemId()) {
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
	        	
	        	if (notes != null && (notes.length() != 0) && m_date != null){
	        		// Save notes and date
	        		m_event.setDate(m_date.getTime());
	        		m_event.setText(notes);
	        		
	        		if (m_purposeView != null && (m_purposeView.getText().toString().length() != 0)) 
	        			m_event.setPurpose(m_purposeView.getText().toString());
	        		if (m_doctorView != null && (m_doctorView.getText().toString().length() != 0)) 
	        			m_event.setDoctor(m_doctorView.getText().toString());
	        		if (m_addressView != null && (m_addressView.getText().toString().length() != 0)) 
	        			m_event.setAddress(m_addressView.getText().toString());
	        		
	        		if (SaveEvent()) m_result = Activity.RESULT_OK;
	        		closeActivity();
	    		}
	        	else {
	        		// Remind user to enter information
		        	m_warnView.setText(m_warning);
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
