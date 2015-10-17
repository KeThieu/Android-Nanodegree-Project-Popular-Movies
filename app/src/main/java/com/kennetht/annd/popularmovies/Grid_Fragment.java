package com.kennetht.annd.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Grid_Fragment extends Fragment {

    private MovieAdapter globalMovies;

    public Grid_Fragment() {
    }

    private void updateMovies() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortUnit = sharedPreferences.getString(getString(R.string.sortByKey), getString(R.string.popularity_desc));
        new FetchMovieData().execute(sortUnit);
    }

    @Override
    public void onStart() {
        super.onStart();

        updateMovies();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_grid_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menu_id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        globalMovies = new MovieAdapter(
                getActivity(),
                R.layout.image_movie,
                R.id.movie_imageview,
                new ArrayList<MovieObject>()
        );

        GridView grid = (GridView)rootView.findViewById(R.id.grid_movies);
        grid.setAdapter(globalMovies);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MovieObject movieObject = (MovieObject) adapterView.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
                intent.putExtra("MovieObject", movieObject);
                startActivity(intent);
            }
        });

        return rootView;
    }


/*
*   Following is the background task for getting data from the network
*
*/
    public class FetchMovieData extends AsyncTask<String, Void, ArrayList<MovieObject>> {
        private final String LOG_TAG = FetchMovieData.class.getSimpleName();

        public ArrayList<MovieObject> parseMovieJSON(String dataJSON) throws JSONException {
            //take the argument and parse them into JSON Objects to get the info we need
            ArrayList<MovieObject> MovieList = new ArrayList<MovieObject>();
            JSONObject initial = new JSONObject(dataJSON);
            JSONArray results = initial.getJSONArray("results");
            for(int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);
                if(obj != null) {
                    MovieObject movieObj = new MovieObject(
                            //Title
                            obj.getString("original_title"),
                            //Poster_path
                            obj.getString("poster_path"),
                            //Overview
                            obj.getString("overview"),
                            //vote_average
                            obj.getDouble("vote_average"),
                            //release date
                            obj.getString("release_date")
                    );
                    MovieList.add(movieObj);
                   /* Log.v(LOG_TAG, "Title: " + movieObj.getTitle() + "\nPoster Path: " + movieObj.getPoster() +
                            "\nOverview: " + movieObj.getOverview() + "\nRating: " + movieObj.getRating() +
                            "\nReleaseDate: " + movieObj.getRelease());
                   */
                }
            }
           // Log.v(LOG_TAG,"Size of MovieList: " + MovieList.size());
            return MovieList;
        }

        public ArrayList<MovieObject> doInBackground(String... params) {
            String authorityStr = "http://api.themoviedb.org/3/discover/movie";
            final String SORT_QUERY = "sort_by";
            String SORT_QUERY_TYPE = null;
            final String API_KEY = "api_key";

            //After implementing settings, change the sort query type when needed
            //default is popularity.desc
            SORT_QUERY_TYPE = "popularity.desc";

            if (params[0] == getString(R.string.vote_avg_desc)) {
                SORT_QUERY_TYPE = "vote_average.desc";
            }

            /*
            *   To the Udacity Reviewer. When you use your own API Key for reviewing my code
            *   replace the following line within the Uri building process
            *   .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
            *   with
            *   .appendQueryParameter(API_KEY, "YOUR API KEY HERE")
            *
             */
            Uri uri = Uri.parse(authorityStr)
                    .buildUpon()
                    .appendQueryParameter(SORT_QUERY, SORT_QUERY_TYPE)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
                    .build();

           // Log.v(LOG_TAG, uri.toString());

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(uri.toString());

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) return null;

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if(buffer.length() == 0) return null;

                String movieJSON = buffer.toString();
            //    Log.v(LOG_TAG, movieJSON);

                try {
                    return parseMovieJSON(movieJSON);

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Cannot parse JSON");
                }

            }catch(IOException e) {
                Log.e(LOG_TAG, "Invalid URL");
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieObject> movies) {
            if(movies != null) {
                globalMovies.clear();
                if (movies.size() > 0) {
                    for(int i = 0; i < movies.size(); i++) {
                       globalMovies.add(movies.get(i));
                    }
                }
            }
            //Log.v(LOG_TAG, "GlobalMovies ArrayList size is now: " + globalMovies.size());
        }
    }
}
