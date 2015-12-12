package com.kennetht.annd.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kennetht.annd.popularmovies.Data.FavoritesColumns;
import com.kennetht.annd.popularmovies.Data.MovieProvider;
import com.kennetht.annd.popularmovies.MovieContainers.MovieObject;
import com.kennetht.annd.popularmovies.MovieContainers.MovieReviews;
import com.kennetht.annd.popularmovies.MovieContainers.MovieTrailers;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MovieDetailActivityFragment extends Fragment implements FetchMovieTrailers.callbackTrailers, FetchMovieReviews.callbackReviews {

    private final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();

    private ViewGroup layoutContainer;

    public MovieDetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        layoutContainer = container;

        Intent receivedIntent = getActivity().getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra("MovieObject")) {
            final MovieObject receivedMovie = receivedIntent.getExtras().getParcelable("MovieObject");

            //Reuse the same URI code for the poster path as MovieAdapter
            String posterPath = receivedMovie.getPoster();
            final String baseURL = "http://image.tmdb.org/t/p/w185";

            Uri uri = Uri.parse(baseURL)
                    .buildUpon()
                    .appendEncodedPath(posterPath)
                    .build();

            //Setting everything within the xml file
            ((TextView) rootView.findViewById(R.id.detail_movieTitle)).setText(receivedMovie.getTitle());
            Picasso.with(getActivity()).load(uri).into((ImageView) rootView.findViewById(R.id.detail_moviePoster));
            ((TextView) rootView.findViewById(R.id.detail_overview)).setText(receivedMovie.getOverview());
            ((TextView) rootView.findViewById(R.id.detail_rating)).setText((receivedMovie.getRating()).toString() + " / 10");
            ((TextView) rootView.findViewById(R.id.detail_releaseDate)).setText(receivedMovie.getRelease());

            final Button favorite_button = (Button) rootView.findViewById(R.id.detail_favorite_button);
            final String movieID = receivedMovie.getMovieID();

            //Check if this movie is in the Content Provider
            if(queryFavoriteDatabase(movieID)) {
                favorite_button.setText("Remove from Favorites");
            } else {
                favorite_button.setText("Add to Favorites");
            }

            favorite_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if(queryFavoriteDatabase(movieID)) {
                        //remove from the database since its in it.
                        final int deleted = getActivity().getContentResolver()
                                .delete(MovieProvider.Favorites.CONTENT_URI,
                                        FavoritesColumns.MOVIE_ID + "=?",
                                        new String[] {movieID});

                        if(deleted > 0) {
                            Log.v(LOG_TAG, "Number of rows deleted: " + deleted);
                            favorite_button.setText("Add to Favorites");
                            if(!queryFavoriteDatabase(movieID)) {
                                Log.v(LOG_TAG, movieID + " is longer in the database");
                            }
                        }

                    } else {
                        //add into the database
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(FavoritesColumns.MOVIE_ID, movieID);
                        contentValues.put(FavoritesColumns.MOVIE_TITLE, receivedMovie.getTitle());
                        contentValues.put(FavoritesColumns.OVERVIEW, receivedMovie.getOverview());
                        contentValues.put(FavoritesColumns.POSTER_PATH, receivedMovie.getPoster());
                        contentValues.put(FavoritesColumns.RELEASE_DATE, receivedMovie.getRelease());
                        contentValues.put(FavoritesColumns.USER_RATING, receivedMovie.getRating());

                        Uri insertURI = getActivity().getContentResolver()
                                .insert(MovieProvider.Favorites.CONTENT_URI,
                                        contentValues);

                        if(!insertURI.equals(Uri.EMPTY)) {
                            Log.v(LOG_TAG, "Successful insert at URI: " + insertURI.toString());
                            favorite_button.setText("Remove from Favorites");
                        } else {
                            throw new android.database.SQLException("Failure to insert into uri: " + insertURI);
                        }
                    }
                }
            });

            //use MovieID to fetch Trailers and Reviews

            if(movieID != null) {
                new FetchMovieTrailers(this).execute(movieID);
                new FetchMovieReviews(this).execute(movieID);
            }

        }
        return rootView;
    }

    public boolean queryFavoriteDatabase(String movieID) {
        Cursor cursor = getActivity().getContentResolver()
                .query(MovieProvider.Favorites.CONTENT_URI, new String[] {FavoritesColumns.MOVIE_ID}, null, null, null);
        if(cursor.moveToFirst()) {
            String curMovieID = cursor.getString(cursor.getColumnIndex(FavoritesColumns.MOVIE_ID));
            if (movieID.equals(curMovieID)) {
                return true;
            } else {
                while (cursor.moveToNext()) {
                    curMovieID = cursor.getString(cursor.getColumnIndex(FavoritesColumns.MOVIE_ID));
                    if (movieID.equals(curMovieID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void createTrailerRows(ArrayList<MovieTrailers> mT) {
        //use this method to create and populate Trailer Views
        View movieTrailerRow;
        ViewGroup trailer_container;

        if(mT.size() > 0) {
            int count = 0;
            for (MovieTrailers m : mT) {
                if(count >= 2) break; //limit the number of trailers to up to 2
                final String video_key = m.getVideoKey();
                String video_name = m.getVideoName();

                movieTrailerRow = LayoutInflater.from(getActivity()).inflate(R.layout.trailer_movie, null);

                movieTrailerRow.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        launchYoutubeIntent(video_key);
                    }
                });

                ((TextView) movieTrailerRow.findViewById(R.id.trailer_movie_name)).setText(video_name);
                trailer_container = (ViewGroup) layoutContainer.findViewById(R.id.trailer_container);
                movieTrailerRow.setBackgroundResource(R.drawable.trailer_selector);
                trailer_container.addView(movieTrailerRow);

                count++;
            }
        } else {
            trailer_container = (ViewGroup) layoutContainer.findViewById(R.id.trailer_container);
            movieTrailerRow = LayoutInflater.from(getActivity()).inflate(R.layout.trailer_movie_none, null);
            trailer_container.addView(movieTrailerRow);

        }
    }


    /*
    *   Check if an app is installed, primarily youtube, so the phone can start the browser as an alternative
     */
    public boolean checkAppInstalled(String package_name) {
        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(package_name);
        if(intent != null) {
            return true;
        }
        return false;
    }

    public void launchYoutubeIntent(String video_key) {
        if(video_key != null) {
            Intent intent;
            if(checkAppInstalled("com.google.android.youtube")) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + video_key));
            } else {
                Uri browser_YT = Uri.parse("https://www.youtube.com/watch")
                        .buildUpon()
                        .appendQueryParameter("v",video_key)
                        .build();
                intent = new Intent(Intent.ACTION_VIEW, browser_YT);
            }
            startActivity(intent);
        }

    }

    public void createReviewRows(ArrayList<MovieReviews> mR) {
        //use this method to create and populate Trailer Views
        View movieReviewRow;
        ViewGroup review_container;
        if(mR.size() > 0) {
            int count = 0;
            for (MovieReviews m : mR) {
                if(count >= 2) break; //limit the number the reviews to up to 2
                final String author_name = m.getReviewAuthor();
                String review_contents = m.getReviewContents();

                movieReviewRow = LayoutInflater.from(getActivity()).inflate(R.layout.review_movie, null);

                ((TextView) movieReviewRow.findViewById(R.id.review_author_name)).setText("Reviewer Name : " + author_name);
                ((TextView) movieReviewRow.findViewById(R.id.review_content)).setText(" " + review_contents);
                review_container = (ViewGroup) layoutContainer.findViewById(R.id.review_container);
                review_container.addView(movieReviewRow);

                count++;
            }
        } else {
            review_container = (ViewGroup) layoutContainer.findViewById(R.id.review_container);
            movieReviewRow = LayoutInflater.from(getActivity()).inflate(R.layout.review_movie_none, null);
            review_container.addView(movieReviewRow);
        }
    }

    @Override
    public void onResultT(ArrayList<MovieTrailers> mT) {
        if(mT != null) {
            createTrailerRows(mT);
        }
    }

    @Override
    public void onResultR(ArrayList<MovieReviews> mR) {
        if(mR != null) {
            createReviewRows(mR);
        }
    }
}
