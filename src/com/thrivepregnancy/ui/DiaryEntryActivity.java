package com.thrivepregnancy.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaRecorder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
public class DiaryEntryActivity extends BaseActivity implements AudioPlayer.PlayerClient{
	
	// Audio file name format
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA);
	
	// Keys for saved state
	private static final String KEY_AUDIO_STATE 	= "audioState";
	private static final String KEY_AUDIO_FILE_NAME = "audioFileName";

	private static final String AnimationDrawable = null;
	
	private static MediaRecorder 	recorder;
	private ViewGroup				areaNone;
	private ViewGroup				areaRecording;
	private ViewGroup				areaFinished;
	private ImageButton				start;
	private ImageButton				delete;
	private Button					done;
	private Button					cancel;
	private AudioPlayer				audioPlayer;
	private File					audioFile;
	private LinearLayout 			layout;
	
	private enum AudioState {
		NONE,		// No recording or playback underway 
		RECORDING,	// Recording 
		FINISHED,	// Finished recording 
		}
	private AudioState audioState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	LayoutInflater inflater = getLayoutInflater();
    	layout = (LinearLayout)inflater.inflate(R.layout.activity_diaryentry, null);
    	setContentView(layout);
    	StartUp(Event.Type.DIARY_ENTRY);
    	SetViews();
    	FillViews(layout);
    	audioPlayer = (AudioPlayer)getLastCustomNonConfigurationInstance();
    	    	
    	setUpAudio(savedInstanceState);
    }
    
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);
	    // Save state across screen rotation
	    savedInstanceState.putString(KEY_AUDIO_STATE, audioState.toString());	    
	    if (m_audioFileName != null){
		    savedInstanceState.putString(KEY_AUDIO_FILE_NAME, m_audioFileName);	    	
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
			m_audioFileName = m_event.getAudioFile();
    		if (m_audioFileName != null && m_audioFileName.length() > 0){
    			setState(AudioState.FINISHED);
			}
			else {
				setState(AudioState.NONE);	
			}
		}
		else {
			setState(AudioState.valueOf(savedInstanceState.getString(KEY_AUDIO_STATE)));
			if (audioState.equals(AudioState.FINISHED)){
				if (audioPlayer != null){
					audioPlayer.restore(this, areaFinished);
				}
			}
			if (savedInstanceState.containsKey(KEY_AUDIO_FILE_NAME)){
				m_audioFileName = savedInstanceState.getString(KEY_AUDIO_FILE_NAME);
			}
		}
    	setupAudioButtons();
    }
	
	// Set recording state and show corresponding view. Disable other UI elements when recording
	private void setState(AudioState newState){
		audioState = newState;
		
		areaNone.setVisibility(audioState.equals(AudioState.NONE) ? View.VISIBLE : View.GONE);
		areaRecording.setVisibility(audioState.equals(AudioState.RECORDING) ? View.VISIBLE : View.GONE);
		areaFinished.setVisibility(audioState.equals(AudioState.FINISHED) ? View.VISIBLE : View.GONE);
		
		if (audioState.equals(AudioState.RECORDING)){
			findViewById(R.id.diary_date).setEnabled(false);
			findViewById(R.id.diary_notes).setEnabled(false);
			findViewById(R.id.diary_delete).setEnabled(false);
			findViewById(R.id.diary_create).setEnabled(false);
			findViewById(R.id.diary_create_text).setEnabled(false);
			ImageView recordingInProgress = (ImageView) findViewById(R.id.recording_in_progress);
			((AnimationDrawable)recordingInProgress.getDrawable()).start();
		}
		else {
			findViewById(R.id.diary_date).setEnabled(true);
			findViewById(R.id.diary_notes).setEnabled(true);
			findViewById(R.id.diary_delete).setEnabled(true);
			findViewById(R.id.diary_create).setEnabled(true);
			findViewById(R.id.diary_create_text).setEnabled(true);
			if (audioState.equals(AudioState.FINISHED)){
				if (audioPlayer == null){
					audioPlayer = new AudioPlayer(this, areaFinished, m_audioFileName, this);
				}
			}
			else if (audioState.equals(AudioState.NONE)){
				m_audioFileName = null;
			}
			ImageView recordingInProgress = (ImageView) findViewById(R.id.recording_in_progress);
			((AnimationDrawable)recordingInProgress.getDrawable()).stop();
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
				recorder.stop();
				recorder.release();
				
				audioFile = new File(getExternalFilesDir(null), formatter.format(new Date()) + "mpg");
				File temporaryFile = getTemporaryAudioFile();
				if (temporaryFile.exists()){
					if (temporaryFile.renameTo(audioFile)){
						m_audioFileName = audioFile.getAbsolutePath();
					}
				}
				setState(AudioState.FINISHED);
			}
		});
		// Cancel recording
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				recorder.stop();
				recorder.release();
				File temporaryFile = getTemporaryAudioFile();
				if (temporaryFile.exists()){
					if (!temporaryFile.delete()){
						//Log.e(MainActivity.DEBUG_TAG, "********** Can't delete " + temporaryFile.getAbsolutePath());
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
				audioPlayer = null;
				m_audioFileName = null;
    			setState(AudioState.NONE);
			}
		});		
	}
	
	private File getTemporaryAudioFile(){
		return new File(getExternalFilesDir(null), "$$$.mpg");
	}
	
	private void startRecording(){
        getTemporaryAudioFile();
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
        	//Log.e(MainActivity.DEBUG_TAG, "Can't record audio", e);
        }
	}
	/** Methods of the AudioPlayer.PlayerClient interface ***********/
	public AudioPlayer getActiveAudioPlayer(){return null;}
	public void setActiveAudioPlayer(AudioPlayer player){}
	public void playerStarted(){
		findViewById(R.id.diary_date).setEnabled(false);
		findViewById(R.id.diary_notes).setEnabled(false);
		findViewById(R.id.diary_delete).setEnabled(false);
		findViewById(R.id.diary_create).setEnabled(false);
		findViewById(R.id.diary_create_text).setEnabled(false);		
	}
	public void playerStopped(){
		findViewById(R.id.diary_date).setEnabled(true);
		findViewById(R.id.diary_notes).setEnabled(true);
		findViewById(R.id.diary_delete).setEnabled(true);
		findViewById(R.id.diary_create).setEnabled(true);
		findViewById(R.id.diary_create_text).setEnabled(true);		
     	InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	inputManager.showSoftInput(layout, InputMethodManager.SHOW_IMPLICIT);
	}
	/****************************************************************/
	
	protected boolean SaveEvent(){
		m_event.setAudioFile(m_audioFileName);
		if (m_audioFileName == null && audioFile != null){
			if (!audioFile.delete()){ 
				//Log.e(MainActivity.DEBUG_TAG, "********** Can't delete " + m_audioFileName);
			}
		}
		if (audioPlayer != null && audioPlayer.isPlaying()){
			audioPlayer.stop();
		}
		return super.SaveEvent();
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
