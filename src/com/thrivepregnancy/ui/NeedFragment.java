package com.thrivepregnancy.ui;

//Vsevolod Geraskin
import java.util.List;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.thrivepregnancy.R;
import com.thrivepregnancy.data.DatabaseHelper;
import com.thrivepregnancy.data.EventDataHelper;
import com.thrivepregnancy.data.Need;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.Fragment;
import android.content.Context;

public class NeedFragment extends Fragment {
	  ListView list;
	  
	  private EventDataHelper 	needHelper = null;
	  
	/**
	 * Empty public constructor required per the {@link Fragment} API documentation
	 */
	public NeedFragment(){}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      return inflater.inflate(R.layout.fragment_need, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    

	    if (needHelper == null) {
	    	needHelper = OpenHelperManager.getHelper(getView().getContext(), EventDataHelper.class);
	    }
	    
	    NeedList adapter = new NeedList(getView().getContext(), needHelper.getNeeds());
	    
	    list=(ListView)getView().findViewById(R.id.lstNeed);
	    list.setAdapter(adapter);
	    
	        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	                @Override
	                public void onItemClick(AdapterView<?> parent, View view,
	                                        int position, long id) {
	                    //Toast.makeText(getView().getContext(), "clicked at " + needs[ + position], Toast.LENGTH_SHORT).show();
	                }
	            });
	     
		}
	
	/**
	 * Releases the database helper
	 */
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (needHelper != null) {
	        OpenHelperManager.releaseHelper();
	        needHelper = null;
	    }
	}
  
  	private class NeedList extends BaseAdapter{
		private final Context context;
		private final List<Need> needs;
		
		public NeedList(Context context, List<Need> needs) {
			this.context = context;
			this.needs = needs;
		}
		
		@Override
		public View getView(int position, View view, ViewGroup parent) {
			View item;
			
			item = view;
			if(view == null) item = LayoutInflater.from(context).inflate(R.layout.need_list, parent, false);
			
			TextView txtNeed = (TextView) item.findViewById(R.id.need);
			
			txtNeed.setText(needs.get(position).getTitle());
			
			return item;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return needs.size();
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