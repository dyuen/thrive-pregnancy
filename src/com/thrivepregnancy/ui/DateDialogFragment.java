package com.thrivepregnancy.ui;

import java.util.Calendar;

import com.thrivepregnancy.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.util.Log;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

/**
 * A Fragment encapsulating a AlertDialog containing a DatePicker. Activities using
 * this fragment must implement interface DatePickerDialog.OnDateSetListener
 */
public class DateDialogFragment extends DialogFragment{
	
	/**
	 * Not applicable value for earliest, latest or default date
	 */
	public static final long UNSPECIFIED = -1;
	
	private DatePicker 			datePicker;
	private OnDateSetListener	dateListener;

	public static DateDialogFragment newInstance(String fragmentNumber, int title, long earliestDate, long latestDate, long defaultDate) {
		DateDialogFragment newInstance = new DateDialogFragment();

		Bundle args = new Bundle();
		args.putString("fragnum", fragmentNumber);
		/***** Minimum and maximum date setting removed due to bug in Android Galaxy 3 (API level 15)		
		args.putLong("earliest", earliestDate);
		args.putLong("latest", latestDate);
    	*****/
		args.putLong("earliest", UNSPECIFIED);
		args.putLong("latest", UNSPECIFIED);

		args.putLong("default", defaultDate);
		args.putInt("title", title);
		newInstance.setArguments(args);
		
		return newInstance;
	}
	
	/**
	 * Called when a fragment is first attached to its activity. 
	 * onCreate() will be called after this, then onCreateView(), onActivityCreated()
	 */
	public void onAttach (Activity activity){
		super.onAttach(activity);
		dateListener = (OnDateSetListener)activity;
	}

	@Override
	public AlertDialog onCreateDialog(Bundle savedInstanceState) {
		Activity parentActivity = getActivity();	
		datePicker = (DatePicker)parentActivity.getLayoutInflater().inflate(R.layout.date_picker, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
		
		Bundle args = getArguments();
		builder.setTitle(getString(args.getInt("title")));		
		builder.setView(datePicker);
		// Pass the selected date back to the activity only when Save is clicked
		builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {			
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
		if (earliest != UNSPECIFIED){
			datePicker.setMinDate(earliest);
		}
		if (latest != UNSPECIFIED){
			datePicker.setMaxDate(latest);
		}
		if (defaultDate != UNSPECIFIED){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(defaultDate);
			datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		}
	}
}
