package com.thrivepregnancy.ui;

import com.j256.ormlite.dao.Dao;
import com.thrivepregnancy.R;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.thrivepregnancy.data.DatabaseHelper;
import com.thrivepregnancy.data.Event;
import com.thrivepregnancy.data.EventDataHelper;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class TimelineFragment extends Fragment {

	private View 				fragmentView;
	private TimelineFragment 	fragment;
	private MainActivity 		activity;
	private ImageButton 		apptButton;
	private ImageButton 		entryButton;
	private TimelineListAdapter adapter;
	private List<Event> 		events;
	private DatabaseHelper	databaseHelper;
	private EventDataHelper	eventDataHelper;
	private boolean			firstDisplay;
	
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
		adapter = new TimelineListAdapter(getView().getContext(), screenWidth, orientation);		
		ListView listView = (ListView)getActivity().findViewById(R.id.lstTimeline);
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
		}
	}

	private class TimelineListAdapter extends BaseAdapter{
		private final Context 	context;
		private List<Event> 	events;
		
		public TimelineListAdapter(Context context, int screenWidth, int orientation) {
			this.context = context;
			events = eventDataHelper.getTimelineEvents();
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			TextView 	date;
			ImageView 	photoView = null;
			Uri 		uri = null;
			
			final Event event = events.get(position);
			String photoFile = event.getPhotoFile();
			if (photoFile != null && photoFile.length() > 0){
				uri = Uri.parse("content://com.thrivepregnancy.assetcontentprovider/" + photoFile);
			}
			
			switch (event.getType()){
			
				case TIP:
					if(view == null || !view.getTag().equals(Event.Type.TIP.name())) {
						view = LayoutInflater.from(context).inflate(R.layout.list_item_tip, parent, false);
					}						
					TextView text = (TextView)view.findViewById(R.id.list_item_tip_text);
					date = (TextView)view.findViewById(R.id.list_item_tip_date);
					photoView = (ImageView)view.findViewById(R.id.list_item_tip_photo);
					
					text.setText(event.getText());
					date.setText("(TBR) " + event.getDate().toString());
					break;
			
				case APPOINTMENT:
					if(view == null || !view.getTag().equals(Event.Type.APPOINTMENT.name())) {
						view = LayoutInflater.from(context).inflate(R.layout.list_item_appointment_timeline, parent, false);
					}						
					TextView purpose = (TextView)view.findViewById(R.id.list_item_appt_purpose);
					TextView dateTime = (TextView)view.findViewById(R.id.list_item_appt_time);
					photoView = (ImageView)view.findViewById(R.id.list_item_tip_photo);
					
					purpose.setText(event.getPurpose());
					dateTime.setText(event.getDate().toString());
					break;
					
				case DIARY_ENTRY:
					if(view == null  || !view.getTag().equals(Event.Type.DIARY_ENTRY.name())) {
						view = LayoutInflater.from(context).inflate(R.layout.list_item_entry, parent, false);
					}						
					TextView notes = (TextView)view.findViewById(R.id.list_item_entry_notes);
					date = (TextView)view.findViewById(R.id.list_item_entry_date);
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
					
					notes.setText(event.getText());
					date.setText(event.getDate().toString());
					break;
			}
			if (photoFile != null){
				photoView.setImageURI(uri);
			}
			
			// This works, but the image isn't scaled up to fill available space
			// even though the image width (800) is wider than my screen (768)!!
			// In fact, it looks as if it might have reduced the size by exactly half
			//				uri = Uri.parse("content://com.thrivepregnancy.assetcontentprovider/" + photoFile);
			//				photo.setMaxHeight(1072);
			//				photo.setMaxWidth(1600);
			//				photo.setImageURI(uri);

			// This doesn't work: decodeFileDescriptor will return null, so no image;
			//						AssetManager assetManager = context.getAssets();
			//						AssetFileDescriptor afd = assetManager.openFd("baby.jpg");
			//						FileDescriptor fd = afd.getFileDescriptor();
			//						Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd);
			//						photo.setImageBitmap(bitmap);

			// This executes but gives a FileNotFoundEXception warning (the image will be missing)
			//						Uri uri =  Uri.parse("file:///android_asset/baby.jpg");
			//						photo.setImageURI(uri);

			// This works
			// Using large images (like the original "rosy.jpg") will cause 
			// "java.lang.OutOfMemoryError at android.graphics.BitmapFactory.nativeDecodeAsset (Native Method)"
			//						photo.setImageResource(R.drawable.baby);

			//						Bitmap bitmap = ImageLoader.compressPicture(R.drawable.rosy, appResources, screenWidth);
			//						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			//						FileInputStream fis = context.openFileInput("rosy.jpg");
			//						Picture picture = Picture.createFromStream(fis);
			//						PictureDrawable drawable = new PictureDrawable(picture);
			//						photo.setImageResource(R.drawable.rosy_compressed);
			//						photo.setImageDrawable(drawable);
			return view;
		}
		
		public void scrollToThisWeek(ListView listView){
			int position = 0;
			Date now = new Date();
			for (Event event: events){
				if (event.getType().equals(Event.Type.TIP)){
					if (now.after(event.getDate())){
						listView.smoothScrollToPosition(position);
					}
				}
				position++;
			}
		}
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
}
