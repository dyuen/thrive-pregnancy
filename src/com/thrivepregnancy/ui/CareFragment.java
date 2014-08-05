package com.thrivepregnancy.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.j256.ormlite.dao.Dao;
import com.thrivepregnancy.R;
import com.thrivepregnancy.data.DatabaseHelper;
import com.thrivepregnancy.data.Event;
import com.thrivepregnancy.data.EventDataHelper;
import com.thrivepregnancy.ui.TimelineFragment.ViewHolder;

import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Implements the "My Care" fragment in the {@link MainActivity} page
 */
public class CareFragment extends Fragment implements OnDateSetListener, MainActivity.InstanceSaver{
	
	private static final SimpleDateFormat dueDateFormat = new SimpleDateFormat("MMM d", Locale.CANADA);
	private static final SimpleDateFormat debugDateFormat = new SimpleDateFormat("MMM d, yyyy ", Locale.CANADA);
	private static final SimpleDateFormat testDateFormat = new SimpleDateFormat("EEEEEEEE MMMMMMMMM d", Locale.CANADA);
	private static final SimpleDateFormat appointmentDateFormat = new SimpleDateFormat("EEEEEEEE MMMMMMMMM d, hh:mm aaa", Locale.CANADA);
	
	private View 				fragmentView;
	private CareFragment 		fragment;
	private EventDataHelper 	dataHelper;
	private Dao<Event, Integer>	eventDao;
	private MainActivity		mainActivity;
	private CareListAdapter 	adapter;
	private boolean				editingProviderContact;
	
	private long	dueDate;
	private String 	providerName;
	private String 	providerLocation;
	private String 	oncallPhone;	

	private SharedPreferences 	preferences;
	
	private int					listCurrentIndex;
	private int					listCurrentOffset;
	
	// Budle keys to store state during rotation.	
	public static final String KEY_EDITING_PROVIDER = "editingProvider";
	public static final String KEY_PENDING_DATE = 	 "pendingdate";
	public static final String KEY_CARE_INDEX = "care-index";
	public static final String KEY_CARE_OFFSET = "care-offset";
	
	/**
	 * Scroll speed
	 */	
	public static final Integer FRICTION_SCALE_FACTOR = 5;
	
	/**
	 * Empty public constructor required per the {@link Fragment} API documentation
	 */
	public CareFragment(){
		super();
		fragment = this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mainActivity = (MainActivity)getActivity();
		DatabaseHelper databaseHelper = mainActivity.getHelper();
		eventDao = databaseHelper.getEventDao();
	    dataHelper = new EventDataHelper(databaseHelper);
	    preferences = getActivity().getSharedPreferences(StartupActivity.PREFERENCES, Context.MODE_PRIVATE);

	    if (savedInstanceState == null){
        	dueDate = preferences.getLong(StartupActivity.PREFERENCE_DUE_DATE, 0);
        }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    // Inflate the layout for this fragment
		fragmentView = inflater.inflate(R.layout.fragment_care, container, false);
		return fragmentView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated( savedInstanceState);
		adapter = new CareListAdapter(fragmentView);
		mainActivity.setCareListAdapter(adapter);
	}
	
	@Override
	public void onPause(){
		super.onPause();
    	adapter.saveCurrentListPosition();
	}
	
    // Implementation of InstanceSaver
   	public Bundle saveInstanceData(){
   		Bundle data = new Bundle();
   		data.putBoolean(KEY_EDITING_PROVIDER, editingProviderContact);
   		data.putLong(KEY_PENDING_DATE, dueDate);
   		data.putInt(KEY_CARE_INDEX, listCurrentIndex);
   		data.putInt(KEY_CARE_OFFSET, listCurrentOffset);
   		return data;
   	}
   	public void restoreInstanceData(Bundle data){
   		if (data != null){
   			editingProviderContact = data.getBoolean(KEY_EDITING_PROVIDER);
   			if (editingProviderContact){
   				dueDate = data.getLong(KEY_PENDING_DATE);
   			}
   			listCurrentIndex = data.getInt(KEY_CARE_INDEX);
   			listCurrentOffset = data.getInt(KEY_CARE_OFFSET);
   		}
   	}

	
	@Override
    public void onDestroy(){
        super.onDestroy();        
        adapter.saveCurrentListPosition();        
    }
	
	@Override
	public void onResume() {
		super.onResume();
		adapter.updateList();
	}
	/**
	 * Called by:
	 * - MainACtivity when delete confirmation result is positive: MyCare visible behind popup,
	 * - TimelineFragment when new appointment created: MyCAre not visible
	 * must be refreshed
	 */
	void refresh(){
		adapter.refreshAdapter();
	}
	
