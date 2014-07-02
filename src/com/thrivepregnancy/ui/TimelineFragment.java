package com.thrivepregnancy.ui;

import com.j256.ormlite.dao.Dao;
import com.thrivepregnancy.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.thrivepregnancy.data.DatabaseHelper;
import com.thrivepregnancy.data.Event;
import com.thrivepregnancy.data.EventDataHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Implements the "My Timeline" fragment in the {@link MainActivity} page
 */
public class TimelineFragment extends Fragment implements OnCompletionListener, OnErrorListener, OnPreparedListener{

	private static SimpleDateFormat diaryEntryFormat = new SimpleDateFormat("MMMMMMMMM d");
	private static SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMMMMMM");
	private static SimpleDateFormat appointmentDateFormat = new SimpleDateFormat("EEEEEEEE MMMMMMMMM d, hh:mm aaa");
	
	private static enum RefreshType {ON_NEW_OR_EDIT, ON_DELETE}
	
	private View 				fragmentView;
	private TimelineFragment 	fragment;
	private MainActivity 		activity;
	private ImageButton 		apptButton;
	private ImageButton 		entryButton;
	private TimelineListAdapter adapter;
	private DatabaseHelper		databaseHelper;
	private EventDataHelper		eventDataHelper;
	private	MediaPlayer 		mediaPlayer;
	private ListView 			listView;
	private int					firstTipWeek;
	
	/**
	 * Empty public constructor required per the {@link Fragment} API documentation
	 */
	public TimelineFragment(){
		fragment = this;
	}
	
