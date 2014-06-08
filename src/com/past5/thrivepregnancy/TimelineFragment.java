//Vsevolod Geraskin
package com.past5.thrivepregnancy;

import android.app.Fragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class TimelineFragment extends Fragment implements OnClickListener {
	
	  ListView m_list;
	  Calendar m_duedate;
	  Calendar m_curdate;
	  String m_name;
	  int m_weekvalue;
      Button tlBtn;
      Button  mcBtn;
      Button  inBtn;
      Button storyBtn;
      Context m_context;
      ImageButton m_btnNote;
      ImageButton m_btnAppt;
      String m_dateArray[][];
      
	  public static final int WEEKS_IN_PREGNANCY = 40;
	  public static final int DAYS_IN_PREGNANCY = WEEKS_IN_PREGNANCY * 7;
	  
	  
    public static final int[] HDR_POS = {0,1,2,3,4,7,10,14,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47,49,51,53,55,57,59,61,
    	63,65,67,69,71,73,75,77,79};
    
    private final String[] LIST = {"Week 1","Week 2","Week 3","Week 4","Week 5",null, "Feelings Today", "Week 6", null,
    		"My Belly", "Week 7", null,"Sunday July 6, 12:00PM","Ultrasound pic", "Week 8",null,null, "Week 9",null,
    		"Week 10",null,"Week 11",null,"Week 12",null,"Week 13",null,"Week 14",null,"Week 15",null,"Week 16",null, "Week 17",null,
    		"Week 18",null, "Week 19",null, "Week 20",null,"Week 21",null,"Week 22",null,"Week 23",null,"Week 24",null,"Week 25",null,
    		"Week 26",null,"Week 27",null, "Week 28",null, "Week 29",null, "Week 30",null,"Week 31",null,"Week 32",null,"Week 33",null,
    		"Week 34",null,"Week 35",null,"Week 36",null, "Week 37",null,"Week 38",null, "Week 39",null, "Week 40",null};
    
    public static final String[] SUBTEXTS = {null,null,null,null,null, "For a healthy baby, I take folic acid every day.", 
    	"I'm tired and excited at the same time?  What am I going to do?", null,"My breasts are sore!  Should be better in a few weeks",
    	null,null,"I am so tired.  Afternoon naps are great","Ultrasound\nBC Womens Hospital\nDr. Jennifer Lee","Here is my baby!  OMG!",
    	null,"It's hard to poop.  Need to drink more water and eat some oatmeal","I really want my kid to meet mom and day one day.  I miss them a lot",
    	null,"Ask my doctor about bloodwork and genetic testing.",null,"Feeling nauseous and throwing up. Try to eat smaller meals more often."
    	,null,"Ask my doctor about an early ultrasound",null,"My appetite is starting to come back and I'm hungry!",null,
    	"Going to pee all the time.  It's a normal part of early pregnancy.",null,"Now that my energy is back, I'm going for a walk every day.",
    	null,"It's okay to sleep in any position I want.  Use lots of pillows to get comfortable.",null,"If I want genetic testing, time for my blood test.",
    	null,"Baby is the size of an onion.  Baby is starting to form bones.",null,"Ask my doctor about a detailed ultrasound.",
    	null,"My baby is the size of a banana.  I may start feeling the baby move.",null,"I am halfway there!",null,"Having lots of wild dreams about baby.",
    	null,"My legs are starting to cramp at night.  Time to start  stretching before bed.",null,"My baby is the size of a grapefruit.  Baby's little fingers now have fingerprints.",
    	null,"ask my doctor about testing for pregnancy diabetes",null,"Baby is the size of an ear of corn.  Starting to grow hair!",
    	null,"A walk after dinner will help me get better sleep at night.",null,"I sing to the baby at night.  Baby can hear well now.",
    	null,"My mood Is up and down. I can talk to my doctor if I'm feeling sad.",null,"My baby is the size of a cucumber.  Baby is getting chubby now.",
    	null,"Time to think about meeting a doula",null,"My back is starting to hurt.  Swimming and stretching will help.",null,
    	"I am waking up a lot at night.  Getting ready for baby's late night feeds.",null,"My baby is the size of a pineapple.  Baby's keeping his eyes open while awake",
    	null,"I'm beginning to think about my labour and birth.  Who do I want at the hospital with me?",null,"Ask my doctor about Group B Strep.",
    	null,"Baby's movements are smaller now.  Less room to move around.",null,"Baby's dropped down.  I'm starting to walk funny.",
    	null,"Cramping and contractions are common now.  Getting ready for baby.",null,"My feet and ankles are swollen.  Time for a blood pressure check.",
    	null,"Can't wait to meet my baby."};
    
    public static final String[] PICTURES = {null,null,null,null,null,"baby.jpg",null,null,null,"belly.jpg",null,null,null,"ultrasound.jpg",null,null,null,null,null,null,
    	null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    	null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    	null,null,null,null,null, null, null,null,null,null,null,null, null, null};
    
    public static final String[] TYPES = {null,null,null,null,null,"Info_Text","Diary_entry",null,"Info_Text","Diary_entry",null,"Info_Text",
    	"Appointment","Diary_entry",null,"Info_Text","Diary_entry",null,"Info_Text",null,
    	"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",
    	null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",
    	null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",
    	null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,
    	"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text",null,"Info_Text"};
    
    private static final Integer LIST_HEADER = 0;
    private static final Integer LIST_ITEM = 1;
    
    public static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }
    
    public void TestDueDate() {
        int daysRemaining = countDiffDay(m_curdate, m_duedate);
        
        int daysIn = DAYS_IN_PREGNANCY - daysRemaining;

        int weekValue = daysIn / 7;
        int weekPart = daysIn % 7;
        Calendar lastDate, firstDate;
        
        String week = weekValue + "." + weekPart;
        
        int i=0;
        
        m_weekvalue = weekValue;
        m_dateArray = new String[40][2];
        
        System.out.println("m_curdate = " + m_curdate.getTime() + " m_duedate = " + m_duedate.getTime());
        
        lastDate = (Calendar)m_duedate.clone();
    	firstDate = (Calendar)m_duedate.clone();
    	lastDate.add(Calendar.DATE, 7);
    	
        for (i=40; i>0; i--) {
        	lastDate.add(Calendar.DATE, -7);
        	firstDate.add(Calendar.DATE, -7);
        	System.out.println("fdate = " + firstDate.getTime() + " ldate = " + lastDate.getTime());
        	
        	m_dateArray[i-1][0] = String.valueOf(MONTHS[firstDate.get(Calendar.MONTH)]) + " " + 
        	String.valueOf(firstDate.get(Calendar.DAY_OF_MONTH));
        	m_dateArray[i-1][1] = String.valueOf(MONTHS[lastDate.get(Calendar.MONTH)]) + " " + 
        	String.valueOf(lastDate.get(Calendar.DAY_OF_MONTH));
        }
        
        TextView txtInfo = (TextView)getView().findViewById(R.id.txtInfo);
        
        txtInfo.setText(m_name + "'s pregnancy\n" + " days remaining: " + daysRemaining + " days in: " + daysIn + " week: " + week);
        
    }
    
    public int countDiffDay(Calendar date1, Calendar date2) {
        int returnInt = 0;
        Calendar c1, c2;
        
        c1 = (Calendar)date1.clone();
        c2 = (Calendar)date2.clone();
        
        while (!c1.after(c2)) {
  	      	c1.add(Calendar.DAY_OF_MONTH, 1);
  	      	returnInt++;
        }

        if (returnInt > 0) {
        	returnInt = returnInt - 1;
        }

        return (returnInt);
    }
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {  
    	super.onCreate(savedInstanceState);
        m_btnNote = (ImageButton)getView().findViewById(R.id.btnNote);
        m_btnAppt = (ImageButton)getView().findViewById(R.id.btnAppt);
        
    	ListView lv = (ListView) getView().findViewById(R.id.lstTimeline);
        lv.setAdapter(new MyListAdapter(m_context = getView().getContext()));
        
        m_curdate = Calendar.getInstance();
        m_duedate= Calendar.getInstance();
        m_duedate.set(2014,Calendar.DECEMBER,15);
        m_name="Sev";
    
        TestDueDate();
        
        lv.setSelection(HDR_POS[m_weekvalue]);
        
    }
    
    private class MyListAdapter extends BaseAdapter {
        public MyListAdapter(Context context) {
        	mContext = context;
        }

        @Override
        public int getCount() {
            return LIST.length;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String headerText = getHeader(position);
            int i;
            
            if(headerText != null) {

                View item = convertView;
                if(convertView == null || convertView.getTag() == LIST_ITEM) {

                    item = LayoutInflater.from(mContext).inflate(
                            R.layout.lv_header_layout, parent, false);
                    item.setTag(LIST_HEADER);

                }

                TextView headerTextView = (TextView)item.findViewById(R.id.lv_list_hdr);
                headerTextView.setText(headerText);
                
                for (i=0; i<40; i++) if(HDR_POS[i]==position) break;
                
                TextView headerDate = (TextView)item.findViewById(R.id.lv_list_dt);
                headerDate.setText(m_dateArray[i][0] + " - " + m_dateArray[i][1]);
                
                
                return item;
            }

            View item = convertView;
            if(convertView == null || convertView.getTag() == LIST_HEADER) {
                item = LayoutInflater.from(mContext).inflate(
                        R.layout.lv_layout, parent, false);
                item.setTag(LIST_ITEM);
            }

            TextView header = (TextView)item.findViewById(R.id.lv_item_header);
            header.setText(LIST[position % LIST.length]);   
            
            if (LIST[position]==null) {
            	header.setHeight(0);
            }
            
            TextView subtext = (TextView)item.findViewById(R.id.lv_item_subtext);
            subtext.setText(SUBTEXTS[position % SUBTEXTS.length]);
            
            if (SUBTEXTS[position]==null) {
            	subtext.setHeight(0);
            }
            
            ImageView imgView = (ImageView) item.findViewById(R.id.imgicon);
            
            if (TYPES[position] != null) {
            	Map<String, Integer> map = new HashMap<String, Integer>();
            	
	            if (TYPES[position].equalsIgnoreCase("Info_Text")) map.put("img", R.drawable.timeline_chat_icon);
	            if (TYPES[position].equalsIgnoreCase("Diary_entry")) map.put("img", R.drawable.timeline_entry_icon);
	            if (TYPES[position].equalsIgnoreCase("Appointment")) map.put("img", R.drawable.timeline_appt_icon);
	            if (TYPES[position] == null) map.put("img", R.drawable.questions_icon);
	            
	            imgView.setImageResource(map.get("img"));
            }
          
            ImageView picView = (ImageView) item.findViewById(R.id.event_photo);
            picView.setImageResource(android.R.color.transparent);
            
            if (PICTURES[position] != null) {
            	String pictureURI = PICTURES[position];
	            String substr=pictureURI.substring(0, pictureURI.indexOf("."));
	            
	            picView.setImageResource(getResources().getIdentifier(substr, "drawable", m_context.getPackageName()));
            }
          
            m_btnNote.bringToFront();
            m_btnAppt.bringToFront();
            
            return item;
        }

        private String getHeader(int position) {
        	int i;
        	for (i=0;i<40;i++) {
	            if(position == HDR_POS[i]) {
	                return LIST[position];
	            }
        	}
            return null;
        }

        private final Context mContext;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
