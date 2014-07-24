package com.thrivepregnancy.ui;

import com.thrivepregnancy.R;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.thrivepregnancy.data.DatabaseHelper;
import com.thrivepregnancy.data.Event;
import com.thrivepregnancy.data.EventDataHelper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
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
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Implements the "My Timeline" fragment in the {@link MainActivity} page
 */
public class TimelineFragment extends Fragment implements OnCompletionListener, OnErrorListener, OnPreparedListener, AudioPlayer.PlayerClient{

	private static SimpleDateFormat diaryEntryFormat = new SimpleDateFormat("MMMMMMMMM d", Locale.CANADA);
	private static SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMMMMMM", Locale.CANADA);
	private static SimpleDateFormat appointmentDateFormat = new SimpleDateFormat("EEEEEEEE MMMMMMMMM d, hh:mm aaa", Locale.CANADA);

	public static enum RefreshType {ON_NEW_OR_EDIT, ON_DELETE, ON_TIMELINE_CHANGE}

	private View 				fragmentView;
	private TimelineFragment 	fragment;
	private MainActivity 		mainActivity;
	private ImageButton 		apptButton;
	private ImageButton 		entryButton;
	private TimelineListAdapter adapter;
	private DatabaseHelper		databaseHelper;
	private EventDataHelper		eventDataHelper;
	private ListView 			listView;
	private int					firstTipWeek;
	private static AudioPlayer	activeAudioPlayer;
	private ViewGroup			player;

	/**
	 * Preference index
	 */
	public static final String PREFERENCE_INDEX = "index";
	/**
	 * Preference top
	 */	
	public static final String PREFERENCE_TOP = "top";
			
	/**
	 * Empty public constructor required per the {@link Fragment} API documentation
	 */
	public TimelineFragment(){
		super();
		fragment = this;
	}

