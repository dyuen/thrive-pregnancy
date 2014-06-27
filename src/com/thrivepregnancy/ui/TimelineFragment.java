package com.thrivepregnancy.ui;

import com.j256.ormlite.dao.Dao;
import com.thrivepregnancy.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
	
	private View 				fragmentView;
	private TimelineFragment 	fragment;
	private MainActivity 		activity;
	private ImageButton 		apptButton;
	private ImageButton 		entryButton;
	private TimelineListAdapter adapter;
	private List<Event> 		events;
	private DatabaseHelper		databaseHelper;
	private EventDataHelper		eventDataHelper;
	private boolean				firstDisplay;
	private	MediaPlayer 		mediaPlayer;
	private ListView 			listView;
	private int					firstTipWeek;
	
	/**
	 * Empty public constructor required per the {@link Fragment} API documentation
	 */
	public TimelineFragment(){
		fragment = this;
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
		firstDisplay = true;
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
		
		// Determine screen width and current orientation
        WindowManager windowManager = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		int orientation = display.getOrientation();
		DisplayMetrics displaymetrics = new DisplayMetrics();
		display.getMetrics(displaymetrics);
		int screenWidth = displaymetrics.widthPixels;
		
		// Create and set the adapter with this list
		List<Event> events = eventDataHelper.getTimelineEvents();
		HashMap<Event, String> weekMap = new HashMap<Event, String>();
		int	tipCount = 0;
		String week = activity.getString(R.string.week) + " ";
		for (Event event: events){
			String weekText = week + String.valueOf(firstTipWeek + tipCount++);
			weekMap.put(event, weekText);
		}
		adapter = new TimelineListAdapter(getView().getContext(), events, weekMap, screenWidth, orientation);		
		listView = (ListView)getActivity().findViewById(R.id.lstTimeline);
		listView.setAdapter(adapter);
		if (firstDisplay){
			firstDisplay = false;
			adapter.scrollToThisWeek(listView);
		}
		
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
			events = eventDataHelper.getTimelineEvents();
			adapter.notifyDataSetChanged();
			adapter.scrollToThisWeek(listView);
		}
	}

	private class TimelineListAdapter extends BaseAdapter{
		private final Context 	context;
		private List<Event> 	events;
		HashMap<Event, String> 	weekMap;
		
		public TimelineListAdapter(Context context, List<Event>	events, HashMap<Event, String> weekMap, int screenWidth, int orientation) {
			this.context = context;
			this.events = events;
			this.weekMap = weekMap;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ImageView 	photoView = null;
			Uri 		uri = null;
			Bitmap 		bitmap = null;
			
			final Event event = events.get(position);
			String photoFile = event.getPhotoFile();
			if (photoFile != null && photoFile.length() > 0){
				if (event.getType().equals(Event.Type.TIP)){
					uri = Uri.parse("content://com.thrivepregnancy.assetcontentprovider/" + photoFile);
				}
				else {					
					File file = new File(photoFile);
					bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());  
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
					TextView purpose = (TextView)view.findViewById(R.id.list_item_appt_purpose);
					TextView dateTime = (TextView)view.findViewById(R.id.list_item_appt_time);
					photoView = (ImageView)view.findViewById(R.id.list_item_appt_photo);
					
					purpose.setText(event.getPurpose());
					dateTime.setText(appointmentDateFormat.format(event.getDate()));
					
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
					
					ImageButton deleteButton = (ImageButton)view.findViewById(R.id.list_item_entry_delete);
					deleteButton.setOnClickListener(new OnClickListener() {	
						// TODO: Confirmation popup dialog
						@Override
						public void onClick(View v) {
							try {
								Dao<Event, Integer> eventDao = activity.getHelper().getDao(Event.class);
								eventDao.delete(event);
								events = eventDataHelper.getTimelineEvents();
								adapter.notifyDataSetChanged();
							}
							catch (SQLException e){
								Log.e(MainActivity.DEBUG_TAG, "Can't delete event", e);
							}
						}
					});					
					
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
			
			if (uri != null){
				photoView.setImageURI(uri);
			}
			else if (bitmap != null){
				photoView.setImageBitmap(bitmap);
			}
			return view;
		}
		
		private void playAudio(String audioFilePath){
    		mediaPlayer = new MediaPlayer ();
    		try {
    			URL url;
    			mediaPlayer.setDataSource(audioFilePath);
//    			mediaPlayer.setDataSource(urlString);
    			mediaPlayer.setOnCompletionListener(fragment);
    			mediaPlayer.setOnErrorListener(fragment);
    			mediaPlayer.setOnPreparedListener(fragment);
    			mediaPlayer.prepareAsync();
    		}
    		catch (IOException e){
    			Log.e(MainActivity.DEBUG_TAG, "Error preparing audio playback", e);
    		}
			
		}
		
		public void scrollToThisWeek(ListView listView){
			int position = 0;
			Date now = new Date();
			for (Event event: events){
				if (event.getType().equals(Event.Type.TIP)){
					if (now.before(event.getDate())){						
						listView.post(new Scroller(position, listView));
						break;
					}
				}
				position++;
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
