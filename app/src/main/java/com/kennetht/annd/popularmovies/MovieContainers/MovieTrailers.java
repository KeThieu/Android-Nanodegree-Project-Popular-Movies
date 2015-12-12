package com.kennetht.annd.popularmovies.MovieContainers;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kenneth on 12/3/2015.
 */
public class MovieTrailers implements Parcelable{

    private String movieID;
    private String videoKey;
    private String videoName;


    public MovieTrailers() {
        //default constructor
        this.videoKey = null;
        this.videoName = null;
        this.movieID = null;
    }

    public MovieTrailers(String movieID, String videoKey, String videoName) {
        this.movieID = movieID;
        this.videoKey = videoKey;
        this.videoName = videoName;
    }

    //Getter methods
    public String getMovieID() {return this.movieID;}

    public String getVideoKey() {return this.videoKey;}

    public String getVideoName() {return this.videoName;}


    //Parcelable needed implementations
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeString(this.movieID);
        outParcel.writeString(this.videoKey);
        outParcel.writeString(this.videoName);
    }

    public static final Parcelable.Creator<MovieTrailers> CREATOR
            = new Parcelable.Creator<MovieTrailers>() {
        public MovieTrailers createFromParcel(Parcel in) {
            return new MovieTrailers(in);
        }

        public MovieTrailers[] newArray(int size) {
            return new MovieTrailers[size];
        }
    };

    private MovieTrailers(Parcel in) {
        movieID = in.readString();
        videoKey = in.readString();
        videoName = in.readString();
    }
}
