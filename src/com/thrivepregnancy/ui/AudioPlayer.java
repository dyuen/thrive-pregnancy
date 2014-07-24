package com.thrivepregnancy.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.thrivepregnancy.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Controls audio playback and the state of the audio player UI
 */
public class AudioPlayer {

	private static SimpleDateFormat formatter = new SimpleDateFormat("m:ss", Locale.CANADA); 
	private final static int MAX_VOLUME = 100;

	private final String 	audioFile;
	private boolean			playing;
	private ImageButton		playStartStop;
	private ProgressBar		progressBar;
	private TextView		elapsed;
	private MediaPlayer		mediaPlayer;
	private Thread 			timelineThread;
	private Calendar		counter;
	private Activity 		activity;
	private int 			secondsPlayed;
	private PlayerClient 	playerClient;
	private boolean			rotating;

	/**
	 * Optional interface for a client object. It allows an AudioPlayer to stop another
	 * instance from playing (e.g. multiple recordings on the timeline)
	 */
	public interface PlayerClient {
		public AudioPlayer getActiveAudioPlayer();
		public void setActiveAudioPlayer(AudioPlayer player);
		public void playerStarted();
		public void playerStopped();
	}	
	
	/**
	 * Constructor
	 * @param activity the parentActivity
	 * @param playerView a viewgroup containing an image button, progress bar, and text field 
	 * @param audioFile audio file to be played
	 */
	public AudioPlayer(Activity activity, ViewGroup playerView, final String audioFile){
		this(activity, playerView, audioFile, null);
	}
	
	/**
	 * Constructor
	 * @param activity the parentActivity
	 * @param playerView a viewgroup containing an image button, progress bar, and text field 
	 * @param audioFile audio file to be played
	 * @param playerClient optional
	 */
	public AudioPlayer(Activity activity, ViewGroup playerView, final String audioFile, PlayerClient playerClient){
		this.audioFile = audioFile;
		this.activity = activity;
		this.playerClient = playerClient;
		
//		Log.d(MainActivity.DEBUG_TAG, "New AudioPlayer for " + audioFile);
		playStartStop = (ImageButton)playerView.findViewById(R.id.audio_playback_start_stop);
		progressBar = (ProgressBar)playerView.findViewById(R.id.audio_playback_progress);
		elapsed = (TextView)playerView.findViewById(R.id.audio_playback_time);
		
		initializeStartStop();
	}
	
	private void initializeStartStop(){
		if (playing){
			playStartStop.setImageResource(R.drawable.ic_pause);
		}
		else {
			playStartStop.setImageResource(R.drawable.ic_play);
		}
		playStartStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (playing){
					playStartStop.setImageResource(R.drawable.ic_play);
					stop();
					if (playerClient != null){
						playerClient.playerStopped();
					}
				}
				else{
					playStartStop.setImageResource(R.drawable.ic_pause);
					start();
					if (playerClient != null){
						playerClient.playerStarted();
					}
				}
			}
		});
	}
	
	public boolean isPlaying(){
		return playing;
	}
	
	public boolean isPlaying(String anAudioFile){
		return anAudioFile.equals(this.audioFile);
	}
	
	/**
	 * Stops playback, disposes of the MediaPlayer, and resets the UI
	 */
	public void stop(){		
		if (mediaPlayer != null){
			if (mediaPlayer.isPlaying()){
				mediaPlayer.stop();
			}
			mediaPlayer.release();
			mediaPlayer = null;
			Log.d(MainActivity.DEBUG_TAG, "Destroyed MediaPlayer");
		}
		secondsPlayed = 0;
		progressBar.setProgress(0);
		elapsed.setText("");
		playStartStop.setImageResource(R.drawable.ic_play);
		playing = false;
		if (playerClient != null){
			playerClient.setActiveAudioPlayer(null);
		}
	}
	
	/**
	 * Restores the UI state after a rotation. This object and its MediaPlayer were retained, but
	 * the UI objects will have been recreated
	 */
	public void restore(Activity activity, ViewGroup playerView){
		this.activity = activity;
		playStartStop = (ImageButton)playerView.findViewById(R.id.audio_playback_start_stop);
		initializeStartStop();
		progressBar = (ProgressBar)playerView.findViewById(R.id.audio_playback_progress);
		elapsed = (TextView)playerView.findViewById(R.id.audio_playback_time);
		rotating = true;
	}
	
	/**
	 * Creates a MediaPlayer and starts playing the file
	 * @param audioFilePath
	 */
	private void start(){
		// Stop any other active player
		if (playerClient != null){
			AudioPlayer activeAudioPlayer = playerClient.getActiveAudioPlayer();
			if (activeAudioPlayer != null){
				activeAudioPlayer.stop();
			}
			playerClient.setActiveAudioPlayer(this);
		}
		playing = true;
		if (mediaPlayer != null){
			stop();
		}
		
		mediaPlayer = new MediaPlayer ();
//		Log.d(MainActivity.DEBUG_TAG, "Created MediaPlayer");
		try {
			mediaPlayer.setDataSource(audioFile);
			mediaPlayer.setOnCompletionListener(new OnCompletionListener(){
				@Override
				public void onCompletion(MediaPlayer mp){
					stop();
					if (playerClient != null){
						playerClient.playerStopped();
					}

				}
			});
			mediaPlayer.setOnErrorListener(new OnErrorListener(){
				@Override
				public boolean onError (MediaPlayer mp, int what, int extra){
					/*
					-1004 MEDIA_ERROR_IO Added in API level 17
					-1007 MEDIA_ERROR_MALFORMED Added in API level 17
			  		  200 MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK
			  		  100 MEDIA_ERROR_SERVER_DIED
			 		 -110 MEDIA_ERROR_TIMED_OUT Added in API level 17
			    	    1 MEDIA_ERROR_UNKNOWN
					-1010 MEDIA_ERROR_UNSUPPORTED Added in API level 17
					*/
					Log.e(MainActivity.DEBUG_TAG, "MediaPlayer error = " + what + " : " + extra);
					// TODO: Report errors back to PlayerActivity?
					mp.release();
					return true;
				}
			});
			mediaPlayer.setOnPreparedListener(new OnPreparedListener(){
				@Override
				public void onPrepared(MediaPlayer mp){
					final int duration = mp.getDuration() / 1000;
					Log.d(MainActivity.DEBUG_TAG, "Duration = " + duration);
					progressBar.setMax(duration);
					
					//volume control
					final float volume = (float) (1 - (Math.log(MAX_VOLUME - 100) / Math.log(MAX_VOLUME)));
					mp.setVolume(volume, volume);
					
					mp.start();
					
					// Initialize the progress bar and elapsed time counter
					counter = Calendar.getInstance();
					counter.clear();
					counter.set(Calendar.MINUTE, 0);
					counter.set(Calendar.SECOND, 0);
					timelineThread = new Thread(new Runnable(){
						public void run(){
							while (playing){
								if (rotating){
									rotating = false;
									progressBar.setMax(duration);
								}
								activity.runOnUiThread(new Runnable(){
									public void run(){
										elapsed.setText(formatter.format(counter.getTime()));
										progressBar.setProgress(secondsPlayed - 1);
									}
								});
								secondsPlayed++;
								counter.add(Calendar.SECOND, 1);
								try {
									Thread.currentThread().sleep(1000);
								}
								catch (InterruptedException e){}
							}
						}
					});
					timelineThread.start();
				}
			});
			
			mediaPlayer.prepareAsync();
		}
		catch (IOException e){
			Log.e(MainActivity.DEBUG_TAG, "Error preparing audio playback", e);
		}
	}
	
}
