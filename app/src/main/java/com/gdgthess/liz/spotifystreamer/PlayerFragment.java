package com.gdgthess.liz.spotifystreamer;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlayerFragment extends DialogFragment{

    ImageView songImage;
    TextView trackName , trackAlbum, trackArtist, trackElapsedTime, trackLeftTime;
    ImageButton previous , play , next;
    int selected;
    List<Track> trackList;
    public static MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    public static String RESTORE_CURRENT_TRACK = "restore_current_track";

    public PlayerFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(RESTORE_CURRENT_TRACK, selected);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Top10Fragment fragment = (Top10Fragment) getFragmentManager().findFragmentById(R.id.fragmentTop10);
        trackList = fragment.trackList;

        mediaPlayer = new MediaPlayer();

        View view = getView();
        if(view != null)  {

            trackName = (TextView) getView().findViewById(R.id.trackNameTxt);
            trackAlbum = (TextView) getView().findViewById(R.id.trackAlbumTxt);
            trackArtist = (TextView) getView().findViewById(R.id.trackArtistTxt);
            trackElapsedTime= (TextView) getView().findViewById(R.id.timeElapsedTxt);
            trackLeftTime= (TextView) getView().findViewById(R.id.timeLeftTxt);

            songImage = (ImageView) getView().findViewById(R.id.songImage);
            previous = (ImageButton) getView().findViewById(R.id.prevBtn);
            play = (ImageButton) getView().findViewById(R.id.playBtn);
            next = (ImageButton) getView().findViewById(R.id.nextBtn);
            seekBar = (SeekBar) getView().findViewById(R.id.trackTimeSeekbar);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            });

            if (savedInstanceState==null) {
                selected = Top10Fragment.getSelectedTrack();
            }else
                selected = savedInstanceState.getInt(RESTORE_CURRENT_TRACK);
            preparePlayer(selected);
            updateSeekBar();

            previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("Previous Track","URL");

                    if(selected > 0){
                        if(mediaPlayer.isPlaying())mediaPlayer.stop();
                        preparePlayer(--selected);
                        play.setImageResource(android.R.drawable.ic_media_play);
                        Toast.makeText(getActivity(), "Loading Previous track", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("Play / Pause","URL");
                    if(mediaPlayer.isPlaying()){
                        play.setImageResource(android.R.drawable.ic_media_play);
                        mediaPlayer.pause();
                    }
                    else{
                        play.setImageResource(android.R.drawable.ic_media_pause);
                        mediaPlayer.start();
                    }
                }
            });

            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("Next Track", "URL");

                    if(selected <= trackList.size()-2) {
                        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                        preparePlayer(++selected);
                        play.setImageResource(android.R.drawable.ic_media_play);
                        Toast.makeText(getActivity(), "Loading Next track", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();

        mediaPlayer.stop();
    }

    public void preparePlayer(int selected){

        Log.i("Opening track",selected+"");
        if(selected == 0){
            previous.setClickable(false);
            next.setClickable(true);
            previous.setVisibility(View.INVISIBLE);
            next.setVisibility(View.VISIBLE);
        }
        else if(selected == trackList.size() - 1){
            next.setClickable(false);
            previous.setClickable(true);
            next.setVisibility(View.INVISIBLE);
            previous.setVisibility(View.VISIBLE);
        }
        else{
            previous.setClickable(true);
            next.setClickable(true);
            previous.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
        }


        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();
        Picasso.with(getActivity().getBaseContext()).load(trackList.get(selected).album.images.get(0).url).into(songImage);

        String url = trackList.get(selected).preview_url; // your URL here
        trackName.setText(trackList.get(selected).name);
        trackAlbum.setText(trackList.get(selected).album.name);

        String artists = "";
        for(int i=0; i<trackList.get(selected).artists.size(); i++)artists+=trackList.get(selected).artists.get(i).name+" , ";
        artists = artists.substring(0, artists.length() - 2);
        trackArtist.setText(artists);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
            seekBar.setMax(mediaPlayer.getDuration());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateSeekBar() {

        // Worker thread that will update the seekbar as each song is playing
        Thread t = new Thread() {
            Handler handler = new Handler();

            @Override
            public void run() {

                int total = (int) mediaPlayer.getDuration();
                seekBar.setMax(total);
                while (mediaPlayer!= null
                        && mediaPlayer.getCurrentPosition() < total) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    } catch (Exception e) {
                        return;
                    }

                    if(mediaPlayer != null) seekBar.setProgress(mediaPlayer.getCurrentPosition());

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            trackElapsedTime.setText("0:" + mediaPlayer.getCurrentPosition() / 1000);
                            trackLeftTime.setText("0:"+(mediaPlayer.getDuration()-mediaPlayer.getCurrentPosition())/1000);
                        }
                    });

                }

            }

        };
        t.start();
    }
}