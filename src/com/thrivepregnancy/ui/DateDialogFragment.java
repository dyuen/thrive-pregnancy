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
	
	private DatePicker 			datePicker;
	private OnDateSetListener	dateListener;

	public static DateDialogFragment newInstance(String fragmentNumber, int title, long earliestDate, long latestDate, long defaultDate) {
		Log.d(MainActivity.DEBUG_TAG, "DateDialogFragment newInstance");
		DateDialogFragment newInstance = new DateDialogFragment();
//		newInstance.setRetainInstance(true);

		Bundle args = new Bundle();
		args.putString("fragnum", fragmentNumber);
		args.putLong("earliest", earliestDate);
		args.putLong("latest", latestDate);
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
		Log.d(MainActivity.DEBUG_TAG, "DateDialogFragment onAttach");		
		dateListener = (OnDateSetListener)activity;
	}

	@Override
	public AlertDialog onCreateDialog(Bundle savedInstanceState) {
		Log.d(MainActivity.DEBUG_TAG, "DateDialogFragment onCreateDialog");
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
		Log.d(MainActivity.DEBUG_TAG, "DateDialogFragment onActivityCreated");
		
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
