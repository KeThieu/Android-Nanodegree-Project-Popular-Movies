package com.kennetht.annd.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kenneth on 12/3/2015.
 */
public class MovieReviews implements Parcelable {

    private String movieID;
    private String reviewAuthor;
    private String reviewContents;


    public MovieReviews() {
        //default constructor
        this.reviewAuthor = null;
        this.reviewContents = null;
        this.movieID = null;
    }

    public MovieReviews(String movieID, String reviewAuthor, String reviewContents) {
        this.movieID = movieID;
        this.reviewAuthor = reviewAuthor;
        this.reviewContents = reviewContents;
    }

    //Getter methods
    public String getMovieID() {return this.movieID;}

    public String getReviewAuthor() {return this.reviewAuthor;}

    public String getReviewContents() {return this.reviewContents;}


    //Parcelable needed implementations
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeString(this.movieID);
        outParcel.writeString(this.reviewAuthor);
        outParcel.writeString(this.reviewContents);
    }

    public static final Parcelable.Creator<MovieReviews> CREATOR
            = new Parcelable.Creator<MovieReviews>() {
        public MovieReviews createFromParcel(Parcel in) {
            return new MovieReviews(in);
        }

        public MovieReviews[] newArray(int size) {
            return new MovieReviews[size];
        }
    };

    private MovieReviews(Parcel in) {
        movieID = in.readString();
        reviewAuthor = in.readString();
        reviewContents = in.readString();
    }
}

