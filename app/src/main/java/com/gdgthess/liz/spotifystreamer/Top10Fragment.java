package com.gdgthess.liz.spotifystreamer;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * A placeholder fragment containing a simple view.
 */
public class Top10Fragment extends Fragment {

    public String artistID;
    public List<Track> trackList;
    public ListView trackListView;
    public String location;
    public TrackAdapter mTrackAdapter;
    public SharedPreferences prefs;

    public Top10Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getActivity().getIntent();
        artistID=intent.getStringExtra(Intent.EXTRA_TEXT);
        trackList=new ArrayList<Track>();
        mTrackAdapter=new TrackAdapter(trackList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootview= inflater.inflate(R.layout.fragment_top10, container, false);
        trackListView=(ListView)rootview.findViewById(R.id.list_view_top10);
        trackListView.setAdapter(mTrackAdapter);
        SearchTracks task=new SearchTracks();
        task.execute(artistID);
        return rootview;
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
            Tracks result=spotify.getArtistTopTrack(artistID, mMap);
            return result.tracks;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            if(tracks.isEmpty())
                Toast.makeText(getActivity(),getString(R.string.track_error), Toast.LENGTH_LONG).show();
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
