package com.thrivepregnancy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * ACtivity for creating or editing an APPOINTMENT event
 */
public class AppointmentActivity  extends BaseActivity{

	private String mode;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView dummyView = new TextView(this);
        dummyView.setText("Placeholder for Appointment Create/Edit");
        setContentView(dummyView);

        // Mode will be one of MainActivity.REQUEST_MODE_NEW or MainActivity.REQUEST_MODE_EDIT
        // If mode is edit, then REQUEST_PRIMARY_KEY will contain the primary key of the Event
        Intent intent = getIntent();
        mode = intent.getStringExtra(MainActivity.REQUEST_MODE);
        
        // When finished, create an Intent, adding extra:
        // - MainActivity.MainActivity.REQUEST_MODE with the mode
        // - MainActivity.RESULT_KEY_DIARY_ENTRY containing the primary key
        // Then call setResult(code, Intent) and finish();
        // For now let's just use Activity.RESULT_OK for the return code
        // Just doing it here for temporary testing
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
