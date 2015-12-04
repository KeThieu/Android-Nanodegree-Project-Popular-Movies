package com.kennetht.annd.popularmovies;

import android.graphics.Movie;

import java.util.ArrayList;

/**
 * This class is meant to hold ArrayList of Parcelable MovieObjects, MovieTrailers, and MovieReviews
 */
public class CompleteMovieContainer {

    private ArrayList<MovieObject> movies;
    private ArrayList<MovieTrailers> movieTrailers;
    private ArrayList<MovieReviews> movieReviews;

    public CompleteMovieContainer(ArrayList<MovieObject> m, ArrayList<MovieTrailers> mT, ArrayList<MovieReviews> mR) {
        movies = new ArrayList<MovieObject>();
        movieTrailers = new ArrayList<MovieTrailers>();
        movieReviews = new ArrayList<MovieReviews>();

        if(m != null && mT != null) {
            //iterate through all parameters and add to the arrayList here
            movies.addAll(m);
            movieTrailers.addAll(mT);
           // movieReviews.addAll(mR);
        }
    }

    //Getter methods
    public ArrayList<MovieObject> getMovies() {return movies;}

    public ArrayList<MovieTrailers> getMovieTrailers() {return movieTrailers;}

    public ArrayList<MovieReviews> getMovieReviews() {return movieReviews;}

}
