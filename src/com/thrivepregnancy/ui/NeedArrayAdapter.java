package com.thrivepregnancy.ui;

import com.thrivepregnancy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * An ArrayAdapter for the "I Need" list. Each list item contains a pair of radio buttons 
 * and a "display info" button, and a datum identifying the corresponding object
 * TODO: Incomplete: partially adapted from another project
 */
public class NeedArrayAdapter extends ArrayAdapter<NeedArrayAdapter.NeedArrayItem> {
	
	private Context 		context;
	private OnClickCallback callback;
	private HashMap<Object, NeedArrayItem>	itemLookup;
	
	public NeedArrayAdapter(Context context, int itemLayout, List<NeedArrayItem> compositeItems) {
	    super(context, itemLayout, compositeItems);
	    this.context = context;
	    itemLookup = new HashMap<Object, NeedArrayItem>();
	    for (NeedArrayItem item: compositeItems){
//	    	itemLookup.put(item.getRegisteredObject(), item);
	    }
	}
	
	public static NeedArrayAdapter create(Context context, int itemLayout, List<NeedArrayItem> registeredObjects){
//		List<NeedArrayItem> compositeItems = createItemList(registeredObjects);
		return new NeedArrayAdapter(context, itemLayout, registeredObjects);
	}
	
	public void setOnClickCallback(OnClickCallback callback){
	    this.callback = callback;
	}
	
	/**
	 * Returns a view for each item within the adapter view
	 * @param position position of the item in the list ?
	 * @param itemView the View for each list item, possibly being reused 
	 */
	@Override
	public View getView(int position, View itemView, ViewGroup parent) {
		
		if (itemView == null){
			// First time the View is being used to display a list item
			itemView = View.inflate (context, R.layout.need_list, null);
		}
		return itemView;		
	}
/*
	private OnClickListener clickListener = new OnClickListener(){
		public void onClick(View view){
			callback.onClick(view, ((NeedArrayItem)view.getTag()).getRegisteredObject());
		}	
	};
	
*/	
	public void addItem(Object o){
		
	}
	
	public static class NeedArrayItem {
    	private String 	need;
		public NeedArrayItem(String need){
			this.need = need;
//			this.registeredObject = registeredObject;
		}
		public String getNeedd() {
			return need;
		}
	}
/*	
	private static List<NeedArrayItem> createItemList(List registeredObjects){
		List<NeedArrayItem> compositeItems = new ArrayList<NeedArrayItem>();
		for (Object registeredObject: registeredObjects){
			NeedArrayItem compositeItem = new NeedArrayAdapter.NeedArrayItem(registeredObject);
			compositeItems.add(compositeItem);
		}
		return compositeItems;
	}
*/	
	public interface OnClickCallback {
		public void onClick(View view, Object registeredObject);
	}
}
