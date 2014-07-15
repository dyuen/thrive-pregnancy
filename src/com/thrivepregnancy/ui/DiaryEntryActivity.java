package com.thrivepregnancy.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.MediaRecorder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
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
public class DiaryEntryActivity extends BaseActivity{
	
	// Audio file name format
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
	
	// Keys for saved state
	private static final String KEY_AUDIO_STATE 	= "audioState";
	private static final String KEY_AUDIO_FILE_NAME = "audioFileName";
	
	private static MediaRecorder 	recorder;
//	private String 					audioFilePath;
	private ViewGroup				areaNone;
	private ViewGroup				areaRecording;
	private ViewGroup				areaFinished;
	private ImageButton				start;
	private ImageButton				delete;
	private Button					done;
	private Button					cancel;
	private AudioPlayer				audioPlayer;
	
	private enum AudioState {
		NONE,		// No recording or playback underway 
		RECORDING,	// Recording 
		FINISHED,	// Finished recording 
		}
	private AudioState audioState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(MainActivity.DEBUG_TAG, "*** onCreate");
    	LayoutInflater inflater = getLayoutInflater();
    	LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.activity_diaryentry, null);
    	setContentView(layout);
    	StartUp(Event.Type.DIARY_ENTRY);
    	SetViews();
    	FillViews(layout);
    	    	
    	setUpAudio(savedInstanceState);
    }
    
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);
	    // Save state across screen rotation
	    savedInstanceState.putString(KEY_AUDIO_STATE, audioState.toString());
	    if (m_event.getAudioFile() != null){
		    savedInstanceState.putString(KEY_AUDIO_FILE_NAME, m_event.getAudioFile());	    	
	    }
	}
	
	@Override
	public Object onRetainCustomNonConfigurationInstance(){
	    if (audioState.equals(AudioState.FINISHED)){
	    	return audioPlayer;
	    }
		return null;
	}

	private void setUpAudio(Bundle savedInstanceState){
		// The three mutually exclusive views corresponding to the recording state
		areaNone = (ViewGroup)findViewById(R.id.audio_area_none);
		areaRecording = (ViewGroup)findViewById(R.id.audio_area_recording);
		areaFinished = (ViewGroup)findViewById(R.id.audio_area_finished);		

		if (savedInstanceState == null){
			String fileName = m_event.getAudioFile();
    		if (fileName != null && fileName.length() > 0){
				setState(AudioState.FINISHED);
			}
			else {
				setState(AudioState.NONE);	
			}
		}
		else {
			setState(AudioState.valueOf(savedInstanceState.getString(KEY_AUDIO_STATE)));
			if (audioState.equals(AudioState.FINISHED)){
				Log.d(MainActivity.DEBUG_TAG, "Restoring previous AudioPlayer");
				audioPlayer = (AudioPlayer)getLastCustomNonConfigurationInstance ();
				audioPlayer.restore(this, areaFinished);
			}
			if (savedInstanceState.containsKey(KEY_AUDIO_FILE_NAME)){
				m_event.setAudioFile(savedInstanceState.getString(KEY_AUDIO_FILE_NAME));
			}
		}
    	setupAudioButtons();
    }
	
	// Set recording state and show corresponding view
	private void setState(AudioState newState){
		audioState = newState;
		
		areaNone.setVisibility(audioState.equals(AudioState.NONE) ? View.VISIBLE : View.GONE);
		areaRecording.setVisibility(audioState.equals(AudioState.RECORDING) ? View.VISIBLE : View.GONE);
		areaFinished.setVisibility(audioState.equals(AudioState.FINISHED) ? View.VISIBLE : View.GONE);
		
		if (audioState.equals(AudioState.FINISHED)){
			Log.d(MainActivity.DEBUG_TAG, "Creating new AudioPlayer");
	    	audioPlayer = new AudioPlayer(this, areaFinished, m_event.getAudioFile());
		}
	}
	
	private void setupAudioButtons(){
		start 			= (ImageButton)findViewById(R.id.audio_record_start);
		delete 			= (ImageButton)findViewById(R.id.audio_delete);
		done 			= (Button)findViewById(R.id.audio_record_done);
		cancel 			= (Button)findViewById(R.id.audio_record_cancel);
		
		// Start recording
		start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setState(AudioState.RECORDING);
				startRecording();
			}
		});
		// Finish recording 
		done.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Log.d(MainActivity.DEBUG_TAG, "Releasing MediaRecorder");
				recorder.stop();
				recorder.release();
				
				File permanentFile = new File(getExternalFilesDir(null), formatter.format(new Date()) + "mpg");
				File temporaryFile = getTemporaryAudioFile();
				if (temporaryFile.exists()){
					if (temporaryFile.renameTo(permanentFile)){
						m_event.setAudioFile(permanentFile.getAbsolutePath());
					}
				}
				setState(AudioState.FINISHED);
			}
		});
		// Cancel recording
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Log.d(MainActivity.DEBUG_TAG, "Releasing MediaRecorder");
				recorder.stop();
				recorder.release();
				File temporaryFile = getTemporaryAudioFile();
				if (temporaryFile.exists()){
					if (!temporaryFile.delete()){
						Log.e(MainActivity.DEBUG_TAG, "********** Can't delete " + temporaryFile.getAbsolutePath());
					}
				}
				setState(AudioState.NONE);
			}
		});
		// Delete recording
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Deletion can occur when player is playing or stopped
				audioPlayer.stop();
				String fileName = m_event.getAudioFile();
	    		if (fileName != null && fileName.length() > 0){
	    			File file = new File(fileName);
	    			if (!file.delete()){ 
	    				Log.e(MainActivity.DEBUG_TAG, "********** Can't delete " + fileName);
	    			}
	    		}
    			setState(AudioState.NONE);
			}
		});
		
	}
	
	private File getTemporaryAudioFile(){
		return new File(getExternalFilesDir(null), "$$$.mpg");
	}
	
	private void startRecording(){
        getTemporaryAudioFile();
//        Log.d(MainActivity.DEBUG_TAG, "Creating new MediaR");
        if (recorder != null){
        	recorder.release();
        }
        recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setOutputFile(getTemporaryAudioFile().getAbsolutePath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        
        try {
        	recorder.prepare();
        	recorder.start();
        }
        catch (IOException e){
        	Log.e(MainActivity.DEBUG_TAG, "Can't record audio", e);
        }
	}
	
	/**
     * These views the responsibility of the superclass
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
