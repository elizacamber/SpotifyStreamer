package com.gdgthess.liz.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public EditText search_txt;
    public ImageButton search_btn;
    public ListView artistListView;
    public ArtistAdapter mArtistAdapter;
    public List<Artist> artistList;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artistList =new ArrayList<Artist>();
        mArtistAdapter=new ArtistAdapter(artistList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView=inflater.inflate(R.layout.fragment_main, container, false);
        search_txt=(EditText)rootView.findViewById(R.id.search_txt);
        search_btn=(ImageButton)rootView.findViewById(R.id.search_btn);
        artistListView =(ListView)rootView.findViewById(R.id.list_view_artist);
        artistListView.setAdapter(mArtistAdapter);

        artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = (Artist) (artistListView.getAdapter()).getItem(position);
                Log.d("Artist", artist.name + " was clicked " + artist.id);
                Intent intent = new Intent(getActivity(), Top10.class);
                intent.putExtra(Intent.EXTRA_TEXT, artist.id);
                startActivity(intent);
            }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String searchString = search_txt.getText().toString();
                Toast.makeText(getActivity(), getString(R.string.search) + " " + searchString, Toast.LENGTH_SHORT).show();
                SearchArtist task = new SearchArtist();
                task.execute(searchString);
            }
        });

        search_txt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    String searchString = search_txt.getText().toString();
                    Toast.makeText(getActivity(), getString(R.string.search) + " " + searchString, Toast.LENGTH_SHORT).show();
                    SearchArtist task = new SearchArtist();
                    task.execute(searchString);
                }
                return false;
            }
        });
        return rootView;
    }

    public class SearchArtist extends AsyncTask<String, Void, List<Artist>> {

        @Override
        protected List<Artist> doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(search_txt.getText().toString());
            Pager<Artist> artistsPager = results.artists;
            List<Artist> list = artistsPager.items;
            return list;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            if(artists!=null)
                mArtistAdapter.clear();
            if (artists.isEmpty())
                Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_LONG).show();
            artistList.addAll(artists);
        }
    }

    public class ArtistAdapter extends ArrayAdapter<Artist> {

        public ArtistAdapter(List<Artist> artists) {
            super(getActivity(),0,artists);
        }

        private class viewHolder{
        TextView ArtistName;
        ImageView ArtistPic;
    }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v =convertView;
            viewHolder vh;
                if(v==null){
                    v= getActivity().getLayoutInflater()
                            .inflate(R.layout.list_item_main, null);
                    vh= new viewHolder();
                    vh.ArtistName=(TextView) v.findViewById(R.id.list_item_artist_text);
                    vh.ArtistPic=(ImageView) v.findViewById(R.id.list_item_artist_img);
                    v.setTag(vh);
                }else
                    vh=(viewHolder) v.getTag();

            Artist artist= artistList.get(position);

                if (artist!=null){
                    vh.ArtistName.setText(artistList.get(position).name);
                        if(artistList.get(position).images.size()!=0){
                            Picasso.with(getContext()).load(artistList.get(position).images.get(0).url).into(vh.ArtistPic);
                        }
                }
            return v;
        }
    }
}
