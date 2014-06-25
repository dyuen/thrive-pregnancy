package com.thrivepregnancy.ui;

import com.thrivepregnancy.R;

import java.util.List;

import com.thrivepregnancy.data.Event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
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
	private Resources 			appResources;
	private TimelineFragment 	fragment;
	private MainActivity 		activity;
	private ImageButton 				apptButton;
	private ImageButton 				entryButton;
	/**
	 * Empty public constructor required per the {@link Fragment} API documentation
	 */
	public TimelineFragment(){
		fragment = this;
		Log.d(MainActivity.DEBUG_TAG, "---new TimelineFragment instance");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(MainActivity.DEBUG_TAG, "---onCreateView");
	    // Inflate the layout for this fragment
		fragmentView = inflater.inflate(R.layout.fragment_timeline, container, false);
		return fragmentView;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		activity = (MainActivity)getActivity();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(MainActivity.DEBUG_TAG, "---onResume");
		// Get the list of all timeline Events
		List<Event> events = activity.getTimelineList();
		
		// Determine screen width and current orientation
        WindowManager windowManager = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
        appResources = activity.getResources();
		Display display = windowManager.getDefaultDisplay();
		int orientation = display.getOrientation();
		DisplayMetrics displaymetrics = new DisplayMetrics();
		display.getMetrics(displaymetrics);
		int screenWidth = displaymetrics.widthPixels;
		
		// Create and set the adapter with this list
		TimelineListAdapter adapter = new TimelineListAdapter(getView().getContext(), events, screenWidth, orientation);		
		ListView listView = (ListView)getActivity().findViewById(R.id.lstTimeline);
		listView.setAdapter(adapter);
		
		//Sev - this check causes apptButton and entryButton to stop responding after scrolling back and forth between other sections
		//if (apptButton == null){
			apptButton = (ImageButton)fragmentView.findViewById(R.id.btnAppt);
			apptButton.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(activity.getApplicationContext(), AppointmentActivity.class);
					intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_NEW);	        	
					fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);
				}
			});
		//}
		//if (entryButton == null){
			entryButton = (ImageButton)fragmentView.findViewById(R.id.btnNote);
			entryButton.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(activity.getApplicationContext(), DiaryEntryActivity.class);
					intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_NEW);	        	
					fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);
				}
			});		
		//}
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
			
		}
	}

	private class TimelineListAdapter extends BaseAdapter{
		private final Context context;
		private final List<Event> events;
		private final int orientation;
		private final int screenWidth;
		
		public TimelineListAdapter(Context context, List<Event> events, int screenWidth, int orientation) {
			this.context = context;
			this.events = events;
			this.orientation = orientation;
			this.screenWidth = screenWidth;
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
						if (view != null){
							Log.d(MainActivity.DEBUG_TAG, "Replacing view; old type = " + view.getTag() + "new type = TIP");
						}
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
						if (view != null){
							Log.d(MainActivity.DEBUG_TAG, "Replacing view; old type = " + view.getTag() + "new type = APPOINTMENT");
						}
						view = LayoutInflater.from(context).inflate(R.layout.list_item_appointment, parent, false);
					}						
					TextView purpose = (TextView)view.findViewById(R.id.list_item_appt_purpose);
					TextView dateTime = (TextView)view.findViewById(R.id.list_item_appt_time);
					photoView = (ImageView)view.findViewById(R.id.list_item_tip_photo);
					
					purpose.setText(event.getDoctor());
					dateTime.setText(event.getDate().toString());
					break;
					
				case DIARY_ENTRY:
					if(view == null  || !view.getTag().equals(Event.Type.DIARY_ENTRY.name())) {
						if (view != null){
							Log.d(MainActivity.DEBUG_TAG, "Replacing view; old type = " + view.getTag() + "new type = DIARY_ENTRY");
						}
						view = LayoutInflater.from(context).inflate(R.layout.list_item_entry, parent, false);
					}						
					TextView notes = (TextView)view.findViewById(R.id.list_item_entry_notes);
					date = (TextView)view.findViewById(R.id.list_item_entry_date);
					photoView = (ImageView)view.findViewById(R.id.list_item_entry_photo);
					
					//sev code for testing appointment edit
					ImageButton editButton = (ImageButton)view.findViewById(R.id.list_item_entry_edit);
					editButton.setOnClickListener(new View.OnClickListener() {			
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(activity.getApplicationContext(), DiaryEntryActivity.class);
							intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_EDIT);
							intent.putExtra(MainActivity.REQUEST_PRIMARY_KEY, event.getId());	
							fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);
						}
					});
					//end sev code
					
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
		
		public int getItemViewType (int position){
			Event event = events.get(position);
			return determineItemLayout(event);
		}
		
		private int determineItemLayout(Event event){
			if (event.getType().equals(Event.Type.APPOINTMENT)){
				return R.layout.list_item_appointment;
			}
			else {
				return R.layout.list_item_entry;
			}			
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return events.size();
		}

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
	}
}
