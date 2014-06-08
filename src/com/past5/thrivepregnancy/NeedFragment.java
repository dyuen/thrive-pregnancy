//Vsevolod Geraskin
package com.past5.thrivepregnancy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Fragment;
import android.content.Context;

public class NeedFragment extends Fragment {
  ListView list;
  
  String[] needs = {"food","vitamins","health cared","social worker","support worker","cas hook up", "place to live", "dental care", 
		  "clothing","prenatal classes","labour support","hospital tour","supplies for baby","safety issues"};
  
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      return inflater.inflate(R.layout.fragment_need, container, false);
  }
  
  @Override
public void onActivityCreated(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    NeedList adapter = new NeedList(getView().getContext(), needs);
    
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
  
  private class NeedList extends BaseAdapter{
		private final Context context;
		private final String[] needs;
		
		public NeedList(Context context, String[] needArray) {
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

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}