    /**
     * Called when the date has been set in the date popup 
     */
	public void onDateSet(DatePicker unused, int year, int month, int day) {
		Calendar date = Calendar.getInstance();
		date.set(Calendar.YEAR, year);		
		date.set(Calendar.MONTH, month);
		date.set(Calendar.DAY_OF_MONTH, day);
		
        dueDate = date.getTimeInMillis();
        final String strDueDate = dueDateFormat.format(dueDate);        
		final EditText dateView = (EditText)fragmentView.findViewById(R.id.delivery_date_edit);
		dateView.post(new Runnable(){
			public void run(){
				dateView.setText(strDueDate);
				adapter.notifyDataSetChanged();
			}
		});
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
    	ElementBacker(final String tag, final int resourceId, int type){
    		this.resourceId = resourceId;
    		this.tag = tag;
    		this.type = type;
    	}
    	ElementBacker(final String tag, final int resourceId, int type, Event event){
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
	 * Handles the result from Appointment or TestResult or CareProvider activities
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		if (intent == null){
			// Return from called activity by pressing back button
			return;
		}
		else {
			adapter.createBackingList();
			
			if (requestCode == MainActivity.REQUEST_CODE_APPOINTMENT){
				this.mainActivity.getTimelineFragment().refreshOnTimelineChange();
			}
			else if (requestCode == MainActivity.REQUEST_CODE_PROVIDER_EDIT){
		       	dueDate = preferences.getLong(StartupActivity.PREFERENCE_DUE_DATE, 0);
		    	providerName = preferences.getString(StartupActivity.PREFERENCE_PROVIDER_NAME, "");
		    	providerLocation = preferences.getString(StartupActivity.PREFERENCE_PROVIDER_LOCATION, "");
		    	oncallPhone = preferences.getString(StartupActivity.PREFERENCE_ONCALL_NUMBER, "");

				adapter.updateTimeline(dueDate);
				mainActivity.getTimelineFragment().refreshOnTimelineChange();
			}
			adapter.notifyDataSetChanged();
		}
	}
	
	private void showKeyboard(View view){
     	InputMethodManager inputManager = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
    	inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}
	private void hideKeyboard(View view){
     	InputMethodManager inputManager = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
    	inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public class CareListAdapter extends BaseAdapter{
		private List<Event> 			appointmentEvents;
		private List<Event> 			questionEvents;
		private List<Event> 			testResultEvents;
		private ListView 				listView;
	    
		private ArrayList<ElementBacker> elementBackers;
		
		void updateList(){
			listView = (ListView)getActivity().findViewById(R.id.lstCare);
			listView.setFriction(ViewConfiguration.getScrollFriction() * FRICTION_SCALE_FACTOR);
			listView.setItemsCanFocus(true);
			scrollToLastPosition();
			listView.setAdapter(this);
		}
		
		/*
		* Saves current list position by its index in the list and the pixel offset
		* from the top of the visible area of the list 
		*/
		private void saveCurrentListPosition() {
			listCurrentIndex = listView.getFirstVisiblePosition();
	        View view = listView.getChildAt(0);
	        // Top of this view, in pixels, relative to the top of the visible area of the list view
	        listCurrentOffset = (view == null) ? 0 : view.getTop();
		}

		public void scrollToLastPosition() {
			final int index = listCurrentIndex;
			final int offset = listCurrentOffset;
			listView.post(new Runnable(){
				public void run(){
					((ListView)getActivity().findViewById(R.id.lstCare)).setSelectionFromTop(index, offset);
				}
			});
		}
		
	    public CareListAdapter(View fragmentView) {	    	
	    	adapter = this;
			
			// "Cache" the provider contact info locally
	    	providerName = preferences.getString(StartupActivity.PREFERENCE_PROVIDER_NAME, "");
	    	providerLocation = preferences.getString(StartupActivity.PREFERENCE_PROVIDER_LOCATION, "");
	    	oncallPhone = preferences.getString(StartupActivity.PREFERENCE_ONCALL_NUMBER, "");
			
	    	// Create and fill list of element backers, one per element in the list
	    	elementBackers = createBackingList();
	    }
	    
	    void refreshAdapter(){
	    	// Create and fill list of element backers, one per element in the list
	    	elementBackers = createBackingList();
	    	updateList();
	    	listView.setAdapter(this);
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
			
			return elementBackers;
	    }
	    

	    
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ImageView 	photoView = null;
			ViewHolder holder;
			
			ElementBacker backer = elementBackers.get(position);
			if(view == null || !view.getTag().equals(backer.tag)) {
				view = LayoutInflater.from(mainActivity).inflate(backer.resourceId, parent, false);
				holder = new ViewHolder();
				holder.position = position;
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
		
			Event event = backer.event;
				
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
				((TextView)view.findViewById(R.id.list_item_add_new_text)).setOnClickListener(addNewAppointmentListener);
				break;
				
			case R.layout.list_item_add_new_question:
				OnClickListener listener = new OnClickListener(){
					@Override
					public void onClick(View view){
						saveCurrentListPosition();
						mainActivity.showQuestionDialog(R.string.Question_New, R.layout.new_question, R.string.save);
					}			
				};
				((ImageButton)view.findViewById(R.id.list_item_add_new)).setOnClickListener(listener);
				((TextView)view.findViewById(R.id.list_item_add_new_text)).setOnClickListener(listener);
				break;
				
			case R.layout.list_item_add_new_test_result:
				((ImageButton)view.findViewById(R.id.list_item_add_new)).setOnClickListener(addNewTestResultListener);
				((TextView)view.findViewById(R.id.list_item_add_new_text)).setOnClickListener(addNewTestResultListener);
				break;				
			}		
			
			if (event != null){		
				String photoFile = event.getPhotoFile();
				holder.picture = photoView;
				
				if (photoFile != null && photoFile.length() > 0){
					ImageLoader imageloader = new ImageLoader(photoFile,photoView, getActivity(),event.getType());
					imageloader.loadBitmap(event.getId(),position,holder);
				} else if (photoView != null) {
					photoView.setVisibility(View.GONE);
				}
			}
			
			return view;
		}
		
		OnClickListener addNewAppointmentListener = new OnClickListener(){
			@Override
			public void onClick(View view){
				saveCurrentListPosition();
				Intent intent = new Intent(mainActivity.getApplicationContext(), AppointmentActivity.class);
				intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_NEW);	        	
				fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);				
			}
		};

