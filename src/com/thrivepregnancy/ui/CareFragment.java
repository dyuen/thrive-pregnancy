package com.thrivepregnancy.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.j256.ormlite.dao.Dao;
import com.thrivepregnancy.R;
import com.thrivepregnancy.data.DatabaseHelper;
import com.thrivepregnancy.data.Event;
import com.thrivepregnancy.data.EventDataHelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Implements the "My Care" fragment in the {@link MainActivity} page
 */
public class CareFragment extends Fragment {
	
	private static SimpleDateFormat dueDateFormat = new SimpleDateFormat("MMM d");
	private static SimpleDateFormat testDateFormat = new SimpleDateFormat("EEEEEEEE MMMMMMMMM d");
	private static SimpleDateFormat appointmentDateFormat = new SimpleDateFormat("EEEEEEEE MMMMMMMMM d, hh:mm aaa");

	private View 				fragmentView;
	private CareFragment 		fragment;
	private EventDataHelper 	dataHelper;
	private Dao<Event, Integer>	eventDao;
	private MainActivity		activity;
	private CareListAdapter 	adapter;
	/**
	 * Empty public constructor required per the {@link Fragment} API documentation
	 */
	public CareFragment(){
		fragment = this;
		Log.d(MainActivity.DEBUG_TAG, "---new CareFragment instance");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    // Inflate the layout for this fragment
		fragmentView = inflater.inflate(R.layout.fragment_care, container, false);
		return fragmentView;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		activity = (MainActivity)getActivity();
		DatabaseHelper databaseHelper = activity.getHelper();
		eventDao = databaseHelper.getEventDao();
	    dataHelper = new EventDataHelper(databaseHelper);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Create and set the adapter with this list
		adapter = new CareListAdapter(fragmentView);
		ListView listView = (ListView)getActivity().findViewById(R.id.lstCare);
		listView.setAdapter(adapter);
	}
	
	// These correspond to the tag attribute of the element's root layout 
    private static final String TAG_PROVIDER 			= "PROVIDER";
    private static final String TAG_APPOINTMENTS_HEADER	= "APPOINTMENTS_HEADER";
    private static final String TAG_APPOINTMENT 		= "APPOINTMENT";
    private static final String TAG_QUESTIONS_HEADER	= "QUESTIONS_HEADER";
    private static final String TAG_QUESTION 			= "QUESTION";
    private static final String TAG_TEST_RESULTS_HEADER	= "TEST_RESULTS_HEADER";
    private static final String TAG_TEST_RESULT 		= "TEST_RESULT";
    private static final String TAG_ADD_NEW_APPOINTMENT	= "ADD_NEW_APPOINTMENT";
    private static final String TAG_ADD_NEW_QUESTION	= "ADD_NEW_QUESTION";
    private static final String TAG_ADD_NEW_TEST_RESULT	= "ADD_NEW_TEST_RESULT";

    // Corresponding integers required by the adapter getItemViewType() method
    private static final int TYPE_PROVIDER 					= 0;
    private static final int TYPE_APPOINTMENTS_HEADER		= 1;
    private static final int TYPE_APPOINTMENT 				= 2;
    private static final int TYPE_QUESTIONS_HEADER			= 3;
    private static final int TYPE_QUESTION 					= 4;
    private static final int TYPE_TEST_RESULTS_HEADER		= 5;
    private static final int TYPE_TEST_RESULT 				= 6;
    private static final int TYPE_ADD_NEW_APPOINTMENT		= 7;
    private static final int TYPE_ADD_NEW_QUESTION			= 8;
    private static final int TYPE_ADD_NEW_TEST_RESULT		= 9;
	
    private class ElementBacker{
    	final String	tag;		// See TAG_.. above
    	int				type;		// See TYPE_.. above
    	final int		resourceId; // Resource id of the layout to render the element
    	Event			event;	    // (for TAG_APPOINTMENT, TAG_QUESTION, TAG_TEST_RESULT only)
    	String			addNewText; // (for TAG_NEW only)
    	ElementBacker(final String tag, final int resourceId, int type){
    		this.resourceId = resourceId;
    		this.tag = tag;
    		this.type = type;
    	}
    	ElementBacker(final String tag, final int resourceId, int type, Event event){
    		this(tag, resourceId, type);
    		this.event = event;
    	}
    	ElementBacker(final String tag, final int resourceId, int type, int stringResourceId){
    		this(tag, resourceId, type);
    		this.event = event;
    	}
    }
    
