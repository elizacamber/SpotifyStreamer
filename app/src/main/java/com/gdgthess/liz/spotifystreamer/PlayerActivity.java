package com.gdgthess.liz.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class PlayerActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            Bundle arguments = getIntent().getExtras();
            PlayerFragment fragment = new PlayerFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.player_fragment_container, fragment)
                    .commit();
        }

        //set the artists' name as the Title of the actionBar
        int position = Top10Fragment.getSelectedTrack();
        getSupportActionBar().setTitle(Top10Fragment.trackList.get(position).artists.get(0).name);

    }
}
