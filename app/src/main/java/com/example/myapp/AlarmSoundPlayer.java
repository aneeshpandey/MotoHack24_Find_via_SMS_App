package com.example.myapp;

import android.content.Context;
import android.media.MediaPlayer;

public class AlarmSoundPlayer {
    private MediaPlayer mediaPlayer;

    public void playAlarmSound(Context context) {
        // Initialize MediaPlayer with an alarm sound file in the raw folder
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound); // Replace with your file name
        mediaPlayer.setLooping(true); // Set looping to true if you want it to continue until stopped
        mediaPlayer.start(); // Start the alarm sound
    }

    public void stopAlarmSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