    // Re-useable "fixed" list element backers 
    private ElementBacker backerPROVIDER 			= new ElementBacker(TAG_PROVIDER, 				R.layout.list_item_provider, 			TYPE_PROVIDER);            
    private ElementBacker backerAPPOINTMENTS_HEADER = new ElementBacker(TAG_APPOINTMENTS_HEADER, 	R.layout.list_item_appointments_header, TYPE_APPOINTMENTS_HEADER);
    private ElementBacker backerQUESTIONS_HEADER 	= new ElementBacker(TAG_QUESTIONS_HEADER, 		R.layout.list_item_questions_header, 	TYPE_QUESTIONS_HEADER);
    private ElementBacker backerTEST_RESULTS_HEADER = new ElementBacker(TAG_TEST_RESULTS_HEADER, 	R.layout.list_item_test_results_header, TYPE_TEST_RESULTS_HEADER);
    private ElementBacker backerADD_NEW_APPOINTMENT = new ElementBacker(TAG_ADD_NEW_APPOINTMENT, 	R.layout.list_item_add_new_appointment, TYPE_ADD_NEW_APPOINTMENT);
    private ElementBacker backerADD_NEW_QUESTION 	= new ElementBacker(TAG_ADD_NEW_QUESTION, 		R.layout.list_item_add_new_question, 	TYPE_ADD_NEW_QUESTION);
    private ElementBacker backerADD_NEW_TEST_RESULT = new ElementBacker(TAG_ADD_NEW_TEST_RESULT, 	R.layout.list_item_add_new_test_result, TYPE_ADD_NEW_TEST_RESULT);

	/**
	 * Handles the result from Appointment or Test Result
	 */
	@Override
	public void onActivityResult(int requestCodeIgnored, int resultCode, Intent intent){
		if (intent == null){
			// Return from called activity by pressing back button
			return;
		}
		else {
			adapter.createBackingList();
			adapter.notifyDataSetChanged();
		}
	}
    private class CareListAdapter extends BaseAdapter{
		private List<Event> 			appointmentEvents;
		private List<Event> 			questionEvents;
		private List<Event> 			testResultEvents;
		private int						appointmentListIndex;
		private int						questionListIndex;
		private int						testResultListIndex;
		private boolean					editingProviderContact;
		private View					providerView;
		private SharedPreferences 		preferences;
		private CareListAdapter			adapter;
		
		private long dueDate;
		private String providerName;
		private String providerLocation;
		private String oncallPhone;		
	    
		private ArrayList<ElementBacker> elementBackers;
		
	    public CareListAdapter(View fragmentView) {	    	
	    	adapter = this;
			editingProviderContact = false;
			
			// "Cache" the provider contact info locally
			preferences = activity.getSharedPreferences(StartupActivity.PREFERENCES, Context.MODE_PRIVATE);
			dueDate = preferences.getLong(StartupActivity.PREFERENCE_DUE_DATE, 0);
	    	providerName = preferences.getString(StartupActivity.PREFERENCE_PROVIDER_NAME, "");
	    	providerLocation = preferences.getString(StartupActivity.PREFERENCE_PROVIDER_LOCATION, "");
	    	oncallPhone = preferences.getString(StartupActivity.PREFERENCE_ONCALL_NUMBER, "");
			
	    	// Create and fill list of element backers, one per element in the list
	    	elementBackers = createBackingList();
	    }
	    
	    /**
		 * Scales image to fix out of memory crash
		 */
		public Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
	 
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
	 
	    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	 
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
	    
	    public ArrayList<ElementBacker> createBackingList(){
	    	elementBackers = new ArrayList<ElementBacker>();	    	
	        elementBackers.add(backerPROVIDER);
	        
	        elementBackers.add(backerAPPOINTMENTS_HEADER);
	        appointmentEvents = dataHelper.getAppointments();
	        for (Event event: appointmentEvents){
	        	elementBackers.add(new ElementBacker(TAG_APPOINTMENT, R.layout.list_item_appointment_care, TYPE_APPOINTMENT, event));
	        }
	        elementBackers.add(backerADD_NEW_APPOINTMENT);
	        
	        elementBackers.add(backerQUESTIONS_HEADER);
	        questionEvents = dataHelper.getQuestions();
	        for (Event event: questionEvents){
	        	elementBackers.add(new ElementBacker(TAG_QUESTION, R.layout.list_item_question, TYPE_QUESTION, event));
	        }
	        elementBackers.add(backerADD_NEW_QUESTION);

	        elementBackers.add(backerTEST_RESULTS_HEADER);
	        testResultEvents = dataHelper.getTestResults();
	        for (Event event: testResultEvents){
	        	elementBackers.add(new ElementBacker(TAG_TEST_RESULT, R.layout.list_item_test_result, TYPE_TEST_RESULT, event));
	        }
	        elementBackers.add(backerADD_NEW_TEST_RESULT);
			
	        resetIndices();
			return elementBackers;
	    }
	    
