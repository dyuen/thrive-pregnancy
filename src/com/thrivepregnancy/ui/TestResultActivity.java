package com.thrivepregnancy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thrivepregnancy.R;
import com.thrivepregnancy.data.Event;

/**
 * Activity for creating or editing a DIARY_ENTRY event
 */
public class TestResultActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
    	LayoutInflater inflater = getLayoutInflater();
    	
    	LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.activity_testresult, null);
    	
    	setContentView(layout);
    	
    	StartUp(Event.Type.TEST_RESULT);
    	
    	SetViews();
    	
    	FillViews(layout);
    }
    
    /**
     * References views from layout
     */
    private void SetViews() {
    	set_dateView((EditText)findViewById(R.id.test_date));
    	set_noteView((EditText)findViewById(R.id.test_notes));
    	set_warnView((TextView)findViewById(R.id.test_warning));
    	set_warning(getString(R.string.Test_Warning));
    	
    	set_buttonDelete((ImageButton)findViewById(R.id.test_delete));
    	set_buttonCreate((ImageButton)findViewById(R.id.test_create));
    	
    	set_photoText((TextView)findViewById(R.id.test_create_text));
    	set_photoView((ImageView)findViewById(R.id.test_image));
    }
}
