package com.gdgthess.liz.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class PlayerFragment extends DialogFragment {

    ImageView songImage;
    TextView trackName, trackAlbum, trackArtist, trackElapsedTime, trackLeftTime;
    ImageButton previous,  next, play_pause;
    int selected, position;
    List<Track> trackList;
    String url;
    private SeekBar seekBar;
    boolean bounded;
    PlayerService service;
    Handler handler= new Handler();
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

    public ServiceConnection connection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.PlayerBinder mBinder= (PlayerService.PlayerBinder) iBinder;
            service = mBinder.getService();
            bounded= true;
            updateSeekbar();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("DISCONNECT", "DISCONNECTED");
            bounded = false;
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Top10Fragment fragment = (Top10Fragment) getFragmentManager().findFragmentById(R.id.fragmentTop10);
        trackList = fragment.trackList;
        initUI();

        if(savedInstanceState==null) {
            // Stop service if it exists
            try {
                getActivity().stopService(new Intent(getActivity(), PlayerService.class));
            } catch (Exception e) {}

            selected = Top10Fragment.getSelectedTrack();
            url = trackList.get(selected).preview_url;
            Intent i = new Intent(getActivity(), PlayerService.class);
            i.putExtra("URL", url);
            getActivity().startService(i);
            getActivity().bindService(i, connection, Context.BIND_AUTO_CREATE);
        }else{
            selected = savedInstanceState.getInt(RESTORE_CURRENT_TRACK);
        }
        getActivity().bindService(new Intent(getActivity(), PlayerService.class), connection, Context.BIND_AUTO_CREATE);
        setLayout(selected);

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Toast.makeText(getActivity(), "Loading Previous track", Toast.LENGTH_SHORT).show();
                    selected = --selected;
                    changeTrack(selected);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Toast.makeText(getActivity(), "Loading Next track", Toast.LENGTH_SHORT).show();
                    selected = ++selected;
                    changeTrack(selected);
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTrack();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) service.seekPosition(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(RESTORE_CURRENT_TRACK, selected);
    }

    public void pauseTrack(){
        if (!bounded) return;
        if (service.mPausePlay(position)) {
            play_pause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            play_pause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    public void changeTrack(int position){
        setLayout(selected);
        service.changeTrack(trackList.get(selected).preview_url);
    }

    public void initUI() {
        View view = getView();
        if (view != null) {
            trackName = (TextView) getView().findViewById(R.id.trackNameTxt);
            trackAlbum = (TextView) getView().findViewById(R.id.trackAlbumTxt);
            trackArtist = (TextView) getView().findViewById(R.id.trackArtistTxt);
            trackElapsedTime = (TextView) getView().findViewById(R.id.timeElapsedTxt);
            trackLeftTime = (TextView) getView().findViewById(R.id.timeLeftTxt);

            songImage = (ImageView) getView().findViewById(R.id.songImage);
            previous = (ImageButton) getView().findViewById(R.id.prevBtn);
            play_pause= (ImageButton) getView().findViewById(R.id.playPauseButton);
            next = (ImageButton) getView().findViewById(R.id.nextBtn);
            seekBar = (SeekBar) getView().findViewById(R.id.trackTimeSeekbar);
        }
    }


    public void setLayout(int selectedSong) {
        Picasso.with(getActivity().getBaseContext()).load(trackList.get(selected).album.images.get(0).url).into(songImage);
        trackName.setText(trackList.get(selected).name);
        trackAlbum.setText(trackList.get(selected).album.name);

        String artists = "";
        for(int i=0; i<trackList.get(selected).artists.size(); i++)artists+=trackList.get(selected).artists.get(i).name+" , ";
        artists = artists.substring(0, artists.length() - 2);
        trackArtist.setText(artists);

        if (selectedSong==0){
            previous.setClickable(false);
            next.setClickable(true);
            previous.setVisibility(View.INVISIBLE);
            next.setVisibility(View.VISIBLE);
        }else if(selected == trackList.size() - 1){
            next.setClickable(false);
            previous.setClickable(true);
            next.setVisibility(View.INVISIBLE);
            previous.setVisibility(View.VISIBLE);
        }else{
            previous.setClickable(true);
            next.setClickable(true);
            previous.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setOnDismissListener(null);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        /*stopService(music);*/
        if (bounded) {
            getActivity().unbindService(connection);
            bounded = false;
        }
        super.onStop();
    }

    public void updateSeekbar(){
        handler.postDelayed(mUpdateSeekbar,100);
    }

    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            if (bounded) {
                seekBar.setMax(service.maxDuration());
                position = service.currentPosition();
                seekBar.setProgress(position);
                trackElapsedTime.setText("0:" +service.currentPosition()/1000);
                int timeLeft= (service.maxDuration()- service.currentPosition()) /1000;
                trackLeftTime.setText("0:" + timeLeft);
                handler.postDelayed(mUpdateSeekbar, 100);
            }
        }
    };
}