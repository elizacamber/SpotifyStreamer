package com.gdgthess.liz.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

/**
 * Created by eliza on 25/9/2015.
 */
public class PlayerService extends Service implements MediaPlayer.OnPreparedListener {

    MediaPlayer mediaPlayer = null;
    final IBinder mBinder = new PlayerBinder();

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url= intent.getStringExtra("URL");
        mediaPlayer= new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        if (!mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
            } catch (Exception e) {}
        }
        return Service.START_NOT_STICKY;
    }

    public void changeTrack(String url){
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (!mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
            } catch (Exception e) {}
        }
    }

    public boolean mPausePlay(int position) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            return false;
        } else {
            mediaPlayer.seekTo(position);
            mediaPlayer.start();
            return true;
        }
    }

    public int currentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int maxDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekPosition(int position) {
        mediaPlayer.seekTo(position);
        mediaPlayer.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }
}