	    private void resetIndices(){
	        appointmentListIndex = 0;
			questionListIndex = 0;
			testResultListIndex = 0;	    	
	    }

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ImageView 	photoView = null;
			Bitmap 		bitmap = null;
			
			ElementBacker backer = elementBackers.get(position);
			if(view == null || !view.getTag().equals(backer.tag)) {
				view = LayoutInflater.from(activity).inflate(backer.resourceId, parent, false);
			}
		
			Event event = backer.event;
			
			if (event != null){		
				String photoFile = event.getPhotoFile();
				if (photoFile != null && photoFile.length() > 0){
					File file = new File(photoFile);
					//bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());  
					bitmap = decodeSampledBitmapFromPath(file.getAbsolutePath(),200,200);
				}
			}
			
			switch (backer.resourceId) {
			case R.layout.list_item_provider:
		    	populateProviderView(view);
	    		break;

			case R.layout.list_item_appointment_care:
				photoView = (ImageView)view.findViewById(R.id.list_item_appt_photo);
				populateAppointmentView(view, backer.event);
				break;

			case R.layout.list_item_question:
				populateQuestionView(view, backer.event);
				break;

			case R.layout.list_item_test_result:
				photoView = (ImageView)view.findViewById(R.id.list_item_test_result_photo);
				populateTestResultView(view, backer.event);
				break;

			case R.layout.list_item_add_new_appointment:
				((ImageButton)view.findViewById(R.id.list_item_add_new)).setOnClickListener(addNewAppointmentListener);
				break;
				
			case R.layout.list_item_add_new_question:
				CreateNewQuestionOnClickLister listener = new CreateNewQuestionOnClickLister(view);
				((ImageButton)view.findViewById(R.id.list_item_add_new)).setOnClickListener(listener);
				((ImageButton)view.findViewById(R.id.new_question_save)).setOnClickListener(listener);
				break;
				
			case R.layout.list_item_add_new_test_result:
				((ImageButton)view.findViewById(R.id.list_item_add_new)).setOnClickListener(addNewTestResultListener);
				break;
				
			}		
			
			if (bitmap != null){
				photoView.setVisibility(View.VISIBLE);
				photoView.setImageBitmap(bitmap);
			} else if (photoView != null) {
				photoView.setVisibility(View.GONE);
			}
			
			return view;
		}
		
		// For both the pencil and the checkmark
		private class CreateNewQuestionOnClickLister implements OnClickListener{
			View parentView;
			CreateNewQuestionOnClickLister(View parentView){
				this.parentView = parentView;
			}
			@Override
			public void onClick(View view){
				View requestSection = parentView.findViewById(R.id.new_question_request);
				View entrySection = parentView.findViewById(R.id.new_question_entry);
				if (requestSection.getVisibility() == View.VISIBLE){
					requestSection.setVisibility(View.GONE);
					entrySection.setVisibility(View.VISIBLE);
				}
				else {
					requestSection.setVisibility(View.VISIBLE);
					entrySection.setVisibility(View.GONE);
					String text = ((TextView)entrySection.findViewById(R.id.new_question_text)).getText().toString();
					// Create the Question event
					Event question = new Event();
					question.setType(Event.Type.QUESTION);
					question.setDate(new Date());
					question.setText(text);
					try {
						eventDao.create(question);
					}
					catch (SQLException e){
						Log.e(MainActivity.DEBUG_TAG, "Can't create question Event",  e);
					}
					adapter.createBackingList();
					adapter.notifyDataSetChanged();
				}
			}			
		}
		