		OnClickListener addNewTestResultListener = new OnClickListener(){
			@Override
			public void onClick(View view){
				Intent intent = new Intent(mainActivity.getApplicationContext(), TestResultActivity.class);
				intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_NEW);	        	
				fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_TEST_RESULT);				
			}
		};

		private void populateProviderView(View view){
			// Display the values stored in preferences
	    	String userName = preferences.getString(StartupActivity.PREFERENCE_NAME, "");
	    	((TextView)view.findViewById(R.id.user_name)).setText(userName);

	    	String strDueDate = dueDateFormat.format(new Date(dueDate));
	    	((TextView)view.findViewById(R.id.delivery_date)).setText(
	    			getResources().getString(R.string.PersonalInfo_Due_Date) + ": " + strDueDate);
	    	((TextView)view.findViewById(R.id.provider_name)).setText(
	    			getResources().getString(R.string.PersonalInfo_Primary_Doctor) + ": " + providerName);
	    	((TextView)view.findViewById(R.id.provider_location)).setText(
	    			getResources().getString(R.string.PersonalInfo_Hospital) + ": " + providerLocation);
	    	((TextView)view.findViewById(R.id.provider_oncall_phone)).setText(
	    			getResources().getString(R.string.PersonalInfo_DrPhone) + ": " + oncallPhone);				
	    	
	    	ImageButton editButton = (ImageButton)view.findViewById(R.id.list_item_contact_open_edit);
	    	editButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					saveCurrentListPosition();
					Intent intent = new Intent(mainActivity.getApplicationContext(), CareProviderActivity.class);
					intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_EDIT);	        	
					fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_PROVIDER_EDIT);				
				}
			});
	    	
	    	EditText dateView = (EditText)view.findViewById(R.id.delivery_date_edit);		    
	    	if (dateView != null) {
		    	dateView.setOnClickListener(new OnClickListener() {        
		            public void onClick(View v) {
		            	Calendar earliest = Calendar.getInstance();
		            	earliest.setTimeInMillis(dueDate);
		            	earliest.add(Calendar.DAY_OF_YEAR, -60);
		            	Calendar latest = Calendar.getInstance();
		            	latest.setTimeInMillis(dueDate);
		            	latest.add(Calendar.DAY_OF_YEAR, 60);
		            	
		            	mainActivity.showDateDialog(R.string.PersonalInfo_Delivery_Date,
		              			earliest.getTimeInMillis(), latest.getTimeInMillis(),dueDate, fragment);		            	
		            }
		        });
		    }
		}
		
		private void updateTimeline(long newDueDate){
			List<Event> tips = dataHelper.getTips();
			Calendar dueDateCal = Calendar.getInstance();
			dueDateCal.setTimeInMillis(newDueDate);
			
			int week = 1;
			for (Event tip: tips){
		        Calendar date = (Calendar)dueDateCal.clone();
		        date.add(Calendar.DAY_OF_YEAR, 7 * (week - 41));
				tip.setDate(date.getTime());
				try {
					eventDao.update(tip);
				}
				catch (SQLException e){
					//Log.e(MainActivity.DEBUG_TAG, "Can't update event", e);
				}
				week++;
			}
		}
		
		private void populateAppointmentView(View view, final Event event){
			
			TextView address = (TextView)view.findViewById(R.id.list_item_appt_address);
			TextView doctor = (TextView)view.findViewById(R.id.list_item_appt_doctor);
			TextView apptnotes = (TextView)view.findViewById(R.id.list_item_appt_notes);
			
			((TextView)view.findViewById(R.id.list_item_appt_purpose)).setText(event.getPurpose());
			((TextView)view.findViewById(R.id.list_item_appt_time)).setText(appointmentDateFormat.format(event.getDate()));
			
			address.setText(event.getAddress());
			doctor.setText(event.getDoctor());
			apptnotes.setText(event.getText());
			
			if (event.getAddress() == null || event.getAddress().length()==0) 
				address.setVisibility(View.GONE);
			
			if (event.getDoctor() == null || event.getDoctor().length()==0) 
				doctor.setVisibility(View.GONE);
			
			if (event.getText() == null || event.getText().length()==0) 
				apptnotes.setVisibility(View.GONE);
			
			ImageButton editAppointment = (ImageButton)view.findViewById(R.id.list_item_appt_edit);
			final Integer eventId = event.getId();
			editAppointment.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					Intent intent = new Intent(mainActivity.getApplicationContext(), AppointmentActivity.class);
					intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_EDIT);	        	
					intent.putExtra(MainActivity.REQUEST_PRIMARY_KEY, eventId);	        	
					fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_APPOINTMENT);
				}				
			});	
			
			ImageButton deleteAppointment = (ImageButton)view.findViewById(R.id.list_item_appt_delete);
			deleteAppointment.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					saveCurrentListPosition();
					mainActivity.showConfirmationDialog(R.string.dlg_delete_appointment, event);
				}				
			});			
			
			ImageView photoView = (ImageView)view.findViewById(R.id.list_item_appt_photo);
	    	setupPhotoView(event.getPhotoFile(), photoView);	    	
	    	photoView.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), PictureActivity.class);
					Bundle parentBundle = new Bundle();
					String imagePath = event.getPhotoFile();
					String activityTitle = "Appointment Photo";
					Integer eventId = event.getId();
					
					parentBundle.putInt("eventid", eventId);
					parentBundle.putString("imagepath", imagePath);
					parentBundle.putString("imagetitle", activityTitle);
					
					intent.putExtras(parentBundle);
					startActivity(intent);
				}
			});
		}
		private void populateQuestionView(View view, final Event event){
			((TextView)view.findViewById(R.id.list_item_question_text)).setText(event.getText());
	    	ImageButton deleteQuestion = (ImageButton)view.findViewById(R.id.list_item_question_delete);
	    	deleteQuestion.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					saveCurrentListPosition();
					mainActivity.showConfirmationDialog(R.string.dlg_delete_question, event);
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
					
					Intent intent = new Intent(mainActivity.getApplicationContext(), TestResultActivity.class);
					intent.putExtra(MainActivity.REQUEST_MODE, MainActivity.REQUEST_MODE_EDIT);	        	
					intent.putExtra(MainActivity.REQUEST_PRIMARY_KEY, eventId);	        	
					fragment.startActivityForResult(intent, MainActivity.REQUEST_CODE_TEST_RESULT);
				}				
			});			
			ImageButton deleteTest = (ImageButton)view.findViewById(R.id.list_item_test_delete);
			deleteTest.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view){
					saveCurrentListPosition();
					mainActivity.showConfirmationDialog(R.string.dlg_delete_test_result, event);
				}				
			});			
			
			ImageView photoView = (ImageView)view.findViewById(R.id.list_item_test_result_photo);			
	    	setupPhotoView(event.getPhotoFile(), photoView);	    	
	    	photoView.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), PictureActivity.class);
					Bundle parentBundle = new Bundle();
					String imagePath = event.getPhotoFile();
					String activityTitle = "Test Result";
					Integer eventId = event.getId();
					
					parentBundle.putInt("eventid", eventId);
					parentBundle.putString("imagepath", imagePath);
					parentBundle.putString("imagetitle", activityTitle);
					
					intent.putExtras(parentBundle);
					startActivity(intent);
				}
			});
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
}
