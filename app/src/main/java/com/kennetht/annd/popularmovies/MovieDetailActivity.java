package com.kennetht.annd.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MovieDetailActivityFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public static class MovieDetailActivityFragment extends Fragment {

        private final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();

        public MovieDetailActivityFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
            Intent receivedIntent =getActivity().getIntent();
            if (receivedIntent != null && receivedIntent.hasExtra("MovieObject")) {
                MovieObject receivedMovie = receivedIntent.getExtras().getParcelable("MovieObject");
                Log.v(LOG_TAG, "Movie Title: " + receivedMovie.getTitle());

                //Reuse the same URI code for the poster path as MovieAdapter
                String posterPath = receivedMovie.getPoster();
                final String baseURL = "http://image.tmdb.org/t/p/w185";

                Uri uri = Uri.parse(baseURL)
                        .buildUpon()
                        .appendEncodedPath(posterPath)
                        .build();

                //Setting everything within the xml file
                ((TextView) rootView.findViewById(R.id.detail_movieTitle)).setText(receivedMovie.getTitle());
                Picasso.with(getActivity()).load(uri).into((ImageView)rootView.findViewById(R.id.detail_moviePoster));
                ((TextView) rootView.findViewById(R.id.detail_overview)).setText("Overview: " + receivedMovie.getOverview());
                ((TextView) rootView.findViewById(R.id.detail_rating)).setText("Rating: " + (receivedMovie.getRating()).toString());
                ((TextView) rootView.findViewById(R.id.detail_releaseDate)).setText("Release Date: " + receivedMovie.getRelease());
            }
            return rootView;
        }
    }
}
