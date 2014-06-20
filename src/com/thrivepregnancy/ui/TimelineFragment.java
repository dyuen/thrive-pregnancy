package com.thrivepregnancy.ui;

import com.thrivepregnancy.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.thrivepregnancy.R;
import com.thrivepregnancy.R.layout;
import com.thrivepregnancy.data.Event;
import com.thrivepregnancy.data.Event.Type;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Implements the "My Timeline" fragment in the {@link MainActivity} page
 */
public class TimelineFragment extends Fragment {

	private View 				fragmentView;
	private Resources 			appResources;
	private TimelineFragment 	theFragment;
	/**
	 * Empty public constructor required per the {@link Fragment} API documentation
	 */
	public TimelineFragment(){
		theFragment = this;
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
	public void onResume() {
		super.onResume();
		Log.d(MainActivity.DEBUG_TAG, "---onResume");
		// Get the list of all timeline Events
		MainActivity activity = (MainActivity)getActivity();		
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
			Event event = events.get(position);
			
			switch (event.getType()){
			
				case APPOINTMENT:
					if(view == null) {
						view = LayoutInflater.from(context).inflate(R.layout.list_item_appointment, parent, false);
					}
						
					TextView purpose = (TextView)view.findViewById(R.id.list_item_appt_purpose);
					TextView dateTime = (TextView)view.findViewById(R.id.list_item_appt_time);
					purpose.setText(event.getPurpose());
					dateTime.setText(event.getDate().toString());
					break;
					
				case DIARY_ENTRY:
					if(view == null) {
						view = LayoutInflater.from(context).inflate(R.layout.list_item_entry, parent, false);
					}
						
					TextView notes = (TextView)view.findViewById(R.id.list_item_entry_notes);
					TextView date = (TextView)view.findViewById(R.id.list_item_entry_date);
					ImageView photo = (ImageView)view.findViewById(R.id.list_item_entry_photo);

					notes.setText(event.getText());
					date.setText(event.getDate().toString());
					FileOutputStream fos;
					
					try {
						// This works, but the image isn't scaled up to fill available space
						// even though the image width (800) is wider than my screen (768)!!
						// In fact, it looks as if it might have reduced the size by exactly half
						Uri uri = Uri.parse("content://com.thrivepregnancy.assetcontentprovider/baby.jpg");
						photo.setMaxHeight(1072);
						photo.setMaxWidth(1600);
						photo.setImageURI(uri);
						
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
					}
					catch (Throwable e){
						notes.setText(event.getText());
					}
					break;
			}
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
