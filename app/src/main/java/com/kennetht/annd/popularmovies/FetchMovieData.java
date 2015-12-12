package com.kennetht.annd.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;

import com.kennetht.annd.popularmovies.MovieContainers.MovieObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/*
*   Following is the background task for getting data from the network
*
*/
public class FetchMovieData extends AsyncTask<String, Void, ArrayList<MovieObject>> {
    private final String LOG_TAG = FetchMovieData.class.getSimpleName();

    private callbackMovieFetch mCallbackMovieFetch;

    public FetchMovieData() {
        mCallbackMovieFetch = null;
    }

    public FetchMovieData(callbackMovieFetch mCMF) {
        mCallbackMovieFetch = mCMF;
    }

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

        /*
        *   universal method of getting json data from a given uri
        */
    public String makingConnection(Uri uri) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        int responseCode = 0;
        try {
            URL url = new URL(uri.toString());

            connection = (HttpURLConnection) url.openConnection();
            //connection.setRequestMethod("GET");
            connection.connect();

            responseCode = connection.getResponseCode();

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

        } catch (MalformedURLException m) {
            Log.e(LOG_TAG, "MalformedURLException occurred");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Invalid URL at uri = " + uri.toString() + " response Code = " + responseCode);

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
        final String SORT_VOTE_COUNT = "vote_count.gte";
        final String API_KEY = "api_key";

        //default is popularity.desc

        SORT_QUERY_TYPE = "popularity.desc";
        if (sortType.equals("vote_average_desc")) {
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
        Uri uri;

        if(sortType.equals("popularity_desc")) {
            uri = Uri.parse(authorityStr)
                    .buildUpon()
                    .appendQueryParameter(SORT_QUERY, SORT_QUERY_TYPE)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
        } else {
            uri = Uri.parse(authorityStr)
                    .buildUpon()
                    .appendQueryParameter(SORT_QUERY, SORT_QUERY_TYPE)
                    .appendQueryParameter(SORT_VOTE_COUNT, "100")
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
                    .build();
        }

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

    public ArrayList<MovieObject> doInBackground(String... params) {

        ArrayList<MovieObject> discoveredMovies = discoverMovies(params[0]);
        if(discoveredMovies != null) {
            Log.v(LOG_TAG, "Size of discoveredMovies = " + discoveredMovies.size());
            return discoveredMovies;
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<MovieObject> movies) {
        if(movies != null && mCallbackMovieFetch != null) {
            mCallbackMovieFetch.onResultFetch(movies);
        }
    }

    public interface callbackMovieFetch {
        void onResultFetch(ArrayList<MovieObject> mO);
    }
}