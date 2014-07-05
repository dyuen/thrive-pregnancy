package com.thrivepregnancy.ui;

import com.thrivepregnancy.R;
import com.thrivepregnancy.data.Event;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Activity for creating or editing an APPOINTMENT event
 */
public class AppointmentActivity  extends BaseActivity{

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	LayoutInflater inflater = getLayoutInflater();
    	
    	LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.activity_appointment, null);
    	
    	setContentView(layout);
    	
    	StartUp(Event.Type.APPOINTMENT);
    	
    	SetViews();
    	
    	FillViews(layout);
    }
    
    /**
     * References views from layout
     */
    private void SetViews() {
    	//EditText date =(EditText)findViewById(R.id.appt_date);
    	//date.setKeyListener(null);
    	
    	set_purposeView((EditText)findViewById(R.id.appt_purpose));
    	set_dateView((EditText)findViewById(R.id.appt_date));
    	set_timeView((EditText)findViewById(R.id.appt_time));
    	set_addressView((EditText)findViewById(R.id.appt_address));
    	set_doctorView ((EditText)findViewById(R.id.appt_doctor));
    	set_noteView((EditText)findViewById(R.id.appt_notes));
    	
    	
    	set_warnView((TextView)findViewById(R.id.appt_warning));
    	set_warning(getString(R.string.Appt_Warning));
    	
    	set_buttonDelete((ImageButton)findViewById(R.id.appt_delete));
    	set_buttonCreate((ImageButton)findViewById(R.id.appt_create));
    	
    	set_photoText((TextView)findViewById(R.id.appt_create_text));
    	set_photoView((ImageView)findViewById(R.id.appt_image));
    }
}
