package com.thrivepregnancy.ui;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.thrivepregnancy.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
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
	private final String 	audioFile;
	private boolean			playing;
	private ImageButton		playStartStop;
	private ProgressBar		progressBar;
	private TextView		elapsed;
	private MediaPlayer		mediaPlayer;
	private Calendar		counter;
	private Activity 		activity;
	private int 			secondsPlayed;
	private static SimpleDateFormat formatter = new SimpleDateFormat("m:ss"); 
	
	/**
	 * Constructor
	 * @param audioFile full path of the audio file
	 * @param player the ViewGroup containing a start/stop button, progress bar, and second counter
	 */
	public AudioPlayer(Activity activity, ViewGroup player, final String audioFile){
		this.audioFile = audioFile;
		this.activity = activity;
		Log.d(MainActivity.DEBUG_TAG, "New AudioPlayer for " + audioFile);
		playStartStop = (ImageButton)player.findViewById(R.id.audio_playback_start_stop);
		progressBar = (ProgressBar)player.findViewById(R.id.audio_playback_progress);
		elapsed = (TextView)player.findViewById(R.id.audio_playback_time);
		
		playStartStop.setImageResource(R.drawable.ic_play);
		playStartStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (playing){
					playStartStop.setImageResource(R.drawable.ic_play);
					stop();
				}
				else{
					playStartStop.setImageResource(R.drawable.ic_pause);
					start();
				}
			}
		});
	}
	
	/**
	 * Stops playback, disposes of the MediaPlayer, and resets the UI
	 */
	public void stop(){		
		playing = false;
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
	}
	
	/**
	 * Restores the UI state after a rotation. This object and its MediaPlayer were retained, but
	 * the UI objects will have been recreated
	 */
	public void restore(Activity activity, ViewGroup player){
		this.activity = activity;
		playStartStop = (ImageButton)player.findViewById(R.id.audio_playback_start_stop);
		progressBar = (ProgressBar)player.findViewById(R.id.audio_playback_progress);
		elapsed = (TextView)player.findViewById(R.id.audio_playback_time);
		
	}
	/**
	 * Creates a MediaPlayer and starts it
	 * @param audioFilePath
	 */
	private void start(){
		playing = true;
		mediaPlayer = new MediaPlayer ();
		Log.d(MainActivity.DEBUG_TAG, "Created MediaPlayer");
		try {
			URL url;
			mediaPlayer.setDataSource(audioFile);
			mediaPlayer.setOnCompletionListener(new OnCompletionListener(){
				@Override
				public void onCompletion(MediaPlayer mp){
					stop();
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
					// Report errors back to PlayerActivity
					mp.release();
					return true;
				}
			});
			mediaPlayer.setOnPreparedListener(new OnPreparedListener(){
				@Override
				public void onPrepared(MediaPlayer mp){
					int duration = mp.getDuration() / 1000;
					Log.d(MainActivity.DEBUG_TAG, "Duration = " + duration);
					progressBar.setMax(duration);
					mp.start();
					counter = Calendar.getInstance();
					counter.clear();
					counter.set(Calendar.MINUTE, 0);
					counter.set(Calendar.SECOND, 0);
					Thread thread = new Thread(new Runnable(){
						public void run(){
							while (playing){
								activity.runOnUiThread(new Runnable(){
									public void run(){
										elapsed.setText(formatter.format(counter.getTime()));
										progressBar.setProgress(secondsPlayed);
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
					thread.start();
				}
			});
			mediaPlayer.prepareAsync();
		}
		catch (IOException e){
			Log.e(MainActivity.DEBUG_TAG, "Error preparing audio playback", e);
		}
	}
	
}