	/**
	* creates and returns the view hierarchy associated with the fragment.
	*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    // Inflate the layout for this fragment
		fragmentView = inflater.inflate(R.layout.fragment_timeline, container, false);
		return fragmentView;
	}

	/**
	* called to do initial creation of the fragment.
	*/
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		mainActivity = (MainActivity)getActivity();
		databaseHelper = mainActivity.getHelper();
		eventDataHelper = new EventDataHelper(databaseHelper);
    	SharedPreferences preferences = mainActivity.getSharedPreferences(StartupActivity.PREFERENCES, Context.MODE_PRIVATE);
    	firstTipWeek = preferences.getInt(StartupActivity.PREFERENCE_FIRST_WEEK, 1);
	}
	
	/**
	* tells the fragment that its activity has completed its own Activity.onCreate()
	*/
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated( savedInstanceState);
		adapter = new TimelineListAdapter(getView().getContext());
		mainActivity.setTimelineListAdapter(adapter);
	}
	
	/*  AudioPlayer.PlayerClient methods *****************************/
	public void setActiveAudioPlayer(AudioPlayer audioPlayer){
		activeAudioPlayer = audioPlayer;
	}
	public AudioPlayer getActiveAudioPlayer(){
		return activeAudioPlayer;
	}
	public void playerStarted(){}
	public void playerStopped(){}
	/*****************************************************************/
	
	@Override
    public void onDestroy()
    {
        super.onDestroy();        
        SaveCurrentListPosition();        
    }
	
	@Override
    public void onPause()
    {
        super.onPause();        
        SaveCurrentListPosition();        
    }
	
	/**
	* saves current list position
	*/
	public void SaveCurrentListPosition() {
		// save list current position
        int index = listView.getFirstVisiblePosition();
        View view = listView.getChildAt(0);
        int top = (view == null) ? 0 : view.getTop();
        
        SharedPreferences preferences = getActivity().getSharedPreferences(StartupActivity.PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
    	editor.putInt(PREFERENCE_INDEX, index);
    	editor.putInt(PREFERENCE_TOP, top);
    	editor.commit();
	}
	/**
	* makes the fragment interacting with the user (based on its containing activity being resumed)
	*/
	@Override
	public void onResume() {
		super.onResume();

		listView = (ListView)getActivity().findViewById(R.id.lstTimeline);
		listView.setAdapter(adapter);
		adapter.scrollToThisWeek(listView);

		apptButton = (ImageButton)fragmentView.findViewById(R.id.btnAppt);
		apptButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mainActivity.getApplicationContext(), AppointmentActivity.class);
				intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_NEW);
				fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);
			}
		});
		entryButton = (ImageButton)fragmentView.findViewById(R.id.btnNote);
		entryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mainActivity.getApplicationContext(), DiaryEntryActivity.class);
				intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_NEW);
				fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_DIARY_ENTRY);
			}
		});
	}

	/**
	 * Handles the result from Appointment, TestResult or DiaryEntry activity
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		if (intent == null){
			// Return from called activity by pressing back button
			return;
		}
		else {		
			adapter.refresh(RefreshType.ON_NEW_OR_EDIT);
			if (requestCode == MainActivity.REQUEST_CODE_APPOINTMENT){
				this.mainActivity.getCareFragment().refresh();
			}
		}
	}
	
	void refreshOnTimelineChange(){
		adapter.refresh(RefreshType.ON_TIMELINE_CHANGE);
	}

	public class TimelineListAdapter extends BaseAdapter{
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
			String week = mainActivity.getString(R.string.week) + " ";
			for (Event event: events){
				if (event.getType().equals(Event.Type.TIP)){
					String weekText = week + String.valueOf(firstTipWeek + tipCount++);
					weekMap.put(event, weekText);
				}
			}
		}

		// Regenerate the event list and wek map, refresh the display
		void refresh(RefreshType refreshType){
			events = eventDataHelper.getTimelineEvents();
			createWeekMap();
			notifyDataSetChanged();
			if (refreshType.equals(RefreshType.ON_NEW_OR_EDIT)){
				scrollToThisWeek(listView);
			}
		}

		// Scroll to the Tip event at beginning of current week
		public void scrollToThisWeek(ListView listView) {
			int index = 0;
			int top = 0;
			
			SharedPreferences preferences = getActivity().getSharedPreferences(StartupActivity.PREFERENCES, Context.MODE_PRIVATE);
		
	    	index = preferences.getInt(PREFERENCE_INDEX, 0); // first visible list item = 0 at cold start
	    	top = preferences.getInt(PREFERENCE_TOP, 0); // = 1 at cold start
	    	
	    	if (index ==0 && top == 0) {
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
	    	} else {
	    		listView.setSelectionFromTop(index, top);
	    	}
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ImageView 	photoView = null;
			Bitmap 		bitmap = null;			
			
			final Event event = events.get(position);
			String photoFile = event.getPhotoFile();

			if (photoFile != null && photoFile.length() > 0){
				if (event.getType().equals(Event.Type.TIP)){
					AssetManager assetManager = getActivity().getAssets();

					try {
						InputStream inputS = assetManager.open(photoFile);
						bitmap = BitmapFactory.decodeStream(inputS);
					} catch (IOException e) {
						Log.e(MainActivity.DEBUG_TAG, "Can't load image from assets", e);
					}
				}
			}

			switch (event.getType()){

				case TIP:
					if(view == null || !view.getTag().equals(Event.Type.TIP.name())) {
						view = LayoutInflater.from(context).inflate(R.layout.list_item_tip, parent, false);
					}
					
					view.setEnabled(false);
					view.setOnClickListener(null);
					
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
						range.setText(startMonth + " " + startDay + " - " + endDay);
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

					TableRow addressDiv = (TableRow)view.findViewById(R.id.list_item_appt_address_div);
					TableRow doctorDiv = (TableRow)view.findViewById(R.id.list_item_appt_doctor_div);
					TableRow notesDiv = (TableRow)view.findViewById(R.id.list_item_appt_notes_div);
					
					if (event.getAddress() == null || event.getAddress().length()==0) {
						addressDiv.setVisibility(View.GONE);
					}
					else {
						((TextView)view.findViewById(R.id.list_item_appt_address)).setText(event.getAddress());
					}
					if (event.getDoctor() == null || event.getDoctor().length()==0) {
						doctorDiv.setVisibility(View.GONE);
					}
					else {
						((TextView)view.findViewById(R.id.list_item_appt_doctor)).setText(event.getDoctor());
					}
					if (event.getText() == null || event.getText().length()==0) {
						notesDiv.setVisibility(View.GONE);
					}
					else {
						((TextView)view.findViewById(R.id.list_item_appt_notes)).setText(event.getText());
					}
					
					photoView = (ImageView)view.findViewById(R.id.list_item_appt_photo);

					ImageButton editButtonA = (ImageButton)view.findViewById(R.id.list_item_appt_edit);
					editButtonA.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(mainActivity.getApplicationContext(), AppointmentActivity.class);
							intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_EDIT);
							intent.putExtra(MainActivity.REQUEST_PRIMARY_KEY, event.getId());
							fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);
						}
					});
					ImageButton deleteAppointment = (ImageButton)view.findViewById(R.id.list_item_appt_delete);
					deleteAppointment.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View view){
							mainActivity.showConfirmationDialog(R.string.dlg_delete_appointment, 
									new DeleteConfirmationListener(event), event, MainActivity.DELETE_FROM_TIMELINE);
						}				
					});		
					
					photoView.setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(v.getContext(), PictureActivity.class);
							Bundle parentBundle = new Bundle();
							String imagePath = event.getPhotoFile();
							String activityTitle = "Appointment Photo";
							
							parentBundle.putString("imagepath", imagePath);
							parentBundle.putString("imagetitle", activityTitle);
							
							intent.putExtras(parentBundle);
							startActivity(intent);
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
							// Stop any active audio plaback
							if (activeAudioPlayer != null){
								activeAudioPlayer.stop();
							}
							Intent intent = new Intent(mainActivity.getApplicationContext(), DiaryEntryActivity.class);
							intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_EDIT);
							intent.putExtra(MainActivity.REQUEST_PRIMARY_KEY, event.getId());
							fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_DIARY_ENTRY);
						}
					});

					ImageButton deleteEntry = (ImageButton)view.findViewById(R.id.list_item_entry_delete);
					deleteEntry.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View view){
							mainActivity.showConfirmationDialog(R.string.dlg_delete_diary_entry,
									new DeleteConfirmationListener(event), event, MainActivity.DELETE_FROM_TIMELINE);
						}
					});

					final String audioFile = event.getAudioFile();
					if (audioFile != null && audioFile.length() > 0){
						View audioView = (View)view.findViewById(R.id.list_item_entry_audio);
						ViewGroup audioPlayerGroup = (ViewGroup)audioView.findViewById(R.id.list_item_entry_audio_player);
						boolean isActivelyPlaying = false;
						if (activeAudioPlayer != null){
							if (activeAudioPlayer.isPlaying(audioFile)){
								activeAudioPlayer.restore(mainActivity, audioPlayerGroup);
								isActivelyPlaying = true;
							}
						}
						if (!isActivelyPlaying){
							new AudioPlayer(mainActivity, audioPlayerGroup, event.getAudioFile(), fragment);
						}
						audioView.setVisibility(View.VISIBLE);
					}
					
					photoView.setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(v.getContext(), PictureActivity.class);
							Bundle parentBundle = new Bundle();
							String imagePath = event.getPhotoFile();
							String activityTitle = "Diary Photo";
							
							parentBundle.putString("imagepath", imagePath);
							parentBundle.putString("imagetitle", activityTitle);
							
							intent.putExtras(parentBundle);
							startActivity(intent);
						}
					});

					notes.setText(event.getText());
					
					if (event.getText() == null || event.getText().length()==0) 
						notes.setVisibility(View.GONE);
					
					date.setText(diaryEntryFormat.format(event.getDate()));
					break;
			}
			
			TextView divider = (TextView)view.findViewById(R.id.list_item_tip_divider);
			
			if (bitmap != null){
				if (divider!= null) divider.setVisibility(View.VISIBLE);
				photoView.setVisibility(View.VISIBLE);
				
				photoView.setImageBitmap(bitmap);
			} 
			else {
				if (divider!= null) divider.setVisibility(View.GONE);
				photoView.setVisibility(View.GONE);
				
				if (photoFile != null && photoFile.length() > 0){
					ImageLoader imageloader = new ImageLoader(photoFile,photoView);
					imageloader.loadBitmap();
				}
			}
			return view;
		}

		/**
		 * Executes scrolling to a specified position
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
	public void onCompletion(MediaPlayer mp){}
	
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
	
	/**
	 * Listener class for delete dialog
	 */
    private class DeleteConfirmationListener implements DialogInterface.OnClickListener {
    	private final Event event;
    	DeleteConfirmationListener(final Event event){
    		this.event = event;
    	}
    	@Override
    	public void onClick(DialogInterface dialog, int which){
    		if (which == DialogInterface.BUTTON_POSITIVE){
    			adapter.refresh(RefreshType.ON_DELETE);
    			adapter.notifyDataSetChanged();
    		}
    	}
    }
    
}
