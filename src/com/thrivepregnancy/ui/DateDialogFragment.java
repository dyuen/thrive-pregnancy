package com.thrivepregnancy.ui;

import java.util.Calendar;
import java.util.Date;

import com.thrivepregnancy.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
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
	private int					title;

	public static DateDialogFragment newInstance(String fragmentNumber, OnDateSetListener dateListener, int title, 
	long earliestDate, long latestDate, long defaultDate) {
		DateDialogFragment newInstance = new DateDialogFragment();
		newInstance.dateListener = dateListener;
		newInstance.title = title;
		
		Bundle args = new Bundle();
		args.putString("fragnum", fragmentNumber);
		args.putLong("earliest", earliestDate);
		args.putLong("latest", latestDate);
		args.putLong("default", defaultDate);
		args.putInt("title", title);
		newInstance.setArguments(args);
		
		return newInstance;
	}

	@Override
	public AlertDialog onCreateDialog(Bundle savedInstanceState) {
		Activity parentActivity = getActivity();	
		datePicker = (DatePicker)parentActivity.getLayoutInflater().inflate(R.layout.date_picker, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
		
		builder.setTitle(getString(title));		
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
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		Bundle args = getArguments();
		long earliest = args.getLong("earliest");
		long latest = args.getLong("latest");
		long defaultDate = args.getLong("default");
		if (earliest != -1){
			datePicker.setMinDate(earliest);
		}
		if (latest != -1){
			datePicker.setMaxDate(latest);
		}
		if (defaultDate != -1){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(defaultDate);
			datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		}

		
	}
}