	/**
	 * Scales image to fix out of memory crash
	 */
	public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
 
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
 
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
 
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        return bmp;
        }
 
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
 
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
 
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
             }
         }
         return inSampleSize;
      }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    // Inflate the layout for this fragment
		fragmentView = inflater.inflate(R.layout.fragment_timeline, container, false);
		return fragmentView;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		activity = (MainActivity)getActivity();
		databaseHelper = activity.getHelper();
		eventDataHelper = new EventDataHelper(databaseHelper);
    	SharedPreferences preferences = activity.getSharedPreferences(StartupActivity.PREFERENCES, Activity.MODE_PRIVATE);
    	firstTipWeek = preferences.getInt(StartupActivity.PREFERENCE_FIRST_WEEK, 0);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated( savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(MainActivity.DEBUG_TAG, "---onResume");
		
		adapter = new TimelineListAdapter(getView().getContext());		
		listView = (ListView)getActivity().findViewById(R.id.lstTimeline);
		listView.setAdapter(adapter);
		adapter.scrollToThisWeek(listView);
		
		apptButton = (ImageButton)fragmentView.findViewById(R.id.btnAppt);
		apptButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity.getApplicationContext(), AppointmentActivity.class);
				intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_NEW);	        	
				fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);
			}
		});
		entryButton = (ImageButton)fragmentView.findViewById(R.id.btnNote);
		entryButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity.getApplicationContext(), DiaryEntryActivity.class);
				intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_NEW);	        	
				fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_DIARY_ENTRY);
			}
		});
	}	

	/**
	 * Handles the result from Appointment, TestResult or DiaryEntry activity
	 */
	@Override
	public void onActivityResult(int requestCodeIgnored, int resultCode, Intent intent){
		if (intent == null){
			// Return from called activity by pressing back button
			return;
		}
		else {
			adapter.refresh(RefreshType.ON_NEW_OR_EDIT);
		}
	}	

	private class TimelineListAdapter extends BaseAdapter{
		private final Context 	context;
		private List<Event> 	events;
		HashMap<Event, String> 	weekMap;
		
		public TimelineListAdapter(Context context) {
			this.context = context;
			events = eventDataHelper.getTimelineEvents();
			createWeekMap();
		}
		
		// Maps Tip events to the to week number strings
		private void createWeekMap(){
			weekMap = new HashMap<Event, String>();
			int	tipCount = 0;
			String week = activity.getString(R.string.week) + " ";
			for (Event event: events){
				if (event.getType().equals(Event.Type.TIP)){				
					String weekText = week + String.valueOf(firstTipWeek + 1 + tipCount++);
					weekMap.put(event, weekText);
				}
			}			
		}
		
		// Regenerate the event list and wek map, refresh the display
		private void refresh(RefreshType refreshType){
			Log.d(MainActivity.DEBUG_TAG, "---Refreshing "+ refreshType.name());
			events = eventDataHelper.getTimelineEvents();
			createWeekMap();
			notifyDataSetChanged();
			if (refreshType.equals(RefreshType.ON_NEW_OR_EDIT)){
				scrollToThisWeek(listView);
			}
		}

		// Scroll to the Tip event at beginning of current week
		public void scrollToThisWeek(ListView listView){
			Log.d(MainActivity.DEBUG_TAG, "------Scrolling to this week");
			int position = 0;
			int nonTipEventsDuringWeek = 0;
			Date now = new Date();
			for (Event event: events){
				if (now.before(event.getDate())){						
					listView.post(new Scroller(position - nonTipEventsDuringWeek - 1, listView));
					break;
				}
				if (event.getType().equals(Event.Type.TIP)){
					nonTipEventsDuringWeek = 0;
				}
				else {
					nonTipEventsDuringWeek++;
				}
				position++;
			}
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ImageView 	photoView = null;
			Bitmap 		bitmap = null;
			
			final Event event = events.get(position);
			
			String photoFile = event.getPhotoFile();
			
			Log.d("position:", Integer.toString(position));
			Log.d("event.getId()",  Integer.toString(event.getId()));
			
			if (photoFile != null && photoFile.length() > 0){
				Log.d("photoFile",  photoFile);
				
				if (event.getType().equals(Event.Type.TIP)){
					AssetManager assetManager = getActivity().getAssets();
		            
					try {
						InputStream inputS = assetManager.open(photoFile);
						bitmap = BitmapFactory.decodeStream(inputS);
					} catch (IOException e) {
						Log.e(MainActivity.DEBUG_TAG, "Can't load image from assets", e);
					}
				}
				else {					
					File file = new File(photoFile);

					//bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());  
					bitmap = decodeSampledBitmapFromPath(file.getAbsolutePath(),200,200);
					
				}
			}	
			
			switch (event.getType()){
			
				case TIP:
					if(view == null || !view.getTag().equals(Event.Type.TIP.name())) {
						view = LayoutInflater.from(context).inflate(R.layout.list_item_tip, parent, false);
					}						
					TextView text = (TextView)view.findViewById(R.id.list_item_tip_text);
					TextView week = (TextView)view.findViewById(R.id.list_item_tip_week);
					TextView range = (TextView)view.findViewById(R.id.list_item_tip_range);
					photoView = (ImageView)view.findViewById(R.id.list_item_tip_photo);
					
					text.setText(event.getText());
					
					Calendar calendar = Calendar.getInstance();					
					calendar.setTime(event.getDate());
					
					String startDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
					String startMonth = monthFormat.format(calendar.getTime()); 
					calendar.add(Calendar.DAY_OF_MONTH, 6);
					String endDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
					String endMonth = monthFormat.format(calendar.getTime()); 
					
					week.setText(weekMap.get(event));
					if (startMonth.equals(endMonth)){
						range.setText(startMonth + " " + startDay + "-" + endDay);
					}
					else {
						range.setText(startMonth + " " + startDay + " - " + endMonth + " " + endDay);
					}
					break;
			
				case APPOINTMENT:
					if(view == null || !view.getTag().equals(Event.Type.APPOINTMENT.name())) {
						view = LayoutInflater.from(context).inflate(R.layout.list_item_appointment_timeline, parent, false);
					}						
					((TextView)view.findViewById(R.id.list_item_appt_purpose)).setText(event.getPurpose());
					((TextView)view.findViewById(R.id.list_item_appt_time)).setText(appointmentDateFormat.format(event.getDate()));
					((TextView)view.findViewById(R.id.list_item_appt_address)).setText(event.getAddress());
					((TextView)view.findViewById(R.id.list_item_appt_doctor)).setText(event.getDoctor());
					((TextView)view.findViewById(R.id.list_item_appt_notes)).setText(event.getText());
					photoView = (ImageView)view.findViewById(R.id.list_item_appt_photo);
					
					ImageButton editButtonA = (ImageButton)view.findViewById(R.id.list_item_appt_edit);
					editButtonA.setOnClickListener(new OnClickListener() {			
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(activity.getApplicationContext(), AppointmentActivity.class);
							intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_EDIT);
							intent.putExtra(MainActivity.REQUEST_PRIMARY_KEY, event.getId());	
							fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);
						}
					});
					ImageButton deleteAppointment = (ImageButton)view.findViewById(R.id.list_item_appt_delete);
					deleteAppointment.setOnClickListener(new DeleteEventListener(event));
					break;
					
				case DIARY_ENTRY:
					
					if(view == null  || !view.getTag().equals(Event.Type.DIARY_ENTRY.name())) {
						view = LayoutInflater.from(context).inflate(R.layout.list_item_entry, parent, false);
					}						
					TextView notes = (TextView)view.findViewById(R.id.list_item_entry_notes);
					TextView date = (TextView)view.findViewById(R.id.list_item_entry_date);
					photoView = (ImageView)view.findViewById(R.id.list_item_entry_photo);
					ImageButton editButton = (ImageButton)view.findViewById(R.id.list_item_entry_edit);
					editButton.setOnClickListener(new OnClickListener() {			
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(activity.getApplicationContext(), DiaryEntryActivity.class);
							intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_EDIT);
							intent.putExtra(MainActivity.REQUEST_PRIMARY_KEY, event.getId());	
							fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_DIARY_ENTRY);
						}
					});
					
					ImageButton deleteEntry = (ImageButton)view.findViewById(R.id.list_item_entry_delete);
					deleteEntry.setOnClickListener(new DeleteEventListener(event));

					final String audioFile = event.getAudioFile();
					if (audioFile != null && audioFile.length() > 0){
						View audioView = (View)view.findViewById(R.id.list_item_entry_audio);
						audioView.setVisibility(View.VISIBLE);
						ImageButton playButton = (ImageButton)view.findViewById(R.id.list_item_entry_audio_button);
						playButton.setOnClickListener(new View.OnClickListener() {							
							@Override
							public void onClick(View v) {
								playAudio(audioFile);
							}
						});
					}
					
					notes.setText(event.getText());
					date.setText(diaryEntryFormat.format(event.getDate()));
					break;
			}
			
			if (bitmap != null){
				photoView.setVisibility(View.VISIBLE);
				photoView.setImageBitmap(bitmap);
			} else {
				photoView.setVisibility(View.GONE);
			}
			
			return view;
		}
		
		// Listener for delete Diary Entry or delete Appointment buttons
		private class DeleteEventListener implements OnClickListener{
			final Event	event;
			DeleteEventListener(final Event event){
				this.event = event;
			}
			// TODO: Confirmation popup dialog
			@Override
			public void onClick(View v) {
				try {
					Dao<Event, Integer> eventDao = activity.getHelper().getDao(Event.class);
					eventDao.delete(event);
					adapter.refresh(RefreshType.ON_DELETE);
				}
				catch (SQLException e){
					Log.e(MainActivity.DEBUG_TAG, "Can't delete event", e);
				}
			}			
		}
		
		private void playAudio(String audioFilePath){
    		mediaPlayer = new MediaPlayer ();
    		try {
    			URL url;
    			mediaPlayer.setDataSource(audioFilePath);
    			mediaPlayer.setOnCompletionListener(fragment);
    			mediaPlayer.setOnErrorListener(fragment);
    			mediaPlayer.setOnPreparedListener(fragment);
    			mediaPlayer.prepareAsync();
    		}
    		catch (IOException e){
    			Log.e(MainActivity.DEBUG_TAG, "Error preparing audio playback", e);
    		}
			
		}		
		
		/**
		 * Executes scrolling to current week on the UI thread. 
		 */
		private class Scroller implements Runnable{
			private int position;
			private ListView listView;
			Scroller(int position, ListView listView){
				this.position = position;
				this.listView = listView;
			}
			public void run() {
				// Using setSelection rather than smoothScrollToPosition.
				// Latter cannot cope with elements being variable sizes
				listView.setSelection(position);
	        }
		};
		
		//***************************************** From BaseAdapter
		// Get the type of View that will be created by getView(int, View, ViewGroup) for the specified item. (0...
		@Override
		public int getItemViewType (int position){
			Event event = events.get(position);
			switch (event.getType()){
			case TIP: 			return 0;
			case DIARY_ENTRY:	return 1;
			case APPOINTMENT:	return 2;
			default:			return -1;
			}
		}
		
		//Returns the number of types of Views that will be created by getView(int, View, ViewGroup)
		@Override
		public int getViewTypeCount (){
			return 3;
		}
		//Indicates whether the item ids are stable across changes to the underlying data.
		@Override public boolean hasStableIds (){
			return true;
		}
		//***************************************** From adapter
		// How many items are in the data set represented by this Adapter
		@Override
		public int getCount() {
			return events.size();
		}
		// Get the data item associated with the specified position in the data set.
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		//*****************************************
		
		private int determineItemLayout(Event event){
			if (event.getType().equals(Event.Type.APPOINTMENT)){
				return R.layout.list_item_appointment_timeline;
			}
			else {
				return R.layout.list_item_entry;
			}			
		}
	}
	/**
	 * Listener methods for the MediaPlayer
	 */
	public void onCompletion(MediaPlayer mp){
		mediaPlayer.release();
	}
	public boolean onError (MediaPlayer mp, int what, int extra){
		/*
		-1004 MEDIA_ERROR_IO Added in API level 17
		-1007 MEDIA_ERROR_MALFORMED Added in API level 17
  		  200 MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK
  		  100 MEDIA_ERROR_SERVER_DIED
 		 -110 MEDIA_ERROR_TIMED_OUT Added in API level 17
    	    1 MEDIA_ERROR_UNKNOWN
		-1010 MEDIA_ERROR_UNSUPPORTED Added in API level 17
		*/
		Log.e(MainActivity.DEBUG_TAG, "MediaPlayer error = " + what + " : " + extra);
		// Report errors back to PlayerActivity
		mp.release();		
		return true;		
	}
	
	public void onPrepared(MediaPlayer mp){
		mp.start();
	}
}
