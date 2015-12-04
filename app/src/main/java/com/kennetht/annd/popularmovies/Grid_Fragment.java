package com.kennetht.annd.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Movie;
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
    private ArrayList<MovieObject> globalMoviesList;

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

        globalMoviesList = new ArrayList<MovieObject>();
        if(savedInstanceState != null) {
            globalMoviesList = savedInstanceState.getParcelableArrayList("key");
        }
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

        if(globalMoviesList.size() > 0) {
            globalMovies = new MovieAdapter(
                    getActivity(),
                    R.layout.image_movie,
                    R.id.movie_imageview,
                    globalMoviesList
            );
        } else {
            globalMovies = new MovieAdapter(
                    getActivity(),
                    R.layout.image_movie,
                    R.id.movie_imageview,
                    new ArrayList<MovieObject>()
            );
        }

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

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putParcelableArrayList("key", globalMoviesList);
        super.onSaveInstanceState(out);
    }
/*
*   Following is the background task for getting data from the network
*
*/
    public class FetchMovieData extends AsyncTask<String, Void, CompleteMovieContainer> {
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
                            //MovieID
                            obj.getString("id"),
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
                }
            }
           // Log.v(LOG_TAG,"Size of MovieList: " + MovieList.size());
            return MovieList;
        }

        public ArrayList<MovieTrailers> parseTrailerData(String dataJSON,
                       String movieID, ArrayList<MovieTrailers> mT) throws JSONException {

            if( dataJSON != null && movieID != null) {

                JSONObject initial = new JSONObject(dataJSON);
                JSONArray results = initial.getJSONArray("results");
                for(int i = 0; i < results.length(); i++) {
                    JSONObject obj = results.getJSONObject(i);
                    final String video_ID = obj.getString("id");
                    final String video_KEY = obj.getString("key");
                    final String video_NAME = obj.getString("name");

                    MovieTrailers m = new MovieTrailers(movieID, video_KEY,video_NAME);
                    mT.add(m);
                }

                return mT;
            }
            return null;
        }

    public ArrayList<MovieReviews> parseReviewData(String dataJSON,
                   String movieID, ArrayList<MovieReviews> mR) throws JSONException {

        if( dataJSON != null && movieID != null) {

            JSONObject initial = new JSONObject(dataJSON);
            JSONArray results = initial.getJSONArray("results");
            for(int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);
                final String review_ID = obj.getString("id");
                final String review_CONTENT = obj.getString("content");
                final String review_AUTHOR = obj.getString("author");

                MovieReviews m = new MovieReviews(movieID, review_AUTHOR, review_CONTENT);
                mR.add(m);
            }

            return mR;
        }
        return null;
    }

        /*
        *   universal method of getting json data from a given uri
        */
        public String makingConnection(Uri uri) {
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
                //Log.v(LOG_TAG, movieJSON);

                return movieJSON;

            } catch(IOException e) {
                Log.e(LOG_TAG, "Invalid URL at uri = " + uri.toString());

            } finally {
                if (connection != null) {
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


        public ArrayList<MovieObject> discoverMovies(String sortType) {
            String authorityStr = "http://api.themoviedb.org/3/discover/movie";
            final String SORT_QUERY = "sort_by";
            String SORT_QUERY_TYPE = null;
            final String API_KEY = "api_key";
            //After implementing settings, change the sort query type when needed
            //default is popularity.desc
            SORT_QUERY_TYPE = "popularity.desc";
            if (sortType == getString(R.string.vote_avg_desc)) {
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
              Log.v(LOG_TAG, "Initial Movie Fetch =" + uri.toString());
            String movieJSON = makingConnection(uri);
            if(movieJSON != null) {
                try {
                    return parseMovieJSON(movieJSON);
                } catch (JSONException e) {
                }
            }
            return null;
        }

        public ArrayList<MovieTrailers> insertTrailerDatabase(ArrayList<MovieObject> data) throws JSONException {

            String authorityString = "http://api.themoviedb.org/3/movie";
            String movieID = null;
            final String VIDEOS = "videos";
            final String API_KEY = "api_key";

            if (data != null) {

                ArrayList<MovieTrailers> mT = new ArrayList<>();
                for(int i = 0; i < data.size(); i++) {
                    movieID = data.get(i).getMovieID();

                    Uri fetchTrailerUri = Uri.parse(authorityString).buildUpon()
                            .appendPath(movieID)
                            .appendPath(VIDEOS)
                            .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
                            .build();

                    Log.v("TRAILER URI", fetchTrailerUri.toString());

                    String movieJSON = makingConnection(fetchTrailerUri);
                    mT = parseTrailerData(movieJSON, movieID, mT);
                }

                return mT;
            }
            return null;
        }

        public ArrayList<MovieReviews> insertReviewDatabase(ArrayList<MovieObject> data) throws JSONException {

            String authorityString = "http://api.themoviedb.org/3/movie";
            String movieID = null;
            final String REVIEWS = "reviews";
            final String API_KEY = "api_key";

            if (data != null) {

                ArrayList<MovieReviews> mR = new ArrayList<>();
                for(int i = 0; i < data.size(); i++) {
                    movieID = data.get(i).getMovieID();

                    Uri fetchReviewUri = Uri.parse(authorityString).buildUpon()
                            .appendPath(movieID)
                            .appendPath(REVIEWS)
                            .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
                            .build();

                    Log.v("REVIEW URI", fetchReviewUri.toString());

                    String movieJSON = makingConnection(fetchReviewUri);
                    mR = parseReviewData(movieJSON, movieID, mR);
                }

                return mR;
            }
            return null;
        }

        public CompleteMovieContainer doInBackground(String... params) {
            ArrayList<MovieObject> discoveredMovies = discoverMovies(params[0]);
            ArrayList<MovieTrailers> mTrailers = new ArrayList<MovieTrailers>();
            ArrayList<MovieReviews> mReviews = new ArrayList<MovieReviews>();
            if(discoveredMovies != null) {
                try {
                    mTrailers = insertTrailerDatabase(discoveredMovies);
                    mReviews = insertReviewDatabase(discoveredMovies);
                } catch (JSONException e) {

                }

                CompleteMovieContainer CMC = new CompleteMovieContainer(discoveredMovies, mTrailers, mReviews);

                return CMC;
            }
                return null;
        }

        @Override
        protected void onPostExecute(CompleteMovieContainer CMC) {

            if(CMC != null) {
                globalMovies.clear();

                ArrayList<MovieObject> m = CMC.getMovies();
                ArrayList<MovieTrailers> mT = CMC.getMovieTrailers();
               // ArrayList<MovieReviews> mR = CMC.getMovieReviews();

                if(m.size() > 0) {
                    for(int i = 0; i < m.size(); i++) {
                        globalMovies.add(m.get(i));
                    }

                    globalMoviesList = (ArrayList<MovieObject>) globalMovies.getMovieList();
                }
            }
            /*
            if(movies != null) {
                globalMovies.clear();
                globalMoviesList.clear();
                if (movies.size() > 0) {
                    for(int i = 0; i < movies.size(); i++) {
                        globalMovies.add(movies.get(i));
                    }
                }
                globalMoviesList = (ArrayList<MovieObject>) globalMovies.getMovieList();

            }
            */
            //Log.v(LOG_TAG, "GlobalMovies ArrayList size is now: " + globalMovies.size());
        }
    }
}
