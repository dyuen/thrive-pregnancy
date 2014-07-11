package com.thrivepregnancy.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
public class DiaryEntryActivity extends BaseActivity {
	
	private static MediaRecorder 	m_Recorder;
	private TextView 		m_AudioInstructions;
	private String 			m_AudioFilePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
    	LayoutInflater inflater = getLayoutInflater();
    	
    	LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.activity_diaryentry, null);
    	
    	setContentView(layout);
    	
    	StartUp(Event.Type.DIARY_ENTRY);
    	
    	SetViews();
    	
    	FillViews(layout);
    	
    	setUpAudio();
    	
    }
    
    private void setUpAudio(){
    	m_AudioInstructions = (TextView)findViewById(R.id.diary_create_audio_instructions);

    	ImageButton m_audioButton = (ImageButton)findViewById(R.id.diary_create_audio_button);
    	m_audioButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (m_AudioInstructions.getVisibility() == View.GONE){
					// Show instructions
					m_AudioInstructions.setVisibility(View.VISIBLE);					
					// Start sound recording
					m_Recorder = new MediaRecorder();
					m_Recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					m_Recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mpg";
			        File file = new File(getExternalFilesDir(null), fileName);
			        m_AudioFilePath = file.getAbsolutePath();
			        m_Recorder.setOutputFile(m_AudioFilePath);
			        m_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			        try {
			        	m_Recorder.prepare();
			        	m_Recorder.start();
			        }
			        catch (IOException e){
			        	Log.e(MainActivity.DEBUG_TAG, "Can't record audio", e);
			        }
				}
				else {
					// Hide instructions
					m_AudioInstructions.setVisibility(View.GONE);
					m_Recorder.stop();
					m_Recorder.release();
				   	m_event.setAudioFile(m_AudioFilePath);
				}
			}
		});
     }
    
    /**
     * References views from layout
     */
    private void SetViews() {
    	set_dateView((EditText)findViewById(R.id.diary_date));
    	set_noteView((EditText)findViewById(R.id.diary_notes));
    	set_warnView((TextView)findViewById(R.id.diary_warning));
    	set_warning(getString(R.string.Diary_Warning));
    	
    	set_buttonDelete((ImageButton)findViewById(R.id.diary_delete));
    	set_buttonCreate((ImageButton)findViewById(R.id.diary_create));
    	
    	set_photoText((TextView)findViewById(R.id.diary_create_text));
    	set_photoView((ImageView)findViewById(R.id.diary_image));
    }
}
