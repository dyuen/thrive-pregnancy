package com.thrivepregnancy.ui;

import com.thrivepregnancy.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

/**
 * A Fragment encapsulating a AlertDialog containing a TimePicker. 
 */
public class TimeDialogFragment extends DialogFragment{

	private TimePicker 			timePicker;
	private OnTimeSetListener 	timeListener;
	
	public static TimeDialogFragment newInstance(String fragmentNumber, OnTimeSetListener timeListener) {
		TimeDialogFragment newInstance = new TimeDialogFragment();
		newInstance.timeListener =timeListener;
		Bundle args = new Bundle();
		args.putString("fragnum", fragmentNumber);
		newInstance.setArguments(args);
		return newInstance;
	}

	@Override
	public AlertDialog onCreateDialog(Bundle savedInstanceState) {
		
		Activity parentActivity = getActivity();	
		timePicker = (TimePicker)parentActivity.getLayoutInflater().inflate(R.layout.time_picker, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
		builder.setTitle("Select time");		
		builder.setView(timePicker);
		// Pass the selected date back to the activity only when Save is clicked
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				timeListener.onTimeSet(timePicker, timePicker.getCurrentHour(), timePicker.getCurrentMinute());				
			}
		});
		
		return builder.create();
	}
}
