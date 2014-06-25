package com.thrivepregnancy.ui;

import java.util.Calendar;
import java.util.Date;

import com.thrivepregnancy.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.view.View;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

/**
 * A Fragment encapsulating a AlertDialog containing a DatePicker. 
 */
public class DateDialogFragment extends DialogFragment{

	private DatePicker 			datePicker;
	private OnDateSetListener 	dateListener;
	private AlertDialog			dialog;
	
	public static DateDialogFragment newInstance(String fragmentNumber, OnDateSetListener dateListener) {
		DateDialogFragment newInstance = new DateDialogFragment();
		newInstance.dateListener = dateListener;
		Bundle args = new Bundle();
		args.putString("fragnum", fragmentNumber);
		newInstance.setArguments(args);
		return newInstance;
	}

	@Override
	public AlertDialog onCreateDialog(Bundle savedInstanceState) {
		
		Activity parentActivity = getActivity();	
		datePicker = (DatePicker)parentActivity.getLayoutInflater().inflate(R.layout.date_picker, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
		builder.setTitle("Select a date");		
		builder.setView(datePicker);
		// Pass the selected date back to the activity only when Save is clicked
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dateListener.onDateSet(datePicker, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());				
			}
		});
		
		return builder.create();
	}
	
	private void configurePickerDates(){
		Calendar now = Calendar.getInstance(); 
		datePicker.setMinDate((now.getTime()).getTime());
		now.add(Calendar.MONTH, 9);
		datePicker.setMaxDate((now.getTime()).getTime());
	}
	
}
