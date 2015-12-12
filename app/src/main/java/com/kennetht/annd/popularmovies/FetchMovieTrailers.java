package com.kennetht.annd.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.kennetht.annd.popularmovies.MovieContainers.MovieTrailers;

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

public class FetchMovieTrailers extends AsyncTask<String, Void, ArrayList<MovieTrailers>> {

    private final String LOG_TAG = FetchMovieTrailers.class.getSimpleName();
    private callbackTrailers callbackMT;

    public FetchMovieTrailers() {
        this.callbackMT = null;
    }

    public FetchMovieTrailers(callbackTrailers cMT) {
        this.callbackMT = cMT;
    }

    public ArrayList<MovieTrailers> fetchMovieTrailers(String movieID) throws JSONException {

        String authorityString = "http://api.themoviedb.org/3/movie";
        final String VIDEOS = "videos";
        final String API_KEY = "api_key";

        if (movieID != null) {

            Uri fetchTrailerUri = Uri.parse(authorityString).buildUpon()
                    .appendPath(movieID)
                    .appendPath(VIDEOS)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
                    .build();

            Log.v("TRAILER URI", fetchTrailerUri.toString());
            String movieJSON = makingConnection(fetchTrailerUri);
            return parseTrailerData(movieJSON, movieID);

        }
        return null;
    }

    public ArrayList<MovieTrailers> parseTrailerData(String dataJSON, String movieID) throws JSONException {

        if( dataJSON != null && movieID != null) {
            ArrayList<MovieTrailers> mTrailers = new ArrayList<>();

            JSONObject initial = new JSONObject(dataJSON);
            JSONArray results = initial.getJSONArray("results");
            for(int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);
                final String video_ID = obj.getString("id");
                final String video_KEY = obj.getString("key");
                final String video_NAME = obj.getString("name");

                MovieTrailers m = new MovieTrailers(movieID, video_KEY,video_NAME);
                mTrailers.add(m);
            }

            return mTrailers;
        }
        return null;
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


    @Override
    public ArrayList<MovieTrailers> doInBackground(String... params) {
        String movieID = params[0];
        try {
            return fetchMovieTrailers(movieID);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Json_Exception");
        }
        return null;
    }

    @Override
    public void onPostExecute(ArrayList<MovieTrailers> trailers) {
        if(trailers != null) {
            callbackMT.onResultT(trailers);
        }
    }

    public interface callbackTrailers {
        void onResultT(ArrayList<MovieTrailers> mT);
    }
}