		OnClickListener addNewAppointmentListener = new OnClickListener(){
			@Override
			public void onClick(View view){
				Intent intent = new Intent(activity.getApplicationContext(), AppointmentActivity.class);
				intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_NEW);	        	
				fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);				
			}
		};

		OnClickListener addNewTestResultListener = new OnClickListener(){
			@Override
			public void onClick(View view){
				Intent intent = new Intent(activity.getApplicationContext(), TestResultActivity.class);
				intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_NEW);	        	
				fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_TEST_RESULT);				
			}
		};

		private void populateProviderView(View view){
			// Display the values stored in preferences
	    	String userName = preferences.getString(StartupActivity.PREFERENCE_NAME, "");
	    	((TextView)view.findViewById(R.id.user_name)).setText(userName);

	    	String strDueDate = dueDateFormat.format(new Date(dueDate));
	    	
	    	if (editingProviderContact){
				(view.findViewById(R.id.provider_contact)).setVisibility(View.GONE);
				(view.findViewById(R.id.provider_contact_edit)).setVisibility(View.VISIBLE);
		    	((EditText)view.findViewById(R.id.delivery_date_edit)).setText(strDueDate);
		    	((EditText)view.findViewById(R.id.provider_name_edit)).setText(providerName);
		    	((EditText)view.findViewById(R.id.provider_location_edit)).setText(providerLocation);
		    	((EditText)view.findViewById(R.id.provider_oncall_phone_edit)).setText(oncallPhone);
		    	((ImageButton)view.findViewById(R.id.list_item_contact_edit)).setImageResource(R.drawable.ic_checkmark);
			}
			else {
				(view.findViewById(R.id.provider_contact_edit)).setVisibility(View.GONE);
				(view.findViewById(R.id.provider_contact)).setVisibility(View.VISIBLE);
		    	((TextView)view.findViewById(R.id.delivery_date)).setText(
		    			getResources().getString(R.string.PersonalInfo_Due_Date) + ": " + strDueDate);
		    	((TextView)view.findViewById(R.id.provider_name)).setText(
		    			getResources().getString(R.string.PersonalInfo_Primary_Doctor) + ": " + providerName);
		    	((TextView)view.findViewById(R.id.provider_location)).setText(
		    			getResources().getString(R.string.PersonalInfo_Hospital) + ": " + providerLocation);
		    	((TextView)view.findViewById(R.id.provider_oncall_phone)).setText(
		    			getResources().getString(R.string.PersonalInfo_DrPhone) + ": " + oncallPhone);				
		    	((ImageButton)view.findViewById(R.id.list_item_contact_edit)).setImageResource(R.drawable.ic_pencil);
			}
	    	ImageButton editOrSaveButton = (ImageButton)view.findViewById(R.id.list_item_contact_edit);
	    	editOrSaveButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					if (editingProviderContact){
						// Update the "cached" values and store them in preferences
						SharedPreferences.Editor editor = preferences.edit();
				    	
				    	String strDueDate = ((EditText)fragmentView.findViewById(R.id.delivery_date_edit)).getText().toString();
				    	dueDate = parseDate(dueDateFormat, strDueDate);
				    	providerName = ((EditText)fragmentView.findViewById(R.id.provider_name_edit)).getText().toString();
				    	providerLocation = ((EditText)fragmentView.findViewById(R.id.provider_location_edit)).getText().toString();
				    	oncallPhone = ((EditText)fragmentView.findViewById(R.id.provider_oncall_phone_edit)).getText().toString();				
				    	
				    	editor.putLong(StartupActivity.PREFERENCE_DUE_DATE, dueDate);
				    	editor.putString(StartupActivity.PREFERENCE_PROVIDER_NAME, providerName);
				    	editor.putString(StartupActivity.PREFERENCE_PROVIDER_LOCATION, providerLocation);
				    	editor.putString(StartupActivity.PREFERENCE_ONCALL_NUMBER, oncallPhone);
				    	editor.commit();
					}
					// Toggle the editing state and notify 
					editingProviderContact = !editingProviderContact;
					adapter.notifyDataSetChanged();
				}
			});
		}
		
		private void populateAppointmentView(View view, final Event event){

			((TextView)view.findViewById(R.id.list_item_appt_purpose)).setText(event.getPurpose());
			((TextView)view.findViewById(R.id.list_item_appt_time)).setText(appointmentDateFormat.format(event.getDate()));
			((TextView)view.findViewById(R.id.list_item_appt_address)).setText(event.getAddress());
			((TextView)view.findViewById(R.id.list_item_appt_doctor)).setText(event.getDoctor());
			((TextView)view.findViewById(R.id.list_item_appt_notes)).setText(event.getText());
			
			ImageButton editAppointment = (ImageButton)view.findViewById(R.id.list_item_appt_edit);
			final Integer eventId = event.getId();
			editAppointment.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					Intent intent = new Intent(activity.getApplicationContext(), AppointmentActivity.class);
					intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_EDIT);	        	
					intent.putExtra(MainActivity.REQUEST_PRIMARY_KEY, eventId);	        	
					fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);
				}				
			});			
			ImageButton deleteAppointment = (ImageButton)view.findViewById(R.id.list_item_appt_delete);
			deleteAppointment.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					try {
						eventDao.delete(event);
						adapter.createBackingList();
						adapter.notifyDataSetChanged();
					}
					catch (SQLException e){
						Log.e(MainActivity.DEBUG_TAG, "Can't delete appointment", e);
					}
				}				
			});			
	    	setupPhotoView(event.getPhotoFile(), (ImageView)view.findViewById(R.id.list_item_appt_photo));
		}
		
		private void populateQuestionView(View view, final Event event){
			((TextView)view.findViewById(R.id.list_item_question_text)).setText(event.getText());
	    	ImageButton deleteQuestion = (ImageButton)view.findViewById(R.id.list_item_question_delete);
	    	deleteQuestion.setOnClickListener(new OnClickListener(){
				@Override
				// TODO Confirmation
				public void onClick(View view){
					Dao<Event, Integer> dao = activity.getHelper().getEventDao();
					try {
						dao.delete(event);
						adapter.createBackingList();
						adapter.notifyDataSetChanged();
					}
					catch (SQLException e){
						Log.e(MainActivity.DEBUG_TAG, "Can't delete question", e);
					}
				}				
			});			
		}
		
		private void populateTestResultView(View view, final Event event){
			((TextView)view.findViewById(R.id.list_item_test_result_description)).setText(event.getText());
			((TextView)view.findViewById(R.id.list_item_test_result_date)).setText(testDateFormat.format(event.getDate()));
			
			ImageButton editTest = (ImageButton)view.findViewById(R.id.list_item_test_edit);
			final Integer eventId = event.getId();
			editTest.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					Intent intent = new Intent(activity.getApplicationContext(), TestResultActivity.class);
					intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_EDIT);	        	
					intent.putExtra(MainActivity.REQUEST_PRIMARY_KEY, eventId);	        	
					fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_TEST_RESULT);
				}				
			});			
			ImageButton deleteTest = (ImageButton)view.findViewById(R.id.list_item_test_delete);
			deleteTest.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					try {
						eventDao.delete(event);
						adapter.createBackingList();
						adapter.notifyDataSetChanged();
					}
					catch (SQLException e){
						Log.e(MainActivity.DEBUG_TAG, "Can't delete test result", e);
					}
				}				
			});			
			
	    	setupPhotoView(event.getPhotoFile(), (ImageView)view.findViewById(R.id.list_item_test_result_photo));
		}
		
		private void setupPhotoView(String photoFile, ImageView photoView){
			if (photoFile != null && photoFile.length() > 0){
				Uri uri = Uri.parse("content://com.thrivepregnancy.assetcontentprovider/" + photoFile);
				photoView.setImageURI(uri);
			}
		}

		//***************************************** From BaseAdapter
		// Get the type of View that will be created by getView(int, View, ViewGroup) for the specified item. (0...
		@Override
		public int getItemViewType (int position){
			return elementBackers.get(position).type;
		}		
		//Returns the number of types of Views that will be created by getView(int, View, ViewGroup)
		@Override
		public int getViewTypeCount (){
			return 10;
		}
		//Indicates whether the item ids are stable across changes to the underlying data.
		@Override public boolean hasStableIds (){
			return true;
		}
		//***************************************** From adapter
		// How many items are in the data set represented by this Adapter
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			int count = (int) dataHelper.getCareEventCount();
			return count + 7;
		}
		// Get the data item associated with the specified position in the data set.
		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

	private long parseDate(SimpleDateFormat format, String date){
		try {
			return format.parse(date).getTime();
		}
		catch (ParseException e){
			Log.e(MainActivity.DEBUG_TAG, "Error parsing date string", e);
			return 0;
		}
	}
}
