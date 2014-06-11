package com.thrivepregnancy.ui;

//import com.thrivepregnancy.ui.NeedList;
import java.util.ArrayList;

import com.thrivepregnancy.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Implements the "I Need" fragment in the {@link MainActivity} page
 * TODO: Remove hard coding, complete 
 */
public class NeedFragment extends Fragment {

	// Temporary to be replaced by resource strings
	String[] needs = {"Food","Housing","Drug & Alcohol Help","Social Work","Income Assistance","Dental Care", "Family Doctor", "Doula", 
			  "Baby Stuff","ID"};
	private Activity		parentActivity;
	/**
	 * Empty public constructor required per the {@link Fragment} API documentation
	 */
	public NeedFragment(){}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		parentActivity = activity;
	}
		
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    // Inflate the layout for this fragment
		View fragmentView = inflater.inflate(R.layout.fragment_need, container, false);
		return fragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    ArrayList<NeedArrayAdapter.NeedArrayItem> needItems = new ArrayList<NeedArrayAdapter.NeedArrayItem>();
	    for (String need: needs){
	    	needItems.add(new NeedArrayAdapter.NeedArrayItem(need));
	    }
	    NeedArrayAdapter adapter = NeedArrayAdapter.create(parentActivity, R.layout.need_list, needItems);
	    
/*	      
	    list=(ListView)getView().findViewById(R.id.lstNeed);
	    list.setAdapter(adapter);
	      
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Toast.makeText(getView().getContext(), "clicked at " + needs[ + position], Toast.LENGTH_SHORT).show();
            }
        });
*/
    }
		
	/**
	 * Custom list adapter for the "needs" list
	
	private class NeedListAdapter extends BaseAdapter{
		private final Context context;
		private final String[] needs;
			
		public NeedListAdapter(Context context, String[] needArray) {
			this.context = context;
			this.needs = needArray;
		}
			
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			View item;
				
			item = view;
			if(view == null) item = LayoutInflater.from(context).inflate(R.layout.need_list, parent, false);
				
			TextView txtNeed = (TextView) item.findViewById(R.id.need);
				
			txtNeed.setText(needs[position]);
				
			return item;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return needs.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}
 
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}*/
}
