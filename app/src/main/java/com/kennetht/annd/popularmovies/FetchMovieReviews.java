package com.kennetht.annd.popularmovies;

import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.kennetht.annd.popularmovies.MovieContainers.MovieObject;
import com.kennetht.annd.popularmovies.MovieContainers.MovieReviews;

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

/**
 * Created by Kenneth on 12/9/2015.
 */
public class FetchMovieReviews extends AsyncTask<String, Void, ArrayList<MovieReviews>> {

    public static final String LOG_TAG = FetchMovieReviews.class.getSimpleName();

    private callbackReviews callbackMR;
    public FetchMovieReviews() {
        callbackMR = null;
    }

    public FetchMovieReviews(callbackReviews cMR) {
        callbackMR = cMR;
    }

    public ArrayList<MovieReviews> insertReviewDatabase(String movieID) throws JSONException {

        String authorityString = "http://api.themoviedb.org/3/movie";
        final String REVIEWS = "reviews";
        final String API_KEY = "api_key";

        if (movieID != null) {

                Uri fetchReviewUri = Uri.parse(authorityString).buildUpon()
                        .appendPath(movieID)
                        .appendPath(REVIEWS)
                        .appendQueryParameter(API_KEY, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

              //  Log.v("REVIEW URI", fetchReviewUri.toString());

                String movieJSON = makingConnection(fetchReviewUri);
                return parseReviewData(movieJSON, movieID);
        }
        return null;
    }

    public ArrayList<MovieReviews> parseReviewData(String dataJSON,
                                                   String movieID) throws JSONException {

        if( dataJSON != null && movieID != null) {
            ArrayList<MovieReviews> mReviews = new ArrayList<>();

            JSONObject initial = new JSONObject(dataJSON);
            JSONArray results = initial.getJSONArray("results");
            for(int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);
                final String review_ID = obj.getString("id");
                final String review_CONTENT = obj.getString("content");
                final String review_AUTHOR = obj.getString("author");

                MovieReviews m = new MovieReviews(movieID, review_AUTHOR, review_CONTENT);
                mReviews.add(m);
            }
            return mReviews;
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
    public ArrayList<MovieReviews> doInBackground(String... params) {
        String movieID = params[0];
        if(movieID != null) {
            try {
                return insertReviewDatabase(movieID);
            } catch (JSONException e) {
                Log.v(LOG_TAG, "JSON Exception");
            }
        }
        return null;
    }

    @Override
    public void onPostExecute(ArrayList<MovieReviews> reviews) {
        if(reviews != null) {
            callbackMR.onResultR(reviews);
        }
    }

    public interface callbackReviews {
        void onResultR(ArrayList<MovieReviews> mR);
    }
}
