package com.gdgthess.liz.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class Top10Fragment extends android.support.v4.app.Fragment {

    public String artistID;
    public static List<Track> trackList;
    public ListView trackListView;
    public String location;
    public TrackAdapter mTrackAdapter;
    public SharedPreferences prefs;
    SharedPreferences sharedpreferences;
    boolean mTwoPane;
    public List<String> tracksUrl;

    public static int selectedTrack;
    public static PlayerFragment newFragment;

    public Top10Fragment() {
    }

    public Top10Fragment(String artistID){
        this.artistID = artistID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        sharedpreferences = getActivity().getSharedPreferences("TabletMode", Context.MODE_PRIVATE);
        mTwoPane = sharedpreferences.getBoolean("isTablet",false);

        if(!mTwoPane){
            Intent intent = getActivity().getIntent();
            artistID = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        trackList=new ArrayList<Track>();
        mTrackAdapter=new TrackAdapter(trackList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootview= inflater.inflate(R.layout.fragment_top10, container, false);

        trackListView=(ListView)rootview.findViewById(R.id.list_view_top10);
        trackListView.setAdapter(mTrackAdapter);
        trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Track track = (Track) (trackListView.getAdapter()).getItem(position);
                Log.d("Track", track.name + " was clicked " + track.id);

                setSelectedTrack(position);
                showPlayerFragment();
            }
        });

        SearchTracks task=new SearchTracks();
        try {
            task.execute(artistID);
        }catch (RetrofitError error){
            Toast.makeText(getActivity(), getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
        return rootview;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        SearchTracks task=new SearchTracks();
//        task.execute(artistID);
//    }

    public void setSelectedTrack(int track){
        selectedTrack = track;
    }
    static public int getSelectedTrack(){
        return selectedTrack;
    }

    public void showPlayerFragment() {

        FragmentManager fragmentManager = getFragmentManager();
        if(newFragment == null) newFragment = new PlayerFragment();

        if (mTwoPane) {
            newFragment.show(fragmentManager, "dialog");
        } else {
            Intent intent = new Intent(getActivity() , PlayerActivity.class);
            startActivity(intent);
        }

    }

    public class SearchTracks extends AsyncTask<String, Void, List<Track>>{

        @Override
        protected List<Track> doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Map<String, Object> mMap=new HashMap<String, Object>();
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            mMap.put("country",location);
            Tracks result=null;
            try {
                result = spotify.getArtistTopTrack(artistID, mMap);
            }catch (RetrofitError e){
                Log.e("Error loading tracks", e+"");
                return null;
            }
            return result.tracks;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            if(tracks.isEmpty())
                Toast.makeText(getActivity(),getString(R.string.track_error), Toast.LENGTH_LONG).show();

            trackList.clear();
            trackList.addAll(tracks);

            mTrackAdapter.notifyDataSetChanged();
        }
    }

    public class TrackAdapter extends ArrayAdapter<Track>{

        public TrackAdapter(List<Track> tracks) {
            super(getActivity(),0,tracks);
        }
        private class viewHolder{
            TextView TrackName;
            TextView AlbumName;
            ImageView TrackPic;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v =convertView;
            viewHolder vh;
            if(v==null){
                v= getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_top10, null);
                vh= new viewHolder();
                vh.TrackName=(TextView) v.findViewById(R.id.list_item_top10_track);
                vh.AlbumName=(TextView) v.findViewById(R.id.list_item_top10_album);
                vh.TrackPic=(ImageView) v.findViewById(R.id.list_item_top10_img);
                v.setTag(vh);
            }else
                vh=(viewHolder) v.getTag();

            Track track= trackList.get(position);
            if (track!=null){
                vh.TrackName.setText(trackList.get(position).name);
                vh.AlbumName.setText(trackList.get(position).album.name);
                    if(trackList.get(position).album.images.size()!=0){
                        Picasso.with(getContext()).load(trackList.get(position).album.images.get(0).url).into(vh.TrackPic);
                    }
            }
            return v;
        }
    }
}
