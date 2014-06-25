package com.thrivepregnancy.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.thrivepregnancy.R;
import com.thrivepregnancy.data.Event;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * NOTE: This is no longer generalized, so that separate columns for keyword and desciption are 
 * defined.
 * 
 * An ArrayAdapter for list items containing both a button and a text area. For each such item,
 * an arbitarty object defined by the View incorporating the list is associated with the item.<p/>
 * 
 * An object implementing the CompositeArrayAdapter.OnClickCallback will receive a click 
 * notification specifying the registered object, and the View (either the button or text area,)
 * on which the click occurred.
 */
public class TimelineArrayAdapter extends ArrayAdapter {
	
	private Context 		context;
	private OnClickCallback callback;
	private HashMap<Object, Event>	itemLookup;
	private List<Event> 	events;
	
	public TimelineArrayAdapter(Context context, int itemLayout, List<Event> events) {
	    super(context, itemLayout, events);
	    this.context = context;
	    itemLookup = new HashMap<Object, Event>();
	    this.events = events;
	}
	
	public static TimelineArrayAdapter create(Context context, int itemLayout, List<Event> events){
		return new TimelineArrayAdapter(context, itemLayout, events);
	}
	
	public void setOnClickCallback(OnClickCallback callback){
	    this.callback = callback;
	}
	
	/**
	 * Returns a view for each item within the adapter view
	 * @param position position of the item in the list ?
	 * @param view the View for each list item, possibly being reused 
	 */
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
				purpose.setText(event.getDoctor());
				dateTime.setText(event.getDate().toString());

				break;
			
			case DIARY_ENTRY:
				if(view == null) {
					view = LayoutInflater.from(context).inflate(R.layout.list_item_entry, parent, false);
				}
			break;
		}
		return view;
	}
/*
	private OnClickListener clickListener = new OnClickListener(){
		public void onClick(View view){
			callback.onClick(view, ((Event)view.getTag()).getRegisteredObject());
		}	
	};
*/	
	/**
	 * Removes the item associated with the specified registered object
	 * @param registeredObject
	 */
	public void removeItem(Object registeredObject){
		Event item = itemLookup.get(registeredObject);
		super.remove(item);
	}
	
	public void addItem(Object o){
		
	}
/*	
	public static class Event {
    	private String 	keyword;
    	private String 	description;
    	private Object	registeredObject;
		public Event(Object registeredObject){
			AudioSource audioSource = (AudioSource)registeredObject;
			this.keyword = audioSource.getKeyword();
			this.description = audioSource.getDescription();
			this.registeredObject = registeredObject;
		}
		public String getKeyword() {
			return keyword;
		}
		public String getDescription() {
			return description;
		}
		public Object getRegisteredObject() {
			return registeredObject;
		}
	}

	private static List<Event> createItemList(List registeredObjects){
		List<Event> events = new ArrayList<Event>();
		for (Object registeredObject: registeredObjects){
			Event compositeItem = new TimelineArrayAdapter.Event(registeredObject);
			events.add(compositeItem);
		}
		return events;
	}
*/		
	public interface OnClickCallback {
		public void onClick(View view, Object registeredObject);
	}
}
