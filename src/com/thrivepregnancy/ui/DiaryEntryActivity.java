package com.thrivepregnancy.ui;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.thrivepregnancy.R;
import com.thrivepregnancy.data.Event;

/**
 * ACtivity for creating or editing a DIARY_ENTRY event
 */
public class DiaryEntryActivity extends BaseActivity implements OnDateSetListener {

	private String m_mode;
	private Dao<Event, Integer> m_eventDao;
	private Event m_diaryEvent;
	private EditText m_dateView;
	private EditText m_noteView;
	private SimpleDateFormat m_dateFormat;
	private Integer m_result = 0;
	private OnDateSetListener m_dateListener;
	
	
	Calendar m_date;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diaryentry);

        // Mode will be one of MainActivity.REQUEST_MODE_NEW or MainActivity.REQUEST_MODE_EDIT
        // If mode is edit, then REQUEST_PRIMARY_KEY will contain the primary key of the Event
        m_mode = getIntent().getStringExtra(MainActivity.REQUEST_MODE);
        
        m_eventDao = getHelper().getEventDao();
        
        m_date = Calendar.getInstance();
        m_dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.CANADA);
        m_dateListener = this;
        
    	CreateEvent();
    	FillViews();
    }
    
    /**
     * Fills existing Views from Event object
     */
    private void FillViews() {
    	m_dateView = (EditText)findViewById(R.id.diary_date);
    	m_noteView = (EditText)findViewById(R.id.diary_notes);
    	
    	m_dateView.setOnClickListener(new OnClickListener() {        
            public void onClick(View v) {
              	DateDialogFragment fragment = DateDialogFragment.newInstance("1", m_dateListener);
               	fragment.show(getSupportFragmentManager(), "1");
            }
        });
    	
    	String strDate;
    	
    	strDate = m_dateFormat.format(m_diaryEvent.getDate());
    	
        m_dateView.setText(strDate);
        if (m_diaryEvent.getText() != null && (m_diaryEvent.getText().length() > 0)) m_noteView.setText(m_diaryEvent.getText());
    }
    
    /**
     * Creates Event Diary object either from existing record or new
     */
    private void CreateEvent() {
    	Log.d("m_mode: ", m_mode);
    	
    	//Edit existing event
        if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_EDIT)) {
        	try {
        		m_diaryEvent = m_eventDao.queryForId(Integer.parseInt(MainActivity.REQUEST_PRIMARY_KEY));
        	} catch (SQLException e) {
    			Log.e(DiaryEntryActivity.class.getName(), "Unable to request diary event", e);
    		}
        	
        //create new event
        } else if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_NEW)) {
        	m_diaryEvent = new Event();
        	m_diaryEvent.setDate(m_date.getTime());
        	m_diaryEvent.setType(Event.Type.DIARY_ENTRY);
        }
    }
    
    /**
     * Saves Event Object to database
     */
    private boolean SaveEvent() {
    	try {
	        if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_EDIT)) {
	        	m_eventDao.update(m_diaryEvent);
	        }  else if (m_mode.equalsIgnoreCase(MainActivity.REQUEST_MODE_NEW)) {
	        	m_eventDao.create(m_diaryEvent);
	        }
	        
	        return true;
    	} catch (SQLException e) {
    		Log.e(DiaryEntryActivity.class.getName(), "Unable to save diary event", e);
    		return false;
    	}
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
					m_date.setTime(m_dateFormat.parse(m_dateView.getText().toString()));
				} catch (ParseException e) {
					m_date = null;
					Log.e(DiaryEntryActivity.class.getName(), "Unable to parse date", e);
				}
	        	
	        	if (notes != null && (notes.length() != 0) && m_date != null){
	        		// Save notes and date
	        		m_diaryEvent.setDate(m_date.getTime());
	        		m_diaryEvent.setText(notes);
	        		
	        		if (SaveEvent()) m_result = Activity.RESULT_OK;
	        		closeActivity();
	    		}
	        	else {
	        		// Remind user to enter both notes and date
		        	TextView textView = (TextView)findViewById(R.id.diary_warning);
	        		textView.setText(R.string.Diary_Warning);
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
    
    private void closeActivity() {
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
}
